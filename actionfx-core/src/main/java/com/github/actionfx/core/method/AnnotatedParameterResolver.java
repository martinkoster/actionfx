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
package com.github.actionfx.core.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Interface for resolving a method parameter having an annotation to a value. Derived classes can be stateful as new
 * instances are created for each parameter resolution.
 *
 * @author koster
 *
 * @param <A>
 *            the annotation that is expected to be applied on the parameter to resolve
 */
public interface AnnotatedParameterResolver<A extends Annotation> {

    /**
     * Resolves the given {@code parameter} that is annotated with the supplied {@code annotation} to the expected type
     * {@code expectedType}.
     *
     * @param <T>
     *            the type that is expected to be returned
     * @param controller
     *            the controller instance holding the method
     * @param method
     *            the method inside the controller that has the given {@code parameter}
     * @param parameter
     *            the parameter that shall be resolved
     * @param annotation
     *            the annotation that is applied to the given {@code parameter}
     * @param expectedType
     *            the expected type the parameter shall be resolved to
     * @return the resolved parameter
     * @throws IllegalStateException
     *             in case the parameter can not be resolved to the desired type {@code expectedType}
     */
    <T> T resolve(Object controller, Method method, final Parameter parameter, A annotation,
            final Class<T> expectedType);

    /**
     * Method that determines whether method invocation shall continue. Derived classes can override this method to stop
     * method invocation (e.g. when a value shall be taken from a UI dialog, but the user cancels this UI dialog).
     *
     * @return {@code true}, if method invocation shall proceed, {@code false}, if method shall not be invoked.
     */
    public default boolean continueMethodInvocation() {
        return true;
    }
}
