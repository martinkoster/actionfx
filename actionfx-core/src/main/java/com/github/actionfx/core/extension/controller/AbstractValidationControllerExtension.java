/*  **************************************************************
 *   Projekt         : actionfx-core (java)
 *  --------------------------------------------------------------
 *   Autor(en)       : MartinKoster
 *   Beginn-Datum    : 21.09.2022
 *  --------------------------------------------------------------
 *   copyright (c) 2015-18   DB Station&Service AG
 *   Alle Rechte vorbehalten.
 *  **************************************************************
 */
package com.github.actionfx.core.extension.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.github.actionfx.core.validation.Validator;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.scene.control.Control;

/**
 * Base class for controller extensions performing a validation.
 *
 * @author MartinKoster
 */
public abstract class AbstractValidationControllerExtension<A extends Annotation>
        extends AbstractAnnotatedFieldControllerExtension<A> {

    protected AbstractValidationControllerExtension(final Class<A> annotationType) {
        super(annotationType);
    }

    @Override
    protected void extend(final Object controller, final Field annotatedElement, final A annotation) {
        final Control control = getFieldValue(controller, annotatedElement, Control.class);
        final ControlWrapper controlWrapper = ControlWrapper
                .of(control);

    }

    /**
     * Implementing controller extension need to instantiate a {@link Validator} for the given {@link ControlWrapper}.
     *
     * @param controller
     *            the ActionFX controller holding the control
     * @param controlWrapper
     *            the control that shall be validated, wrapped inside a {@link ControlWrapper}
     * @param annotation
     *            the annotation taken from the field holding the control
     * @return the validator instance
     */
    protected abstract Validator createValidator(Object controller, ControlWrapper controlWrapper, A annotation);

    /**
     * Is the control annotated by {@code annotation} a required field?
     *
     * @param annotation
     *            the annotation on the control to be validated
     * @return {@code true}, if the control is a mandatory control, {@code false}, otherwise.
     */
    protected abstract boolean isRequired(A annotation);
}
