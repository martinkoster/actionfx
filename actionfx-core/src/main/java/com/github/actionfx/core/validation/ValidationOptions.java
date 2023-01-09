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

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.ValidationMode;

/**
 * Configuration object for a single control and value validation.
 *
 * @author koster
 */
public class ValidationOptions {

	private ValidationMode validationMode;

	private boolean required;

	private int validationStartTimeoutMs;

	private boolean applyValidationResultDecorations;

	protected ValidationOptions() {
		validationMode = determineValidationMode();
		required = false;
		validationStartTimeoutMs = determineValidationStartTimeoutMs();
		applyValidationResultDecorations = determineValidationApplyResultDecoration();
	}

	/**
	 * Creates a new {@link ValidationOptions} instance for building.
	 *
	 * @return the {@link ValidationOptions} instance
	 */
	public static ValidationOptions options() {
		return new ValidationOptions();
	}

	/**
	 * Defines, when the actual validation is performed.
	 * <p>
	 * Possible options are:
	 * <p>
	 * <ul>
	 * <li>{@link ValidationMode#ONCHANGE}: Validation is applied, when a change
	 * occurs in a control that holds any validation annotation.</li>
	 * <li>{@link ValidationMode#MANUAL}: Validation occurs only, when an explicit
	 * call to {@link com.github.actionfx.core.ActionFX#validate(Object)} is
	 * performed.
	 * </ul>
	 *
	 *
	 * @param validationMode the validation mode. Default is
	 *                       {@link ValidationMode#ONCHANGE}.
	 * @return this builder
	 */
	public ValidationOptions validationMode(final ValidationMode validationMode) {
		this.validationMode = validationMode;
		return this;
	}

	/**
	 * Indicates whether the control value is mandatory and needs to hold a value.
	 *
	 * @param required {@code true}, if the control value needs to have a value,
	 *                 {@code false} otherwise. Default is {@code false}, if not set
	 *                 by this builder method.
	 * @return this builder
	 */
	public ValidationOptions required(final boolean required) {
		this.required = required;
		return this;
	}

	/**
	 * Defines the timeout in milliseconds that has to pass after changing a control
	 * value, before the actual validation is applied to.
	 * <p>
	 * This value is only used, if validation mode {@link ValidationMode#ONCHANGE}
	 * is supplied in attribute {@link #validationMode(ValidationMode)}.
	 *
	 * @param validationStartTimeoutMs the timeout value in milliseconds. Default is
	 *                                 100.
	 * @return this builder
	 */
	public ValidationOptions validationStartTimeoutMs(final int validationStartTimeoutMs) {
		if (validationStartTimeoutMs != -1) {
			this.validationStartTimeoutMs = validationStartTimeoutMs;
		}
		return this;
	}

	/**
	 * Indicates whether validation errors shall be displayed as decorations, when
	 * there is a validation failure.
	 *
	 * @param applyValidationResultDecorations {@code true}, if validation
	 *                                         decorations shall be applied to
	 *                                         affected controls, {@code false}
	 *                                         otherwise. Default is {@code true},
	 *                                         if not set by this builder method.
	 * @return this builder
	 */
	public ValidationOptions applyValidationResultDecorations(final boolean applyValidationResultDecorations) {
		this.applyValidationResultDecorations = applyValidationResultDecorations;
		return this;
	}

	public ValidationMode getValidationMode() {
		return validationMode;
	}

	public boolean isRequired() {
		return required;
	}

	public int getValidationStartTimeoutMs() {
		return validationStartTimeoutMs;
	}

	public boolean isApplyValidationResultDecorations() {
		return applyValidationResultDecorations;
	}

	/**
	 * Checks, if a global validation mode is set (this will be used as default). If
	 * no global validation mode is set, this method returns
	 * {@link ValidationMode#MANUAL}.
	 *
	 * @return the determined validation mode
	 */
	protected ValidationMode determineValidationMode() {
		final ValidationMode globalMode = ActionFX.getInstance().getValidationGlobalMode();
		return globalMode != ValidationMode.GLOBAL_VALIDATION_MODE_UNSPECIFIED ? globalMode : ValidationMode.MANUAL;
	}

	/**
	 * Checks, if a global validation start timeout is defined.
	 *
	 * @return the globally defined validation timeout (or -1, if there is not
	 *         global timeout defined).
	 */
	protected int determineValidationStartTimeoutMs() {
		return ActionFX.getInstance().getValidationStartTimeoutMs();
	}

	/**
	 * Checks, if validation decorations for validation results shall be applied or
	 * not
	 *
	 * @return boolean flag for applying or not applying validation decorations
	 */
	protected boolean determineValidationApplyResultDecoration() {
		return ActionFX.getInstance().isValidationApplyResultDecoration();
	}
}
