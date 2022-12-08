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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat(converter.apply("true"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("TRUE"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("TRue"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("on"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("yes"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("si"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("sì"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("oui"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("ja"), equalTo(Boolean.TRUE));
        assertThat(converter.apply("1"), equalTo(Boolean.TRUE));
    }

    @Test
    void testApply_convertsToFalse() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(true);

        // WHEN and THEN
        assertThat(converter.apply("false"), equalTo(Boolean.FALSE));
        assertThat(converter.apply("FALSE"), equalTo(Boolean.FALSE));
        assertThat(converter.apply("FalSE"), equalTo(Boolean.FALSE));
        assertThat(converter.apply("off"), equalTo(Boolean.FALSE));
        assertThat(converter.apply("no"), equalTo(Boolean.FALSE));
        assertThat(converter.apply("non"), equalTo(Boolean.FALSE));
        assertThat(converter.apply("nein"), equalTo(Boolean.FALSE));
        assertThat(converter.apply("0"), equalTo(Boolean.FALSE));
    }

    @Test
    void testApply_nullValueNotAllowed_expectDefaultValue() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(false);

        // WHEN and THEN
        assertThat(converter.apply(null), equalTo(Boolean.FALSE));
        assertThat(converter.apply("invalid"), equalTo(Boolean.FALSE));
    }

    @Test
    void testApply_unknownValueProvided_expectDefaultValue() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(false);

        // WHEN and THEN
        assertThat(converter.apply("invalid"), equalTo(Boolean.FALSE));
    }

    @Test
    void testApply_nullValueAllowed_expectNullValue() {
        // GIVEN
        final StringToBooleanConverter converter = new StringToBooleanConverter(true);

        // WHEN and THEN
        assertThat(converter.apply(null), nullValue());
    }

}
