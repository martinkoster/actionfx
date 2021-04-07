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
package com.github.actionfx.core.container.extension;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.annotation.AFXUseFilteredList;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;

/**
 * JUnit test case for {@link UseFilteredListControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class UseFilteredListControllerExtensionTest {

	@Test
	void testAccept_filteredList() {
		// GIVEN
		final ControllerFilteredListTable controller = new ControllerFilteredListTable();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.tableView.getItems(), instanceOf(FilteredList.class));
	}

	@Test
	void testAccept_sortedAndFilteredList() {
		// GIVEN
		final ControllerSortedAndFilteredListTable controller = new ControllerSortedAndFilteredListTable();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.tableView.getItems(), instanceOf(SortedList.class));
		final SortedList<String> sortedList = (SortedList<String>) controller.tableView.getItems();
		assertThat(sortedList.getSource(), instanceOf(FilteredList.class));
	}

	public class ControllerFilteredListTable {

		public View _view;

		@AFXUseFilteredList
		protected TableView<String> tableView = new TableView<>();
	}

	public class ControllerSortedAndFilteredListTable {

		public View _view;

		@AFXUseFilteredList(wrapInSortedList = true)
		protected TableView<String> tableView = new TableView<>();
	}

}
