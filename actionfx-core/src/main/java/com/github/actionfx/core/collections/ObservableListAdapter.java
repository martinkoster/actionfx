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
package com.github.actionfx.core.collections;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

/**
 * Adapter class for populating values in an {@link ObservableList}. Considers
 * that lists can be of a type, where the items in the list can not be directly
 * set.
 * <p>
 * For example for populating a {@link FilteredList}, we need to access the
 * attribute via {@link FilteredList#getSource()}. Same is the case for a
 * {@link SortedList}.
 *
 * @author koster
 *
 */
public class ObservableListAdapter<E> {

	// the original list that is wrapped by this adapter
	private final ObservableList<E> wrappedOriginal;

	// the extracted modifiable list - can be also identical with "wrappedOriginal"
	private final ObservableList<E> modifiableList;

	public ObservableListAdapter(final ObservableList<E> list) {
		this.wrappedOriginal = list;
		this.modifiableList = lookupModifiableList(list);
	}

	/**
	 * Extracts the modifiable list from the supplied {@code list}. Considers that
	 * {@link FilteredList} can be stacked in a {@link SortedList}.
	 *
	 * @param list the list to extract the modifiable list from
	 * @return the modifiable list
	 */
	@SuppressWarnings("unchecked")
	private ObservableList<E> lookupModifiableList(final ObservableList<? extends E> list) {
		if (SortedList.class.isAssignableFrom(list.getClass())) {
			final SortedList<? extends E> sortedList = (SortedList<? extends E>) list;
			return lookupModifiableList(sortedList.getSource());
		}
		if (FilteredList.class.isAssignableFrom(list.getClass())) {
			final FilteredList<? extends E> filteredList = (FilteredList<? extends E>) list;
			return lookupModifiableList(filteredList.getSource());
		}
		return (ObservableList<E>) list;
	}

	public ObservableList<E> getWrappedOriginal() {
		return wrappedOriginal;
	}

	public ObservableList<E> getModifiableList() {
		return modifiableList;
	}

}
