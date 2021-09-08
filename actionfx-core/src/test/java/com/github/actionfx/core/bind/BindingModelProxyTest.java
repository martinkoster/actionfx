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
package com.github.actionfx.core.bind;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link BindingModelProxy}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class BindingModelProxyTest {

	private TextField textField1;
	private ListView<String> listView;
	private TextField textField2;

	@BeforeEach
	public void onSetup() {
		textField1 = new TextField();
		listView = new ListView<>();
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listView.getItems().add("Item 1");
		listView.getItems().add("Item 2");
		listView.getItems().add("Item 3");
		textField2 = new TextField();
	}

	@Test
	void testBind() {
		// GIVEN
		final Model model = new Model();
		final List<BindingTarget> bindingTargets = list(bindingTarget(textField1, "stringValue"),
				bindingTarget(listView, "observableList"), bindingTarget(textField2, "plainString"));
		final BindingModelProxy proxy = new BindingModelProxy(model, bindingTargets);

		// WHEN
		proxy.bind();

		// THEN
		assertThat(textField1.getText(), equalTo("Hello World"));
		assertThat(listView.getSelectionModel().getSelectedItems(), contains("Item 1", "Item 2"));
		assertThat(textField2.getText(), equalTo("Hello World"));
	}

	@Test
	void testBind_checkBidirectionality() {
		// GIVEN
		final Model model = new Model();
		final List<BindingTarget> bindingTargets = list(bindingTarget(textField1, "stringValue"),
				bindingTarget(listView, "observableList"), bindingTarget(textField2, "plainString"));
		final BindingModelProxy proxy = new BindingModelProxy(model, bindingTargets);
		proxy.bind();

		// WHEN
		textField1.setText("Changed Value!");
		listView.getSelectionModel().clearAndSelect(2);
		textField2.setText("And another new value!");

		// THEN
		assertThat(model.stringValue.get(), equalTo("Changed Value!"));
		assertThat(model.observableList, contains("Item 3"));
		assertThat(model.plainString, equalTo("And another new value!"));
	}

	@Test
	void testUnbind() {
		// GIVEN
		final Model model = new Model();
		final List<BindingTarget> bindingTargets = list(bindingTarget(textField1, "stringValue"),
				bindingTarget(listView, "observableList"), bindingTarget(textField2, "plainString"));
		final BindingModelProxy proxy = new BindingModelProxy(model, bindingTargets);
		proxy.bind();

		// WHEN
		proxy.unbind();

		// THEN
		textField1.setText("Changed Value!");
		listView.getSelectionModel().clearAndSelect(2);
		textField2.setText("And another new value!");
		assertThat(model.stringValue.get(), equalTo("Hello World")); // no value change, since unbound
		assertThat(model.observableList, contains("Item 1", "Item 2"));// no value change, since unbound
		assertThat(model.plainString, equalTo("Hello World"));// no value change, since unbound
	}

	private static List<BindingTarget> list(final BindingTarget... bindingTargets) {
		return Arrays.asList(bindingTargets);
	}

	private static BindingTarget bindingTarget(final Control control, final String path) {
		return new BindingTarget(control, ControlProperties.USER_VALUE_OBSERVABLE, Model.class, path, "");
	}

	public class Model {

		private final StringProperty stringValue = new SimpleStringProperty("Hello World");

		private final ObservableList<String> observableList = FXCollections.observableArrayList("Item 1", "Item 2");

		private String plainString = "Hello World";

		public final StringProperty stringValueProperty() {
			return stringValue;
		}

		public final String getStringValue() {
			return stringValueProperty().get();
		}

		public final void setStringValue(final String stringValue) {
			stringValueProperty().set(stringValue);
		}

		public ObservableList<String> getObservableList() {
			return observableList;
		}

		public String getPlainString() {
			return plainString;
		}

		public void setPlainString(final String plainString) {
			this.plainString = plainString;
		}

	}
}
