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

import javafx.beans.property.BooleanProperty;

/**
 * Annotated methods are invoked when the value of the specified
 * {@link #controlId()) is changed.
 *
 * @author koster
 *
 */
@Retention(RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface AFXOnValueChanged {

	/**
	 * ID of the control whose value shall be observed for changes. Please note that
	 * the given Id needs to be an existing node ID in the scene graph that
	 * evaluates to a {@link Control}.
	 *
	 * @return the node ID of a control, whose value shall be observed for changes.
	 */
	public String controlId();

	/**
	 * An optional timeout that is waited after the value change in the control
	 * occurs. The default value is 0, i.e. means the method is immediately executed
	 * after the value change occurs. In case there is a positive value specified,
	 * there is only one method invocation for the last change event that occurred
	 * in the time between first change event and the given number of timeout
	 * milliseconds.
	 * <p>
	 * This value can be used e.g. for reducing the number of method invocation
	 * (e.g. for a TextField you might not want to have this method invoked on every
	 * key stroke, but you might want to wait for multiple changes).
	 *
	 * @return the timeout in milliseconds
	 */
	public long timeoutMs() default 0;

	/**
	 * An optional expression that must resolve to a field of type
	 * {@link BooleanProperty}, if specified. The annotated method is only called,
	 * when the boolean property holds the value {@code true}. This attribute can be
	 * useful, when you want to activate the callback methods after a complete
	 * initialization of the JavaFX dialogue with values, etc.
	 *
	 * @return an expression that points to a field of type {@link BooleanProperty}.
	 *         Default is the empty string "", that means that the annotated method
	 *         is always called on changes.
	 */
	public String listenerActiveBooleanProperty() default "";
}
