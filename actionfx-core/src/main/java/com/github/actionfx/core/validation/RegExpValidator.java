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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.core.view.graph.ControlWrapper;

/**
 * {@link Validator} implementation that
 *
 * @author MartinKoster
 */
public class RegExpValidator extends AbstractRequiredValidator {

	private final Pattern pattern;

    public RegExpValidator(final String message, final String regExp, final boolean required) {
        super(message, required);
        pattern = Pattern.compile(regExp);
    }

    @Override
    protected ValidationResult validateAfterRequiredCheck(final ControlWrapper controlWrapper,
            final ControlProperties controlProperty) {
        final Object value = controlWrapper.getValue(controlProperty);
        return ValidationResult.builder().addErrorMessageIf(getMessage(), controlWrapper.getWrapped(),
                value instanceof String
                        && !(StringUtils.isBlank((String) value) || pattern.matcher((String) value).matches()));
    }

}
