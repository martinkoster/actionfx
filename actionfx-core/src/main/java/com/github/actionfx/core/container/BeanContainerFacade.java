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

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import com.github.actionfx.core.container.extension.ControllerExtensionBean;

import javafx.beans.value.ObservableValue;

/**
 * Facade for accessing a bean container holding necessary view and controller
 * instances.
 *
 * @author koster
 *
 */
public interface BeanContainerFacade {

	/**
	 * The bean name of an {@link ObservableValue} holding the application locale.
	 */
	public static String LOCALE_PROPERTY_BEANNAME = "localePropertyBean";

	/**
	 * The bean name of the application {@link java.util.Locale}. Preferably use
	 * {@link #LOCALE_PROPERTY_BEANNAME} (which is an {@link ObservableValue}),
	 * because classes using the {@link Locale} directly are not notified on
	 * potential changes.
	 */
	public static String LOCALE_BEANNAME = "localeBean";

	/**
	 * The bean name of ActionFX itself. ActionFX can be injected via
	 * {@code @Inject} into controller classes.
	 */
	public static String ACTIONFX_BEANNAME = "actionFX";

	/**
	 * The bean name of an {@link ControllerExtensionBean} holding all user defined
	 * controller extensions.
	 */
	public static String CONTROLLER_EXTENSION_BEANNAME = "controllerExtensionBean";

	/**
	 * Populates the container with view components by scanning the given
	 * {@code rootPackage} and its subpackages for annotated classes relevant for
	 * ActionFX.
	 *
	 * @param rootPackage the root package that acts as source for view components
	 */
	void runComponentScan(String rootPackage);

	/**
	 * Adds a new bean definition to the bean container.
	 *
	 * @param id                    the bean ID / name
	 * @param beanClass             the bean type
	 * @param singleton             {@code true}, if the created instance of the
	 *                              bean shall be a singleton, {@code false}, if
	 *                              there should be a new instance created, whenever
	 *                              {@link #getBean(Class)} is called.
	 * @param lazyInit              Flag that controls the initialization of the
	 *                              view and controller. If set to {@code true},
	 *                              beans are lazily initialized at the point the
	 *                              bean is really required and requested to be
	 *                              used. If set to {@link false}, the beans are
	 *                              initialized at the startup of the ActionFX
	 *                              application, when the view manager is
	 *                              initialized. Although lazy loading should be
	 *                              preferred, disabling of lazy loading makes
	 *                              sense, when you want to have a fail-early
	 *                              fail-fast pattern and exceptions during view
	 *                              initializations should/must be thrown at
	 *                              application startup (and not later, when you
	 *                              already work with the application).
	 * @param instantiationSupplier the supplier that provides the instance
	 */
	void addBeanDefinition(String id, Class<?> beanClass, boolean singleton, boolean lazyInit,
			Supplier<?> instantiationSupplier);

	/**
	 * Gets a bean from the underlying bean container by ID.
	 *
	 * @param <T> the type of the bean
	 * @param id  the bean ID
	 * @return the bean instance
	 */
	<T> T getBean(String id);

	/**
	 * Gets a bean from the underlying bean container by the bean type described by
	 * {@code beanClass}.
	 *
	 * @param <T>       the type of the bean
	 * @param beanClass the bean class
	 * @return the bean instance
	 */
	<T> T getBean(Class<T> beanClass);

	/**
	 * Retrieves the resource bundle holding internationalized texts for the given
	 * {@code controllerClass} and supplied {@link Locale}.
	 *
	 * @param controllerClass the controller class for that the resource bundle
	 *                        shall be retrieved
	 * @param locale          the locale
	 * @return the resolved resource bundle
	 */
	ResourceBundle resolveResourceBundle(Class<?> controllerClass, Locale locale);

	/**
	 * Derives an ID from the given {@code controllerClass} under that the component
	 * shall be stored and retrieved at.
	 * <p>
	 * The default implementation takes the simple class name and decapitalizes the
	 * first letter.
	 * <p>
	 * <p>
	 * <b>Example:</b>
	 *
	 * <pre>
	 * deriveControllerId(SomeController.class) = "someController"
	 *
	 * <pre>
	 *
	 * @param controllerClass the controller class
	 * @return the derived controller ID
	 */
	default String deriveControllerId(final Class<?> controllerClass) {
		final String className = controllerClass.getSimpleName();
		return className.length() > 1
				? className.substring(0, 1).toLowerCase() + className.substring(1, className.length())
				: className.substring(0, 1).toLowerCase();
	}
}
