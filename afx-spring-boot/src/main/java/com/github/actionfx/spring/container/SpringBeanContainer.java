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

import java.util.concurrent.ExecutionException;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.spring.postpocessor.InFxThread;

import javafx.concurrent.Task;

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
		// post process bean definitions and add a factory method for @InFxThread
		// annotated classes
		for (final String beanName : registry.getBeanDefinitionNames()) {
			final BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
			if (beanDefinition.getBeanClassName() != null) {
				final Class<?> beanClass = ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), null);
				final InFxThread inFxThread = IrisReflectionUtils.findAnnotation(beanClass, InFxThread.class);
				if (inFxThread != null) {

					// add the factory method "createFxComponent" to the bean definition
					beanDefinition.setFactoryBeanName(ACTIONFX_BEANREGISTRY_POST_PROCESSOR_BEAN);
					beanDefinition.setFactoryMethodName("createFxComponent");
					final ConstructorArgumentValues cav = beanDefinition.getConstructorArgumentValues();
					cav.addGenericArgumentValue(beanClass);
				}
			}
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

	/**
	 * Factory method for <tt>@InFxThread</tt> annotated bean classes.
	 * <p>
	 * The generic return type here is significant. If the signature is changed to
	 * return an instance of type <tt>Object</tt>, Spring is not able to know the
	 * bean class type of the created bean (although there is an attribute
	 * <tt>beanClass</tt> in <tt>BeanDefinition</tt>).
	 *
	 * @param beanClass the bean class to create
	 * @return the created bean instance
	 */
	public <T> T createFxComponent(final Class<T> beanClass) {
		final Task<T> instantiationTask = new Task<T>() {
			@Override
			protected T call() throws Exception {
				return IrisReflectionUtils.instantiateClass(beanClass);
			}
		};
		try {
			// execute the task in the JavaFX thread and wait for the result
			final T bean = FxUtils.runInFxThreadAndWait(instantiationTask);
			return bean;
		} catch (InterruptedException | ExecutionException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw new BeanInstantiationException(beanClass,
					"Failed to instantiate class '" + beanClass.getCanonicalName() + "' in JavaFX thread!", e);
		}
	}

}
