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

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.collections.ObservableListAdapter;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.ControllerMethodInvocationAdapter;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;

/**
 * Extends controllers for functionality for {@link AFXLoadControlData}
 * annotation. Annotated methods are executed during controller initialization
 * after dependency injection, but before {@code @PostConstruct} annotated
 * methods are invoked.
 *
 * @author koster
 *
 */
public class OnLoadControlDataMethodControllerExtension
		extends AbstractAnnotatedMethodControllerExtension<AFXLoadControlData> {

	private static final Comparator<Method> AFXLOADCONTROLDATA_COMPARATOR = new OrderBasedAnnotatedMethodComparator<>(
			AFXLoadControlData.class, AFXLoadControlData::controlId, AFXLoadControlData::order);

	public OnLoadControlDataMethodControllerExtension() {
		super(AFXLoadControlData.class, AFXLOADCONTROLDATA_COMPARATOR);
	}

	@Override
	protected void extend(final Object controller, final Method annotatedElement, final AFXLoadControlData annotation) {
		final View view = ControllerWrapper.getViewFrom(controller);
		final BooleanProperty loadingActiveBooleanProperty = lookupBooleanProperty(controller,
				annotation.loadingActiveBooleanProperty());
		final ControlWrapper controlWrapper = createControlWrapper(annotation.controlId(), view);
		// check, whether the wrapped control supports multi-selection or only single
		// selection
		if (controlWrapper.supportsValues()) {
			populateControlsObservableList(controller, annotatedElement, loadingActiveBooleanProperty, controlWrapper,
					annotation.async());
		} else if (controlWrapper.supportsValue()) {
			populateControlsWritableValue(controller, annotatedElement, loadingActiveBooleanProperty, controlWrapper,
					annotation.async());
		} else {

			throw new IllegalStateException("Control with ID='" + annotation.controlId()
					+ "' does not support user input listening! Please check your ActionFX annotations inside constroller '"
					+ controller.getClass().getCanonicalName() + "'!");
		}
	}

	/**
	 * Populates the control that is wrapped inside {@link ControlWrapper} with the
	 * data that the given {@link Method} is returning.The value of the control is
	 * expected to be of type {@link ObservableList}.
	 *
	 * @param instance                     the instance holding the method
	 * @param method                       the method that returns the control data
	 * @param loadingActiveBooleanProperty an optional boolean property that
	 *                                     signalizes, whether the data can be
	 *                                     loaded (when set to {@code true})
	 * @param controlWrapper               the wrapped control
	 * @param asynchronous                 {@code true},if the data shall be
	 *                                     asynchronously loaded in a separate
	 *                                     thread without blocking the JavaFX
	 *                                     thread, {@code false},if it should be
	 *                                     loaded in the same thread.
	 */
	private void populateControlsObservableList(final Object instance, final Method method,
			final BooleanProperty loadingActiveBooleanProperty, final ControlWrapper controlWrapper,
			final boolean asynchronous) {
		final ObservableList<Object> valuesObservableList = controlWrapper.getValues();
		final ControllerMethodInvocationAdapter methodInvocationAdapter = createMethodInvocationAdapter(instance,
				method);
		if (loadingActiveBooleanProperty == null || loadingActiveBooleanProperty.get()) {
			populateObservableList(valuesObservableList, methodInvocationAdapter, asynchronous);
		}
		if (loadingActiveBooleanProperty != null) {
			// whenever value switches from false to true, we trigger a loading
			loadingActiveBooleanProperty.addListener((observable, oldValue, newValue) -> {
				if (Boolean.FALSE.equals(oldValue) && Boolean.TRUE.equals(newValue)) {
					populateObservableList(valuesObservableList, methodInvocationAdapter, asynchronous);
				}
			});
		}
	}

	/**
	 * Populates the given {@code observableList} with values from the supplied
	 * {@code methodInvocationAdapter}.
	 *
	 * @param observableList          the observable list to populate with values
	 *                                from the method invocation
	 * @param methodInvocationAdapter the method invocation adapter that will
	 *                                provide the values
	 * @param asynchronous            {@code true},if the data shall be
	 *                                asynchronously loaded in a separate thread
	 *                                without blocking the JavaFX thread,
	 *                                {@code false},if it should be loaded in the
	 *                                same thread.
	 */
	@SuppressWarnings({ "rawtypes" })
	private void populateObservableList(final ObservableList observableList,
			final ControllerMethodInvocationAdapter methodInvocationAdapter, final boolean asynchronous) {
		if (asynchronous) {
			methodInvocationAdapter.invokeAsynchronously(data -> setDataInObservableList(observableList, (List) data));
		} else {
			final List data = methodInvocationAdapter.invoke();
			setDataInObservableList(observableList, data);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void setDataInObservableList(final ObservableList observableList, final List data) {
		final ObservableListAdapter adapter = new ObservableListAdapter<>(observableList);
		adapter.getModifiableList().setAll(data);
	}

	/**
	 * Populates the control that is wrapped inside {@link ControlWrapper} with the
	 * data that the given {@link Method} is returning. The value of the control is
	 * expected to be of type {@link WritableValue}.
	 *
	 * @param instance                     the instance holding the method
	 * @param method                       the method that returns the control data
	 * @param loadingActiveBooleanProperty an optional boolean property that
	 *                                     signalizes, whether the data can be
	 *                                     loaded (when set to {@code true})
	 * @param controlWrapper               the wrapped control
	 * @param asynchronous                 {@code true},if the data shall be
	 *                                     asynchronously loaded in a separate
	 *                                     thread without blocking the JavaFX
	 *                                     thread, {@code false},if it should be
	 *                                     loaded in the same thread.
	 */
	@SuppressWarnings("unchecked")
	private void populateControlsWritableValue(final Object instance, final Method method,
			final BooleanProperty loadingActiveBooleanProperty, final ControlWrapper controlWrapper,
			final boolean asynchronous) {
		final ObservableValue<Object> observable = controlWrapper.getValueProperty();
		if (observable == null || !WritableValue.class.isAssignableFrom(observable.getClass())) {
			throw new IllegalStateException("Value property of control with ID='" + controlWrapper.getId()
					+ "' can not be populated with data from method '" + method.getName() + "' inside controller '"
					+ instance.getClass().getCanonicalName() + "'! Is the control holding a writable value property?");
		}
		final ControllerMethodInvocationAdapter methodInvocationAdapter = createMethodInvocationAdapter(instance,
				method);
		final WritableValue<Object> writableValue = (WritableValue<Object>) observable;
		if (loadingActiveBooleanProperty == null || loadingActiveBooleanProperty.get()) {
			populateWritableValue(writableValue, methodInvocationAdapter, asynchronous);
		}
		if (loadingActiveBooleanProperty != null) {
			// whenever value switches from false to true, we trigger a loading
			loadingActiveBooleanProperty.addListener((obs, oldValue, newValue) -> {
				if (Boolean.FALSE.equals(oldValue) && Boolean.TRUE.equals(newValue)) {
					populateWritableValue(writableValue, methodInvocationAdapter, asynchronous);
				}
			});
		}
	}

	/**
	 * Populates the given {@code writableValue} with values from the supplied
	 * {@code methodInvocationAdapter}.
	 *
	 * @param writableValue           the writable value to be populated with values
	 *                                from the method invocation
	 * @param methodInvocationAdapter the method invocation adapter that will
	 *                                provide the values
	 * @param asynchronous            {@code true},if the data shall be
	 *                                asynchronously loaded in a separate thread
	 *                                without blocking the JavaFX thread,
	 *                                {@code false},if it should be loaded in the
	 *                                same thread.
	 */
	private void populateWritableValue(final WritableValue<Object> writableValue,
			final ControllerMethodInvocationAdapter methodInvocationAdapter, final boolean asynchronous) {
		if (asynchronous) {
			methodInvocationAdapter.invokeAsynchronously(writableValue::setValue);
		} else {
			final Object data = methodInvocationAdapter.invoke();
			writableValue.setValue(data);
		}
	}

}
