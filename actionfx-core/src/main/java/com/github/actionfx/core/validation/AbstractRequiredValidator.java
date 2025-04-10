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

import org.apache.commons.lang3.StringUtils;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.scene.control.Control;

/**
 * Abstract base class for {@link Validator} implementations that need to ensure that a required value is provided,
 * before the actual validation can take place.
 *
 * @author koster
 */
public abstract class AbstractRequiredValidator implements Validator {

	private final String message;

	private final boolean required;

    protected AbstractRequiredValidator(final String message, final boolean required) {
        this.message = message;
        this.required = required;
    }

    /**
     * Validates a required value.
     *
     * @param control
     *            the control holding the value
     * @param controlProperty
     *            the control property inside the given {@code control} to be validated
     * @return the validation result
     */
    @Override
    public ValidationResult validate(final Control control, final ControlProperties controlProperty) {
        final ControlWrapper controlWrapper = ControlWrapper.of(control);
        if (required && !controlWrapper.hasValue(controlProperty)) {
            return ValidationResult.builder().addErrorMessage(message, control);
        }
        return validateAfterRequiredCheck(controlWrapper, controlProperty);
    }

    /**
     * Derived classes can implement this method to perform specific validations after the "required" value check has
     * been successfully performed, in case that is required.
     *
     * @param controlWrapper
     *            the control wrapper
     * @param controlProperty
     *            the control property to validate
     * @return the validation result
     */
    protected abstract ValidationResult validateAfterRequiredCheck(ControlWrapper controlWrapper,
            ControlProperties controlProperty);

    public String getMessage() {
        return message;
    }

    public boolean isRequired() {
        return required;
    }

    /**
     * Convenient method to access ActionFX's conversion service for converting a given value.
     *
     * @param <T>
     *            the type to convert into
     * @param value
     *            the value to convert
     * @param targetType
     *            the target type
     * @param formatPattern
     *            optional and nullabe format pattern to use for conversion
     * @return the converted value
     */
    protected <T> T convert(final Object value, final Class<T> targetType, final String formatPattern) {
        final ConversionService conversionService = ActionFX.getInstance().getConversionService();
        if (!StringUtils.isBlank(formatPattern)) {
            return conversionService.convert(value, targetType, formatPattern);
        } else {
            return conversionService.convert(value, targetType);
        }
    }

    /**
     * Convenient method to access ActionFX's conversion service for checking, whether a given value can be converted.
     *
     * @param <T>
     *            the type to convert into
     * @param value
     *            the value to check
     * @param targetType
     *            the target type
     * @param formatPattern
     *            optional and nullabe format pattern to use for conversion
     * @return {@code true}, if and only if the value can be converted to the given target type
     */
    protected <T> boolean canConvert(final Object value, final Class<T> targetType, final String formatPattern) {
        final ConversionService conversionService = ActionFX.getInstance().getConversionService();
        if (value != null && !conversionService.canConvert(value.getClass(), targetType)) {
            return false;
        }
        try {
            // in order to know, whether a value is really convertible, we have to do it
            // (e.g. converting a string into a number/date/etc. requires the actual parsing of the conversion routine).
            final Object converted = convert(value, targetType, formatPattern);
            final boolean isStringWithValue = isStringWithValue(value);
            return isStringWithValue && converted != null || !isStringWithValue;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Convenient method to check, if a value is a non-blank string value.
     *
     * @param value
     *            the value
     * @return {@code true}, if the value is a non-blank string, {@code false} otherwise.
     */
    protected boolean isStringWithValue(final Object value) {
        return value instanceof String str && !StringUtils.isBlank(str);
    }
}
