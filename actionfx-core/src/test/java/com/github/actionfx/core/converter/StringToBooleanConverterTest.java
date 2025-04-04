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

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link StringToBooleanConverter}.
 *
 * @author koster
 *
 */
class StringToBooleanConverterTest {

    @Test
    void testApply_convertsToTrue() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(true);

		// WHEN and THEN
		assertThat(converter.apply("true")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("TRUE")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("TRue")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("on")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("yes")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("si")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("sì")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("oui")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("ja")).isEqualTo(Boolean.TRUE);
		assertThat(converter.apply("1")).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testApply_convertsToFalse() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(true);

		// WHEN and THEN
		assertThat(converter.apply("false")).isEqualTo(Boolean.FALSE);
		assertThat(converter.apply("FALSE")).isEqualTo(Boolean.FALSE);
		assertThat(converter.apply("FalSE")).isEqualTo(Boolean.FALSE);
		assertThat(converter.apply("off")).isEqualTo(Boolean.FALSE);
		assertThat(converter.apply("no")).isEqualTo(Boolean.FALSE);
		assertThat(converter.apply("non")).isEqualTo(Boolean.FALSE);
		assertThat(converter.apply("nein")).isEqualTo(Boolean.FALSE);
		assertThat(converter.apply("0")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void testApply_nullValueNotAllowed_expectDefaultValue() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(false);

		// WHEN and THEN
		assertThat(converter.apply(null)).isEqualTo(Boolean.FALSE);
		assertThat(converter.apply("invalid")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void testApply_unknownValueProvided_expectDefaultValue() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(false);

		// WHEN and THEN
		assertThat(converter.apply("invalid")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void testApply_nullValueAllowed_expectNullValue() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(true);

		// WHEN and THEN
		assertThat(converter.apply(null)).isNull();
    }

}
