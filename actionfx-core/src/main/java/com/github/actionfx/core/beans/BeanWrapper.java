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
package com.github.actionfx.core.beans;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;

/**
 * Wrapper around a JavaBean to access its properties via convenience methods.
 *
 * @author koster
 *
 */
public class BeanWrapper {

	private static final Map<CacheKey, BeanProperty<?>> PROPERTY_CACHE = Collections.synchronizedMap(new HashMap<>());

	private final Object bean;

	/**
	 * Constructor accepting the bean to wrap
	 *
	 * @param bean the bean to wrap
	 */
	public BeanWrapper(final Object bean) {
		this.bean = bean;
	}

	/**
	 * Convenience factory method for creating a {@link BeanWrapper}.
	 *
	 * @param bean the bean to wrap
	 * @return the created {@code BeanWrapper} instance
	 */
	public static BeanWrapper of(final Object bean) {
		return new BeanWrapper(bean);
	}

	/**
	 * Retrieves a value described by a nested path {@code nestedPropertyPath}.
	 * <p>
	 * In case the {@link nestedPathProperty} resolves to an instance of type
	 * {@link ObservableValue}, the property value and not the property itself is
	 * returned (because we do not do direct field access, but we use the
	 * corresponding getter-method. Getter-methods on properties return the value,
	 * not the property).
	 * <p>
	 * In case you need access to the {@link ObservableValue} instance itself,
	 * please use method {@link #getFxProperty(String)}, which uses the
	 * property-getter and not the regular getter for retrieving the field instance.
	 * <p>
	 * In case one of the path elements of the nested path evaluates to
	 * {@code null}, {@code null} is returned by this method.
	 *
	 * @param nestedPropertyPath a nested path to a property
	 * @return the instance that is the provider of the filed
	 */
	public <T> T getPropertyValue(final String nestedPropertyPath) {
		final BeanPropertyReference<T> reference = getBeanPropertyReference(nestedPropertyPath);
		if (reference == null) {
			// can happen, when one of the path elements resolves to null
			return null;
		}
		return reference.getValue();
	}

	/**
	 * Retrieves a JavaFX property described by a nested path
	 * ({@code nestedPropertyPath}. The value is expected to be of type
	 * {@link ReadOnlyProperty}.
	 * <p>
	 * This method uses the "property-getter" method for accessing the property
	 * (which is "propertyNameProperty()" for a property with name "propertyName").
	 * <p>
	 * In case one of the path elements of the nested path evaluates to
	 * {@code null}, {@code null} is returned by this method.
	 * <p>
	 * In case, there is no
	 *
	 * @param nestedPropertyPath a nested path to a property
	 * @return the instance that is the provider of the property
	 */
	public <T> ReadOnlyProperty<T> getFxProperty(final String nestedPropertyPath) {
		final BeanPropertyReference<T> reference = getBeanPropertyReference(nestedPropertyPath);
		if (reference == null) {
			// can happen, when one of the path elements resolves to null
			return null;
		}
		return reference.getFxProperty();
	}

	/**
	 * Retrieves a property reference described by a nested path
	 * {@code nestedPropertyPath}.
	 * <p>
	 * In case one of the path elements of the nested path evaluates to
	 * {@code null}, {@code null} is returned by this method.
	 *
	 * @param nestedPropertyPath a nested path to a property
	 * @return the property
	 */
	@SuppressWarnings("unchecked")
	public <T> BeanPropertyReference<T> getBeanPropertyReference(final String nestedPropertyPath) {
		final NestedExpression expression = new NestedExpression(nestedPropertyPath);
		BeanProperty<?> property = null;
		Object value = bean;
		final Iterator<SingleExpression> it = expression.iterator();
		while (it.hasNext()) {
			final SingleExpression se = it.next();
			if (value == null) {
				// one of the path elements "in the middle" is null
				return null;
			} else {
				property = getBeanProperty(value.getClass(), se.getPropertyName(), se.getIndex(), se.getKey());
			}
			if (it.hasNext()) {
				// in case there are further path elements, we need to resolve the value for the
				// next iteration
				value = property.getValue(value);
			} else {
				return new BeanPropertyReference<>((BeanProperty<T>) property, value);
			}
		}
		return null;
	}

	/**
	 * Returns the wrapped bean.
	 *
	 * @return the wrapped bean
	 */
	public Object getBean() {
		return bean;
	}

	/**
	 * Gets the property, either from cache or by creating a new instance (which is
	 * then again cached).
	 *
	 * @param beanClass the bean class
	 * @param name      the property name
	 * @param index     the index (in case of indexed property, {@code -1}
	 *                  otherwise)
	 * @param mapKey    the mapping key (in case of mapped property, {@code null}
	 *                  otherwise)
	 * @return the property
	 */
	private static BeanProperty<?> getBeanProperty(final Class<?> beanClass, final String name, final int index,
			final String mapKey) {
		return PROPERTY_CACHE.computeIfAbsent(CacheKey.of(beanClass, name, index, mapKey),
				cacheKey -> new BeanProperty<>(beanClass, name, index, mapKey));
	}

	private static class CacheKey {

		private final Class<?> clazz;

		private final String name;

		private final int index;

		private final String mapKey;

		public CacheKey(final Class<?> clazz, final String name, final int index, final String mapKey) {
			this.clazz = clazz;
			this.name = name;
			this.index = index;
			this.mapKey = mapKey;
		}

		static CacheKey of(final Class<?> clazz, final String name, final int index, final String mapKey) {
			return new CacheKey(clazz, name, index, mapKey);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (clazz == null ? 0 : clazz.hashCode());
			result = prime * result + index;
			result = prime * result + (mapKey == null ? 0 : mapKey.hashCode());
			result = prime * result + (name == null ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final CacheKey other = (CacheKey) obj;
			if (clazz == null) {
				if (other.clazz != null) {
					return false;
				}
			} else if (!clazz.equals(other.clazz)) {
				return false;
			}
			if (index != other.index) {
				return false;
			}
			if (mapKey == null) {
				if (other.mapKey != null) {
					return false;
				}
			} else if (!mapKey.equals(other.mapKey)) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			return true;
		}

	}
}
