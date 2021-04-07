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
package com.github.actionfx.core.instrumentation.interceptors;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXShowView;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * JUnit test case for {@link AFXActionMethodInterceptor}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class AFXActionMethodInterceptorTest {

	@BeforeEach
	void onSetup() {
		ActionFX.builder().scanPackage(AFXActionMethodInterceptorTest.class.getPackageName()).build()
				.scanForActionFXComponents();
	}

	@AfterEach
	void tearDown() {
		ActionFX.getInstance().reset();
	}

	@Test
	@TestInFxThread
	void testInterceptAFXAction_showViewTwoInNewStage() throws Exception {
		// GIVEN
		final Callable<?> callable = Mockito.mock(Callable.class);
		final Method method = findMethod(ControllerOne.class, "showViewTwoInNewStage");
		final AFXShowView afxAction = findAnnotation(method);
		final ControllerOne controllerOne = ActionFX.getInstance().getBean(ControllerOne.class);
		final View viewOfControllerOne = ControllerWrapper.of(controllerOne).getView();
		final ControllerTwo controllerTwo = ActionFX.getInstance().getBean(ControllerTwo.class);
		final View viewOfControllerTwo = ControllerWrapper.of(controllerTwo).getView();
		// display view in a stage
		final Stage primaryStage = new Stage();
		viewOfControllerOne.show(primaryStage);

		// WHEN
		AFXActionMethodInterceptor.interceptAFXAction(afxAction, callable, controllerOne);

		// THEN (check that view of controller two is part of new stage that is
		// different from the primary stage)
		assertThat(viewOfControllerTwo.getWindow(), notNullValue());
		assertThat(viewOfControllerTwo.getWindow(), not(sameInstance(primaryStage)));
	}

	@Test
	@TestInFxThread
	void testInterceptAFXAction_showViewTwoInSameWindow_windowIsStage() throws Exception {
		// GIVEN
		final Callable<?> callable = Mockito.mock(Callable.class);
		final Method method = findMethod(ControllerOne.class, "showViewTwoInSameWindow");
		final AFXShowView afxAction = findAnnotation(method);
		final ControllerOne controllerOne = ActionFX.getInstance().getBean(ControllerOne.class);
		final View viewOfControllerOne = ControllerWrapper.of(controllerOne).getView();
		final ControllerTwo controllerTwo = ActionFX.getInstance().getBean(ControllerTwo.class);
		final View viewOfControllerTwo = ControllerWrapper.of(controllerTwo).getView();
		// display view in a stage
		final Stage primaryStage = new Stage();
		viewOfControllerOne.show(primaryStage);

		// WHEN
		AFXActionMethodInterceptor.interceptAFXAction(afxAction, callable, controllerOne);

		// THEN (check that view of controller two is identical to the primary stage)
		assertThat(viewOfControllerTwo.getWindow(), notNullValue());
		assertThat(viewOfControllerTwo.getWindow(), sameInstance(primaryStage));
	}

	@Test
	@TestInFxThread
	void testInterceptAFXAction_showViewTwoInSameWindow_windowIsPopup() throws Exception {
		// GIVEN
		final Callable<?> callable = Mockito.mock(Callable.class);
		final Method method = findMethod(ControllerOne.class, "showViewTwoInSameWindow");
		final AFXShowView afxAction = findAnnotation(method);
		final ControllerOne controllerOne = ActionFX.getInstance().getBean(ControllerOne.class);
		final View viewOfControllerOne = ControllerWrapper.of(controllerOne).getView();
		final ControllerTwo controllerTwo = ActionFX.getInstance().getBean(ControllerTwo.class);
		final View viewOfControllerTwo = ControllerWrapper.of(controllerTwo).getView();
		// display view in a Popup
		final Popup popup = new Popup();
		final Stage primaryStage = new Stage();
		viewOfControllerOne.show(popup, primaryStage);

		// WHEN
		AFXActionMethodInterceptor.interceptAFXAction(afxAction, callable, controllerOne);

		// THEN (check that view of controller two is identical to the popup)
		assertThat(viewOfControllerTwo.getWindow(), notNullValue());
		assertThat(viewOfControllerTwo.getWindow(), sameInstance(popup));
	}

	@Test
	@TestInFxThread
	void testInterceptAFXAction_showViewAsNested() throws Exception {
		// GIVEN
		final Callable<?> callable = Mockito.mock(Callable.class);
		final Method method = findMethod(ControllerOne.class, "showViewAsNested");
		final AFXShowView afxAction = findAnnotation(method);
		final ControllerOne controllerOne = ActionFX.getInstance().getBean(ControllerOne.class);
		final View viewOfControllerOne = ControllerWrapper.of(controllerOne).getView();
		final ControllerTwo controllerTwo = ActionFX.getInstance().getBean(ControllerTwo.class);
		final View viewOfControllerTwo = ControllerWrapper.of(controllerTwo).getView();
		// display view in a stage
		final Stage primaryStage = new Stage();
		viewOfControllerOne.show(primaryStage);

		// WHEN
		AFXActionMethodInterceptor.interceptAFXAction(afxAction, callable, controllerOne);

		// THEN (check that view of controller two is identical to the primary stage)
		assertThat(viewOfControllerTwo.getWindow(), notNullValue());
		assertThat(viewOfControllerTwo.getWindow(), sameInstance(primaryStage));

		// assert that the center content of the border pane is the content of
		// "viewOfControllerTwo"
		assertThat(controllerOne.getMainBorderPane().getCenter(), sameInstance(viewOfControllerTwo.getRootNode()));
	}

	private static AFXShowView findAnnotation(final Method method) {
		return method.getAnnotation(AFXShowView.class);
	}

	private static Method findMethod(final Class<?> clazz, final String methodName) {
		Method method;
		try {
			method = clazz.getMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(
					"Unable to retrieve method '" + methodName + "' from class '" + clazz.getCanonicalName() + "'!", e);
		}
		return method;
	}

}
