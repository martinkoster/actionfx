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
package com.github.actionfx.datacontainerapp.controller;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.github.actionfx.common.converter.DoubleCurrencyStringConverter;
import com.github.actionfx.core.annotation.AFXCellValueConfig;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXConverter;
import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.datacontainerapp.model.Employee;
import com.github.actionfx.datacontainerapp.view.DatacontainerView;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.util.StringConverter;

/**
 * Controller for the datacontainer demo app. It uses a statically coded view
 * (instead of FXML, which should be preferred).
 * <p>
 * The following controls are used for displaying employee data:
 * <ul>
 * <li>{@link TableView}
 * <li>{@link ListView}
 * <li>{@link TreeTableView}
 * <li>{@link TreeView}
 * <li>{@link ComboBox}
 * <li>{@link ChoiceBox}
 * </ul>
 *
 * @author koster
 *
 */
@AFXController(viewId = "datacontainerDemoView", viewClass = DatacontainerView.class, maximized = true)
public class DatacontainerController {

	@AFXCellValueConfig(colId = "firstNameColumn", propertyValue = "firstName", editable = true)
	@AFXCellValueConfig(colId = "lastNameColumn", propertyValue = "lastName", editable = true)
	@AFXCellValueConfig(colId = "salaryColumn", propertyValue = "salary", stringConverter = DoubleCurrencyStringConverter.class, editable = true)
	@FXML
	private TableView<Employee> employeeTableView;

	@AFXCellValueConfig(stringConverter = EmployeeStringConverter.class, editable = true)
	@FXML
	private ListView<Employee> employeeListView;

	@AFXCellValueConfig(colId = "firstNameTreeColumn", propertyValue = "firstName", editable = true)
	@AFXCellValueConfig(colId = "lastNameTreeColumn", propertyValue = "lastName", editable = true)
	@AFXCellValueConfig(colId = "salaryTreeColumn", propertyValue = "salary", stringConverter = DoubleCurrencyStringConverter.class, editable = true)
	@FXML
	private TreeTableView<Employee> employeeTreeTableView;

	@AFXCellValueConfig(stringConverter = EmployeeStringConverter.class, editable = true)
	@FXML
	private TreeView<Employee> employeeTreeView;

	@AFXConverter(EmployeeStringConverter.class)
	@FXML
	private ComboBox<Employee> employeeComboBox;

	@AFXConverter(EmployeeStringConverter.class)
	@FXML
	private ChoiceBox<Employee> employeeChoiceBox;

	@AFXLoadControlData(controlId = "employeeTableView")
	public List<Employee> loadEmployeeTableData() {
		return Arrays.asList(new Employee("Big", "Boss", 100000.0), new Employee("John", "Doe", 50000.0),
				new Employee("Jane", "Doe", 60000.0), new Employee("New", "Guy", 30000.0));
	}

	@AFXLoadControlData(controlId = "employeeListView")
	public List<Employee> loadEmployeeListData() {
		return loadEmployeeTableData();
	}

	@AFXLoadControlData(controlId = "employeeTreeTableView")
	public TreeItem<Employee> loadEmployeeTreeTableData() {
		final TreeItem<Employee> boss = new TreeItem<>(new Employee("Big", "Boss", 100000.0));
		final TreeItem<Employee> john = new TreeItem<>(new Employee("John", "Doe", 50000.0));
		final TreeItem<Employee> jane = new TreeItem<>(new Employee("Jane", "Doe", 60000.0));
		final TreeItem<Employee> newGuy = new TreeItem<>(new Employee("New", "Guy", 30000.0));
		boss.getChildren().add(john);
		boss.getChildren().add(jane);
		jane.getChildren().add(newGuy);
		return boss;
	}

	@AFXLoadControlData(controlId = "employeeTreeView")
	public TreeItem<Employee> loadEmployeeTreeViewData() {
		return loadEmployeeTreeTableData();
	}

	@AFXLoadControlData(controlId = "employeeComboBox")
	public List<Employee> loadEmployeeComboData() {
		return loadEmployeeTableData();
	}

	@AFXLoadControlData(controlId = "employeeChoiceBox")
	public List<Employee> loadEmployeeChoiceData() {
		return loadEmployeeTableData();
	}

	public static class EmployeeStringConverter extends StringConverter<Employee> {

		@Override
		public String toString(final Employee object) {
			if (object == null) {
				return "";
			}
			return String.format("%s %s %.2f $", object.getFirstName(), object.getLastName(), object.getSalary());
		}

		/**
		 * This method is invoked when the cell content is edited.
		 */
		@Override
		public Employee fromString(final String string) {
			final String[] tokens = string.split(" ");
			final String firstName = getToken(tokens, 0, "");
			final String lastName = getToken(tokens, 1, "");
			final NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
			Double salary = Double.valueOf(0.0);
			try {
				salary = format.parse(getToken(tokens, 2, "0.0")).doubleValue();
			} catch (final ParseException e) {
			}
			return new Employee(firstName, lastName, salary);
		}

		private String getToken(final String[] tokens, final int token, final String defaultValue) {
			return tokens.length > token ? tokens[token] : defaultValue;
		}
	}
}
