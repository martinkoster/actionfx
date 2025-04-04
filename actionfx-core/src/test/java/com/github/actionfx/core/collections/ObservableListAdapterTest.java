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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

/**
 * JUnit test case for {@link ObservableListAdapter}.
 *
 * @author koster
 *
 */
class ObservableListAdapterTest {

	@Test
	void testObservableListAdapter_noModifiableListWrapper() {
		// GIVEN
		final ObservableList<String> list = FXCollections.emptyObservableList();

		// WHEN
		final ObservableListAdapter<String> adapter = new ObservableListAdapter<>(list);

		// THEN
		assertThat(adapter.getWrappedOriginal()).isSameAs(list);
		assertThat(adapter.getModifiableList()).isSameAs(list);
	}

	@Test
	void testObservableListAdapter_filteredList() {
		// GIVEN
		final ObservableList<String> list = FXCollections.emptyObservableList();
		final FilteredList<String> filteredList = new FilteredList<>(list);

		// WHEN
		final ObservableListAdapter<String> adapter = new ObservableListAdapter<>(filteredList);

		// THEN
		assertThat(adapter.getWrappedOriginal()).isSameAs(filteredList);
		assertThat(adapter.getModifiableList()).isSameAs(list);
	}

	@Test
	void testObservableListAdapter_sortedList() {
		// GIVEN
		final ObservableList<String> list = FXCollections.emptyObservableList();
		final SortedList<String> sortedList = new SortedList<>(list);

		// WHEN
		final ObservableListAdapter<String> adapter = new ObservableListAdapter<>(sortedList);

		// THEN
		assertThat(adapter.getWrappedOriginal()).isSameAs(sortedList);
		assertThat(adapter.getModifiableList()).isSameAs(list);
	}

	@Test
	void testObservableListAdapter_filteredListInSortedList() {
		// GIVEN
		final ObservableList<String> list = FXCollections.emptyObservableList();
		final FilteredList<String> filteredList = new FilteredList<>(list);
		final SortedList<String> sortedList = new SortedList<>(filteredList);

		// WHEN
		final ObservableListAdapter<String> adapter = new ObservableListAdapter<>(sortedList);

		// THEN
		assertThat(adapter.getWrappedOriginal()).isSameAs(sortedList);
		assertThat(adapter.getModifiableList()).isSameAs(list);
	}

}
