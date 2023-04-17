/*
 * Copyright (c) 2022 Martin Koster
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
package com.github.actionfx.core.validation;

import static com.github.actionfx.core.validation.ValidationResultUtils.assertThatMessageWithTextIsPresent;
import static com.github.actionfx.core.validation.ValidationResultUtils.assertThatStatusIs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link SizeValidator}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class SizeValidatorTest {

    @BeforeEach
    void setup() {
        ActionFX.builder().build().scanForActionFXComponents();
    }

    @AfterEach
    void tearDown() {
        ActionFX.getInstance().reset();
    }

    @Test
    void testValidate_fromTextField_isWithinRange() {
        // GIVEN
        final TextField textField = new TextField("Hello");
        final SizeValidator validator = new SizeValidator("String must of length between 2 and 5", 2,
                5, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_fromTextField_isOutsideRange_less() {
        // GIVEN
        final TextField textField = new TextField("H");
        final SizeValidator validator = new SizeValidator("String must of length between 2 and 5", 2,
                5, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "String must of length between 2 and 5");
    }

    @Test
    void testValidate_fromTextField_isOutsideRange_more() {
        // GIVEN
        final TextField textField = new TextField("Hello World");
        final SizeValidator validator = new SizeValidator("String must of length between 2 and 5", 2,
                5, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "String must of length between 2 and 5");
    }

    @Test
    void testValidate_fromTextField_isAllowedToBeEmpty() {
        // GIVEN
        final TextField textField = new TextField("");
        final SizeValidator validator = new SizeValidator("String must of length between 2 and 5", 2,
                5, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_fromTextField_isAllowedToBeNull() {
        // GIVEN
        final TextField textField = new TextField(null);
        final SizeValidator validator = new SizeValidator("String must of length between 2 and 5", 2,
                5, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_fromTextField_isNotAllowedToBeEmpty() {
        // GIVEN
        final TextField textField = new TextField("");
        final SizeValidator validator = new SizeValidator("String must of length between 2 and 5", 2,
                5, true);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "String must of length between 2 and 5");
    }

    @Test
    void testValidate_fromListView_isWithinRange() {
        // GIVEN
        final ListView<String> listView = new ListView<>();
        listView.getItems().addAll("Item 1", "Item 2", "Item 3");
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().select("Item 2");
        listView.getSelectionModel().select("Item 3");
        final SizeValidator validator = new SizeValidator("Please select 2 to 3 items", 2,
                3, false);

        // WHEN
        final ValidationResult vr = validator.validate(listView, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_fromListView_isOutsideRange() {
        // GIVEN
        final ListView<String> listView = new ListView<>();
        listView.getItems().addAll("Item 1", "Item 2", "Item 3");
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().select("Item 1"); // only one item selected
        final SizeValidator validator = new SizeValidator("Please select 2 to 3 items", 2,
                3, false);

        // WHEN
        final ValidationResult vr = validator.validate(listView, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Please select 2 to 3 items");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testValidate_fromSpinner_isWithinRange() {
        // GIVEN
        final Spinner spinner = new Spinner(0, 10, 5);

        final SizeValidator validator = new SizeValidator("Please select a value between 3 and 7", 3,
                7, false);

        // WHEN
        final ValidationResult vr = validator.validate(spinner, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testValidate_fromSpinner_isOutsideRange() {
        // GIVEN
        final Spinner spinner = new Spinner(0, 10, 9);

        final SizeValidator validator = new SizeValidator("Please select a value between 3 and 7", 3,
                7, false);

        // WHEN
        final ValidationResult vr = validator.validate(spinner, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Please select a value between 3 and 7");
    }

}
