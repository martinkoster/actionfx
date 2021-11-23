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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.container.instantiation.ConstructorBasedInstantiationSupplier;
import com.github.actionfx.core.container.instantiation.ControllerInstancePostProcessor;
import com.github.actionfx.core.container.instantiation.ControllerInstantiationSupplier;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ClassPathScanningUtils;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;

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

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBeanContainer.class);

	// bean definition map with key: id -> value: bean definition
	private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

	// map for bean definition -> singleton instances
	private final Map<BeanDefinition, Object> singletonCache = new HashMap<>();

	// strategies to resolve an ID or type to a bean
	private final List<BeanResolutionFunction> beanResolverFunctions = new ArrayList<>();

	// post-processor that wires JavaFX components to annotated methods
	private final ControllerInstancePostProcessor controllerInstancePostProcessor;

	/**
	 * The default constructor (without custom controller extensions).
	 */
	public DefaultBeanContainer() {
		this(Collections.emptyList());
	}

	/**
	 * Constructor that accepts custom controller extensions that are applied to a
	 * newly created controller instance after dependency injection, but before
	 * methods annotated with {@code @PostConstruct} are executed.
	 *
	 * @param customControllerExtensions a list of custom controller extensions
	 *                                   implementing the {@link Consumer} interface
	 */
	public DefaultBeanContainer(final List<Consumer<Object>> customControllerExtensions) {

		controllerInstancePostProcessor = new ControllerInstancePostProcessor(customControllerExtensions);

		// bean resolution strategies
		// the first strategy is to resolve by bean name / ID
		beanResolverFunctions.add((id, type) -> getBean(id));

		// the second strategy is to resolve by type
		beanResolverFunctions.add((id, type) -> getBean(type));

		// the third is to add a bean definition and instantiating via the default
		// constructor, if it is not a Java primitive
		beanResolverFunctions.add((id, type) -> {
			if (isNotPrimitiveOrString(type)) {
				addBeanDefinition(id, type, true, true, new ConstructorBasedInstantiationSupplier<>(type));
				return getBean(id);
			} else {
				return null;
			}
		});
	}

	@Override
	public void runComponentScan(final String rootPackage) {
		final List<Class<?>> controllerClasses = ClassPathScanningUtils.findClassesWithAnnotation(rootPackage,
				AFXController.class);
		for (final Class<?> controllerClass : controllerClasses) {
			addControllerBeanDefinition(controllerClass);
		}
		// all non-lazy beans are instantiated now after reading all bean definitions
		instantiateNonLazyBeans();
	}

	@Override
	public void addBeanDefinition(final String id, final Class<?> beanClass, final boolean singleton,
			final boolean lazyInit, final Supplier<?> instantiationSupplier) {
		beanDefinitionMap.put(id, new BeanDefinition(id, beanClass, singleton, lazyInit, instantiationSupplier));
	}

	@Override
	public void addControllerBeanDefinition(final Class<?> controllerClass) {
		final AFXController afxController = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);
		if (afxController == null) {
			throw new IllegalArgumentException(controllerClass + " is not annotated by @AFXController!");
		}
		final ControllerInstantiationSupplier<?> controllerSupplier = new ControllerInstantiationSupplier<>(
				controllerClass, resolveResourceBundle(controllerClass, getBean(Locale.class)));
		final String controllerId = deriveBeanId(controllerClass);

		// add a bean definition for the controller
		addBeanDefinition(controllerId, controllerClass, afxController.singleton(), afxController.lazyInit(),
				controllerSupplier);

		// and add a bean definition for the view (view itself is injected into the
		// controller instance)
		addBeanDefinition(afxController.viewId(), View.class, afxController.singleton(), afxController.lazyInit(),
				() -> ControllerWrapper.getViewFrom(getBean(controllerId)));
	}

	@Override
	public <T> T getBean(final String id) {
		final BeanDefinition beanDefinition = beanDefinitionMap.get(id);
		if (beanDefinition == null) {
			// no bean defined with that ID
			return null;
		}
		return getBeanByDefinition(beanDefinition);
	}

	@Override
	public <T> T getBean(final Class<T> beanClass) {
		final BeanDefinition beanDefinition = beanDefinitionMap.values().stream()
				.filter(definition -> beanClass.isAssignableFrom(definition.getBeanClass())).findFirst().orElse(null);
		if (beanDefinition == null) {
			return null;
		}
		return getBeanByDefinition(beanDefinition);
	}

	@Override
	public ResourceBundle resolveResourceBundle(final Class<?> controllerClass, final Locale locale) {
		final AFXController afxController = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);
		if (afxController == null) {
			return null;
		}
		final String baseName = afxController.resourcesBasename();
		if ("".equals(baseName)) {
			return null;
		}
		return ResourceBundle.getBundle(baseName, locale);
	}

	/**
	 * Retrieves the bean by the provided {@code beanDefinition}.
	 *
	 * @param <T>            the bean type
	 * @param beanDefinition the bean definition
	 * @return the bean instance
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getBeanByDefinition(final BeanDefinition beanDefinition) {
		if (beanDefinition.isSingleton()) {
			if (singletonCache.containsKey(beanDefinition)) {
				return (T) singletonCache.get(beanDefinition);
			} else {
				// instance is singleton, but it is not yet created, so let's do this
				final T bean = createBeanInstance(beanDefinition);
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
	 * Creates the instance based on the {@link BeanDefinition}
	 *
	 * @param <T>            the result type
	 * @param beanDefinition the bean definition
	 * @return the created instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createBeanInstance(final BeanDefinition beanDefinition) {
		final T instance = (T) beanDefinition.getInstantiationSupplier().get();

		// inject potential dependencies
		injectDependencies(instance);

		// wire JavaFX components to annotated methods
		if (AnnotationUtils.findAnnotation(instance.getClass(), AFXController.class) != null) {
			controllerInstancePostProcessor.postProcess(instance);
		}

		// invoke methods annotated with @PostConstruct
		AnnotationUtils.invokeMethodWithAnnotation(instance.getClass(), instance, PostConstruct.class);

		return instance;
	}

	/**
	 * Performs dependency injection on the supplied {@code bean}. Dependency
	 * injection is performed on fields that are annotated by {@link Inject}.
	 *
	 * @param bean the bean to perform dependency injection on
	 */
	protected void injectDependencies(final Object bean) {
		final Class<? extends Object> clazz = bean.getClass();
		injectMembers(clazz, bean);
	}

	protected void injectMembers(final Class<?> clazz, final Object instance) {
		LOG.debug("Injecting members for class {} and instance {}", clazz.getCanonicalName(), instance);
		final AFXController afxController = AnnotationUtils.findAnnotation(clazz, AFXController.class);
		final Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {
			for (final Field field : fields) {
				if (field.isAnnotationPresent(Inject.class)) {
					injectSingleField(instance, afxController, field);
				}
			}
		}
		final Class<? extends Object> superclass = clazz.getSuperclass();
		if (superclass != null) {
			injectMembers(superclass, instance);
		}
	}

	private void injectSingleField(final Object instance, final AFXController afxController, final Field field) {
		final Class<?> type = field.getType();
		final String key = field.getName();
		Object value;
		// in case the controller wants to have injected its own view instance,
		// we can not do the same via "resolveBean", as this would lead to a stack
		// overflow (we are still in construction phase of the controller)
		if (afxController != null && field.getName().equals(afxController.viewId())) {
			value = ControllerWrapper.getViewFrom(instance);
		} else {
			value = resolveBean(key, type);
		}
		LOG.debug("Field annotated with @Inject found: {}, resolved value: {}", field.getName(), value);
		if (value != null) {
			ReflectionUtils.setFieldValue(field, instance, value);
		}
	}

	private static boolean isNotPrimitiveOrString(final Class<?> type) {
		return !type.isPrimitive() && !type.isAssignableFrom(String.class);
	}

	/**
	 * Tries to resolve the ID and / or type to a bean by using the internal
	 * {@link BeanResolutionFunction}.
	 *
	 * @param <T>
	 * @param id   the ID / bean name
	 * @param type the type
	 * @return the bean instance, or {@code null}, if resolution is not possible
	 */
	@SuppressWarnings("unchecked")
	protected <T> T resolveBean(final String id, final Class<?> type) {
		for (final BeanResolutionFunction function : beanResolverFunctions) {
			final T value = (T) function.resolve(id, type);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Instantiate all non-lazy beans.
	 */
	protected void instantiateNonLazyBeans() {
		beanDefinitionMap.values().stream().filter(beanDefinition -> !beanDefinition.isLazyInitialisation())
				.forEach(this::getBeanByDefinition);
	}

	/**
	 * Internal bean definition structure.
	 *
	 * @author koster
	 *
	 */
	private static class BeanDefinition {

		private final String id;

		private final Class<?> beanClass;

		private final boolean singleton;

		private final boolean lazyInitialisation;

		private final Supplier<?> instantiationSupplier;

		public BeanDefinition(final String id, final Class<?> beanClass, final boolean singleton,
				final boolean lazyInitialisation, final Supplier<?> instantiationSupplier) {
			this.id = id;
			this.beanClass = beanClass;
			this.singleton = singleton;
			this.lazyInitialisation = lazyInitialisation;
			this.instantiationSupplier = instantiationSupplier;
		}

		public Class<?> getBeanClass() {
			return beanClass;
		}

		public boolean isSingleton() {
			return singleton;
		}

		public boolean isLazyInitialisation() {
			return lazyInitialisation;
		}

		public Supplier<?> getInstantiationSupplier() {
			return instantiationSupplier;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (id == null ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final BeanDefinition other = (BeanDefinition) obj;
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

	/**
	 * Strategy interface for resolving an id or class to a bean.
	 *
	 * @author koster
	 *
	 */
	@FunctionalInterface
	private static interface BeanResolutionFunction {

		/**
		 * Resolves the ID or type to a bean
		 *
		 * @param <T>
		 * @param id   the ID to resolve
		 * @param type the type to resolve
		 * @return the resolve instance, or {@link null}, if the ID or type can not be
		 *         resolved.
		 */
		public Object resolve(String id, Class<?> type);
	}

}
