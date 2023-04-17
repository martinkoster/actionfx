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
package com.github.actionfx.core.extension.controller;

import com.github.actionfx.core.annotation.AFXValidateMinMax;
import com.github.actionfx.core.validation.MinMaxValidator;
import com.github.actionfx.core.validation.ValidationOptions;
import com.github.actionfx.core.validation.Validator;
import com.github.actionfx.core.view.graph.ControlProperties;

import javafx.scene.control.Control;

/**
 * Controller field extension that applies validation on it using a min/max
 * validator on a control's value.
 *
 * @author koster
 */
public class ValidateMinMaxControllerExtension extends AbstractValidationControllerExtension<AFXValidateMinMax> {

	public ValidateMinMaxControllerExtension() {
		super(AFXValidateMinMax.class);
	}

	@Override
	protected Validator createValidator(final Object controller, final Control control,
			final AFXValidateMinMax annotation) {
		return new MinMaxValidator(getMessage(controller.getClass(), annotation.messageKey(), annotation.message()),
				annotation.min(), annotation.max(), annotation.formatPattern(), annotation.required());
	}

	@Override
	protected ValidationOptions createValidationOptions(final AFXValidateMinMax annotation) {
		return ValidationOptions.options().required(annotation.required()).validationMode(annotation.validationMode())
				.validationStartTimeoutMs(annotation.validationStartTimeoutMs());
	}

	@Override
	protected ControlProperties getValidatedControlProperty(final AFXValidateMinMax annotation) {
		return annotation.validationTargeProperty();
	}

}
