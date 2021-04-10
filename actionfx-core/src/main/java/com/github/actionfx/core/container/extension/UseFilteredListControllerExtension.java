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

import com.github.actionfx.core.annotation.AFXUseFilteredList;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Control;

/**
 * Extends controllers for functionality for {@link AFXUseFilteredList}
 * annotation. It injects a filtered list into annotated control fields.
 *
 * @author koster
 *
 */
public class UseFilteredListControllerExtension extends AbstractAnnotatedFieldControllerExtension<AFXUseFilteredList> {

	public UseFilteredListControllerExtension() {
		super(AFXUseFilteredList.class);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void extend(final Object controller, final Field annotatedElement, final AFXUseFilteredList annotation) {
		final ControlWrapper controlWrapper = ControlWrapper
				.of(getFieldValue(controller, annotatedElement, Control.class));
		final ObservableList list = controlWrapper.getValues();
		final FilteredList filteredList = new FilteredList(list);
		if (annotation.wrapInSortedList()) {
			controlWrapper.setValues(new SortedList(filteredList));
		} else {
			controlWrapper.setValues(filteredList);
		}
	}

}
