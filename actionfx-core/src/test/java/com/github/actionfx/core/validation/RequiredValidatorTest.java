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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * JUnit test for {@link RequiredValidator}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class RequiredValidatorTest {

    @Test
    void testValidate_textField_valuePresent() {
        // GIVEN
        final TextField textField = new TextField("Hello there");
        final RequiredValidator validator = new RequiredValidator("Value is mandatory!");

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_valueNotPresent() {
        // GIVEN
        final TextField textField = new TextField("   ");
        final RequiredValidator validator = new RequiredValidator("Value is mandatory!");

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value is mandatory!");
    }

    @Test
    void testValidate_listView_itemPresent() {
        // GIVEN
        final ListView<String> listView = new ListView<>();
        listView.getItems().add("Item 1");
        final RequiredValidator validator = new RequiredValidator("Items are mandatory!");

        // WHEN
        final ValidationResult vr = validator.validate(listView, ControlProperties.ITEMS_OBSERVABLE_LIST);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_listView_itemNotPresent() {
        // GIVEN
        final ListView<String> listView = new ListView<>();
        final RequiredValidator validator = new RequiredValidator("Items are mandatory!");

        // WHEN
        final ValidationResult vr = validator.validate(listView, ControlProperties.ITEMS_OBSERVABLE_LIST);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Items are mandatory!");
    }

    @Test
    void testValidate_listView_itemSelected() {
        // GIVEN
        final ListView<String> listView = new ListView<>();
        listView.getItems().add("Item 1");
        listView.getSelectionModel().select("Item 1");
        final RequiredValidator validator = new RequiredValidator("You need to select an item!");

        // WHEN
        final ValidationResult vr = validator.validate(listView, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_listView_itemNotSelected() {
        // GIVEN
        final ListView<String> listView = new ListView<>();
        listView.getItems().add("Item 1");
        final RequiredValidator validator = new RequiredValidator("You need to select an item!");

        // WHEN
        final ValidationResult vr = validator.validate(listView, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "You need to select an item!");
    }

}
