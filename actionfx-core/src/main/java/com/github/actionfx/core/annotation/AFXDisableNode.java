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
package com.github.actionfx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation that can be applied to a {@link Node} for defining, when
 * this node shall be disabled (and when enabled).
 *
 * @author koster
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXDisableNode {

	/**
	 * Annotated node is disabled, if all controls specified in this attribute have
	 * a user value (i.e. a text in a {@link javafx.scene.control.TextField} or
	 * <b>selected values</b> in a {@link javafx.scene.control.TableView}).
	 *
	 * @return the list of controls
	 */
	public String[] whenAllContolsHaveUserValues() default {};

	/**
	 * Annotated node is disabled, if at least one of the controls specified in this
	 * attribute has a user value (i.e. a text in a
	 * {@link javafx.scene.control.TextField} or <b>selected values</b> in a
	 * {@link javafx.scene.control.TableView}).
	 *
	 * @return the list of controls
	 */
	public String[] whenAtLeastOneContolHasUserValue() default {};

	/**
	 * Annotated node is disabled, if all controls specified in this attribute have
	 * a value (i.e. a text in a {@link javafx.scene.control.TextField} or items set
	 * in a {@link javafx.scene.control.TableView}).
	 *
	 * @return the list of controls
	 */
	public String[] whenAllControlsHaveValues() default {};

	/**
	 * Annotated node is disabled, if at least one control specified in this
	 * attribute has a value (i.e. a text in a
	 * {@link javafx.scene.control.TextField} or items set in a
	 * {@link javafx.scene.control.TableView}).
	 *
	 * @return the list of controls
	 */
	public String[] whenAtLeastOneControlHasValues() default {};

	/**
	 * In case more than one attribute is specified as part of this annotation, this
	 * boolean operation describes how the different attributes shall be logically
	 * linked with each other. Possible values are {@link BooleanOp#AND} and
	 * {@link BooleanOp#OR}. Default is {@link BooleanOp#AND}.
	 *
	 * @return the logical operation how attributes are linked with each other.
	 *         Default is {@link BooleanOp#AND}.
	 */
	public BooleanOp logicalOp() default BooleanOp.AND;

}
