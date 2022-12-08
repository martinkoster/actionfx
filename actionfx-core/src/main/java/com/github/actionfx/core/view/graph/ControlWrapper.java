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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.beans.BeanPropertyReference;
import com.github.actionfx.core.beans.BeanWrapper;
import com.github.actionfx.core.bind.Binding;
import com.github.actionfx.core.bind.ObservableListBinding;
import com.github.actionfx.core.bind.ObservableValueBinding;
import com.github.actionfx.core.collections.ValueChangeAwareObservableList;
import com.github.actionfx.core.container.instantiation.ConstructorBasedInstantiationSupplier;
import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.util.StringConverter;

/**
 * Wrapper around a {@link Control} in order to have a unique access to its values.
 * <p>
 * This wrapper expects a control configuration properties on the classpath in the following location:
 *
 * <pre>
 * /afxcontrolwrapper/<full-canonical-control-class-name>.properties
 * </pre>
 *
 * Example: /afxcontrolwrapper/javafx.scene.control.TextField.properties
 * <p>
 * For third party library integration it is also required to wrap the library-specific "selection model", so that it
 * can be accessed like a {@code SelectionModel}. This is required for example for integrating ControlsFX and their
 * controls using a "CheckModel".
 * <p>
 * In case you need to wrap a third party library "selection model", just create a properties under following location
 * on the classpath:
 *
 * <pre>
 *  afxcontrolwrapper/<full-canonical-classname-of-third-party-selection-model>.properties
 * </pre>
 *
 * Inside the properties, specify a single property "wrapperClass" that holds the fully qualified class name of the
 * wrapper that is expected to implement the JavaFX interface {@link SelectionModel}.
 *
 * @author koster
 **/

public class ControlWrapper extends NodeWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(ControlWrapper.class);

    /**
     * Key to {@link Node#getProperties()} under that an instance of this control wrapper is stored after construction.
     */
    public static final String USER_PROPERTIES_CONTROLWRAPPER_KEY = "actionfx-controlwrapper-instance";

    /**
     * key to the user properties whose value identifies the wrapped control as "required" or not
     */
    public static final String USER_PROPERTIES_REQUIRED_KEY = "actionfx-required";

    /**
     * expected location of the control configuration properties, where "%s" is the full canonical class name of the
     * control.
     */
    public static final String PROPERTIES_TEMPLATE = "/afxcontrolwrapper/%s.properties";

    // cache for control configurations read from external Properties-file
    private static final Map<Class<? extends Control>, ControlConfig> CONTROL_CONFIG_CACHE = Collections
            .synchronizedMap(new HashMap<>());

    // cache for selection model wrapper classes (used for integrating 3rd party
    // libraries like ControlsFX (CheckModel is wrapped)
    private static final Map<Class<?>, Class<? extends SelectionModel<?>>> SELECTION_MODEL_WRAPPER = Collections
            .synchronizedMap(new HashMap<>());

    // the field name of a converter property for controls that support it
    private static final String CONVERTER_PROPERTY_NAME = "converter";

    // property keys that are expected in a control-specific properties file
    private static final String PROPERTY_KEY_VALUE_PROPERTY = "valueProperty";

    private static final String PROPERTY_KEY_ITEMS_OBSERVABLE_LIST = "itemsObservableList";

    private static final String PROPERTY_KEY_SELECTION_MODEL_PROPERTY = "selectionModelProperty";

    // property keys that are expected for a selection model wrapper
    private static final String PROPERTY_KEY_WRAPPER_CLASS = "wrapperClass";

    private final ControlConfig controlConfig;

    // registered change listeners
    private final List<ChangeListener<?>> addedValueChangeListener = new ArrayList<>();

    private final List<ListChangeListener<?>> addedValuesChangeListener = new ArrayList<>();

    private final List<ChangeListener<?>> addedSelectedValueChangeListener = new ArrayList<>();

    private final List<ListChangeListener<?>> addedSelectedValuesChangeListener = new ArrayList<>();

    // properties extracted from the wrapped control - lazily loaded when accessed
    // through the corresponding getter
    private ObservableValue<?> valueProperty;

    private ObservableList<?> valuesObservableList;

    private SelectionModel<?> selectionModel;

    // active bindings for this control

    public ControlWrapper(final Control control) {
        super(control);
        controlConfig = retrieveControlConfig(control.getClass());
        // cache the instance in the node
        control.getProperties().put(USER_PROPERTIES_CONTROLWRAPPER_KEY, this);
    }

    /**
     * Convenient factory method for creating a {@link ControlWrapper} instance.
     * <p>
     * This method tries to retrieve the {@link ControlWrapper} instance from the user properties of the supplied
     * {@link Control}. If there is no instance of type {@link ControlWrapper} present, a new instance is returned.
     * <p>
     * This mechanism ensures that you can work always with the same instance of {@link ControlWrapper} for the same
     * instance of a {@link Control}. This might be especially desirable, when you added listeners to values and you
     * want to remove all listener again through e.g. {@link #removeAllValueChangeListener()} without holding a
     * reference to the added listener.
     *
     * @param control
     *            the control to wrap
     * @return the control wrapper instance
     */
    public static ControlWrapper of(final Control control) {
        ControlWrapper wrapper = (ControlWrapper) control.getProperties().get(USER_PROPERTIES_CONTROLWRAPPER_KEY);
        if (wrapper == null) {
            wrapper = new ControlWrapper(control);
        }
        return wrapper;
    }

    /**
     * Checks, whether the wrapped {@link Control} supports multiple values or not. Multiple values are supported for
     * example by a {@link javafx.scene.control.ListView} with its {@code getItems()} method.
     *
     * @return {@code true}, if the control supports multiple values, {@code false} otherwise.
     */
    public boolean supportsMultipleValues() {
        return controlConfig.hasItemsObservableList();
    }

    /**
     * Gets the current value of the control.
     *
     * @return the current value
     */
    public <V> V getValue() {
        final ObservableValue<V> observableValue = getValueProperty();
        if (observableValue != null) {
            return observableValue.getValue();
        }
        // values are not supported - we return null here...
        return null;
    }

    /**
     * Gets the value property.
     *
     * @param <V>
     *            the value type
     * @return the value property, or {@code null}, in case the control does not have a value property.
     */
    @SuppressWarnings("unchecked")
    public <V> ObservableValue<V> getValueProperty() {
        if (valueProperty == null) {
            final Control control = getWrapped();
            valueProperty = controlConfig.hasValueProperty() ? controlConfig.getValueProperty(control) : null;
        }
        return (ObservableValue<V>) valueProperty;
    }

    /**
     * Get all items from the underlying wrapped {@link Control}.
     *
     * @return all items of the underlying wrapped {@link Control}.
     */
    @SuppressWarnings("unchecked")
    public <V> ObservableList<V> getItems() {
        if (valuesObservableList == null) {
            final Control control = getWrapped();
            if (controlConfig.hasItemsObservableList()) {
                valuesObservableList = controlConfig.getItemsObservableList(control);
            } else {
                // values are not supported - we return an empty list here...
                valuesObservableList = FXCollections.emptyObservableList();
            }
        }
        return (ObservableList<V>) valuesObservableList;
    }

    /**
     * Checks, whether the control supports a {@link SelectionModel} property.
     *
     * @return {@code true}, if the control supports a {@link SelectionModel}, {@code false} otherwise.
     */
    public boolean supportsSelectionModelProperty() {
        return controlConfig.hasSelectionModelProperty();
    }

    /**
     * Checks, whether the control supports a selection of values. This requires that the control exposes a
     * {@link SelectionModel} or something that can be wrapped to a {@link SelectionModel}.
     *
     * @return {@code true}, if the control supports a selection of values, {@code false} otherwise.
     */
    public boolean supportsSelection() {
        return controlConfig.hasSelectionModelProperty();
    }

    /**
     * Checks, whether the wrapped {@link Control} supports multiple selected values or not.
     *
     * @return {@code true}, if the control supports multiple selected values, {@code false} otherwise.
     */
    public boolean supportsMultiSelection() {
        final SelectionModel<?> model = getSelectionModel();
        if (model == null || !MultipleSelectionModel.class.isAssignableFrom(model.getClass())) {
            return false;
        }
        final MultipleSelectionModel<?> multipleSelectionModel = (MultipleSelectionModel<?>) model;
        return multipleSelectionModel.getSelectionMode() == SelectionMode.MULTIPLE;
    }

    /**
     * Checks, whether the wrapped {@link Control} supports a "value" property (e.g.
     * for @{@link javafx.scene.control.TextField} that is the {@link javafx.scene.control.TextField#textProperty()} as
     * configured in the properties file on classpath
     * {@code /afxcontrolwrapper/javafx.scene.control.TextField.properties).}
     *
     * @return {@code true}, if a value property is available and accessible, {@code false} otherwise.
     */
    public boolean supportsValue() {
        return controlConfig.hasValueProperty();
    }

    /**
     * Checks, whether the wrapped {@link Control} supports a "items" property (e.g.
     * for @{@link javafx.scene.control.ChoiceBox} that is the {@link javafx.scene.control.ChoiceBox#itemsProperty()} as
     * configured in the properties file on classpath
     * {@code /afxcontrolwrapper/javafx.scene.control.ChoiceBox.properties).}
     *
     * @return {@code true}, if a value property is available and accessible, {@code false} otherwise.
     */
    public boolean supportsItems() {
        return controlConfig.hasItemsObservableList();
    }

    /**
     * Gets the selection model from the wrapped control. In case the wrapped control does not have a selection model,
     * then {@code null} is returned.
     *
     * @param <V>
     *            the value type
     * @return the selection model, or {@code null}, in case the wrapped control does not have a selection model
     */
    @SuppressWarnings("unchecked")
    public <V> SelectionModel<V> getSelectionModel() {
        if (!supportsSelectionModelProperty()) {
            return null;
        }
        if (selectionModel == null) {
            selectionModel = lookupSelectionModel();
        }
        return (SelectionModel<V>) selectionModel;
    }

    /**
     * Looks up the underlying {@link SelectionModel} from the wrapped control.
     * <p>
     * The field value that is resolved from "selectionModelProperty" can be of the following types:
     * <ul>
     * <li>{@link SelectionModel} or {@code null}: In this case we found our "selection model" and we will return it.
     * <li>any other type: we are checking, if there is a properties-file for this type that holds a
     * "wrapperClass"-property. In this case, the custom selection model is wrapped into that class</li>
     * </ul>
     *
     * @return the looked up {@link SelectionModel}, or {@code null}, in case the property is "empty"
     */
    private SelectionModel<?> lookupSelectionModel() {
        final Control control = getWrapped();
        final Object controlSpecificSelectionModel = controlConfig.getSelectionModel(control);
        if (controlSpecificSelectionModel == null
                || SelectionModel.class.isAssignableFrom(controlSpecificSelectionModel.getClass())) {
            return (SelectionModel<?>) controlSpecificSelectionModel;
        } else {
            // Check for a wrapper class so that we can wrap the unknown "selection model"
            // into a JavaFX SelectionModel
            final Class<? extends SelectionModel<?>> selectionModelWrapperClass = getSelectionModelWrapperClass(
                    controlSpecificSelectionModel.getClass());
            if (selectionModelWrapperClass == null) {
                throw new IllegalStateException("Unable to access class '"
                        + controlSpecificSelectionModel.getClass().getCanonicalName()
                        + "' as a javafx.scene.control.SelectionModel, is the properties configuration for the control to wrap correct?");
            }
            final ConstructorBasedInstantiationSupplier<? extends SelectionModel<?>> instantiationSupplier = new ConstructorBasedInstantiationSupplier<>(
                    selectionModelWrapperClass, controlSpecificSelectionModel, getItems());
            return instantiationSupplier.get();
        }
    }

    /**
     * Gets the currently selected value. In case there are multiple values selected, the latest selected value is
     * returned.
     *
     * @return the currently selected value
     */
    @SuppressWarnings("unchecked")
    public <V> V getSelectedValue() {
        if (controlConfig.hasSelectionModelProperty()) {
            final SelectionModel<?> model = getSelectionModel();
            return model != null ? (V) model.getSelectedItem() : null;
        } else {
            // selection of values are not supported - we return null here...
            return null;
        }
    }

    /**
     * Gets the selected value property.
     *
     * @param <V>
     *            the value type
     * @return the selected value property, or {@code null}, in case the control does not have a selected value
     *         property.
     */
    @SuppressWarnings("unchecked")
    public <V> ObservableValue<V> getSelectedValueProperty() {
        if (controlConfig.hasSelectionModelProperty()) {
            final SelectionModel<?> model = getSelectionModel();
            return model != null ? (ObservableValue<V>) model.selectedItemProperty() : null;
        } else {
            // selection of values are not supported - we return null here...
            return null;
        }
    }

    /**
     * Gets the currently selected values. In case the wrapped {@link Control} does not support multiple values (i.e.
     * method {@link #supportsMultiSelection()} returns {@code false}), then a list with one element is returned, while
     * the element is identical to {@link #getSelectedValue()}.
     *
     * @return the selected values
     */
    @SuppressWarnings("unchecked")
    public <V> ObservableList<V> getSelectedValues() {
        final SelectionModel<?> model = getSelectionModel();
        if (model == null) {
            return FXCollections.emptyObservableList();
        }
        if (MultipleSelectionModel.class.isAssignableFrom(model.getClass())) {
            return ((MultipleSelectionModel<V>) model).getSelectedItems();
        } else {
            final V selectionItem = (V) model.getSelectedItem();
            return selectionItem != null ? FXCollections.observableArrayList(selectionItem)
                    : FXCollections.emptyObservableList();
        }
    }

    /**
     * Gets the user value from the wrapped {@link Control}.
     * <p>
     * The user value is assumed to be one of the following (entries with higher order are of higher priority):
     * <ul>
     * <li>in case the control supports multi-selection, the user value is the list of selected items (e.g. the
     * multi-selection inside a {@link javafx.scene.control.TableView})</li>
     * <li>in case the control supports sinle-selection, the user value is the single selected item (e.g. the
     * single-selection inside a {@link javafx.scene.control.TableView})</li>
     * <li>in case the control supports a single-value, the user value is the single value (e.g. the "text" inside a
     * {@link javafx.scene.control.TextField} control)</li>
     * <li>in case the control supports a multi-values, the user value is the list of values</li>
     * </ul>
     * The first matching rule wins for the extraction of the user value.
     * <p>
     * This method considers values selected via a {@link SelectionModel}.
     */
    public Object getUserValue() {
        final Observable observable = getUserValueAsObservable();
        return getValueFromObservable(observable);
    }

    /**
     * Gets the value from the property described by {@link ControlProperties}.
     * <p>
     * The following values are returned for the specified control property:
     * <ul>
     * <li>{@link ControlProperties#SINGLE_VALUE_PROPERTY}: The simple value, e.g. the text from a
     * {@link javafx.scene.control.TextField} is returned.</li>
     * <li>{@link ControlProperties#ITEMS_OBSERVABLE_LIST}: The items of the control are returned, e.g. all items
     * displayed in a {@link javafx.scene.control.TableView}.</li>
     * <li>{@link ControlProperties#USER_VALUE_OBSERVABLE}: The user value is returned. This can be a single value e.g.
     * for a {@link javafx.scene.control.TextField} or a {@link javafx.scene.control.TableView} with single-selection
     * model, or can be a list for a {@link javafx.scene.control.TableView} with a multi-selection model.</li>
     * </ul>
     *
     * @param controlProperty
     *            the property to retrieve the value from
     * @return the retrieved value
     */
    public Object getValue(final ControlProperties controlProperty) {
        switch (controlProperty) {
        case SINGLE_VALUE_PROPERTY:
            return getValue();
        case ITEMS_OBSERVABLE_LIST:
            return getItemsOrValue();
        case USER_VALUE_OBSERVABLE:
        default:
            return getUserValue();
        }
    }

    /**
     * Checks, whether the wrapped control holds a value (non-null, non-blank) under the given {@code controlProperty}.
     *
     * @param controlProperty
     *            the property that is checked
     * @return {@code true}, if and only if the control holds a non-null, non-blank value under the given
     *         {@code controlProperty}.
     */
    public boolean hasValue(final ControlProperties controlProperty) {
        final Observable observable = getObservable(controlProperty);
        return hasValue(observable);
    }

    /**
     * Checks, whether the given {@code observable} holds a non-null, non-blank value.
     *
     * @param observable
     *            the observable to check
     * @return {@code true}, if and only if the observable's value is non-null and non-blank.
     */
    private boolean hasValue(final Observable observable) {
        if (observable == null) {
            return false;
        }
        if (ObservableValue.class.isAssignableFrom(observable.getClass())) {
            final ObservableValue<?> observableValue = (ObservableValue<?>) observable;
            return observableValueHoldsValue(observableValue);
        } else if (ObservableList.class.isAssignableFrom(observable.getClass())) {
            final ObservableList<?> observableList = (ObservableList<?>) observable;
            return !observableList.isEmpty();
        }
        throw new IllegalStateException(
                "Observable in control '" + getWrappedType().getCanonicalName()
                        + "' is of unknown type '" + observable.getClass().getCanonicalName() + "'!");

    }

    /**
     * Gets the {@link Observable} from the property described by {@link ControlProperties}.
     * <p>
     * The following value of the observable are the following:
     * <ul>
     * <li>{@link ControlProperties#SINGLE_VALUE_PROPERTY}: The simple value, e.g. the text from a
     * {@link javafx.scene.control.TextField} is returned.</li>
     * <li>{@link ControlProperties#ITEMS_OBSERVABLE_LIST}: The items of the control are returned, e.g. all items
     * displayed in a {@link javafx.scene.control.TableView}.</li>
     * <li>{@link ControlProperties#USER_VALUE_OBSERVABLE}: The user value is returned. This can be a single value e.g.
     * for a {@link javafx.scene.control.TextField} or a {@link javafx.scene.control.TableView} with single-selection
     * model, or can be a list for a {@link javafx.scene.control.TableView} with a multi-selection model.</li>
     * </ul>
     *
     * @param controlProperty
     *            the property to retrieve the value from
     * @return the retrieved observable
     */
    public Observable getObservable(final ControlProperties controlProperty) {
        switch (controlProperty) {
        case SINGLE_VALUE_PROPERTY:
            return getValueProperty();
        case ITEMS_OBSERVABLE_LIST:
            return getItems();
        case USER_VALUE_OBSERVABLE:
        default:
            return getUserValueAsObservable();
        }
    }

    /**
     * Gets the items or the value from the wrapped {@link Control}.
     * <p>
     * The value is assumed to be one of the following (entries with higher order are of higher priority):
     * <ul>
     * <li>in case the control supports a single-value, the user value is the single value (e.g. the "text" inside a
     * {@link javafx.scene.control.TextField} control)</li>
     * <li>in case the control supports a multi-values, the user value is the list of values</li>
     * </ul>
     * The first matching rule wins for the extraction of the value.
     * <p>
     * Unlike method {@link #getUserValue()}, this method does not consider selected values through a
     * {@link SelectionModel}.
     */
    public Object getItemsOrValue() {
        final Observable observable = getValueOrItemsAsObservable();
        return getValueFromObservable(observable);
    }

    /**
     * Extracts the actual value from the supplied {@link Observable}.
     *
     * @param observable
     *            the observable to extract the value from
     * @return the extracted value
     */
    protected Object getValueFromObservable(final Observable observable) {
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
     * Gets the user value as an {@link Observable} which is usually either an instance of {@link ObservableValue} or of
     * {@link ObservableList}. However, it is up to the caller to do type checks on the return value.
     * <p>
     * A "user value" is a specifically value set by the user, e.g. by entering text in a text field or by selecting
     * entries in a table view.
     *
     * @return the value as an observable
     */
    public Observable getUserValueAsObservable() {
        if (supportsMultiSelection()) {
            return getSelectedValues();
        } else if (supportsSelection()) {
            return getSelectedValueProperty();
        } else {
            return getValueOrItemsAsObservable();
        }
    }

    /**
     * Gets the value as an {@link Observable} which is usually either an instance of {@link ObservableValue} or of
     * {@link ObservableList}. However, it is up to the caller to do type checks on the return value.
     * <p>
     * A "value" is either a value set by a user (e.g. text in a text field) or the "items" behind e.g. a list/table
     * view or elements inside a choice/combo box.
     *
     * @return the value as an observable
     */
    public Observable getValueOrItemsAsObservable() {
        if (supportsValue()) {
            return getValueProperty();
        } else if (supportsItems()) {
            return getItems();
        } else {
            // no user value supported, we simply return null here
            return null;
        }
    }

    private static boolean isObservableList(final Object observable) {
        return ObservableList.class.isAssignableFrom(observable.getClass());
    }

    private static boolean isObservableValue(final Object observable) {
        return ObservableValue.class.isAssignableFrom(observable.getClass());
    }

    /**
     * Adds a change listener to the value of the wrapped {@link Control}.
     *
     * @param <V>
     *            the value type
     * @param changeListener
     *            the change listener to add
     */
    public <V> void addValueChangeListener(final ChangeListener<V> changeListener) {
        final ObservableValue<V> observableValue = getValueProperty();
        if (observableValue != null) {
            observableValue.addListener(changeListener);
            addedValueChangeListener.add(changeListener);
        }
    }

    /**
     * Removes all attached value change listener.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void removeAllValueChangeListener() {
        final ObservableValue<?> observableValue = getValueProperty();
        if (observableValue != null) {
            for (final ChangeListener listener : addedValueChangeListener) {
                observableValue.removeListener(listener);
            }
            addedValueChangeListener.clear();
        }
    }

    /**
     * Adds a change listener to the items observable list of the wrapped {@link Control}.
     *
     * @param <V>
     *            the value type
     * @param changeListener
     *            the list change listener to add
     */
    public <V> void addItemsChangeListener(final ListChangeListener<V> changeListener) {
        final ObservableList<V> observableList = getItems();
        if (observableList != null) {
            observableList.addListener(changeListener);
            addedValuesChangeListener.add(changeListener);
        }
    }

    /**
     * Removes all attached items change listener.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void removeAllItemsChangeListener() {
        final ObservableList<?> observableList = getItems();
        if (observableList != null) {
            for (final ListChangeListener listener : addedValuesChangeListener) {
                observableList.removeListener(listener);
            }
            addedValuesChangeListener.clear();
        }
    }

    /**
     * Adds a change listener to the selected value of the wrapped {@link Control}.
     *
     * @param <V>
     *            the value type
     * @param changeListener
     *            the change listener to add
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
     * Adds a change listener to the selected values observable list of the wrapped {@link Control}.
     *
     * @param <V>
     *            the value type
     * @param changeListener
     *            the list change listener to add
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
     * Enables multi-selection for the wrapped control. This requires that the control has a selection model property
     * that is of type {@link MultipleSelectionModel}. In case this property is not present or has a different type,
     * calling this method has no effect on the wrapped control.
     */
    public void enableMultiSelection() {
        final SelectionModel<?> model = getSelectionModel();
        if (model == null || !MultipleSelectionModel.class.isAssignableFrom(model.getClass())) {
            return;
        }
        final MultipleSelectionModel<?> multiSelectionModel = (MultipleSelectionModel<?>) model;
        multiSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Sets the given {@code valueList} as "values" to the underlying control. Please note that the underlying control
     * must allow setting an observable list under the "values" attribute.
     *
     * @param <V>
     *            the value type
     * @param valueList
     *            the value list to set
     */
    public <V> void setItems(final ObservableList<V> valueList) {
        if (controlConfig.hasItemsObservableList()) {
            controlConfig.setItemsObservableList(getWrapped(), valueList);
            valuesObservableList = valueList;
        } else {
            throw new IllegalStateException("Control of type '" + getWrappedType().getCanonicalName()
                    + "' has no 'values' property that can be set!");
        }
    }

    /**
     * Returns the "converter" property of a control. In case this property is not supported, {@code null} is returned.
     *
     * @return the "converter" property, or {@code null}, in case the property is not supported by the wrapped control
     */
    @SuppressWarnings("unchecked")
    public ObjectProperty<StringConverter<?>> getConverterProperty() {
        final Field field = ReflectionUtils.findField(getWrappedType(), CONVERTER_PROPERTY_NAME);
        if (field == null) {
            return null;
        }
        final Object value = ReflectionUtils.getFieldValueByPropertyGetter(field, getWrapped());
        if (value == null) {
            return null;
        }
        if (!ObjectProperty.class.isAssignableFrom(value.getClass())) {
            throw new IllegalStateException("Converter property in control of type '"
                    + getWrappedType().getCanonicalName() + "' has type '" + value.getClass().getCanonicalName()
                    + "', expected was type '" + ObjectProperty.class.getCanonicalName() + "'!");
        }
        return (ObjectProperty<StringConverter<?>>) value;
    }

    /**
     * Checks, whether the wrapped control has either items or a value set. A value is e.g. text in a
     * {@link javafx.scene.control.TextField} or items in a {@link javafx.scene.control.TableView} (even when not
     * selected).
     *
     * @return {@code true}, if the wrapped control has values set, {@code false} otherwise.
     */
    public boolean hasValueOrItemsSet() {
        final Observable observable = getValueOrItemsAsObservable();
        return hasValue(observable);
    }

    /**
     * Checks, whether the wrapped control has a value set by the user. User values are e.g. text in a
     * {@link javafx.scene.control.TextField} or selected values in a {@link javafx.scene.control.TableView}.
     *
     * @return {@code true}, if the wrapped control has value set by the user, {@code false} otherwise.
     */
    public boolean hasUserValueSet() {
        return hasValue(ControlProperties.USER_VALUE_OBSERVABLE);
    }

    /**
     * Performs a binding between the attribute referenced by {@code beanPropertyReference} with the control's user
     * value.
     *
     * @param beanPropertyReference
     *            the bean property reference to use as binding target
     * @return the established {@link Binding} instance that can be used to unbind the binding source again
     */
    public Binding bindUserValue(final BeanPropertyReference<?> beanPropertyReference) {
        return bind(beanPropertyReference, ControlProperties.USER_VALUE_OBSERVABLE);
    }

    /**
     * Performs a binding between the attribute referenced by {@code beanPropertyReference} with the control's "items"
     * (e.g. the items in a table view).
     *
     * @param beanPropertyReference
     *            the bean property reference to use as binding target
     * @return the established {@link Binding} instance that can be used to unbind the binding source again
     */
    public Binding bindItemsObservableList(final BeanPropertyReference<?> beanPropertyReference) {
        return bind(beanPropertyReference, ControlProperties.ITEMS_OBSERVABLE_LIST);
    }

    /**
     * Performs a binding between the attribute referenced by {@code beanPropertyReference} with the control's single
     * value (e.g. a text in a text field).
     *
     * @param beanPropertyReference
     *            the bean property reference to use as binding target
     * @return the established {@link Binding} instance that can be used to unbind the binding source again
     */
    public Binding bindSingleValueProperty(final BeanPropertyReference<?> beanPropertyReference) {
        return bind(beanPropertyReference, ControlProperties.SINGLE_VALUE_PROPERTY);
    }

    /**
     * Performs a binding of the control's property with the given {@code beanPropertyReference}. Depending of the data
     * type referenced by the supplied {@link BeanPropertyReference} and the desired target binding property expressed
     * by {@code controlProperty} different types of bindings are created.
     * <p>
     * Bidirectional binding is possible, if the supplied {@code beanPropertyReference} is an {@link Observable} that
     * allows also to write values to it (e.g. a {@link javafx.beans.property.Property}).
     * <p>
     * In case the control's property is a {@link javafx.beans.property.ReadOnlyProperty}, but the control offers a
     * {@link SelectionModel} for manipulating the read-only property, a bidirectional binding is even also possible.
     * This is e.g. the case for selected item(s) in a {@link javafx.scene.control.ListView}.
     * <p>
     * In case only a unidirectional binding is possible (e.g. for a {@link javafx.beans.property.ReadOnlyProperty}) an
     * unidirectional binding is established.
     * <p>
     * In case an unidirectional binding is not possible, e.g. because the supplied {@code beanPropertyReference} is not
     * an {@link Observable}, but e.g. a string, the value of the {@code beanPropertyReference} is just set to the
     * property of the wrapped control and changes in the control will be again reflected in the provided
     * {@code beanPropertyReference} (which corresponds to a unidirectional binding logically).
     *
     * @param beanPropertyReference
     *            the bean property reference to use as binding target
     * @param controlProperty
     *            the control's property that shall be used as binding target
     * @return the established {@link Binding} instance that can be used to unbind the binding source again
     */
    public Binding bind(final BeanPropertyReference<?> beanPropertyReference, final ControlProperties controlProperty) {
        return bind(beanPropertyReference, controlProperty, null);
    }

    /**
     * Performs a binding of the control's property with the given {@code beanPropertyReference}. Depending of the data
     * type referenced by the supplied {@link BeanPropertyReference} and the desired target binding property expressed
     * by {@code controlProperty} different types of bindings are created.
     * <p>
     * Bidirectional binding is possible, if the supplied {@code beanPropertyReference} is an {@link Observable} that
     * allows also to write values to it (e.g. a {@link javafx.beans.property.Property}).
     * <p>
     * In case the control's property is a {@link javafx.beans.property.ReadOnlyProperty}, but the control offers a
     * {@link SelectionModel} for manipulating the read-only property, a bidirectional binding is even also possible.
     * This is e.g. the case for selected item(s) in a {@link javafx.scene.control.ListView}.
     * <p>
     * In case only a unidirectional binding is possible (e.g. for a {@link javafx.beans.property.ReadOnlyProperty}) an
     * unidirectional binding is established.
     * <p>
     * In case an unidirectional binding is not possible, e.g. because the supplied {@code beanPropertyReference} is not
     * an {@link Observable}, but e.g. a string, the value of the {@code beanPropertyReference} is just set to the
     * property of the wrapped control and changes in the control will be again reflected in the provided
     * {@code beanPropertyReference} (which corresponds to a unidirectional binding logically).
     * <p>
     * An additional {@link StringConverter} can be provided to perform type conversion between the
     * {@code beanPropertyReference} the control's property.
     *
     * @param beanPropertyReference
     *            the bean property reference to use as binding target
     * @param controlProperty
     *            the property inside this control acting as binding target
     * @param formatPattern
     *            an optional, nullable format pattern (e.g. for {@link java.text.NumberFormat} or
     *            {@link java.time.format.DateTimeFormatter})
     * @return the established {@link Binding} instance that can be used to unbind the binding source again
     */
    public Binding bind(final BeanPropertyReference<?> beanPropertyReference, final ControlProperties controlProperty,
            final String formatPattern) {
        final Observable observable = getObservable(controlProperty);
        if (isObservableList(observable)) {
            return bindTypeObservableList((ObservableList<?>) observable, beanPropertyReference,
                    controlProperty == ControlProperties.USER_VALUE_OBSERVABLE);
        } else if (isObservableValue(observable)) {
            return bindTypeObservableValue((ObservableValue<?>) observable, beanPropertyReference,
                    controlProperty == ControlProperties.USER_VALUE_OBSERVABLE, formatPattern);
        } else {
            throw new IllegalStateException("Property '" + controlProperty + "' of wrapped control " + getWrappedType()
                    + " is neither an ObservableList nor an ObservableValue, but of type '" + observable.getClass()
                    + "'!");
        }
    }

    /**
     * Binding routine for control properties of type {@link ObservableList}.
     *
     * @param observableList
     *            the observable list of the underlying control to use as binding target
     * @param beanPropertyReference
     *            the bean property reference to use as binding target
     * @param useSelectionModelForBinding
     *            {@code true}, if manipulation of the supplied {@code observableList} shall be done via the selection
     *            model of this control, {@code false}, if manipulation shall be directly performed.
     * @return the {@link Binding} instance that can be used to unbind the binding source again
     */
    @SuppressWarnings("unchecked")
    protected <T> Binding bindTypeObservableList(final ObservableList<T> observableList,
            final BeanPropertyReference<?> beanPropertyReference, final boolean useSelectionModelForBinding) {
        final Object bindingSource = beanPropertyReference.getValue();
        if (bindingSource == null) {
            throw new IllegalStateException("Can not bind 'null' value to an an ObservableList!");
        }
        if (List.class.isAssignableFrom(bindingSource.getClass())) {
            final ObservableListBinding<T> binding = new ObservableListBinding<>((List<T>) bindingSource,
                    observableList, useSelectionModelForBinding ? getSelectionModel() : null);
            binding.bind();
            return binding;
        } else {
            throw new IllegalStateException(
                    "Unable to bind a value of type '" + bindingSource.getClass() + "' to an ObservableList!");
        }
    }

    /**
     * Binding routine for control properties of type {@link ObservableValue}.
     *
     * @param observableValue
     *            the observable value of the underlying control to use as binding target
     * @param beanPropertyReference
     *            the bean property reference to use as binding target
     * @param useSelectionModelForBinding
     *            {@code true}, if manipulation of the supplied {@code observableList} shall be done via the selection
     *            model of this control, {@code false}, if manipulation shall be directly performed.
     * @param formatPattern
     *            an optional, nullable format pattern (e.g. for {@link java.text.NumberFormat} or
     *            {@link java.time.format.DateTimeFormatter})
     * @return the {@link Binding} instance that can be used to unbind the binding source again
     */
    @SuppressWarnings("unchecked")
    protected <T> Binding bindTypeObservableValue(final ObservableValue<T> observableValue,
            final BeanPropertyReference<?> beanPropertyReference, final boolean useSelectionModelForBinding,
            final String formatPattern) {
        final ObservableValueBinding<?, T> binding = new ObservableValueBinding<>(
                (BeanPropertyReference<T>) beanPropertyReference, observableValue,
                useSelectionModelForBinding ? getSelectionModel() : null, formatPattern);
        binding.bind();
        return binding;
    }

    /**
     * Checks, whether the given {@link ObservableValue} has a value set.
     *
     * @param observableValue
     *            the observable value
     * @return {@code true}, if and only if the observable value holds a value
     */
    private boolean observableValueHoldsValue(final ObservableValue<?> observableValue) {
        final Object value = observableValue.getValue();
        if (value == null) {
            return false;
        }
        if (value.getClass() == String.class) {
            return !StringUtils.isBlank((String) value);
        }
        return true;
    }

    /**
     * Identifies the given control as mandatory.
     *
     * @param required
     *            {@code true}, if the control and its value is required, {@code false} otherwise.
     */
    public void setRequired(final boolean required) {
        final Control control = getWrapped();
        control.getProperties().put(USER_PROPERTIES_REQUIRED_KEY, Boolean.valueOf(required));
    }

    /**
     * Checks whether the control is mandatory.
     *
     * @return {@code true}, if the control's value is required, {@code false} otherwise.
     */
    public boolean isRequired() {
        final Control control = getWrapped();
        final Object value = control.getProperties().get(USER_PROPERTIES_REQUIRED_KEY);
        return value instanceof Boolean && ((Boolean) value).booleanValue();
    }

    /**
     * Reads the control configuration from a .properties file. In case there is no control configuration found on the
     * classpath, an empty {@link Properties} instance is returned.
     *
     * @param clazz
     *            the control class for that the configuration shall be looked up
     * @return the loaded properties
     */
    private ControlConfig retrieveControlConfig(final Class<? extends Control> clazz) {
        // check the cache first in order to avoid unnecessary IO-operations
        return CONTROL_CONFIG_CACHE.computeIfAbsent(clazz, controlClass -> {
            final String configPropertiesPath = String.format(PROPERTIES_TEMPLATE, controlClass.getCanonicalName());
            try (InputStream inputStream = ControlWrapper.class.getResourceAsStream(configPropertiesPath)) {
                final Properties controlConfigProperties = new Properties();
                controlConfigProperties.load(inputStream);
                return mapToControlConfig(controlConfigProperties);
            } catch (final Exception e) {
                LOG.warn("File '{}' does not exist or can not be read.", configPropertiesPath);
            }
            return null;
        });
    }

    /**
     * Determines a wrapper class for a third party selection model. This functionality is not used in the ActionFX core
     * library, but is required to integrate further third party libraries like ControlsFX which has a "CheckModel" as
     * "selection model".
     *
     * @param thirdPartySelectionModelClass
     *            the third party selection model class
     * @return the wrapper class, or {@code null}, if there is no wrapper configured.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends SelectionModel<?>> getSelectionModelWrapperClass(
            final Class<?> thirdPartySelectionModelClass) {
        return SELECTION_MODEL_WRAPPER.computeIfAbsent(thirdPartySelectionModelClass, selectionModelClass -> {
            final Set<Class<?>> classesToCheck = ReflectionUtils.getAllSuperClassesAndInterfaces(selectionModelClass);
            for (final Class<?> clazz : classesToCheck) {
                final String configPropertiesPath = String.format(PROPERTIES_TEMPLATE, clazz.getCanonicalName());
                try (InputStream inputStream = ControlWrapper.class.getResourceAsStream(configPropertiesPath)) {
                    if (inputStream == null) {
                        continue;
                    }
                    final Properties controlConfigProperties = new Properties();
                    controlConfigProperties.load(inputStream);
                    return (Class<? extends SelectionModel<?>>) Class
                            .forName(controlConfigProperties.getProperty(PROPERTY_KEY_WRAPPER_CLASS));
                } catch (final IOException e) {
                    LOG.debug("File '{}' does not exist or can not be read.", configPropertiesPath);
                } catch (final ClassNotFoundException e) {
                    LOG.debug("Wrapper class can not be loaded because it does not exist!", e);
                }
            }
            // no properties found?
            return null;
        });
    }

    /**
     * Maps the given properties to an instance of {@link ControlConfig}.
     *
     * @param properties
     *            the control config properties
     * @return the control config
     */
    private ControlConfig mapToControlConfig(final Properties properties) {
        return new ControlConfig(properties.getProperty(PROPERTY_KEY_VALUE_PROPERTY, "").trim(),
                properties.getProperty(PROPERTY_KEY_ITEMS_OBSERVABLE_LIST, "").trim(),
                properties.getProperty(PROPERTY_KEY_SELECTION_MODEL_PROPERTY, "").trim());
    }

    /**
     * Control configuration that is read out from a .properties file.
     *
     * @author koster
     *
     */
    private static class ControlConfig {

        private final String valueProperty;

        private final String itemsObservableList;

        private final String selectionModelProperty;

        public ControlConfig(final String valueProperty, final String itemsObservableList,
                final String selectionModelProperty) {
            this.valueProperty = valueProperty;
            this.itemsObservableList = itemsObservableList;
            this.selectionModelProperty = selectionModelProperty;
        }

        public boolean hasValueProperty() {
            return hasValue(valueProperty);
        }

        public <V> ObservableValue<V> getValueProperty(final Control control) {
            return BeanWrapper.of(control).getFxProperty(valueProperty);
        }

        public boolean hasItemsObservableList() {
            return hasValue(itemsObservableList);
        }

        @SuppressWarnings("unchecked")
        public <V> ObservableList<V> getItemsObservableList(final Control control) {
            // check, if the property holds a comma-separated list of single properties to
            // combine in an observable list
            if (itemsObservableList.contains(",")) {
                final String[] propertyNames = itemsObservableList.split(",");
                final List<ObservableValue<V>> observableValues = new ArrayList<>();
                for (final String propertyName : propertyNames) {
                    final ObservableValue<V> observable = BeanWrapper.of(control).getFxProperty(propertyName.trim());
                    if (observable != null) {
                        observableValues.add(observable);
                    }
                }
                return new ValueChangeAwareObservableList<>(observableValues);
            } else {
                return (ObservableList<V>) BeanWrapper.of(control).getPropertyValue(itemsObservableList);
            }
        }

        public <V> void setItemsObservableList(final Control control, final ObservableList<V> items) {
            final Field field = ReflectionUtils.findField(control.getClass(), itemsObservableList);
            ReflectionUtils.setFieldValueBySetter(field, control, items);
        }

        public boolean hasSelectionModelProperty() {
            return hasValue(selectionModelProperty);
        }

        public Object getSelectionModel(final Control control) {
            return BeanWrapper.of(control).getPropertyValue(selectionModelProperty);
        }

        private boolean hasValue(final String value) {
            return !"".equals(value);
        }
    }
}
