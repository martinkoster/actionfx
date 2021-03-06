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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.actionfx.core.collections.ValueChangeAwareObservableList.SingleValueChange;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

/**
 * JUnit test case for {@link ValueChangeAwareObservableList}.
 *
 * @author koster
 *
 */
class ValueChangeAwareObservableListTest {

	private ValueChangeAwareObservableList<String> list;

	private final StringProperty propertyOne = new SimpleStringProperty("one");

	private final StringProperty propertyTwo = new SimpleStringProperty("two");

	private final StringProperty propertyThree = new SimpleStringProperty("three");

	@BeforeEach
	void setup() {
		list = new ValueChangeAwareObservableList<>(Arrays.asList(propertyOne, propertyTwo, propertyThree));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testAddListenerAndValueChange_listChangeListenerIsFired() {
		// GIVEN
		final ObjectProperty<Change<String>> changeProperty = new SimpleObjectProperty<>(null);
		final ListChangeListener<String> listener = change -> {
			changeProperty.set((Change<String>) change);
		};
		list.addListener(listener);

		// WHEN
		propertyThree.set("hello");

		// THEN
		assertChange(changeProperty.get(), 2, propertyThree, "three", "hello");
	}

	@Test
	void testAddListenerValueChange_invalidationListenerIsFired() {
		// GIVEN
		final ObjectProperty<Observable> valueProperty = new SimpleObjectProperty<>(null);
		final InvalidationListener listener = observable -> {
			valueProperty.set(observable);
		};
		list.addListener(listener);

		// WHEN
		propertyTwo.set("hello");

		// THEN
		assertThat(valueProperty.get(), notNullValue());
		assertThat(valueProperty.get(), sameInstance(propertyTwo));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testRemoveListenerAndValueChange_listChangeListenerIsFired() {
		// GIVEN
		final ObjectProperty<Change<String>> changeProperty = new SimpleObjectProperty<>(null);
		final ListChangeListener<String> listener = change -> {
			changeProperty.set((Change<String>) change);
		};
		list.addListener(listener);
		list.removeListener(listener);

		// WHEN
		propertyThree.set("hello");

		// THEN (listener not fired, it has been removed again)
		assertThat(changeProperty.get(), nullValue());
	}

	@Test
	void testRemoveListenerValueChange_invalidationListenerIsFired() {
		// GIVEN
		final ObjectProperty<Observable> valueProperty = new SimpleObjectProperty<>(null);
		final InvalidationListener listener = observable -> {
			valueProperty.set(observable);
		};
		list.addListener(listener);
		list.removeListener(listener);

		// WHEN
		propertyTwo.set("hello");

		// THEN (listener not fired, it has been removed again)
		assertThat(valueProperty.get(), nullValue());
	}

	private void assertChange(final Change<String> change, final int expIndex,
			final ObservableValue<String> expObservable, final String oldValue, final String newValue) {
		assertThat(change, notNullValue());
		assertThat(change, instanceOf(SingleValueChange.class));
		final SingleValueChange<String> svc = (SingleValueChange<String>) change;
		if (svc.next()) {
			assertThat(svc.getObservableValue(), sameInstance(expObservable));
			assertThat(svc.getOldValue(), equalTo(oldValue));
			assertThat(svc.getNewValue(), equalTo(newValue));
			assertThat(svc.getFrom(), equalTo(expIndex));
			assertThat(svc.getTo(), equalTo(expIndex + 1));
			assertThat(svc.wasUpdated(), equalTo(true));
			assertThat(svc.wasAdded(), equalTo(false));
			assertThat(svc.wasRemoved(), equalTo(false));
		} else {
			fail("SingleValueChange did not contain any change!");
		}
	}

	@Test
	void testAddAll() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.addAll("hello", "world"));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testSetAll() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.setAll("hello", "world"));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testSetAll_withCollection() {
		// GIVEN
		final List<String> collection = Arrays.asList("hello", "world");

		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.setAll(collection));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testRetainAll() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.retainAll("hello", "world"));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testRemove_fromTo() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.remove(0, 2));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testFiltered() {
		// WHEN
		final FilteredList<String> result = list.filtered(str -> str.equals("one"));

		// THEN
		assertThat(result, hasSize(1));
		assertThat(result.get(0), equalTo("one"));
	}

	@Test
	void testSorted_withComparator() {
		// WHEN
		final SortedList<String> result = list.sorted((str1, str2) -> str2.compareTo(str1));

		// THEN
		assertThat(result, contains("two", "three", "one"));
	}

	@Test
	void testSorted() {
		// WHEN
		final SortedList<String> result = list.sorted();

		// THEN
		assertThat(result, contains("one", "three", "two"));
	}

	@Test
	void testsize() {
		// WHEN and THEN
		assertThat(list.size(), equalTo(3));
	}

	@Test
	void testIsEmpty() {
		// WHEN and THEN
		assertThat(list.isEmpty(), equalTo(false));
		assertThat(new ValueChangeAwareObservableList<>(Collections.emptyList()).isEmpty(), equalTo(true));
	}

	@Test
	void testContains() {
		// WHEN and THEN
		assertThat(list.contains("two"), equalTo(true));
		assertThat(list.contains("four"), equalTo(false));
	}

	@Test
	void testIterator() {
		// WHEN
		final Iterator<String> it = list.iterator();

		// THEN
		assertThat(it, notNullValue());
		assertThat(it.next(), equalTo("one"));
		assertThat(it.next(), equalTo("two"));
		assertThat(it.next(), equalTo("three"));
	}

	@Test
	void testToArray() {
		// WHEN
		final Object[] arr = list.toArray();

		// THEN
		assertThat(arr.length, equalTo(3));
		assertThat(arr[0], equalTo("one"));
		assertThat(arr[1], equalTo("two"));
		assertThat(arr[2], equalTo("three"));
	}

	@Test
	void testToArray_withArrayArg() {
		// WHEN
		final String[] arr = list.toArray(new String[3]);

		// THEN
		assertThat(arr.length, equalTo(3));
		assertThat(arr[0], equalTo("one"));
		assertThat(arr[1], equalTo("two"));
		assertThat(arr[2], equalTo("three"));
	}

	@Test
	void testAdd() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.add("hello"));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testRemove() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.remove("hello"));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void containsAll() {
		// WHEN
		assertThat(list.containsAll(Arrays.asList("one", "two", "three")), equalTo(true));
		assertThat(list.containsAll(Arrays.asList("one", "four")), equalTo(false));
	}

	@Test
	void testAddAll_withCollection() {
		// GIVEN
		final List<String> collection = Arrays.asList("hello", "world");

		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.addAll(collection));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testAddAll_withCollection_andIndex() {
		// GIVEN
		final List<String> collection = Arrays.asList("hello", "world");

		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.addAll(0, collection));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testRemoveAll_withCollection() {
		// GIVEN
		final List<String> collection = Arrays.asList("hello", "world");

		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.removeAll(collection));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testRetainAll_withCollection() {
		// GIVEN
		final List<String> collection = Arrays.asList("hello", "world");

		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.retainAll(collection));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));

	}

	@Test
	void testReplaceAll() {
		// GIVEN
		final UnaryOperator<String> unaryOperator = UnaryOperator.identity();

		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.replaceAll(unaryOperator));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testToArray_withIntFunction() {
		// WHEN
		final Object[] arr = list.toArray(num -> new String[3]);

		// THEN
		assertThat(arr.length, equalTo(3));
		assertThat(arr[0], equalTo("one"));
		assertThat(arr[1], equalTo("two"));
		assertThat(arr[2], equalTo("three"));
	}

	@Test
	void sort_withComparator() {
		// WHEN
		final Comparator<String> comparator = String::compareTo;
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.sort(comparator));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testClear() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> list.clear());

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testEquals() {
		// WHEN and THEN
		assertThat(list.equals(Arrays.asList("one", "two", "three")), equalTo(true));
		assertThat(list.equals(Arrays.asList("one", "two")), equalTo(false));
	}

	@Test
	void testHashCode() {
		// WHEN and THEN
		assertThat(list.hashCode(), equalTo(Arrays.asList("one", "two", "three").hashCode()));
		assertThat(list.hashCode(), not(equalTo(Arrays.asList("one", "two").hashCode())));
	}

	@Test
	void testGet() {
		// WHEN and THEN
		assertThat(list.get(0), equalTo("one"));
		assertThat(list.get(1), equalTo("two"));
		assertThat(list.get(2), equalTo("three"));
	}

	@Test
	void testSet() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.set(0, "hello"));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testAdd_withIndex() {
		// WHEN
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.add(0, "hello"));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testRemoveIf() {
		// WHEN
		final Predicate<String> predicate = value -> true;
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.removeIf(predicate));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testRemove_withIndex() {
		final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> list.remove(0));

		// THEN
		assertThat(ex.getMessage(), equalTo("The list is immutable!"));
	}

	@Test
	void testIndexOf() {
		// WHEN and THEN
		assertThat(list.indexOf("two"), equalTo(1));
		assertThat(list.indexOf("four"), equalTo(-1));
	}

	@Test
	void testLastIndexOf() {
		// WHEN and THEN
		assertThat(list.lastIndexOf("two"), equalTo(1));
		assertThat(list.lastIndexOf("four"), equalTo(-1));
	}

	@Test
	void listIterator() {
		// WHEN
		final ListIterator<String> it = list.listIterator();

		// THEN
		assertThat(it, notNullValue());
		assertThat(it.next(), equalTo("one"));
		assertThat(it.next(), equalTo("two"));
		assertThat(it.next(), equalTo("three"));
	}

	@Test
	void listIterator_withIndex() {
		// WHEN
		final ListIterator<String> it = list.listIterator(1);

		// THEN
		assertThat(it, notNullValue());
		assertThat(it.next(), equalTo("two"));
		assertThat(it.next(), equalTo("three"));
	}

	@Test
	void testSubList() {
		assertThat(list.subList(1, 3), contains("two", "three"));
	}

	@Test
	void testSpliterator() {
		assertThat(list.spliterator(), notNullValue());
	}

	@Test
	void stream() {
		assertThat(list.stream(), notNullValue());
	}

	@Test
	void parallelStream() {
		assertThat(list.parallelStream(), notNullValue());
	}

}
