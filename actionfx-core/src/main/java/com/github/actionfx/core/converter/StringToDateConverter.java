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
import java.util.Date;
import java.util.Locale;

/**
 * Converts a {@link String} to a {@link java.util.Date}.
 *
 * @author koster
 *
 */
public class StringToDateConverter extends AbstractDateConverter<String, Date> {

	/**
	 * Accepts a format pattern like "dd.MM.yyyy hh:mm" and a locale.
	 *
	 * @param formatPattern the format pattern
	 * @param locale        the locale
	 */
	public StringToDateConverter(final String formatPattern, final Locale locale) {
		super(formatPattern, locale);
	}

	@Override
	public Date convert(final String source) {
		try {
			return getDateFormat().parse(source);
		} catch (final ParseException e) {
			return null;
		}
	}

}
