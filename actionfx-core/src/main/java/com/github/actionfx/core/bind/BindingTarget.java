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
package com.github.actionfx.core.bind;

import com.github.actionfx.core.view.graph.ControlProperties;

import javafx.scene.control.Control;

/**
 * Describes a desired binding between a {@link Control} and a value inside a
 * JavaBean.
 *
 * @author koster
 *
 */
public class BindingTarget {

	private final Control control;

	private final ControlProperties targetProperty;

	private final String formatPattern;

	private final Class<?> beanClass;

	private final String beanPathExpression;

	/**
	 * Constructor accepting all required arguments for defining a binding target.
	 *
	 * @param control            the control holding the value to bind
	 * @param targetProperty     the target property of the control that shall be
	 *                           used as binding target
	 * @param beanClass          the model bean class
	 * @param beanPathExpression the bean path expression relative to the root been
	 *                           (used for nested beans inside a bean hierarchy).
	 *                           Expression is empty, in case the
	 *                           {@code owningInstance} is the {@code rootInstance}
	 * @param formatPattern      an optional format pattern that is used as
	 *                           constructor argument for a supplied
	 *                           {@code converter}.
	 */
	public BindingTarget(final Control control, final ControlProperties targetProperty, final Class<?> beanClass,
			final String beanPathExpression, final String formatPattern) {
		this.control = control;
		this.targetProperty = targetProperty;
		this.beanClass = beanClass;
		this.beanPathExpression = beanPathExpression;
		this.formatPattern = formatPattern;
	}

	public Control getControl() {
		return control;
	}

	public String getBeanPathExpression() {
		return beanPathExpression;
	}

	public Object getBeanClass() {
		return beanClass;
	}

	public ControlProperties getTargetProperty() {
		return targetProperty;
	}

	public String getFormatPattern() {
		return formatPattern;
	}

}
