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
package com.github.actionfx.core.view.graph;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.collections.ValueChangeAwareObservableList;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;

/**
 * JUnit test case for {@link ControlWrapper}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
@TestInFxThread
class ControlWrapperTest {

	@Test
	void testOf_returnsIdenticalInstance_forSameControl() {
		// GIVEN
		final CheckBox checkBox = new CheckBox();

		// WHEN
		final ControlWrapper wrapperOne = ControlWrapper.of(checkBox);
		final ControlWrapper wrapperTwo = ControlWrapper.of(checkBox);

		// THEN
		assertThat(wrapperOne, notNullValue());
		assertThat(wrapperTwo, notNullValue());
		assertThat(wrapperOne, sameInstance(wrapperTwo));
	}

	@Test
	void testAccordion_noSelection() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.accordion(false);
		final Accordion accordion = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, accordion.getPanes().get(0), accordion.getPanes().get(1));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@Test
	void testAccordion_firstTitledPaneIsSelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.accordion(true);
		final Accordion accordion = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, accordion.getPanes().get(0));
		assertValues(wrapper, accordion.getPanes().get(0), accordion.getPanes().get(1));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, accordion.getPanes().get(0));
	}

	@Test
	void testButton() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.button();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Hello World");
	}

	@Test
	void testButtonBar() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.buttonBar();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValueIsNotSupported(wrapper);
	}

	@Test
	void testCheckBox_isNotChecked() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.checkBox(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, false);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, false);
	}

	@Test
	void testCheckBox_isChecked() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.checkBox(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, true);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, true);
	}

	@Test
	void testChoiceBox_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.choiceBox(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@Test
	void testChoiceBox_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.choiceBox(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Choice 2");
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 2");
		assertSelectedValues(wrapper, "Choice 2");
		assertUserValue(wrapper, "Choice 2");
	}

	@Test
	void testColorPicker() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.colorPicker();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, Color.AZURE);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Color.AZURE);
	}

	@Test
	void testComboBox_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.comboBox(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@Test
	void testComboBox_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.comboBox(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Choice 2");
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 2");
		assertSelectedValues(wrapper, "Choice 2");
		assertUserValue(wrapper, "Choice 2");
	}

	@Test
	void testComboBox_valueManuallySpecified() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.comboBox(false);
		final ComboBox<String> comboBox = wrapper.getWrapped();
		comboBox.setValue("Manually entered");

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Manually entered");
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Manually entered");
		assertSelectedValues(wrapper, "Manually entered");
		assertUserValue(wrapper, "Manually entered");
	}

	@Test
	void testDatePicker() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.datePicker();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, LocalDate.of(2020, 12, 31));
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, LocalDate.of(2020, 12, 31));
	}

	@Test
	void testHyperlink() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.hyperlink();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "https://www.google.com");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "https://www.google.com");
	}

	@Test
	void testLabel() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.label();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Hello World");

	}

	@Test
	void testListView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.listView(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@Test
	void testListView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.listView(false, "Choice 2");

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 2");
		assertSelectedValues(wrapper, "Choice 2");
		assertUserValue(wrapper, "Choice 2");
	}

	@Test
	void testListView_multiSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.listView(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Collections.EMPTY_LIST);
	}

	@Test
	void testListView_multiSelection_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.listView(true, "Choice 2", "Choice 3");

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 3");
		assertSelectedValues(wrapper, "Choice 2", "Choice 3");
		assertUserValue(wrapper, Arrays.asList("Choice 2", "Choice 3"));
	}

	@Test
	void testMenuBar() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.menuBar();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValueIsNotSupported(wrapper);
	}

	@Test
	void testMenuButton() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.menuButton();
		final MenuButton button = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Menu 2");
		assertThat(wrapper.getValues(), equalTo(button.getItems()));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Menu 2");
	}

	@Test
	void testPagination() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.pagination();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValueIsNotSupported(wrapper);
	}

	@Test
	void testPasswordField() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.passwordField();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Password");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Password");
	}

	@Test
	void testProgressBar() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.progressBar();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 0.75);
	}

	@Test
	void testProgressIndicator() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.progressIndicator();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 0.75);
	}

	@Test
	void testRadioButton_notSelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.radioButton(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, false);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, false);
	}

	@Test
	void testRadioButton_selected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.radioButton(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, true);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, true);
	}

	@Test
	void testScrollBar() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.scrollBar();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 0.75);
	}

	@Test
	void testScrollPane() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.scrollPane();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValueIsNotSupported(wrapper);
	}

	@Test
	void testSeparator() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.separator();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValueIsNotSupported(wrapper);
	}

	@Test
	void testSlider() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.slider();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 0.75);
	}

	@Test
	void testSpinner() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.spinner();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 0.75);
	}

	@Test
	void testSplitMenuButton() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.splitMenuButton();
		final SplitMenuButton button = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Menu 2");
		assertThat(wrapper.getValues(), equalTo(button.getItems()));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Menu 2");
	}

	@Test
	void testSplitPane() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.splitPane();
		final SplitPane pane = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertThat(wrapper.getValues(), equalTo(pane.getItems()));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, pane.getItems());
	}

	@Test
	void testTableView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.tableView(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@Test
	void testTableView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.tableView(false, "Item 2");

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, "Item 2");
		assertSelectedValues(wrapper, "Item 2");
		assertUserValue(wrapper, "Item 2");
	}

	@Test
	void testTableView_multiSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.tableView(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Collections.EMPTY_LIST);
	}

	@Test
	void testTableView_multiSelection_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.tableView(true, "Item 2", "Item 3");

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, "Item 3");
		assertSelectedValues(wrapper, "Item 2", "Item 3");
		assertUserValue(wrapper, Arrays.asList("Item 2", "Item 3"));
	}

	@Test
	void testTabPane_noTabSelected_defaultIsFirstTabIsSelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.tabPane(false);
		final TabPane pane = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper.getValues(), pane.getTabs().get(0), pane.getTabs().get(1), pane.getTabs().get(2));
		assertSelectedValue(wrapper, pane.getTabs().get(0));
		assertSelectedValues(wrapper, pane.getTabs().get(0));
		assertUserValue(wrapper, pane.getTabs().get(0));
	}

	@Test
	void testTabPane_secondTabSelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.tabPane(true);
		final TabPane pane = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper.getValues(), pane.getTabs().get(0), pane.getTabs().get(1), pane.getTabs().get(2));
		assertSelectedValue(wrapper, pane.getTabs().get(1));
		assertSelectedValues(wrapper, pane.getTabs().get(1));
		assertUserValue(wrapper, pane.getTabs().get(1));
	}

	@Test
	void testTextArea() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.textArea();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Hello World");
	}

	@Test
	void testTextField() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.textField();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Hello World");
	}

	@Test
	void testTitledPane() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.titledPane();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Title 1");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Title 1");
	}

	@Test
	void testToggleButton_isNotToggled() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.toggleButton(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, false);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, false);
	}

	@Test
	void testToggleButton_isToggled() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.toggleButton(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, true);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, true);
	}

	@Test
	void testToolBar() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.toolBar();
		final ToolBar c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper.getValues(), c.getItems().get(0), c.getItems().get(1), c.getItems().get(2));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, c.getItems());
	}

	@Test
	void testTreeTableView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.treeTableView(false, false, false);
		final TreeTableView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	@Disabled
	void testTreeTableView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.treeTableView(false, true, false);
		final TreeTableView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root.getChildren().get(0));
		assertSelectedValues(wrapper, root.getChildren().get(0));
		assertUserValue(wrapper, root.getChildren().get(0));
	}

	@Test
	void testTreeTableView_mutiSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.treeTableView(true, false, false);
		final TreeTableView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Collections.EMPTY_LIST);
	}

	@SuppressWarnings("unchecked")
	@Test
	@Disabled
	void testTreeTableView_multiSelection_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.treeTableView(true, true, true);
		final TreeTableView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root);
		assertSelectedValues(wrapper, root.getChildren().get(0), root.getChildren().get(1));
		assertUserValue(wrapper, Arrays.asList(root.getChildren().get(0), root.getChildren().get(1)));
	}

	@Test
	void testTreeView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.treeView(false, false, false);
		final TreeView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testTreeView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.treeView(false, true, false);
		final TreeView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root.getChildren().get(0));
		assertSelectedValues(wrapper, root.getChildren().get(0));
		assertUserValue(wrapper, root.getChildren().get(0));
	}

	@Test
	void testTreeView_mutiSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.treeView(true, false, false);
		final TreeView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Collections.EMPTY_LIST);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testTreeView_multiSelection_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.treeView(true, true, true);
		final TreeView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root.getChildren().get(1));
		assertSelectedValues(wrapper, root.getChildren().get(0), root.getChildren().get(1));
		assertUserValue(wrapper, Arrays.asList(root.getChildren().get(0), root.getChildren().get(1)));
	}

	@Test
	void testCustomControl() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.customControl();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, "Hello World");
		assertValues(wrapper, "", "", "");
		assertSelectedValue(wrapper, null);
		assertSelectedValues(wrapper, "Selected Value");// comes from CustomSelectionModelWrapper
		assertUserValue(wrapper, Arrays.asList("Selected Value")); // comes from CustomSelectionModelWrapper
	}

	@Test
	void testCustomControl_commaSeparatedList_asObservableValueList() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.customControl();
		final CustomControl cc = wrapper.getWrapped();
		cc.setValueOne("one");
		cc.setValueTwo("two");
		cc.setValueThree("three");

		// WHEN
		final ObservableList<String> values = wrapper.getValues();

		// THEN
		assertThat(values, instanceOf(ValueChangeAwareObservableList.class));
		assertThat(values, contains("one", "two", "three"));
	}

	@Test
	void testCustomControlWithObservableList_asSelectionModel() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.customControlWithObservableList();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);// comes from ObservableListSelectionModel
		assertUserValue(wrapper, Collections.emptyList()); // comes from ObservableListSelectionModel
	}

	@Test
	void testCustomControlWithObservableList_asSelectionModel_selectIntoTargetItems() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.customControlWithObservableList();
		final CustomControlWithObservableList cc = wrapper.getWrapped();
		cc.getTargetItems().add("Item 2");
		cc.getTargetItems().add("Item 3");

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, "Item 3");
		assertSelectedValues(wrapper, "Item 2", "Item 3"); // comes from ObservableListSelectionModel
		assertUserValue(wrapper, Arrays.asList("Item 2", "Item 3")); // comes from ObservableListSelectionModel
	}

	@Test
	void testGetSelectionModel_customSelectionModel_isWrapped() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.customControl();
		final CustomControl control = wrapper.getWrapped();

		// WHEN and THEN
		assertThat(wrapper.getSelectionModel(), notNullValue());
		assertThat(wrapper.getSelectionModel(), instanceOf(CustomSelectionModelWrapper.class));
		final SelectionModel<String> selectionModel = wrapper.getSelectionModel();
		final CustomSelectionModelWrapper selectionModelWrapper = (CustomSelectionModelWrapper) selectionModel;
		assertThat(selectionModelWrapper.getCustomSelectionModel(), sameInstance(control.getCustomSelectionModel()));
	}

	@Test
	void testEnableMultiSelection_tabViewHasMultipleSelectionModel() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.tableView(false);

		// WHEN and THEN (initially, multi-selection is not enabled)
		assertSupportsMultiSelection(wrapper, false);

		// and WHEN (multi-selection is enabled)
		wrapper.enableMultiSelection();

		// and THEN (multi-selection is supported)
		assertSupportsMultiSelection(wrapper, true);
	}

	@Test
	void testEnableMultiSelection_noEffectOnControlWithoutSelectionModel() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.textField();

		// WHEN and THEN (initially, multi-selection is not enabled)
		assertSupportsMultiSelection(wrapper, false);

		// and WHEN (multi-selection is enabled)
		wrapper.enableMultiSelection();

		// and THEN (multi-selection is still not supported - textfield has no selection
		// model)
		assertSupportsMultiSelection(wrapper, false);
	}

	@Test
	void testEnableMultiSelection_noEffectOnControlWithSingleSelectionModel() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.choiceBox(false);

		// WHEN and THEN (initially, multi-selection is not enabled)
		assertSupportsMultiSelection(wrapper, false);

		// and WHEN (multi-selection is enabled)
		wrapper.enableMultiSelection();

		// and THEN (multi-selection is still not supported - choicebox only has a
		// SingleSelectionModel)
		assertSupportsMultiSelection(wrapper, false);
	}

	@Test
	void testAddValueChangeListener() {
		// GIVEN
		final BooleanProperty changeListenerFired = new SimpleBooleanProperty(false);
		final ChangeListener<String> listener = (observable, oldValue, newValue) -> changeListenerFired.set(true);
		final ControlWrapper wrapper = ControlWrapperProvider.textField();
		final TextField textField = wrapper.getWrapped();

		// WHEN
		wrapper.addValueChangeListener(listener);
		textField.setText("Change Value");

		// THEN
		assertThat(changeListenerFired.get(), equalTo(true));

		// and GIVEN
		changeListenerFired.set(false);

		// and WHEN (the listener is removed again)
		wrapper.removeAllValueChangeListener();

		// and WHEN (a value change occurs)
		textField.setText("Change value again");

		// and THEN ( the listener is not fired, because it is removed already)
		assertThat(changeListenerFired.get(), equalTo(false));
	}

	@Test
	void testAddValuesChangeListener() {
		// GIVEN
		final BooleanProperty changeListenerFired = new SimpleBooleanProperty(false);
		final ListChangeListener<String> listener = change -> changeListenerFired.set(true);
		final ControlWrapper wrapper = ControlWrapperProvider.choiceBox(false);
		final ChoiceBox<String> choiceBox = wrapper.getWrapped();

		// WHEN
		wrapper.addValuesChangeListener(listener);
		choiceBox.getItems().add("Choice 4");

		// THEN
		assertThat(changeListenerFired.get(), equalTo(true));

		// and GIVEN
		changeListenerFired.set(false);

		// and WHEN (the listener is removed again)
		wrapper.removeAllValuesChangeListener();

		// and WHEN (a value change occurs)
		choiceBox.getItems().add("Choice 5");

		// and THEN ( the listener is not fired, because it is removed already)
		assertThat(changeListenerFired.get(), equalTo(false));
	}

	@Test
	void testAddSelectedValueChangeListener() {
		// GIVEN
		final BooleanProperty changeListenerFired = new SimpleBooleanProperty(false);
		final ChangeListener<String> listener = (observable, oldValue, newValue) -> changeListenerFired.set(true);
		final ControlWrapper wrapper = ControlWrapperProvider.choiceBox(false);
		final ChoiceBox<String> choiceBox = wrapper.getWrapped();

		// WHEN
		wrapper.addSelectedValueChangeListener(listener);
		choiceBox.getSelectionModel().select("Choice 2");

		// THEN
		assertThat(changeListenerFired.get(), equalTo(true));

		// and GIVEN
		changeListenerFired.set(false);

		// and WHEN (the listener is removed again)
		wrapper.removeAllSelectedValueChangeListener();

		// and WHEN (a value change occurs)
		choiceBox.getSelectionModel().select("Choice 1");

		// and THEN ( the listener is not fired, because it is removed already)
		assertThat(changeListenerFired.get(), equalTo(false));
	}

	@Test
	void testAddSelectedValuesChangeListener() {
		// GIVEN
		final BooleanProperty changeListenerFired = new SimpleBooleanProperty(false);
		final ListChangeListener<String> listener = change -> changeListenerFired.set(true);
		final ControlWrapper wrapper = ControlWrapperProvider.listView(true);
		final ListView<String> listView = wrapper.getWrapped();

		// WHEN
		wrapper.addSelectedValuesChangeListener(listener);
		listView.getSelectionModel().selectAll();

		// THEN
		assertThat(changeListenerFired.get(), equalTo(true));

		// and GIVEN
		changeListenerFired.set(false);

		// and WHEN (the listener is removed again)
		wrapper.removeAllSelectedValuesChangeListener();

		// and WHEN (a value change occurs)
		listView.getSelectionModel().clearSelection();

		// and THEN ( the listener is not fired, because it is removed already)
		assertThat(changeListenerFired.get(), equalTo(false));
	}

	@Test
	void testGetOnActionProperty() {
		// GIVEN
		final ControlWrapper wrapper = ControlWrapperProvider.button();

		// WHEN
		final ObjectProperty<EventHandler<ActionEvent>> onActionProperty = wrapper.getOnActionProperty();

		// THEN
		assertThat(onActionProperty, notNullValue());
	}

	private static <V> void assertValue(final ControlWrapper wrapper, final V expectedValue) {
		assertThat(wrapper.getValue(), equalTo(expectedValue));
	}

	@SuppressWarnings("unchecked")
	private static <V> void assertValues(final ControlWrapper wrapper, final V... expectedValues) {
		assertThat(wrapper.getValues(), contains(expectedValues));
	}

	@SuppressWarnings("unchecked")
	private static <V> void assertValues(final List<V> actualValues, final V... expectedValues) {
		assertThat(actualValues, contains(expectedValues));
	}

	private static <V> void assertValuesAreEmpty(final ControlWrapper wrapper) {
		assertThat(wrapper.getValues(), hasSize(0));
	}

	private static void assertSupportsSelection(final ControlWrapper wrapper, final boolean expected) {
		assertThat(wrapper.supportsSelection(), equalTo(expected));
	}

	private static void assertSupportsMultiSelection(final ControlWrapper wrapper, final boolean expected) {
		assertThat(wrapper.supportsMultiSelection(), equalTo(expected));
	}

	private static void assertSupportsValue(final ControlWrapper wrapper, final boolean expected) {
		assertThat(wrapper.supportsValue(), equalTo(expected));
	}

	private static void assertSupportsValues(final ControlWrapper wrapper, final boolean expected) {
		assertThat(wrapper.supportsValues(), equalTo(expected));
	}

	private static <V> void assertSelectedValue(final ControlWrapper wrapper, final V expectedValue) {
		assertThat(wrapper.getSelectedValue(), equalTo(expectedValue));
	}

	@SuppressWarnings("unchecked")
	private static <V> void assertSelectedValues(final ControlWrapper wrapper, final V... expectedValues) {
		assertThat(wrapper.getSelectedValues(), equalTo(Arrays.asList(expectedValues)));
	}

	private static <V> void assertSelectedValuesAreEmpty(final ControlWrapper wrapper) {
		assertThat(wrapper.getSelectedValues(), hasSize(0));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void assertUserValue(final ControlWrapper wrapper, final Object expectedValue) {
		final Object actualValue = wrapper.getUserValue();
		if (actualValue == null && expectedValue == null) {
			return;
		}
		if (expectedValue != null && List.class.isAssignableFrom(expectedValue.getClass())) {
			final List expectedValueList = (List) expectedValue;
			if (expectedValueList.isEmpty()) {
				assertThat(((List) actualValue).isEmpty(), equalTo(true));
			} else {
				assertThat((List<Object>) actualValue, contains(expectedValueList.toArray()));
			}
		} else {
			assertThat(actualValue, equalTo(expectedValue));
		}
	}

	private static void assertUserValueIsNotSupported(final ControlWrapper wrapper) {
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> wrapper.getUserValue());
		assertThat(ex.getMessage(), containsString("does not support user value retrieval!"));
	}
}
