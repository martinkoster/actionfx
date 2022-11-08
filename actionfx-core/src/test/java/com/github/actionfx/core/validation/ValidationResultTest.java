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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * JUnit test for {@link ValidationResult}.
 *
 * @author MartinKoster
 */
class ValidationResultTest {

    @Test
    void testGetStatus_statusError() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder().addErrorMessage("Error message", null)
                .addWarningMessage("Warning message", null).addInfoMessage("Info message", null)
                .addOKMessage("OK message", null);

        // WHEN and THEN
        assertThat(vr.getStatus(), equalTo(ValidationStatus.ERROR));
    }

    @Test
    void testGetStatus_statusWarning() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder().addWarningMessage("Warning message", null)
                .addInfoMessage("Info message", null)
                .addOKMessage("OK message", null);

        // WHEN and THEN
        assertThat(vr.getStatus(), equalTo(ValidationStatus.WARNING));
    }

    @Test
    void testGetStatus_statusInfo() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder().addInfoMessage("Info message", null)
                .addOKMessage("OK message", null);

        // WHEN and THEN
        assertThat(vr.getStatus(), equalTo(ValidationStatus.INFO));
    }

    @Test
    void testGetStatus_statusOK() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder()
                .addOKMessage("OK message", null);

        // WHEN and THEN
        assertThat(vr.getStatus(), equalTo(ValidationStatus.OK));
    }

    @Test
    void testGetStatus_noMessages_statusOK() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder();

        // WHEN and THEN
        assertThat(vr.getStatus(), equalTo(ValidationStatus.OK));
    }

    @Test
    void testFrom() {
        // GIVEN
        final ValidationResult vr1 = ValidationResult.builder().addErrorMessage("Error message", null)
                .addWarningMessage("Warning message", null);
        final ValidationResult vr2 = ValidationResult.builder().addInfoMessage("Info message", null);
        final ValidationResult vr3 = ValidationResult.builder().addOKMessage("OK message", null);

        // WHEN
        final ValidationResult combined = ValidationResult.from(Arrays.asList(vr1, vr2, vr3));

        // THEN
        assertThat(combined.getMessages().stream().map(ValidationMessage::getStatus).collect(Collectors.toList()),
                contains(ValidationStatus.ERROR, ValidationStatus.WARNING, ValidationStatus.INFO, ValidationStatus.OK));
        assertThat(combined.getMessages().stream().map(ValidationMessage::getText).collect(Collectors.toList()),
                contains("Error message", "Warning message", "Info message", "OK message"));
    }

    @Test
    void testGetErrors() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder().addErrorMessage("Error message", null)
                .addWarningMessage("Warning message", null).addInfoMessage("Info message", null)
                .addOKMessage("OK message", null);

        // WHEN and THEN
        assertThat(vr.getErrors().stream().map(ValidationMessage::getText).collect(Collectors.toList()),
                contains("Error message"));
    }

    @Test
    void testGetWarnings() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder().addErrorMessage("Error message", null)
                .addWarningMessage("Warning message", null).addInfoMessage("Info message", null)
                .addOKMessage("OK message", null);

        // WHEN and THEN
        assertThat(vr.getWarnings().stream().map(ValidationMessage::getText).collect(Collectors.toList()),
                contains("Warning message"));
    }

    @Test
    void testGetInfo() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder().addErrorMessage("Error message", null)
                .addWarningMessage("Warning message", null).addInfoMessage("Info message", null)
                .addOKMessage("OK message", null);

        // WHEN and THEN
        assertThat(vr.getInfos().stream().map(ValidationMessage::getText).collect(Collectors.toList()),
                contains("Info message"));
    }

    @Test
    void testGetMessages_emptyList() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder();

        // WHEN and THEN
        assertThat(vr.getMessages(), hasSize(0));
    }

    @Test
    void testOverrideApplyValidationDecoration_applyValidationDecoration_isTrue() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder().addErrorMessage("Error message", null, false)
                .addWarningMessage("Warning message", null, true).addInfoMessage("Info message", null, true)
                .addOKMessage("OK message", null, false);

        // WHEN
        vr.overrideApplyValidationDecoration(true);

        // THEN
        assertThat(vr.getMessages(), hasSize(4));
        assertThat(vr.getMessages().get(0).isApplyValidationDecoration(), equalTo(false));
        assertThat(vr.getMessages().get(1).isApplyValidationDecoration(), equalTo(true));
        assertThat(vr.getMessages().get(2).isApplyValidationDecoration(), equalTo(true));
        assertThat(vr.getMessages().get(3).isApplyValidationDecoration(), equalTo(false));
    }

    @Test
    void testOverrideApplyValidationDecoration_applyValidationDecoration_isFalse() {
        // GIVEN
        final ValidationResult vr = ValidationResult.builder().addErrorMessage("Error message", null, false)
                .addWarningMessage("Warning message", null, true).addInfoMessage("Info message", null, true)
                .addOKMessage("OK message", null, false);

        // WHEN
        vr.overrideApplyValidationDecoration(false);

        // THEN
        assertThat(vr.getMessages(), hasSize(4));
        assertThat(vr.getMessages().get(0).isApplyValidationDecoration(), equalTo(false));
        assertThat(vr.getMessages().get(1).isApplyValidationDecoration(), equalTo(false));
        assertThat(vr.getMessages().get(2).isApplyValidationDecoration(), equalTo(false));
        assertThat(vr.getMessages().get(3).isApplyValidationDecoration(), equalTo(false));
    }

}
