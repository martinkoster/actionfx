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
package com.github.actionfx.core.container.instantiation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXOnValueChanged;
import com.github.actionfx.core.annotation.AFXOnValueSelected;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.listener.TimedChangeListener;
import com.github.actionfx.core.listener.TimedListChangeListener;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.MethodInvocationAdapter;
import com.github.actionfx.core.utils.MethodInvocationAdapter.ParameterValue;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.ViewBuilder;
import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Control;

/**
 * Instantiation supplier for controller instances. This class is responsible
 * for instantiating controller classes. In case that ActionFX is configured to
 * use {@link EnhancementStrategy#SUBCLASSING} as enhancement strategy, a
 * dynamic sub-class is created for the supplied controller class by using the
 * configured {@link ActionFXEnhancer}.
 *
 * @author koster
 *
 */
public class ControllerInstantiationSupplier<T> extends AbstractInstantiationSupplier<T> {

	private final Class<T> controllerClass;

	private static final Comparator<Method> AFXONVALUECHANGE_COMPARATOR = new AFXOnValueChangeAnnotatedMethodComparator();

	private static final Comparator<Method> AFXONVALUESELECTED_COMPARATOR = new AFXOnValueSelectedAnnotatedMethodComparator();

	public ControllerInstantiationSupplier(final Class<T> controllerClass) {
		this.controllerClass = prepareControllerClass(controllerClass);
	}

	/**
	 * Prepares the {@code controllerClass} to use. In case the sub-classing
	 * enhancement strategy is configured, a dynamic sub-class is created by using
	 * the configure {@link ActionFXEnhancer}.
	 *
	 * @param controllerClass the controller class to prepare
	 * @return the prepared controller class, potentially sub-classes depending on
	 *         the ActionFX configuration
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> prepareControllerClass(final Class<T> controllerClass) {
		final ActionFX actionFX = ActionFX.getInstance();
		return actionFX.getEnhancementStrategy() == EnhancementStrategy.SUBCLASSING
				? (Class<T>) actionFX.getEnhancer().enhanceClass(controllerClass)
				: controllerClass;
	}

	@Override
	protected T createInstance() {
		try {
			final T controller = controllerClass.getDeclaredConstructor().newInstance();
			final FxmlView fxmlView = createFxmlViewInstance(controller);
			injectView(controller, fxmlView);
			applyFieldLevelAnnotations(controller);
			applyMethodLevelEventAnnotations(controller, fxmlView);
			return controller;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Can not instaniate class '" + controllerClass.getCanonicalName()
					+ "'! Is there a no-arg constructor present?");
		}
	}

	protected FxmlView createFxmlViewInstance(final Object controller) {
		final AFXController afxController = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);
		final FxmlView fxmlView = new FxmlView(afxController.viewId(), afxController.fxml(), controller);
		final ViewBuilder<FxmlView> builder = new ViewBuilder<>(fxmlView);
		return builder.posX(afxController.posX()).posY(afxController.posY()).width(afxController.width())
				.height(afxController.height()).maximized(afxController.maximized())
				.modalDialogue(afxController.modal()).icon(afxController.icon())
				.stylesheets(afxController.stylesheets()).nestedViews(afxController.nestedViews())
				.windowTitle(afxController.title()).getView();
	}

	/**
	 * Injects the view into the supplied controller. Please note that the
	 * controller is expected to be enhanced with a field of name "_view".
	 *
	 * @param controller the controller
	 * @param view       the view
	 */
	protected void injectView(final T controller, final View view) {
		ControllerWrapper.setViewOn(controller, view);
	}

	/**
	 * Applies method-level annotations (e.g. {@link AFXOnValueChanged}.
	 *
	 * @param instance the instance that is checked for ActionFX method level
	 *                 annotations
	 * @param view     the view that belongs to the controller
	 */
	protected void applyMethodLevelEventAnnotations(final Object instance, final View view) {
		enableOnValueChangeActions(instance, view);
		enableOnValueSelectedActions(instance, view);
	}

	/**
	 * Applies field-level annotations (e.g. {@link AFXEnableMultiSelection}.
	 *
	 * @param instance the instance that is checked for ActionFX field level
	 *                 annotations
	 */
	protected void applyFieldLevelAnnotations(final Object instance) {
		enableMultiSelectionControls(instance);
	}

	/**
	 * Wires methods annotated with {@link AFXOnValueChanged} to the corresponding
	 * value inside the control.
	 *
	 * @param instance the instance holding the methods
	 * @param view     the view that belongs to the controller
	 */
	private void enableOnValueChangeActions(final Object instance, final View view) {
		final List<Method> methods = ReflectionUtils.findMethods(instance.getClass(),
				method -> method.getAnnotation(AFXOnValueChanged.class) != null);
		methods.sort(AFXONVALUECHANGE_COMPARATOR);
		for (final Method method : methods) {
			final AFXOnValueChanged onValueChanged = method.getAnnotation(AFXOnValueChanged.class);
			final BooleanProperty listenerActionBooleanProperty = lookupListenerActiveBooleanProperty(instance,
					onValueChanged.listenerActiveBooleanProperty());
			final ControlWrapper controlWrapper = createControlWrapper(onValueChanged.controlId(), view);
			final TimedChangeListener<?> changeListener = createValueChangeListener(instance, method,
					onValueChanged.timeoutMs(), listenerActionBooleanProperty);
			controlWrapper.addValueChangeListener(changeListener);
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
		final NodeWrapper wrappedRootNode = NodeWrapper.of(view.getRootNode());
		final NodeWrapper wrappedTargetNode = wrappedRootNode.lookup(controlId);
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
	 * @param instance                          the instance
	 * @param listenerActiveBooleanPropertyPath the property path (potentially
	 *                                          nested) pointing to a
	 *                                          {@link BooleanProperty}
	 * @return the looked-up boolean property, or {@code null}, if the property can
	 *         not be looked up
	 */
	private BooleanProperty lookupListenerActiveBooleanProperty(final Object instance,
			final String listenerActiveBooleanPropertyPath) {
		BooleanProperty listenerActionBooleanProperty = null;
		if (!"".equals(listenerActiveBooleanPropertyPath)) {
			listenerActionBooleanProperty = (BooleanProperty) ReflectionUtils
					.getNestedFieldValue(listenerActiveBooleanPropertyPath, instance);
		}
		return listenerActionBooleanProperty;
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
			final MethodInvocationAdapter adapter = new MethodInvocationAdapter(instance, method,
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
	 * @param valuesSupplier                a supplier that gets the values to
	 *                                      retrieve
	 * @return
	 */
	private TimedListChangeListener<?> createListChangeListener(final Object instance, final Method method,
			final long timeoutMs, final BooleanProperty listenerActionBooleanProperty,
			final Supplier<?> valuesSupplier) {
		return new TimedListChangeListener<>(change -> {
			final List<Object> addedList = new ArrayList<>();
			final List<Object> removedList = new ArrayList<>();
			while (change.next()) {
				if (change.wasAdded()) {
					addedList.addAll(change.getAddedSubList());
				} else if (change.wasRemoved()) {
					removedList.addAll(change.getRemoved());
				}
			}
			change.reset();
			final MethodInvocationAdapter adapter = new MethodInvocationAdapter(instance, method,
					ParameterValue.ofAllSelectedValues(valuesSupplier.get()), ParameterValue.ofAddedValues(addedList),
					ParameterValue.ofRemovedValues(removedList), ParameterValue.of(change));
			adapter.invoke();
		}, timeoutMs, listenerActionBooleanProperty);
	}

	/**
	 * Wires methods annotated with {@link AFXOnValueSelected} to the corresponding
	 * value inside the control.
	 *
	 * @param instance the instance holding the methods
	 * @param view     the view that belongs to the controller
	 */
	private void enableOnValueSelectedActions(final Object instance, final View view) {
		final List<Method> methods = ReflectionUtils.findMethods(instance.getClass(),
				method -> method.getAnnotation(AFXOnValueSelected.class) != null);
		methods.sort(AFXONVALUESELECTED_COMPARATOR);
		for (final Method method : methods) {
			final AFXOnValueSelected onValueSelected = method.getAnnotation(AFXOnValueSelected.class);
			final BooleanProperty listenerActionBooleanProperty = lookupListenerActiveBooleanProperty(instance,
					onValueSelected.listenerActiveBooleanProperty());
			final ControlWrapper controlWrapper = createControlWrapper(onValueSelected.controlId(), view);
			// check, whether the wrapped control supports multi-selection or only single
			// selection
			if (controlWrapper.supportsMultiSelection()) {
				final TimedListChangeListener<?> changeListener = createListChangeListener(instance, method,
						onValueSelected.timeoutMs(), listenerActionBooleanProperty, controlWrapper::getSelectedValues);
				controlWrapper.addSelectedValuesChangeListener(changeListener);
			} else if (controlWrapper.supportsSelection()) {
				final TimedChangeListener<?> changeListener = createValueChangeListener(instance, method,
						onValueSelected.timeoutMs(), listenerActionBooleanProperty);
				controlWrapper.addSelectedValueChangeListener(changeListener);
			} else {
				throw new IllegalStateException("Control with ID='" + onValueSelected.controlId()
						+ "' does not support selection! Please check your ActionFX annotations inside constroller '"
						+ instance.getClass().getCanonicalName() + "'!");
			}
		}
	}

	/**
	 * Enables multi-selection for fields annotated with
	 * {@link AFXEnableMultiSelection}.
	 *
	 * @param instance the instance holding the fields
	 */
	private void enableMultiSelectionControls(final Object instance) {
		final List<Field> enableMultiSelectionFields = AnnotationUtils.findAllAnnotatedFields(instance.getClass(),
				AFXEnableMultiSelection.class, true);
		for (final Field enableMultiSelectionField : enableMultiSelectionFields) {
			final Object fieldValue = ReflectionUtils.getFieldValue(enableMultiSelectionField, instance);
			if (fieldValue == null) {
				throw new IllegalStateException("Field '" + enableMultiSelectionField.getName() + "' in class '"
						+ instance.getClass().getCanonicalName()
						+ "' is annotated by @AFXEnableMultiSelection, but field value is 'null'!");
			}
			if (!Control.class.isAssignableFrom(fieldValue.getClass())) {
				throw new IllegalStateException("Field '" + enableMultiSelectionField.getName() + "' in class '"
						+ instance.getClass().getCanonicalName()
						+ "' is annotated by @AFXEnableMultiSelection, but field value is not of type 'javafx.scene.control.Control'!");

			}
			final ControlWrapper controlWrapper = new ControlWrapper((Control) fieldValue);
			controlWrapper.enableMultiSelection();
		}
	}

	/**
	 * Comparator implementation that orders methods according to the "order"
	 * attribute among same controls.
	 *
	 * @author koster
	 *
	 */
	public static class AFXOnValueChangeAnnotatedMethodComparator implements Comparator<Method> {

		@Override
		public int compare(final Method o1, final Method o2) {
			final AFXOnValueChanged c1 = o1.getAnnotation(AFXOnValueChanged.class);
			final AFXOnValueChanged c2 = o2.getAnnotation(AFXOnValueChanged.class);
			return Comparator.comparing(AFXOnValueChanged::controlId).thenComparing(AFXOnValueChanged::order)
					.compare(c1, c2);
		}
	}

	/**
	 * Comparator implementation that orders methods according to the "order"
	 * attribute among same controls.
	 *
	 * @author koster
	 *
	 */
	public static class AFXOnValueSelectedAnnotatedMethodComparator implements Comparator<Method> {

		@Override
		public int compare(final Method o1, final Method o2) {
			final AFXOnValueSelected c1 = o1.getAnnotation(AFXOnValueSelected.class);
			final AFXOnValueSelected c2 = o2.getAnnotation(AFXOnValueSelected.class);
			return Comparator.comparing(AFXOnValueSelected::controlId).thenComparing(AFXOnValueSelected::order)
					.compare(c1, c2);
		}
	}

}
