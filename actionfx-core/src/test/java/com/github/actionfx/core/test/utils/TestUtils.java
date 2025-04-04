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
package com.github.actionfx.core.test.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.beans.Observable;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionModel;

/**
 * Util class for testing ActionFX components.
 *
 * @author koster
 *
 */
public class TestUtils {

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void assertControlHasUserValue(final View view, final String controlId, final Object value) {
		final ControlWrapper controlWrapper = getControlWrapper(view, controlId);
		if (List.class.isAssignableFrom(value.getClass())) {
			assertThat((List) controlWrapper.getUserValue()).contains(((List<?>) value).toArray());
		} else {
			assertThat(controlWrapper.getUserValue()).isEqualTo(value);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void enterValue(final View view, final String controlId, final Object value) {
		final ControlWrapper controlWrapper = getControlWrapper(view, controlId);
		final SelectionModel selectionModel = controlWrapper.getSelectionModel();
		if (selectionModel != null) {
			selectionModel.clearSelection();
			if (List.class.isAssignableFrom(value.getClass())) {
				((List) value).forEach(selectionModel::select);
			} else {
				selectionModel.select(value);
			}
		} else {
			final Observable observable = controlWrapper.getUserValueAsObservable();
			if (WritableValue.class.isAssignableFrom(observable.getClass())) {
				((WritableValue) observable).setValue(value);
			} else if (ObservableList.class.isAssignableFrom(observable.getClass())) {
				if (List.class.isAssignableFrom(value.getClass())) {
					((ObservableList) observable).addAll((List) value);
				} else {
					((ObservableList) observable).add(value);
				}
			} else {
				throw new IllegalStateException(
						"Can not set user value to observable of type '" + observable.getClass() + "'!");
			}
		}
	}

	public static ControlWrapper getControlWrapper(final View view, final String controlId) {
		final NodeWrapper nodeWrapper = view.lookupNode(controlId);
		return ControlWrapper.of(nodeWrapper.getWrapped());
	}

}
