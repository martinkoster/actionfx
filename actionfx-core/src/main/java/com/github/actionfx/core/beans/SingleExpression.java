package com.github.actionfx.core.beans;

/**
 * Represent a single property path expression e.g. {@code list[0]}.
 *
 * @author koster
 *
 */
public class SingleExpression extends Expression {

	public SingleExpression(final String expression) {
		super(expression);
	}

	/**
	 * Return the property name from the property expression.
	 *
	 * @param expression The property expression
	 * @return The property name
	 */

	public String getPropertyName() {
		final String expression = getValue();
		if (expression == null || expression.length() == 0) {
			return "";
		}
		for (int i = 0; i < expression.length(); i++) {
			final char c = expression.charAt(i);
			if (c == EXPR_NESTED || c == EXPR_MAPPED_START || c == EXPR_INDEXED_START) {
				return expression.substring(0, i);
			}
		}
		return expression;
	}

	/**
	 * Return the index value from the property expression or -1.
	 *
	 * @return The index value or -1 if the property is not indexed
	 * @throws IllegalArgumentException If the indexed property is illegally formed
	 *                                  or has an invalid (non-numeric) value.
	 */
	public int getIndex() {
		if (getValue() == null || getValue().length() == 0) {
			return -1;
		}
		for (int i = 0; i < getValue().length(); i++) {
			final char c = getValue().charAt(i);
			if (c == EXPR_NESTED || c == EXPR_MAPPED_START) {
				return -1;
			}
			if (c == EXPR_INDEXED_START) {
				final int end = getValue().indexOf(EXPR_INDEXED_END, i);
				if (end < 0) {
					throw new IllegalArgumentException("Missing end delimiter in expression '" + getValue() + "'");
				}
				final String value = getValue().substring(i + 1, end);
				if (value.length() == 0) {
					throw new IllegalArgumentException("No Index Value");
				}
				int index = 0;
				try {
					index = Integer.parseInt(value, 10);
				} catch (final Exception e) {
					throw new IllegalArgumentException("Invalid index value '" + value + "'");
				}
				return index;
			}
		}
		return -1;
	}

	/**
	 * Return the map key from the property expression or <code>null</code>.
	 *
	 * @return The index value
	 * @throws IllegalArgumentException If the mapped property is illegally formed.
	 */
	public String getKey() {
		if (getValue() == null || getValue().length() == 0) {
			return "";
		}
		for (int i = 0; i < getValue().length(); i++) {
			final char c = getValue().charAt(i);
			if (c == EXPR_NESTED || c == EXPR_INDEXED_START) {
				return "";
			} else if (c == EXPR_MAPPED_START) {
				final int end = getValue().indexOf(EXPR_MAPPED_END, i);
				if (end < 0) {
					throw new IllegalArgumentException("Missing end delimiter in expression '" + getValue() + "'");
				}
				return getValue().substring(i + 1, end);
			}
		}
		return "";
	}

	/**
	 * Indicate whether the expression is for an indexed property or not.
	 *
	 * @return {@code true} if the expression is indexed, otherwise {@code false}
	 */

	public boolean isIndexed() {
		if (getValue() == null || getValue().length() == 0) {
			return false;
		}
		for (int i = 0; i < getValue().length(); i++) {
			final char c = getValue().charAt(i);
			if (c == EXPR_NESTED || c == EXPR_MAPPED_START) {
				return false;
			} else if (c == EXPR_INDEXED_START) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicate whether the expression is for a mapped property or not.
	 *
	 * @return {@code true} if the expression is mapped, otherwise {@code false}
	 */
	public boolean isMapped() {
		if (getValue() == null || getValue().length() == 0) {
			return false;
		}
		for (int i = 0; i < getValue().length(); i++) {
			final char c = getValue().charAt(i);
			if (c == EXPR_NESTED || c == EXPR_INDEXED_START) {
				return false;
			} else if (c == EXPR_MAPPED_START) {
				return true;
			}
		}
		return false;
	}
}