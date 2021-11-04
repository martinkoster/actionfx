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

import com.github.actionfx.core.ActionFX;

/**
 * Allows a simple event mechanism and a loose couple of ActionFX controllers.
 * Annotated methods are invoked, when an event of type {@code #value()} is
 * emitted through {@link ActionFX#publishNotification(Object)}.
 * <p>
 * In case the annotated method has a method argument that is of the same type
 * than {@link #value()}, then the emitted event is used as method argument to
 * that method.
 * <p>
 * Please note that methods annotated by {@link AFXSubscribe} can also have
 * additional method argument, that are e.g. annotated by
 * {@link AFXControlValue}.
 *
 * @author koster
 *
 */
@Retention(RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface AFXSubscribe {

	/**
	 * The emitted type that the annotated method shall be invoked on.
	 *
	 * @return the type the is listened to
	 */
	public Class<?> value();

	/**
	 * An optional order that can be specified to define the order of execution of
	 * the annotated method, in case more than one method have been subscribed to
	 * the given .
	 *
	 * @return the order, default is 1
	 */
	public int order() default 1;

	/**
	 * Optional flag that determines, whether the annotated method shall be executed
	 * in an asynchronous fashion. When set to {@code true}, the annotated method is
	 * not executed inside the JavaFX-thread, but in its own thread in order not to
	 * block the JavaFX thread. In case that UI components need to be updated in the
	 * method, the update itself needs to be run with
	 * {@link javafx.application.Platform#runLater(Runnable)}.
	 *
	 * @return {@code true},if method execution shall be performed from a new
	 *         thread, {@code false}, if the method shall be executed synchronously
	 *         inside the JavaFX thread. Default is {@code false}.
	 */
	public boolean async() default false;
}
