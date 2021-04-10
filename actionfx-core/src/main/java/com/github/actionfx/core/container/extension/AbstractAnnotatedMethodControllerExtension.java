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
package com.github.actionfx.core.container.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.actionfx.core.listener.TimedChangeListener;
import com.github.actionfx.core.listener.TimedListChangeListener;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ControllerMethodInvocationAdapter;
import com.github.actionfx.core.utils.ControllerMethodInvocationAdapter.ParameterValue;
import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.beans.property.BooleanProperty;

/**
 * Controller extension base class that extends annotated fields.
 *
 * @author koster
 *
 * @param <A> the annotation
 */
public abstract class AbstractAnnotatedMethodControllerExtension<A extends Annotation>
		extends AbstractAnnotationBasedControllerExtension<A, Method> {

	private final Comparator<Method> methodComparator;

	protected AbstractAnnotatedMethodControllerExtension(final Class<A> annotationType) {
		this(annotationType, null);
	}

	protected AbstractAnnotatedMethodControllerExtension(final Class<A> annotationType,
			final Comparator<Method> methodComparator) {
		super(annotationType);
		this.methodComparator = methodComparator;
	}

	@Override
	protected Map<Method, List<A>> lookupAnnotatedElements(final Object controller) {
		final var result = methodComparator != null ? new TreeMap<Method, List<A>>(methodComparator)
				: new LinkedHashMap<Method, List<A>>();
		final List<Method> annotatedMethods = ReflectionUtils.findMethods(controller.getClass(),
				method -> method.getAnnotation(annotationType) != null);
		for (final Method annotatedMethod : annotatedMethods) {
			final List<A> annotations = AnnotationUtils.findAllAnnotations(annotatedMethod, annotationType);
			result.put(annotatedMethod, annotations);
		}
		return result;
	}

	/**
	 * Creates a new {@link MethodInvocation} for the supplied {@code method} on the
	 * given {@code instance}.
	 *
	 */
	protected ControllerMethodInvocationAdapter createMethodInvocationAdapter(final Object instance,
			final Method method, final ParameterValue... parameterValues) {
		return new ControllerMethodInvocationAdapter(instance, method, parameterValues);
	}

	/**
	 * Creates a value change listener that invokes the supplied {@link Method} on
	 * the given {@code instance}.
	 *
	 * @param instance                      the instance holding the method
	 * @param method                        the method to execute from the change
	 *                                      listener
	 * @param timeoutMs                     the timeout in milliseconds
	 * @param listenerActionBooleanProperty an optional boolean property that must
	 *                                      be set to {@code true}, so that the
	 *                                      change listener is executed
	 * @return
	 */
	protected TimedChangeListener<Object> createValueChangeListener(final Object instance, final Method method,
			final long timeoutMs, final BooleanProperty listenerActionBooleanProperty) {
		return new TimedChangeListener<>((observable, oldValue, newValue) -> {
			final ControllerMethodInvocationAdapter adapter = createMethodInvocationAdapter(instance, method,
					ParameterValue.ofAllSelectedValues(
							newValue == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(newValue))),
					ParameterValue.ofNewValue(newValue), ParameterValue.ofOldValue(oldValue),
					ParameterValue.of(observable));
			adapter.invoke();
		}, timeoutMs, listenerActionBooleanProperty);
	}

	/**
	 * Creates a list change listener that invokes the supplied {@link Method} on
	 * the given {@code instance}.
	 *
	 * @param instance                      the instance holding the method
	 * @param method                        the method to execute from the list
	 *                                      change listener
	 * @param timeoutMs                     the timeout in milliseconds
	 * @param listenerActionBooleanProperty an optional boolean property that must
	 *                                      be set to {@code true}, so that the
	 *                                      change listener is executed
	 * @param allValuesSupplier             a supplier that gets all values that are
	 *                                      selected
	 * @param singleValueSupplier           a supplier that gets a single selected
	 *                                      value (this is usually the last selected
	 *                                      item)
	 * @return
	 */
	protected TimedListChangeListener<Object> createListChangeListener(final Object instance, final Method method,
			final long timeoutMs, final BooleanProperty listenerActionBooleanProperty,
			final Supplier<List<Object>> allValuesSupplier, final Supplier<Object> singleValueSupplier) {
		return new TimedListChangeListener<>(change -> {
			final List<Object> addedList = new ArrayList<>();
			final List<Object> removedList = new ArrayList<>();
			while (change.next()) {
				if (change.wasAdded()) {
					addedList.addAll(change.getAddedSubList());
				}
				if (change.wasRemoved()) {
					removedList.addAll(change.getRemoved());
				}
			}
			change.reset();
			final ControllerMethodInvocationAdapter adapter = createMethodInvocationAdapter(instance, method,
					ParameterValue.ofAllSelectedValues(allValuesSupplier.get()),
					ParameterValue.ofAddedValues(addedList), ParameterValue.ofRemovedValues(removedList),
					ParameterValue.of(change), ParameterValue.of(singleValueSupplier.get()));
			adapter.invoke();
		}, timeoutMs, listenerActionBooleanProperty);
	}

	/**
	 * Comparator implementation that orders methods according to the "order"
	 * attribute among same controls.
	 *
	 * @param <A> the annotation type
	 * @author koster
	 *
	 */
	public static class OrderBasedAnnotatedMethodComparator<A extends Annotation> implements Comparator<Method> {

		private final Class<A> annotationClass;
		private final Function<A, String> controlIdFunction;
		private final Function<A, Integer> orderFunction;

		public OrderBasedAnnotatedMethodComparator(final Class<A> annotationClass,
				final Function<A, String> controlIdFunction, final Function<A, Integer> orderFunction) {
			this.annotationClass = annotationClass;
			this.controlIdFunction = controlIdFunction;
			this.orderFunction = orderFunction;
		}

		@Override
		public int compare(final Method o1, final Method o2) {
			final A c1 = o1.getAnnotation(annotationClass);
			final A c2 = o2.getAnnotation(annotationClass);
			return Comparator.comparing(controlIdFunction).thenComparing(orderFunction).compare(c1, c2);
		}
	}
}
