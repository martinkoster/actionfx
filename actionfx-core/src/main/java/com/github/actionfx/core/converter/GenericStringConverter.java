/*
 * Copyright (c) 2021 Martin Koster
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

import javafx.util.StringConverter;

/**
 * {@link StringConverter} that is able to use 2 {@link Converter} to perform
 * to-/from-string conversion.
 *
 * @param <T> the type to convert to/from string
 * @author koster
 *
 */
public class GenericStringConverter<T> extends StringConverter<T> implements BidirectionalConverter<T, String> {

	private final Converter<T, String> toStringConverter;

	private final Converter<String, T> fromStringConverter;

	/**
	 * Constructor accepting a to-string and a from-string converter.
	 *
	 * @param toStringConverter   the to-string converter
	 * @param fromStringConverter the from-string converter
	 */
	public GenericStringConverter(final Converter<T, String> toStringConverter,
			final Converter<String, T> fromStringConverter) {
		this.toStringConverter = toStringConverter != null ? toStringConverter : defaultToStringConverter();
		this.fromStringConverter = fromStringConverter != null ? fromStringConverter : defaultFromStringConverter();
	}

	/**
	 * Creates a default from-string converter which always maps a string to
	 * {@code null}.
	 *
	 * @return the default to-string converter
	 */
	private Converter<String, T> defaultFromStringConverter() {
		return value -> null;
	}

	/**
	 * Creates a default to-string converter, which basically calls the
	 * {@link String#toString()} method of the type.
	 *
	 * @return the default to-string converter
	 */
	private Converter<T, String> defaultToStringConverter() {
		return value -> value == null ? "" : value.toString();
	}

	@Override
	public String toString(final T value) {
		return to(value);
	}

	@Override
	public T fromString(final String value) {
		return from(value);
	}

	@Override
	public String to(final T source) {
		return toStringConverter.apply(source);
	}

	@Override
	public T from(final String target) {
		return fromStringConverter.apply(target);
	}

}
