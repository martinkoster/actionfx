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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

/**
 * JUnit test for {@link NestedExpression}.
 *
 * @author koster
 *
 */
class NestedExpressionTest {

	@Test
	void testHasNext() {
        assertThat(expr(null).iterator().hasNext()).isFalse();
        assertThat(expr("").iterator().hasNext()).isFalse();
        assertThat(expr("field").iterator().hasNext()).isTrue();
        assertThat(expr("field[0]").iterator().hasNext()).isTrue();
        assertThat(expr("field(map.key)").iterator().hasNext()).isTrue();
        assertThat(expr("field.anotherfield").iterator().hasNext()).isTrue();
	}

	@Test
	void testNext_isSingleExpression() {
		// GIVEN
		final NestedExpression exp = expr("field");

		// WHEN
		final SingleExpression se = exp.iterator().next();

		// THEN
		assertThat(se).isNotNull();
		assertThat(se.getValue()).isEqualTo("field");
	}

	@Test
	void testNext_isSingleExpression_indexed() {
		// GIVEN
		final NestedExpression exp = expr("field[0]");

		// WHEN
		final SingleExpression se = exp.iterator().next();

		// THEN
		assertThat(se).isNotNull();
		assertThat(se.getValue()).isEqualTo("field[0]");
        assertThat(se.isIndexed()).isTrue();
	}

	@Test
	void testNext_isSingleExpression_mapped() {
		// GIVEN
		final NestedExpression exp = expr("field(map.key)");

		// WHEN
		final SingleExpression se = exp.iterator().next();

		// THEN
		assertThat(se).isNotNull();
		assertThat(se.getValue()).isEqualTo("field(map.key)");
        assertThat(se.isMapped()).isTrue();
	}

	@Test
	void testNext_isNested() {
		// GIVEN
		final NestedExpression exp = expr("field.list[4].map(map.key).elem");
		final List<SingleExpression> seList = new ArrayList<>();
		final Iterator<SingleExpression> it = exp.iterator();

		// WHEN
		while (it.hasNext()) {
			seList.add(it.next());
		}

		// THEN
        assertThat(seList.stream().map(SingleExpression::getValue).toList()).containsExactly("field", "list[4]", "map(map.key)", "elem");
	}

	@Test
	void testStream_isNested() {
		// GIVEN
		final NestedExpression exp = expr("field.list[4].map(map.key).elem");

		// WHEN
        final List<String> seList = exp.stream().map(SingleExpression::getValue).toList();

		// THEN
		assertThat(seList).containsExactly("field", "list[4]", "map(map.key)", "elem");
	}

	@Test
	void testNext_isEmpty() {
		// GIVEN
		final NestedExpression exp = expr("");
		final Iterator<SingleExpression> it = exp.iterator();

		// WHEN
		final NoSuchElementException ex = assertThrows(NoSuchElementException.class, it::next);

		// THEN
		assertThat(ex.getMessage()).isEqualTo("No expressions left");
	}

	@Test
	void testNext_isNull() {
		// GIVEN
		final NestedExpression exp = expr(null);
		final Iterator<SingleExpression> it = exp.iterator();

		// WHEN
		final NoSuchElementException ex = assertThrows(NoSuchElementException.class, it::next);

		// THEN
		assertThat(ex.getMessage()).isEqualTo("No expressions left");
	}

	private static NestedExpression expr(final String value) {
		return new NestedExpression(value);
	}

}
