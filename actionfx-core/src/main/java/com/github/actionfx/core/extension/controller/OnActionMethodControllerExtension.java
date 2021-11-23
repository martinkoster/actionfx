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

import java.lang.reflect.Method;

import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.method.ActionFXMethodInvocation;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Extends controllers for functionality for {@link AFXOnAction} annotation. It
 * wires the "onAction" property of controls inside the view's scenegraph to the
 * annotated methods.
 *
 * @author koster
 *
 */
public class OnActionMethodControllerExtension extends AbstractAnnotatedMethodControllerExtension<AFXOnAction> {

	public OnActionMethodControllerExtension() {
		super(AFXOnAction.class);
	}

	@Override
	protected void extend(final Object controller, final Method annotatedElement, final AFXOnAction annotation) {
		final View view = ControllerWrapper.getViewFrom(controller);
		final NodeWrapper nodeWrapper = createNodeWrapper(annotation.nodeId(), view);
		final ObjectProperty<EventHandler<ActionEvent>> onActionProperty = nodeWrapper.getOnActionProperty();
		if (onActionProperty == null) {
			throw new IllegalStateException("Control with id='" + annotation.nodeId() + "' and type '"
					+ nodeWrapper.getWrappedType().getCanonicalName()
					+ "' does not support an 'onAction' property! Please verify your @AFXOnAction annotation in controller class '"
					+ controller.getClass().getCanonicalName() + "', method '" + annotatedElement.getName() + "'!");
		}
		if (annotation.async()) {
			onActionProperty.setValue(ActionFXMethodInvocation.forOnActionPropertyWithAsyncCall(returnValue -> {
			}, controller, annotatedElement));
		} else {
			onActionProperty.setValue(ActionFXMethodInvocation.forOnActionProperty(controller, annotatedElement));

		}
	}

}
