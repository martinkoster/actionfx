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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.annotation.AFXCellValueConfig;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.StringConverter;

/**
 * JUnit test case for {@link CellValueConfigControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class CellValueConfigControllerExtensionTest {

	@Test
	void testAccept_tableView_configureByIndex() {
		// GIVEN
		final ControllerWithTableViewConfiguredByIndex controller = new ControllerWithTableViewConfiguredByIndex();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		fillTableView(controller.tableView);
		assertCellContains(controller.tableView, 0, 0, "John Doe");
		assertCellContains(controller.tableView, 0, 1, 42.6);
		assertCellContains(controller.tableView, 1, 0, "Jane Doe");
		assertCellContains(controller.tableView, 1, 1, 37.1);

		// check conversion of double to string
		assertCellConvertsValueToString(controller.tableView, 1, 42.6, "42.6");
	}

	@Test
	void testAccept_tableView_configureByIndex_wrongIndex() {
		// GIVEN
		final ControllerWithTableViewConfiguredByWrongIndex controller = new ControllerWithTableViewConfiguredByWrongIndex();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"Annotation @AFXTableColum is applied to a javafx.scene.control.TableView, but requested column index exceeds the number of available columns!"));
	}

	@Test
	void testAccept_tableView_configureById() {
		// GIVEN
		final ControllerWithTableViewConfiguredById controller = new ControllerWithTableViewConfiguredById();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		fillTableView(controller.tableView);
		assertCellContains(controller.tableView, 0, 0, "John Doe");
		assertCellContains(controller.tableView, 0, 1, 42.6);
		assertCellContains(controller.tableView, 1, 0, "Jane Doe");
		assertCellContains(controller.tableView, 1, 1, 37.1);

		// check conversion of double to string
		assertCellConvertsValueToString(controller.tableView, 1, 42.6, "42.6");
	}

	@Test
	void testAccept_tableView_configureById_wrongId() {
		// GIVEN
		final ControllerWithTableViewConfiguredByWrongId controller = new ControllerWithTableViewConfiguredByWrongId();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"Annotation @AFXTableColum is applied to a javafx.scene.control.TableView, but requested column ID is not present!"));
	}

	@Test
	void testAccept_tableView_configureTableColumns() {
		// GIVEN
		final ControllerWithTableColumns controller = new ControllerWithTableColumns();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		fillTableView(controller.tableView);
		assertCellContains(controller.tableView, 0, 0, "John Doe");
		assertCellContains(controller.tableView, 0, 1, 42.6);
		assertCellContains(controller.tableView, 1, 0, "Jane Doe");
		assertCellContains(controller.tableView, 1, 1, 37.1);

		// check conversion of double to string
		assertCellConvertsValueToString(controller.tableView, 1, 42.6, "42.6");
	}

	@Test
	void testAccept_tableView_annotatedFieldIsNull() {
		// GIVEN
		final ControllerWithNullField controller = new ControllerWithNullField();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"Field 'tableView' in controller 'com.github.actionfx.core.container.extension.CellValueConfigControllerExtensionTest.ControllerWithNullField' is annotated by @AFXCellValueConfig, but value is null!"));
	}

	@Test
	void testAccept_tableView_annotatationDoesNotDefineColIdOrColIdx() {
		// GIVEN
		final ControllerWithInvalidAnnotation controller = new ControllerWithInvalidAnnotation();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"Annotation @AFXTableColum is applied to a javafx.scene.control.TableView, but annotation does not define, which column shall be configured!"));
	}

	void fillTableView(final TableView<Person> tableView) {
		tableView.getItems().add(new Person("John Doe", 42.6));
		tableView.getItems().add(new Person("Jane Doe", 37.1));
	}

	@SuppressWarnings("rawtypes")
	void assertCellContains(final TableView tableView, final int row, final int col, final Object expectedValue) {
		final TableColumn column = (TableColumn) tableView.getColumns().get(col);
		assertThat(column.getCellData(row), equalTo(expectedValue));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void assertCellConvertsValueToString(final TableView tableView, final int col, final Object value,
			final String expectedText) {
		final TableColumn column = (TableColumn) tableView.getColumns().get(col);
		final TextFieldTableCell cell = (TextFieldTableCell) column.getCellFactory().call(column);
		cell.updateItem(value, false);
		assertThat(cell.getText(), equalTo(expectedText));
	}

	@Test
	void testAccept_treeTableView_configureByIndex() {
		// GIVEN
		final ControllerWithTreeTableViewConfiguredByIndex controller = new ControllerWithTreeTableViewConfiguredByIndex();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		fillTreeTableView(controller.tableView);
		assertCellContains(controller.tableView, 0, 0, "John Doe");
		assertCellContains(controller.tableView, 0, 1, 42.6);

		// check conversion of double to string
		assertCellConvertsValueToString(controller.tableView, 1, 42.6, "42.6");
	}

	@Test
	void testAccept_treeTableView_configureByIndex_wrongIndex() {
		// GIVEN
		final ControllerWithTreeTableViewConfiguredByWrongIndex controller = new ControllerWithTreeTableViewConfiguredByWrongIndex();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"Annotation @AFXTableColum is applied to a javafx.scene.control.TreeTableView, but requested column index exceeds the number of available columns!"));
	}

	@Test
	void testAccept_treeTableView_configureById() {
		// GIVEN
		final ControllerWithTreeTableViewConfiguredById controller = new ControllerWithTreeTableViewConfiguredById();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		fillTreeTableView(controller.tableView);
		assertCellContains(controller.tableView, 0, 0, "John Doe");
		assertCellContains(controller.tableView, 0, 1, 42.6);

		// check conversion of double to string
		assertCellConvertsValueToString(controller.tableView, 1, 42.6, "42.6");
	}

	@Test
	void testAccept_treeTableView_configureById_wrongId() {
		// GIVEN
		final ControllerWithTreeTableViewConfiguredByWrongId controller = new ControllerWithTreeTableViewConfiguredByWrongId();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"Annotation @AFXTableColum is applied to a javafx.scene.control.TreeTableView, but requested column ID is not present!"));
	}

	@Test
	void testAccept_treeTableView_configureTableColumns() {
		// GIVEN
		final ControllerWithTreeTableColumns controller = new ControllerWithTreeTableColumns();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		fillTreeTableView(controller.tableView);
		assertCellContains(controller.tableView, 0, 0, "John Doe");
		assertCellContains(controller.tableView, 0, 1, 42.6);

		// check conversion of double to string
		assertCellConvertsValueToString(controller.tableView, 1, 42.6, "42.6");
	}

	@Test
	void testAccept_treeTableView_annotatedFieldIsNull() {
		// GIVEN
		final ControllerWithTreeTableNullField controller = new ControllerWithTreeTableNullField();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"Field 'tableView' in controller 'com.github.actionfx.core.container.extension.CellValueConfigControllerExtensionTest.ControllerWithTreeTableNullField' is annotated by @AFXCellValueConfig, but value is null!"));
	}

	@Test
	void testAccept_treeTableView_annotatationDoesNotDefineColIdOrColIdx() {
		// GIVEN
		final ControllerWithTreeTableInvalidAnnotation controller = new ControllerWithTreeTableInvalidAnnotation();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"Annotation @AFXTableColum is applied to a javafx.scene.control.TreeTableView, but annotation does not define, which column shall be configured!"));
	}

	void fillTreeTableView(final TreeTableView<Person> tableView) {
		final TreeItem<Person> root = new TreeItem<>(new Person("John Doe", 42.6));
		tableView.setRoot(root);
	}

	@SuppressWarnings("rawtypes")
	void assertCellContains(final TreeTableView tableView, final int row, final int col, final Object expectedValue) {
		final TreeTableColumn column = (TreeTableColumn) tableView.getColumns().get(col);
		assertThat(column.getCellData(row), equalTo(expectedValue));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void assertCellConvertsValueToString(final TreeTableView tableView, final int col, final Object value,
			final String expectedText) {
		final TreeTableColumn column = (TreeTableColumn) tableView.getColumns().get(col);
		final TextFieldTreeTableCell cell = (TextFieldTreeTableCell) column.getCellFactory().call(column);
		cell.updateItem(value, false);
		assertThat(cell.getText(), equalTo(expectedText));
	}

	@Test
	void testAccept_treeView() {
		// GIVEN
		final ControllerWithTreeView controller = new ControllerWithTreeView();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		fillTreeView(controller.treeView);
		assertCellConvertsValueToString(controller.treeView, controller.treeView.getRoot().getValue(),
				"John Doe (age 42.6)");
	}

	void fillTreeView(final TreeView<Person> treeView) {
		final TreeItem<Person> root = new TreeItem<>(new Person("John Doe", 42.6));
		treeView.setRoot(root);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void assertCellConvertsValueToString(final TreeView treeView, final Object value, final String expectedText) {
		final TextFieldTreeCell cell = (TextFieldTreeCell) treeView.getCellFactory().call(treeView);
		cell.updateItem(value, false);
		assertThat(cell.getText(), equalTo(expectedText));
	}

	@Test
	void testAccept_listView() {
		// GIVEN
		final ControllerWithListView controller = new ControllerWithListView();
		final CellValueConfigControllerExtension extension = new CellValueConfigControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		fillTreeView(controller.listView);
		assertCellConvertsValueToString(controller.listView, controller.listView.getItems().get(0),
				"John Doe (age 42.6)");
	}

	void fillTreeView(final ListView<Person> listView) {
		listView.getItems().add(new Person("John Doe", 42.6));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void assertCellConvertsValueToString(final ListView<Person> listView, final Object value,
			final String expectedText) {
		final TextFieldListCell cell = (TextFieldListCell) listView.getCellFactory().call(listView);
		cell.updateItem(value, false);
		assertThat(cell.getText(), equalTo(expectedText));
	}

	public class ControllerWithTableViewConfiguredByIndex {

		public View _view;

		@AFXCellValueConfig(colIdx = 0, propertyValue = "name")
		@AFXCellValueConfig(colIdx = 1, propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TableView<Person> tableView = new TableView<>();

		public ControllerWithTableViewConfiguredByIndex() {
			final TableColumn<Person, String> nameColumn = new TableColumn<>();
			final TableColumn<Person, Double> ageColumn = new TableColumn<>();
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithTableViewConfiguredByWrongIndex {

		public View _view;

		@AFXCellValueConfig(colIdx = 0, propertyValue = "name")
		@AFXCellValueConfig(colIdx = 2, propertyValue = "age", stringConverter = DoubleConverter.class) // wrong
		protected TableView<Person> tableView = new TableView<>();

		public ControllerWithTableViewConfiguredByWrongIndex() {
			final TableColumn<Person, String> nameColumn = new TableColumn<>();
			final TableColumn<Person, Double> ageColumn = new TableColumn<>();
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithTableViewConfiguredById {

		public View _view;

		@AFXCellValueConfig(colId = "nameColumn", propertyValue = "name")
		@AFXCellValueConfig(colId = "ageColumn", propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TableView<Person> tableView = new TableView<>();

		public ControllerWithTableViewConfiguredById() {
			final TableColumn<Person, String> nameColumn = new TableColumn<>();
			nameColumn.setId("nameColumn");
			final TableColumn<Person, Double> ageColumn = new TableColumn<>();
			ageColumn.setId("ageColumn");
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithTableViewConfiguredByWrongId {

		public View _view;

		@AFXCellValueConfig(colId = "nonExistingFantasyColumn", propertyValue = "name") // wrong
		@AFXCellValueConfig(colId = "ageColumn", propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TableView<Person> tableView = new TableView<>();

		public ControllerWithTableViewConfiguredByWrongId() {
			final TableColumn<Person, String> nameColumn = new TableColumn<>();
			nameColumn.setId("nameColumn");
			final TableColumn<Person, Double> ageColumn = new TableColumn<>();
			ageColumn.setId("ageColumn");
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithTableColumns {

		public View _view;

		protected TableView<Person> tableView = new TableView<>();

		@AFXCellValueConfig(propertyValue = "name")
		protected TableColumn<Person, String> nameColumn = new TableColumn<>();

		@AFXCellValueConfig(propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TableColumn<Person, String> ageColumn = new TableColumn<>();

		public ControllerWithTableColumns() {
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public static class DoubleConverter extends StringConverter<Double> {

		@Override
		public String toString(final Double object) {
			return String.valueOf(object);
		}

		@Override
		public Double fromString(final String string) {
			return Double.parseDouble(string);
		}
	}

	public class ControllerWithTreeTableViewConfiguredByIndex {

		public View _view;

		@AFXCellValueConfig(colIdx = 0, propertyValue = "name")
		@AFXCellValueConfig(colIdx = 1, propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TreeTableView<Person> tableView = new TreeTableView<>();

		public ControllerWithTreeTableViewConfiguredByIndex() {
			final TreeTableColumn<Person, String> nameColumn = new TreeTableColumn<>();
			final TreeTableColumn<Person, Double> ageColumn = new TreeTableColumn<>();
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithTreeTableViewConfiguredByWrongIndex {

		public View _view;

		@AFXCellValueConfig(colIdx = 0, propertyValue = "name")
		@AFXCellValueConfig(colIdx = 2, propertyValue = "age", stringConverter = DoubleConverter.class) // wrong
		protected TreeTableView<Person> tableView = new TreeTableView<>();

		public ControllerWithTreeTableViewConfiguredByWrongIndex() {
			final TreeTableColumn<Person, String> nameColumn = new TreeTableColumn<>();
			final TreeTableColumn<Person, Double> ageColumn = new TreeTableColumn<>();
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithTreeTableViewConfiguredById {

		public View _view;

		@AFXCellValueConfig(colId = "nameColumn", propertyValue = "name")
		@AFXCellValueConfig(colId = "ageColumn", propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TreeTableView<Person> tableView = new TreeTableView<>();

		public ControllerWithTreeTableViewConfiguredById() {
			final TreeTableColumn<Person, String> nameColumn = new TreeTableColumn<>();
			nameColumn.setId("nameColumn");
			final TreeTableColumn<Person, Double> ageColumn = new TreeTableColumn<>();
			ageColumn.setId("ageColumn");
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithTreeTableViewConfiguredByWrongId {

		public View _view;

		@AFXCellValueConfig(colId = "nonExistingFantasyColumn", propertyValue = "name") // wrong
		@AFXCellValueConfig(colId = "ageColumn", propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TreeTableView<Person> tableView = new TreeTableView<>();

		public ControllerWithTreeTableViewConfiguredByWrongId() {
			final TreeTableColumn<Person, String> nameColumn = new TreeTableColumn<>();
			nameColumn.setId("nameColumn");
			final TreeTableColumn<Person, Double> ageColumn = new TreeTableColumn<>();
			ageColumn.setId("ageColumn");
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithTreeTableColumns {

		public View _view;

		protected TreeTableView<Person> tableView = new TreeTableView<>();

		@AFXCellValueConfig(propertyValue = "name")
		protected TreeTableColumn<Person, String> nameColumn = new TreeTableColumn<>();

		@AFXCellValueConfig(propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TreeTableColumn<Person, String> ageColumn = new TreeTableColumn<>();

		public ControllerWithTreeTableColumns() {
			tableView.getColumns().add(nameColumn);
			tableView.getColumns().add(ageColumn);
		}
	}

	public class ControllerWithNullField {

		public View _view;

		@AFXCellValueConfig(propertyValue = "name")
		protected TableView<Person> tableView = null;

	}

	public class ControllerWithInvalidAnnotation {

		public View _view;

		@AFXCellValueConfig(propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TableView<Person> tableView = new TableView<>();

	}

	public class ControllerWithTreeTableNullField {

		public View _view;

		@AFXCellValueConfig(propertyValue = "name")
		protected TreeTableView<Person> tableView = null;

	}

	public class ControllerWithTreeTableInvalidAnnotation {

		public View _view;

		@AFXCellValueConfig(propertyValue = "age", stringConverter = DoubleConverter.class)
		protected TreeTableView<Person> tableView = new TreeTableView<>();

	}

	public class ControllerWithTreeView {

		public View _view;

		@AFXCellValueConfig(stringConverter = PersonStringConverter.class)
		protected TreeView<Person> treeView = new TreeView<>();

	}

	public class ControllerWithListView {

		public View _view;

		@AFXCellValueConfig(stringConverter = PersonStringConverter.class)
		protected ListView<Person> listView = new ListView<>();

	}

	public static class PersonStringConverter extends StringConverter<Person> {

		@Override
		public String toString(final Person object) {
			return object.getName() + " (age " + String.valueOf(object.getAge()) + ")";
		}

		@Override
		public Person fromString(final String string) {
			final int bracketIdx = string.indexOf('(');
			final String name = string.substring(0, bracketIdx);
			return new Person(name, Double.valueOf(string.substring(bracketIdx + 5, string.length() - 1)));
		}
	}

	public static class Person {

		private String name;

		private Double age;

		public Person(final String name, final Double age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public Double getAge() {
			return age;
		}

		public void setAge(final Double age) {
			this.age = age;
		}
	}
}
