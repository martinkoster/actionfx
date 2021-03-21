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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utils class for working with annotations.
 *
 * @author koster
 *
 */
public class AnnotationUtils {

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
	 * @return the annotation, or {@code null}, if the annotation is not present on
	 *         the given class.
	 */
	public static <A extends Annotation> A findAnnotation(final Class<?> clazz, final Class<A> annotationType) {
		final A annotation = clazz.getAnnotation(annotationType);
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
	 * Finds all annotations on the given {@code clazz}. In case the annotation is
	 * not present on {@code clazz}, the super classes of {@code clazz} are also
	 * checked. This method supports repeatable annotations.
	 *
	 * @param <A>            the annotation type
	 * @param clazz          the class to check
	 * @param annotationType the annotation class
	 * @return all found annotations, or an empty list, if the annotation is not
	 *         present on any of the classes of the class hierarchy.
	 */
	public static <A extends Annotation> List<A> findAllAnnotations(final Class<?> clazz,
			final Class<A> annotationType) {
		final List<A> result = new ArrayList<>();
		final A[] annotations = clazz.getAnnotationsByType(annotationType);
		if (annotations != null) {
			result.addAll(Arrays.asList(annotations));
		}
		if (clazz.getSuperclass() != null) {
			result.addAll(findAllAnnotations(clazz.getSuperclass(), annotationType));
		}
		return result;
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
	public static <A extends Annotation> A findAnnotation(final AnnotatedElement element,
			final Class<A> annotationType) {
		return element.getAnnotation(annotationType);
	}

	/**
	 * Finds all annotations on the given {@code element}. This method is intended
	 * for repeatable annotations.In case the annotation is not present on
	 * {@code field}, {@code null} is returned.
	 *
	 * @param <A>            the annotation type
	 * @param element        the annotated element (class, method, field) to check
	 * @param annotationType the annotation class
	 * @return the list of annotations, or an empty list, in case the
	 *         {@code element} does not carry the expected annotation
	 */
	public static <A extends Annotation> List<A> findAllAnnotations(final AnnotatedElement element,
			final Class<A> annotationType) {
		final A[] annotations = element.getAnnotationsByType(annotationType);
		if (annotations == null || annotations.length == 0) {
			return new ArrayList<>();
		}
		return Arrays.asList(annotations);
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
	public static <A extends Annotation> void invokeMethodWithAnnotation(final Class<?> clazz, final Object instance,
			final Class<A> annotationClass) {
		final Method[] declaredMethods = clazz.getDeclaredMethods();
		for (final Method method : declaredMethods) {
			if (method.isAnnotationPresent(annotationClass)) {
				ReflectionUtils.invokeMethod(method, instance);
			}
		}
		final Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) {
			invokeMethodWithAnnotation(superclass, instance, annotationClass);
		}
	}

	/**
	 * Extracts all fields annotated by <tt>annotationClazz</tt> in class
	 * <tt>clazz</tt> and returns a map, where the key is the field itself and the
	 * value is the list of annotations (which may be repeatable).
	 *
	 * @param clazz             the class to be checked for annotated fields
	 * @param annotationClazz   the annotation class to be looked up for
	 * @param checkSuperClasses flag, that determines, whether super classes shall
	 *                          be scanned or not
	 * @param comparator        an optional <tt>Comparator</tt> instance for sorting
	 *                          the resulting map
	 * @return a map, where the key is the field instance and the value is the list
	 *         of retrieved annotations (annotations can be repeatable)
	 */
	public static <T extends Annotation> Map<Field, List<T>> findAnnotatedFields(final Class<?> clazz,
			final Class<T> annotationClazz, final boolean checkSuperClasses, final Comparator<Field> comparator) {
		Map<Field, List<T>> fieldMap;
		if (comparator != null) {
			fieldMap = new TreeMap<>(comparator);
		} else {
			fieldMap = new HashMap<>();
		}
		final Field[] fields = ReflectionUtils.getDeclaredFields(clazz);
		if (fields != null) {
			for (final Field field : fields) {
				final List<T> annotationList = findAllAnnotations(field, annotationClazz);
				if (!annotationList.isEmpty()) {
					fieldMap.put(field, annotationList);
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
	 * <tt>clazz</tt> and returns a map. Because annotations may be repeatable, the
	 * value of the returned map is a list.
	 *
	 * @param clazz             the class to be checked for annotated fields
	 * @param annotationClazz   the annotation class to be looked up for
	 * @param checkSuperClasses flag, that determines, whether super classes shall
	 *                          be scanned or not
	 * @return a map, where the key is the field and the value is the list of the
	 *         (repeatable) annotations.
	 */
	public static <T extends Annotation> Map<Field, List<T>> findAnnotatedFields(final Class<?> clazz,
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
	 * @return a list of fields
	 */
	public static <T extends Annotation> List<Field> findAllAnnotatedFields(final Class<?> clazz,
			final Class<T> annotationClazz, final boolean checkSuperClasses) {
		final List<Field> fieldList = new ArrayList<>();
		final List<Field> fields = ReflectionUtils.findAllDeclaredFields(clazz);
		for (final Field field : fields) {
			final T a = AnnotationUtils.findAnnotation(field, annotationClazz);
			if (a != null) {
				fieldList.add(field);
			}
		}
		if (checkSuperClasses && clazz.getSuperclass() != null) {
			fieldList.addAll(findAllAnnotatedFields(clazz.getSuperclass(), annotationClazz, checkSuperClasses));
		}
		return fieldList;
	}

}
