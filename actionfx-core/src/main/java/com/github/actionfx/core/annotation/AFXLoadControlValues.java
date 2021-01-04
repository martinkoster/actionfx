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
 * Annotated methods are returning the data that is displayed in referenced
 * {@link Control}s.
 *
 * @author koster
 *
 */
@Retention(RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface AFXLoadControlValues {

	/**
	 * ID of the control whose values shall be loaded for by the annotated method
	 *
	 * @return the node ID of a control for that data shall be loaded
	 */
	public String controlId();

	/**
	 * Optional flag that determines, whether the data shall be loaded in an
	 * asynchronous fashion. When set to {@code true}, the annotated method is not
	 * executed inside the JavaFX-thread, but in its own thread in order not to
	 * block the JavaFX thread. The data itself however is set again to the
	 * referenced control from inside the JavaFX thread.
	 *
	 * @return {@code true},if method execution shall be performed from a new
	 *         thread, {@code false}, if the method shall be executed synchronously
	 *         inside the JavaFX thread. Default is {@code false}.
	 */
	public boolean async() default false;

	/**
	 * An optional expression that must resolve to a field of type
	 * {@link BooleanProperty}, if specified. The annotated method is only called
	 * after construction (before {@code @PostConstruct} initialization methods),
	 * when the boolean property holds the value {@code true}.
	 * <p>
	 * After construction time, the data is loaded, whenever the boolean property
	 * switches its value from {@code false} to {@code true}.
	 *
	 * @return an expression that points to a field of type {@link BooleanProperty}.
	 *         Default is the empty string "", that means that the annotated method
	 *         is called after construction time of the controller, but before
	 *         {@code @PostConstruct} annotated methods.
	 */
	public String loadingActiveBooleanProperty() default "";

	/**
	 * An optional order that can be specified to define the order of execution of
	 * the annotated method, in case more than one data loading routine is present
	 * inside the ActionFX controller.
	 *
	 * @return the order, default is 1
	 */
	public int order() default 1;
}
