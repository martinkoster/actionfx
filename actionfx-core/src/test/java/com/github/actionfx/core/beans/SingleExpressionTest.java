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
package com.github.actionfx.core.beans;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link SingleExpression}.
 *
 * @author koster
 *
 */
class SingleExpressionTest {

	@Test
	void testGetPropertyName() {
		assertThat(expr(null).getPropertyName(), equalTo(""));
		assertThat(expr("").getPropertyName(), equalTo(""));
		assertThat(expr("field").getPropertyName(), equalTo("field"));
		assertThat(expr("field[2]").getPropertyName(), equalTo("field"));
		assertThat(expr("field(map.key)").getPropertyName(), equalTo("field"));
	}

	@Test
	void testGetIndex() {
		assertThat(expr(null).getIndex(), equalTo(-1));
		assertThat(expr("").getIndex(), equalTo(-1));
		assertThat(expr("field").getIndex(), equalTo(-1));
		assertThat(expr("field[2]").getIndex(), equalTo(2));
		assertThat(expr("field(map.key)").getIndex(), equalTo(-1));
		assertThrows(IllegalArgumentException.class, () -> expr("field[nonnumber]").getIndex());
		assertThrows(IllegalArgumentException.class, () -> expr("field[]").getIndex());
		assertThrows(IllegalArgumentException.class, () -> expr("field[nonnumber").getIndex());
	}

	@Test
	void testGetKey() {
		assertThat(expr(null).getKey(), nullValue());
		assertThat(expr("").getKey(), nullValue());
		assertThat(expr("field").getKey(), nullValue());
		assertThat(expr("field[2]").getKey(), equalTo(""));
		assertThat(expr("field(map.key)").getKey(), equalTo("map.key"));
		assertThrows(IllegalArgumentException.class, () -> expr("field(map.key").getKey());
	}

	@Test
	void testIsIndexed() {
		assertThat(expr(null).isIndexed(), equalTo(false));
		assertThat(expr("").isIndexed(), equalTo(false));
		assertThat(expr("field").isIndexed(), equalTo(false));
		assertThat(expr("field[2]").isIndexed(), equalTo(true));
		assertThat(expr("field(map.key)").isIndexed(), equalTo(false));
	}

	@Test
	void testIsMapped() {
		assertThat(expr(null).isMapped(), equalTo(false));
		assertThat(expr("").isMapped(), equalTo(false));
		assertThat(expr("field").isMapped(), equalTo(false));
		assertThat(expr("field[2]").isMapped(), equalTo(false));
		assertThat(expr("field(map.key)").isMapped(), equalTo(true));
	}

	private static SingleExpression expr(final String value) {
		return new SingleExpression(value);
	}

}
