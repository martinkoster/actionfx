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

import java.lang.reflect.Method;
import java.util.Comparator;

import com.github.actionfx.core.annotation.AFXOnControlValueChange;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.listener.TimedChangeListener;
import com.github.actionfx.core.listener.TimedListChangeListener;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.property.BooleanProperty;

/**
 * Extends controllers for functionality for {@link AFXOnControlValueChange}
 * annotation. It links value change events of referenced controls to an
 * execution of the annotated method.
 *
 * @author koster
 *
 */
public class OnControlValueChangeMethodControllerExtension
		extends AbstractAnnotatedMethodControllerExtension<AFXOnControlValueChange> {

	private static final Comparator<Method> AFXONCONTROLVALUECHANGECOMPARATOR = new OrderBasedAnnotatedMethodComparator<>(
			AFXOnControlValueChange.class, AFXOnControlValueChange::controlId, AFXOnControlValueChange::order);

	public OnControlValueChangeMethodControllerExtension() {
		super(AFXOnControlValueChange.class, AFXONCONTROLVALUECHANGECOMPARATOR);
	}

	@Override
	protected void extend(final Object controller, final Method annotatedElement,
			final AFXOnControlValueChange annotation) {
		final View view = ControllerWrapper.getViewFrom(controller);
		final BooleanProperty listenerActionBooleanProperty = lookupObservableValue(controller,
				annotation.listenerActiveBooleanProperty(), BooleanProperty.class);
		final ControlWrapper controlWrapper = createControlWrapper(view, annotation.controlId());
		// check, whether the wrapped control supports multi-selection or only single
		// selection
		if (controlWrapper.supportsMultiSelection()) {
			final TimedListChangeListener<?> changeListener = createListChangeListener(controller, annotatedElement,
					annotation.timeoutMs(), listenerActionBooleanProperty, controlWrapper::getSelectedValues,
					controlWrapper::getSelectedValue);
			controlWrapper.addSelectedValuesChangeListener(changeListener);
		} else if (controlWrapper.supportsSelection()) {
			final TimedChangeListener<?> changeListener = createValueChangeListener(controller, annotatedElement,
					annotation.timeoutMs(), listenerActionBooleanProperty);
			controlWrapper.addSelectedValueChangeListener(changeListener);
		} else if (controlWrapper.supportsValue()) {
			final TimedChangeListener<?> changeListener = createValueChangeListener(controller, annotatedElement,
					annotation.timeoutMs(), listenerActionBooleanProperty);
			controlWrapper.addValueChangeListener(changeListener);
		} else if (controlWrapper.supportsValues()) {
			final TimedListChangeListener<?> changeListener = createListChangeListener(controller, annotatedElement,
					annotation.timeoutMs(), listenerActionBooleanProperty, controlWrapper::getValues,
					controlWrapper::getValue);
			controlWrapper.addValuesChangeListener(changeListener);
		} else {
			throw new IllegalStateException("Control with ID='" + annotation.controlId()
					+ "' does not support user input listening! Please check your ActionFX annotations inside constroller '"
					+ controller.getClass().getCanonicalName() + "'!");
		}
	}

}
