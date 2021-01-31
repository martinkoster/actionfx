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
package com.github.actionfx.core.selection;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;

/**
 * Wrapper for an {@link ObservableList} to access the items of this list as a
 * {@link SelectionModel}.
 * <p>
 * This class is used to access user selections in controls via the
 * {@link com.github.actionfx.core.view.graph.ControlWrapper}, in case the
 * selection is made available as plain {@link ObservableList} inside the
 * control (instead of a {@link SelectionModel}).
 *
 * @author koster
 *
 * @param <T> the element type
 */
public class ObservableListSelectionModel<T> extends MultipleSelectionModel<T> {

	private final ObservableList<T> selectedItems;

	private final ObservableList<T> items;

	private final ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();

	public ObservableListSelectionModel(final ObservableList<T> selectedItems, final ObservableList<T> items) {
		this.selectedItems = selectedItems;
		this.items = items;
		setSelectionMode(SelectionMode.MULTIPLE);
		selectedItems.forEach(element -> selectedIndices.add(items.indexOf(element)));
		updateSingleSelection();
		selectedItems.addListener((InvalidationListener) observable -> updateSelectedIndices()); // NOSONAR
	}

	@Override
	public ObservableList<Integer> getSelectedIndices() {
		return selectedIndices;
	}

	@Override
	public ObservableList<T> getSelectedItems() {
		return selectedItems;
	}

	@Override
	public void selectIndices(final int index, final int... indices) {
		T selected = items.get(index);
		selectedItems.add(selected);
		if (indices.length > 0) {
			for (final int i : indices) {
				selected = items.get(i);
				selectedItems.add(selected);
			}
		}
		updateSingleSelection();
	}

	@Override
	public void selectAll() {
		for (final T item : items) {
			if (!selectedItems.contains(item)) {
				selectedItems.add(item);
			}
		}
	}

	@Override
	public void selectFirst() {
		if (!items.isEmpty()) {
			final T first = items.get(0);
			select(first);
		}
	}

	@Override
	public void selectLast() {
		if (!items.isEmpty()) {
			final T last = items.get(items.size() - 1);
			select(last);
		}
	}

	@Override
	public void clearAndSelect(final int index) {
		selectedItems.clear();
		select(index);
	}

	@Override
	public void select(final int index) {
		final T selectedItem = items.get(index);
		select(selectedItem);
	}

	@Override
	public void select(final T obj) {
		if (!selectedItems.contains(obj)) {
			selectedItems.add(obj);
		}
	}

	@Override
	public void clearSelection(final int index) {
		final T selectedItem = items.get(index);
		selectedItems.remove(selectedItem);
	}

	@Override
	public void clearSelection() {
		selectedItems.clear();
		setSelectedItem(null);
		setSelectedIndex(-1);

	}

	@Override
	public boolean isSelected(final int index) {
		return selectedIndices.contains(Integer.valueOf(index));
	}

	@Override
	public boolean isEmpty() {
		return selectedItems.isEmpty();
	}

	@Override
	public void selectPrevious() {
		final int index = getSelectedIndex();
		if (index > 0) {
			select(index - 1);
		}
	}

	@Override
	public void selectNext() {
		final int index = getSelectedIndex();
		if (index < items.size() - 1) {
			select(index + 1);
		}
	}

	/**
	 * Updates the observable list of selected indices, after a change of the
	 * selected items occurs.
	 */
	protected void updateSelectedIndices() {
		final List<Integer> indicesToRetain = new ArrayList<>();
		selectedItems.forEach(element -> indicesToRetain.add(items.indexOf(element)));
		selectedIndices.retainAll(indicesToRetain); // keep only indices that are in the indicesToRetain-list
		// add indices that were not in the selectedIndices list before
		indicesToRetain.forEach(element -> {
			if (!selectedIndices.contains(element)) {
				selectedIndices.add(element);
			}
		});
		updateSingleSelection();
	}

	/**
	 * Updates the single selection item and index in the underlying
	 * {@link SelectionModel}.
	 */
	private void updateSingleSelection() {
		setSelectedItem(selectedItems.isEmpty() ? null : selectedItems.get(selectedItems.size() - 1));
		setSelectedIndex(selectedIndices.isEmpty() ? -1 : selectedIndices.get(selectedIndices.size() - 1));
	}

}
