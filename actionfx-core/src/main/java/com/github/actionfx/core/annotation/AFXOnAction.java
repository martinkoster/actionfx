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
 * Annotated methods are invoked when the node or control specified with fires {@link javafx.event.ActionEvent}. This is
 * e.g. when the user clicks on {@link javafx.scene.control.Button}.
 * <p>
 * Annotated methods can be of the following signature:
 * <ul>
 * <li><tt>void methodName()</tt></li>
 * <li><tt>void methodName(javafx.event.ActionEvent event)</tt></li>
 * </ul>
 * You can also combine this annotation with annotation {@link AFXControlValue}:
 * <ul>
 * <li><tt>void methodName(@AFXControlValue("usernameTextField") String username)</tt></li>
 * </ul>
 * In this case, the user value entered in text field with ID 'usernameTextField' will be injected as method argument.
 *
 * @author koster
 *
 */
@Retention(RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface AFXOnAction {

    /**
     * ID of the node whose action property shall be set to execute the annotated method.
     *
     * @return the node ID, whose action property will invoke the annotated method.
     */
    public String nodeId();

    /**
     * Optional flag that determines, whether the annotated method shall be executed in an asynchronous fashion. When
     * set to {@code true}, the annotated method is not executed inside the JavaFX-thread, but in its own thread in
     * order not to block the JavaFX thread. In case that UI components need to be updated in the method, the update
     * itself needs to be run with {@link javafx.application.Platform#runLater(Runnable)}.
     *
     * @return {@code true},if method execution shall be performed from a new thread, {@code false}, if the method shall
     *         be executed synchronously inside the JavaFX thread. Default is {@code false}.
     */
    public boolean async() default false;

}
