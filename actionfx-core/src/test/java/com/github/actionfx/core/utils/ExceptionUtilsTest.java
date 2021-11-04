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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link ExceptionUtils}.
 *
 * @author koster
 *
 */
class ExceptionUtilsTest {

	@Test
	void testToPrintableString() {
		assertThat(ExceptionUtils.toPrintableString(new Object[0]), equalTo("[]"));
		assertThat(ExceptionUtils.toPrintableString(null, null), equalTo("[null, null]"));
		assertThat(ExceptionUtils.toPrintableString(Integer.valueOf(42), "Hello World"), equalTo("[42, Hello World]"));
		assertThat(ExceptionUtils.toPrintableString(String.class, Integer.class),
				equalTo("[class java.lang.String, class java.lang.Integer]"));
	}

	@Test
	void testToPrintableString_withArray() {
		assertThat(ExceptionUtils.toPrintableString(Integer.valueOf(42),
				new Integer[] { Integer.valueOf(1), Integer.valueOf(2) }), equalTo("[42, [1, 2]]"));
	}

	@Test
	void testToPrintableString_withCollection() {
		assertThat(ExceptionUtils.toPrintableString(Integer.valueOf(42),
				Arrays.asList(Integer.valueOf(1), Integer.valueOf(2))), equalTo("[42, [1, 2]]"));
	}

	@Test
	void testWrapInRuntimeExceptionIfNecessary_isException() {
		// GIVEN
		final Exception ex = new Exception();

		// WHEN
		final RuntimeException wrapped = ExceptionUtils.wrapInRuntimeExceptionIfNecessary(ex);

		// THEN
		assertThat(wrapped, instanceOf(RuntimeWithNestedThrowableException.class));
		final RuntimeWithNestedThrowableException nested = (RuntimeWithNestedThrowableException) wrapped;
		assertThat(nested.getCause(), equalTo(ex));
	}

	@Test
	void testWrapInRuntimeExceptionIfNecessary_isRuntimeException() {
		// GIVEN
		final RuntimeException ex = new RuntimeException();

		// WHEN
		final RuntimeException wrapped = ExceptionUtils.wrapInRuntimeExceptionIfNecessary(ex);

		// THEN
		assertThat(wrapped, equalTo(ex));
	}
}
