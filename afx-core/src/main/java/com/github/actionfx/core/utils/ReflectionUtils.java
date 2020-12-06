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
import java.util.function.Predicate;

/**
 * Utils for handling reflections.
 * 
 * @author koster
 *
 */
public class ReflectionUtils {

	private static final Predicate<Method> PUBLIC_METHOD_WITH_RESULT_PREDICATE = publicMethodWithReturnTypePredicate();

	private static final Predicate<Method> PUBLIC_METHOD_PREDICATE = publicMethodPredicate();
	/**
	 * Cache for {@link Class#getDeclaredFields()}, allowing for fast iteration.
	 */
	private static final Map<Class<?>, Field[]> DECLARED_FIELD_CACHE = Collections.synchronizedMap(new HashMap<>());

	private ReflectionUtils() {
		// class can not be instantiated
	}

	/**
	 * Gets the value from the supplied {@link Field} and {@code instance}.
	 * 
	 * @param field    the field
	 * @param instance the instance holding the value for the field
	 * @return the field value
	 */
	public static Object getFieldValue(Field field, Object instance) {
		return AccessController.doPrivileged((PrivilegedAction<?>) () -> {
			boolean wasAccessible = field.canAccess(instance);
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
	public static Object getFieldValueByGetter(Field field, Object instance) {
		Method method = findMethod(instance.getClass(), getterMethodName(field));
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
	public static Object getFieldValueByPropertyGetter(Field field, Object instance) {
		Method method = findMethod(instance.getClass(), propertyGetterMethodName(field));
		return invokeMethod(method, instance, Object.class);
	}

	/**
	 * Constructs the {@code getter}-method name for the supplied {@link field}
	 * 
	 * @param field the field for that the {@code getter}-method name shall be
	 *              constructed
	 * @return the {@code getter}-method name
	 */
	private static String getterMethodName(Field field) {
		String fieldName = field.getName();
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
	private static String propertyGetterMethodName(Field field) {
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
			boolean wasAccessible = field.canAccess(instance);
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
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
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
	public static Field findField(Class<?> clazz, String name) {
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
	public static Field findField(Class<?> clazz, String name, Class<?> type) {
		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			Field[] fields = getDeclaredFields(searchType);
			for (Field field : fields) {
				if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
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
	private static Field[] getDeclaredFields(Class<?> clazz) {
		Field[] result = DECLARED_FIELD_CACHE.get(clazz);
		if (result == null) {
			try {
				result = clazz.getDeclaredFields();
				DECLARED_FIELD_CACHE.put(clazz, (result.length == 0 ? new Field[0] : result));
			} catch (Exception ex) {
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
	public static <T> T invokeMethod(Method method, Object instance, Class<T> returnType, Object... arguments) {
		return (T) AccessController.doPrivileged((PrivilegedAction<?>) () -> {
			boolean wasAccessible = method.canAccess(instance);
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
	public static Method findMethod(Class<?> clazz, String name) {
		return findMethod(clazz, name, new Class[0]);
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
	public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : clazz.getDeclaredMethods());
			for (Method method : methods) {
				if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
		return (paramTypes.length == method.getParameterCount()
				&& Arrays.equals(paramTypes, method.getParameterTypes()));
	}

	/**
	 * Finds all public methods in a given <tt>clazz</tt>.
	 *
	 * @param clazz the class to be checked for public methods
	 * @return a <tt>java.util.List</tt> of methods
	 */
	public static List<Method> findAllPublicMethods(final Class<?> clazz) {
		return findPublicMethodsByPredicate(clazz, PUBLIC_METHOD_PREDICATE, new HashSet<Class<?>>());
	}

	/**
	 * Finds all public methods with a return type in a given <tt>clazz</tt>.
	 *
	 * @param clazz the class to be checked for public methods
	 * @return a <tt>java.util.List</tt> of methods
	 */
	public static List<Method> findPublicMethodsWithReturnType(final Class<?> clazz) {
		return findPublicMethodsByPredicate(clazz, PUBLIC_METHOD_WITH_RESULT_PREDICATE, new HashSet<Class<?>>());
	}

	/**
	 * Finds all methods that match the given {@code methodFilterPredicate}.
	 *
	 * @param clazz                 the class to be checked for public methods
	 * @param methodFilterPredicate the predicate acting as a public method filter.
	 *                              needs to evaluate to <tt>true</tt>.
	 * @param visited               a set of classes already visited (in order to
	 *                              avoid cycles)
	 * @return a <tt>java.util.List</tt> of methods
	 */
	public static List<Method> findPublicMethodsByPredicate(final Class<?> clazz,
			final Predicate<Method> methodFilterPredicate) {
		return findPublicMethodsByPredicate(clazz, methodFilterPredicate, new HashSet<Class<?>>());
	}

	/**
	 * Finds all public methods with a return type in a given <tt>clazz</tt>.
	 *
	 * @param clazz                 the class to be checked for public methods
	 * @param methodFilterPredicate the predicate acting as a public method filter.
	 *                              needs to evaluate to <tt>true</tt>.
	 * @param visited               a set of classes already visited (in order to
	 *                              avoid cycles)
	 * @return a <tt>java.util.List</tt> of methods
	 */
	private static List<Method> findPublicMethodsByPredicate(final Class<?> clazz,
			final Predicate<Method> methodFilterPredicate, final Set<Class<?>> visited) {
		final List<Method> methodList = new ArrayList<>();
		visited.add(clazz);
		final Method[] methods = clazz.getMethods();
		if (methods != null) {
			for (final Method method : methods) {
				if (methodFilterPredicate.test(method)) {
					methodList.add(method);
				}
			}
		}
		return methodList;
	}

	/**
	 * Predicate for public methods with a return type (except from Object.class).
	 *
	 * @return the predicate
	 */
	private static Predicate<Method> publicMethodWithReturnTypePredicate() {
		return method -> {
			final Class<?> returnType = method.getReturnType();
			return method.getDeclaringClass() != Object.class && returnType != null && returnType != void.class;
		};
	}

	/**
	 * Predicate for all public methods (except from Object.class).
	 *
	 * @return the predicate
	 */
	private static Predicate<Method> publicMethodPredicate() {
		return method -> method.getDeclaringClass() != Object.class;
	}
}
