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
package com.github.actionfx.core.utils;

import java.util.Collection;

/**
 * Utils for working with exceptions in ActionFX.
 *
 * @author koster
 *
 */
public class ExceptionUtils {

	public static final String NULL_VALUE_STRING = "null";

	/**
	 * Converts the {@code values} to a printable string e.g. as part of an
	 * exception.
	 *
	 * @param values the values that shall be converted into a printable string
	 * @return the printable string
	 */
	public static String toPrintableString(final Object... values) {
		if (values.length == 0) {
			return "[]";
		}
		final String[] stringValues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			final Object value = values[i];
			if (value == null) {
				stringValues[i] = NULL_VALUE_STRING;
			} else if (value.getClass().isArray()) {
				stringValues[i] = toPrintableString((Object[]) value);
			} else if (Collection.class.isAssignableFrom(value.getClass())) {
				stringValues[i] = toPrintableString(((Collection<?>) value).toArray());
			} else {
				stringValues[i] = value.toString();
			}
		}
		return "[" + String.join(", ", stringValues) + "]";
	}

}
