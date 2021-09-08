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

import java.text.ParseException;
import java.util.Locale;

/**
 * Converts a {@link String} to a {@link Float}.
 *
 * @author koster
 *
 */
public class StringToFloatConverter extends AbstractFloatingPointConverter<String, Float> {

	public static final Float DEFAULT_VALUE_ON_NULL_AND_PARSE_ERRORS = Float.valueOf(0.0f);

	private final boolean allowNullValue;

	/**
	 * Constructor accepting the pattern, locale and whether {@code null} is allowed
	 * as return value.
	 *
	 * @param formatPattern  the format pattern (e.g. "#,###.##")
	 * @param locale         the locale
	 * @param allowNullValue {@code true}, whether {@code null} can be returned on
	 *                       an empty input, {@code false}, if {@code null} is not
	 *                       allowed and a default value must be returned.
	 */
	public StringToFloatConverter(final String formatPattern, final Locale locale, final boolean allowNullValue) {
		super(formatPattern, locale);
		this.allowNullValue = allowNullValue;
	}

	@Override
	public Float convert(final String source) {
		if (source == null || "".equals(source)) {
			return onNullOrParseError();
		}
		try {
			return Float.valueOf(getNumberFormat().parse(source).floatValue());
		} catch (final ParseException e) {
			return onNullOrParseError();
		}
	}

	private Float onNullOrParseError() {
		return allowNullValue ? null : DEFAULT_VALUE_ON_NULL_AND_PARSE_ERRORS;
	}

}
