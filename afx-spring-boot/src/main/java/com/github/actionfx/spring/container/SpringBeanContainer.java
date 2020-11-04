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
import com.github.actionfx.core.container.instantiation.FxmlViewInstantiationSupplier;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.FxmlView;

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

	private BeanDefinitionRegistry beanDefinitionRegistry;

	private ApplicationContext applicationContext;

	/**
	 * Constructor that requires access to the {@link BeanDefinitionRegistry} for
	 * adding new bean definitions and the {@link ApplicationContext} for requesting
	 * beans from the context.
	 * 
	 * @param beanDefinitionRegistry the bean definition registry
	 * @param applicationContext     the application context
	 */
	public SpringBeanContainer(BeanDefinitionRegistry beanDefinitionRegistry, ApplicationContext applicationContext) {
		this.beanDefinitionRegistry = beanDefinitionRegistry;
		this.applicationContext = applicationContext;
	}

	@Override
	public void populateContainer(String rootPackage) {

		// scan for FxController annotations...
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(AFXController.class));
		Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(rootPackage);

		for (BeanDefinition beanDefinition : beanDefinitions) {
			final Class<?> beanClass = ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), null);
			AFXController afxController = AnnotationUtils.findAnnotation(beanClass, AFXController.class);

			ControllerInstantiationSupplier<?> controllerSupplier = new ControllerInstantiationSupplier<>(beanClass);

			// add a bean definition for the controller
			registerBeanDefinition(deriveControllerId(beanClass),
					createBeanDefinitionForController(beanDefinition, afxController, controllerSupplier));

			// add a bean definition for the view
			registerBeanDefinition(afxController.viewId(),
					createBeanDefinitionForView(beanDefinition, afxController, controllerSupplier));
		}

	}

	@Override
	public void addBeanDefinition(String id, Class<?> beanClass, boolean singleton, Supplier<?> instantiationSupplier) {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(beanClass);
		beanDefinition.setScope(singleton ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
		beanDefinition.setInstanceSupplier(instantiationSupplier);
		registerBeanDefinition(id, beanDefinition);
	}

	private BeanDefinition createBeanDefinitionForController(BeanDefinition beanDefinition, AFXController afxController,
			ControllerInstantiationSupplier<?> controllerSupplier) {
		GenericBeanDefinition result = new GenericBeanDefinition(beanDefinition);
		result.setScope(afxController.singleton() ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
		result.setInstanceSupplier(controllerSupplier);
		return result;

	}

	private BeanDefinition createBeanDefinitionForView(BeanDefinition beanDefinition, AFXController afxController,
			ControllerInstantiationSupplier<?> controllerSupplier) {
		// views follow the same rules than the controller (scope, dependsOn, etc.)
		GenericBeanDefinition result = new GenericBeanDefinition(beanDefinition);
		result.setScope(afxController.singleton() ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
		result.setBeanClass(FxmlView.class);
		result.setInstanceSupplier(new FxmlViewInstantiationSupplier(controllerSupplier, afxController));
		return result;
	}

	/**
	 * Registers the <code>BeanDefinition</code> with the Spring context.
	 * 
	 * @param beanName    the bean name
	 * @param definition  the bean definition
	 * @param beanFactory the bean factory
	 */
	protected void registerBeanDefinition(String beanName, BeanDefinition definition) {
		beanDefinitionRegistry.registerBeanDefinition(beanName, definition);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Registered BeanDefinition of type '{}' with bean name '{}'.", definition.getBeanClassName(),
					beanName);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(String id) {
		return (T) applicationContext.getBean(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(Class<?> beanClass) {
		return (T) applicationContext.getBean(beanClass);
	}
}
