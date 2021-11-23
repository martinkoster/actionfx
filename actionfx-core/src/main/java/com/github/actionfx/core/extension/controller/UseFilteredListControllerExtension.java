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
import java.util.function.Predicate;

import com.github.actionfx.core.annotation.AFXUseFilteredList;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.value.ObservableValue;
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
		final ObservableList list = controlWrapper.getItems();
		final FilteredList filteredList = new FilteredList(list);
		if (!"".equals(annotation.filterPredicateProperty())) {
			bindFilteredListPredicate(controller, annotatedElement, annotation, filteredList);
		}
		if (annotation.wrapInSortedList()) {
			controlWrapper.setItems(new SortedList(filteredList));
		} else {
			controlWrapper.setItems(filteredList);
		}
	}

	/**
	 * Binds the predicate of the filtered list to the observable value specified as
	 * part of the {@link AFXUseFilteredList} annotation.
	 *
	 * @param controller       the controller
	 * @param annotatedElement the annotated field
	 * @param annotation       the annotation
	 * @param filteredList     the filtered list
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void bindFilteredListPredicate(final Object controller, final Field annotatedElement,
			final AFXUseFilteredList annotation, final FilteredList filteredList) {
		final ObservableValue<?> observableValue = lookupObservableValue(controller,
				annotation.filterPredicateProperty(), ObservableValue.class);
		if (observableValue == null) {
			throw new IllegalStateException(
					"Attribute 'filterPredicateProperty' in @AFXUseFilteredList annotation on field '"
							+ annotatedElement.getName() + "' in class '" + controller.getClass().getCanonicalName()
							+ "' resolved to null!");
		}
		final Object propertyValue = observableValue.getValue();
		if (propertyValue == null) {
			throw new IllegalStateException(
					"Attribute 'filterPredicateProperty' in @AFXUseFilteredList annotation on field '"
							+ annotatedElement.getName() + "' in class '" + controller.getClass().getCanonicalName()
							+ "' resolved to an ObservableValue, but its value is null!");

		}
		if (!Predicate.class.isAssignableFrom(propertyValue.getClass())) {
			throw new IllegalStateException(
					"Attribute 'filterPredicateProperty' in @AFXUseFilteredList annotation on field '"
							+ annotatedElement.getName() + "' in class '" + controller.getClass().getCanonicalName()
							+ "' resolved to an ObservableValue, but its value is not a java.util.function.Predicate!");
		}
		filteredList.predicateProperty().bind(observableValue);
	}

}
