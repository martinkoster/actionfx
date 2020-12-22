/*
 * Copyright (c) 2020 Martin Koster
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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javafx.stage.Stage;

/**
 * Annotation to be applied at method level that allows to define which view to
 * be display after method invocation.
 *
 * @author koster
 *
 */
@AFXMethodInterceptable
@Retention(RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface AFXShowView {

	/**
	 * The view to be displayed, when the method successfully terminates. This
	 * attribute competes with attribute {@link #showNestedViews()}, while this
	 * attribute has higher precedence than {@link #showNestedViews()}.
	 *
	 * @return the view ID for the success view
	 */
	public String showView() default "";

	/**
	 * Determines whether the view defined in {@link #showView()} shall be displayed
	 * in its own {@link Stage}. The specification of this attribute does not affect
	 * view transition in case the attribute {@link #showNestedViews()} is given.
	 *
	 * @return {@code true}, if the referenced view shall be displayed in its own
	 *         {@link Stage}, {@code false}, if the currently used {@link Stage}
	 *         shall be used. Default is {@code false}.
	 */
	public boolean showInNewWindow() default false;

	/**
	 * The nested views to be displayed, when the method successfully terminates.
	 * This attribute allows to embed view into the current scene graph and
	 * {@link Stage}. Please take note, that this attribute must not be used
	 * together with {@link #showView()} and {@link #showInNewWindow()}.
	 *
	 * @return the nested views to be displayed
	 */
	public AFXNestedView[] showNestedViews() default {};

}
