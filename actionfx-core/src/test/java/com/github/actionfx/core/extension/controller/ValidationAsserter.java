/*
 * Copyright (c) 2021,2022 Martin Koster
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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.ArgumentCaptor;

import com.github.actionfx.core.annotation.ValidationMode;
import com.github.actionfx.core.validation.ValidationOptions;
import com.github.actionfx.core.validation.Validator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlProperties;

import javafx.scene.control.Control;

/**
 * Util class for asserting validations.
 *
 * @author koster
 *
 */
public class ValidationAsserter {

	private ValidationAsserter() {
		// class can not be instantiated
	}

	public static void assertRegisterValidator(final View view, final Control control,
			final Class<? extends Validator> actualValidatorClass, final ValidationMode validationMode,
			final boolean required, final int validationStartTimeoutMs, final boolean applyValidationDecorations) {
		final ArgumentCaptor<Validator> validatorCaptor = ArgumentCaptor.forClass(Validator.class);
		final ArgumentCaptor<ValidationOptions> optionsCaptor = ArgumentCaptor.forClass(ValidationOptions.class);

		verify(view, times(1)).registerValidator(eq(control), eq(ControlProperties.USER_VALUE_OBSERVABLE),
				validatorCaptor.capture(), optionsCaptor.capture());
		assertThat(validatorCaptor.getValue(), instanceOf(actualValidatorClass));
		final ValidationOptions options = optionsCaptor.getValue();
		assertThat(options.getValidationMode(), equalTo(validationMode));
		assertThat(options.getValidationStartTimeoutMs(), equalTo(validationStartTimeoutMs));
		assertThat(options.isApplyValidationResultDecorations(), equalTo(applyValidationDecorations));
		assertThat(options.isRequired(), equalTo(required));
	}
}
