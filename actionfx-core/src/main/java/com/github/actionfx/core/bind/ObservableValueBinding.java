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

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.beans.BeanPropertyReference;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.utils.AFXUtils;

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
public class ObservableValueBinding<S, E> extends AbstractBinding<BeanPropertyReference<S>, ObservableValue<E>> {

	private final BindingType bindingType;

	private SelectionModel<E> selectionModel;

	private final BindingListener contentBinding;

	private boolean bound = false;

	private Class<? extends E> targetType;

	private final ConversionService conversionService;

	/**
	 * Constructor accepting a binding source and binding target, while the binding
	 * target must be an {@link Property}.
	 *
	 * @param source the binding source
	 * @param target the binding target
	 */
	public ObservableValueBinding(final BeanPropertyReference<S> source, final ObservableValue<E> target) {
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
	@SuppressWarnings("unchecked")
	public ObservableValueBinding(final BeanPropertyReference<S> source, final ObservableValue<E> target,
			final SelectionModel<E> selectionModel) {
		super(source, target);
		this.selectionModel = selectionModel;
		this.conversionService = ActionFX.isInitialized() ? ActionFX.getInstance().getConversionService()
				: new ConversionService();
		bindingType = selectBindingType(source);
		contentBinding = new BindingListener();
		targetType = (Class<? extends E>) AFXUtils.determineObservableValueType(target);
	}

	@Override
	public void bind() {
		switch (bindingType) {
		case BIDIRECTIONAL:
			bindBidirectional();
			break;
		default:
		case UNIDIRECTIONAL:
			bindUnidirectional();
			break;
		}
		bound = true;
	}

	@Override
	public void unbind() {
		switch (bindingType) {
		case BIDIRECTIONAL:
			unbindBidirectional();
			break;
		default:
		case UNIDIRECTIONAL:
			unbindUnidirectional();
			break;
		}
		bound = false;
	}

	@Override
	public BindingType getBindingType() {
		return bindingType;
	}

	@Override
	public boolean isBound() {
		return bound;
	}

	/**
	 * Binds {@code source} and {@code target} bidirectionally.
	 */
	protected void bindBidirectional() {
		setTargetValue(source.getValue());
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
		setTargetValue(source.getValue());
		target.addListener(contentBinding);
	}

	/**
	 * Removes a unidirectional binding between {@code source} and {@code target}.
	 */
	protected void unbindUnidirectional() {
		target.removeListener(contentBinding);
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
	private BindingType selectBindingType(final BeanPropertyReference<?> source) {
		if (source.hasFxProperty()) {
			final Class<?> propertyClass = source.getFxProperty() != null ? source.getFxProperty().getClass() : null;
			if (propertyClass == null) {
				throw new IllegalStateException("Property '" + source.getName() + "' in '" + source.getBeanClass()
						+ "' has a property-getter, but property-getter did a null value!");
			}
			if (Property.class.isAssignableFrom(propertyClass) || useSelectionModelForBinding()) {
				// bi-directional binding possible
				return BindingType.BIDIRECTIONAL;
			} else {
				// only uni-directional binding is possible
				return BindingType.UNIDIRECTIONAL;
			}
		} else {
			// source is not an observable, so we will do a value-based binding
			return BindingType.UNIDIRECTIONAL;
		}
	}

	@SuppressWarnings("unchecked")
	private void setTargetValue(final S value) {
		final E targetValue = toType(value, targetType);
		if (useSelectionModelForBinding()) {
			selectionModel.select(targetValue);
		} else {
			if (WritableValue.class.isAssignableFrom(target.getClass())) {
				((WritableValue<E>) target).setValue(targetValue);
			}
		}
	}

	private <T> T toType(final Object value, final Class<T> type) {
		return conversionService.convert(value, type);
	}

	/**
	 * Change listener that is makes use of the
	 * {@link ObservableValueBinding#setTargetValue(Object)} method, which uses a
	 * potentially supplied selection model to manipulate the target list
	 * indirectly.
	 *
	 * @author koster
	 *
	 */
	private class BindingListener implements ChangeListener<Object> {

		private boolean updating = false;

		@SuppressWarnings("unchecked")
		@Override
		public void changed(final ObservableValue<? extends Object> sourceProperty, final Object oldValue,
				final Object newValue) {
			if (!updating) {
				try {
					updating = true;
					if (source.hasFxProperty() && sourceProperty == source.getFxProperty()) {
						setTargetValue((S) newValue);
					} else {
						setSourceValue((E) newValue);
					}
				} catch (final RuntimeException e) {
					restoreOldValue(sourceProperty, oldValue, e);
				} finally {
					updating = false;
				}

			}
		}

		@SuppressWarnings("unchecked")
		private void restoreOldValue(final ObservableValue<? extends Object> sourceProperty, final Object oldValue,
				final RuntimeException e) {
			try {
				if (source.hasFxProperty() && sourceProperty == source.getFxProperty()) {
					setTargetValue((S) oldValue);
				} else {
					setSourceValue((E) oldValue);
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

		private void setSourceValue(final E value) {
			source.setValue(toType(value, source.getType()));
		}
	}

}
