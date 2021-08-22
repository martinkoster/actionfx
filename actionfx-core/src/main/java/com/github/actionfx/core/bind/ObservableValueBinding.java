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

import com.github.actionfx.core.beans.BeanPropertyReference;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.scene.control.SelectionModel;

/**
 * Binding implementation that binds an {@link ObservableValue} to another
 * {@link BeanPropertyReference}. In case the bean property reference provided
 * is a {@link Property}, the binding will be bidirectional. In case the object
 * contains an {@link ObservableValue} a unidirectional binding is applied. If
 * the object is neither a {@link Property} nor an {@link ObservableValue}, the
 * object is assumed to be the value to be set directly into the target
 * observable value this binding is intended for.
 * <p>
 * This binding also supports a {@link SelectionModel}, that can be optionally
 * used to set a value to {@code target}. This is handy for binding e.g.
 * read-only properties from controls like a
 * {@link javafx.scene.control.ChoiceBox} that can not be bound directly
 * bidirectionally.
 *
 * @param <E> the type contained inside the {@link Property}.
 * @author koster
 *
 */
public class ObservableValueBinding<E> extends AbstractBinding<BeanPropertyReference<E>, ObservableValue<E>> {

	/**
	 * Describes the internal binding type that will be established.
	 *
	 * @author koster
	 *
	 */
	private enum BindingType {
		BIDRECTIONAL, UNIDIRECTIONAL, VALUEBASED
	}

	private final BindingType bindingType;

	private SelectionModel<E> selectionModel;

	private final ChangeListener<E> contentBinding;

	/**
	 * Constructor accepting a binding source and binding target, while the binding
	 * target must be an {@link Property}.
	 *
	 * @param source the binding source
	 * @param target the binding target
	 */
	public ObservableValueBinding(final BeanPropertyReference<E> source, final ObservableValue<E> target) {
		this(source, target, null);
	}

	/**
	 * Constructor accepting a binding source and binding target, while the binding
	 * target must be an {@link Property}.
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
	 * @param selectionModel an optional selection model for manipulating the value
	 *                       in the binding {@code target}
	 */
	public ObservableValueBinding(final BeanPropertyReference<E> source, final ObservableValue<E> target,
			final SelectionModel<E> selectionModel) {
		super(source, target);
		this.selectionModel = selectionModel;
		bindingType = selectBindingType(source);
		contentBinding = new SelectionModelAwareChangeListener();
	}

	@Override
	public void bind() {
		switch (bindingType) {
		case BIDRECTIONAL:
			bindBidirectional();
			break;
		case UNIDIRECTIONAL:
			bindUnidirectional();
			break;
		case VALUEBASED:
		default:
			bindValue();
			break;
		}
	}

	@Override
	public void unbind() {
		switch (bindingType) {
		case BIDRECTIONAL:
			unbindBidirectional();
			break;
		case UNIDIRECTIONAL:
			unbindUnidirectional();
			break;
		case VALUEBASED:
		default:
			unbindValue();
			break;
		}
	}

	/**
	 * Binds {@code source} and {@code target} bidirectionally.
	 */
	protected void bindBidirectional() {
		if (useSelectionModelForBinding()) {
			transferValueViaSelectionModel();
		} else {
			transferValueViaSetting();
		}
		source.getFxProperty().addListener(contentBinding);
		target.addListener(contentBinding);
	}

	/**
	 * Removes a bidirectional binding between {@code source} and {@code target}.
	 */
	protected void unbindBidirectional() {
		source.getFxProperty().removeListener(contentBinding);
		target.removeListener(contentBinding);
	}

	/**
	 * Binds {@code source} and {@code target} unidirectionally.
	 */
	protected void bindUnidirectional() {
		if (useSelectionModelForBinding()) {
			transferValueViaSelectionModel();
		} else {
			transferValueViaSetting();
		}
		target.addListener(contentBinding);
	}

	/**
	 * Removes a unidirectional binding between {@code source} and {@code target}.
	 */
	protected void unbindUnidirectional() {
		target.removeListener(contentBinding);
	}

	/**
	 * Binds {@code source} and {@code target} by setting the value of source into
	 * target.
	 */
	protected void bindValue() {
		this.bindUnidirectional();
	}

	/**
	 * Unbinds {@code source} and {@code target} after a value-based binding.
	 */
	protected void unbindValue() {
		this.unbindUnidirectional();
	}

	private void transferValueViaSelectionModel() {
		selectionModel.select(source.getValue());
	}

	@SuppressWarnings("unchecked")
	private void transferValueViaSetting() {
		if (WritableValue.class.isAssignableFrom(target.getClass())) {
			((WritableValue<E>) target).setValue(source.getValue());
		}
	}

	/**
	 * Determines, whether a potentially supplied {@link SelectionModel} shall be
	 * taken to manipulate elements in the target list or a regular binding via
	 * change listener shall be applied.
	 *
	 * @return {@code true}, if the selection model shall be used (if supplied),
	 *         {@code false} for regular binding.
	 */
	protected boolean useSelectionModelForBinding() {
		return selectionModel != null && ObservableValue.class.isAssignableFrom(target.getClass());
	}

	/**
	 * Determines the best binding type for the given {@code source}.
	 *
	 * @param source the binding source
	 * @return the determined binding type
	 */
	private BindingType selectBindingType(final BeanPropertyReference<E> source) {
		if (source.hasFxProperty()) {
			final Class<?> propertyClass = source.getFxProperty() != null ? source.getFxProperty().getClass() : null;
			if (propertyClass == null) {
				throw new IllegalStateException("Property '" + source.getName() + "' in '" + source.getBeanClass()
						+ "' has a property-getter, but property-getter did a null value!");
			}
			if (Property.class.isAssignableFrom(propertyClass) || useSelectionModelForBinding()) {
				// bi-directional binding possible
				return BindingType.BIDRECTIONAL;
			} else {
				// only uni-directional binding is possible
				return BindingType.UNIDIRECTIONAL;
			}
		} else {
			// source is not an observable, so we will do a value-based binding
			return BindingType.VALUEBASED;
		}
	}

	/**
	 * Change listener that is aware of a potential {@link SelectionModel}. In case
	 * such a {@link SelectionModel} is provided, the {@code target} is only
	 * manipulated through this selection model.
	 *
	 * @author koster
	 *
	 */
	private class SelectionModelAwareChangeListener implements ChangeListener<E> {

		private boolean updating = false;

		@Override
		public void changed(final ObservableValue<? extends E> sourceProperty, final E oldValue, final E newValue) {
			if (!updating) {
				try {
					updating = true;
					if (source.hasFxProperty() && sourceProperty == source.getFxProperty()) {
						if (useSelectionModelForBinding()) {
							selectionModel.select(newValue);
						} else {
							((Property<E>) target).setValue(newValue);
						}
					} else {
						source.setValue(newValue);
					}
				} catch (final RuntimeException e) {
					restoreOldValue(sourceProperty, oldValue, e);
				} finally {
					updating = false;
				}

			}
		}

		private void restoreOldValue(final ObservableValue<? extends E> sourceProperty, final E oldValue,
				final RuntimeException e) {
			try {
				if (source.hasFxProperty() && sourceProperty == source.getFxProperty()) {
					if (useSelectionModelForBinding()) {
						selectionModel.select(oldValue);
					} else {
						((Property<E>) target).setValue(oldValue);
					}
				} else {
					source.setValue(oldValue);
				}
			} catch (final Exception e2) {
				e2.addSuppressed(e);
				unbind();
				throw new IllegalStateException("Bidirectional binding failed together with an attempt"
						+ " to restore the source property to the previous value."
						+ " Removing the bidirectional binding from properties " + source.getName() + " and " + target,
						e2);
			}
			throw new IllegalStateException("Bidirectional binding failed, setting to the previous value", e);
		}
	}
}
