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
package com.github.actionfx.core.extension.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;

/**
 * Controller extension base class that extends annotated fields.
 *
 * @author koster
 *
 * @param <A> the annotation
 */
public abstract class AbstractAnnotatedFieldControllerExtension<A extends Annotation>
		extends AbstractAnnotationBasedControllerExtension<A, Field> {

	protected AbstractAnnotatedFieldControllerExtension(final Class<A> annotationType) {
		super(annotationType);
	}

	@Override
	protected Map<Field, List<A>> lookupAnnotatedElements(final Class<?> clazz) {
		return AnnotationUtils.findAnnotatedFields(clazz, annotationType, true);
	}

	/**
	 * Retrieves the field value from {@code field} inside the supplied
	 * {@code instance}. It is required that the retrieved value is not {@code null}
	 * and is of type {@code expectedType}.
	 *
	 * @param <T>          the type
	 * @param instance     the instance to retrieve the field value from
	 * @param field        the field
	 * @param expectedType the expected type
	 * @return the field value of type {@code expectedType}, never {@code null}
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getFieldValue(final Object instance, final Field field, final Class<T> expectedType) {
		final Object fieldValue = ReflectionUtils.getFieldValue(field, instance);
		if (fieldValue == null) {
			throw new IllegalStateException("Field '" + field.getName() + "' in class '"
					+ instance.getClass().getCanonicalName() + "' has 'null' value!");
		}
		if (!expectedType.isAssignableFrom(fieldValue.getClass())) {
			throw new IllegalStateException(
					"Field value of '" + field.getName() + "' in class '" + instance.getClass().getCanonicalName()
							+ "' is not of type '" + expectedType.getCanonicalName() + "'!");

		}
		return (T) fieldValue;
	}

}
