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
package com.github.actionfx.core.container.instantiation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.annotation.AFXNestedView.AFXNestedViews;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXOnControlValueChange;
import com.github.actionfx.core.annotation.AFXUseFilteredList;
import com.github.actionfx.core.collections.ObservableListAdapter;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.listener.TimedChangeListener;
import com.github.actionfx.core.listener.TimedListChangeListener;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ControllerMethodInvocationAdapter;
import com.github.actionfx.core.utils.ControllerMethodInvocationAdapter.ParameterValue;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;

/**
 * Post-processor for controller instances that is invoked after view creation
 * and dependency injection, but before potential
 * {@code @PostConstruct}-annotated methods.
 *
 * @author koster
 *
 */
public class ControllerInstancePostProcessor {

	private static final Comparator<Method> AFXONUSERINPUT_COMPARATOR = new OrderBasedAnnotatedMethodComparator<>(
			AFXOnControlValueChange.class, AFXOnControlValueChange::controlId, AFXOnControlValueChange::order);

	private static final Comparator<Method> AFXLOADCONTROLDATA_COMPARATOR = new OrderBasedAnnotatedMethodComparator<>(
			AFXLoadControlData.class, AFXLoadControlData::controlId, AFXLoadControlData::order);

	/**
	 * Performs a post-processing on the supplied {@code controller}, including
	 * parsing of applied ActionFX annotations like {@link AFXNestedViews},
	 * {@link AFXLoadControlData}, {@link AFXOnAction}.
	 *
	 * @param controller the controller instance to post process
	 */
	public void postProcess(final Object controller) {
		attachNestedViews(controller);
		applyFieldLevelAnnotations(controller);
		applyMethodLevelEventAnnotations(controller, ControllerWrapper.getViewFrom(controller));
	}

	/**
	 * Applies method-level annotations (e.g. {@link AFXOnControlValueChange}.
	 *
	 * @param instance the instance that is checked for ActionFX method level
	 *                 annotations
	 * @param view     the view that belongs to the controller
	 */
	private void applyMethodLevelEventAnnotations(final Object instance, final View view) {
		wireOnActions(instance, view);
		wireLoadControlData(instance, view);
		wireOnUserInputActions(instance, view);
	}

	/**
	 * Applies field-level annotations (e.g. {@link AFXEnableMultiSelection} or
	 * {@code AFXUseFilteredList}).
	 *
	 * @param instance the instance that is checked for ActionFX field level
	 *                 annotations
	 */
	private void applyFieldLevelAnnotations(final Object instance) {
		enableMultiSelectionControls(instance);
		enableFilteredList(instance);
	}

	/**
	 * Wires methods annotated with {@link AFXOnAction} to the corresponding
	 * control.
	 *
	 * @param instance the instance holding the methods
	 * @param view     the view that belongs to the controller
	 */
	private void wireOnActions(final Object instance, final View view) {
		final List<Method> methods = ReflectionUtils.findMethods(instance.getClass(),
				method -> method.getAnnotation(AFXOnAction.class) != null);
		for (final Method method : methods) {
			final AFXOnAction onAction = method.getAnnotation(AFXOnAction.class);
			final ControlWrapper controlWrapper = createControlWrapper(onAction.controlId(), view);
			final ObjectProperty<EventHandler<ActionEvent>> onActionProperty = controlWrapper.getOnActionProperty();
			if (onActionProperty == null) {
				throw new IllegalStateException("Control with id='" + onAction.controlId() + "' and type '"
						+ controlWrapper.getWrappedType().getCanonicalName()
						+ "' does not support an 'onAction' property! Please verify your @AFXOnAction annotation in controller class '"
						+ instance.getClass().getCanonicalName() + "', method '" + method.getName() + "'!");
			}
			onActionProperty.setValue(actionEvent -> {
				final ControllerMethodInvocationAdapter adapter = new ControllerMethodInvocationAdapter(instance,
						method, ParameterValue.of(actionEvent));
				adapter.invoke();
			});
		}
	}

	/**
	 * Wires methods annotated with {@link AFXOnControlValueChange} to the
	 * corresponding value inside the control.
	 *
	 * @param instance the instance holding the methods
	 * @param view     the view that belongs to the controller
	 */
	private void wireOnUserInputActions(final Object instance, final View view) {
		final List<Method> methods = ReflectionUtils.findMethods(instance.getClass(),
				method -> method.getAnnotation(AFXOnControlValueChange.class) != null);
		methods.sort(AFXONUSERINPUT_COMPARATOR);
		for (final Method method : methods) {
			final AFXOnControlValueChange onUserInput = method.getAnnotation(AFXOnControlValueChange.class);
			final BooleanProperty listenerActionBooleanProperty = lookupBooleanProperty(instance,
					onUserInput.listenerActiveBooleanProperty());
			final ControlWrapper controlWrapper = createControlWrapper(onUserInput.controlId(), view);
			// check, whether the wrapped control supports multi-selection or only single
			// selection
			if (controlWrapper.supportsMultiSelection()) {
				final TimedListChangeListener<?> changeListener = createListChangeListener(instance, method,
						onUserInput.timeoutMs(), listenerActionBooleanProperty, controlWrapper::getSelectedValues,
						controlWrapper::getSelectedValue);
				controlWrapper.addSelectedValuesChangeListener(changeListener);
			} else if (controlWrapper.supportsSelection()) {
				final TimedChangeListener<?> changeListener = createValueChangeListener(instance, method,
						onUserInput.timeoutMs(), listenerActionBooleanProperty);
				controlWrapper.addSelectedValueChangeListener(changeListener);
			} else if (controlWrapper.supportsValue()) {
				final TimedChangeListener<?> changeListener = createValueChangeListener(instance, method,
						onUserInput.timeoutMs(), listenerActionBooleanProperty);
				controlWrapper.addValueChangeListener(changeListener);
			} else if (controlWrapper.supportsValues()) {
				final TimedListChangeListener<?> changeListener = createListChangeListener(instance, method,
						onUserInput.timeoutMs(), listenerActionBooleanProperty, controlWrapper::getValues,
						controlWrapper::getValue);
				controlWrapper.addValuesChangeListener(changeListener);
			} else {
				throw new IllegalStateException("Control with ID='" + onUserInput.controlId()
						+ "' does not support user input listening! Please check your ActionFX annotations inside constroller '"
						+ instance.getClass().getCanonicalName() + "'!");
			}
		}
	}

	/**
	 * Wires methods annotated with {@link AFXLoadControlData} to the corresponding
	 * value inside the control.
	 *
	 * @param instance the instance holding the methods
	 * @param view     the view that belongs to the controller
	 */
	private void wireLoadControlData(final Object instance, final View view) {
		final List<Method> methods = ReflectionUtils.findMethods(instance.getClass(),
				method -> method.getAnnotation(AFXLoadControlData.class) != null);
		methods.sort(AFXLOADCONTROLDATA_COMPARATOR);
		for (final Method method : methods) {
			final AFXLoadControlData loadControlData = method.getAnnotation(AFXLoadControlData.class);
			final BooleanProperty loadingActiveBooleanProperty = lookupBooleanProperty(instance,
					loadControlData.loadingActiveBooleanProperty());
			final ControlWrapper controlWrapper = createControlWrapper(loadControlData.controlId(), view);
			// check, whether the wrapped control supports multi-selection or only single
			// selection
			if (controlWrapper.supportsValues()) {
				populateControlsObservableList(instance, method, loadingActiveBooleanProperty, controlWrapper,
						loadControlData.async());
			} else if (controlWrapper.supportsValue()) {
				populateControlsWritableValue(instance, method, loadingActiveBooleanProperty, controlWrapper,
						loadControlData.async());
			} else {

				throw new IllegalStateException("Control with ID='" + loadControlData.controlId()
						+ "' does not support user input listening! Please check your ActionFX annotations inside constroller '"
						+ instance.getClass().getCanonicalName() + "'!");
			}
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

	/**
	 * Creates a {@link ControlWrapper} for a control identified by
	 * {@code controlId}. The control with the given ID is supposed to by inside the
	 * given {@code view}.
	 *
	 * @param controlId the control ID for that the wrapper shall be created
	 * @param view      the view containing the control with id {@code controlId}
	 * @return the control wrapper instance
	 */
	private ControlWrapper createControlWrapper(final String controlId, final View view) {
		final NodeWrapper wrappedTargetNode = view.lookupNode(controlId);
		if (wrappedTargetNode == null) {
			throw new IllegalStateException("Node with id='" + controlId + "' does not exist!");
		}
		if (!Control.class.isAssignableFrom(wrappedTargetNode.getWrappedType())) {
			throw new IllegalStateException(
					"Node with id='" + controlId + "' is not an instance of javafx.scene.control.Control!");
		}
		return ControlWrapper.of(wrappedTargetNode.getWrapped());
	}

	/**
	 * Looks up a {@link BooleanProperty} inside the given {@code instance}.
	 *
	 * @param instance            the instance
	 * @param booleanPropertyPath the property path (potentially nested) pointing to
	 *                            a {@link BooleanProperty}
	 * @return the looked-up boolean property, or {@code null}, if the property can
	 *         not be looked up
	 */
	private BooleanProperty lookupBooleanProperty(final Object instance, final String booleanPropertyPath) {
		BooleanProperty booleanProperty = null;
		if (!"".equals(booleanPropertyPath)) {
			booleanProperty = (BooleanProperty) ReflectionUtils.getNestedFieldValue(booleanPropertyPath, instance);
		}
		return booleanProperty;
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
	private TimedChangeListener<?> createValueChangeListener(final Object instance, final Method method,
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
	private TimedListChangeListener<?> createListChangeListener(final Object instance, final Method method,
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
	 * Creates a new {@link MethodInvocation} for the supplied {@code method} on the
	 * given {@code instance}.
	 *
	 */
	private ControllerMethodInvocationAdapter createMethodInvocationAdapter(final Object instance, final Method method,
			final ParameterValue... parameterValues) {
		return new ControllerMethodInvocationAdapter(instance, method, parameterValues);
	}

	/**
	 * Enables multi-selection for fields annotated with
	 * {@link AFXEnableMultiSelection}.
	 *
	 * @param instance the instance holding the fields
	 */
	private void enableMultiSelectionControls(final Object instance) {
		applyBiConsumerToFieldsAnnotatedBy(AFXEnableMultiSelection.class, instance,
				(annotation, controlWrapper) -> controlWrapper.enableMultiSelection());
	}

	/**
	 * Enables filtered lists on controls annotated with {@link AFXUseFilteredList}.
	 *
	 * @param instance the instance holding the fields
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void enableFilteredList(final Object instance) {
		applyBiConsumerToFieldsAnnotatedBy(AFXUseFilteredList.class, instance, (annotation, controlWrapper) -> {
			final ObservableList list = controlWrapper.getValues();
			final FilteredList filteredList = new FilteredList(list);
			if (annotation.wrapInSortedList()) {
				controlWrapper.setValues(new SortedList(filteredList));
			} else {
				controlWrapper.setValues(filteredList);
			}
		});
	}

	/**
	 * Applies the given {@link BiConsumer} to fields annotated by
	 * {@link annotationType} inside the given {@code instance}.
	 *
	 * @param annotationType the annotation that is searched for on field-level
	 * @param instance       the instance to check
	 * @param consumer       the bi-consumer applied on the fields (accepting the
	 *                       annotation and the control wrapped in a
	 *                       {@link ControlWrapper})
	 */
	private <A extends Annotation> void applyBiConsumerToFieldsAnnotatedBy(final Class<A> annotationType, final Object instance,
			final BiConsumer<A, ControlWrapper> consumer) {
		final List<Field> annotatedFields = AnnotationUtils.findAllAnnotatedFields(instance.getClass(), annotationType,
				true);
		for (final Field annotatedField : annotatedFields) {
			final A annotation = AnnotationUtils.findAnnotation(annotatedField, annotationType);
			final Object fieldValue = ReflectionUtils.getFieldValue(annotatedField, instance);
			if (fieldValue == null) {
				throw new IllegalStateException("Field '" + annotatedField.getName() + "' in class '"
						+ instance.getClass().getCanonicalName() + "' is annotated by '"
						+ annotationType.getCanonicalName() + "', but field value is 'null'!");
			}
			if (!Control.class.isAssignableFrom(fieldValue.getClass())) {
				throw new IllegalStateException(
						"Field '" + annotatedField.getName() + "' in class '" + instance.getClass().getCanonicalName()
								+ "' is annotated by '" + annotationType.getCanonicalName()
								+ "', but field value is not of type 'javafx.scene.control.Control'!");

			}
			final ControlWrapper controlWrapper = ControlWrapper.of((Control) fieldValue);
			consumer.accept(annotation, controlWrapper);
		}
	}

	/**
	 * Checks for fields annotated by {@link AFXNestedView} and attaches these views
	 * to the nodes.
	 */
	protected void attachNestedViews(final Object controller) {
		final Map<Field, List<AFXNestedView>> annotatedFieldMap = AnnotationUtils
				.findAnnotatedFields(controller.getClass(), AFXNestedView.class, true);
		if (annotatedFieldMap.isEmpty()) {
			return;
		}
		final ActionFX actionFX = ActionFX.getInstance();
		for (final var entry : annotatedFieldMap.entrySet()) {
			final Field field = entry.getKey();
			final List<AFXNestedView> nestedViews = entry.getValue();

			// get views to attach
			for (final AFXNestedView nestedView : nestedViews) {
				final View viewToAttach = actionFX.getView(nestedView.refViewId());
				if (viewToAttach == null) {
					throw new IllegalStateException("Nested view with viewId='" + nestedView.refViewId()
							+ "' does not exist, can not embed it for controller class '"
							+ controller.getClass().getCanonicalName() + "'!");
				}
				final Object fieldValue = ReflectionUtils.getFieldValue(field, controller);
				if (fieldValue == null) {
					throw new IllegalStateException("Nested view with viewId='" + nestedView.refViewId()
							+ "' can not be attached to field with name='" + field.getName()
							+ "', the field value is null!");

				}
				final NodeWrapper target = NodeWrapper.of(fieldValue);
				target.attachNode(viewToAttach.getRootNode(), NodeWrapper.nodeAttacherFor(target, nestedView));
			}
		}
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
