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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;

/**
 * Custom control for testing.
 *
 * @author koster
 *
 */
public class CustomControl extends Control {

	private StringProperty customValue;

	private StringProperty valueOne;

	private StringProperty valueTwo;

	private StringProperty valueThree;

	// custom selection model that does not implement
	// "javafx.scene.control.SelectionModel". The instance is wrapped!
	private ObjectProperty<CustomSelectionModel> customSelectionModel;

	public final StringProperty customValueProperty() {
		if (customValue == null) {
			customValue = new SimpleStringProperty();
		}
		return customValue;
	}

	public final String getCustomValue() {
		return customValueProperty().get();
	}

	public final void setCustomValue(final String value) {
		customValueProperty().set(value);
	}

	public final ObjectProperty<CustomSelectionModel> customSelectionModelProperty() {
		if (customSelectionModel == null) {
			customSelectionModel = new SimpleObjectProperty<>(new CustomSelectionModel());
		}
		return customSelectionModel;
	}

	public final CustomSelectionModel getCustomSelectionModel() {
		return customSelectionModelProperty().get();
	}

	public final void setCustomSelectionModel(final CustomSelectionModel customSelectionModel) {
		customSelectionModelProperty().set(customSelectionModel);
	}

	public final StringProperty valueOneProperty() {
		if (valueOne == null) {
			valueOne = new SimpleStringProperty("");
		}
		return valueOne;
	}

	public final String getValueOne() {
		return valueOneProperty().get();
	}

	public final void setValueOne(final String valueOne) {
		valueOneProperty().set(valueOne);
	}

	public final StringProperty valueTwoProperty() {
		if (valueTwo == null) {
			valueTwo = new SimpleStringProperty("");
		}
		return valueTwo;
	}

	public final String getValueTwo() {
		return valueTwoProperty().get();
	}

	public final void setValueTwo(final String valueTwo) {
		valueTwoProperty().set(valueTwo);
	}

	public final StringProperty valueThreeProperty() {
		if (valueThree == null) {
			valueThree = new SimpleStringProperty("");
		}
		return valueThree;
	}

	public final String getValueThree() {
		return valueThreeProperty().get();
	}

	public final void setValueThree(final String valueThree) {
		valueThreeProperty().set(valueThree);
	}

}
