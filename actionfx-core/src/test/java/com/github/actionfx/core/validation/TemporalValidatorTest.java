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
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

/**
 * JUnit test for {@link TemporalValidator}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class TemporalValidatorTest {

    @BeforeEach
    void setup() {
        ActionFX.builder().build().scanForActionFXComponents();
    }

    @AfterEach
    void tearDown() {
        ActionFX.getInstance().reset();
    }

    @Test
    void testValidate_textField_valueIsNull_notRequired_validationOK() {
        // GIVEN
        final TextField textField = new TextField(null);
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_valueIsEmpty_notRequired_validationOK() {
        // GIVEN
        final TextField textField = new TextField("");
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_valueIsNull_required_validationERROR() {
        // GIVEN
        final TextField textField = new TextField(null);
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(true);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past");
    }

    @Test
    void testValidate_textField_withFormatPattern_withoutTimeComponent_valueIsNotCompliantToPattern_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("1.1.22");
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past");
    }

    @Test
    void testValidate_textField_withFormatPattern_withTimeComponent_valueIsNotCompliantToPattern_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 1:30");
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past");
    }

    @Test
    void testValidate_textField_past_withFormatPattern_withTimeComponent_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 12:50");
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_past_withFormatPattern_withTimeComponent_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:10");
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past");
    }

    @Test
    void testValidate_textField_past_withFormatPattern_withoutTimeComponent_validationOK() {
        // GIVEN
        final TextField textField = new TextField("30.12.2022");
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_past_withFormatPattern_withoutTimeComponent_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022");
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past");
    }

    @Test
    void testValidate_textField_pastOrPresent_withFormatPattern_withTimeComponent_isPast_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 12:50");
        final TemporalValidator validator = temporalValidatorPastOrPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_pastOrPresent_withFormatPattern_withTimeComponent_isPresent_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:00");
        final TemporalValidator validator = temporalValidatorPastOrPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_pastOrPresent_withFormatPattern_withTimeComponent_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:10");
        final TemporalValidator validator = temporalValidatorPastOrPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past or present");
    }

    @Test
    void testValidate_textField_pastOrPresent_withFormatPattern_withoutTimeComponent_isPast_validationOK() {
        // GIVEN
        final TextField textField = new TextField("30.12.2022");
        final TemporalValidator validator = temporalValidatorPastOrPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_pastOrPresent_withFormatPattern_withoutTimeComponent_isPresent_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022");
        final TemporalValidator validator = temporalValidatorPastOrPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_pastOrPresent_withFormatPattern_withoutTimeComponent_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("01.01.2023");
        final TemporalValidator validator = temporalValidatorPastOrPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past or present");
    }

    @Test
    void testValidate_textField_future_withFormatPattern_withTimeComponent_isFuture_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:10");
        final TemporalValidator validator = temporalValidatorFutureAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_future_withFormatPattern_withTimeComponent_isPresent_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:00");
        final TemporalValidator validator = temporalValidatorFutureAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the future");
    }

    @Test
    void testValidate_textField_future_withFormatPattern_withTimeComponent_isPastvalidationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 12:50");
        final TemporalValidator validator = temporalValidatorFutureAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the future");
    }

    @Test
    void testValidate_textField_future_withFormatPattern_withoutTimeComponent_isPast_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("30.12.2022");
        final TemporalValidator validator = temporalValidatorFutureAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the future");
    }

    @Test
    void testValidate_textField_future_withFormatPattern_withoutTimeComponent_isPresent_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022");
        final TemporalValidator validator = temporalValidatorFutureAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the future");
    }

    @Test
    void testValidate_textField_future_withFormatPattern_withoutTimeComponent_isFuture_validationOK() {
        // GIVEN
        final TextField textField = new TextField("01.01.2023");
        final TemporalValidator validator = temporalValidatorFutureAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_futureOrPresent_withFormatPattern_withTimeComponent_isFuture_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:10");
        final TemporalValidator validator = temporalValidatorFutureOrPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_futureOrPresent_withFormatPattern_withTimeComponent_isPresent_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:00");
        final TemporalValidator validator = temporalValidatorFutureOrPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_futureOrPresent_withFormatPattern_withTimeComponent_isPastvalidationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 12:50");
        final TemporalValidator validator = temporalValidatorFutureOrPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the future or present");
    }

    @Test
    void testValidate_textField_futureOrPresent_withFormatPattern_withoutTimeComponent_isPast_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("30.12.2022");
        final TemporalValidator validator = temporalValidatorFutureOrPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the future or present");
    }

    @Test
    void testValidate_textField_futureOrPresent_withFormatPattern_withoutTimeComponent_isPresent_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022");
        final TemporalValidator validator = temporalValidatorFutureOrPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_futureOrPresent_withFormatPattern_withoutTimeComponent_isFuture_validationOK() {
        // GIVEN
        final TextField textField = new TextField("01.01.2023");
        final TemporalValidator validator = temporalValidatorFutureOrPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_present_withFormatPattern_withTimeComponent_isPresent_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:00");
        final TemporalValidator validator = temporalValidatorPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_present_withFormatPattern_withTimeComponent_isPast_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 12:50");
        final TemporalValidator validator = temporalValidatorPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the present");
    }

    @Test
    void testValidate_textField_present_withFormatPattern_withTimeComponent_isFuture_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022 13:10");
        final TemporalValidator validator = temporalValidatorPresentAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the present");
    }

    @Test
    void testValidate_textField_present_withFormatPattern_withoutTimeComponent_isPresent_validationOK() {
        // GIVEN
        final TextField textField = new TextField("31.12.2022");
        final TemporalValidator validator = temporalValidatorPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_textField_present_withFormatPattern_withoutTimeComponent_isPast_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("30.12.2022");
        final TemporalValidator validator = temporalValidatorPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the present");
    }

    @Test
    void testValidate_textField_present_withFormatPattern_withoutTimeComponent_isFuture_validationERROR() {
        // GIVEN
        final TextField textField = new TextField("01.01.2023");
        final TemporalValidator validator = temporalValidatorPresentAndFormatPatternWithoutTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(textField, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the present");
    }

    @Test
    void testValidate_datePicker_past_isPast_validationOK() {
        // GIVEN
        final DatePicker datePicker = new DatePicker(
                LocalDate.parse("30.12.2022", DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(datePicker, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.OK);
    }

    @Test
    void testValidate_datePicker_past_isPresent_validationERROR() {
        // GIVEN
        final DatePicker datePicker = new DatePicker(
                LocalDate.parse("31.12.2022", DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(datePicker, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past");
    }

    @Test
    void testValidate_datePicker_past_isFuture_validationERROR() {
        // GIVEN
        final DatePicker datePicker = new DatePicker(
                LocalDate.parse("01.01.2023", DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        final TemporalValidator validator = temporalValidatorPastAndFormatPatternWithTimeComponent(false);

        // WHEN
        final ValidationResult vr = validator.validate(datePicker, ControlProperties.SINGLE_VALUE_PROPERTY);

        // THEN
        assertThatStatusIs(vr, ValidationStatus.ERROR);
        assertThatMessageWithTextIsPresent(vr, "Value must be in the past");
    }

    private TemporalValidator temporalValidatorPastAndFormatPatternWithTimeComponent(final boolean required) {
        return temporalValidator("Value must be in the past", true, false, false, false, "dd.MM.yyyy HH:mm", required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorPastOrPresentAndFormatPatternWithTimeComponent(final boolean required) {
        return temporalValidator("Value must be in the past or present", false, true, false, false, "dd.MM.yyyy HH:mm",
                required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorFutureAndFormatPatternWithTimeComponent(final boolean required) {
        return temporalValidator("Value must be in the future", false, false, true, false, "dd.MM.yyyy HH:mm", required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorFutureOrPresentAndFormatPatternWithTimeComponent(
            final boolean required) {
        return temporalValidator("Value must be in the future or present", false, false, false, true,
                "dd.MM.yyyy HH:mm", required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorPresentAndFormatPatternWithTimeComponent(final boolean required) {
        return temporalValidator("Value must be in the present", false, true, false, true, "dd.MM.yyyy HH:mm", required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorPastAndFormatPatternWithoutTimeComponent(final boolean required) {
        return temporalValidator("Value must be in the past", true, false, false, false, "dd.MM.yyyy", required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorPastOrPresentAndFormatPatternWithoutTimeComponent(
            final boolean required) {
        return temporalValidator("Value must be in the past or present", false, true, false, false, "dd.MM.yyyy",
                required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorFutureAndFormatPatternWithoutTimeComponent(final boolean required) {
        return temporalValidator("Value must be in the future", false, false, true, false, "dd.MM.yyyy", required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorFutureOrPresentAndFormatPatternWithoutTimeComponent(
            final boolean required) {
        return temporalValidator("Value must be in the future or present", false, false, false, true, "dd.MM.yyyy",
                required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidatorPresentAndFormatPatternWithoutTimeComponent(final boolean required) {
        return temporalValidator("Value must be in the present", false, true, false, true, "dd.MM.yyyy", required,
                "31.12.2022 13:00", "31.12.2022");
    }

    private TemporalValidator temporalValidator(final String message, final boolean past, final boolean pastOrPresent,
            final boolean future, final boolean futureOrPresent, final String formatPattern, final boolean required,
            final String now, final String today) {

        final TemporalValidator validator = Mockito.spy(
                new TemporalValidator(message, past, pastOrPresent, future, futureOrPresent, formatPattern, required));
        final LocalDate ld = LocalDate.parse(today, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        final LocalDateTime ldt = LocalDateTime.parse(now, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        when(validator.getNow()).thenReturn(ldt);
        when(validator.getToday()).thenReturn(ld);
        return validator;
    }

}
