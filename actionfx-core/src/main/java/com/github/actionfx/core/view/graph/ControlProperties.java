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
package com.github.actionfx.core.view.graph;

/**
 * Enumeration for addressing properties inside a {@link javafx.scene.control.Control} in a more general fashion.
 * <p>
 * ActionFX introduces the concept of a "user value", which is a value set by the user (text in
 * {@link javafx.scene.control.TextField}, selected entries in a {@link javafx.scene.control.TableView} or entry entry
 * selected or entered in a {@link javafx.scene.control.ComboBox}).
 * <p>
 * Aside from the "user value", a control has further properties relevant for developers, e.g. the displayed "items" in
 * a {@code TableView}.
 * <p>
 * Because JavaFX holds these values in differently named properties, this enumeration acts as a generalization for
 * referring to these different types of properties in a unique fashion.
 *
 *
 * @author koster
 *
 */
public enum ControlProperties {

    /**
     * Single value property of a control, e.g. {@link javafx.scene.control.TextField#textProperty()} (which is the
     * "user value" at the same time), or {@link javafx.scene.control.TreeView#rootProperty()} (which is not the "user
     * value" - here it is one or multiple selected entries in the tree view).
     * <p>
     * Properties of this type are stored either in a {@link javafx.beans.property.Property} or
     * {@link javafx.beans.value.ObservableValue}.
     */
    SINGLE_VALUE_PROPERTY(0),

    /**
     * Multiple entries/items inside a control, e.g. items in a {@link javafx.scene.control.TableView} or
     * {@link javafx.scene.control.ChoiceBox}. Properties of this type are stored in a
     * {@link javafx.collections.ObservableList}.
     */
    ITEMS_OBSERVABLE_LIST(1),

    /**
     * The user value is a value entered/selected by the user. The property for a user value can be equal to the
     * {@link #SINGLE_VALUE_PROPERTY} or {@link #ITEMS_OBSERVABLE_LIST}, but it can be also one or many selected values
     * through a {@link javafx.scene.control.SelectionModel}.
     * <p>
     * The user value is stored as implementation of {@link javafx.beans.Observable}, which can be e.g. a
     * {@link javafx.beans.property.Property} or a {@link javafx.collections.ObservableList}.
     */
    USER_VALUE_OBSERVABLE(2);

    private int order;

    private ControlProperties(final int order) {
        this.order = order;

    }

    public int getOrder() {
        return order;
    }
}
