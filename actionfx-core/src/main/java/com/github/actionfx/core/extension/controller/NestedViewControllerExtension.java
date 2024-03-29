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

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.NodeWrapper;

/**
 * Extends controllers for functionality for {@link AFXNestedView} annotation.
 * It retrieves the desired view and injects it into the annotated field.
 *
 * @author koster
 *
 */
public class NestedViewControllerExtension extends AbstractAnnotatedFieldControllerExtension<AFXNestedView> {

	public NestedViewControllerExtension() {
		super(AFXNestedView.class);
	}

	@Override
	protected void extend(final Object controller, final Field annotatedElement, final AFXNestedView annotation) {
		final ActionFX actionFX = ActionFX.getInstance();
		final View viewToAttach = actionFX.getView(annotation.refViewId());
		final Object fieldValue = ReflectionUtils.getFieldValue(annotatedElement, controller);
		if (fieldValue == null) {
			throw new IllegalStateException(
					"Nested view with viewId='" + annotation.refViewId() + "' can not be attached to field with name='"
							+ annotatedElement.getName() + "', the field value is null!");

		}
		final NodeWrapper target = NodeWrapper.of(fieldValue);
		if (!target.isParent()) {
			throw new IllegalStateException(
					"Nested view with viewId='" + annotation.refViewId() + "' can not be attached to field with name='"
							+ annotatedElement.getName() + "', the field value is not a javafx.scene.Parent!");
		}
		viewToAttach.attachViewToParent(target.getWrapped(), NodeWrapper.nodeAttacherFor(target, annotation));
	}

}
