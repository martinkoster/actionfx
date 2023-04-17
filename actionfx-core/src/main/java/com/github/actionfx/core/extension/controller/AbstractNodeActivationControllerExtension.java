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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.github.actionfx.core.annotation.BooleanOp;
import com.github.actionfx.core.utils.BooleanBindingBuilder;
import com.github.actionfx.core.utils.BooleanBindingBuilder.MatchingOp;
import com.github.actionfx.core.validation.ValidationMessage;
import com.github.actionfx.core.validation.ValidationResult;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;

/**
 * Abstract base class for controller field extension that manipulates the node state via the
 * {@link javafx.scene.Node#disabledProperty()} depending on controls in the JavaFX scene graph.
 *
 * @author koster
 *
 */
public abstract class AbstractNodeActivationControllerExtension<A extends Annotation>
        extends AbstractAnnotatedFieldControllerExtension<A> {

    protected AbstractNodeActivationControllerExtension(final Class<A> annotationType) {
        super(annotationType);
    }

    /**
     * Creates a new {@link BooleanBindingBuilder} instance with the controls defined in the arrays
     * {@code whenAllControlsHaveUserValues}. {@code whenAllControlsHaveValues},
     * {@code whenAtLeastOneContolHasUserValue} and {@code whenAtLeastOneControlHasValues}. The controls are taken from
     * the supplied {@link View}.
     *
     * @param view
     *            the view
     * @param whenAllContolsHaveUserValuesIds
     *            control IDs that are checked, whether all have user values
     * @param whenAllControlsHaveValuesIds
     *            control IDs that are checked, whether all have a value or values
     * @param whenAtLeastOneContolHasUserValueIds
     *            control IDs that are checked, whether at least on has a user value
     * @param whenAtLeastOneControlHasValuesIds
     *            control IDs that are checked, whether at least on has a value or values
     * @param whenControlsAreValidIds
     *            list of control IDs that are required to be valid
     * @param whenAllControlsAreValid
     *            flag that determines whether all controls inside the view must be valid
     * @param booleanOp
     *            the boolean operation that shall be used to link the individual conditions with each other
     * @return a builder for creating a boolean binding with the supplied data
     */
    protected BooleanBindingBuilder createBooleanBindingBuilder(final View view,
            final String[] whenAllContolsHaveUserValuesIds, final String[] whenAllControlsHaveValuesIds,
            final String[] whenAtLeastOneContolHasUserValueIds, final String[] whenAtLeastOneControlHasValuesIds,
            final String[] whenControlsAreValidIds, final boolean whenAllControlsAreValid,
            final BooleanOp booleanOp) {
        final ControlWrapper[] whenAllContolsHaveUserValues = createControlWrapper(view,
                whenAllContolsHaveUserValuesIds);
        final ControlWrapper[] whenAllControlsHaveValues = createControlWrapper(view, whenAllControlsHaveValuesIds);
        final ControlWrapper[] whenAtLeastOneContolHasUserValue = createControlWrapper(view,
                whenAtLeastOneContolHasUserValueIds);
        final ControlWrapper[] whenAtLeastOneControlHasValues = createControlWrapper(view,
                whenAtLeastOneControlHasValuesIds);
        final Control[] whenControlsAreValid = lookupControls(view, whenControlsAreValidIds);
        return createBooleanBindingBuilder(view, whenAllContolsHaveUserValues, whenAllControlsHaveValues,
                whenAtLeastOneContolHasUserValue, whenAtLeastOneControlHasValues, whenControlsAreValid,
                whenAllControlsAreValid, booleanOp);
    }

    /**
     * Creates a new {@link BooleanBindingBuilder} instance with the controls defined in the arrays
     * {@code whenAllControlsHaveUserValues}. {@code whenAllControlsHaveValues},
     * {@code whenAtLeastOneContolHasUserValue} and {@code whenAtLeastOneControlHasValues}. The controls are taken from
     * the supplied {@link View}.
     *
     * @param view
     *            the view instance
     * @param whenAllContolsHaveUserValues
     *            controls that are checked, whether all have user values
     * @param whenAllControlsHaveValues
     *            controls that are checked, whether all have a value or values
     * @param whenAtLeastOneContolHasUserValue
     *            controls that are checked, whether at least on has a user value
     * @param whenAtLeastOneControlHasValues
     *            controls that are checked, whether at least on has a value or values
     * @param whenControlsAreValid
     *            list of controls that are required to be valid
     * @param whenAllControlsAreValid
     *            flag that determines whether all controls inside the view must be valid (if set to {@code true}, this
     *            supersedes any value provided {@code whenControlsAreValid}.
     * @param booleanOp
     *            the boolean operation that shall be used to link the individual conditions with each other
     * @return the boolean binding builder
     */
    @SuppressWarnings("unchecked")
    protected BooleanBindingBuilder createBooleanBindingBuilder(final View view,
            final ControlWrapper[] whenAllContolsHaveUserValues, final ControlWrapper[] whenAllControlsHaveValues,
            final ControlWrapper[] whenAtLeastOneContolHasUserValue,
            final ControlWrapper[] whenAtLeastOneControlHasValues, final Control[] whenControlsAreValid,
            final boolean whenAllControlsAreValid, final BooleanOp booleanOp) {
        final BooleanBindingBuilder builder = BooleanBindingBuilder.builder();
        if (whenAllContolsHaveUserValues.length > 0) {
            builder.andAllTestablesFulfillPredicate(ControlWrapper::hasUserValueSet).booleanOp(booleanOp)
                    .testables(whenAllContolsHaveUserValues)
                    .observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
                    .finishPredicate();
        }
        if (whenAllControlsHaveValues.length > 0) {
            builder.andAllTestablesFulfillPredicate(ControlWrapper::hasValueOrItemsSet).booleanOp(booleanOp)
                    .testables(whenAllControlsHaveValues)
                    .observableExtractorFunction(ControlWrapper::getValueOrItemsAsObservable)
                    .finishPredicate();
        }
        if (whenAtLeastOneContolHasUserValue.length > 0) {
            builder.andAtLeastOneTestableFulfillsPredicate(ControlWrapper::hasUserValueSet).booleanOp(booleanOp)
                    .testables(whenAtLeastOneContolHasUserValue)
                    .observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
                    .finishPredicate();
        }
        if (whenAtLeastOneControlHasValues.length > 0) {
            builder.andAtLeastOneTestableFulfillsPredicate(ControlWrapper::hasValueOrItemsSet).booleanOp(booleanOp)
                    .testables(whenAtLeastOneControlHasValues)
                    .observableExtractorFunction(ControlWrapper::getValueOrItemsAsObservable)
                    .finishPredicate();
        }
        if (whenAllControlsAreValid) {
            builder.andForPredicate(createAllControlsAreValidPredicate(view)).matchingOp(MatchingOp.ALL_MATCH)
                    .testables(new ObservableValue[] { view.validationResultProperty() })
                    .finishPredicate();
        } else if (whenControlsAreValid.length > 0) {
            builder.andForPredicate(createControlsAreValidPredicate(whenControlsAreValid))
                    .matchingOp(MatchingOp.ALL_MATCH)
                    .testables(new ObservableValue[] { view.validationResultProperty() })
                    .finishPredicate();
        }
        return builder;
    }

    /**
     * Creates a predicate that tests whether all controls that are validated inside the view have no validation errors.
     *
     * @param view
     *            the view instance holding the validating controls
     * @return the predicate
     */
    protected Predicate<ObservableValue<ValidationResult>> createAllControlsAreValidPredicate(final View view) {
        return observable -> {
            final ValidationResult vr = observable.getValue();
            return vr.getErrors().isEmpty();
        };
    }

    /**
     * Creates a predicate that tests whether the given controls have no validation errors.
     *
     * @param controls
     *            the list of validated controls
     * @return the predicate
     */
    protected Predicate<ObservableValue<ValidationResult>> createControlsAreValidPredicate(
            final Control[] controls) {
        return observable -> {
            final ValidationResult vr = observable.getValue();
            final List<ValidationMessage> errorMessages = vr.getErrors();
            return errorMessages.stream().map(ValidationMessage::getTarget).distinct()
                    .noneMatch(Arrays.asList(controls)::contains);
        };
    }

}
