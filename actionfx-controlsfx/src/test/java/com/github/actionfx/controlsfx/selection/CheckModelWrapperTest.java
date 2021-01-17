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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import org.controlsfx.control.CheckComboBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit test case for {@link CheckModelWrapper}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class CheckModelWrapperTest {

	private static final String ITEM_1 = "item-1";

	private static final String ITEM_2 = "item-2";

	private static final String ITEM_3 = "item-3";

	private CheckComboBox<String> checkComboBox;

	private CheckModelWrapper<String> wrapper;

	@BeforeEach
	public void onSetup() {
		checkComboBox = new CheckComboBox<>();
		checkComboBox.getItems().add(ITEM_1);
		checkComboBox.getItems().add(ITEM_2);
		checkComboBox.getItems().add(ITEM_3);
		wrapper = new CheckModelWrapper<>(checkComboBox.getCheckModel(), checkComboBox.getItems());
	}

	@Test
	public void testSelectIndicesAndSelectedItems_selectSingleItem() {
		// WHEN
		wrapper.selectIndices(0);

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(0));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_1));
	}

	@Test
	public void testSelectIndicesAndSelectedItems_selectMultipleItems() {
		// WHEN
		wrapper.selectIndices(1, 2);

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(1, 2));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_2, ITEM_3));
	}

	@Test
	public void testCheckAndSelectedItems_singleCheck() {
		// WHEN
		checkComboBox.getCheckModel().check(0);

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(0));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_1));
	}

	@Test
	public void testCheckAndSelectedItems_multipleCheck() {
		// WHEN
		checkComboBox.getCheckModel().check(1);
		checkComboBox.getCheckModel().check(2);

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(1, 2));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_2, ITEM_3));
	}

	@Test
	public void testSelectAll() {
		// WHEN
		wrapper.selectAll();

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(0, 1, 2));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_1, ITEM_2, ITEM_3));
	}

	@Test
	public void testSelectFirst() {
		// WHEN
		wrapper.selectFirst();

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(0));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_1));
	}

	@Test
	public void testSelectLast() {
		// WHEN
		wrapper.selectLast();

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(2));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_3));
	}

	@Test
	public void testClearAndSelect() {
		// GIVEN
		wrapper.selectFirst();
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(0));

		// WHEN
		wrapper.clearAndSelect(1);

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(1));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_2));
	}

	@Test
	public void testSelectByIndex() {
		// WHEN
		wrapper.select(2);

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(2));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_3));
	}

	@Test
	public void testSelectByValue() {
		// WHEN
		wrapper.select(ITEM_2);

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(1));
		assertThat(wrapper.getSelectedItems(), containsInAnyOrder(ITEM_2));
	}

	@Test
	public void testClearSelectionByIndex() {
		// GIVEN
		wrapper.selectFirst();
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(0));

		// WHEN
		wrapper.clearSelection(0);

		// THEN
		assertThat(wrapper.getSelectedIndices(), hasSize(0));
		assertThat(wrapper.getSelectedItems(), hasSize(0));
	}

	@Test
	public void testClearSelection() {
		// GIVEN
		wrapper.selectIndices(0, 1);
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(0, 1));

		// WHEN
		wrapper.clearSelection();

		// THEN
		assertThat(wrapper.getSelectedIndices(), hasSize(0));
		assertThat(wrapper.getSelectedItems(), hasSize(0));
	}

	@Test
	public void testIsSelected() {
		// GIVEN
		wrapper.select(2);
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(2));

		// WHEN and THEN
		assertThat(wrapper.isSelected(2), equalTo(true));
		assertThat(wrapper.isSelected(0), equalTo(false));
		assertThat(wrapper.isSelected(1), equalTo(false));
	}

	@Test
	public void testSelectionIndexUniqueness() {
		// WHEN
		wrapper.select(2);
		wrapper.select(2); // select again - we don't want the entry to be twice in the selected index list

		// THEN
		assertThat(wrapper.getSelectedIndices(), notNullValue());
		assertThat(wrapper.getSelectedIndices(), hasSize(1));
		assertThat(wrapper.getSelectedIndices(), containsInAnyOrder(2));
		assertThat(wrapper.isSelected(2), equalTo(true));
	}

}
