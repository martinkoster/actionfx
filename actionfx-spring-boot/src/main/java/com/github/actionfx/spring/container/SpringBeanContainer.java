/*
 * Copyright (c) 2020 Martin Koster
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.actionfx.spring.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.container.AbstractActionFXBeanContainer;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.extension.ActionFXExtensionsBean;
import com.github.actionfx.core.utils.AnnotationUtils;

/**
 * Implementation of {@link BeanContainerFacade} that leverages Spring as
 * underlying bean container.
 * <p>
 * In order to be able to register additional Spring beans, we need access to
 * the {@link BeanDefinitionRegistry}, on top of the actual
 * {@link ApplicationContext} instance for retrieving beans from the Spring
 * context.
 * <p>
 * This container can be instantiated before the actual Spring context is
 * already available. That might be required as the bean container is setup
 * during the setup of ActionFX, while Spring might get started after that. It
 * is important, that the Spring application context is set into this bean
 * container via method
 * {@link #onSpringContextAvailable(BeanDefinitionRegistry, ApplicationContext)}
 * as soon as the Spring context is available.
 * <p>
 * Although the Spring context is not avaiable right from the beginning, it is
 * already possible to add bean definitions to this container. In this case, the
 * bean definitions are cached and executed against the "real" Spring
 * application context as soon as it is supplied.
 *
 * @author koster
 *
 */
public class SpringBeanContainer extends AbstractActionFXBeanContainer {

	private static final Logger LOG = LoggerFactory.getLogger(SpringBeanContainer.class);

	private BeanDefinitionRegistry beanDefinitionRegistry;

	private ApplicationContext applicationContext;

	// in case beans shall be added to this container before the Spring context is
	// available, bean definitions are cached here in the list
	private final List<AddBeanDefinitionCallback> addBeanDefinitionCallbacks = new ArrayList<>();

	/**
	 * Default constructor for instantiating the container without custom
	 * extensions.
	 */
	public SpringBeanContainer() {
		this(null);
	}

	/**
	 * Constructor that accepts custom ActionFX extensions (controller and beans
	 * extensions).
	 *
	 * @param extensionsBean the bean holding the extensions.
	 */
	public SpringBeanContainer(final ActionFXExtensionsBean extensionsBean) {
		super(extensionsBean);
	}

	/**
	 * Sets the {@link BeanDefinitionRegistry} for adding new bean definitions and
	 * the {@link ApplicationContext} for requesting beans from the context.
	 * <p>
	 * This method is invoked by {@link AFXApplicationContextInitializer} when the
	 * Spring context is starting up.
	 *
	 * @param beanDefinitionRegistry the bean definition registry
	 * @param applicationContext     the application context
	 */
	public void onSpringContextAvailable(final BeanDefinitionRegistry beanDefinitionRegistry,
			final ApplicationContext applicationContext) {
		this.beanDefinitionRegistry = beanDefinitionRegistry;
		this.applicationContext = applicationContext;

		// register beans that could not yet have been added to the "real" Spring
		// application context
		if (!addBeanDefinitionCallbacks.isEmpty()) {
			addBeanDefinitionCallbacks.forEach(AddBeanDefinitionCallback::addBeanDefinition);
			addBeanDefinitionCallbacks.clear();
		}
	}

	@Override
	public void runComponentScan(final String rootPackage) {
		checkSpringContextAvailability();

		// scan for AFXController annotations...
		final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(AFXController.class));
		final Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(rootPackage);

		for (final BeanDefinition beanDefinition : beanDefinitions) {
			if (beanDefinition == null) {
				continue;
			}
			final String beanClassName = beanDefinition.getBeanClassName();
			if (beanClassName == null) {
				continue;
			}
			final Class<?> beanClass = ClassUtils.resolveClassName(beanClassName, // NORSONAR
					getClass().getClassLoader());
			addControllerBeanDefinition(beanClass);
		}
	}

	@Override
	public void addBeanDefinition(final String id, final Class<?> beanClass, final boolean singleton,
			final boolean lazyInit, final Supplier<?> instantiationSupplier) {
		if (isSpringContextAvailable()) {
			addBeanDefinitionInternal(id, beanClass, singleton, lazyInit, instantiationSupplier);
		} else {
			addBeanDefinitionCallbacks
					.add(new AddBeanDefinitionCallback(id, beanClass, singleton, lazyInit, instantiationSupplier));
		}
	}

	private void addBeanDefinitionInternal(final String id, final Class<?> beanClass, final boolean singleton,
			final boolean lazyInit, final Supplier<?> instantiationSupplier) {
		final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(beanClass);
		beanDefinition.setScope(singleton ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
		beanDefinition.setLazyInit(lazyInit);
		beanDefinition.setInstanceSupplier(instantiationSupplier);
		registerBeanDefinition(id, beanDefinition);
		postProcessBeanDefinition(beanClass, id, singleton, lazyInit);
	}

	/**
	 * Registers the <code>BeanDefinition</code> with the Spring context.
	 *
	 * @param beanName    the bean name
	 * @param definition  the bean definition
	 * @param beanFactory the bean factory
	 */
	protected void registerBeanDefinition(final String beanName, final BeanDefinition definition) {
		checkSpringContextAvailability();
		beanDefinitionRegistry.registerBeanDefinition(beanName, definition);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Registered BeanDefinition of type '{}' with bean name '{}'.", definition.getBeanClassName(),
					beanName);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(final String id) {
		checkSpringContextAvailability();
		return (T) applicationContext.getBean(id);
	}

	@Override
	public <T> T getBean(final Class<T> beanClass) {
		checkSpringContextAvailability();
		return applicationContext.getBean(beanClass);
	}

	/**
	 * Resolves to a {@link ResourceBundle} for the given {@code controllerClass}.
	 * <p>
	 * The logic is as follows: In case the controller class does not hold a
	 * resource base name under {@link AFXController#resourcesBasename()}, it is
	 * expected that Spring's central {@link MessageSource} holds all
	 * internationalized texts. In that case we wrap the {@link MessageSource} in a
	 * {@link MessageSourceResourceBundle}, so that it can be accessed by JavaFX.
	 * <p>
	 * In case a base name is provided under
	 * {@link AFXController#resourcesBasename()}, then the regular
	 * {@link ResourceBundle} is returned as it would be in a non-Spring setup.
	 *
	 */
	@Override
	public ResourceBundle resolveResourceBundle(final Class<?> controllerClass, final Locale locale) {
		checkSpringContextAvailability();
		final MessageSource messageSource = applicationContext.getBean(MessageSource.class);
		final AFXController afxController = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);
		// in case there is no resourceBasename on the controller, we return Spring
		// message source as we expect that
		// it holds the messages
		if (afxController == null || "".equals(afxController.resourcesBasename())) {
			return new MessageSourceResourceBundle(messageSource, locale);
		}
		final String baseName = afxController.resourcesBasename();
		return ResourceBundle.getBundle(baseName, locale);
	}

	/**
	 * Checks that the Spring context is already available for usage. The container
	 * itself is instantiated while setting up ActionFX via its builder. Spring
	 * itself might be available later in the application's lifecycle.
	 *
	 * @throws IllegalStateException in case the Spring container is not available
	 */
	protected void checkSpringContextAvailability() {
		if (!isSpringContextAvailable()) {
			throw new IllegalStateException(
					"Spring context is not available yet! Please perform the desired operation after Spring is properly booted and the ApplicationContext is set via method SpringBeanContainer.onSpringContextAvailable(..)!");
		}
	}

	/**
	 * Checks whether the Spring context is already available for usage. The
	 * container itself is instantiated while setting up ActionFX via its builder.
	 * Spring itself might be available later in the application's lifecycle.
	 *
	 * @return {@code true}, if and only if the Spring context is available.
	 */
	protected boolean isSpringContextAvailable() {
		return applicationContext != null && beanDefinitionRegistry != null;
	}

	/**
	 * Callback for adding a bean definition to the Spring application callback as
	 * soon it is available.
	 *
	 * @author koster
	 *
	 */
	private class AddBeanDefinitionCallback {

		private final String id;

		private final Class<?> beanClass;

		private final boolean singleton;

		private final boolean lazyInit;

		private final Supplier<?> instantiationSupplier;

		public AddBeanDefinitionCallback(final String id, final Class<?> beanClass, final boolean singleton,
				final boolean lazyInit, final Supplier<?> instantiationSupplier) {
			this.id = id;
			this.beanClass = beanClass;
			this.singleton = singleton;
			this.lazyInit = lazyInit;
			this.instantiationSupplier = instantiationSupplier;
		}

		public void addBeanDefinition() {
			SpringBeanContainer.this.addBeanDefinition(id, beanClass, singleton, lazyInit, instantiationSupplier);
		}
	}

}
