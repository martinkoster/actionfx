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
package com.github.actionfx.core.extension.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.annotation.AFXUseFilteredList;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
		final ControllerWithTableView controller = new ControllerWithTableView();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.tableView.getItems()).isInstanceOf(FilteredList.class);
	}

	@Test
	void testAccept_sortedAndFilteredList() {
		// GIVEN
		final ControllerWithTableViewAndWrapedInSortedList controller = new ControllerWithTableViewAndWrapedInSortedList();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.tableView.getItems()).isInstanceOf(SortedList.class);
		final SortedList<String> sortedList = (SortedList<String>) controller.tableView.getItems();
		assertThat(sortedList.getSource()).isInstanceOf(FilteredList.class);
	}

	@Test
	void testAccept_filteredList_withFilterPredicate() {
		// GIVEN
		final ControllerWithFilterPredicate controller = new ControllerWithFilterPredicate();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.tableView.getItems()).isInstanceOf(FilteredList.class);
		final FilteredList<String> filteredList = (FilteredList<String>) controller.tableView.getItems();
		assertThat(filteredList.getPredicate()).isSameAs(controller.filterPredicateProperty.get());
	}

	@Test
	void testAccept_filteredList_filterPredicateProperty_isNotOfExpectedType() {
		// GIVEN
		final ControllerWithFilterPrecateOfUnexpectedType controller = new ControllerWithFilterPrecateOfUnexpectedType();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).contains("is not of expected type");
	}

	@Test
	void testAccept_filteredList_filterPredicateProperty_observableIsNull() {
		// GIVEN
		final ControllerWithNullObservable controller = new ControllerWithNullObservable();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).contains("resolved to null!");
	}

	@Test
	void testAccept_filteredList_filterPredicateProperty_predicateIsNull() {
		// GIVEN
		final ControllerWithNullPredicate controller = new ControllerWithNullPredicate();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).contains("resolved to an ObservableValue, but its value is null!");
	}

	@Test
	void testAccept_filteredList_filterPredicateProperty_observableHoldsNoPredicate() {
		// GIVEN
		final ControllerWithPropertyHoldsNoPredicate controller = new ControllerWithPropertyHoldsNoPredicate();
		final UseFilteredListControllerExtension extension = new UseFilteredListControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).contains("resolved to an ObservableValue, but its value is not a java.util.function.Predicate!");
	}

	public class ControllerWithTableView {

		public View _view;

		@AFXUseFilteredList
		protected TableView<String> tableView = new TableView<>();
	}

	public class ControllerWithTableViewAndWrapedInSortedList {

		public View _view;

		@AFXUseFilteredList(wrapInSortedList = true)
		protected TableView<String> tableView = new TableView<>();
	}

	public class ControllerWithFilterPredicate {

		public View _view;

		private final ObjectProperty<Predicate<String>> filterPredicateProperty = new SimpleObjectProperty<>();

		@AFXUseFilteredList(filterPredicateProperty = "filterPredicateProperty")
		protected TableView<String> tableView = new TableView<>();

		public ControllerWithFilterPredicate() {
			final Predicate<String> predicate = value -> true;
			filterPredicateProperty.set(predicate);
		}
	}

	public class ControllerWithFilterPrecateOfUnexpectedType {

		public View _view;

		public final String filterPredicateProperty = "not_an_observable_value";

		@AFXUseFilteredList(filterPredicateProperty = "filterPredicateProperty")
		protected TableView<String> tableView = new TableView<>();
	}

	public class ControllerWithNullObservable {

		public View _view;

		public final ObjectProperty<Predicate<String>> filterPredicateProperty = null;

		@AFXUseFilteredList(filterPredicateProperty = "filterPredicateProperty")
		protected TableView<String> tableView = new TableView<>();
	}

	public class ControllerWithNullPredicate {

		public View _view;

		public final ObjectProperty<Predicate<String>> filterPredicateProperty = new SimpleObjectProperty<>(null);

		@AFXUseFilteredList(filterPredicateProperty = "filterPredicateProperty")
		protected TableView<String> tableView = new TableView<>();
	}

	public class ControllerWithPropertyHoldsNoPredicate {

		public View _view;

		public final ObjectProperty<String> filterPredicateProperty = new SimpleObjectProperty<>("not_a_predicate");

		@AFXUseFilteredList(filterPredicateProperty = "filterPredicateProperty")
		protected TableView<String> tableView = new TableView<>();
	}
}
