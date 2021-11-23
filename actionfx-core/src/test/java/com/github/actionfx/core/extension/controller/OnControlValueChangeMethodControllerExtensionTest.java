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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.annotation.AFXArgHint;
import com.github.actionfx.core.annotation.AFXOnControlValueChange;
import com.github.actionfx.core.annotation.ArgumentHint;
import com.github.actionfx.core.extension.controller.OnControlValueChangeMethodControllerExtension;
import com.github.actionfx.core.test.ViewCreator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link OnControlValueChangeMethodControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class OnControlValueChangeMethodControllerExtensionTest {

	@SuppressWarnings("unchecked")
	@Test
	@TestInFxThread
	void testAccept_valueChangetextField_allListenersAreActive() {
		// GIVEN
		final TextField textField = new TextField();
		final ControllerWithTextField controller = Mockito
				.spy(new ControllerWithTextField(ViewCreator.create(textField, "textField")));
		final OnControlValueChangeMethodControllerExtension extension = new OnControlValueChangeMethodControllerExtension();

		// WHEN
		extension.accept(controller);
		textField.setText("Hello World"); // triggers listener

		// THEN (all 3 annotated methods are invoked)
		verify(controller, times(1)).onTextFieldValueChange(Mockito.eq("Hello World"));
		verify(controller, times(1)).onTextFieldValueChangeWithNewAndOldValue(Mockito.eq("Hello World"), Mockito.eq(""),
				(ObservableValue<String>) Mockito.any(Observable.class));
		verify(controller, times(1)).onTextFieldValueChangeWithAnnotatedArguments(Mockito.eq(""),
				Mockito.eq("Hello World"), (ObservableValue<String>) Mockito.any(Observable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	@TestInFxThread
	void testAccept_valueChangetextField_onlyOneListenerIsActive_usingListenerActiveBooleanPropertyInAnnotation() {
		// GIVEN
		final TextField textField = new TextField();
		final ControllerWithTextField controller = Mockito
				.spy(new ControllerWithTextField(ViewCreator.create(textField, "textField")));
		final OnControlValueChangeMethodControllerExtension extension = new OnControlValueChangeMethodControllerExtension();

		// WHEN
		extension.accept(controller);
		controller.listenerEnabled.set(false);
		textField.setText("Hello World"); // triggers listener

		// THEN (only 1 method invocation is invoked)
		verify(controller, times(1)).onTextFieldValueChange(Mockito.eq("Hello World"));
		verify(controller, times(0)).onTextFieldValueChangeWithNewAndOldValue(Mockito.any(String.class),
				Mockito.any(String.class), (ObservableValue<String>) Mockito.any(Observable.class));
		verify(controller, times(0)).onTextFieldValueChangeWithAnnotatedArguments(Mockito.any(String.class),
				Mockito.any(String.class), (ObservableValue<String>) Mockito.any(Observable.class));
	}

	@Test
	@TestInFxThread
	void testAccept_selection_singleValueChange_inTableView() {
		// GIVEN
		final TableView<String> tableView = new TableView<>();
		tableView.getItems().add("Item 1");
		tableView.getItems().add("Item 2");
		tableView.getItems().add("Item 3");
		final ControllerWithSingleSelectionTable controller = Mockito
				.spy(new ControllerWithSingleSelectionTable(ViewCreator.create(tableView, "singleSelectionTable")));
		final OnControlValueChangeMethodControllerExtension extension = new OnControlValueChangeMethodControllerExtension();

		// WHEN
		extension.accept(controller);
		tableView.getSelectionModel().select("Item 2");

		// THEN
		verify(controller, times(1)).onSelectValueInSingleSelectionTable(Mockito.eq("Item 2"));
		verify(controller, times(1)).onSelectValueInSingleSelectionTableWithList(Mockito.eq(Arrays.asList("Item 2")));
	}

	@SuppressWarnings("unchecked")
	@Test
	@TestInFxThread
	void testAccept_selection_multiValueChange_inTableView() {
		// GIVEN
		final TableView<String> tableView = new TableView<>();
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableView.getItems().add("Item 1");
		tableView.getItems().add("Item 2");
		tableView.getItems().add("Item 3");
		final ControllerWithMultiSelectionTable controller = Mockito
				.spy(new ControllerWithMultiSelectionTable(ViewCreator.create(tableView, "multiSelectionTable")));
		final OnControlValueChangeMethodControllerExtension extension = new OnControlValueChangeMethodControllerExtension();

		// WHEN
		extension.accept(controller);
		tableView.getSelectionModel().selectAll();

		// THEN
		verify(controller, times(1))
				.onSelectValueInMultiSelectionTable(Mockito.eq(Arrays.asList("Item 1", "Item 2", "Item 3")));
		verify(controller, times(1)).onSelectValueInMultiSelectionTableWithFullArguments(
				Mockito.eq(Arrays.asList("Item 1", "Item 2", "Item 3")),
				Mockito.eq(Arrays.asList("Item 1", "Item 2", "Item 3")), Mockito.eq(Collections.emptyList()),
				Mockito.eq(null), Mockito.any(Change.class));
		verify(controller, times(1)).onSelectValueInMultiSelectionTableWithAnnotatedArguments(
				Mockito.eq(Arrays.asList("Item 1", "Item 2", "Item 3")),
				Mockito.eq(Arrays.asList("Item 1", "Item 2", "Item 3")), Mockito.eq(Collections.emptyList()),
				Mockito.eq(null), Mockito.any(Change.class));
	}

	public class ControllerWithTextField {

		public View _view;

		// switching this boolean property to false will deactivate all method
		// invocation
		// that reference this listenerActiveProperty
		protected final BooleanProperty listenerEnabled = new SimpleBooleanProperty(true);

		public ControllerWithTextField(final View view) {
			_view = view;
		}

		// order 10: changes on textfield will trigger this method first
		@AFXOnControlValueChange(controlId = "textField", order = 10)
		public void onTextFieldValueChange(final String value) {
		}

		// order 30: changes on textfield will trigger this method third
		@AFXOnControlValueChange(controlId = "textField", order = 30, listenerActiveBooleanProperty = "listenerEnabled")
		public void onTextFieldValueChangeWithAnnotatedArguments(
				@AFXArgHint(ArgumentHint.OLD_VALUE) final String oldValue,
				@AFXArgHint(ArgumentHint.NEW_VALUE) final String newValue, final ObservableValue<String> observable) {
		}

		// order 20: changes on textfield will trigger this method second
		@AFXOnControlValueChange(controlId = "textField", order = 20, listenerActiveBooleanProperty = "listenerEnabled")
		public void onTextFieldValueChangeWithNewAndOldValue(final String newValue, final String oldValue,
				final ObservableValue<String> observable) {
		}
	}

	public class ControllerWithSingleSelectionTable {

		public View _view;

		// switching this boolean property to false will deactivate all method
		// invocation
		// that reference this listenerActiveProperty
		protected final BooleanProperty listenerEnabled = new SimpleBooleanProperty(true);

		public ControllerWithSingleSelectionTable(final View view) {
			_view = view;
		}

		@AFXOnControlValueChange(controlId = "singleSelectionTable", order = 1)
		public void onSelectValueInSingleSelectionTable(final String selected) {
		}

		@AFXOnControlValueChange(controlId = "singleSelectionTable", order = 2)
		public void onSelectValueInSingleSelectionTableWithList(final List<String> selected) {
		}
	}

	public class ControllerWithMultiSelectionTable {

		public View _view;

		// switching this boolean property to false will deactivate all method
		// invocation
		// that reference this listenerActiveProperty
		protected final BooleanProperty listenerEnabled = new SimpleBooleanProperty(true);

		public ControllerWithMultiSelectionTable(final View view) {
			_view = view;
		}

		@AFXOnControlValueChange(controlId = "multiSelectionTable", order = 1)
		public void onSelectValueInMultiSelectionTable(final List<String> selected) {
		}

		@AFXOnControlValueChange(controlId = "multiSelectionTable", order = 2)
		public void onSelectValueInMultiSelectionTableWithFullArguments(final List<String> selected,
				final List<String> added, final List<String> removed, final String lastSelected,
				final ListChangeListener.Change<String> change) {
		}

		@AFXOnControlValueChange(controlId = "multiSelectionTable", order = 3)
		public void onSelectValueInMultiSelectionTableWithAnnotatedArguments(
				@AFXArgHint(ArgumentHint.ADDED_VALUES) final List<String> added,
				@AFXArgHint(ArgumentHint.ALL_SELECTED) final List<String> selected,
				@AFXArgHint(ArgumentHint.REMOVED_VALUES) final List<String> removed, final String lastSelected,
				final ListChangeListener.Change<String> change) {
		}
	}
}
