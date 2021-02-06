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
package com.github.actionfx.core.view.graph;

import java.time.LocalDate;

import javafx.beans.property.ReadOnlyStringWrapper;
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
 * Helper-class for providing {@link ControlWrapper} instances.
 *
 * @author koster
 *
 */
public class ControlWrapperProvider {

	private ControlWrapperProvider() {
		// class can not be instatiated
	}

	public static ControlWrapper accordion(final boolean selectFirst) {
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

	public static ControlWrapper button() {
		final Button c = new Button();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	public static ControlWrapper buttonBar() {
		final ButtonBar c = new ButtonBar();
		c.getButtons().add(new Button("Hello World"));
		return new ControlWrapper(c);
	}

	public static ControlWrapper checkBox(final boolean selected) {
		final CheckBox c = new CheckBox();
		c.setText("CheckBox");
		c.setSelected(selected);
		return new ControlWrapper(c);
	}

	public static ControlWrapper choiceBox(final boolean selectSecond) {
		final ChoiceBox<String> c = new ChoiceBox<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper colorPicker() {
		final ColorPicker c = new ColorPicker();
		c.setValue(Color.AZURE);
		return new ControlWrapper(c);
	}

	public static ControlWrapper comboBox(final boolean selectSecond) {
		final ComboBox<String> c = new ComboBox<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper datePicker() {
		final DatePicker c = new DatePicker();
		c.setValue(LocalDate.of(2020, 12, 31));
		return new ControlWrapper(c);
	}

	public static ControlWrapper hyperlink() {
		final Hyperlink c = new Hyperlink();
		c.setText("https://www.google.com");
		return new ControlWrapper(c);
	}

	public static ControlWrapper label() {
		final Label c = new Label();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	public static ControlWrapper listView(final boolean multiSelection, final String... selectedEntries) {
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

	public static ControlWrapper menuBar() {
		final MenuBar c = new MenuBar();
		c.getMenus().add(new Menu("Menu 1"));
		c.getMenus().add(new Menu("Menu 1"));
		c.getMenus().add(new Menu("Menu 1"));
		return new ControlWrapper(c);
	}

	public static ControlWrapper menuButton() {
		final MenuButton c = new MenuButton();
		c.getItems().add(new MenuItem("Menu 1"));
		c.getItems().add(new MenuItem("Menu 2"));
		c.getItems().add(new MenuItem("Menu 3"));
		c.setText("Menu 2");
		return new ControlWrapper(c);
	}

	public static ControlWrapper pagination() {
		final Pagination c = new Pagination();
		return new ControlWrapper(c);
	}

	public static ControlWrapper passwordField() {
		final PasswordField c = new PasswordField();
		c.setText("Password");
		return new ControlWrapper(c);
	}

	public static ControlWrapper progressBar() {
		final ProgressBar c = new ProgressBar();
		c.setProgress(0.75);
		return new ControlWrapper(c);
	}

	public static ControlWrapper progressIndicator() {
		final ProgressIndicator c = new ProgressIndicator();
		c.setProgress(0.75);
		return new ControlWrapper(c);
	}

	public static ControlWrapper radioButton(final boolean selected) {
		final RadioButton c = new RadioButton();
		c.setSelected(selected);
		return new ControlWrapper(c);
	}

	public static ControlWrapper scrollBar() {
		final ScrollBar c = new ScrollBar();
		c.setValue(0.75);
		return new ControlWrapper(c);
	}

	public static ControlWrapper scrollPane() {
		final ScrollPane c = new ScrollPane();
		return new ControlWrapper(c);
	}

	public static ControlWrapper separator() {
		return new ControlWrapper(new Separator());
	}

	public static ControlWrapper slider() {
		final Slider c = new Slider();
		c.setValue(0.75);
		return new ControlWrapper(c);
	}

	public static ControlWrapper spinner() {
		final Spinner<Double> c = new Spinner<>(0.0, 1.0, 0.75);
		return new ControlWrapper(c);
	}

	public static ControlWrapper splitMenuButton() {
		final SplitMenuButton c = new SplitMenuButton();
		c.getItems().add(new MenuItem("Menu 1"));
		c.getItems().add(new MenuItem("Menu 2"));
		c.getItems().add(new MenuItem("Menu 3"));
		c.setText("Menu 2");
		return new ControlWrapper(c);
	}

	public static ControlWrapper splitPane() {
		final SplitPane c = new SplitPane();
		c.getItems().add(new AnchorPane());
		c.getItems().add(new BorderPane());
		return new ControlWrapper(c);
	}

	public static ControlWrapper tableView(final boolean multiSelection, final String... selectedEntries) {
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

	public static ControlWrapper tabPane(final boolean selectSecond) {
		final TabPane c = new TabPane();
		c.getTabs().add(new Tab("Tab 1"));
		c.getTabs().add(new Tab("Tab 2"));
		c.getTabs().add(new Tab("Tab 3"));
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper textArea() {
		final TextArea c = new TextArea();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	public static ControlWrapper textField() {
		final TextField c = new TextField();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	public static ControlWrapper titledPane() {
		final TitledPane c = new TitledPane();
		c.setText("Title 1");
		return new ControlWrapper(c);
	}

	public static ControlWrapper toggleButton(final boolean selected) {
		final ToggleButton c = new ToggleButton();
		c.setSelected(selected);
		return new ControlWrapper(c);
	}

	public static ControlWrapper toolBar() {
		final ToolBar c = new ToolBar();
		c.getItems().add(new ToggleButton("Button 1"));
		c.getItems().add(new ToggleButton("Button 2"));
		c.getItems().add(new ToggleButton("Button 3"));
		return new ControlWrapper(c);
	}

	public static ControlWrapper treeTableView(final boolean multiSelection, final boolean selectSecond,
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

	public static ControlWrapper treeView(final boolean multiSelection, final boolean selectSecond,
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

	public static ControlWrapper customControl() {
		final CustomControl c = new CustomControl();
		c.setCustomValue("Hello World");
		return new ControlWrapper(c);
	}

	public static ControlWrapper customControlWithObservableList() {
		final CustomControlWithObservableList c = new CustomControlWithObservableList();
		c.getSourceItems().add("Item 1");
		c.getSourceItems().add("Item 2");
		c.getSourceItems().add("Item 3");
		return new ControlWrapper(c);
	}

}
