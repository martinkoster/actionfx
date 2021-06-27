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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.beans.value.ObservableValue;

/**
 * Wrapper around a JavaBean to access its properties via convenience methods.
 *
 * @author koster
 *
 */
public class BeanWrapper {

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
	 * Retrieves a field value described by a nested path
	 * {@code nestedPropertyPath}.
	 * <p>
	 * In case the {@link nestedPathProperty} resolves to an instance of type
	 * {@link ObservableValue}, the property value and not the property itself is
	 * returned (because we do not do direct field access, but we use the
	 * corresponding getter-method. Getter-methods on properties return the value,
	 * not the property).
	 * <p>
	 * In case you need access to the {@link ObservableValue} instance itself,
	 * please use method {@link #getObservableValue(String, Object)}, which uses the
	 * property-getter and not the regular getter for retrieving the field instance.
	 * <p>
	 * In case one of the path elements of the nested path evaluates to
	 * {@code null}, {@code null} is returned by this method.
	 *
	 * @param nestedPropertyPath a nested path to a property
	 * @return the instance that is the provider of the filed
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(final String nestedPropertyPath) {
		return (T) getPropertyValue(nestedPropertyPath, Object.class);
	}

	/**
	 * /** Retrieves a field value described by a nested path
	 * ({@code nestedPropertyPath}. The value is expected to be of type
	 * {@link ObservableValue}.
	 * <p>
	 * Unlike method {@link #getPropertyValue(String, Object)}, this method uses the
	 * "property-getter" method for accessing the property (which is
	 * "propertyNameProperty()" for a property with name "propertyName").
	 * <p>
	 * In case one of the path elements of the nested path evaluates to
	 * {@code null}, {@code null} is returned by this method.
	 *
	 * @param nestedPropertyPath a nested path to a property
	 * @return the instance that is the provider of the property
	 */
	@SuppressWarnings("unchecked")
	public <T> ObservableValue<T> getObservableValue(final String nestedPropertyPath) {
		return getPropertyValue(nestedPropertyPath, ObservableValue.class);
	}

	/**
	 * Follows the given {@code nestedPropertyPath} along the given
	 * {@code rootInstance} and extracts a value that is expected of given
	 * {@code expectedType}.
	 * <p>
	 * The values are retrieved from the underlying wrapper by using preferably
	 * getter-methods. In case a field does not offer a getter-method, direct field
	 * access is performed.
	 * <p>
	 * In case the {@code expectedType} is of type {@link ObservableValue}, then the
	 * corresponding property-getter method is invoked (which is <field>Property())
	 *
	 * @param nestedPropertyPath the nested property path
	 * @param expectedType       the type that is expected
	 * @return the extracted value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(final String nestedPropertyPath, final Class<T> expectedType) {
		final NestedExpression expression = new NestedExpression(nestedPropertyPath);
		BeanWrapper current = this;
		final Iterator<SingleExpression> it = expression.iterator();
		while (it.hasNext()) {
			final SingleExpression se = it.next();
			if (current.getBean() == null) {
				return null;
			} else if (se.isIndexed()) {
				current = BeanWrapper.of(current.getIndexedProperty(se.getPropertyName(), se.getIndex()));
			} else if (se.isMapped()) {
				current = BeanWrapper.of(current.getMappedProperty(se.getPropertyName(), se.getKey()));
			} else if (it.hasNext()) {
				current = BeanWrapper.of(current.getSimplePropertyValue(se.getPropertyName(), Object.class));
			} else {
				// expected type is only relevant for the last iteration
				current = BeanWrapper.of(current.getSimplePropertyValue(se.getPropertyName(), expectedType));
			}
		}
		return (T) current.getBean();
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
	 * Return the value of the specified indexed property of the specified bean,
	 * with no type conversions. In addition to supporting the JavaBeans
	 * specification, this method has been extended to support {@link List} objects
	 * as well.
	 *
	 * @param propertyName simple property name of the property value to be
	 *                     extracted
	 * @param index        Index of the property value to be extracted
	 * @return the indexed property value
	 */
	private Object getIndexedProperty(final String propertyName, final int index) {
		try {
			return PropertyUtils.getIndexedProperty(bean, propertyName, index);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalArgumentException("Unable to access indexed property of name '" + propertyName
					+ "' in class '" + bean.getClass().getCanonicalName() + "' at index '" + index + "'!", e);
		}
	}

	/**
	 * Return the value of the specified mapped property of the specified bean, with
	 * no type conversions.
	 *
	 * @param propertyName Mapped property name of the property value to be
	 *                     extracted
	 * @param key          Key of the property value to be extracted
	 * @return the mapped property value
	 */
	private Object getMappedProperty(final String propertyName, final String key) {
		try {
			return PropertyUtils.getMappedProperty(bean, propertyName, key);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalArgumentException("Unable to access mapped property of name '" + propertyName
					+ "' in class '" + bean.getClass().getCanonicalName() + "' with key '" + key + "'!", e);
		}
	}

	/**
	 * Gets the value of a property with name {@code propertyName}.
	 * <p>
	 * The following logic is applied for accessing the property value:
	 * <ul>
	 * <li>In case the expected value is an {@link ObservableValue}, then the
	 * property getter is tried (with pattern {@code <fieldName>Property()})</li>
	 * <li>In case step 1 did not apply or there was no property-getter, the regular
	 * get-method is invoked ({@code get<fieldName>()}).</li>
	 * <li>In case the two steps before failed, direct field access is tried.</li>
	 * </ul>
	 * <p>
	 * This method does not support nested paths.
	 *
	 * @param instance      the instance holding the
	 * @param propertyName  the name of the property
	 * @param exptectedType the expected type
	 * @return the property value
	 */
	protected <T> T getSimplePropertyValue(final String propertyName, final Class<T> exptectedType) {
		final Class<?> clazz = bean.getClass();
		final Field field = ReflectionUtils.findField(clazz, propertyName);
		if (field == null) {
			throw new IllegalStateException(
					"Class '" + clazz.getCanonicalName() + "' does not have a field with name '" + propertyName + "'!");
		}
		// if the expected value is an ObservableValue, we preferable access the
		// property getter method
		if (ObservableValue.class.isAssignableFrom(exptectedType)) {
			final Method propertyGetterMethod = ReflectionUtils.findMethod(clazz,
					propertyGetterMethodName(field.getName()));
			if (propertyGetterMethod != null) {
				// if property-getter-method is available, we take the value through the getter
				return cast(ReflectionUtils.invokeMethod(propertyGetterMethod, bean), exptectedType);
			}
		}
		final Method getterMethod = ReflectionUtils.findMethod(clazz,
				getterMethodName(field.getName(), field.getType().equals(boolean.class)));
		if (getterMethod != null) {
			// if getter-method is available, we take the value through the getter
			return cast(ReflectionUtils.invokeMethod(getterMethod, bean), exptectedType);
		} else {
			return cast(ReflectionUtils.getFieldValue(field, bean), exptectedType);
		}
	}

	/**
	 * Constructs the {@code getter}-method name for the supplied {@link field}
	 *
	 * @param fieldName the field name for that the {@code getter}-method name shall
	 *                  be constructed
	 * @return the {@code getter}-method name
	 */
	private static String getterMethodName(final String fieldName, final boolean isPrimitiveBoolean) {
		return (isPrimitiveBoolean ? "is" : "get") + fieldName.substring(0, 1).toUpperCase()
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
	 * Performs a type check, if the {@code value} can be cast to {@code expected}.
	 * If this is not possible, an {@link IllegalStateException} is thrown. If it is
	 * possible, the value is casted.
	 *
	 * @param <T>          the type
	 * @param value        the value
	 * @param expectedType the expected type
	 * @return the casted value
	 */
	@SuppressWarnings("unchecked")
	private static <T> T cast(final Object value, final Class<T> expectedType) {
		if (value == null) {
			return null;
		}
		if (!expectedType.isAssignableFrom(value.getClass())) {
			throw new IllegalStateException(
					"Value '" + value + "' can not be cast to '" + expectedType.getCanonicalName() + "'!");
		}
		return (T) value;
	}

}
