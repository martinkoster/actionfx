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
package com.github.actionfx.core.view;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * JUnit test case for {@link ParentView}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ParentViewTest {

	@Test
	void testParentView() {
		// WHEN
		final ParentView view = new ParentView("viewId", ViewClass.class, new ViewController());

		// THEN
		assertThat(view.getController(), instanceOf(ViewController.class));
		assertThat(view.getId(), equalTo("viewId"));
		assertThat(view.getRootNode(), instanceOf(ViewClass.class));

		// check that view component injection into controller works
		final ViewController controller = (ViewController) view.getController();
		assertThat(controller.label, notNullValue());
		assertThat(controller.label.getId(), equalTo("label"));
		assertThat(controller.label.getText(), equalTo("Hello World"));
		assertThat(controller.tableView, notNullValue());
		assertThat(controller.tableView.getId(), equalTo("tableView"));
	}

	@Test
	void testParentView_withResourceBundle() {
		// WHEN
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.TestResources", Locale.US);
		final ParentView view = new ParentView("viewId", ViewClassWithResourceBundle.class, new ViewController(),
				resourceBundle);

		// THEN
		assertThat(view.getController(), instanceOf(ViewController.class));
		assertThat(view.getId(), equalTo("viewId"));
		assertThat(view.getRootNode(), instanceOf(ViewClassWithResourceBundle.class));

		// check that view component injection into controller works
		final ViewController controller = (ViewController) view.getController();
		assertThat(controller.label, notNullValue());
		assertThat(controller.label.getId(), equalTo("label"));
		assertThat(controller.label.getText(), equalTo("Hello World"));
		assertThat(controller.tableView, notNullValue());
		assertThat(controller.tableView.getId(), equalTo("tableView"));
	}

	@Test
	void testGetViewNodesAsStream() {
		// GIVEN
		final ParentView view = new ParentView("viewId", ViewClass.class, new ViewController());

		// WHEN
		final List<String> idList = view.getViewNodesAsStream()
				.map(wrapper -> wrapper.getWrapped().getClass().getSimpleName()).collect(Collectors.toList());

		// THEN
		assertThat(idList, contains("ViewClass", "HBox", "Label", "String", "TableView"));
	}

	/**
	 * View class.
	 *
	 * @author koster
	 *
	 */
	public static class ViewClass extends AnchorPane {

		public ViewClass() {
			final HBox hbox = new HBox();

			final Label label = new Label("Hello World");
			label.setId("label");

			final TableView<String> tableView = new TableView<>();
			tableView.setId("tableView");

			hbox.getChildren().addAll(label, tableView);

			getChildren().add(hbox);
		}
	}

	/**
	 * View class with constructor accepting a {@link ResourceBundle}.
	 *
	 * @author koster
	 *
	 */
	public static class ViewClassWithResourceBundle extends AnchorPane {

		public ViewClassWithResourceBundle(final ResourceBundle resourceBundle) {
			final HBox hbox = new HBox();

			// lets get the internationalized text from the bundle
			final Label label = new Label(resourceBundle.getString("label.text"));
			label.setId("label");

			final TableView<String> tableView = new TableView<>();
			tableView.setId("tableView");

			hbox.getChildren().addAll(label, tableView);

			getChildren().add(hbox);
		}
	}

	/**
	 * Controller class for {@link ViewClass} and
	 * {@link ViewClassWithResourceBundle}.
	 *
	 * @author koster
	 *
	 */
	public static class ViewController {

		@FXML
		protected Label label;

		@FXML
		protected TableView<String> tableView;
	}

}
