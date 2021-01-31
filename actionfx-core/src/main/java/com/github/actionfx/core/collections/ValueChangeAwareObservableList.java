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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

/**
 * Special implementation of {@link ObservableList} that is aware when values of
 * list elements are changing. In that case, attached {@link ListChangeListener}
 * are notified about that change.
 * <p>
 * In order for this mechanism to work, we can of course not just "add" simple
 * values, but we need to add observable instances of type
 * {@link ObservableValue}.
 * <p>
 * Please note that the actual list itself is immutable, i.e. that it is not
 * possible to add to or remove elements from the list. In case an element is
 * added or removed, an {@link UnsupportedOperationException} will be thrown.
 *
 * @param <E> the value type of the element
 * @author koster
 *
 */
public class ValueChangeAwareObservableList<E> implements ObservableList<E> {

	private final List<ObservableValue<E>> observableValues;

	// all calls from this observable list are delegate to this implementation,
	// because implementations are all in "com.sun." packages (what a crap)
	private final ObservableList<E> delegateObservableList;

	private final ChangeListener<E> valueChangeListener;

	private final List<ListChangeListener<? super E>> listChangeListener = new ArrayList<>();

	private final List<InvalidationListener> invalidationListener = new ArrayList<>();

	public ValueChangeAwareObservableList(final List<ObservableValue<E>> observableValues) {
		this.delegateObservableList = FXCollections.observableArrayList(
				observableValues.stream().map(ObservableValue::getValue).collect(Collectors.toList()));
		this.observableValues = observableValues;
		valueChangeListener = (observable, oldValue, newValue) -> {
			notifyInvalidationListener(observable);
			notifyListChangeListener(observable, oldValue, newValue);
		};
		for (final ObservableValue<E> observableValue : observableValues) {
			observableValue.addListener(valueChangeListener);
		}
	}

	/**
	 * Notifies all added {@link ListChangeListener} about a value change.
	 */
	protected void notifyListChangeListener(final ObservableValue<? extends E> observable, final E oldValue,
			final E newValue) {
		final int index = observableValues.indexOf(observable);
		for (final ListChangeListener<? super E> listener : listChangeListener) {
			listener.onChanged(new SingleValueChange<>(this, index, observable, oldValue, newValue));
		}
	}

	/**
	 * Notifies all added {@link InvalidationListener} about a value change.
	 */
	protected void notifyInvalidationListener(final ObservableValue<? extends E> observable) {
		for (final InvalidationListener listener : invalidationListener) {
			listener.invalidated(observable);
		}
	}

	@Override
	public void forEach(final Consumer<? super E> action) {
		delegateObservableList.forEach(action);
	}

	@Override
	public void addListener(final ListChangeListener<? super E> listener) {
		delegateObservableList.addListener(listener);
		listChangeListener.add(listener);
	}

	@Override
	public void addListener(final InvalidationListener listener) {
		delegateObservableList.addListener(listener);
		invalidationListener.add(listener);
	}

	@Override
	public void removeListener(final ListChangeListener<? super E> listener) {
		delegateObservableList.removeListener(listener);
		listChangeListener.remove(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(final E... elements) {
		throwUnsupportedOperationException();
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean setAll(final E... elements) { // NOSONAR
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public boolean setAll(final Collection<? extends E> col) {
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public void removeListener(final InvalidationListener listener) {
		delegateObservableList.removeListener(listener);
		invalidationListener.remove(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(final E... elements) {
		return delegateObservableList.removeAll(elements);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(final E... elements) { // NOSONAR
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public void remove(final int from, final int to) {
		throwUnsupportedOperationException();
	}

	@Override
	public FilteredList<E> filtered(final Predicate<E> predicate) {
		return delegateObservableList.filtered(predicate);
	}

	@Override
	public SortedList<E> sorted(final Comparator<E> comparator) {
		return delegateObservableList.sorted(comparator);
	}

	@Override
	public SortedList<E> sorted() {
		return delegateObservableList.sorted();
	}

	@Override
	public int size() {
		return delegateObservableList.size();
	}

	@Override
	public boolean isEmpty() {
		return delegateObservableList.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		return delegateObservableList.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return delegateObservableList.iterator();
	}

	@Override
	public Object[] toArray() {
		return delegateObservableList.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return delegateObservableList.toArray(a);
	}

	@Override
	public boolean add(final E e) {
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public boolean remove(final Object o) {
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return delegateObservableList.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public boolean retainAll(final Collection<?> c) { // NOSONAR
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public void replaceAll(final UnaryOperator<E> operator) {
		throwUnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(final IntFunction<T[]> generator) {
		return delegateObservableList.toArray(generator);
	}

	@Override
	public void sort(final Comparator<? super E> c) {
		throwUnsupportedOperationException();
	}

	@Override
	public void clear() {
		throwUnsupportedOperationException();
	}

	@Override
	public boolean equals(final Object o) {
		return delegateObservableList.equals(o);
	}

	@Override
	public int hashCode() {
		return delegateObservableList.hashCode();
	}

	@Override
	public E get(final int index) {
		return delegateObservableList.get(index);
	}

	@Override
	public E set(final int index, final E element) {
		throwUnsupportedOperationException();
		return null;
	}

	@Override
	public void add(final int index, final E element) {
		throwUnsupportedOperationException();
	}

	@Override
	public boolean removeIf(final Predicate<? super E> filter) {
		throwUnsupportedOperationException();
		return false;
	}

	@Override
	public E remove(final int index) {
		throwUnsupportedOperationException();
		return null;
	}

	@Override
	public int indexOf(final Object o) {
		return delegateObservableList.indexOf(o);
	}

	@Override
	public int lastIndexOf(final Object o) {
		return delegateObservableList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return delegateObservableList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		return delegateObservableList.listIterator(index);
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		return delegateObservableList.subList(fromIndex, toIndex);
	}

	@Override
	public Spliterator<E> spliterator() {
		return delegateObservableList.spliterator();
	}

	@Override
	public Stream<E> stream() {
		return delegateObservableList.stream();
	}

	@Override
	public Stream<E> parallelStream() {
		return delegateObservableList.parallelStream();
	}

	protected void throwUnsupportedOperationException() {
		throw new UnsupportedOperationException("The list is immutable!");
	}

	/**
	 * Implementation of {@link Change} that represents a "list change", when values
	 * of the list are updated.
	 *
	 * @author koster
	 *
	 * @param <E>
	 */
	public static class SingleValueChange<E> extends Change<E> {

		private boolean onChange;

		private final int index;

		private final ObservableValue<? extends E> observableValue;

		private final E oldValue;

		private final E newValue;

		/**
		 * Constructor which accepts the full information about a single value change.
		 *
		 * @param list            the list that triggered the list change listener
		 * @param index           the element index that triggered the change
		 * @param observableValue the observable value that has been changed
		 * @param oldValue        the old value of the observable
		 * @param newValue        the new value of the observable
		 */
		public SingleValueChange(final ObservableList<E> list, final int index,
				final ObservableValue<? extends E> observableValue, final E oldValue, final E newValue) {
			super(list);
			this.index = index;
			this.observableValue = observableValue;
			this.oldValue = oldValue;
			this.newValue = newValue;
			this.onChange = false;
		}

		@Override
		public boolean next() {
			if (onChange) {
				return false;
			}
			onChange = true;
			return true;
		}

		@Override
		public void reset() {
			onChange = false;
		}

		@Override
		public int getFrom() {
			return index;
		}

		@Override
		public int getTo() {
			return getFrom() + 1;
		}

		@Override
		public List<E> getRemoved() {
			return Collections.emptyList();
		}

		@Override
		protected int[] getPermutation() {
			return new int[0];
		}

		@Override
		public boolean wasUpdated() {
			return true;
		}

		@Override
		public String toString() {
			return "Value changed: { oldValue: " + oldValue + ", newValue: " + newValue + " }";
		}

		public ObservableValue<? extends E> getObservableValue() { // NOSONAR
			return observableValue;
		}

		public E getOldValue() {
			return oldValue;
		}

		public E getNewValue() {
			return newValue;
		}

	}

	public List<ObservableValue<E>> getObservableValues() {
		return observableValues;
	}

}
