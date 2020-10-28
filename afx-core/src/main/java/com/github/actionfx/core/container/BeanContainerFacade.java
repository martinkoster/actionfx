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

import java.util.function.Supplier;

/**
 * Facade for accessing a bean container holding necessary view and controller
 * instances.
 * 
 * @author koster
 *
 */
public interface BeanContainerFacade {

	/**
	 * Adds a bean definition to the bean container.
	 * 
	 * @param id                     the bean ID
	 * @param beanClass              the bean class
	 * @param singleton              flag that determines whether the bean instance
	 *                               is a singleton. If the flag is set to
	 *                               {@code false}, a call to
	 * @param instantiantionSupplier a supplier that performs the instantiation
	 */
	void addBeanDefinition(String id, Class<?> beanClass, boolean singleton, Supplier<?> instantiantionSupplier);

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
	<T> T getBean(Class<?> beanClass);
}
