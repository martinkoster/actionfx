/*
 * Copyright (c) 2022 Martin Koster
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
package com.github.actionfx.core.validation;

import java.lang.reflect.Method;

import com.github.actionfx.core.method.ActionFXMethodInvocation;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.scene.control.Control;

/**
 * {@link Validator} implementation that forwards the validation to a method inside an ActionFX controller returning a
 * {@link ValidationResult} value.
 *
 * @author MartinKoster
 */
public class CustomMethodValidator implements Validator {

	private final Object controller;

	private final Method method;

    public CustomMethodValidator(final Object controller, final Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public ValidationResult validate(final Control control, final ControlProperties controlProperty) {
        final ControlWrapper controlWrapper = ControlWrapper.of(control);
        final Object currentValue = controlWrapper.getValue(controlProperty);
        final ActionFXMethodInvocation methodInvocation = new ActionFXMethodInvocation(controller,
                method, currentValue);
        final Object returnValue = methodInvocation.call();
        return returnValue instanceof ValidationResult ? (ValidationResult) returnValue : null;
    }

}
