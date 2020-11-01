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
package com.github.actionfx.core.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.utils.AFXUtils;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ClassPathScanningUtils;

import javafx.concurrent.Task;

/**
 * Default implementation of a bean container using an underlying hash map as
 * bean cache.
 * <p>
 * The bean container respects the @PostConstruct annotation and performs
 * corresponding initialization after bean creation.
 * 
 * @author koster
 *
 */
public class DefaultBeanContainer implements BeanContainerFacade {

	// bean definition map with key: id -> value: bean definition
	private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

	// map for bean defintion -> singleton instances
	private Map<BeanDefinition, Object> singletonCache = new HashMap<>();

	@Override
	public void populateContainer(String rootPackage) {
		List<Class<?>> controllerClasses = ClassPathScanningUtils.findClassesWithAnnotation(rootPackage,
				AFXController.class);
		for (Class<?> controllerClass : controllerClasses) {
			AFXController afxController = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);

		}
	}

	protected void addBeanDefinition(String id, Class<?> beanClass, boolean singleton,
			Supplier<?> instantiationSupplier) {
		beanDefinitionMap.put(id, new BeanDefinition(id, beanClass, singleton, instantiationSupplier));
	}

	@Override
	public <T> T getBean(String id) {
		BeanDefinition beanDefinition = beanDefinitionMap.get(id);
		if (beanDefinition == null) {
			// no bean defined with that ID
			return null;
		}
		return getBeanByDefinition(beanDefinition);
	}

	@Override
	public <T> T getBean(Class<?> beanClass) {
		BeanDefinition beanDefinition = beanDefinitionMap.values().stream()
				.filter(definition -> beanClass.isAssignableFrom(definition.getBeanClass())).findFirst().orElse(null);
		if (beanDefinition == null) {
			return null;
		}
		return getBeanByDefinition(beanDefinition);
	}

	/**
	 * Retrieves the bean by the provided {@code beanDefinition}.
	 * 
	 * @param <T>            the bean type
	 * @param beanDefinition the bean definition
	 * @return the bean instance
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getBeanByDefinition(BeanDefinition beanDefinition) {
		if (beanDefinition.isSingleton()) {
			if (singletonCache.containsKey(beanDefinition)) {
				return (T) singletonCache.get(beanDefinition);
			} else {
				// instance is single but it is not yet created, so let's do this
				T bean = createBeanInstance(beanDefinition);
				singletonCache.put(beanDefinition, bean);
				return bean;
			}
		} else {
			// bean is not a singleton, so we have to create a new instance whenever this
			// method is called
			return createBeanInstance(beanDefinition);
		}

	}

	/**
	 * Creates a new, fresh instance based on the supplied bean definition. This
	 * method ensures that instantiation is performed in the JavaFX thread, as this
	 * is required for certain view components (e.g. a WebView).
	 * 
	 * @param <T>            the bean type
	 * @param beanDefinition the bean definition
	 * @return the created bean instance
	 */
	protected <T> T createBeanInstance(BeanDefinition beanDefinition) {
		Class<?> beanClass = beanDefinition.getBeanClass();
		try {
			final Task<T> instantiationTask = new Task<T>() {
				@Override
				protected T call() throws Exception {
					return instantiateBean(beanDefinition);
				}

			};
			// execute the task in the JavaFX thread and wait for the result
			return AFXUtils.runInFxThreadAndWait(instantiationTask);

		} catch (InterruptedException | ExecutionException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw new IllegalStateException(
					"Failed to instantiate class '" + beanClass.getCanonicalName() + "' in JavaFX thread!", e);
		}
	}

	/**
	 * Creates the instance based on the {@link BeanDefinition}
	 * 
	 * @param <T>            the result type
	 * @param beanDefinition the bean definition
	 * @return the created instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T instantiateBean(BeanDefinition beanDefinition) {
		T instance = (T) beanDefinition.getInstantiationSupplier().get();
		// invoke methods annotated with @PostConstruct
		AnnotationUtils.invokeMethodWithAnnotation(instance.getClass(), instance, PostConstruct.class);
		return instance;
	}

	/**
	 * Internal bean definition structure.
	 * 
	 * @author koster
	 *
	 */
	private static class BeanDefinition {

		private String id;

		private Class<?> beanClass;

		private boolean singleton;

		private Supplier<?> instantiationSupplier;

		public BeanDefinition(String id, Class<?> beanClass, boolean singleton, Supplier<?> instantiatiationSupplier) {
			this.id = id;
			this.beanClass = beanClass;
			this.singleton = singleton;
			instantiationSupplier = instantiatiationSupplier;
		}

		public String getId() {
			return id;
		}

		public Class<?> getBeanClass() {
			return beanClass;
		}

		public boolean isSingleton() {
			return singleton;
		}

		public Supplier<?> getInstantiationSupplier() {
			return instantiationSupplier;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			BeanDefinition other = (BeanDefinition) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			return true;
		}

	}

}
