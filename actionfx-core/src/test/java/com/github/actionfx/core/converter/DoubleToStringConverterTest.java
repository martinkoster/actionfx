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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link DoubleToStringConverter}.
 *
 * @author koster
 *
 */
class DoubleToStringConverterTest {

	@Test
	void testApply_germanLocale() {
		// GIVEN
		final DoubleToStringConverter converter = new DoubleToStringConverter("#,###.##", Locale.GERMANY, true);

		// WHEN and THEN
		assertThat(converter.apply(Double.valueOf(10000.4356)), equalTo("10.000,44")); // German format with rounding
		assertThat(converter.apply(Double.valueOf(100)), equalTo("100"));
		assertThat(converter.apply(null), nullValue());
	}

	@Test
	void testApply_usLocale() {
		// GIVEN
		final DoubleToStringConverter converter = new DoubleToStringConverter("#,###.##", Locale.US, true);

		// WHEN and THEN
		assertThat(converter.apply(Double.valueOf(10000.4356)), equalTo("10,000.44")); // US format with rounding
		assertThat(converter.apply(Double.valueOf(100)), equalTo("100"));
		assertThat(converter.apply(null), nullValue());
	}

	@Test
	void testApply_noPattern_usLocale() {
		// GIVEN
		final DoubleToStringConverter converter = new DoubleToStringConverter(null, Locale.US, true);

		// WHEN and THEN
		assertThat(converter.apply(Double.valueOf(10000.435)), equalTo("10,000.435")); // US format
		assertThat(converter.apply(Double.valueOf(100)), equalTo("100"));
		assertThat(converter.apply(null), nullValue());
	}

	@Test
	void testApply_nullValueNotAllowed_expectDefaultValue() {
		// GIVEN
		final DoubleToStringConverter converter = new DoubleToStringConverter("#,###.##", Locale.GERMANY, false);

		// WHEN and THEN
		assertThat(converter.apply(null), equalTo("0"));
	}

}
