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
package com.github.actionfx.core.instrumentation;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.instrumentation.bytebuddy.ActionFXByteBuddyEnhancer;
import com.github.actionfx.core.test.TestController;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JUnit test case for {@link ControllerWrapper}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ControllerWrapperTest {

	@Test
	void testSetView_andThenGetView() {
		// GIVEN
		final ControllerWrapper<TestController> wrapper = new ControllerWrapper<>(createEnhancedTestController());
		final View view = Mockito.mock(View.class);

		// WHEN
		wrapper.setView(view);

		// THEN
		assertThat(wrapper.getView(), sameInstance(view));
	}

	@Test
	void testSetViewOn_andThenGetViewFrom() {
		// GIVEN
		final View view = Mockito.mock(View.class);
		final TestController controller = createEnhancedTestController();

		// WHEN
		ControllerWrapper.setViewOn(controller, view);

		// THEN
		assertThat(ControllerWrapper.getViewFrom(controller), sameInstance(view));
	}

	@Test
	void testGetSetView_classIsNotEnhanced() {
		// GIVEN
		final ControllerWrapper<TestController> wrapper = new ControllerWrapper<>(createUnenhancedTestController());
		final View view = Mockito.mock(View.class);

		// WHEN (then exception is expected)
		assertThrows(IllegalStateException.class, () -> wrapper.setView(view));
	}

	@Test
	@TestInFxThread
	void testGetWindow() {
		// GIVEN
		final TestController controller = createEnhancedTestController();
		final AFXController afxController = AnnotationUtils.findAnnotation(TestController.class, AFXController.class);
		final FxmlView fxmlView = new FxmlView(afxController.viewId(), afxController.fxml(), controller);
		ControllerWrapper.setViewOn(controller, fxmlView);
		final Stage stage = new Stage();
		fxmlView.show(stage);

		// WHEN
		final Window window = ControllerWrapper.of(controller).getWindow();

		// THEN
		assertThat(window, notNullValue());
		assertThat(window, sameInstance(stage));
	}

	@Test
	void testGetScene() {
		// GIVEN
		final TestController controller = createEnhancedTestController();
		final AFXController afxController = AnnotationUtils.findAnnotation(TestController.class, AFXController.class);
		final FxmlView fxmlView = new FxmlView(afxController.viewId(), afxController.fxml(), controller);
		ControllerWrapper.setViewOn(controller, fxmlView);
		final Scene scene = new Scene(fxmlView.getRootNode());

		// WHEN
		final Scene nodeScene = ControllerWrapper.of(controller).getScene();

		// THEN
		assertThat(nodeScene, notNullValue());
		assertThat(nodeScene, sameInstance(scene));
	}

	TestController createEnhancedTestController() {
		try {
			final ActionFXByteBuddyEnhancer enhancer = new ActionFXByteBuddyEnhancer();
			final Class<?> controllerClass = enhancer.enhanceClass(TestController.class);
			return (TestController) controllerClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Can not instantiate TestController!", e);
		}
	}

	TestController createUnenhancedTestController() {
		return new TestController();
	}
}
