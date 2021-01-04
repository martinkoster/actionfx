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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
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
	void testAccordion_noSelection() {
		// GIVEN
		final ControlWrapper wrapper = controlAccordion(false);
		final Accordion accordion = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, accordion.getPanes().get(0), accordion.getPanes().get(1));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testAccordion_firstTitledPaneIsSelected() {
		// GIVEN
		final ControlWrapper wrapper = controlAccordion(true);
		final Accordion accordion = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, accordion.getPanes().get(0), accordion.getPanes().get(1));
		assertSelectedValue(wrapper, accordion.getPanes().get(0));
		assertSelectedValues(wrapper, accordion.getPanes().get(0));
	}

	@Test
	void testButton() {
		// GIVEN
		final ControlWrapper wrapper = controlButton();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testButtonBar() {
		// GIVEN
		final ControlWrapper wrapper = controlButtonBar();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testCheckBox_isNotChecked() {
		// GIVEN
		final ControlWrapper wrapper = controlCheckBox(false);

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, false);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testCheckBox_isChecked() {
		// GIVEN
		final ControlWrapper wrapper = controlCheckBox(true);

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, true);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testChoiceBox_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlChoiceBox(false);

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testChoiceBox_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlChoiceBox(true);
		final ChoiceBox<String> cb = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Choice 2");
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, cb.getItems().get(1));
		assertSelectedValues(wrapper, "Choice 2");
	}

	@Test
	void testColorPicker() {
		// GIVEN
		final ControlWrapper wrapper = controlColorPicker();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, Color.AZURE);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testComboBox_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlComboBox(false);

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testComboBox_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlComboBox(true);

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Choice 2");
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 2");
		assertSelectedValues(wrapper, "Choice 2");
	}

	@Test
	void testDatePicker() {
		// GIVEN
		final ControlWrapper wrapper = controlDatePicker();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, LocalDate.of(2020, 12, 31));
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testHyperlink() {
		// GIVEN
		final ControlWrapper wrapper = controlHyperlink();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "https://www.google.com");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testLabel() {
		// GIVEN
		final ControlWrapper wrapper = controlLabel();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);

	}

	@Test
	void testListView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlListView(false);

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testListView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlListView(false, "Choice 2");

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 2");
		assertSelectedValues(wrapper, "Choice 2");
	}

	@Test
	void testListView_multiSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlListView(true);

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testListView_multiSelection_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlListView(true, "Choice 2", "Choice 3");

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 3");
		assertSelectedValues(wrapper, "Choice 2", "Choice 3");
	}

	@Test
	void testMenuBar() {
		// GIVEN
		final ControlWrapper wrapper = controlMenuBar();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testMenuButton() {
		// GIVEN
		final ControlWrapper wrapper = controlMenuButton();
		final MenuButton button = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Menu 2");
		assertThat(wrapper.getValues(), equalTo(button.getItems()));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testPagination() {
		// GIVEN
		final ControlWrapper wrapper = controlPagination();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testPasswordField() {
		// GIVEN
		final ControlWrapper wrapper = controlPasswordField();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Password");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testProgressBar() {
		// GIVEN
		final ControlWrapper wrapper = controlProgressBar();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testProgressIndicator() {
		// GIVEN
		final ControlWrapper wrapper = controlProgressIndicator();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testRadioButton_notSelected() {
		// GIVEN
		final ControlWrapper wrapper = controlRadioButton(false);

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, false);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testRadioButton_selected() {
		// GIVEN
		final ControlWrapper wrapper = controlRadioButton(true);

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, true);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testScrollBar() {
		// GIVEN
		final ControlWrapper wrapper = controlScrollBar();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testScrollPane() {
		// GIVEN
		final ControlWrapper wrapper = controlScrollPane();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testSeparator() {
		// GIVEN
		final ControlWrapper wrapper = controlSeparator();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);

	}

	@Test
	void testSlider() {
		// GIVEN
		final ControlWrapper wrapper = controlSlider();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testSpinner() {
		// GIVEN
		final ControlWrapper wrapper = controlSpinner();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testSplitMenuButton() {
		// GIVEN
		final ControlWrapper wrapper = controlSplitMenuButton();
		final SplitMenuButton button = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Menu 2");
		assertThat(wrapper.getValues(), equalTo(button.getItems()));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testSplitPane() {
		// GIVEN
		final ControlWrapper wrapper = controlSplitPane();
		final SplitPane pane = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertThat(wrapper.getValues(), equalTo(pane.getItems()));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testTableView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTableView(false);

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testTableView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTableView(false, "Item 2");

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, "Item 2");
		assertSelectedValues(wrapper, "Item 2");
	}

	@Test
	void testTableView_multiSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTableView(true);

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testTableView_multiSelection_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTableView(true, "Item 2", "Item 3");

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, "Item 3");
		assertSelectedValues(wrapper, "Item 2", "Item 3");
	}

	@Test
	void testTabPane_noTabSelected_defaultIsFirstTabIsSelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTabPane(false);
		final TabPane pane = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper.getValues(), pane.getTabs().get(0), pane.getTabs().get(1), pane.getTabs().get(2));
		assertSelectedValue(wrapper, pane.getTabs().get(0));
		assertSelectedValues(wrapper, pane.getTabs().get(0));
	}

	@Test
	void testTabPane_secondTabSelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTabPane(true);
		final TabPane pane = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper.getValues(), pane.getTabs().get(0), pane.getTabs().get(1), pane.getTabs().get(2));
		assertSelectedValue(wrapper, pane.getTabs().get(1));
		assertSelectedValues(wrapper, pane.getTabs().get(1));
	}

	@Test
	void testTextArea() {
		// GIVEN
		final ControlWrapper wrapper = controlTextArea();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testTextField() {
		// GIVEN
		final ControlWrapper wrapper = controlTextField();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testTitledPane() {
		// GIVEN
		final ControlWrapper wrapper = controlTitledPane();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Title 1");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testToggleButton_isNotToggled() {
		// GIVEN
		final ControlWrapper wrapper = controlToggleButton(false);

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, false);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testToggleButton_isToggled() {
		// GIVEN
		final ControlWrapper wrapper = controlToggleButton(true);

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, true);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testToolBar() {
		// GIVEN
		final ControlWrapper wrapper = controlToolBar();
		final ToolBar c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper.getValues(), c.getItems().get(0), c.getItems().get(1), c.getItems().get(2));
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testTreeTableView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTreeTableView(false, false, false);
		final TreeTableView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@SuppressWarnings("unchecked")
	@Test
	@Disabled
	void testTreeTableView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTreeTableView(false, true, false);
		final TreeTableView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root.getChildren().get(0));
		assertSelectedValues(wrapper, root.getChildren().get(0));
	}

	@Test
	void testTreeTableView_mutiSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTreeTableView(true, false, false);
		final TreeTableView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@SuppressWarnings("unchecked")
	@Test
	@Disabled
	void testTreeTableView_multiSelection_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTreeTableView(true, true, true);
		final TreeTableView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root);
		assertSelectedValues(wrapper, root.getChildren().get(0), root.getChildren().get(1));
	}

	@Test
	void testTreeView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTreeView(false, false, false);
		final TreeView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testTreeView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTreeView(false, true, false);
		final TreeView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root.getChildren().get(0));
		assertSelectedValues(wrapper, root.getChildren().get(0));
	}

	@Test
	void testTreeView_mutiSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTreeView(true, false, false);
		final TreeView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testTreeView_multiSelection_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = controlTreeView(true, true, true);
		final TreeView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root.getChildren().get(1));
		assertSelectedValues(wrapper, root.getChildren().get(0), root.getChildren().get(1));
	}

	@Test
	void testCustomControl() {
		// GIVEN
		final ControlWrapper wrapper = controlCustomControl();

		// WHEN and THEN
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
	}

	@Test
	void testEnableMultiSelection_tabViewHasMultipleSelectionModel() {
		// GIVEN
		final ControlWrapper wrapper = controlTableView(false);

		// WHEN and THEN (initially, multi-selection is not enabled)
		assertSupportsMultiSelection(wrapper, false);

		// and WHEN (multi-selection is enabled)
		wrapper.enableMultiSelection();

		// and THEN (multi-selection is supported)
		assertSupportsMultiSelection(wrapper, true);
	}

	@Test
	void testEnableMultiSelection_noEffectOnControlsWithoutSelectionModel() {
		// GIVEN
		final ControlWrapper wrapper = controlTextField();

		// WHEN and THEN (initially, multi-selection is not enabled)
		assertSupportsMultiSelection(wrapper, false);

		// and WHEN (multi-selection is enabled)
		wrapper.enableMultiSelection();

		// and THEN (multi-selection is still not supported - textfield has no selection
		// model)
		assertSupportsMultiSelection(wrapper, false);
	}

	@Test
	void testEnableMultiSelection_noEffectOnControlsWithSingleSelectionModel() {
		// GIVEN
		final ControlWrapper wrapper = controlChoiceBox(false);

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
		final ControlWrapper wrapper = controlTextField();
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
		final ControlWrapper wrapper = controlChoiceBox(false);
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
		final ControlWrapper wrapper = controlChoiceBox(false);
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
		final ControlWrapper wrapper = controlListView(true);
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

	private static ControlWrapper controlAccordion(final boolean selectFirst) {
		final Accordion c = new Accordion();
		final TitledPane p1 = new TitledPane();
		final TitledPane p2 = new TitledPane();
		c.getPanes().add(p1);
		c.getPanes().add(p2);
		if (selectFirst) {
			c.setExpandedPane(p1);
		}
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlButton() {
		final Button c = new Button();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlButtonBar() {
		final ButtonBar c = new ButtonBar();
		c.getButtons().add(new Button("Hello World"));
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlCheckBox(final boolean selected) {
		final CheckBox c = new CheckBox();
		c.setText("CheckBox");
		c.setSelected(selected);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlChoiceBox(final boolean selectSecond) {
		final ChoiceBox<String> c = new ChoiceBox<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlColorPicker() {
		final ColorPicker c = new ColorPicker();
		c.setValue(Color.AZURE);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlComboBox(final boolean selectSecond) {
		final ComboBox<String> c = new ComboBox<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlDatePicker() {
		final DatePicker c = new DatePicker();
		c.setValue(LocalDate.of(2020, 12, 31));
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlHyperlink() {
		final Hyperlink c = new Hyperlink();
		c.setText("https://www.google.com");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlLabel() {
		final Label c = new Label();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlListView(final boolean multiSelection, final String... selectedEntries) {
		final ListView<String> c = new ListView<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		if (multiSelection) {
			c.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		}
		if (selectedEntries != null) {
			for (final String entry : selectedEntries) {
				c.getSelectionModel().select(entry);
			}
		}
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlMenuBar() {
		final MenuBar c = new MenuBar();
		c.getMenus().add(new Menu("Menu 1"));
		c.getMenus().add(new Menu("Menu 1"));
		c.getMenus().add(new Menu("Menu 1"));
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlMenuButton() {
		final MenuButton c = new MenuButton();
		c.getItems().add(new MenuItem("Menu 1"));
		c.getItems().add(new MenuItem("Menu 2"));
		c.getItems().add(new MenuItem("Menu 3"));
		c.setText("Menu 2");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlPagination() {
		final Pagination c = new Pagination();
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlPasswordField() {
		final PasswordField c = new PasswordField();
		c.setText("Password");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlProgressBar() {
		final ProgressBar c = new ProgressBar();
		c.setProgress(0.75);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlProgressIndicator() {
		final ProgressIndicator c = new ProgressIndicator();
		c.setProgress(0.75);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlRadioButton(final boolean selected) {
		final RadioButton c = new RadioButton();
		c.setSelected(selected);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlScrollBar() {
		final ScrollBar c = new ScrollBar();
		c.setValue(0.75);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlScrollPane() {
		final ScrollPane c = new ScrollPane();
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlSeparator() {
		return new ControlWrapper(new Separator());
	}

	private static ControlWrapper controlSlider() {
		final Slider c = new Slider();
		c.setValue(0.75);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlSpinner() {
		final Spinner<Double> c = new Spinner<>(0.0, 1.0, 0.75);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlSplitMenuButton() {
		final SplitMenuButton c = new SplitMenuButton();
		c.getItems().add(new MenuItem("Menu 1"));
		c.getItems().add(new MenuItem("Menu 2"));
		c.getItems().add(new MenuItem("Menu 3"));
		c.setText("Menu 2");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlSplitPane() {
		final SplitPane c = new SplitPane();
		c.getItems().add(new AnchorPane());
		c.getItems().add(new BorderPane());
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlTableView(final boolean multiSelection, final String... selectedEntries) {
		final TableView<String> c = new TableView<>();
		c.getItems().add("Item 1");
		c.getItems().add("Item 2");
		c.getItems().add("Item 3");
		if (multiSelection) {
			c.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		}
		if (selectedEntries != null) {
			for (final String entry : selectedEntries) {
				c.getSelectionModel().select(entry);
			}
		}
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlTabPane(final boolean selectSecond) {
		final TabPane c = new TabPane();
		c.getTabs().add(new Tab("Tab 1"));
		c.getTabs().add(new Tab("Tab 2"));
		c.getTabs().add(new Tab("Tab 3"));
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlTextArea() {
		final TextArea c = new TextArea();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlTextField() {
		final TextField c = new TextField();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlTitledPane() {
		final TitledPane c = new TitledPane();
		c.setText("Title 1");
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlToggleButton(final boolean selected) {
		final ToggleButton c = new ToggleButton();
		c.setSelected(selected);
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlToolBar() {
		final ToolBar c = new ToolBar();
		c.getItems().add(new ToggleButton("Button 1"));
		c.getItems().add(new ToggleButton("Button 2"));
		c.getItems().add(new ToggleButton("Button 3"));
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlTreeTableView(final boolean multiSelection, final boolean selectSecond,
			final boolean selectThird) {
		final TreeTableView<String> c = new TreeTableView<>();
		final TreeItem<String> root = new TreeItem<>("root");
		final TreeItem<String> child1 = new TreeItem<>("child1");
		final TreeItem<String> child2 = new TreeItem<>("child2");
		final TreeTableColumn<String, String> column = new TreeTableColumn<>();
		column.setCellValueFactory(
				(final TreeTableColumn.CellDataFeatures<String, String> param) -> new ReadOnlyStringWrapper(
						param.getValue().getValue()));
		c.getColumns().add(column);
		root.getChildren().add(child1);
		root.getChildren().add(child2);
		c.setRoot(root);
		if (multiSelection) {
			c.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		}
		if (selectSecond) {
			c.getSelectionModel().select(child1);
		}
		if (selectThird) {
			c.getSelectionModel().select(child2);
		}
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlTreeView(final boolean multiSelection, final boolean selectSecond,
			final boolean selectThird) {
		final TreeView<String> c = new TreeView<>();
		final TreeItem<String> root = new TreeItem<>("root");
		final TreeItem<String> child1 = new TreeItem<>("child1");
		final TreeItem<String> child2 = new TreeItem<>("child2");
		root.getChildren().add(child1);
		root.getChildren().add(child2);
		c.setRoot(root);
		if (multiSelection) {
			c.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		}
		if (selectSecond) {
			c.getSelectionModel().select(child1);
		}
		if (selectThird) {
			c.getSelectionModel().select(child2);
		}
		return new ControlWrapper(c);
	}

	private static ControlWrapper controlCustomControl() {
		final CustomControl c = new CustomControl();
		c.setCustomValue("Hello World");
		return new ControlWrapper(c);
	}

}
