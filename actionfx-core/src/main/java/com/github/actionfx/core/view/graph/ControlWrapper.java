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
package com.github.actionfx.core.view.graph;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;

/**
 * Wrapper around a {@link Control} in order to have a unique access to its
 * values.
 * <p>
 * This wrapper expects a control configuration properties on the classpath in
 * the following location:
 *
 * <pre>
 * /afxcontrolwrapper/<full-canonical-control-class-name>.properties
 * </pre>
 *
 * Example: /afxcontrolwrapper/javafx.scene.control.TextField.properties
 *
 * @author koster
 *
 */
public class ControlWrapper extends NodeWrapper {

	private static final Logger LOG = LoggerFactory.getLogger(ControlWrapper.class);

	// cache for control configurations read from external Properties-file
	private static final Map<Class<? extends Control>, ControlConfig> CONTROL_CONFIG_CACHE = Collections
			.synchronizedMap(new HashMap<>());

	// the field name of the "onAction" property inside controls that do support it
	private static final String ON_ACTION_FIELD_NAME = "onAction";

	private static final String PROPERTY_KEY_VALUE_PROPERTY = "valueProperty";
	private static final String PROPERTY_KEY_VALUES_OBSERVABLE_LIST = "valuesObservableList";
	private static final String PROPERTY_KEY_SELECTION_MODEL_PROPERTY = "selectionModelProperty";
	private static final String PROPERTY_KEY_SELECTED_VALUE_PROPERTY = "selectedValueProperty";
	private static final String PROPERTY_KEY_SELECTED_VALUES_OBSERVABLE_LIST = "selectedValuesObservableList";

	// expected location of the control configurationn properties, where "%s" is the
	// full canonical class name of the control
	public static final String CONTROL_PROPERTIES_PATH = "/afxcontrolwrapper/%s.properties";

	private final ControlConfig controlConfig;

	// registered change listeners
	private final List<ChangeListener<?>> addedValueChangeListener = new ArrayList<>();
	private final List<ListChangeListener<?>> addedValuesChangeListener = new ArrayList<>();
	private final List<ChangeListener<?>> addedSelectedValueChangeListener = new ArrayList<>();
	private final List<ListChangeListener<?>> addedSelectedValuesChangeListener = new ArrayList<>();

	public ControlWrapper(final Control control) {
		super(control);
		controlConfig = retrieveControlConfig(control.getClass());
	}

	/**
	 * Convenient factory method for creating a {@link ControlWrapper} instance.
	 *
	 * @param control the control to wrap
	 * @return the control wrapper instance
	 */
	public static ControlWrapper of(final Control control) {
		return new ControlWrapper(control);
	}

	/**
	 * Checks, whether the wrapped {@link Control} supports multiple values or not.
	 * Multiple values are supported for example by a {@link ListView} with its
	 * {@code getItems()} method.
	 *
	 * @return {@code true}, if the control supports multiple values, {@code false}
	 *         otherwise.
	 */
	public boolean supportsMultipleValues() {
		return controlConfig.hasValuesObservableList();
	}

	/**
	 * Gets the current value of the control.
	 *
	 * @return the current value
	 */
	public <V> V getValue() {
		final Control control = getWrapped();
		if (controlConfig.hasValueProperty()) {
			final Property<V> property = controlConfig.getValueProperty(control);
			return property.getValue();
		} else {
			// values are not supported - we return null here...
			return null;
		}
	}

	/**
	 * Gets the value property.
	 *
	 * @param <V> the value type
	 * @return the value property, or {@code null}, in case the control does not
	 *         have a value property.
	 */
	public <V> ObservableValue<V> getValueProperty() {
		final Control control = getWrapped();
		return controlConfig.hasValueProperty() ? controlConfig.getValueProperty(control) : null;
	}

	/**
	 * Get all values from the underlying wrapped {@link Control}.
	 *
	 * @return all allowed values of the underlying wrapped {@link Control}.
	 */
	public <V> ObservableList<V> getValues() {
		final Control control = getWrapped();
		if (controlConfig.hasValuesObservableList()) {
			return controlConfig.getValuesObservableList(control);
		} else {
			// values are not supported - we return an empty list here...
			return FXCollections.emptyObservableList();
		}
	}

	/**
	 * Checks, whether the control supports a {@link SelectionModel} property.
	 *
	 * @return {@code true}, if the control supports a {@link SelectionModel},
	 *         {@code false} otherwise.
	 */
	public boolean supportsSelectionModelProperty() {
		return controlConfig.hasSelectionModelProperty();
	}

	/**
	 * Checks, whether the control supports a selection of values.
	 *
	 * @return {@code true}, if the control supports a selection of values,
	 *         {@code false} otherwise.
	 */
	public boolean supportsSelection() {
		return controlConfig.hasSelectionModelProperty() || controlConfig.hasSelectedValueProperty()
				|| controlConfig.hasSelectedValuesObservableList();
	}

	/**
	 * Checks, whether the wrapped {@link Control} supports multiple selected values
	 * or not.
	 *
	 * @return {@code true}, if the control supports multiple selected values,
	 *         {@code false} otherwise.
	 */
	public boolean supportsMultiSelection() {
		final SelectionModel<?> selectionModel = getSelectionModel();
		if (selectionModel == null || !MultipleSelectionModel.class.isAssignableFrom(selectionModel.getClass())) {
			return false;
		}
		final MultipleSelectionModel<?> multipleSelectionModel = (MultipleSelectionModel<?>) selectionModel;
		return multipleSelectionModel.getSelectionMode() == SelectionMode.MULTIPLE;
	}

	/**
	 * Checks, whether the wrapped {@link Control} supports a "value" property (e.g.
	 * for @{@link javafx.scene.control.TextField} that is the
	 * {@link javafx.scene.control.TextField#textProperty()} as configured in the
	 * properties file on classpath
	 * {@code /afxcontrolwrapper/javafx.scene.control.TextField.properties).}
	 *
	 * @return {@code true}, if a value property is available and accessible,
	 *         {@code false} otherwise.
	 */
	public boolean supportsValue() {
		return controlConfig.hasValueProperty();
	}

	/**
	 * Checks, whether the wrapped {@link Control} supports a "values" property
	 * (e.g. for @{@link javafx.scene.control.ChoiceBox} that is the
	 * {@link javafx.scene.control.ChoiceBox#itemsProperty()} as configured in the
	 * properties file on classpath
	 * {@code /afxcontrolwrapper/javafx.scene.control.ChoiceBox.properties).}
	 *
	 * @return {@code true}, if a value property is available and accessible,
	 *         {@code false} otherwise.
	 */
	public boolean supportsValues() {
		return controlConfig.hasValuesObservableList();
	}

	/**
	 * Gets the selection model from the wrapped control. In case the wrapped
	 * control does not have a selection model, then {@code null} is returned.
	 *
	 * @param <V> the value type
	 * @return the selection model, or {@code null}, in case the wrapped control
	 *         does not have a selection model
	 */
	@SuppressWarnings("unchecked")
	public <V> SelectionModel<V> getSelectionModel() {
		if (!supportsSelectionModelProperty()) {
			return null;
		}
		final Control control = getWrapped();
		final Property<SelectionModel<Object>> selectionModelProperty = controlConfig
				.getSelectionModelProperty(control);
		return (SelectionModel<V>) selectionModelProperty.getValue();
	}

	/**
	 * Gets the currently selected value. In case there are multiple values
	 * selected, the latest selected value is returned.
	 *
	 * @return the currently selected value
	 */
	public <V> V getSelectedValue() {
		final Control control = getWrapped();
		if (controlConfig.hasSelectedValueProperty()) {
			final ObservableValue<V> property = controlConfig.getSelectedValueProperty(control);
			return property.getValue();
		} else if (controlConfig.hasSelectedValuesObservableList()) {
			final ObservableList<V> list = controlConfig.getSelectedValuesObservableList(control);
			return list != null && !list.isEmpty() ? list.get(0) : null;
		} else {
			// values are not supported - we return null here...
			return null;
		}
	}

	/**
	 * Gets the selected value property.
	 *
	 * @param <V> the value type
	 * @return the selected value property, or {@code null}, in case the control
	 *         does not have a selected value property.
	 */
	public <V> ObservableValue<V> getSelectedValueProperty() {
		final Control control = getWrapped();
		return controlConfig.hasSelectedValueProperty() ? controlConfig.getSelectedValueProperty(control) : null;
	}

	/**
	 * Gets the currently selected values. In case the wrapped {@link Control} does
	 * not support multiple values (i.e. method {@link #supportsMultiselection()}
	 * returns {@code false}), then a list with one element is returned, while the
	 * element is identical to {@link #getSelectedValue()}.
	 *
	 * @return the selected values
	 */
	@SuppressWarnings("unchecked")
	public <V> ObservableList<V> getSelectedValues() {
		final Control control = getWrapped();
		if (controlConfig.hasSelectedValuesObservableList()) {
			return controlConfig.getSelectedValuesObservableList(control);
		} else if (controlConfig.hasSelectedValueProperty()) {
			final ObservableValue<V> property = controlConfig.getSelectedValueProperty(control);
			final V value = property.getValue();
			if (value == null) {
				return FXCollections.emptyObservableList();
			} else {
				return FXCollections.observableArrayList(value);
			}
		} else {
			// values are not supported - we return an empty list here...
			return FXCollections.emptyObservableList();
		}
	}

	/**
	 * Gets the user value from the wrapped {@link Control}.
	 * <p>
	 * The user value is assumed to be one of the following (entries with higher
	 * order are of higher priority):
	 * <ul>
	 * <li>in case the control supports multi-selection, the user value is the list
	 * of selected items (e.g. the multi-selection inside a
	 * {@link javafx.scene.control.TableView})</li>
	 * <li>in case the control supports sinle-selection, the user value is the
	 * single selected item (e.g. the single-selection inside a
	 * {@link javafx.scene.control.TableView})</li>
	 * <li>in case the control supports a single-value, the user value is the single
	 * value (e.g. the "text" inside a {@link javafx.scene.control.TextField}
	 * control)</li>
	 * <li>in case the control supports a multi-values, the user value is the list
	 * of values</li>
	 * </ul>
	 * The first matching rule wins for the extraction of the user value.
	 */
	public Object getUserValue() {
		final Object observable = getUserValueAsObservable();
		if (observable == null) {
			return null;
		}
		if (isObservableList(observable)) {
			// observableList is value and observable at the same time
			return observable;
		} else if (isObservableValue(observable)) {
			// for ObservableValues, we need to extract the value
			final ObservableValue<?> observableValue = (ObservableValue<?>) observable;
			return observableValue.getValue();
		} else {
			// we don't know the type...but we return it. it is up to the caller to decide
			// what to do
			return observable;
		}
	}

	/**
	 * Gets the user value as an observable, which is usually either an instance of
	 * {@link ObservableValue} or of {@link ObservableList}. However, it is up to
	 * the caller to do type checks on the return value.
	 *
	 * @return the value as an observable
	 */
	public Object getUserValueAsObservable() {
		if (supportsMultiSelection()) {
			return getSelectedValues();
		} else if (supportsSelection()) {
			return getSelectedValue();
		} else if (supportsValue()) {
			return getValueProperty();
		} else if (supportsValues()) {
			return getValues();
		} else {
			throw new IllegalStateException(
					"Control with ID='" + getId() + "' does not support user value retrieval! ");
		}
	}

	private boolean isObservableList(final Object observable) {
		return ObservableList.class.isAssignableFrom(observable.getClass());
	}

	private boolean isObservableValue(final Object observable) {
		return ObservableValue.class.isAssignableFrom(observable.getClass());
	}

	/**
	 * Adds a change listener to the value of the wrapped {@link Control}.
	 *
	 * @param <V>            the value type
	 * @param changeListener the change listener to add
	 */
	public <V> void addValueChangeListener(final ChangeListener<V> changeListener) {
		final ObservableValue<V> valueProperty = getValueProperty();
		if (valueProperty != null) {
			valueProperty.addListener(changeListener);
			addedValueChangeListener.add(changeListener);
		}
	}

	/**
	 * Removes all attached value change listener.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void removeAllValueChangeListener() {
		final ObservableValue<?> valueProperty = getValueProperty();
		if (valueProperty != null) {
			for (final ChangeListener listener : addedValueChangeListener) {
				valueProperty.removeListener(listener);
			}
			addedValueChangeListener.clear();
		}
	}

	/**
	 * Adds a change listener to the values observable list of the wrapped
	 * {@link Control}.
	 *
	 * @param <V>            the value type
	 * @param changeListener the list change listener to add
	 */
	public <V> void addValuesChangeListener(final ListChangeListener<V> changeListener) {
		final ObservableList<V> valuesObservableList = getValues();
		if (valuesObservableList != null) {
			valuesObservableList.addListener(changeListener);
			addedValuesChangeListener.add(changeListener);
		}
	}

	/**
	 * Removes all attached values change listener.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void removeAllValuesChangeListener() {
		final ObservableList<?> valuesObservableList = getValues();
		if (valuesObservableList != null) {
			for (final ListChangeListener listener : addedValuesChangeListener) {
				valuesObservableList.removeListener(listener);
			}
			addedValuesChangeListener.clear();
		}
	}

	/**
	 * Adds a change listener to the selected value of the wrapped {@link Control}.
	 *
	 * @param <V>            the value type
	 * @param changeListener the change listener to add
	 */
	public <V> void addSelectedValueChangeListener(final ChangeListener<V> changeListener) {
		final ObservableValue<V> selectedValueProperty = getSelectedValueProperty();
		if (selectedValueProperty != null) {
			selectedValueProperty.addListener(changeListener);
			addedSelectedValueChangeListener.add(changeListener);
		}
	}

	/**
	 * Removes all attached selected value change listener.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void removeAllSelectedValueChangeListener() {
		final ObservableValue<?> selectedValueProperty = getSelectedValueProperty();
		if (selectedValueProperty != null) {
			for (final ChangeListener listener : addedSelectedValueChangeListener) {
				selectedValueProperty.removeListener(listener);
			}
			addedSelectedValueChangeListener.clear();
		}
	}

	/**
	 * Adds a change listener to the selected values observable list of the wrapped
	 * {@link Control}.
	 *
	 * @param <V>            the value type
	 * @param changeListener the list change listener to add
	 */
	public <V> void addSelectedValuesChangeListener(final ListChangeListener<V> changeListener) {
		final ObservableList<V> selecedValuesObservableList = getSelectedValues();
		if (selecedValuesObservableList != null) {
			selecedValuesObservableList.addListener(changeListener);
			addedSelectedValuesChangeListener.add(changeListener);
		}
	}

	/**
	 * Removes all attached selected values change listener.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void removeAllSelectedValuesChangeListener() {
		final ObservableList<?> selecedValuesObservableList = getSelectedValues();
		if (selecedValuesObservableList != null) {
			for (final ListChangeListener listener : addedSelectedValuesChangeListener) {
				selecedValuesObservableList.removeListener(listener);
			}
			addedSelectedValuesChangeListener.clear();
		}
	}

	/**
	 * Enables multi-selection for the wrapped control. This requires that the
	 * control has a selection model property that is of type
	 * {@link MultipleSelectionModel}. In case this property is not present or has a
	 * different type, calling this method has no effect on the wrapped control.
	 */
	public void enableMultiSelection() {
		final SelectionModel<?> selectionModel = getSelectionModel();
		if (selectionModel == null || !MultipleSelectionModel.class.isAssignableFrom(selectionModel.getClass())) {
			return;
		}
		final MultipleSelectionModel<?> multiSelectionModel = (MultipleSelectionModel<?>) selectionModel;
		multiSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
	}

	/**
	 * Returns the "on action" property of a control. In case this property is not
	 * supported, {@code null} is returned.
	 *
	 * @return the "on action" property, or {@code null}, in case the property is
	 *         not supported by the wrapped control
	 */
	@SuppressWarnings("unchecked")
	public ObjectProperty<EventHandler<ActionEvent>> getOnActionProperty() {
		final Field field = ReflectionUtils.findField(getWrappedType(), ON_ACTION_FIELD_NAME);
		if (field == null) {
			return null;
		}
		final Object value = ReflectionUtils.getFieldValueByPropertyGetter(field, getWrapped());
		if (value == null) {
			return null;
		}
		if (!ObjectProperty.class.isAssignableFrom(value.getClass())) {
			throw new IllegalStateException("OnAction property in control of type '"
					+ getWrappedType().getCanonicalName() + "' has type '" + value.getClass().getCanonicalName()
					+ "', expected was type '" + ObjectProperty.class.getCanonicalName() + "'!");
		}
		return (ObjectProperty<EventHandler<ActionEvent>>) value;
	}

	/**
	 * Reads the control configuration from a .properties file. In case there is no
	 * control configuration found on the classpath, an empty {@link Properties}
	 * instance is returned.
	 *
	 * @param clazz the control class for that the configuration shall be looked up
	 * @return the loaded properties
	 */
	private ControlConfig retrieveControlConfig(final Class<? extends Control> clazz) {
		// check the cache first in order to avoid unnecessary IO-operations
		if (CONTROL_CONFIG_CACHE.containsKey(clazz)) {
			return CONTROL_CONFIG_CACHE.get(clazz);
		}
		ControlConfig result = null;
		final String configPropertiesPath = String.format(CONTROL_PROPERTIES_PATH, clazz.getCanonicalName());
		try (InputStream inputStream = ControlWrapper.class.getResourceAsStream(configPropertiesPath)) {
			final Properties controlConfigProperties = new Properties();
			controlConfigProperties.load(inputStream);
			result = mapToControlConfig(controlConfigProperties);
		} catch (final IOException e) {
			LOG.warn("File '{}' does not exist or can not be read.", configPropertiesPath);
		}
		CONTROL_CONFIG_CACHE.put(clazz, result);
		return result;
	}

	/**
	 * Maps the given properties to an instance of {@link ControlConfig}.
	 *
	 * @param properties the control config properties
	 * @return the control config
	 */
	private ControlConfig mapToControlConfig(final Properties properties) {
		return new ControlConfig(properties.getProperty(PROPERTY_KEY_VALUE_PROPERTY, ""),
				properties.getProperty(PROPERTY_KEY_VALUES_OBSERVABLE_LIST, ""),
				properties.getProperty(PROPERTY_KEY_SELECTION_MODEL_PROPERTY, ""),
				properties.getProperty(PROPERTY_KEY_SELECTED_VALUE_PROPERTY, ""),
				properties.getProperty(PROPERTY_KEY_SELECTED_VALUES_OBSERVABLE_LIST, ""));
	}

	/**
	 * Control configuration that is read out from a .properties file.
	 *
	 * @author koster
	 *
	 */
	private static class ControlConfig {

		private final String valueProperty;

		private final String valuesObservableList;

		private final String selectionModelProperty;

		private final String selectedValueProperty;

		private final String selectedValuesObservableList;

		public ControlConfig(final String valueProperty, final String valuesObservableList,
				final String selectionModelProperty, final String selectedValueProperty,
				final String selectedValuesObservableList) {
			this.valueProperty = valueProperty;
			this.valuesObservableList = valuesObservableList;
			this.selectionModelProperty = selectionModelProperty;
			this.selectedValueProperty = selectedValueProperty;
			this.selectedValuesObservableList = selectedValuesObservableList;
		}

		public String getValueProperty() {
			return valueProperty;
		}

		public boolean hasValueProperty() {
			return hasValue(getValueProperty());
		}

		public <V> Property<V> getValueProperty(final Control control) {
			return ReflectionUtils.getNestedFieldProperty(valueProperty, control);
		}

		public String getValuesObservableList() {
			return valuesObservableList;
		}

		public boolean hasValuesObservableList() {
			return hasValue(getValuesObservableList());
		}

		@SuppressWarnings("unchecked")
		public <V> ObservableList<V> getValuesObservableList(final Control control) {
			return ReflectionUtils.getNestedFieldValue(valuesObservableList, control, ObservableList.class);
		}

		public String getSelectionModelProperty() {
			return selectionModelProperty;
		}

		public boolean hasSelectionModelProperty() {
			return hasValue(getSelectionModelProperty());
		}

		public <V> Property<SelectionModel<V>> getSelectionModelProperty(final Control control) {
			return ReflectionUtils.getNestedFieldProperty(selectionModelProperty, control);
		}

		public String getSelectedValueProperty() {
			return selectedValueProperty;
		}

		public boolean hasSelectedValueProperty() {
			return hasValue(getSelectedValueProperty());
		}

		@SuppressWarnings("unchecked")
		public <V> ObservableValue<V> getSelectedValueProperty(final Control control) {
			return ReflectionUtils.getNestedFieldValue(selectedValueProperty, control, ObservableValue.class);
		}

		public String getSelectedValuesObservableList() {
			return selectedValuesObservableList;
		}

		public boolean hasSelectedValuesObservableList() {
			return hasValue(getSelectedValuesObservableList());
		}

		@SuppressWarnings("unchecked")
		public <V> ObservableList<V> getSelectedValuesObservableList(final Control control) {
			return ReflectionUtils.getNestedFieldValue(selectedValuesObservableList, control, ObservableList.class);
		}

		private boolean hasValue(final String value) {
			return !"".equals(value);
		}
	}

}
