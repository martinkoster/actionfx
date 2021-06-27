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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.actionfx.core.annotation.AFXFormBinding;
import com.github.actionfx.core.annotation.AFXFormMapping;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Control;

/**
 * Extends controllers for binding a value inside an annotated field of type
 * {@link ObjectProperty} to the conrols of the view.
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
		final Object objectPropertyValue = objectProperty.getValue();
		final View view = ControllerWrapper.getViewFrom(controller);
		final List<AFXFormMapping> mappingAnnotations = AnnotationUtils.findAllAnnotations(annotatedElement,
				AFXFormMapping.class);
		final Map<String, String> fieldToControlMap = mappingAnnotations.stream()
				.collect(Collectors.toMap(AFXFormMapping::name, AFXFormMapping::controlId));

		final ControlWrapper controlWrapper = ControlWrapper
				.of(getFieldValue(controller, annotatedElement, Control.class));
		controlWrapper.enableMultiSelection();
	}

}
