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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

	/**
	 * Map with primitive type to its wrapper type, e.g. int.class -> Integer.class.
	 */
	private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_TO_WRAPPER_MAP = new IdentityHashMap<>(8);

	/**
	 * Map with primitive wrapper to its type, e.g. Integer.class -> int.class.
	 */
	private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = new IdentityHashMap<>(8);

	static {
		initializeTypes();
	}

	private ReflectionUtils() {
		// class can not be instantiated
	}

	private static void initializeTypes() {
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Character.class, char.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Double.class, double.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Float.class, float.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Integer.class, int.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Long.class, long.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Short.class, short.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Void.class, void.class);

		for (final Map.Entry<Class<?>, Class<?>> entry : PRIMITIVE_WRAPPER_TYPE_MAP.entrySet()) {
			PRIMITIVE_TYPE_TO_WRAPPER_MAP.put(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * Instantiates the given {@code clazz} by using the constructor that is suited
	 * for the supplied {@code constructorArguments}.
	 *
	 * @param <T>                  the instance type
	 * @param clazz                the class to instation
	 * @param constructorArguments the arguments to pass to the constructor. Leave
	 *                             this parameter empty for invoking the default,
	 *                             no-arg constructor. <b>Please note:</b> If
	 *                             constructor arguments are provided, no argument
	 *                             must be {@code null}. The type of the constructor
	 *                             argument is used to identify the proper
	 *                             constructor to invoke. This mechanism is not
	 *                             possible with {@code null} value constructor
	 *                             arguments.
	 * @return the created instance
	 */
	public static <T> T instantiateClass(final Class<T> clazz, final Object... constructorArguments) {
		if (constructorArguments.length == 0) {
			try {
				return clazz.getDeclaredConstructor().newInstance();
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalArgumentException("Can not invoke default no-arg constructor for class '"
						+ clazz.getCanonicalName() + "'! Is it existing and public?", e);
			}
		} else {
			final Class<?>[] argumentTypes = new Class[constructorArguments.length];
			for (int i = 0; i < constructorArguments.length; i++) {
				argumentTypes[i] = constructorArguments[i].getClass();
			}
			Constructor<T> constructor;
			try {
				constructor = clazz.getDeclaredConstructor(argumentTypes);
				return constructor.newInstance(constructorArguments);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalArgumentException("Can not invoke constructor for class '" + clazz.getCanonicalName()
						+ "' accepting the given arguments. ", e);
			}
		}
	}

	/**
	 * Extracts all super classes and all interfaces that the given {@code clazz} is
	 * implementing.
	 *
	 * @param <T>   the class type
	 * @param clazz the class
	 * @return all super classes and interfaces the given {@code clazz} is
	 *         implementing
	 */
	public static Set<Class<?>> getAllSuperClassesAndInterfaces(final Class<?> clazz) {
		final Set<Class<?>> result = new HashSet<>();
		result.add(clazz);
		if (clazz.getInterfaces() != null && clazz.getInterfaces().length > 0) {
			for (final Class<?> iface : clazz.getInterfaces()) {
				result.addAll(getAllSuperClassesAndInterfaces(iface));
			}
		}
		if (clazz.getSuperclass() != null) {
			result.addAll(getAllSuperClassesAndInterfaces(clazz.getSuperclass()));
		}
		return result;
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
				field.setAccessible(true); // NOSONAR
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
		return invokeMethod(method, instance);
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
		return invokeMethod(method, instance);
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
	 * Constructs the {@code setter}-method name for the supplied {@link field}
	 *
	 * @param field the field for that the {@code setter}-method name shall be
	 *              constructed
	 * @return the {@code setter}-method name
	 */
	private static String setterMethodName(final Field field) {
		final String fieldName = field.getName();
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
				field.setAccessible(true); // NOSONAR
				field.set(instance, value); // NOSONAR
				return null; // return nothing...
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new IllegalStateException("Cannot set field: " + field + " with value " + value, ex);
			} finally {
				field.setAccessible(wasAccessible);
			}
		});
	}

	/**
	 * Sets the {@code field} value by calling the proper "setter" method.
	 *
	 * @param field    the field whose value shall be set through the "setter"
	 * @param instance the instance holding the field value
	 * @param value    the value to set
	 */
	public static void setFieldValueBySetter(final Field field, final Object instance, final Object value) {
		final Method method = findMethod(instance.getClass(), setterMethodName(field));
		invokeMethod(method, instance, value);
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
	public static Field[] getDeclaredFields(final Class<?> clazz) {
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
	public static <T> T invokeMethod(final Method method, final Object instance, final Object... arguments) {
		return AccessController.doPrivileged((PrivilegedAction<T>) () -> {
			final boolean wasAccessible = method.canAccess(instance);
			try {
				method.setAccessible(true); // NOSONAR
				if (arguments.length == 0) {
					return (T) method.invoke(instance);
				} else {
					return (T) method.invoke(instance, arguments);
				}
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
	 * Check if the given class represents a primitive wrapper, i.e. Boolean, Byte,
	 * Character, Short, Integer, Long, Float, Double, or Void.
	 *
	 * @param clazz the class to check
	 * @return whether the given class is a primitive wrapper class
	 */
	public static boolean isPrimitiveWrapper(final Class<?> clazz) {
		return PRIMITIVE_WRAPPER_TYPE_MAP.containsKey(clazz);
	}

	/**
	 * Resolve the given class, if it is a primitive class, returning the
	 * corresponding primitive wrapper type instead.
	 *
	 * @param clazz the class to check
	 * @return the original class, or a primitive wrapper for the original primitive
	 *         type
	 */
	public static Class<?> resolvePrimitiveIfNecessary(final Class<?> clazz) {
		return clazz.isPrimitive() && clazz != void.class ? PRIMITIVE_TYPE_TO_WRAPPER_MAP.get(clazz) : clazz;
	}

	/**
	 * Check if the given class represents a primitive (i.e. boolean, byte, char,
	 * short, int, long, float, or double), {@code void}, or a wrapper for those
	 * types (i.e. Boolean, Byte, Character, Short, Integer, Long, Float, Double, or
	 * Void).
	 *
	 * @param clazz the class to check
	 * @return {@code true} if the given class represents a primitive, void, or a
	 *         wrapper class
	 */
	public static boolean isPrimitiveOrWrapper(final Class<?> clazz) {
		return clazz.isPrimitive() || isPrimitiveWrapper(clazz);
	}

	/**
	 * Finds all public/private/protected/package protected methods with a return
	 * type in a given {@code clazz}.
	 * <p>
	 * Please note that overridden methods are only once in the result list, namely
	 * for the top-most class.
	 *
	 * @param clazz        the class to be checked for
	 *                     public/private/protected/package protected methods
	 * @param methodFilter the predicate acting as a method filter. needs to
	 *                     evaluate to {@code true}
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
			// only add methods from super classes, if these are not contained in method
			// list yet (overriden methods are filtered out)
			methodList.addAll(findMethodsByFilter(clazz.getSuperclass(), methodFilter, visited).stream()
					.filter(m -> !containsMethodWithSameNameAndSignature(methodList, m)).collect(Collectors.toList()));
		}
		return methodList;
	}

	/**
	 * Checks, if the supplied list of {@code methods} already contains a method
	 * that has the same name and signature than the specified {@code method}. The
	 * owning class however can be different (overridden methods e.g. have a same
	 * name and same arguments, but a different owning class).
	 *
	 * @param methods the list of methods to check
	 * @param method  the method
	 * @return {@code true} if the specified list already has a method with same
	 *         name and arguments (but different owning class), {@code false}
	 *         otherwise.
	 */
	private static boolean containsMethodWithSameNameAndSignature(final List<Method> methods, final Method method) {
		final Optional<Method> duplicate = methods.stream().filter(
				m -> m.getName().equals(method.getName()) && sameParameter(m.getParameters(), method.getParameters()))
				.findFirst();
		return !duplicate.isEmpty();
	}

	/**
	 * Checks, if the supplied {@link Parameter} in {@code params1} {@code params2}
	 * are identical.
	 *
	 * @param params1 the first parameter array
	 * @param params2 the second parameter array
	 * @return
	 */
	private static boolean sameParameter(final Parameter[] params1, final Parameter[] params2) {
		if (params1.length != params2.length) {
			return false;
		}
		for (int i = 0; i < params1.length; i++) {
			if (params1[i].getType() != params2[i].getType()) {
				return false;
			}
		}
		return true;
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
