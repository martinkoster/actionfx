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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

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
			final Class<A> annotationClass) throws IllegalStateException, SecurityException {
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
}
