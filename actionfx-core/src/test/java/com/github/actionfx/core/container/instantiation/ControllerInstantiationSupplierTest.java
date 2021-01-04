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
package com.github.actionfx.core.container.instantiation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForEachMonocleExtension;

import javafx.scene.control.SelectionMode;

/**
 * JUnit test case for {@link FxmlViewInstantiationSupplier}
 *
 * @author koster
 *
 */
@TestInFxThread
@ExtendWith(FxThreadForEachMonocleExtension.class)
class ControllerInstantiationSupplierTest {

	@BeforeAll
	static void beforeAll() {
		ActionFX.builder().build();
	}

	@Test
	void testCreateInstance_viewCreationTest() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewController> supplier = new ControllerInstantiationSupplier<>(
				SampleViewController.class);

		// WHEN
		final SampleViewController controller = supplier.get();

		// THEN
		assertThat(controller, notNullValue());
		final View view = ControllerWrapper.getViewFrom(controller);
		assertThat(view, notNullValue());
		assertThat(view.getRootNode(), notNullValue());
		assertThat(view.getId(), equalTo("testId"));

		assertThat(view, instanceOf(View.class));
		final FxmlView fxmlView = (FxmlView) view;
		assertThat(fxmlView.getWidth(), equalTo(100));
		assertThat(fxmlView.getHeight(), equalTo(50));
		assertThat(fxmlView.getIcon(), equalTo("icon.png"));
		assertThat(fxmlView.getPosX(), equalTo(10));
		assertThat(fxmlView.getPosY(), equalTo(20));
		assertThat(fxmlView.isMaximized(), equalTo(true));
		assertThat(fxmlView.isModalDialogue(), equalTo(false));
		assertThat(fxmlView.getWindowTitle(), equalTo("Hello World"));
		assertThat(fxmlView.getStylesheets(), hasItems(equalTo("cssClass1"), equalTo("cssClass2")));
	}

	@Test
	void testCreateInstance_listenerTest_textField_allListenersAreActive() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		controller.textField.setText("Hello World"); // triggers listener

		// THEN (all 3 annotated methods are invoked)
		assertThat(controller.invocations,
				contains("onTextFieldValueChange('Hello World')",
						"onTextFieldValueChangeWithNewAndOldValue('Hello World', '', ObservableValue)",
						"onTextFieldValueChangeWithAnnotatedArguments('', 'Hello World', ObservableValue)"));
	}

	@Test
	void testCreateInstance_listenerTest_textField_onlyOneListenerIsActive_usingListenerActiveBooleanPropertyInAnnotation() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		// deactivate 2 out of 3 listeners (2 annotations use the
		// "listenerActiveBooleanProperty"
		controller.listenerEnabled.set(false);
		controller.textField.setText("Hello World"); // triggers listener

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations, contains("onTextFieldValueChange('Hello World')"));
	}

	@Test
	void testCreateInstance_listenerTest_choiceBox() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		controller.choiceBox.setValue("Hello World"); // triggers listener

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations, contains("onChoiceBoxValueChange('Hello World')"));
	}

	@Test
	void testCreateInstance_listenerTest_comboBox() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		controller.comboBox.setValue("Hello World"); // triggers listener

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations, contains("onComboBoxValueChange('Hello World')"));
	}

	@Test
	void testCreateInstance_enableMultiSelectionControls() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();

		// THEN
		assertThat(controller.singleSelectionTable.getSelectionModel().getSelectionMode(),
				equalTo(SelectionMode.SINGLE));
		assertThat(controller.multiSelectionTable.getSelectionModel().getSelectionMode(),
				equalTo(SelectionMode.MULTIPLE));
	}
}
