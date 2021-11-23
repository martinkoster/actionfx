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
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;

import com.github.actionfx.core.extension.AbstractAnnotationBasedExtension;

/**
 * Base class for beans extensions that apply a logic to controller fields and
 * methods that are marked with annotations.
 *
 * @param <T> the type of annotated element, e.g. a field or method.
 *
 * @author koster
 *
 */
public abstract class AbstractAnnotationBasedBeansExtension<A extends Annotation, E extends AnnotatedElement>
		extends AbstractAnnotationBasedExtension<A, E> implements BeanExtension {

	protected AbstractAnnotationBasedBeansExtension(final Class<A> annotationType) {
		super(annotationType);
	}

	/**
	 * This method is the entry to a controller extension that aims at extending the
	 * given {@code controller} instance.
	 *
	 * @param beanClass the bean class
	 */
	@Override
	public void extendBean(final Class<?> beanClass, final String beanId, final boolean singleton,
			final boolean lazyInit) {
		final Map<E, List<A>> annotatedElementsMap = lookupAnnotatedElements(beanClass);
		for (final var entry : annotatedElementsMap.entrySet()) {
			for (final var annotation : entry.getValue()) {
				extend(beanClass, beanId, singleton, lazyInit, entry.getKey(), annotation);
			}
		}
	}

	/**
	 * The actual extension logic is implemented in this method, which receives the
	 * controller instance, as well as the annotated element and one annotation.
	 *
	 * @param beanClass        the bean class
	 * @param beanId           the bean id
	 * @param singleton        flag that indicates whether the bean is a singleton
	 *                         ({@code true}) or a prototype-scoped bean
	 *                         ({@code false})
	 * @param lazyInit         flag that indicates whether the bean is lazily
	 *                         initialized ({@code true}) or not ({@code false})
	 * @param annotatedElement the annotated element (e.g. field or method)
	 * @param annotation       the annotation applied to the annotated element
	 */
	protected abstract void extend(Class<?> beanClass, String beanId, boolean singleton, boolean lazyInit,
			final E annotatedElement, final A annotation);
}
