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
package com.github.actionfx.core.container.extension;

import java.lang.annotation.Annotation;

import com.github.actionfx.core.annotation.BooleanOp;
import com.github.actionfx.core.utils.ControlBasedBooleanBindingBuilder;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;

/**
 * Abstract base class for controller field extension that manipulates the node
 * state via the {@link javafx.scene.Node#disabledProperty()} depending on
 * controls in the JavaFX scene graph.
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
	 * Creates a new {@link ControlBasedBooleanBindingBuilder} instance with the
	 * controls defined in the arrays {@code whenAllControlsHaveUserValues}.
	 * {@code whenAllControlsHaveValues}, {@code whenAtLeastOneContolHasUserValue}
	 * and {@code whenAtLeastOneControlHasValues}. The controls are taken from the
	 * supplied {@link View}.
	 *
	 * @param view                                the view
	 * @param whenAllContolsHaveUserValuesIds     control IDs that are checked,
	 *                                            whether all have user values
	 * @param whenAllControlsHaveValuesIds        control IDs that are checked,
	 *                                            whether all have a value or values
	 * @param whenAtLeastOneContolHasUserValueIds control IDs that are checked,
	 *                                            whether at least on has a user
	 *                                            value
	 * @param whenAtLeastOneControlHasValuesIds   control IDs that are checked,
	 *                                            whether at least on has a value or
	 *                                            values
	 * @param booleanOp                           the boolean operation that shall
	 *                                            be used to link the individual
	 *                                            conditions with each other
	 * @return
	 */
	protected ControlBasedBooleanBindingBuilder createBooleanBindingBuilder(final View view,
			final String[] whenAllContolsHaveUserValuesIds, final String[] whenAllControlsHaveValuesIds,
			final String[] whenAtLeastOneContolHasUserValueIds, final String[] whenAtLeastOneControlHasValuesIds,
			final BooleanOp booleanOp) {
		final ControlWrapper[] whenAllContolsHaveUserValues = createControlWrapper(view,
				whenAllContolsHaveUserValuesIds);
		final ControlWrapper[] whenAllControlsHaveValues = createControlWrapper(view, whenAllControlsHaveValuesIds);
		final ControlWrapper[] whenAtLeastOneContolHasUserValue = createControlWrapper(view,
				whenAtLeastOneContolHasUserValueIds);
		final ControlWrapper[] whenAtLeastOneControlHasValues = createControlWrapper(view,
				whenAtLeastOneControlHasValuesIds);
		return createBooleanBindingBuilder(whenAllContolsHaveUserValues, whenAllControlsHaveValues,
				whenAtLeastOneContolHasUserValue, whenAtLeastOneControlHasValues, booleanOp);
	}

	/**
	 * Creates a new {@link ControlBasedBooleanBindingBuilder} instance with the
	 * controls defined in the arrays {@code whenAllControlsHaveUserValues}.
	 * {@code whenAllControlsHaveValues}, {@code whenAtLeastOneContolHasUserValue}
	 * and {@code whenAtLeastOneControlHasValues}. The controls are taken from the
	 * supplied {@link View}.
	 *
	 * @param whenAllContolsHaveUserValues     controls that are checked, whether
	 *                                         all have user values
	 * @param whenAllControlsHaveValues        controls that are checked, whether
	 *                                         all have a value or values
	 * @param whenAtLeastOneContolHasUserValue controls that are checked, whether at
	 *                                         least on has a user value
	 * @param whenAtLeastOneControlHasValues   controls that are checked, whether at
	 *                                         least on has a value or values
	 * @param booleanOp                        the boolean operation that shall be
	 *                                         used to link the individual
	 *                                         conditions with each other
	 * @return
	 */
	protected ControlBasedBooleanBindingBuilder createBooleanBindingBuilder(
			final ControlWrapper[] whenAllContolsHaveUserValues, final ControlWrapper[] whenAllControlsHaveValues,
			final ControlWrapper[] whenAtLeastOneContolHasUserValue,
			final ControlWrapper[] whenAtLeastOneControlHasValues, final BooleanOp booleanOp) {
		final ControlBasedBooleanBindingBuilder builder = ControlBasedBooleanBindingBuilder.create(false);
		if (whenAllContolsHaveUserValues.length > 0) {
			builder.addNewPredicate(booleanOp).forControl(whenAllContolsHaveUserValues)
					.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
					.allFulfilledFunction(ControlWrapper::hasUserValueSet);
		}
		if (whenAllControlsHaveValues.length > 0) {
			builder.addNewPredicate(booleanOp).forControl(whenAllControlsHaveValues)
					.observableExtractorFunction(ControlWrapper::getValueOrValuesAsObservable)
					.allFulfilledFunction(ControlWrapper::hasValueOrValuesSet);
		}
		if (whenAtLeastOneContolHasUserValue.length > 0) {
			builder.addNewPredicate(booleanOp).forControl(whenAtLeastOneContolHasUserValue)
					.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
					.atLeastOneFulfilledFunction(ControlWrapper::hasUserValueSet);
		}
		if (whenAtLeastOneControlHasValues.length > 0) {
			builder.addNewPredicate(booleanOp).forControl(whenAtLeastOneControlHasValues)
					.observableExtractorFunction(ControlWrapper::getValueOrValuesAsObservable)
					.atLeastOneFulfilledFunction(ControlWrapper::hasValueOrValuesSet);
		}
		return builder;
	}

}
