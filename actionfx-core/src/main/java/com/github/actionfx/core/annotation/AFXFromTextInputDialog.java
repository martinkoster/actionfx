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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method argument annotation for requesting a text input of type {@code String}
 * from a "text input" dialog.
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
 * | Content Text   [Default Value                   ]  |
 * |                                                    |
 * |               [OK]     [Cancel]                    |
 * ------------------------------------------------------
 * </pre>
 *
 * @author koster
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AFXFromTextInputDialog {

	/**
	 * A title text to be displayed in the text input dialog. In case
	 * {@link #titleKey()} is set, than this value as a lower priority.
	 *
	 * @return the text input dialog title
	 */
	public String title() default "Text Input";

	/**
	 * A header text to be displayed in the text input dialog. In case
	 * {@link #headerKey()} is set, than this value as a lower priority.
	 *
	 * @return the text input dialog header text
	 */
	public String header() default "";

	/**
	 * A content text to be displayed in the text input dialog. In case
	 * {@link #contentKey()} is set, than this value as a lower priority.
	 *
	 * @return the text input dialog content text
	 */
	public String content() default "";

	/**
	 * A resource bundle property key for the title text to be displayed in the text
	 * input dialog. A value in this attribute has a higher priority than the value
	 * specified in {@link #title()}.
	 *
	 * @return the resource bundle property key for a text input dialog title
	 */
	public String titleKey() default "";

	/**
	 * A resource bundle property key for the header text to be displayed in the
	 * text input dialog. A value in this attribute has a higher priority than the
	 * value specified in {@link #header()}.
	 *
	 * @return the resource bundle property key for a text input dialog header text
	 */
	public String headerKey() default "";

	/**
	 * A resource bundle property key for the content text to be displayed in the
	 * text input dialog. A value in this attribute has a higher priority than the
	 * value specified in {@link #content()}.
	 *
	 * @return the resource bundle property key for a text input dialog content text
	 */
	public String contentKey() default "";

	/**
	 * A default value to be set into the text field of the "text input" dialog.
	 *
	 * @return the default value to set in the "text input" dialog.
	 */
	public String defaultValue() default "";

	/**
	 * Flag that determines what shall happen, in case the user cancels the "text
	 * input" dialog. By default, the method invocation is also cancelled, when the
	 * "text input" dialog is cancelled. If this flag is set to {@code true}, the
	 * method is invoked even if the "text input" dialog is cancelled. In that case,
	 * the method argument will be {@code null}.
	 *
	 * @return {@code true}, if the method shall be invoked, even if the "text
	 *         input" dialog is cancelled, {@code false}, if the method invocation
	 *         shall be also cancelled. Default is {@code false}.
	 */
	public boolean continueOnCancel() default false;
}
