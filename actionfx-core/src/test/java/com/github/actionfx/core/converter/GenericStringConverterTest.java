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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * JUnit test case for {@link GenericStringConverter}.
 *
 * @author koster
 *
 */
class GenericStringConverterTest {

	private Converter<Integer, String> toStringConverter;

	private Converter<String, Integer> fromStringConverter;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void onSetup() {
		toStringConverter = Mockito.mock(Converter.class);
		fromStringConverter = Mockito.mock(Converter.class);
		when(toStringConverter.apply(anyInt())).thenReturn("hello world");
		when(fromStringConverter.apply(anyString())).thenReturn(Integer.valueOf(42));
	}

	@Test
	void testFromString() {
		// GIVEN
		final GenericStringConverter<Integer> converter = new GenericStringConverter<>(toStringConverter,
				fromStringConverter);

		// WHEN and THEN
		assertThat(converter.fromString("hello world")).isEqualTo(Integer.valueOf(42));
	}

	@Test
	void testToString() {
		// GIVEN
		final GenericStringConverter<Integer> converter = new GenericStringConverter<>(toStringConverter,
				fromStringConverter);

		// WHEN and THEN
		assertThat(converter.toString(Integer.valueOf(42))).isEqualTo("hello world");
	}

	@Test
	void testFromString_defaultConverter() {
		// GIVEN
		final GenericStringConverter<Integer> converter = new GenericStringConverter<>(null, null);

		// WHEN and THEN
		assertThat(converter.fromString("hello world")).isNull();
	}

	@Test
	void testToString_defaultConverter() {
		// GIVEN
		final GenericStringConverter<Integer> converter = new GenericStringConverter<>(null, null);

		// WHEN and THEN
		assertThat(converter.toString(Integer.valueOf(42))).isEqualTo("42");
	}

}
