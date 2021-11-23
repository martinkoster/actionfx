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

import com.github.actionfx.core.annotation.AFXDisableNode;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.ControlBasedBooleanBindingBuilder;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;

import javafx.scene.Node;

/**
 * Controller field extension that disables nodes of the JavaFX scene graph
 * depending on control values.
 *
 * @author koster
 *
 */
public class DisableNodeControllerExtension extends AbstractNodeActivationControllerExtension<AFXDisableNode> {

	public DisableNodeControllerExtension() {
		super(AFXDisableNode.class);
	}

	@Override
	protected void extend(final Object controller, final Field annotatedElement, final AFXDisableNode annotation) {
		final Object annotatedNode = ReflectionUtils.getFieldValue(annotatedElement, controller);
		if (annotatedNode == null) {
			throw new IllegalStateException("Field value of field '" + annotatedElement.getName() + "' is null!");
		}
		if (!Node.class.isAssignableFrom(annotatedNode.getClass())) {
			throw new IllegalStateException(
					"Field value of field '" + annotatedElement.getName() + "' is not of type javafx.scene.Node!");
		}
		final Node node = (Node) annotatedNode;
		final View view = ControllerWrapper.getViewFrom(controller);
		final ControlBasedBooleanBindingBuilder builder = createBooleanBindingBuilder(view,
				annotation.whenAllContolsHaveUserValues(), annotation.whenAllControlsHaveValues(),
				annotation.whenAtLeastOneContolHasUserValue(), annotation.whenAtLeastOneControlHasValues(),
				annotation.logicalOp());
		node.disableProperty().unbind();
		node.disableProperty().bind(builder.build());
	}
}
