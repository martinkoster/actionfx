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
package com.github.actionfx.core.container.instantiation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.collections.ObservableListAdapter;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForEachMonocleExtension;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.SelectionMode;

/**
 * JUnit integration test case for {@link ControllerInstancePostProcessor}. This
 * is considered as integration test as it requires all controller extensions to
 * be present.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForEachMonocleExtension.class)
class ControllerInstancePostProcessorIntegrationTest {

	private final ControllerInstancePostProcessor postProcessor = new ControllerInstancePostProcessor();

	@BeforeAll
	static void beforeAll() {
		ActionFX.builder().scanPackage("dummy.package").build().scanForActionFXComponents();
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnAction_referencedControlDoesNotHaveOnActionProperty() {
		// GIVEN
		final ControllerInstantiationSupplier<ControllerWithWrongAFXOnAction> supplier = new ControllerInstantiationSupplier<>(
				ControllerWithWrongAFXOnAction.class);

		// WHEN
		final ControllerWithWrongAFXOnAction controller = supplier.get();
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> postProcessor.postProcess(controller));

		// THEN
		assertThat(ex.getMessage()).contains("Control with id='singleSelectionTable' and type 'javafx.scene.control.TableView' does not support an 'onAction' property!");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnUserInput_valueChangetextField_allListenersAreActive() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);
		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);
		controller.textField.setText("Hello World"); // triggers listener

		// THEN (all 3 annotated methods are invoked)
		assertThat(controller.invocations).containsExactly("onTextFieldValueChange('Hello World')", "onTextFieldValueChangeWithNewAndOldValue('Hello World', '', ObservableValue)", "onTextFieldValueChangeWithAnnotatedArguments('', 'Hello World', ObservableValue)");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireEnableNode() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);
		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN (text field is empty, so action button is inactive
        assertThat(controller.actionButtonTextField.isDisabled()).isTrue();

		// and WHEN
		controller.textField.setText("Hello World");

		// and THEN (button is activated, because text field now holds a non-empty user
		// value)
        assertThat(controller.actionButtonTextField.isDisabled()).isFalse();
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnUserInput_valueChangetextField_onlyOneListenerIsActive_usingListenerActiveBooleanPropertyInAnnotation() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);
		// deactivate 2 out of 3 listeners (2 annotations use the
		// "listenerActiveBooleanProperty"
		controller.listenerEnabled.set(false);
		controller.textField.setText("Hello World"); // triggers listener

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations).containsExactly("onTextFieldValueChange('Hello World')");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnUserInput_valueChangechoiceBox() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);
		controller.choiceBox.setValue("Hello World"); // triggers listener

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations).containsExactly("onChoiceBoxValueChange('Hello World')");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnUserInput_valueChangecomboBox() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);
		controller.comboBox.setValue("Hello World"); // triggers listener

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations).containsExactly("onComboBoxValueChange('Hello World')");
	}

	@Test
	@TestInFxThread
	void testPostProcess_enableMultiSelectionControls() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN
		assertThat(controller.singleSelectionTable.getSelectionModel().getSelectionMode()).isEqualTo(SelectionMode.SINGLE);
		assertThat(controller.multiSelectionTable.getSelectionModel().getSelectionMode()).isEqualTo(SelectionMode.MULTIPLE);
	}

	@Test
	@TestInFxThread
	void testPostProcess_useFilteredList() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN
		assertThat(controller.singleSelectionTable.getItems()).isInstanceOf(FilteredList.class);
		assertThat(controller.multiSelectionTable.getItems()).isInstanceOf(SortedList.class);
		final SortedList<String> sortedList = (SortedList<String>) controller.multiSelectionTable.getItems();
		assertThat(sortedList.getSource()).isInstanceOf(FilteredList.class);
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnAction() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN
		assertThat(controller.actionButton.getOnAction()).isNotNull();

		// and WHEN (fire action)
		Event.fireEvent(controller.actionButton, new ActionEvent());

		// and THEN (invocation was performed)
		assertThat(controller.invocations).containsExactly("onActionButtonClicked()");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnAction_togetherWith_wireOnControlUserValue() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN
		assertThat(controller.actionWithSubmissionButton.getOnAction()).isNotNull();

		// and WHEN (fire action)
		Event.fireEvent(controller.actionWithSubmissionButton, new ActionEvent());

		// and THEN (invocation was performed, with control user value injected into
		// method)
		assertThat(controller.invocations).containsExactly("onActionWithSubmissionButtonClicked(ActionEvent, 'Hello World')");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnUserInput_selection_singleValueChange_inTableView() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);
		final ObservableListAdapter<String> adapter = new ObservableListAdapter<>(
				controller.singleSelectionTable.getItems());
		adapter.getModifiableList().add("Item 1");
		adapter.getModifiableList().add("Item 2");
		adapter.getModifiableList().add("Item 3");
		controller.singleSelectionTable.getSelectionModel().select("Item 2");

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations).containsExactly("onSelectValueInSingleSelectionTable('Item 2')", "onSelectValueInSingleSelectionTableWithList([Item 2])");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireOnUserInput_selection_multiValueChange_inTableView() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);
		final ObservableListAdapter<String> adapter = new ObservableListAdapter<>(
				controller.multiSelectionTable.getItems());
		adapter.getModifiableList().add("Item 1");
		adapter.getModifiableList().add("Item 2");
		adapter.getModifiableList().add("Item 3");
		controller.multiSelectionTable.getSelectionModel().selectAll();

		// THEN (only 1 method invocation is invoked)
		assertThat(controller.invocations).containsExactly("onSelectValueInMultiSelectionTable('Item 1','Item 2','Item 3')", "onSelectValueInMultiSelectionTableWithFullArguments([Item 1,Item 2,Item 3],[Item 1,Item 2,Item 3],[],'null',change)", "onSelectValueInMultiSelectionTableWithAnnotatedArguments([Item 1,Item 2,Item 3],[Item 1,Item 2,Item 3],[],'null',change)");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireLoadControlData_valueIsObservableList() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN (verify that dataLoadedSelectionTable has the items loaded from the
		// method "loadData")
		assertThat(controller.dataLoadedSelectionTable.getItems()).containsExactly("Loaded 1", "Loaded 2", "Loaded 3");
	}

	@Test
	void testPostProcess_wireLoadControlData_valueIsObservableList_dataIsLoadedAsynchronously() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN (initially, data is empty, because the data loading flag is set to false
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
        assertThat(controller.asyncDataLoadedSelectionTable.getItems()).isEmpty();

		// and WHEN (we switch the loading flag to "true")
		controller.loadDataForTableViewActivated.set(true);

		// and THEN
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
		assertThat(controller.dataLoadedSelectionTable.getItems()).containsExactly("Loaded 1", "Loaded 2", "Loaded 3");
	}

	@Test
	@TestInFxThread
	void testPostProcess_wireLoadControlData_valueIsWritableValue() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN
		assertThat(controller.dataLoadedTreeView.getRoot()).isNotNull();
		assertThat(controller.dataLoadedTreeView.getRoot().getValue()).isEqualTo("root");
	}

	@Test
	void testPostProcess_wireLoadControlData_valueIsWritableValue_dataIsLoadedAsynchronously() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewControllerWithListener> supplier = new ControllerInstantiationSupplier<>(
				SampleViewControllerWithListener.class);

		// WHEN
		final SampleViewControllerWithListener controller = supplier.get();
		postProcessor.postProcess(controller);

		// THEN (initially, root value is empty, because the data loading flag is set to
		// false
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
		assertThat(controller.asyncDataLoadedTreeView.getRoot()).isNull();

		// and WHEN (we switch the loading flag to "true")
		controller.loadDataForTreeViewActivated.set(true);

		// and THEN
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
		assertThat(controller.asyncDataLoadedTreeView.getRoot()).isNotNull();
		assertThat(controller.asyncDataLoadedTreeView.getRoot().getValue()).isEqualTo("root");
	}

}
