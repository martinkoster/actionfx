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

import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.scene.control.Control;

/**
 * Extends controllers for functionality for {@link AFXNestedView} annotation.
 * It enables multi-selection for e.g. for table views, tree table views, list
 * views, etc.
 *
 * @author koster
 *
 */
public class EnableMultiSelectionControllerExtension
		extends AbstractAnnotatedFieldControllerExtension<AFXEnableMultiSelection> {

	public EnableMultiSelectionControllerExtension() {
		super(AFXEnableMultiSelection.class);
	}

	@Override
	protected void extend(final Object controller, final Field annotatedElement,
			final AFXEnableMultiSelection annotation) {
		final ControlWrapper controlWrapper = ControlWrapper
				.of(getFieldValue(controller, annotatedElement, Control.class));
		controlWrapper.enableMultiSelection();
	}

}
