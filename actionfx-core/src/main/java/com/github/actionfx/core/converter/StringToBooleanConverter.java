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
package com.github.actionfx.core.converter;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts a {@link java.lang.String} into a {@link java.lang.Boolean}.
 *
 * @author koster
 */
public class StringToBooleanConverter implements Converter<String, Boolean> {

    private static final Set<String> TRUE_VALUES = new HashSet<>(8);

    private static final Set<String> FALSE_VALUES = new HashSet<>(6);

    static {
        TRUE_VALUES.add("true");
        TRUE_VALUES.add("on");
        TRUE_VALUES.add("yes");
        TRUE_VALUES.add("si");
        TRUE_VALUES.add("sì");
        TRUE_VALUES.add("oui");
        TRUE_VALUES.add("ja");
        TRUE_VALUES.add("1");

        FALSE_VALUES.add("false");
        FALSE_VALUES.add("off");
        FALSE_VALUES.add("no");
        FALSE_VALUES.add("non");
        FALSE_VALUES.add("nein");
        FALSE_VALUES.add("0");
    }

    private static final Boolean DEFAULT_VALUE_ON_NULL_AND_UNKNOWN = Boolean.FALSE;

    private final boolean allowNullValue;

    /**
     * Constructor defining whether {@code null} is allowed as return value.
     *
     * @param allowNullValue
     *            {@code true}, whether {@code null} can be returned on an empty input, {@code false}, if {@code null}
     *            is not allowed and a default value must be returned.
     */
    public StringToBooleanConverter(final boolean allowNullValue) {
        this.allowNullValue = allowNullValue;
    }

    /**
     * Converts {@code source} as String parameter to a {@link java.lang.Boolean}.
     */
    @Override
    public Boolean convert(final String source) {
        if (source == null) {
            return allowNullValue ? null : DEFAULT_VALUE_ON_NULL_AND_UNKNOWN;
        } else {
            String value = source.trim();
            value = value.toLowerCase();
            if (TRUE_VALUES.contains(value)) {
                return Boolean.TRUE;
            } else if (FALSE_VALUES.contains(value)) {
                return Boolean.FALSE;
            }
            return DEFAULT_VALUE_ON_NULL_AND_UNKNOWN;
        }
    }
}
