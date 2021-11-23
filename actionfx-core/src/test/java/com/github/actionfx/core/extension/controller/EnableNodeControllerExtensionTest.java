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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.annotation.AFXEnableNode;
import com.github.actionfx.core.annotation.BooleanOp;
import com.github.actionfx.core.extension.controller.EnableNodeControllerExtension;
import com.github.actionfx.core.test.ViewCreator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link EnableNodeControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class EnableNodeControllerExtensionTest {

	@Test
	void testAccept_allControlHaveValues_isFulfilled() {
		// GIVEN
		final ControllerWithControls controller = new ControllerWithControls();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.button1.isDisabled(), equalTo(true));
		setValues(controller, "hello", "world", null, null, null, null);
		assertThat(controller.button1.isDisabled(), equalTo(false));
	}

	@Test
	void testAccept_allControlHaveValues_isNotFulfilled() {
		// GIVEN
		final ControllerWithControls controller = new ControllerWithControls();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.button1.isDisabled(), equalTo(true));
		setValues(controller, "hello", null, null, null, null, null);
		assertThat(controller.button1.isDisabled(), equalTo(true));
	}

	@Test
	void testAccept_allControlHaveValues_isFulfilled_withTableView() {
		// GIVEN
		final ControllerWithControls controller = new ControllerWithControls();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.button3.isDisabled(), equalTo(true));
		setValues(controller, "hello", null, null, null, Arrays.asList("item"), null);
		assertThat(controller.button3.isDisabled(), equalTo(false));
	}

	@Test
	void testAccept_atLeastOneControlHasValues_isFulfilled() {
		// GIVEN
		final ControllerWithControls controller = new ControllerWithControls();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.button2.isDisabled(), equalTo(true));
		setValues(controller, "hello", null, null, null, null, null);
		assertThat(controller.button2.isDisabled(), equalTo(false));
	}

	@Test
	void testAccept_atLeastOneControlHasValues_isFulfilled_withTableView() {
		// GIVEN
		final ControllerWithControls controller = new ControllerWithControls();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.button4.isDisabled(), equalTo(true));
		setValues(controller, null, null, null, null, Arrays.asList("item"), null);
		assertThat(controller.button4.isDisabled(), equalTo(false));
	}

	@Test
	void testAccept_usingLogicalOp_AND() {
		// GIVEN
		final ControllerWithControls controller = new ControllerWithControls();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.button5.isDisabled(), equalTo(true));
		setValues(controller, "hello", "world", null, null, null, null);
		assertThat(controller.button5.isDisabled(), equalTo(true));
		setValues(controller, "hello", "world", null, "another value", null, null);
		assertThat(controller.button5.isDisabled(), equalTo(false));
	}

	@Test
	void testAccept_usingLogicalOp_OR() {
		// GIVEN
		final ControllerWithControls controller = new ControllerWithControls();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.button6.isDisabled(), equalTo(true));
		setValues(controller, "hello", null, null, null, null, null);
		assertThat(controller.button6.isDisabled(), equalTo(true));
		setValues(controller, "hello", null, null, "another value", null, null);
		assertThat(controller.button6.isDisabled(), equalTo(false));
	}

	@Test
	void testAccept_annotedButtonIsNull() {
		// GIVEN
		final ControllerWithNullButton controller = new ControllerWithNullButton();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), equalTo("Field value of field 'button' is null!"));
	}

	@Test
	void testAccept_annotedFieldIsNotANode() {
		// GIVEN
		final ControllerWithNonNode controller = new ControllerWithNonNode();
		final EnableNodeControllerExtension extension = new EnableNodeControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), equalTo("Field value of field 'string' is not of type javafx.scene.Node!"));
	}

	private void setValues(final ControllerWithControls controller, final String text1, final String text2,
			final String text3, final String text4, final List<String> tableItems, final String selectedTableItem) {
		controller.textField1.setText(text1);
		controller.textField2.setText(text2);
		controller.textField3.setText(text3);
		controller.textField4.setText(text4);
		if (tableItems != null) {
			controller.tableView.getItems().addAll(tableItems);
		}
		if (selectedTableItem != null) {
			controller.tableView.getSelectionModel().select(selectedTableItem);
		}
	}

	public class ControllerWithControls {

		public View _view;

		@AFXEnableNode(whenAllContolsHaveUserValues = { "textField1", "textField2" })
		public Button button1;

		@AFXEnableNode(whenAtLeastOneContolHasUserValue = { "textField1", "textField2" })
		public Button button2;

		@AFXEnableNode(whenAllControlsHaveValues = { "textField1", "tableView" })
		public Button button3;

		@AFXEnableNode(whenAtLeastOneControlHasValues = { "textField1", "tableView" })
		public Button button4;

		@AFXEnableNode(whenAllContolsHaveUserValues = { "textField1",
				"textField2" }, whenAtLeastOneContolHasUserValue = { "textField3",
						"textField4" }, logicalOp = BooleanOp.AND)
		public Button button5;

		@AFXEnableNode(whenAllControlsHaveValues = { "textField1", "textField2" }, whenAtLeastOneControlHasValues = {
				"textField3", "textField4" }, logicalOp = BooleanOp.OR)
		public Button button6;

		public TextField textField1;

		public TextField textField2;

		public TextField textField3;

		public TextField textField4;

		public TableView<String> tableView;

		public ControllerWithControls() {
			textField1 = new TextField();
			textField2 = new TextField();
			textField3 = new TextField();
			textField4 = new TextField();
			tableView = new TableView<>();
			_view = ViewCreator.create().appendNode(textField1, "textField1").appendNode(textField2, "textField2")
					.appendNode(textField3, "textField3").appendNode(textField4, "textField4")
					.appendNode(tableView, "tableView");
			button1 = new Button();
			button2 = new Button();
			button3 = new Button();
			button4 = new Button();
			button5 = new Button();
			button6 = new Button();
		}

	}

	public class ControllerWithNullButton {

		public View _view;

		@AFXEnableNode(whenAllContolsHaveUserValues = { "textField" })
		public Button button;

		public TextField textField;

		public ControllerWithNullButton() {
			textField = new TextField();
			_view = ViewCreator.create().appendNode(textField, "textField");
		}
	}

	public class ControllerWithNonNode {

		public View _view;

		@AFXEnableNode(whenAllContolsHaveUserValues = { "textField" })
		public String string;

		public TextField textField;

		public ControllerWithNonNode() {
			textField = new TextField();
			_view = ViewCreator.create().appendNode(textField, "textField");
			string = "Hello World";
		}
	}

}
