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
package com.github.actionfx.core.bind;

import java.util.List;

import com.github.actionfx.core.selection.ObservableListSelectionModel;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionModel;

/**
 * Binding implementation that binds a target {@link ObservableList} to a source
 * {@link List}. In case the source list provided is also an
 * {@link ObservableList}, the binding will be bidirectional. In case the source
 * list is not observable, a unidirectional binding is applied.
 * <p>
 * This binding also supports a {@link SelectionModel}, that can be optionally
 * used to add / remove elements to/from the {@code target} list. This is handy
 * for binding e.g. multi-selection lists from controls like a
 * {@link javafx.scene.control.ListView} that can not be bound directly
 * bidirectionally , as these are read-only observable lists.
 *
 * @param <E> the element type of the source and target list
 * @author koster
 *
 */
public class ObservableListBinding<E> extends AbstractBinding<List<E>, ObservableList<E>> {

	private final SelectionModel<E> selectionModel;

	private SelectionModelAwareListChangeListener listContentBinding;

	private boolean bindBidirectional;

	private boolean bound = false;

	/**
	 * Constructor accepting the binding source and binding target, where the source
	 * can be of type {@link List} and the binding target must be of type
	 * {@link ObservableList}.
	 *
	 * @param source the binding source
	 * @param target the binding target
	 */
	public ObservableListBinding(final List<E> source, final ObservableList<E> target) {
		this(source, target, null);
	}

	/**
	 * Constructor accepting the binding source and binding target, where the source
	 * can be of type {@link List} and the binding target must be of type
	 * {@link ObservableList}.
	 * <p>
	 * As third argument, a {@link SelectionModel} can be provided. This argument is
	 * {@code nullable}. However, if provided, manipulation of the binding target
	 * list is only performed through the supplied {@link SelectionModel}, as it
	 * might be the case that the supplied binding target list does not allow the
	 * user to add elements (this is the case e.g. for the selected items observable
	 * list in a {@link javafx.scene.control.ListView}).
	 *
	 * @param source         the binding source
	 * @param target         the binding target
	 * @param selectionModel an optional selection model for manipulating entries in
	 *                       the binding {@code target}
	 */
	public ObservableListBinding(final List<E> source, final ObservableList<E> target,
			final SelectionModel<E> selectionModel) {
		super(source, target);
		bindBidirectional = sourceIsObservableList();
		this.selectionModel = selectionModel;
		listContentBinding = new SelectionModelAwareListChangeListener();
	}

	@Override
	public void bind() {
		if (bindBidirectional) {
			this.bindBidirectional();
		} else {
			this.bindUnidirectional();
		}
		bound = true;
	}

	@Override
	public void unbind() {
		if (bindBidirectional) {
			this.unbindBidirectional();
		} else {
			this.unbindUnidirectional();
		}
		bound = false;
	}

	@Override
	public BindingType getBindingType() {
		return bindBidirectional ? BindingType.BIDIRECTIONAL : BindingType.UNIDIRECTIONAL;
	}

	@Override
	public boolean isBound() {
		return bound;
	}

	/**
	 * Binds {@code source} and {@code target} bidirectionally.
	 */
	protected void bindBidirectional() {
		if (useSelectionModelForBinding()) {
			transferListElementsViaSelectionModel();
		} else {
			transferListElementsViaAdding();
		}
		((ObservableList<E>) source).addListener(listContentBinding);
		target.addListener(listContentBinding);
	}

	/**
	 * Removes a bidirectional binding between {@code source} and {@code target}.
	 */
	protected void unbindBidirectional() {
		((ObservableList<E>) source).removeListener(listContentBinding);
		target.removeListener(listContentBinding);
	}

	/**
	 * Binds {@code source} and {@code target} unidirectionally.
	 */
	protected void bindUnidirectional() {
		if (useSelectionModelForBinding()) {
			transferListElementsViaSelectionModel();
		} else {
			transferListElementsViaAdding();
		}
		target.addListener(listContentBinding);
	}

	/**
	 * Removes a unidirectional binding between {@code source} and {@code target}.
	 */
	protected void unbindUnidirectional() {
		target.removeListener(listContentBinding);
	}

	private void transferListElementsViaSelectionModel() {
		// in case the target list is a taken from a selection model, the target list
		// itself is read-only and element selection
		// has to be performed via the selection model
		selectionModel.clearSelection();
		if (MultipleSelectionModel.class.isAssignableFrom(selectionModel.getClass())) {
			final MultipleSelectionModel<E> multipleSelectionModel = (MultipleSelectionModel<E>) selectionModel;
			source.stream().forEach(multipleSelectionModel::select);
		}
	}

	private void transferListElementsViaAdding() {
		target.setAll(source);
	}

	/**
	 * Determines, whether a potentially supplied {@link SelectionModel} shall be
	 * taken to manipulate elements in the target list or a regular binding via
	 * change listener shall be applied.
	 *
	 * @return {@code true}, if the selection model shall be used (if supplied and
	 *         not of type {@link ObservableListSelectionModel}), {@code false} for
	 *         regular binding.
	 */
	private boolean useSelectionModelForBinding() {
		return selectionModel != null
				&& !ObservableListSelectionModel.class.isAssignableFrom(selectionModel.getClass());
	}

	/**
	 * Checks, if the supplied {@code source} list is an {@link ObservableList}.
	 *
	 * @return {@code true}, if and only if the source list is an observable list
	 */
	private boolean sourceIsObservableList() {
		return ObservableList.class.isAssignableFrom(source.getClass());
	}

	/**
	 * List change listener implementation that adds/removes elements to a target
	 * list by using a {@link SelectionModel} in case a selection model is present.
	 *
	 * @author koster
	 *
	 */
	private class SelectionModelAwareListChangeListener implements ListChangeListener<E> {

		private boolean updating = false;

		@Override
		public void onChanged(final Change<? extends E> change) {
			if (!updating) {
				try {
					updating = true;
					final List<E> dest = source == change.getList() ? target : source;
					if (dest == target && useSelectionModelForBinding()) {
						// the target list is only updated through the selection model
						onChangeUsingSelectionModel(change);
					} else {
						onChangeUsingListOperation(change, dest);
					}
				} finally {
					updating = false;
				}
			}
		}

		/**
		 * Event callback that is executed, when the destination list can be modified by
		 * using list operations directly.
		 *
		 * @param change the change event
		 * @param dest   the list to modify
		 */
		private void onChangeUsingListOperation(final Change<? extends E> change, final List<E> dest) {
			while (change.next()) {
				if (change.wasPermutated()) {
					performPermutationChange(change, dest);
				} else {
					if (change.wasRemoved()) {
						performRemovalChange(change, dest);
					}
					if (change.wasAdded()) {
						performAddingChange(change, dest);
					}
				}
			}
		}

		private void performPermutationChange(final Change<? extends E> change, final List<E> dest) {
			if (ObservableList.class.isAssignableFrom(dest.getClass())) {
				permutateObservableList(change, (ObservableList<E>) dest);
			} else {
				permutateList(change, dest);
			}
		}

		private void performRemovalChange(final Change<? extends E> change, final List<E> dest) {
			if (ObservableList.class.isAssignableFrom(dest.getClass())) {
				removeFromObservableList(change, (ObservableList<E>) dest);
			} else {
				removeFromList(change, dest);
			}
		}

		private void performAddingChange(final Change<? extends E> change, final List<E> dest) {
			dest.addAll(change.getFrom(), change.getAddedSubList());
		}

		private void removeFromObservableList(final Change<? extends E> change, final ObservableList<E> dest) {
			dest.remove(change.getFrom(), change.getFrom() + change.getRemovedSize());
		}

		private void permutateObservableList(final Change<? extends E> change, final ObservableList<E> dest) {
			dest.remove(change.getFrom(), change.getTo());
			dest.addAll(change.getFrom(), change.getList().subList(change.getFrom(), change.getTo()));
		}

		private void removeFromList(final Change<? extends E> change, final List<E> dest) {
			dest.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
		}

		private void permutateList(final Change<? extends E> change, final List<E> dest) {
			dest.subList(change.getFrom(), change.getTo()).clear();
			dest.addAll(change.getFrom(), change.getList().subList(change.getFrom(), change.getTo()));
		}

		/**
		 * Event callback that is executed, when the target list shall be modified by
		 * using the selection model and not by using list operations directly.
		 *
		 * @param change the change event
		 */
		private void onChangeUsingSelectionModel(final Change<? extends E> change) {
			while (change.next()) {
				if (change.wasRemoved()) {
					for (int i = change.getFrom(); i < change.getFrom() + change.getRemovedSize(); ++i) {
						selectionModel.clearSelection(i);
					}
				}
				if (change.wasAdded()) {
					for (final E item : change.getAddedSubList()) {
						selectionModel.select(item);
					}
				}
			}
		}
	}

}
