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

import static org.assertj.core.api.Assertions.assertThat;
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
		assertThat(expr(null).getPropertyName()).isEqualTo("");
		assertThat(expr("").getPropertyName()).isEqualTo("");
		assertThat(expr("field").getPropertyName()).isEqualTo("field");
		assertThat(expr("field[2]").getPropertyName()).isEqualTo("field");
		assertThat(expr("field(map.key)").getPropertyName()).isEqualTo("field");
	}

	@Test
	void testGetIndex() {
		assertThat(expr(null).getIndex()).isEqualTo(-1);
		assertThat(expr("").getIndex()).isEqualTo(-1);
		assertThat(expr("field").getIndex()).isEqualTo(-1);
		assertThat(expr("field[2]").getIndex()).isEqualTo(2);
		assertThat(expr("field(map.key)").getIndex()).isEqualTo(-1);
		assertThrows(IllegalArgumentException.class, () -> expr("field[nonnumber]").getIndex());
		assertThrows(IllegalArgumentException.class, () -> expr("field[]").getIndex());
		assertThrows(IllegalArgumentException.class, () -> expr("field[nonnumber").getIndex());
	}

	@Test
	void testGetKey() {
		assertThat(expr(null).getKey()).isNull();
		assertThat(expr("").getKey()).isNull();
		assertThat(expr("field").getKey()).isNull();
		assertThat(expr("field[2]").getKey()).isEqualTo("");
		assertThat(expr("field(map.key)").getKey()).isEqualTo("map.key");
		assertThrows(IllegalArgumentException.class, () -> expr("field(map.key").getKey());
	}

	@Test
	void testIsIndexed() {
		assertThat(expr(null).isIndexed()).isFalse();
		assertThat(expr("").isIndexed()).isFalse();
		assertThat(expr("field").isIndexed()).isFalse();
		assertThat(expr("field[2]").isIndexed()).isTrue();
		assertThat(expr("field(map.key)").isIndexed()).isFalse();
	}

	@Test
	void testIsMapped() {
		assertThat(expr(null).isMapped()).isFalse();
		assertThat(expr("").isMapped()).isFalse();
		assertThat(expr("field").isMapped()).isFalse();
		assertThat(expr("field[2]").isMapped()).isFalse();
		assertThat(expr("field(map.key)").isMapped()).isTrue();
	}

	private static SingleExpression expr(final String value) {
		return new SingleExpression(value);
	}

}
