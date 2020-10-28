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
package com.github.actionfx.core.test;

import java.util.function.Supplier;

import com.github.actionfx.core.container.BeanContainerFacade;

/**
 * Test class of a BeanContainerFacade implementation without a no-arg
 * constructor.
 * 
 * @author koster
 *
 */
public class CustomBeanContainerImplWithoutNoArgConstructor implements BeanContainerFacade {

	public CustomBeanContainerImplWithoutNoArgConstructor(String someArgument) {
		// no to do here, we just need to replace the no-arg constructor with a
		// constructor that requires an argument
	}

	@Override
	public void addBeanDefinition(String id, Class<?> beanClass, boolean singleton, Supplier<?> instantiationSupplier) {
	}

	@Override
	public <T> T getBean(String id) {
		return null;
	}

	@Override
	public <T> T getBean(Class<?> beanClass) {
		return null;
	}

}
