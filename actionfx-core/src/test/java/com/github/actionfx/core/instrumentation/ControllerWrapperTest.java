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

import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.test.ViewCreator;
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
		final ControllerWrapper<TestController> wrapper = new ControllerWrapper<>(new TestController());
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
		final TestController controller = new TestController();

		// WHEN
		ControllerWrapper.setViewOn(controller, view);

		// THEN
		assertThat(ControllerWrapper.getViewFrom(controller), sameInstance(view));
	}

	@Test
	void testGetSetView_classIsNotEnhanced() {
		// GIVEN
		final ControllerWrapper<TestControllerWithoutView> wrapper = new ControllerWrapper<>(
				new TestControllerWithoutView());
		final View view = Mockito.mock(View.class);

		// WHEN (then exception is expected)
		assertThrows(IllegalStateException.class, () -> wrapper.setView(view));
	}

	@Test
	@TestInFxThread
	void testGetWindow() {
		// GIVEN
		final TestController controller = new TestController();
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
	@TestInFxThread
	void testGetResourceBundleFrom() {
		// GIVEN
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.TestResources");
		final TestControllerWithResourceBundle controller = new TestControllerWithResourceBundle(resourceBundle);

		// WHEN
		final ResourceBundle bundle = ControllerWrapper.getResourceBundleFrom(controller);

		// THEN
		assertThat(bundle, sameInstance(resourceBundle));
	}

	@Test
	void testGetScene() {
		// GIVEN
		final TestController controller = new TestController();
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

	@AFXController(viewId = "testId", fxml = "/testfxml/SampleView.fxml", icon = "icon.png", singleton = true, maximized = true, modal = false, title = "Hello World", width = 100, height = 50, posX = 10, posY = 20, stylesheets = {
			"cssClass1", "cssClass2" })
	public static class TestController {
		public View _view;
	}

	public static class TestControllerWithoutView {
	}

	public static class TestControllerWithResourceBundle {

		public View _view;

		public TestControllerWithResourceBundle(final ResourceBundle resourceBundle) {
			_view = ViewCreator.create().resourceBundle(resourceBundle);
		}
	}
}
