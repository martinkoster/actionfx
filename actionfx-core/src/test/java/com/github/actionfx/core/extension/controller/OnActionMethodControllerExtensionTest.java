/*
 * Copyright (c) 2021 Martin Koster
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
package com.github.actionfx.core.extension.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.test.ViewCreator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 * JUnit test case for {@link OnActionMethodControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class OnActionMethodControllerExtensionTest {

	@Test
	@TestInFxThread
	void testAccept() {
		// GIVEN
		final Button button = new Button();
		final ControllerWithAFXOnAction controller = Mockito
				.spy(new ControllerWithAFXOnAction(ViewCreator.create(button, "actionButton")));
		final OnActionMethodControllerExtension extension = new OnActionMethodControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(button.getOnAction()).isNotNull();

		// and WHEN (fire action)
		Event.fireEvent(button, new ActionEvent());

		// and THEN (invocation was performed)
		verify(controller, times(1)).onActionButtonClicked();
		// execution was inside JavaFX thread
        assertThat(controller.executedInJavaFxThread).isTrue();
	}

	@Test
	@TestInFxThread
	void testAccept_withActionEvent() {
		// GIVEN
		final Button button = new Button();
		final ControllerWithAFXOnActionAndActionEvent controller = Mockito
				.spy(new ControllerWithAFXOnActionAndActionEvent(ViewCreator.create(button, "actionButton")));
		final OnActionMethodControllerExtension extension = new OnActionMethodControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(button.getOnAction()).isNotNull();

		// and WHEN (fire action)
		Event.fireEvent(button, new ActionEvent());

		// and THEN (invocation was performed)
		verify(controller, times(1)).onActionButtonClicked(Mockito.any(ActionEvent.class));
	}

	@Test
	@TestInFxThread
	void testAccept_asyncAction() {
		// GIVEN
		final Button button = new Button();
		final ControllerWithAFXOnActionAsync controller = Mockito
				.spy(new ControllerWithAFXOnActionAsync(ViewCreator.create(button, "actionButton")));
		final OnActionMethodControllerExtension extension = new OnActionMethodControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(button.getOnAction()).isNotNull();

		// and WHEN (fire action)
		Event.fireEvent(button, new ActionEvent());
		WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);

		// and THEN (invocation was performed)
		verify(controller, times(1)).onActionButtonClicked();
		// execution was not inside JavaFX thread
        assertThat(controller.executedInJavaFxThread).isFalse();
	}

	@Test
	@TestInFxThread
	void testAccept_referencedControlDoesNotHaveOnActionProperty() {
		// GIVEN (view with tableView - which does not have an "onAction" property)
		final ControllerWithAFXOnAction controller = Mockito
				.spy(new ControllerWithAFXOnAction(ViewCreator.create(new TableView<>(), "actionButton")));
		final OnActionMethodControllerExtension extension = new OnActionMethodControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).contains("Control with id='actionButton' and type 'javafx.scene.control.TableView' does not support an 'onAction' property!");
	}

	public class ControllerWithAFXOnAction {

		public View _view;

		protected boolean executedInJavaFxThread;

		public ControllerWithAFXOnAction(final View view) {
			_view = view;
		}

		@AFXOnAction(nodeId = "actionButton")
		public void onActionButtonClicked() {
			if (Platform.isFxApplicationThread()) {
				executedInJavaFxThread = true;
			}
		}
	}

	public class ControllerWithAFXOnActionAsync {

		public View _view;

		protected boolean executedInJavaFxThread = true;

		public ControllerWithAFXOnActionAsync(final View view) {
			_view = view;
		}

		@AFXOnAction(nodeId = "actionButton", async = true)
		public void onActionButtonClicked() {
			if (!Platform.isFxApplicationThread()) {
				// for testing...because "async=true" is set, this method is not executed in
				// JavaFX thread
				executedInJavaFxThread = false;
			}
		}
	}

	public class ControllerWithAFXOnActionAndActionEvent {

		public View _view;

		public ControllerWithAFXOnActionAndActionEvent(final View view) {
			_view = view;
		}

		@AFXOnAction(nodeId = "actionButton")
		public void onActionButtonClicked(final ActionEvent e) {
		}
	}
}
