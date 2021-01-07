/*
 * Copyright (c) 2020 Martin Koster
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
package com.github.actionfx.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

/**
 * Utils for handling reflections.
 *
 * @author koster
 *
 */
public class ReflectionUtils {

	private static final MethodFilter PUBLIC_METHOD_WITH_RESULT_PREDICATE = publicMethodWithReturnTypePredicate();

	private static final MethodFilter PUBLIC_METHOD_PREDICATE = publicMethodPredicate();
	/**
	 * Cache for {@link Class#getDeclaredFields()}, allowing for fast iteration.
	 */
	private static final Map<Class<?>, Field[]> DECLARED_FIELD_CACHE = Collections.synchronizedMap(new HashMap<>());

	private ReflectionUtils() {
		// class can not be instantiated
	}

	/**
	 * Retrieves a field value described by a nested path
	 * ({@code nestedPropertyPath}.
	 * <p>
	 * In case the {@link nestedPathProperty} resolves to an instance of type
	 * {@link Property}, the property value and not the property itself is returned
	 * (because we do not do direct field access, but we use the corresponding
	 * getter-method. Getter-methods on properties return the value, not the
	 * property).
	 * <p>
	 * In case you need access to the {@link Property} instance itself, please use
	 * method {@link #getNestedFieldProperty(String, Object)}, which uses the
	 * property-getter and not the regular getter for retrieving the field instance.
	 * <p>
	 * In case one of the path elements of the nested path evaluates to
	 * {@code null}, {@code null} is returned by this method.
	 *
	 * @param nestedPropertyPath a nested path to a property
	 * @param rootInstance       the instance that the nested property path is
	 *                           referring to
	 * @return the instance that is the provider of the filed
	 */
	public static Object getNestedFieldValue(final String nestedPropertyPath, final Object rootInstance) {
		return getNestedFieldValue(nestedPropertyPath, rootInstance, Object.class);
	}

	/**
	 * Retrieves a field value described by a nested path
	 * ({@code nestedPropertyPath}. The value is expected to be of type
	 * {@link Property}.
	 * <p>
	 * Unlike method {@link #getNestedFieldValue(String, Object)}, this method uses
	 * the "property-getter" method for accessing the property (which is
	 * "propertyNameProperty()" for a property with name "propertyName").
	 * <p>
	 * In case one of the path elements of the nested path evaluates to
	 * {@code null}, {@code null} is returned by this method.
	 *
	 * @param nestedPropertyPath a nested path to a property
	 * @param rootInstance       the instance that the nested property path is
	 *                           referring to
	 * @return the instance that is the provider of the property
	 */
	@SuppressWarnings("unchecked")
	public static <T> Property<T> getNestedFieldProperty(final String nestedPropertyPath, final Object rootInstance) {
		return getNestedFieldValue(nestedPropertyPath, rootInstance, Property.class);
	}

	/**
	 * Follows the given {@code nestedPropertyPath} along the given
	 * {@code rootInstance} and extracts a value that is expected of given
	 * {@code expectedType}.
	 *
	 * @param nestedPropertyPath the nested property path
	 * @param rootInstance       the instance holding the property
	 * @param expectedType       the type that is expected
	 * @return the extracted value
	 */
	public static <T> T getNestedFieldValue(final String nestedPropertyPath, final Object rootInstance,
			final Class<T> expectedType) {
		final int dotIndex = nestedPropertyPath.indexOf('.');
		if (dotIndex == -1 || dotIndex == nestedPropertyPath.length() - 1) {
			return extractValueFromNonNestedPath(nestedPropertyPath, rootInstance, expectedType, dotIndex);
		} else {
			final String nextPropertyPathElement = nestedPropertyPath.substring(0, dotIndex);
			final String remainingPropertyPath = nestedPropertyPath.substring(dotIndex + 1);
			final Object nextInstance = extractPropertyValue(rootInstance, nextPropertyPathElement);
			if (nextInstance == null) {
				return null;
			} else {
				return getNestedFieldValue(remainingPropertyPath, nextInstance, expectedType);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T extractValueFromNonNestedPath(final String propertyName, final Object rootInstance,
			final Class<T> expectedType, final int dotIndex) {
		// no nested path? then the rootInstance is the instance that holds the field
		// itself - we just need to check the type that the caller wants to have
		// returned
		final String cleanedPropertyName = dotIndex == -1 ? propertyName : propertyName.substring(0, dotIndex);
		if (ObservableValue.class.isAssignableFrom(expectedType)) {
			// use "property-getter" for accessing the value ("Property" implements
			// "ObservableValue", so this
			// branch is also executed for expectedType == "Property")
			return (T) extractProperty(rootInstance, cleanedPropertyName);
		} else {
			// use regular "getter" for extracting the value
			final Object value = extractPropertyValue(rootInstance, cleanedPropertyName);
			if (value == null) {
				return null;
			} else if (!expectedType.isAssignableFrom(value.getClass())) {
				throw new IllegalStateException("Field at nested path '" + propertyName + "' inside class '"
						+ rootInstance.getClass().getCanonicalName() + "' does not resolve to type '"
						+ expectedType.getCanonicalName() + "'!");
			} else {
				return (T) value;
			}
		}
	}

	/**
	 * Gets the value from the supplied {@link Field} and {@code instance}.
	 *
	 * @param field    the field
	 * @param instance the instance holding the value for the field
	 * @return the field value
	 */
	public static Object getFieldValue(final Field field, final Object instance) {
		return AccessController.doPrivileged((PrivilegedAction<?>) () -> {
			final boolean wasAccessible = field.canAccess(instance);
			try {
				field.setAccessible(true);
				return field.get(instance);
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new IllegalStateException("Cannot get value from field: " + field, ex);
			} finally {
				field.setAccessible(wasAccessible);
			}
		});
	}

	/**
	 * Gets the value from the supplied {@link Field} and {@code instance}, but by
	 * trying to access the corresponding {@code getter} method.
	 *
	 * @param field    the field
	 * @param instance the instance holding the value for the field
	 * @return the field value
	 */
	public static Object getFieldValueByGetter(final Field field, final Object instance) {
		final Method method = findMethod(instance.getClass(), getterMethodName(field));
		return invokeMethod(method, instance, Object.class);
	}

	/**
	 * Gets the value from the supplied {@link Field} and {@code instance}, but by
	 * trying to access the corresponding {@code property-getter} method. This
	 * method is sometimes to be preferred over the direct field access, especially
	 * when a lazy-initialization of the field is performed in the respective
	 * {@code getter} method.
	 * <p>
	 * The name of the property-getter for a field with name "field" is derived as
	 * {@code fieldProperty()}.
	 *
	 * @param field    the field
	 * @param instance the instance holding the value for the field
	 * @return the field value
	 */
	public static Object getFieldValueByPropertyGetter(final Field field, final Object instance) {
		final Method method = findMethod(instance.getClass(), propertyGetterMethodName(field));
		return invokeMethod(method, instance, Object.class);
	}

	/**
	 * Constructs the {@code getter}-method name for the supplied {@link field}
	 *
	 * @param field the field for that the {@code getter}-method name shall be
	 *              constructed
	 * @return the {@code getter}-method name
	 */
	private static String getterMethodName(final Field field) {
		final String fieldName = field.getName();
		return (field.getType().equals(boolean.class) ? "is" : "get") + fieldName.substring(0, 1).toUpperCase()
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
	private static String propertyGetterMethodName(final Field field) {
		return field.getName() + "Property";
	}

	/**
	 * Sets the given {@code value} to the supplied {@code field} that the given
	 * {@code instance} holds.
	 *
	 * @param field    the field
	 * @param instance the instance that holds the field
	 * @param value    the value to set as field value
	 */
	public static void setFieldValue(final Field field, final Object instance, final Object value) {
		AccessController.doPrivileged((PrivilegedAction<?>) () -> {
			final boolean wasAccessible = field.canAccess(instance);
			try {
				field.setAccessible(true);
				field.set(instance, value);
				return null; // return nothing...
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new IllegalStateException("Cannot set field: " + field + " with value " + value, ex);
			} finally {
				field.setAccessible(wasAccessible);
			}
		});
	}

	/**
	 * Finds all declared fields, including the inherited fields, in the given
	 * <tt>clazz</tt>.
	 *
	 * @param clazz the class to find declared fields
	 * @return the list of retrieved fields
	 */
	public static List<Field> findAllDeclaredFields(final Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			fields.addAll(Arrays.asList(getDeclaredFields(c)));
		}
		return fields;
	}

	/**
	 * Attempt to find a {@link Field field} on the supplied {@link Class} with the
	 * supplied {@code name}. Searches all superclasses up to {@link Object}.
	 *
	 * @param clazz the class to introspect
	 * @param name  the name of the field
	 * @return the corresponding Field object, or {@code null} if not found
	 */
	public static Field findField(final Class<?> clazz, final String name) {
		return findField(clazz, name, null);
	}

	/**
	 * Attempt to find a {@link Field field} on the supplied {@link Class} with the
	 * supplied {@code name} and/or {@link Class type}. Searches all super-classes
	 * up to {@link Object}.
	 *
	 * @param clazz the class to introspect
	 * @param name  the name of the field (may be {@code null} if type is specified)
	 * @param type  the type of the field (may be {@code null} if name is specified)
	 * @return the corresponding Field object, or {@code null} if not found
	 */
	public static Field findField(final Class<?> clazz, final String name, final Class<?> type) {
		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			final Field[] fields = getDeclaredFields(searchType);
			for (final Field field : fields) {
				if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	/**
	 * Attempt to find all {@link Field field} on the supplied {@link Class} that
	 * matches the supplied {@link FieldFilter}. Searches all super-classes up to
	 * {@link Object}.
	 *
	 * @param clazz  the class to introspect
	 * @param filter the field filter that must evaluate to {@code true}
	 * @return the corresponding Field object, or {@code null} if not found
	 */
	public static List<Field> findFields(final Class<?> clazz, final FieldFilter filter) {
		final List<Field> result = new ArrayList<>();
		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			final Field[] fields = getDeclaredFields(searchType);
			for (final Field field : fields) {
				if (filter.matches(field)) {
					result.add(field);
				}
			}
			searchType = searchType.getSuperclass();
		}
		return result;
	}

	/**
	 * This variant retrieves {@link Class#getDeclaredFields()} from a local cache
	 * in order to avoid the JVM's SecurityManager check and defensive array
	 * copying.
	 *
	 * @param clazz the class to introspect
	 * @return the cached array of fields
	 * @throws IllegalStateException if introspection fails
	 * @see Class#getDeclaredFields()
	 */
	private static Field[] getDeclaredFields(final Class<?> clazz) {
		Field[] result = DECLARED_FIELD_CACHE.get(clazz);
		if (result == null) {
			try {
				result = clazz.getDeclaredFields();
				DECLARED_FIELD_CACHE.put(clazz, result.length == 0 ? new Field[0] : result);
			} catch (final Exception ex) {
				throw new IllegalStateException("Failed to retrieved declared fields from class '" + clazz.getName()
						+ "' with classLoader '" + clazz.getClassLoader() + "'", ex);
			}
		}
		return result;
	}

	/**
	 * Invokes the given {@link Method} on the supplied {@code instance}, expecting
	 * the return type of type {@code returnType}.
	 *
	 * @param <T>        the return type parameter
	 * @param method     the method to invoke
	 * @param instance   the instance on that the method shall be invoked
	 * @param returnType the return type
	 * @param arguments  the method arguments (can be left empty, in case the method
	 *                   does not need arguments)
	 * @return the result of the method invocation. Is {@code null} for {@code void}
	 *         methods.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(final Method method, final Object instance, final Class<T> returnType,
			final Object... arguments) {
		return (T) AccessController.doPrivileged((PrivilegedAction<?>) () -> {
			final boolean wasAccessible = method.canAccess(instance);
			try {
				method.setAccessible(true);
				return method.invoke(instance, arguments);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new IllegalStateException("Problem invoking method '" + method.getName() + "'!", ex);
			} finally {
				method.setAccessible(wasAccessible);
			}
		});
	}

	/**
	 * Attempt to find a {@link Method} on the supplied class with the supplied name
	 * and no parameters. Searches all super-classes up to {@code Object}.
	 * <p>
	 * Returns {@code null} if no {@link Method} can be found.
	 *
	 * @param clazz the class to introspect
	 * @param name  the name of the method
	 * @return the Method object, or {@code null} if none found
	 */
	public static Method findMethod(final Class<?> clazz, final String name) {
		return findMethod(clazz, name, new Class<?>[0]);
	}

	/**
	 * Attempt to find a {@link Method} on the supplied class with the supplied name
	 * and parameter types. Searches all super-classes up to {@code Object}.
	 * <p>
	 * Returns {@code null} if no {@link Method} can be found.
	 *
	 * @param clazz      the class to introspect
	 * @param name       the name of the method
	 * @param paramTypes the parameter types of the method (may be {@code null} to
	 *                   indicate any signature)
	 * @return the Method object, or {@code null} if none found
	 */
	public static Method findMethod(final Class<?> clazz, final String name, final Class<?>... paramTypes) {
		Class<?> searchType = clazz;
		while (searchType != null) {
			final Method[] methods = searchType.isInterface() ? searchType.getMethods()
					: searchType.getDeclaredMethods();
			for (final Method method : methods) {
				if (name.equals(method.getName())
						&& (paramTypes == null || paramTypes.length == 0 || hasSameParams(method, paramTypes))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	private static boolean hasSameParams(final Method method, final Class<?>[] paramTypes) {
		return paramTypes.length == method.getParameterCount() && Arrays.equals(paramTypes, method.getParameterTypes());
	}

	/**
	 * Finds all public methods in a given <tt>clazz</tt>.
	 *
	 * @param clazz the class to be checked for public methods
	 * @return a {@link java.util.List} of methods
	 */
	public static List<Method> findAllPublicMethods(final Class<?> clazz) {
		return findMethodsByFilter(clazz, PUBLIC_METHOD_PREDICATE, new HashSet<>());
	}

	/**
	 * Finds all public methods with a return type in a given <tt>clazz</tt>.
	 *
	 * @param clazz the class to be checked for public methods
	 * @return a {@link java.util.List} of methods
	 */
	public static List<Method> findPublicMethodsWithReturnType(final Class<?> clazz) {
		return findMethodsByFilter(clazz, PUBLIC_METHOD_WITH_RESULT_PREDICATE, new HashSet<>());
	}

	/**
	 * Finds all methods that match the given {@code methodFilterPredicate}.
	 *
	 * @param clazz        the class to be checked for public methods
	 * @param methodFilter the predicate acting as a public method filter. needs to
	 *                     evaluate to <tt>true</tt>.
	 * @param visited      a set of classes already visited (in order to avoid
	 *                     cycles)
	 * @return a {@link java.util.List} of methods
	 */
	public static List<Method> findMethods(final Class<?> clazz, final MethodFilter methodFilter) {
		return findMethodsByFilter(clazz, methodFilter, new HashSet<>());
	}

	/**
	 * Finds all public methods with a return type in a given <tt>clazz</tt>.
	 *
	 * @param clazz        the class to be checked for public methods
	 * @param methodFilter the predicate acting as a public method filter. needs to
	 *                     evaluate to <tt>true</tt>.
	 * @param visited      a set of classes already visited (in order to avoid
	 *                     cycles)
	 * @return a {@link java.util.List} of methods
	 */
	private static List<Method> findMethodsByFilter(final Class<?> clazz, final MethodFilter methodFilter,
			final Set<Class<?>> visited) {
		final List<Method> methodList = new ArrayList<>();
		if (visited.contains(clazz)) {
			return methodList;
		}
		visited.add(clazz);
		final Method[] methods = clazz.getDeclaredMethods();
		if (methods != null) {
			for (final Method method : methods) {
				if (methodFilter.matches(method)) {
					methodList.add(method);
				}
			}
		}
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
			methodList.addAll(findMethodsByFilter(clazz.getSuperclass(), methodFilter, visited));
		}
		return methodList;
	}

	/**
	 * Filter for public methods with a return type (except from Object.class).
	 *
	 * @return the predicate
	 */
	private static MethodFilter publicMethodWithReturnTypePredicate() {
		return method -> {
			final Class<?> returnType = method.getReturnType();
			return method.getDeclaringClass() != Object.class && returnType != null && returnType != void.class
					&& isPublic(method);
		};
	}

	/**
	 * Filter for all public methods (except from Object.class).
	 *
	 * @return the predicate
	 */
	private static MethodFilter publicMethodPredicate() {
		return method -> method.getDeclaringClass() != Object.class && isPublic(method);
	}

	/**
	 * Checks, whether the given {@link Method} is public.
	 *
	 * @param method the method to check
	 * @return {@code true}, if and only if the given method is public
	 */
	private static boolean isPublic(final Method method) {
		return (method.getModifiers() & Modifier.PUBLIC) > 0;
	}

	/**
	 * Gets the value of a property with name {@code propertyName}. Preferably, the
	 * getter-method is accessed for retrieving the property value. If there is no
	 * getter available for the property, a direct field access is tried.
	 * <p>
	 * This method does not support nested paths.
	 *
	 * @param instance     the instance holding the
	 * @param propertyName the name of the property
	 * @return the property value
	 */
	private static Object extractPropertyValue(final Object instance, final String propertyName) {
		final Class<?> clazz = instance.getClass();
		final Field field = findField(clazz, propertyName);
		if (field == null) {
			throw new IllegalStateException(
					"Class '" + clazz.getCanonicalName() + "' does not have a field with name '" + propertyName + "'!");
		}
		// check different ways of accessing the field value, preferably through the
		// getter
		final Method method = findMethod(clazz, getterMethodName(field));
		if (method != null) {
			// if getter-method is available, we take the value through the getter
			return invokeMethod(method, instance, Object.class);
		} else {
			return getFieldValue(field, instance);
		}
	}

	/**
	 * Gets the property with name {@code propertyName}. For this method, it is
	 * expected that the propertyName evaluates to an instance of {@link Property}.
	 * If this is not the case, an {@link ClassCastException} will be thrown
	 * <p>
	 * Preferably, the property-getter-method (e.g. "propertyNameProperty()") is
	 * accessed for retrieving the property value. If there is no getter available
	 * for the property, a direct field access is tried.
	 * <p>
	 * This method does not support nested paths.
	 *
	 * @param instance     the instance holding the
	 * @param propertyName the name of the property
	 * @return the property value
	 */
	@SuppressWarnings("unchecked")
	private static <T> ObservableValue<T> extractProperty(final Object instance, final String propertyName) {
		final Class<?> clazz = instance.getClass();
		final Field field = findField(clazz, propertyName);
		if (field == null) {
			throw new IllegalStateException(
					"Class '" + clazz.getCanonicalName() + "' does not have a field with name '" + propertyName + "'!");
		}
		// check different ways of accessing the field value, preferably through the
		// getter
		final Method method = findMethod(clazz, propertyGetterMethodName(field));
		if (method != null) {
			// if getter-method is available, we take the value through the getter
			return invokeMethod(method, instance, ObservableValue.class);
		} else {
			return (Property<T>) getFieldValue(field, instance);
		}
	}

	/**
	 * Callback optionally used to filter methods to be operated on by a method
	 * callback.
	 */
	@FunctionalInterface
	public interface MethodFilter {

		/**
		 * Determine whether the given method matches.
		 *
		 * @param method the method to check
		 */
		boolean matches(Method method);
	}

	/**
	 * Callback optionally used to filter fields to be operated on by a field
	 * callback.
	 */
	@FunctionalInterface
	public interface FieldFilter {

		/**
		 * Determine whether the given field matches.
		 *
		 * @param field the field to check
		 */
		boolean matches(Field field);
	}

}
