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
package com.github.actionfx.core.instrumentation.bytebuddy;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.test.TestController;

/**
 * JUnit test case {@link ActionFXByteBuddyEnhancer}.
 * 
 * @author koster
 *
 */
class ActionFXByteBuddyEnhancerTest {

	@Test
	void testInstall() {
		// GIVEN
		ActionFXByteBuddyEnhancer enhancer = new ActionFXByteBuddyEnhancer();

		// WHEN
		enhancer.installAgent();

		// THEN
		TestController controller = new TestController();
		controller.actionMethod();
	}

	@Test
	void testEnhanceClass() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		// GIVEN
		ActionFXByteBuddyEnhancer enhancer = new ActionFXByteBuddyEnhancer();

		// WHEN
		Class<?> enhancedClass = enhancer.enhanceClass(TestController.class);

		// THEN
		TestController controller = (TestController) enhancedClass.getDeclaredConstructor().newInstance();
		controller.actionMethod();
	}

}
