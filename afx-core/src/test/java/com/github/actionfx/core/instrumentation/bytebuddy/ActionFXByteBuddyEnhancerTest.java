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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.test.TestController;
import com.github.actionfx.core.view.View;

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
		final ActionFXByteBuddyEnhancer enhancer = new ActionFXByteBuddyEnhancer();

		// WHEN
		enhancer.installAgent();

		// THEN
		final TestController controller = new TestController();
		assertViewFieldIsPresent(controller.getClass());
		controller.actionMethod();
	}

	@Test
	void testEnhanceClass() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		// GIVEN
		final ActionFXByteBuddyEnhancer enhancer = new ActionFXByteBuddyEnhancer();

		// WHEN
		final Class<?> enhancedClass = enhancer.enhanceClass(TestController.class);

		// THEN
		final TestController controller = (TestController) enhancedClass.getDeclaredConstructor().newInstance();
		assertViewFieldIsPresent(controller.getClass());
		controller.actionMethod();
	}

	private static void assertViewFieldIsPresent(final Class<?> controllerClass) {
		Field field;
		try {
			field = controllerClass.getField(ActionFXEnhancer.VIEW_FIELD_NAME);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException(
					"Class has not the expected field '" + ActionFXEnhancer.VIEW_FIELD_NAME + "'!", e);
		}
		assertThat(field, notNullValue());
		assertThat(field.getType(), equalTo(View.class));
	}

}
