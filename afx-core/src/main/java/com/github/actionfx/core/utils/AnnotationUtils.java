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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Utils class for working with annotations.
 * 
 * @author koster
 *
 */
public class AnnotationUtils {

	private static final Predicate<Method> PUBLIC_METHOD_WITH_RESULT_PREDICATE = publicMethodWithReturnTypePredicate();

	private static final Predicate<Method> PUBLIC_METHOD_PREDICATE = publicMethodPredicate();

	private AnnotationUtils() {
		// class can not be instantiated
	}

	/**
	 * Finds an annotation on the given {@code clazz}. In case the annotation is not
	 * present on {@code clazz}, the super classes of {@code clazz} are also
	 * checked.
	 * 
	 * @param <A>            the annotation type
	 * @param clazz          the class to check
	 * @param annotationType the annotation class
	 * @return
	 */
	public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
		A annotation = clazz.getAnnotation(annotationType);
		if (annotation != null) {
			return annotation;
		}
		if (clazz.getSuperclass() != null) {
			return findAnnotation(clazz.getSuperclass(), annotationType);
		}
		// annotation not present? we return null.
		return null;
	}

	/**
	 * Finds an annotation on the given {@code element}. In case the annotation is
	 * not present on {@code field}, {@code null} is returned.
	 * 
	 * @param <A>            the annotation type
	 * @param element        the annotated element (class, method, field) to check
	 * @param annotationType the annotation class
	 * @return the annotation instance
	 */
	public static <A extends Annotation> A findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		return element.getAnnotation(annotationType);
	}

	/**
	 * Invokes all methods on the supplied {@code instance} that carry the given
	 * {@code annotationClass}.
	 * 
	 * @param clazz           the class on that the annotations shall be retrieved
	 *                        for
	 * @param instance        the instance
	 * @param annotationClass the annotation class
	 * @throws IllegalStateException
	 * @throws SecurityException
	 */
	public static <A extends Annotation> void invokeMethodWithAnnotation(Class<?> clazz, final Object instance,
			final Class<A> annotationClass) {
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (final Method method : declaredMethods) {
			if (method.isAnnotationPresent(annotationClass)) {
				AccessController.doPrivileged((PrivilegedAction<?>) () -> {
					boolean wasAccessible = method.canAccess(instance);
					try {
						method.setAccessible(true);
						return method.invoke(instance, new Object[] {});
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						throw new IllegalStateException("Problem invoking " + annotationClass + " : " + method, ex);
					} finally {
						method.setAccessible(wasAccessible);
					}
				});
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) {
			invokeMethodWithAnnotation(superclass, instance, annotationClass);
		}
	}

	/**
	 * Extracts all fields annotated by <tt>annotationClazz</tt> in class
	 * <tt>clazz</tt> and returns a map.
	 *
	 * @param clazz             the class to be checked for annotated fields
	 * @param annotationClazz   the annotation class to be looked up for
	 * @param checkSuperClasses flag, that determines, whether super classes shall
	 *                          be scanned or not
	 * @param comparator        an optional <tt>Comparator</tt> instance for sorting
	 *                          the resulting map
	 * @return a map, where the key is the instance of the the annotation and the
	 *         value the <tt>Field</tt>
	 */
	public static <T extends Annotation> Map<T, Field> findAnnotatedFields(final Class<?> clazz,
			final Class<T> annotationClazz, final boolean checkSuperClasses, final Comparator<T> comparator) {
		Map<T, Field> fieldMap;
		if (comparator != null) {
			fieldMap = new TreeMap<>(comparator);
		} else {
			fieldMap = new HashMap<>();
		}
		final Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {
			for (final Field field : fields) {
				final T a = findAnnotation(field, annotationClazz);
				if (a != null) {
					fieldMap.put(a, field);
				}
			}
		}
		if (checkSuperClasses && clazz.getSuperclass() != null) {
			fieldMap.putAll(findAnnotatedFields(clazz.getSuperclass(), annotationClazz, checkSuperClasses, comparator));
		}
		return fieldMap;
	}

	/**
	 * Extracts all fields annotated by <tt>annotationClazz</tt> in class
	 * <tt>clazz</tt> and returns a map.
	 *
	 * @param clazz             the class to be checked for annotated fields
	 * @param annotationClazz   the annotation class to be looked up for
	 * @param checkSuperClasses flag, that determines, whether super classes shall
	 *                          be scanned or not
	 * @return a map, where the key is the instance of the the annotation and the
	 *         value the <tt>Field</tt>
	 */
	public static <T extends Annotation> Map<T, Field> findAnnotatedFields(final Class<?> clazz,
			final Class<T> annotationClazz, final boolean checkSuperClasses) {
		return findAnnotatedFields(clazz, annotationClazz, checkSuperClasses, null);
	}

	/**
	 * Extracts all fields annotated by <tt>annotationClazz</tt> in class
	 * <tt>clazz</tt> and returns a list.
	 *
	 * @param clazz             the class to be checked for annotated fields
	 * @param annotationClazz   the annotation class to be looked up for
	 * @param checkSuperClasses flag, that determines, whether super classes shall
	 *                          be scanned or not
	 * @return a map, where the key is the instance of the the annotation and the
	 *         value the <tt>Field</tt>
	 */
	public static <T extends Annotation> List<Field> findAllAnnotatedFields(final Class<?> clazz,
			final Class<T> annotationClazz, final boolean checkSuperClasses) {
		final List<Field> fieldList = new ArrayList<>();
		final Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {
			for (final Field field : fields) {
				final T a = AnnotationUtils.findAnnotation(field, annotationClazz);
				if (a != null) {
					fieldList.add(field);
				}
			}
		}
		if (checkSuperClasses && clazz.getSuperclass() != null) {
			fieldList.addAll(findAllAnnotatedFields(clazz.getSuperclass(), annotationClazz, checkSuperClasses));
		}
		return fieldList;
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
