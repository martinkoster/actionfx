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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper;

/**
 * Implementation of {@link AnnotatedParameterResolver} that uses annotation
 * {@link AFXControlValue} to resolve the parameter argument by taking the value
 * from the control specified in {@link AFXControlValue}.
 *
 * @author koster
 *
 */
public class ControlValueAnnotatedParameterResolver implements AnnotatedParameterResolver<AFXControlValue> {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T resolve(final Object controller, final Method method, final Parameter parameter,
			final AFXControlValue controlValue, final Class<T> expectedType) {
		final View view = ControllerWrapper.getViewFrom(controller);
		if (view == null) {
			throw new IllegalStateException("There is no view associated with controller of type '"
					+ controller.getClass().getCanonicalName() + "'!");
		}
		final NodeWrapper control = view.lookupNode(controlValue.value());
		if (control == null) {
			throw new IllegalStateException("There is no node with ID='" + controlValue.value()
					+ "' inside the view associated with controller '" + controller.getClass().getCanonicalName()
					+ "'!");
		}
		if (!javafx.scene.control.Control.class.isAssignableFrom(control.getWrappedType())) {
			throw new IllegalStateException(
					"Node with ID='" + controlValue.value() + "' inside the view hosted by controller '"
							+ controller.getClass().getCanonicalName() + "' is not a javafx.scene.control.Control!");
		}
		final ControlWrapper controlWrapper = ControlWrapper.of(control.getWrapped());
		final Object value;
		switch (controlValue.sourceProperty()) {
		case SINGLE_VALUE_PROPERTY:
			value = controlWrapper.getValue();
			break;
		case ITEMS_OBSERVABLE_LIST:
			value = controlWrapper.getItems();
			break;
		case USER_VALUE_OBSERVABLE:
			value = controlWrapper.getUserValue();
			break;
		default:
			throw new IllegalStateException("Unsupported control property '" + controlValue.sourceProperty() + "'!");
		}
		if (value != null && !parameter.getType().isAssignableFrom(value.getClass())) {
			throw new IllegalStateException("Value retrieved for control with ID='" + controlValue.value()
					+ "' inside the view hosted by controller '" + controller.getClass()
					+ "' is not compatible with the method argument of type '" + parameter.getType()
					+ "'! Control value is of type '" + value.getClass() + "'");

		}
		return (T) value;
	}

}
