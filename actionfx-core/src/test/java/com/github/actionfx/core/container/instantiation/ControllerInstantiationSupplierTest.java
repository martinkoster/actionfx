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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForEachMonocleExtension;

import javafx.event.ActionEvent;
import javafx.event.Event;
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
	void testCreateInstance_wireOnUserInput_valueChangetextField_allListenersAreActive() {
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
	void testCreateInstance_wireOnUserInput_valueChangetextField_onlyOneListenerIsActive_usingListenerActiveBooleanPropertyInAnnotation() {
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
	void testCreateInstance_wireOnUserInput_valueChangechoiceBox() {
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
	void testCreateInstance_wireOnUserInput_valueChangecomboBox() {
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

	@Test
	void testCreateInstance_wireOnAction() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();

		// THEN
		assertThat(controller.actionButton.getOnAction(), notNullValue());

		// and WHEN (fire action)
		Event.fireEvent(controller.actionButton, new ActionEvent());

		// and THEN (invocation was performed)
		assertThat(controller.invocations, contains("onActionButtonClicked()"));
	}

	@Test
	void testCreateInstance_wireOnAction_referencedControlDoesNotHaveOnActionProperty() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithWrongAFXOnAction> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithWrongAFXOnAction.class);

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> supplier.get());

		// THEN
		assertThat(ex.getMessage(), equalTo(
				"Control with id='singleSelectionTable' and type 'javafx.scene.control.TableView' does not support an 'onAction' property! Please verify your @AFXOnAction annotation in controller class 'com.github.actionfx.core.container.instantiation.SampleViewControllerWithWrongAFXOnAction', method 'willNeverBeCalledAsOnActionIsWrong'!"));
	}

	@Test
	void testCreateInstance_wireOnUserInput_selection_singleValueChange_inTableView() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		controller.singleSelectionTable.getItems().add("Item 1");
		controller.singleSelectionTable.getItems().add("Item 2");
		controller.singleSelectionTable.getItems().add("Item 3");
		controller.singleSelectionTable.getSelectionModel().select("Item 2");

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations, contains("onSelectValueInSingleSelectionTable('Item 2')",
				"onSelectValueInSingleSelectionTableWithList([Item 2])"));
	}

	@Test
	void testCreateInstance_wireOnUserInput_selection_multiValueChange_inTableView() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		controller.multiSelectionTable.getItems().add("Item 1");
		controller.multiSelectionTable.getItems().add("Item 2");
		controller.multiSelectionTable.getItems().add("Item 3");
		controller.multiSelectionTable.getSelectionModel().selectAll();

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations, contains("onSelectValueInMultiSelectionTable('Item 1','Item 2','Item 3')",
				"onSelectValueInMultiSelectionTableWithFullArguments([Item 1,Item 2,Item 3],[Item 1,Item 2,Item 3],[],'null',change)",
				"onSelectValueInMultiSelectionTableWithAnnotatedArguments([Item 1,Item 2,Item 3],[Item 1,Item 2,Item 3],[],'null',change)"));
	}

	@Test
	void testCreateInstance_wireLoadControlData() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();

		// THEN (verify that dataLoadedSelectionTable has the items loaded from the
		// method "loadData")
		assertThat(controller.dataLoadedSelectionTable.getItems(), contains("Loaded 1", "Loaded 2", "Loaded 3"));
	}

}
