/*
 * Copyright (c) 2020 Martin Koster
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
package com.github.actionfx.core.view;

import java.lang.reflect.Field;
import java.util.Map;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.utils.AFXUtils;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.graph.NodeWrapper;

/**
 * {@link View} implementation that uses FXML for describing the view.
 *
 * @author koster
 *
 */
public class FxmlView extends AbstractView {

	private final Object controller;

	public FxmlView(final String id, final String fxmlLocation, final Object controller) {
		this.id = id;
		rootNode = AFXUtils.loadFxml(fxmlLocation, controller);
		this.controller = controller;
		attachNestedViews(controller);
	}

	/**
	 * Checks for fields annotated by {@link AFXNestedView} and attaches these views
	 * to the nodes.
	 */
	protected void attachNestedViews(final Object controller) {
		final Map<AFXNestedView, Field> annotatedFieldMap = AnnotationUtils.findAnnotatedFields(controller.getClass(),
				AFXNestedView.class, true);
		if (annotatedFieldMap.isEmpty()) {
			return;
		}
		final ActionFX actionFX = ActionFX.getInstance();
		for (final var entry : annotatedFieldMap.entrySet()) {
			final AFXNestedView nestedView = entry.getKey();
			final Field field = entry.getValue();

			// get view to attach
			final View viewToAttach = actionFX.getView(nestedView.refViewId());
			if (viewToAttach == null) {
				throw new IllegalStateException("Nested view with viewId='" + nestedView.refViewId()
						+ "' does not exist, can not embed it into this view '" + id + "'!");
			}
			final Object fieldValue = ReflectionUtils.getFieldValue(field, controller);
			if (fieldValue == null) {
				throw new IllegalStateException("Nested view with viewId='" + nestedView.refViewId()
						+ "' can not be attached to field with name='" + field.getName()
						+ "', the field value is null!");

			}
			final NodeWrapper target = NodeWrapper.of(fieldValue);
			target.attachNode(viewToAttach.getRootNode(), NodeWrapper.nodeAttacherFor(target, nestedView));
		}
	}

	@Override
	public Object getController() {
		return controller;
	}
}
