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

/**
 * Facade for accessing a bean container holding necessary view and controller
 * instances.
 * 
 * @author koster
 *
 */
public interface BeanContainerFacade {

	/**
	 * Populates the container with view components by scanning the given
	 * {@code rootPackage} and its subpackages for annotated classes relevant for
	 * ActionFX.
	 * 
	 * @param rootPackage the root package that acts as source for view components
	 */
	void populateContainer(String rootPackage);

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
