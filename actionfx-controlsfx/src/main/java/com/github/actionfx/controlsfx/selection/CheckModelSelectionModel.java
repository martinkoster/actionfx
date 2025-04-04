/*
 * Copyright (c) 2020,2021 Martin Koster
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
package com.github.actionfx.controlsfx.selection;

import java.util.List;

import org.controlsfx.control.CheckModel;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;

/**
 * Wrapper for a ControlsFX {@link CheckModel} that allows it to be treated the
 * same way as a JavaFX {@link SelectionModel}. This class allows access to
 * checked values through {@link MultipleSelectionModel#getSelectedItems()}.
 *
 * @author koster
 */
public class CheckModelSelectionModel<T> extends MultipleSelectionModel<T> implements ListChangeListener<T> {

	private final CheckModel<T> checkModel;

	private final ObservableList<T> items;

	private final ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();

	private final ObservableList<T> selectedItems = FXCollections.observableArrayList();

	@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Holding a reference to CheckModel  and ObservableList is fully intended here.")
	public CheckModelSelectionModel(final CheckModel<T> checkModel, final ObservableList<T> items) {
		this.checkModel = checkModel;
		this.items = items;
		this.checkModel.getCheckedItems().addListener(this);
		setSelectionMode(SelectionMode.MULTIPLE);
		final ObservableList<T> checkedItems = checkModel.getCheckedItems();
		this.selectedItems.addAll(checkedItems);
		if (!selectedItems.isEmpty()) {
			setSelectedItem(selectedItems.get(selectedItems.size() - 1));
			setSelectedIndex(checkedItems.size() - 1);
		}
		for (int i = 0; i < checkedItems.size(); i++) {
			this.selectedIndices.add(i);

		}
	}

	@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Returning the original ObservableList is fully intended here.")
	@Override
	public ObservableList<Integer> getSelectedIndices() {
		return this.selectedIndices;
	}

	@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Returning the original ObservableList is fully intended here.")
	@Override
	public ObservableList<T> getSelectedItems() {
		return this.selectedItems;
	}

	@Override
	public void selectIndices(final int index, final int... indices) {
		this.checkModel.check(this.items.get(index));
		if (indices != null) {
			for (final int i : indices) {
				select(this.items.get(i));
			}
		}
	}

	@Override
	public void selectAll() {
		this.checkModel.checkAll();
	}

	@Override
	public void selectFirst() {
		final T item = this.items.get(0);
		select(item);
	}

	@Override
	public void selectLast() {
		final T item = this.items.get(this.items.size() - 1);
		select(item);
	}

	@Override
	public void clearAndSelect(final int index) {
		this.checkModel.clearChecks();
		select(index);
	}

	@Override
	public void select(final int index) {
		final T item = this.items.get(index);
		select(item);
	}

	@Override
	public void select(final T obj) {
		this.checkModel.check(obj);
	}

	@Override
	public void clearSelection(final int index) {
		final T item = this.items.get(index);
		this.checkModel.clearCheck(item);
	}

	@Override
	public void clearSelection() {
		this.checkModel.clearChecks();
	}

	@Override
	public boolean isSelected(final int index) {
		final T item = this.items.get(index);
		return this.checkModel.isChecked(item);
	}

	@Override
	public boolean isEmpty() {
		return this.checkModel.isEmpty();
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
	 * Adds a selection to the <tt>selectedIndices</tt> list, if it does not already
	 * exist.
	 *
	 * @param item the item to add
	 */
	private void addSelectionIfNotExists(final T item) {
		final int index = this.items.indexOf(item);
		if (index != -1 && !this.selectedIndices.contains(index)) {
			this.selectedIndices.add(index);
			setSelectedIndex(index);
		}
		if (!this.selectedItems.contains(item)) {
			this.selectedItems.add(item);
			setSelectedItem(item); // also save selection as "single selection", so that this property contains the
									// last selected item
		}
	}

	/**
	 * Removes selection from the <tt>selectedIndices</tt> list, if it does exist.
	 *
	 * @param item the item to remove
	 */
	private void removeSelectionIfExists(final T item) {
		final int index = this.items.indexOf(item);
		if (index != -1 && this.selectedIndices.contains(index)) {
			this.selectedIndices.remove(Integer.valueOf(index));
			setSelectedIndex(selectedIndices.isEmpty() ? -1 : selectedIndices.get(selectedIndices.size() - 1));
		}
		if (this.selectedItems.contains(item)) {
			this.selectedItems.remove(item);
			if (item.equals(getSelectedItem())) {
				setSelectedItem(selectedItems.isEmpty() ? null : selectedItems.get(selectedItems.size() - 1));
			}
		}
	}

	/**
	 * OnChange handler that receives change events from the <tt>CheckComboBox</tt>
	 * and applies them to the internal selection lists here.
	 */
	@Override
	public void onChanged(final javafx.collections.ListChangeListener.Change<? extends T> c) {
		while (c.next()) {
			if (c.wasRemoved()) {
				final List<? extends T> removedItems = c.getRemoved();
				for (final T item : removedItems) {
					this.removeSelectionIfExists(item);
				}
			}
			if (c.wasAdded()) {
				final List<? extends T> addedItems = c.getAddedSubList();
				for (final T item : addedItems) {
					this.addSelectionIfNotExists(item);
				}
			}
		}
	}
}
