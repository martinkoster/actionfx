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

import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.container.instantiation.ControllerInstantiationSupplier;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.View;

/**
 * Implementation of {@link BeanContainerFacade} that leverages Spring as
 * underlying bean container.
 * <p>
 * In order to be able to register additional Spring beans, we need access to
 * the {@link BeanDefinitionRegistry}, on top of the actual
 * {@link ApplicationContext} instance for retrieving beans from the Spring
 * context.
 *
 * @author koster
 *
 */
public class SpringBeanContainer implements BeanContainerFacade {

	private static Logger LOG = LoggerFactory.getLogger(SpringBeanContainer.class);

	private final BeanDefinitionRegistry beanDefinitionRegistry;

	private final ApplicationContext applicationContext;

	/**
	 * Constructor that requires access to the {@link BeanDefinitionRegistry} for
	 * adding new bean definitions and the {@link ApplicationContext} for requesting
	 * beans from the context.
	 *
	 * @param beanDefinitionRegistry the bean definition registry
	 * @param applicationContext     the application context
	 */
	public SpringBeanContainer(final BeanDefinitionRegistry beanDefinitionRegistry,
			final ApplicationContext applicationContext) {
		this.beanDefinitionRegistry = beanDefinitionRegistry;
		this.applicationContext = applicationContext;
	}

	@Override
	public void populateContainer(final String rootPackage) {

		// scan for FxController annotations...
		final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(AFXController.class));
		final Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(rootPackage);

		for (final BeanDefinition beanDefinition : beanDefinitions) {
			if (beanDefinition == null || beanDefinition.getBeanClassName() == null) {
				continue;
			}
			final Class<?> beanClass = ClassUtils.resolveClassName(beanDefinition.getBeanClassName(),
					getClass().getClassLoader());
			final AFXController afxController = AnnotationUtils.findAnnotation(beanClass, AFXController.class);

			final ControllerInstantiationSupplier<?> controllerSupplier = new ControllerInstantiationSupplier<>(
					beanClass);

			// add a bean definition for the controller
			final String controllerBeanId = deriveControllerId(beanClass);
			registerBeanDefinition(controllerBeanId,
					createBeanDefinitionForController(beanDefinition, afxController, controllerSupplier));

			// add a bean definition for the view
			registerBeanDefinition(afxController.viewId(), createBeanDefinitionForView(beanDefinition, afxController,
					() -> ControllerWrapper.getViewFrom(getBean(controllerBeanId))));
		}

	}

	@Override
	public void addBeanDefinition(final String id, final Class<?> beanClass, final boolean singleton,
			final Supplier<?> instantiationSupplier) {
		final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(beanClass);
		beanDefinition.setScope(singleton ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
		beanDefinition.setInstanceSupplier(instantiationSupplier);
		registerBeanDefinition(id, beanDefinition);
	}

	private BeanDefinition createBeanDefinitionForController(final BeanDefinition beanDefinition,
			final AFXController afxController, final Supplier<?> controllerInstantiationSupplier) {
		final GenericBeanDefinition result = new GenericBeanDefinition(beanDefinition);
		result.setScope(afxController.singleton() ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
		result.setInstanceSupplier(controllerInstantiationSupplier);
		return result;

	}

	private BeanDefinition createBeanDefinitionForView(final BeanDefinition beanDefinition,
			final AFXController afxController, final Supplier<?> viewSupplier) {
		// views follow the same rules than the controller (scope, dependsOn, etc.)
		final GenericBeanDefinition result = new GenericBeanDefinition(beanDefinition);
		result.setScope(afxController.singleton() ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
		result.setBeanClass(View.class);
		result.setInstanceSupplier(viewSupplier);
		return result;
	}

	/**
	 * Registers the <code>BeanDefinition</code> with the Spring context.
	 *
	 * @param beanName    the bean name
	 * @param definition  the bean definition
	 * @param beanFactory the bean factory
	 */
	protected void registerBeanDefinition(final String beanName, final BeanDefinition definition) {
		beanDefinitionRegistry.registerBeanDefinition(beanName, definition);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Registered BeanDefinition of type '{}' with bean name '{}'.", definition.getBeanClassName(),
					beanName);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(final String id) {
		return (T) applicationContext.getBean(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(final Class<?> beanClass) {
		return (T) applicationContext.getBean(beanClass);
	}
}
