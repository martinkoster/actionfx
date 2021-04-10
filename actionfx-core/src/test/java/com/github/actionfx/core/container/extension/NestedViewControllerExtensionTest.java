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
package com.github.actionfx.core.container.extension;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.test.ViewCreator;
import com.github.actionfx.core.view.BorderPanePosition;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForEachMonocleExtension;

import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * JUnit test case for {@link NestedViewControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForEachMonocleExtension.class)
@TestInFxThread
class NestedViewControllerExtensionTest {

	private final View topView = ViewCreator.create(new TextField(), "textField");

	private final View centerView = ViewCreator.create(new TextField(), "textField");

	@BeforeEach
	void onSetup() {
		final BeanContainerFacade container = Mockito.mock(BeanContainerFacade.class);
		when(container.getBean(Mockito.eq("topView"))).thenReturn(topView);
		when(container.getBean(Mockito.eq("centerView"))).thenReturn(centerView);
		ActionFX.builder().build().scanForActionFXComponents(container);
	}

	@AfterEach
	void tearDown() {
		ActionFX.getInstance().reset();
	}

	@Test
	void testAccept() {
		// GIVEN
		final ControllerWithNestedviewOnField controller = new ControllerWithNestedviewOnField();
		final NestedViewControllerExtension extension = new NestedViewControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.mainBorderPane.getTop(), sameInstance(topView.getRootNode()));
		assertThat(controller.mainBorderPane.getCenter(), sameInstance(centerView.getRootNode()));
	}

	public class ControllerWithNestedviewOnField {

		public View _view;

		@AFXNestedView(refViewId = "topView", attachToBorderPanePosition = BorderPanePosition.TOP)
		@AFXNestedView(refViewId = "centerView", attachToBorderPanePosition = BorderPanePosition.CENTER)
		public BorderPane mainBorderPane = new BorderPane();
	}
}
