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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link StringToObjectConverter}.
 *
 * @author koster
 *
 */
class StringToObjectConverterTest {

    @Test
    void testApply_withValueOf() {
        // GIVEN
        final StringToObjectConverter<CustomObjectWithValueOf> converter = new StringToObjectConverter<>(
                CustomObjectWithValueOf.class);

        // WHEN
        final CustomObjectWithValueOf result = converter.convert("Hello there");

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getValue()).isEqualTo("Hello there");
    }

    @Test
    void testApply_withConstructor() {
        // GIVEN
        final StringToObjectConverter<CustomObjectWithConstructor> converter = new StringToObjectConverter<>(
                CustomObjectWithConstructor.class);

        // WHEN
        final CustomObjectWithConstructor result = converter.convert("Hello there");

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getValue()).isEqualTo("Hello there");
    }

    @Test
    void testApply_unknownObject_nullIsReturned() {
        // GIVEN
        final StringToObjectConverter<UnknownObject> converter = new StringToObjectConverter<>(
                UnknownObject.class);

		// WHEN and THEN
		assertThat(converter.convert("Hello there")).isNull();
    }

    @Test
    void testApply_withNullValue_nullIsReturned() {
        // GIVEN
        final StringToObjectConverter<UnknownObject> converter = new StringToObjectConverter<>(
                UnknownObject.class);

		// WHEN and THEN
		assertThat(converter.convert(null)).isNull();
    }

    @Test
    void testApply_withCharset() {
        // GIVEN
        final StringToObjectConverter<Charset> converter = new StringToObjectConverter<>(
                Charset.class);

        // WHEN
        final Charset result = converter.convert("UTF8");

		// THEN
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(StandardCharsets.UTF_8);
    }

	public static final class CustomObjectWithValueOf {

        private String value;

        private CustomObjectWithValueOf(final String value) {
            this.value = value;
        }

        public static CustomObjectWithValueOf valueOf(final String value) {
            return new CustomObjectWithValueOf(value);
        }

        String getValue() {
            return value;
        }

    }

    public static class CustomObjectWithConstructor {

        private String value;

        public CustomObjectWithConstructor(final String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    public static class UnknownObject {

    }

}
