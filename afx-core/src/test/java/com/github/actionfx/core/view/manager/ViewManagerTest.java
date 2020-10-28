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
package com.github.actionfx.core.view.manager;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.actionfx.core.container.DefaultBeanContainer;
import com.github.actionfx.core.test.CustomBeanContainerImpl;
import com.github.actionfx.core.test.CustomBeanContainerImplWithoutNoArgConstructor;

/**
 * JUnit test case for {@code ViewManager}.
 * 
 * @author koster
 *
 */
class ViewManagerTest {

	@BeforeEach
	void onSetup() {
		// set view manager instance to 'null' in order to force the creation of a
		// ViewManager instance for each test
		ViewManager.instance = null;
	}

	@Test
	void testViewManagerInstance_withDefaultBeanContainer() {
		// GIVEN (no system property supplied)
		System.setProperty(ViewManager.BEAN_CONTAINER_IMPLEMENTATION_SYSTEM_PROPERTY, "");

		// WHEN
		ViewManager viewManager = ViewManager.getInstance();

		// THEN
		assertThat(viewManager.getBeanContainer(), instanceOf(DefaultBeanContainer.class));
	}

	@Test
	void testViewManagerInstance_withCustomBeanContainer() {
		// GIVEN (a bean container that has a no-arg constructor)
		System.setProperty(ViewManager.BEAN_CONTAINER_IMPLEMENTATION_SYSTEM_PROPERTY,
				CustomBeanContainerImpl.class.getCanonicalName());

		ViewManager viewManager = ViewManager.getInstance();

		// THEN
		assertThat(viewManager.getBeanContainer(), instanceOf(CustomBeanContainerImpl.class));

	}

	@Test
	void testViewManagerInstance_withCustomButInvalidBeanContainer() {
		// GIVEN (a bean container lacking a no-arg constructor)
		System.setProperty(ViewManager.BEAN_CONTAINER_IMPLEMENTATION_SYSTEM_PROPERTY,
				CustomBeanContainerImplWithoutNoArgConstructor.class.getCanonicalName());

		// WHEN and THEN
		assertThrows(IllegalStateException.class, () -> ViewManager.getInstance());
	}

}
