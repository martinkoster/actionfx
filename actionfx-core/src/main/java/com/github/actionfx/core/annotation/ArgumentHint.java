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
package com.github.actionfx.core.annotation;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener.Change;

/**
 * A hint for a method parameter that can help in case two different method
 * parameters have the same type.
 *
 * @author koster
 *
 */
public enum ArgumentHint {
	/**
	 * The "old" value of a single-value property (see
	 * {@link ChangeListener#changed(javafx.beans.value.ObservableValue, Object, Object)}).
	 */
	OLD_VALUE,

	/**
	 * The "new" value of a single-value property (see
	 * {@link ChangeListener#changed(javafx.beans.value.ObservableValue, Object, Object)}).
	 */
	NEW_VALUE,

	/**
	 * Method arguments are matched by type.
	 */
	TYPE_BASED,

	/**
	 * A list of added values for list changes (see
	 * {@link Change#getAddedSubList()}).
	 */
	ADDED_VALUES,

	/**
	 * A list of removed values for list changes (see {@link Change#getRemoved()}.
	 */
	REMOVED_VALUES,

	/**
	 * A list of all selected values.
	 */
	ALL_SELECTED
}