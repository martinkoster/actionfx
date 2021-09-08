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

import javafx.beans.property.ReadOnlyProperty;

/**
 * A reference to a Java bean property and its value;
 *
 * @author koster
 *
 */
public class BeanPropertyReference<T> {

	private final BeanProperty<T> property;

	private final Object bean;

	/**
	 * Constructor accepting the property and the bean holding the property.
	 *
	 * @param property the property
	 * @param bean     the bean holding the property value
	 */
	public BeanPropertyReference(final BeanProperty<T> property, final Object bean) {
		this.property = property;
		this.bean = bean;
	}

	/**
	 * Get the value from the bean.
	 *
	 * @return the value from the bean
	 */
	public T getValue() {
		return property.getValue(bean);
	}

	/**
	 * Sets the value inside the bean.
	 *
	 * @param value the value to set
	 */
	public void setValue(final T value) {
		property.setValue(bean, value);
	}

	/**
	 * Gets the the JavaFX property from the underlying bean (if available). If not
	 * available, an {@link IllegalStateException} is thrown.
	 *
	 * @return the JavaFX property, accessed via the propery-getter
	 */
	public ReadOnlyProperty<T> getFxProperty() {
		return property.getFxProperty(bean);
	}

	public Object getBean() {
		return bean;
	}

	public boolean isWritable() {
		return property.isWritable();
	}

	public boolean isReadable() {
		return property.isReadable();
	}

	public boolean hasFxProperty() {
		return property.hasFxProperty();
	}

	public String getName() {
		return property.getName();
	}

	public Class<? extends T> getType() {
		return property.getType();
	}

	public BeanProperty<T> getProperty() {
		return property;
	}

	public Class<?> getBeanClass() {
		return property.getBeanClass();
	}
}
