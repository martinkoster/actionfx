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

import java.util.Collection;

import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.core.view.graph.ControlWrapper;

/**
 * {@link Validator} implementation that checks whether the control's value size is between a min- and a max-value
 * (inclusively).
 * <p>
 * In case the control's value is a list, than the size of the list is checked to be between min- and max. If the value
 * is a {@code java.lang.String}, the length of the string is checked.
 *
 * @author koster
 */
public class SizeValidator extends AbstractRequiredValidator {

	private final long min;

	private final long max;

    /**
     * Default constructor.
     *
     * @param message
     *            the message to integrate in the validation result in case of a validation failure
     * @param min
     *            the minimum value that the value must be (inclusively)
     * @param max
     *            the maximum value that the value must be (inclusively)
     * @param required
     *            flag that indicates whether a value is required or not
     */
    public SizeValidator(final String message, final long min, final long max, final boolean required) {
        super(message, required);
        this.min = min;
        this.max = max;
    }

    @Override
    protected ValidationResult validateAfterRequiredCheck(final ControlWrapper controlWrapper,
            final ControlProperties controlProperty) {
        if (!controlWrapper.hasValue(controlProperty)) {
            return ValidationResult.ok();
        }
        final long value = getValue(controlWrapper, controlProperty);
        // value is allowed to be null - if not, then you need to use the "required=true" attribute
        return ValidationResult.builder().addErrorMessageIf(getMessage(), controlWrapper.getWrapped(),
                value < min || value > max);
    }

    protected long getValue(final ControlWrapper controlWrapper, final ControlProperties controlProperty) {
        final Object value = controlWrapper.getValue(controlProperty);
        if (Collection.class.isAssignableFrom(value.getClass())) {
            final Collection<?> collection = (Collection<?>) value;
            return collection.size();
        }
        if (String.class.isAssignableFrom(value.getClass())) {
            final String string = (String) value;
            return string.length();
        }
        return convert(value, long.class, null);
    }
}
