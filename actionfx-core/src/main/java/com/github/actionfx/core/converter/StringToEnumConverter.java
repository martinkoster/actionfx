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

/**
 * Converts a {@link java.lang.String} into a {@link java.lang.Enum}.
 *
 * @author koster
 */
public class StringToEnumConverter<T extends Enum<T>> implements Converter<String, T> {

    private final Class<T> targetType;

    /**
     * Constructor accepting the target enum type to convert into.
     *
     * @param targetType
     *            the target enum type to convert into
     */
    public StringToEnumConverter(final Class<T> targetType) {
        this.targetType = targetType;
    }

    /**
     * Converts {@code source} as String parameter to a {@link java.lang.Boolean}.
     */
    @Override
    public T convert(final String source) {
        return Enum.valueOf(targetType, source);

    }
}
