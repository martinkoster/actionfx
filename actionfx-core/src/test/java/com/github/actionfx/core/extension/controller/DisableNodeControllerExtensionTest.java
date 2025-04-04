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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXDisableNode;
import com.github.actionfx.core.annotation.BooleanOp;
import com.github.actionfx.core.annotation.ValidationMode;
import com.github.actionfx.core.test.ViewCreator;
import com.github.actionfx.core.validation.ValidationOptions;
import com.github.actionfx.core.validation.ValidationResult;
import com.github.actionfx.core.validation.ValidationStatus;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link DisableNodeControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class DisableNodeControllerExtensionTest {

    @BeforeAll
    static void onSetup() {
        ActionFX.builder().validationGlobalMode(ValidationMode.ONCHANGE).validationStartTimeoutMs(0).build()
                .scanForActionFXComponents();
    }

    @AfterAll
    static void tearDown() {
        ActionFX.getInstance().reset();
    }

    @Test
    void testAccept_allControlHaveValues_isFulfilled() {
        // GIVEN
        final ControllerWithControls controller = new ControllerWithControls();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        extension.accept(controller);

		// THEN
		assertThat(controller.button1.isDisabled()).isEqualTo(false);
        setValues(controller, "hello", "world", null, null, null, null);
		assertThat(controller.button1.isDisabled()).isEqualTo(true);
    }

    @Test
    void testAccept_allControlHaveValues_isNotFulfilled() {
        // GIVEN
        final ControllerWithControls controller = new ControllerWithControls();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        extension.accept(controller);

		// THEN
		assertThat(controller.button1.isDisabled()).isEqualTo(false);
        setValues(controller, "hello", null, null, null, null, null);
		assertThat(controller.button1.isDisabled()).isEqualTo(false);
    }

    @Test
    void testAccept_allControlHaveValues_isFulfilled_withTableView() {
        // GIVEN
        final ControllerWithControls controller = new ControllerWithControls();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        extension.accept(controller);

		// THEN
		assertThat(controller.button3.isDisabled()).isEqualTo(false);
        setValues(controller, "hello", null, null, null, Arrays.asList("item"), null);
		assertThat(controller.button3.isDisabled()).isEqualTo(true);
    }

    @Test
    void testAccept_atLeastOneControlHasValues_isFulfilled() {
        // GIVEN
        final ControllerWithControls controller = new ControllerWithControls();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        extension.accept(controller);

		// THEN
		assertThat(controller.button2.isDisabled()).isEqualTo(false);
        setValues(controller, "hello", null, null, null, null, null);
		assertThat(controller.button2.isDisabled()).isEqualTo(true);
    }

    @Test
    void testAccept_atLeastOneControlHasValues_isFulfilled_withTableView() {
        // GIVEN
        final ControllerWithControls controller = new ControllerWithControls();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        extension.accept(controller);

		// THEN
		assertThat(controller.button4.isDisabled()).isEqualTo(false);
        setValues(controller, null, null, null, null, Arrays.asList("item"), null);
		assertThat(controller.button4.isDisabled()).isEqualTo(true);
    }

    @Test
    void testAccept_usingLogicalOp_AND() {
        // GIVEN
        final ControllerWithControls controller = new ControllerWithControls();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        extension.accept(controller);

		// THEN
		assertThat(controller.button5.isDisabled()).isEqualTo(false);
        setValues(controller, "hello", "world", null, null, null, null);
		assertThat(controller.button5.isDisabled()).isEqualTo(false);
        setValues(controller, "hello", "world", null, "another value", null, null);
		assertThat(controller.button5.isDisabled()).isEqualTo(true);
    }

    @Test
    void testAccept_usingLogicalOp_OR() {
        // GIVEN
        final ControllerWithControls controller = new ControllerWithControls();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        extension.accept(controller);

		// THEN
		assertThat(controller.button6.isDisabled()).isEqualTo(false);
        setValues(controller, "hello", null, null, null, null, null);
		assertThat(controller.button6.isDisabled()).isEqualTo(false);
        setValues(controller, "hello", null, null, "another value", null, null);
		assertThat(controller.button6.isDisabled()).isEqualTo(true);
    }

    @Test
    void testAccept_annotedButtonIsNull() {
        // GIVEN
        final ControllerWithNullButton controller = new ControllerWithNullButton();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).isEqualTo("Field value of field 'button' is null!");
    }

    @Test
    void testAccept_annotedFieldIsNotANode() {
        // GIVEN
        final ControllerWithNonNode controller = new ControllerWithNonNode();
        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).isEqualTo("Field value of field 'string' is not of type javafx.scene.Node!");
    }

    @Test
    @TestInFxThread
    void testAccept_withValidations_validationsFullfilled() {
        // GIVEN
        final ControllerWithValidations controller = new ControllerWithValidations();
        final ObjectProperty<ValidationStatus> statusForTextField1 = new SimpleObjectProperty<>(ValidationStatus.ERROR);
        final ObjectProperty<ValidationStatus> statusForTextField2 = new SimpleObjectProperty<>(ValidationStatus.ERROR);
        addValidationResultToControl(controller._view, controller.textField1, statusForTextField1);
        addValidationResultToControl(controller._view, controller.textField2, statusForTextField2);

        final DisableNodeControllerExtension extension = new DisableNodeControllerExtension();

        // WHEN
        extension.accept(controller);
        controller.textField1.setText("trigger change 1");
        controller.textField2.setText("trigger change 1");

		// THEN
		assertThat(controller.enableWhenSingleControlValidButton.isDisabled()).isEqualTo(false); // control is invalid, so control is enabled
		assertThat(controller.enableWhenAllValidButton.isDisabled()).isEqualTo(false);

        // and WHEN
        statusForTextField1.set(ValidationStatus.OK);
        controller.textField1.setText("trigger change 2");
		assertThat(controller.enableWhenSingleControlValidButton.isDisabled()).isEqualTo(true); // textField1 is now valid, so control is disabled
		assertThat(controller.enableWhenAllValidButton.isDisabled()).isEqualTo(false); // still enabled

        statusForTextField2.set(ValidationStatus.OK);
        controller.textField2.setText("trigger change 2");

		// and THEN
		assertThat(controller.enableWhenSingleControlValidButton.isDisabled()).isEqualTo(true);
		assertThat(controller.enableWhenAllValidButton.isDisabled()).isEqualTo(true); // now all controls are valid - fields are all disabled
    }

    private void addValidationResultToControl(final View view, final Control control,
            final ObjectProperty<ValidationStatus> statusProperty) {
        view.registerValidator(control, ControlProperties.USER_VALUE_OBSERVABLE,
                (ctrl, ctrlProperties) -> ValidationResult.builder().addMessage(statusProperty.get(),
                        statusProperty.get().toString(), control),
                ValidationOptions.options().validationMode(ValidationMode.ONCHANGE).validationStartTimeoutMs(0));
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

        @AFXDisableNode(whenAllContolsHaveUserValues = { "textField1", "textField2" })
        public Button button1;

        @AFXDisableNode(whenAtLeastOneContolHasUserValue = { "textField1", "textField2" })
        public Button button2;

        @AFXDisableNode(whenAllControlsHaveValues = { "textField1", "tableView" })
        public Button button3;

        @AFXDisableNode(whenAtLeastOneControlHasValues = { "textField1", "tableView" })
        public Button button4;

        @AFXDisableNode(whenAllContolsHaveUserValues = { "textField1",
                "textField2" }, whenAtLeastOneContolHasUserValue = { "textField3",
                        "textField4" }, logicalOp = BooleanOp.AND)
        public Button button5;

        @AFXDisableNode(whenAllControlsHaveValues = { "textField1", "textField2" }, whenAtLeastOneControlHasValues = {
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

        @AFXDisableNode(whenAllContolsHaveUserValues = { "textField" })
        public Button button;

        public TextField textField;

        public ControllerWithNullButton() {
            textField = new TextField();
            _view = ViewCreator.create().appendNode(textField, "textField");
        }
    }

    public class ControllerWithNonNode {

        public View _view;

        @AFXDisableNode(whenAllContolsHaveUserValues = { "textField" })
        public String string;

        public TextField textField;

        public ControllerWithNonNode() {
            textField = new TextField();
            _view = ViewCreator.create().appendNode(textField, "textField");
            string = "Hello World";
        }
    }

    public class ControllerWithValidations {

        public View _view;

        // since it is unit testing, we can not use annotations for validation here
        public TextField textField1;

        // since it is unit testing, we can not use annotations for validation here
        public TextField textField2;

        @AFXDisableNode(whenAllControlsValid = true)
        public Button enableWhenAllValidButton;

        @AFXDisableNode(whenControlsAreValid = { "textField1" })
        public Button enableWhenSingleControlValidButton;

        public ControllerWithValidations() {
            textField1 = new TextField();
            textField2 = new TextField();
            enableWhenAllValidButton = new Button();
            enableWhenAllValidButton.setDisable(true);
            enableWhenSingleControlValidButton = new Button();
            enableWhenSingleControlValidButton.setDisable(true);
            _view = ViewCreator.create().appendNode(textField1, "textField1")
                    .appendNode(textField2, "textField2")
                    .appendNode(enableWhenAllValidButton, "enableWhenAllValidButton")
                    .appendNode(enableWhenSingleControlValidButton, "enableWhenSingleControlValidButton");
        }
    }

}
