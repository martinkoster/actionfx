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
package com.github.actionfx.core.extension.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.validation.ValidationOptions;
import com.github.actionfx.core.validation.Validator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.scene.control.Control;

/**
 * Base class for controller extensions performing a validation.
 *
 * @author koster
 */
public abstract class AbstractValidationControllerExtension<A extends Annotation>
        extends AbstractAnnotatedFieldControllerExtension<A> {

    protected AbstractValidationControllerExtension(final Class<A> annotationType) {
        super(annotationType);
    }

    @Override
    protected void extend(final Object controller, final Field annotatedElement, final A annotation) {
        final View view = ControllerWrapper.of(controller).getView();
        final Control control = getFieldValue(controller, annotatedElement, Control.class);
        final Validator validator = createValidator(controller, control, annotation);
        view.registerValidator(control, getValidatedControlProperty(annotation), validator,
                createValidationOptions(annotation));
    }

    /**
     * Implementing controller extension need to instantiate a {@link Validator} for the given {@link ControlWrapper}.
     *
     * @param controller
     *            the ActionFX controller holding the control
     * @param control
     *            the control that shall be validated
     * @param annotation
     *            the annotation taken from the field holding the control
     * @return the validator instance
     */
    protected abstract Validator createValidator(Object controller, Control control, A annotation);

    /**
     * Implementing classes need to derive a valid set of {@link ValidationOptions} from the supplied
     * {@code annotation}.
     *
     * @param annotation
     *            the annotation
     * @return the derived validation options.
     */
    protected abstract ValidationOptions createValidationOptions(A annotation);

    /**
     * Determines which property of the control shall be validated, using the given {@code annotation} as information
     * source.
     *
     * @param annotation
     *            the annotation specifying the property of the control to be validated
     * @return the control property to be validated
     */
    protected abstract ControlProperties getValidatedControlProperty(A annotation);

}
