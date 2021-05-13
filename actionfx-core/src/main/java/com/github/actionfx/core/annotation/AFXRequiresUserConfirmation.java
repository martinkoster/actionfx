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

/**
 * Annotated methods are only executed, when the user confirms the execution via
 * a confirmation dialog.
 * <p>
 * The layout of the dialog is as follows:
 * <p>
 *
 * <pre>
 * ------------------------------------------------------
 * | Title                                              |
 * |----------------------------------------------------|
 * |                                                    |
 * | Header Text                                        |
 * |												    |
 * |----------------------------------------------------|
 * | Content Text                                       |
 * |                                                    |
 * |               [OK]     [Cancel]                    |
 * ------------------------------------------------------
 * </pre>
 *
 * @author koster
 *
 */
@Retention(RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface AFXRequiresUserConfirmation {

	/**
	 * A title text to be displayed in the confirmation dialog. In case
	 * {@link #titleKey()} is set, than this value as a lower priority.
	 *
	 * @return the confirmation dialog title
	 */
	public String title() default "Confirmation";

	/**
	 * A header text to be displayed in the confirmation dialog. In case
	 * {@link #headerKey()} is set, than this value as a lower priority.
	 *
	 * @return the confirmation dialog header text
	 */
	public String header() default "";

	/**
	 * A content text to be displayed in the confirmation dialog. In case
	 * {@link #contentKey()} is set, than this value as a lower priority.
	 *
	 * @return the confirmation dialog content text
	 */
	public String content() default "";

	/**
	 * A resource bundle property key for the title text to be displayed in the
	 * confirmation dialog. A value in this attribute has a higher priority than the
	 * value specified in {@link #title()}.
	 *
	 * @return the resource bundle property key for a confirmation dialog title
	 */
	public String titleKey() default "";

	/**
	 * A resource bundle property key for the header text to be displayed in the
	 * confirmation dialog. A value in this attribute has a higher priority than the
	 * value specified in {@link #header()}.
	 *
	 * @return the resource bundle property key for a confirmation dialog header
	 *         text
	 */
	public String headerKey() default "";

	/**
	 * A resource bundle property key for the content text to be displayed in the
	 * confirmation dialog. A value in this attribute has a higher priority than the
	 * value specified in {@link #content()}.
	 *
	 * @return the resource bundle property key for a confirmation dialog content
	 *         text
	 */
	public String contentKey() default "";
}
