/*
 * Copyright (c) 2020 Martin Koster
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

import com.github.actionfx.core.annotation.ValidationHelper;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.TextField;

/**
 * JUnit test for {@link RegExpValidator}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class RegExpValidatorTest {

    @Test
    void testValidate_email_valid() {
        // GIVEN
        final TextField textField = new TextField("koster@gmx.de");
        final RegExpValidator validator = new RegExpValidator("Value is not an e-mail address!",
                ValidationHelper.EMAIL_ADDRESS_REG_EXP, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_email_invalid() {
        // GIVEN
        final TextField textField = new TextField("koster@gmx");
        final RegExpValidator validator = new RegExpValidator("Value is not an e-mail address!",
                ValidationHelper.EMAIL_ADDRESS_REG_EXP, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value is not an e-mail address!");
    }

    @Test
    void testValidate_email_isNull_notRequired_valid() {
        // GIVEN
        final TextField textField = new TextField(null);
        final RegExpValidator validator = new RegExpValidator("Value is not an e-mail address!",
                ValidationHelper.EMAIL_ADDRESS_REG_EXP, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_email_isNull_required_valid() {
        // GIVEN
        final TextField textField = new TextField(null);
        final RegExpValidator validator = new RegExpValidator("Value is not an e-mail address!",
                ValidationHelper.EMAIL_ADDRESS_REG_EXP, true);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value is not an e-mail address!");
    }

    @Test
    void testValidate_email_isEmpty_notRequired_valid() {
        // GIVEN
        final TextField textField = new TextField("   ");
        final RegExpValidator validator = new RegExpValidator("Value is not an e-mail address!",
                ValidationHelper.EMAIL_ADDRESS_REG_EXP, false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_email_isEmpty_required_invalid() {
        // GIVEN
        final TextField textField = new TextField("   ");
        final RegExpValidator validator = new RegExpValidator("Value is not an e-mail address!",
                ValidationHelper.EMAIL_ADDRESS_REG_EXP, true);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value is not an e-mail address!");
    }
}
