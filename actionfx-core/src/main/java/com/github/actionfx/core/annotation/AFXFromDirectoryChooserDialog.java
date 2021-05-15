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
 * Method argument annotation which request the value of the annotated argument
 * of type {@link java.io.File}, {@link java.lang.String},
 * {@link java.nio.file.Path} or {@link java.net.URI} from a "directory chooser"
 * dialog.
 * <p>
 * In case the dialog is cancelled and no directory is chosen, the method
 * invocation is also cancelled, unless you set the attribute
 * {@link #continueOnCancel} to {@code true}. If this is the case, the method
 * argument will be {@code null}.
 *
 * @author koster
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AFXFromDirectoryChooserDialog {

	/**
	 * The title to be displayed in the "directory chooser" dialog.
	 *
	 * @return the title of the file open dialog. Default is "Open File"
	 */
	public String title() default "Select Folder";

	/**
	 * A resource bundle property key for the title text to be displayed in the
	 * "directory chooser" dialog. A value in this attribute has a higher priority
	 * than the value specified in {@link #title()}.
	 *
	 * @return the resource bundle property key for a "directory chooser" dialog
	 *         title
	 */
	public String titleKey() default "";

	/**
	 * Flag that determines what shall happen, in case the user cancels the
	 * "directory chooser" dialog. By default, the method invocation is also
	 * cancelled, when the "directory chooser" dialog is cancelled. If this flag is
	 * set to {@code true}, the method is invoked even if the "directory chooser"
	 * dialog is cancelled. In that case, the method argument will be {@code null}.
	 *
	 * @return {@code true}, if the method shall be invoked, even if the "directory
	 *         chooser" dialog is cancelled, {@code false}, if the method invocation
	 *         shall be also cancelled. Default is {@code false}.
	 */
	public boolean continueOnCancel() default false;
}
