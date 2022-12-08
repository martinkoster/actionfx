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

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

/**
 * JUnit test for {@link BooleanValidator}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class BooleanValidatorTest {

    @BeforeEach
    void setup() {
        ActionFX.builder().build().scanForActionFXComponents();
    }

    @AfterEach
    void tearDown() {
        ActionFX.getInstance().reset();
    }

    @Test
    void testValidate_fromCheckBox_expectedValuePresent() {
        // GIVEN
        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        final BooleanValidator validator = new BooleanValidator("Please agree to the terms and conditions", true,
                false);

        // WHEN
        final ValidationResult vr = validator.validate(checkBox, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_fromCheckBox_unexpectedValuePresent() {
        // GIVEN
        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(false);
        final BooleanValidator validator = new BooleanValidator("Please agree to the terms and conditions", true,
                false);

        // WHEN
        final ValidationResult vr = validator.validate(checkBox, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Please agree to the terms and conditions");
    }

    @Test
    void testValidate_fromTextField_expectedValuePresent() {
        // GIVEN
        final TextField textField = new TextField("yes");
        final BooleanValidator validator = new BooleanValidator("Please agree to the terms and conditions", true,
                false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_fromTextField_unexpectedValuePresent() {
        // GIVEN
        final TextField textField = new TextField("no");
        final BooleanValidator validator = new BooleanValidator("Please agree to the terms and conditions", true,
                false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Please agree to the terms and conditions");
    }

    @Test
    void testValidate_fromTextField_invalidValuePresent() {
        // GIVEN
        final TextField textField = new TextField("invalid");
        final BooleanValidator validator = new BooleanValidator("Please agree to the terms and conditions", true,
                false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Please agree to the terms and conditions");
    }
}
