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
package com.github.actionfx.core.extension.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;

/**
 * Beans extension base class that extends annotated methods.
 *
 * @author koster
 *
 * @param <A> the annotation
 */
public abstract class AbstractAnnotatedMethodBeansExtension<A extends Annotation>
		extends AbstractAnnotationBasedBeansExtension<A, Method> {

	private final Comparator<Method> methodComparator;

	protected AbstractAnnotatedMethodBeansExtension(final Class<A> annotationType) {
		this(annotationType, null);
	}

	protected AbstractAnnotatedMethodBeansExtension(final Class<A> annotationType,
			final Comparator<Method> methodComparator) {
		super(annotationType);
		this.methodComparator = methodComparator;
	}

	@Override
	protected Map<Method, List<A>> lookupAnnotatedElements(final Class<?> clazz) {
		final var result = methodComparator != null ? new TreeMap<Method, List<A>>(methodComparator)
				: new LinkedHashMap<Method, List<A>>();
		final List<Method> annotatedMethods = ReflectionUtils.findMethods(clazz,
				method -> method.getAnnotation(annotationType) != null);
		for (final Method annotatedMethod : annotatedMethods) {
			final List<A> annotations = AnnotationUtils.findAllAnnotations(annotatedMethod, annotationType);
			result.put(annotatedMethod, annotations);
		}
		return result;
	}
}
