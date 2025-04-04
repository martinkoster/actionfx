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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * JUnit test case for {@link ObservableListSelectionModel}.
 *
 * @author koster
 *
 */
class ObservableListSelectionModelTest {

	@Test
	void testGetSelectedIndices_checkIndicesAfterInitialization() {
		// GIVEN
		final ObservableList<String> selected = obs("hello", "world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");

		// WHEN
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// THEN
		assertThat(model.getSelectedIndices()).containsExactly(0, 1);
		assertThat(model.getSelectedIndex()).isEqualTo(1);
	}

	@Test
	void testGetSelectedIndices_changeSelectedAfterInitialization() {
		// GIVEN
		final ObservableList<String> selected = obs("hello", "world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		selected.add("are");

		// THEN
		assertThat(model.getSelectedIndices()).containsExactly(0, 1, 3);
		assertThat(model.getSelectedIndex()).isEqualTo(3);
	}

	@Test
	void testGetSelectedItems_checkItemsAfterInitialization() {
		// GIVEN
		final ObservableList<String> selected = obs("hello", "world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");

		// WHEN
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("hello", "world");
		assertThat(model.getSelectedItem()).isEqualTo("world");
	}

	@Test
	void testGetSelectedItems_changeSelectedAfterInitialization() {
		// GIVEN
		final ObservableList<String> selected = obs("hello", "world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		selected.add("are");

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("hello", "world", "are");
		assertThat(model.getSelectedItem()).isEqualTo("are");
	}

	@Test
	void testSelectIndices() {
		// GIVEN
		final ObservableList<String> selected = obs();
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.selectIndices(0, 1, 4);

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("hello", "world", "you");
		assertThat(model.getSelectedItem()).isEqualTo("you");
		assertThat(model.getSelectedIndices()).containsExactly(0, 1, 4);
		assertThat(model.getSelectedIndex()).isEqualTo(4);
	}

	@Test
	void testSelectAll() {
		// GIVEN
		final ObservableList<String> selected = obs();
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.selectAll();

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("hello", "world", "how", "are", "you");
		assertThat(model.getSelectedItem()).isEqualTo("you");
		assertThat(model.getSelectedIndices()).containsExactly(0, 1, 2, 3, 4);
		assertThat(model.getSelectedIndex()).isEqualTo(4);
	}

	@Test
	void testSelectAll_selectionContainsValuesAlready() {
		// GIVEN
		final ObservableList<String> selected = obs();
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);
		selected.add("are");

		// WHEN
		model.selectAll();

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("are", "hello", "world", "how", "you");
		assertThat(model.getSelectedItem()).isEqualTo("you");
		assertThat(model.getSelectedIndices()).containsExactly(3, 0, 1, 2, 4);
		assertThat(model.getSelectedIndex()).isEqualTo(4);
	}

	@Test
	void testSelectFirst() {
		// GIVEN
		final ObservableList<String> selected = obs();
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.selectFirst();

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("hello");
		assertThat(model.getSelectedItem()).isEqualTo("hello");
		assertThat(model.getSelectedIndices()).containsExactly(0);
		assertThat(model.getSelectedIndex()).isEqualTo(0);
	}

	@Test
	void testSelectLast() {
		// GIVEN
		final ObservableList<String> selected = obs();
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.selectLast();

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("you");
		assertThat(model.getSelectedItem()).isEqualTo("you");
		assertThat(model.getSelectedIndices()).containsExactly(4);
		assertThat(model.getSelectedIndex()).isEqualTo(4);
	}

	@Test
	void testClearAndSelect() {
		// GIVEN
		final ObservableList<String> selected = obs("hello", "world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.clearAndSelect(2);

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("how");
		assertThat(model.getSelectedItem()).isEqualTo("how");
		assertThat(model.getSelectedIndices()).containsExactly(2);
		assertThat(model.getSelectedIndex()).isEqualTo(2);
	}

	@Test
	void testSelect_withIndex() {
		// GIVEN
		final ObservableList<String> selected = obs();
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.select(1);

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("world");
		assertThat(model.getSelectedItem()).isEqualTo("world");
		assertThat(model.getSelectedIndices()).containsExactly(1);
		assertThat(model.getSelectedIndex()).isEqualTo(1);
	}

	@Test
	void testSelect_withElement() {
		// GIVEN
		final ObservableList<String> selected = obs();
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.select("world");

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("world");
		assertThat(model.getSelectedItem()).isEqualTo("world");
		assertThat(model.getSelectedIndices()).containsExactly(1);
		assertThat(model.getSelectedIndex()).isEqualTo(1);
	}

	@Test
	void testClearSelection_withIndex() {
		// GIVEN
		final ObservableList<String> selected = obs("hello");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.clearSelection(0);

		// THEN
		assertThat(model.getSelectedItems()).hasSize(0);
		assertThat(model.getSelectedItem()).isNull();
		assertThat(model.getSelectedIndices()).hasSize(0);
		assertThat(model.getSelectedIndex()).isEqualTo(-1);
	}

	@Test
	void testClearSelection_withIndex_indexIsNotSelected() {
		// GIVEN
		final ObservableList<String> selected = obs("hello");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.clearSelection(1);

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("hello");
		assertThat(model.getSelectedItem()).isEqualTo("hello");
		assertThat(model.getSelectedIndices()).containsExactly(0);
		assertThat(model.getSelectedIndex()).isEqualTo(0);
	}

	@Test
	void clearSelection() {
		// GIVEN
		final ObservableList<String> selected = obs("hello", "world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.clearSelection();

		// THEN
		assertThat(model.getSelectedItems()).hasSize(0);
		assertThat(model.getSelectedItem()).isNull();
		assertThat(model.getSelectedIndices()).hasSize(0);
		assertThat(model.getSelectedIndex()).isEqualTo(-1);
	}

	@Test
	void isSelected_withIndex() {
		// GIVEN
		final ObservableList<String> selected = obs("hello", "world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN and THEN
		assertThat(model.isSelected(0)).isEqualTo(true);
		assertThat(model.isSelected(1)).isEqualTo(true);
		assertThat(model.isSelected(2)).isEqualTo(false);
		assertThat(model.isSelected(3)).isEqualTo(false);
		assertThat(model.isSelected(4)).isEqualTo(false);
	}

	@Test
	void testIsEmpty_selectionIsNotEmpty() {
		// GIVEN
		final ObservableList<String> selected = obs("hello", "world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN and THEN
		assertThat(model.isEmpty()).isEqualTo(false);
	}

	@Test
	void testIsEmpty_selectionIsEmpty() {
		// GIVEN
		final ObservableList<String> selected = obs();
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN and THEN
		assertThat(model.isEmpty()).isEqualTo(true);
	}

	@Test
	void testSelectPrevious() {
		// GIVEN
		final ObservableList<String> selected = obs("world");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.selectPrevious();

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("world", "hello");
		assertThat(model.getSelectedItem()).isEqualTo("hello");
		assertThat(model.getSelectedIndices()).containsExactly(1, 0);
		assertThat(model.getSelectedIndex()).isEqualTo(0);

	}

	@Test
	void testSelectPrevious_noPreviousAvailable() {
		// GIVEN
		final ObservableList<String> selected = obs("hello");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.selectPrevious();

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("hello");
		assertThat(model.getSelectedItem()).isEqualTo("hello");
		assertThat(model.getSelectedIndices()).containsExactly(0);
		assertThat(model.getSelectedIndex()).isEqualTo(0);

	}

	@Test
	void testSelectNext() {
		// GIVEN
		final ObservableList<String> selected = obs("hello");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.selectNext();

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("hello", "world");
		assertThat(model.getSelectedItem()).isEqualTo("world");
		assertThat(model.getSelectedIndices()).containsExactly(0, 1);
		assertThat(model.getSelectedIndex()).isEqualTo(1);
	}

	@Test
	void testSelectNext_noNextAvailable() {
		// GIVEN
		final ObservableList<String> selected = obs("you");
		final ObservableList<String> items = obs("hello", "world", "how", "are", "you");
		final ObservableListSelectionModel<String> model = selectionModel(selected, items);

		// WHEN
		model.selectNext();

		// THEN
		assertThat(model.getSelectedItems()).containsExactly("you");
		assertThat(model.getSelectedItem()).isEqualTo("you");
		assertThat(model.getSelectedIndices()).containsExactly(4);
		assertThat(model.getSelectedIndex()).isEqualTo(4);
	}

	static ObservableListSelectionModel<String> selectionModel(final ObservableList<String> selected,
			final ObservableList<String> items) {
		return new ObservableListSelectionModel<>(selected, items);
	}

	static ObservableList<String> obs(final String... items) {
		return FXCollections.observableArrayList(items);
	}
}
