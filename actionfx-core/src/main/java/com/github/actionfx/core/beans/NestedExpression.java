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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a property path expression that can be nested or not. Offers an
 * iterator to iterate over each {@link SingleExpression} inside this
 * {@link NestedExpression}.
 *
 * @author koster
 *
 */
public class NestedExpression extends Expression implements Iterable<SingleExpression> {

	public NestedExpression(final String value) {
		super(value);
	}

	/**
	 * Returns an iterator over the single expressions inside this nested
	 * expression.
	 */
	@Override
	public Iterator<SingleExpression> iterator() {
		return new NestedExpressionIterator(getValue());
	}

	/**
	 * Returns a stream over the single expressions inside this nested expression.
	 *
	 * @return the stream
	 */
	public Stream<SingleExpression> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Iterator over a nested path expression e.g. {@code list[0].value}.
	 *
	 * @author koster
	 *
	 */
	class NestedExpressionIterator implements Iterator<SingleExpression> {

		private String value;

		public NestedExpressionIterator(final String expression) {
			value = expression;
		}

		/**
		 * Indicates whether or not the iterator contains further property expressions
		 * or not.
		 *
		 * @return {@code true}, if there are further expressions in this iterator,
		 *         {@code false} otherwise.
		 */
		@Override
		public boolean hasNext() {
			return getValue() != null && getValue().length() > 0;
		}

		/**
		 * Extract the next {@link SingleExpression} from the current expression and
		 * removes it from the internal expression stack.
		 *
		 * @return the next property expression
		 */
		@Override
		public SingleExpression next() {
			final String expression = getValue();
			if (expression == null || expression.length() == 0) {
				throw new NoSuchElementException("No expressions left");
			}
			final SingleExpression singleExpression = previewNext();
			setValue(removeLeadingExpression(singleExpression.getValue()));
			return singleExpression;
		}

		/**
		 * Extract the next {@link SingleExpression} from the current expression without
		 * removing it.
		 *
		 * @return the next property expression
		 */
		protected SingleExpression previewNext() {
			final String expression = getValue();
			boolean indexed = false;
			boolean mapped = false;
			for (int i = 0; i < expression.length(); i++) {
				final char c = expression.charAt(i);
				if (indexed) {
					if (c == EXPR_INDEXED_END) {
						return new SingleExpression(expression.substring(0, i + 1));
					}
				} else if (mapped) {
					if (c == EXPR_MAPPED_END) {
						return new SingleExpression(expression.substring(0, i + 1));
					}
				} else {
					if (c == EXPR_NESTED) {
						return new SingleExpression(expression.substring(0, i));
					} else if (c == EXPR_MAPPED_START) {
						mapped = true;
					} else if (c == EXPR_INDEXED_START) {
						indexed = true;
					}
				}
			}
			return new SingleExpression(expression);
		}

		/**
		 * Remove the given property expression from the current expression.
		 *
		 * @param expression the expression to remove
		 * @return the new expression value, with first property expression removed -
		 *         null if there are no more expressions
		 */
		private String removeLeadingExpression(final String expression) {
			final String currentExpression = getValue();
			if (currentExpression.length() == expression.length()) {
				return null;
			}
			int start = expression.length();
			if (currentExpression.charAt(start) == EXPR_NESTED) {
				start++;
			}
			return currentExpression.substring(start);
		}

		private String getValue() {
			return value;
		}

		private void setValue(final String value) {
			this.value = value;
		}
	}
}
