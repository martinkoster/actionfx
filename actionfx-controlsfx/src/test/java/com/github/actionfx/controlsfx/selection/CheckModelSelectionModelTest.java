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
package com.github.actionfx.controlsfx.selection;

import static org.assertj.core.api.Assertions.assertThat;

import org.controlsfx.control.CheckComboBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit test case for {@link CheckModelSelectionModel}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class CheckModelSelectionModelTest {

	private static final String ITEM_1 = "item-1";

	private static final String ITEM_2 = "item-2";

	private static final String ITEM_3 = "item-3";

	private CheckComboBox<String> checkComboBox;

	private CheckModelSelectionModel<String> wrapper;

	@BeforeEach
	void onSetup() {
		checkComboBox = new CheckComboBox<>();
		checkComboBox.getItems().add(ITEM_1);
		checkComboBox.getItems().add(ITEM_2);
		checkComboBox.getItems().add(ITEM_3);
		wrapper = new CheckModelSelectionModel<>(checkComboBox.getCheckModel(), checkComboBox.getItems());
	}

	@Test
	void testSelectIndicesAndSelectedItems_selectSingleItem() {
		// WHEN
		wrapper.selectIndices(0);

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(0);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_1);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_1);
        assertThat(wrapper.getSelectedIndex()).isZero();
	}

	@Test
	void testSelectIndicesAndSelectedItems_selectMultipleItems() {
		// WHEN
		wrapper.selectIndices(1, 2);

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(1, 2);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_2, ITEM_3);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_3);
		assertThat(wrapper.getSelectedIndex()).isEqualTo(2);
	}

	@Test
	void testCheckAndSelectedItems_singleCheck() {
		// WHEN
		checkComboBox.getCheckModel().check(0);

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(0);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_1);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_1);
        assertThat(wrapper.getSelectedIndex()).isZero();
	}

	@Test
	void testCheckAndSelectedItems_multipleCheck() {
		// WHEN
		checkComboBox.getCheckModel().check(1);
		checkComboBox.getCheckModel().check(2);

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(1, 2);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_2, ITEM_3);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_3);
		assertThat(wrapper.getSelectedIndex()).isEqualTo(2);
	}

	@Test
	void testSelectAll() {
		// WHEN
		wrapper.selectAll();

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(0, 1, 2);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_1, ITEM_2, ITEM_3);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_3);
		assertThat(wrapper.getSelectedIndex()).isEqualTo(2);
	}

	@Test
	void testSelectFirst() {
		// WHEN
		wrapper.selectFirst();

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(0);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_1);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_1);
        assertThat(wrapper.getSelectedIndex()).isZero();
	}

	@Test
	void testSelectLast() {
		// WHEN
		wrapper.selectLast();

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(2);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_3);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_3);
		assertThat(wrapper.getSelectedIndex()).isEqualTo(2);
	}

	@Test
	void testClearAndSelect() {
		// GIVEN
		wrapper.selectFirst();
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(0);

		// WHEN
		wrapper.clearAndSelect(1);

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(1);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_2);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_2);
		assertThat(wrapper.getSelectedIndex()).isEqualTo(1);
	}

	@Test
	void testSelectByIndex() {
		// WHEN
		wrapper.select(2);

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(2);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_3);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_3);
		assertThat(wrapper.getSelectedIndex()).isEqualTo(2);
	}

	@Test
	void testSelectByValue() {
		// WHEN
		wrapper.select(ITEM_2);

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(1);
		assertThat(wrapper.getSelectedItems()).containsExactlyInAnyOrder(ITEM_2);
		assertThat(wrapper.getSelectedItem()).isEqualTo(ITEM_2);
		assertThat(wrapper.getSelectedIndex()).isEqualTo(1);
	}

	@Test
	void testClearSelectionByIndex() {
		// GIVEN
		wrapper.selectFirst();
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(0);

		// WHEN
		wrapper.clearSelection(0);

		// THEN
        assertThat(wrapper.getSelectedIndices()).isEmpty();
        assertThat(wrapper.getSelectedItems()).isEmpty();
		assertThat(wrapper.getSelectedItem()).isNull();
		assertThat(wrapper.getSelectedIndex()).isEqualTo(-1);
	}

	@Test
	void testClearSelection() {
		// GIVEN
		wrapper.selectIndices(0, 1);
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(0, 1);

		// WHEN
		wrapper.clearSelection();

		// THEN
        assertThat(wrapper.getSelectedIndices()).isEmpty();
        assertThat(wrapper.getSelectedItems()).isEmpty();
		assertThat(wrapper.getSelectedItem()).isNull();
		assertThat(wrapper.getSelectedIndex()).isEqualTo(-1);
	}

	@Test
	void testIsSelected() {
		// GIVEN
		wrapper.select(2);
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(2);

		// WHEN and THEN
        assertThat(wrapper.isSelected(2)).isTrue();
        assertThat(wrapper.isSelected(0)).isFalse();
        assertThat(wrapper.isSelected(1)).isFalse();
	}

	@Test
	void testSelectionIndexUniqueness() {
		// WHEN
		wrapper.select(2);
		wrapper.select(2); // select again - we don't want the entry to be twice in the selected index list

		// THEN
		assertThat(wrapper.getSelectedIndices()).isNotNull();
		assertThat(wrapper.getSelectedIndices()).hasSize(1);
		assertThat(wrapper.getSelectedIndices()).containsExactlyInAnyOrder(2);
		assertThat(wrapper.getSelectedIndex()).isEqualTo(2);
        assertThat(wrapper.isSelected(2)).isTrue();
	}

	@Test
	void testSelectPrevious() {
		// GIVEN
		wrapper.select(1);

		// WHEN
		wrapper.selectPrevious();

		// THEN
		assertThat(wrapper.getSelectedIndices()).containsExactly(1, 0);
		assertThat(wrapper.getSelectedItems()).containsExactly("item-2", "item-1");
        assertThat(wrapper.getSelectedIndex()).isZero();
		assertThat(wrapper.getSelectedItem()).isEqualTo("item-1");
	}

	@Test
	void testSelectPrevious_noPreviousAvailable() {
		// GIVEN
		wrapper.select(0);

		// WHEN
		wrapper.selectPrevious();

		// THEN
		assertThat(wrapper.getSelectedIndices()).containsExactly(0);
		assertThat(wrapper.getSelectedItems()).containsExactly("item-1");
        assertThat(wrapper.getSelectedIndex()).isZero();
		assertThat(wrapper.getSelectedItem()).isEqualTo("item-1");
	}

	@Test
	void testSelectNext() {
		// GIVEN
		wrapper.select(1);

		// WHEN
		wrapper.selectNext();

		// THEN
		assertThat(wrapper.getSelectedIndices()).containsExactly(1, 2);
		assertThat(wrapper.getSelectedItems()).containsExactly("item-2", "item-3");
		assertThat(wrapper.getSelectedIndex()).isEqualTo(2);
		assertThat(wrapper.getSelectedItem()).isEqualTo("item-3");
	}

	@Test
	void testSelectNext_noNextAvailable() {
		// GIVEN
		wrapper.select(2);

		// WHEN
		wrapper.selectNext();

		// THEN
		assertThat(wrapper.getSelectedIndices()).containsExactly(2);
		assertThat(wrapper.getSelectedItems()).containsExactly("item-3");
		assertThat(wrapper.getSelectedIndex()).isEqualTo(2);
		assertThat(wrapper.getSelectedItem()).isEqualTo("item-3");
	}
}
