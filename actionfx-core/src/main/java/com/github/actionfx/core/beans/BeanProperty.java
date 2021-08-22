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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.beans.property.ReadOnlyProperty;

/**
 * A specific property inside a Java bean. This class holds routines to access
 * the value via regular getter, property-getter or direct field access.
 * <p>
 * Constructing this instance is (comparably) expensive, because it uses
 * reflection to lookup getter, setter and field. Instances of this class should
 * be therefore cached.
 *
 * @author koster
 *
 */
public class BeanProperty<T> {

	private final Class<?> beanClass;

	private final String name;

	private Method getter;

	private Method setter;

	private Method propertyGetter;

	private Field field;

	private Class<?> type;

	private int index;

	private String mapKey;

	/**
	 * Instantiates a simple property with the given {@code name} inside the
	 * supplied {@code beanClass}.
	 *
	 * @param beanClass the bean class
	 * @param name      the property name
	 */
	public BeanProperty(final Class<?> beanClass, final String name) {
		this(beanClass, name, -1, null);
	}

	/**
	 * Instantiates an indexed property with the given {@code name} and
	 * {@code index} inside the supplied {@code beanClass}.
	 *
	 * @param beanClass the bean class
	 * @param name      the property name
	 * @param index     the index
	 */
	public BeanProperty(final Class<?> beanClass, final String name, final int index) {
		this(beanClass, name, index, null);
	}

	/**
	 * Instantiates a mapped property with the given {@code name} and {@code mapKey}
	 * inside the supplied {@code beanClass}.
	 *
	 * @param beanClass the bean class
	 * @param name      the property name
	 * @param mapKey    the map key
	 */
	public BeanProperty(final Class<?> beanClass, final String name, final String mapKey) {
		this(beanClass, name, -1, mapKey);
	}

	/**
	 * Instantiates an indexed or a mapped property with the given {@code name} and
	 * {@code index} or {@code mapKey} inside the supplied {@code beanClass}.
	 *
	 * @param beanClass the bean class
	 * @param name      the property name
	 * @param index     the index
	 * @param mapKey    the map key
	 */
	protected BeanProperty(final Class<?> beanClass, final String name, final int index, final String mapKey) {
		this.beanClass = beanClass;
		this.name = name;
		this.index = index;
		this.mapKey = mapKey;
		initialize();
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public String getMapKey() {
		return mapKey;
	}

	/**
	 * Gets the value from the supplied {@code bean}.
	 * <p>
	 * In case, the property is indexed, the indexed value is returned.
	 * <p>
	 * In case the property is mapped, the value under the map key is retrieved.
	 * <p>
	 * In case it is a non-indexed, non-mapped property, the value is retrieved
	 * either via the regular getter or via the property-getter, in case the getter
	 * is not available. In case the property-getter is also not available, direct
	 * field access for getting the value is attempted.
	 *
	 * @param bean the bean holding the property value
	 * @return the property value
	 */
	@SuppressWarnings("unchecked")
	public T getValue(final Object bean) {
		if (isIndexed()) {
			return (T) getIndexedProperty(bean);
		} else if (isMapped()) {
			return (T) getMappedProperty(bean);
		} else {
			return getSimpleValue(bean);
		}
	}

	@SuppressWarnings("unchecked")
	private T getSimpleValue(final Object bean) {
		if (getter == null && propertyGetter == null && field == null) {
			throw new IllegalStateException("Property '" + name + "' in class '" + beanClass
					+ "' has no getter, no property-getter and can  not be resolved to a field!");
		}
		if (getter != null) {
			return ReflectionUtils.invokeMethod(getter, bean);
		} else if (propertyGetter != null) {
			final ReadOnlyProperty<T> property = getFxProperty(bean);
			return property.getValue();
		} else {
			return (T) ReflectionUtils.getFieldValue(field, bean);
		}
	}

	/**
	 * Sets the value of the property inside the supplied {@code bean} to the
	 * specified {@code value}, either via the regular setter or via direct field
	 * access. attempted.
	 *
	 * @param bean  the bean holding the property
	 * @param value the value to set
	 */
	public void setValue(final Object bean, final T value) {
		if (isIndexed()) {
			setIndexedProperty(bean, value);
		} else if (isMapped()) {
			setMappedProperty(bean, value);
		} else {
			setSimpleValue(bean, value);
		}
	}

	private void setSimpleValue(final Object bean, final T value) {
		if (setter == null && field == null) {
			throw new IllegalStateException("Property '" + name + "' in class '" + beanClass
					+ "' has no setter and can  not be resolved to a field!");
		}
		if (setter != null) {
			ReflectionUtils.invokeMethod(setter, bean, value);
		} else {
			ReflectionUtils.setFieldValue(field, bean, value);
		}
	}

	/**
	 * Get the {@link javafx.beans.value.ObservableValue} implementation of the
	 * property.
	 *
	 * @param bean The {@code bean} instance for which the property should be read
	 * @return The {@code ReadOnlyProperty} instance of the property
	 */
	public ReadOnlyProperty<T> getFxProperty(final Object bean) {
		if (!hasFxProperty()) {
			throw new IllegalStateException(
					"Property '" + name + "' in class '" + beanClass + "' has no property-getter!");
		}
		return ReflectionUtils.invokeMethod(propertyGetter, bean);
	}

	/**
	 * Checks, if a property can be set.
	 *
	 * @return {@code true}, if the property can be set, {@code false} otherwise
	 */
	public boolean isWritable() {
		return setter != null || field != null;
	}

	/**
	 * Checks, if a property can be get.
	 *
	 * @return {@code true}, if the property can be get, {@code false} otherwise
	 */
	public boolean isReadable() {
		return getter != null || field != null;
	}

	/**
	 * Checks, if a property provides an implementation of
	 * {@link javafx.beans.value.ObservableValue}.
	 *
	 * @return {@code true}, if the property has a property-getter, {@code false}
	 *         otherwise.
	 */
	public boolean hasFxProperty() {
		return propertyGetter != null;
	}

	/**
	 * The type of the underlying property.
	 *
	 * @return the type of the property
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Returns {@code true}, if this property represents an indexed property.
	 *
	 * @return {@code true}, if this property is an indexed property, {@code false}
	 *         otherwise.
	 */
	public boolean isIndexed() {
		return index != -1;
	}

	/**
	 * Returns {@code true}, if this property represents a mapped property.
	 *
	 * @return {@code true}, if this property is a mapped property, {@code false}
	 *         otherwise.
	 */
	public boolean isMapped() {
		return mapKey != null;
	}

	/**
	 * Initializes the fields in this class by applying reflection.
	 */
	private void initialize() {
		initializeGetterAndType();
		initializeSetterAndType();
		initializePropertyGetterAndObservableType();
		initializeField();
	}

	private void initializeGetterAndType() {
		// check "get<name>"
		getter = ReflectionUtils.findMethod(beanClass, getterMethodName(name, false));
		if (getter == null) {
			// try "is<name>"
			getter = ReflectionUtils.findMethod(beanClass, getterMethodName(name, true));
		}
		if (getter != null) {
			type = getter.getReturnType();
		}
	}

	private void initializeSetterAndType() {
		final Method candidate = ReflectionUtils.findMethod(beanClass, setterMethodName(name));
		if (candidate != null) {
			final Class<?>[] parameterTypes = candidate.getParameterTypes();
			if (parameterTypes.length == 1) {
				setter = candidate;
				if (type == null) {
					type = parameterTypes[0];
				}
			}
		}
	}

	private void initializePropertyGetterAndObservableType() {
		propertyGetter = ReflectionUtils.findMethod(beanClass, propertyGetterMethodName(name));
	}

	private void initializeField() {
		field = ReflectionUtils.findField(beanClass, name);
	}

	/**
	 * Constructs the {@code getter}-method name for the supplied {@link field}
	 *
	 * @param fieldName          the field name for that the {@code getter}-method
	 *                           name shall be constructed
	 * @param isPrimitiveBoolean flag that controls, whether the boolean setter of
	 *                           form "is<field>", shall be constructed.
	 * @return the {@code getter}-method name
	 */
	private static String getterMethodName(final String fieldName, final boolean isPrimitiveBoolean) {
		return (isPrimitiveBoolean ? "is" : "get") + fieldName.substring(0, 1).toUpperCase()
				+ (fieldName.length() > 1 ? fieldName.substring(1, fieldName.length()) : "");
	}

	/**
	 * Constructs the {@code setter}-method name for the supplied {@link field}
	 *
	 * @param fieldName the field name for that the {@code setter}-method name shall
	 *                  be constructed
	 * @return the {@code getter}-method name
	 */
	private static String setterMethodName(final String fieldName) {
		return "set" + fieldName.substring(0, 1).toUpperCase()
				+ (fieldName.length() > 1 ? fieldName.substring(1, fieldName.length()) : "");
	}

	/**
	 * Constructs the {@code property-getter}-method name for the supplied
	 * {@link field}.
	 * <p>
	 * The name of the property-getter for a field with name "field" is derived as
	 * {@code fieldProperty}.
	 *
	 * @param field the field for that the {@code getter}-method name shall be
	 *              constructed
	 * @return the {@code getter}-method name
	 */
	private static String propertyGetterMethodName(final String fieldName) {
		return fieldName + "Property";
	}

	/**
	 * Return the value of the specified indexed property of the specified bean,
	 * with no type conversions. In addition to supporting the JavaBeans
	 * specification, this method has been extended to support {@link List} objects
	 * as well.
	 *
	 * @param bean the bean
	 * @return the indexed property value
	 */
	private Object getIndexedProperty(final Object bean) {
		try {
			return PropertyUtils.getIndexedProperty(bean, name, index);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalArgumentException("Unable to access indexed property of name '" + name + "' in class '"
					+ bean.getClass().getCanonicalName() + "' at index '" + index + "'!", e);
		}
	}

	/**
	 * Return the value of the specified mapped property of the specified bean, with
	 * no type conversions.
	 *
	 * @param bean the bean
	 * @return the mapped property value
	 */
	private Object getMappedProperty(final Object bean) {
		try {
			return PropertyUtils.getMappedProperty(bean, name, mapKey);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalArgumentException("Unable to access mapped property of name '" + name + "' in class '"
					+ bean.getClass().getCanonicalName() + "' with key '" + mapKey + "'!", e);
		}
	}

	/**
	 * Sets the value of the specified indexed property of the specified bean, with
	 * no type conversions. In addition to supporting the JavaBeans specification,
	 * this method has been extended to support {@link List} objects as well.
	 *
	 * @param bean  the bean
	 * @param value the value to set
	 * @return the indexed property value
	 */
	private void setIndexedProperty(final Object bean, final Object value) {
		try {
			PropertyUtils.setIndexedProperty(bean, name, index, value);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalArgumentException("Unable to set indexed property of name '" + name + "' in class '"
					+ bean.getClass().getCanonicalName() + "' at index '" + index + "'!", e);
		}
	}

	/**
	 * Sets the value of the specified mapped property of the specified bean, with
	 * no type conversions.
	 *
	 * @param bean  the bean
	 * @param value the value to set
	 * @return the mapped property value
	 */
	private void setMappedProperty(final Object bean, final Object value) {
		try {
			PropertyUtils.setMappedProperty(bean, name, mapKey, value);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalArgumentException("Unable to set mapped property of name '" + name + "' in class '"
					+ bean.getClass().getCanonicalName() + "' with key '" + mapKey + "'!", e);
		}
	}

}
