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
package com.github.actionfx.core.view.graph;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;

/**
 * Wrapper class around a custom selection model that is not derived from
 * {@link SelectionModel}.
 * <p>
 * This test class does not provide the "real" functionality of the
 * {@link MultipleSelectionModel} class. We just want to show here that this
 * wrapper is applied, when there is a properties configuration file for the
 * class to wrap.
 *
 * @author koster
 *
 */
public class CustomSelectionModelWrapper extends MultipleSelectionModel<String> {

	private final CustomSelectionModel customSelectionModel;

	public CustomSelectionModelWrapper(final CustomSelectionModel customSelectionModel) {
		this.customSelectionModel = customSelectionModel;
		setSelectionMode(SelectionMode.MULTIPLE);
	}

	@Override
	public ObservableList<Integer> getSelectedIndices() {
		return FXCollections.emptyObservableList();
	}

	@Override
	public ObservableList<String> getSelectedItems() {
		return FXCollections.observableArrayList("Selected Value");
	}

	@Override
	public void selectIndices(final int index, final int... indices) {
	}

	@Override
	public void selectAll() {
	}

	@Override
	public void selectFirst() {
	}

	@Override
	public void selectLast() {
	}

	@Override
	public void clearAndSelect(final int index) {
	}

	@Override
	public void select(final int index) {
	}

	@Override
	public void select(final String obj) {
	}

	@Override
	public void clearSelection(final int index) {
	}

	@Override
	public void clearSelection() {
	}

	@Override
	public boolean isSelected(final int index) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void selectPrevious() {
	}

	@Override
	public void selectNext() {
	}

	public CustomSelectionModel getCustomSelectionModel() {
		return customSelectionModel;
	}

}
