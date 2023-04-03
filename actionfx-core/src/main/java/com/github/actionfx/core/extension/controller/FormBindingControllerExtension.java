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
package com.github.actionfx.core.extension.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.github.actionfx.core.annotation.AFXFormBinding;
import com.github.actionfx.core.annotation.AFXFormMapping;
import com.github.actionfx.core.annotation.BooleanValue;
import com.github.actionfx.core.bind.MappingBasedBindingTargetResolver;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.method.ActionFXMethodInvocation;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.validation.BooleanValidator;
import com.github.actionfx.core.validation.CustomMethodValidator;
import com.github.actionfx.core.validation.MinMaxValidator;
import com.github.actionfx.core.validation.RegExpValidator;
import com.github.actionfx.core.validation.RequiredValidator;
import com.github.actionfx.core.validation.SizeValidator;
import com.github.actionfx.core.validation.TemporalValidator;
import com.github.actionfx.core.validation.ValidationOptions;
import com.github.actionfx.core.validation.Validator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Control;

/**
 * Extends controllers for binding a value inside an annotated field of type {@link ObjectProperty} to the conrols of
 * the view.
 *
 * @author koster
 *
 */
public class FormBindingControllerExtension extends AbstractAnnotatedFieldControllerExtension<AFXFormBinding> {

    public FormBindingControllerExtension() {
        super(AFXFormBinding.class);
    }

    @Override
    protected void extend(final Object controller, final Field annotatedElement, final AFXFormBinding annotation) {
        final Object fieldValue = ReflectionUtils.getFieldValue(annotatedElement, controller);
        if (fieldValue == null) {
            throw new IllegalStateException("Field '" + annotatedElement.getName() + "' in controller '"
                    + controller.getClass().getCanonicalName()
                    + "' is annotated by @AFXFormBinding, but value is null!");
        }
        if (!ObjectProperty.class.isAssignableFrom(fieldValue.getClass())) {
            throw new IllegalStateException("Field '" + annotatedElement.getName() + "' in controller '"
                    + controller.getClass().getCanonicalName()
                    + "' is annotated by @AFXFormBinding, is not of expected type javafx.beans.property.ObjectProperty!");
        }
        final ObjectProperty<?> objectProperty = (ObjectProperty<?>) fieldValue;
        final View view = ControllerWrapper.getViewFrom(controller);
        final List<AFXFormMapping> mappingAnnotations = AnnotationUtils.findAllAnnotations(annotatedElement,
                AFXFormMapping.class);
        final MappingBasedBindingTargetResolver resolver = new MappingBasedBindingTargetResolver(
                annotation.disableNameBasedMapping(), annotation.controlPrefix(), annotation.controlSuffix());
        mappingAnnotations.forEach(fm -> resolver.registerMapping(fm.controlId(), fm.targetProperty(),
                fm.propertyName(), fm.formatPattern()));
        prepareBinding(objectProperty, view, resolver);
        addValidatorsToView(controller, view, mappingAnnotations);
    }

    /**
     * Prepares the binding between a model object inside the supplied {@code objectProperty} and controls inside the
     * given {@code view}.
     * <p>
     * In case the value in the {@code objectProperty} is not null, an immediate binding is required. Additionally, when
     * changing the model inside the property, the binding with the new model is created.
     *
     * @param objectProperty
     *            the property holding the model class
     * @param view
     *            the view holding the controls
     * @param resolver
     *            the binding target resolver
     */
    private void prepareBinding(final ObjectProperty<?> objectProperty, final View view,
            final MappingBasedBindingTargetResolver resolver) {
        final Object objectPropertyValue = objectProperty.getValue();
        if (objectPropertyValue != null) {
            view.bind(objectPropertyValue, resolver);
        }
        objectProperty.addListener((observable, oldValue, newValue) -> {
            view.unbind(oldValue);
            view.bind(newValue, resolver);
        });
    }

    /**
     * Adds validators to the view instance, in case {@code mappingAnnotations} contain validation instructions.
     *
     * @param controller
     *            the controller
     * @param view
     *            the view to register validators to
     * @param mappingAnnotations
     *            the list of mapping annotations with potential validation rules
     */
    private void addValidatorsToView(final Object controller, final View view,
            final List<AFXFormMapping> mappingAnnotations) {
        for (final AFXFormMapping fm : mappingAnnotations) {
            final String validationMessage = getMessage(controller.getClass(), fm);
            final ControlWrapper controlWrapper = createControlWrapper(view, fm.controlId());
            final List<Validator> validatorList = new ArrayList<>();
            final ValidationOptions validationOptions = createValidationOptions(fm);
            addRequiredValidator(validatorList, fm, validationMessage);
            addMinMaxValidator(validatorList, fm, validationMessage);
            addSizeValidator(validatorList, fm, validationMessage);
            addRegExpValidator(validatorList, fm, validationMessage);
            addTemporalValidator(validatorList, fm, validationMessage);
            addBooleanValidator(validatorList, view, fm, validationMessage, controlWrapper);
            addCustomMethodValidator(validatorList, controller, fm, controlWrapper);
            validatorList.forEach(validator -> view.registerValidator((Control) controlWrapper.getWrapped(),
                    fm.targetProperty(), validator, validationOptions));
        }
    }

    private void addCustomMethodValidator(final List<Validator> validatorList, final Object controller,
            final AFXFormMapping fm,
            final ControlWrapper controlWrapper) {
        if (!"".equals(fm.validationMethod())) {
            final Object currentValue = controlWrapper.getValue(fm.targetProperty());
            final ActionFXMethodInvocation methodInvocation = new ActionFXMethodInvocation(controller,
                    fm.validationMethod(), true, currentValue);
            validatorList.add(new CustomMethodValidator(controller, methodInvocation.getMethod()));
        }
    }

    private void addRequiredValidator(final List<Validator> validatorList, final AFXFormMapping fm,
            final String validationMessage) {
        if (isRequiredValidationOnly(fm)) {
            validatorList.add(new RequiredValidator(validationMessage));
        }
    }

    private boolean isRequiredValidationOnly(final AFXFormMapping fm) {
        return fm.required() && !fm.past() && !fm.pastOrPresent() && !fm.future() && !fm.futureOrPresent()
                && "".equals(fm.regExp()) && fm.minVal() == Double.MIN_VALUE && fm.maxVal() == Double.MAX_VALUE
                && fm.minSize() == Long.MIN_VALUE && fm.maxSize() == Long.MAX_VALUE
                && fm.expectedBoolean() == BooleanValue.UNDEFINED;
    }

    private void addMinMaxValidator(final List<Validator> validatorList, final AFXFormMapping fm,
            final String validationMessage) {
        if (fm.minVal() != Double.MIN_VALUE || fm.maxVal() != Double.MAX_VALUE) {
            validatorList.add(new MinMaxValidator(validationMessage, fm.minVal(), fm.maxVal(),
                    fm.formatPattern(), fm.required()));
        }
    }

    private void addSizeValidator(final List<Validator> validatorList, final AFXFormMapping fm,
            final String validationMessage) {
        if (fm.minSize() != Long.MIN_VALUE || fm.maxSize() != Long.MAX_VALUE) {
            validatorList.add(new SizeValidator(validationMessage, fm.minSize(), fm.maxSize(), fm.required()));
        }
    }

    private void addRegExpValidator(final List<Validator> validatorList, final AFXFormMapping fm,
            final String validationMessage) {
        if (!"".equals(fm.regExp())) {
            validatorList.add(new RegExpValidator(validationMessage, fm.regExp(), fm.required()));
        }
    }

    private void addTemporalValidator(final List<Validator> validatorList, final AFXFormMapping fm,
            final String validationMessage) {
        if (fm.past() || fm.pastOrPresent() || fm.futureOrPresent() || fm.future()) {
            validatorList.add(new TemporalValidator(validationMessage, fm.past(), fm.pastOrPresent(),
                    fm.future(), fm.futureOrPresent(), fm.formatPattern(), fm.required()));
        }
    }

    private void addBooleanValidator(final List<Validator> validatorList, final View view, final AFXFormMapping fm,
            final String validationMessage,
            final ControlWrapper controlWrapper) {
        if (fm.expectedBoolean() != BooleanValue.UNDEFINED) {
            validatorList.add(new BooleanValidator(validationMessage, fm.expectedBoolean().getValue(), fm.required()));
        }
    }

    private ValidationOptions createValidationOptions(final AFXFormMapping fm) {
        return ValidationOptions.options().required(fm.required()).validationMode(fm.validationMode())
                .validationStartTimeoutMs(fm.validationStartTimeoutMs());
    }

    private String getMessage(final Class<?> controller, final AFXFormMapping fm) {
        return getMessage(controller.getClass(), fm.validationMessageKey(), fm.validationMessage());
    }
}
