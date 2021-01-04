package com.github.actionfx.core.container.instantiation;

import java.util.ArrayList;
import java.util.List;

import com.github.actionfx.core.annotation.AFXArgHint;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXOnValueChanged;
import com.github.actionfx.core.annotation.ArgumentHint;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

@AFXController(viewId = "testId", fxml = "/testfxml/SampleViewWithListener.fxml", icon = "icon.png", singleton = true, maximized = true, modal = false, title = "Hello World", width = 100, height = 50, posX = 10, posY = 20, stylesheets = {
		"cssClass1", "cssClass2" })
public class SampleViewControllerWithListener {

	// switching this boolean property to false will deactivate all method
	// invocation
	// that reference this listenerActiveProperty
	protected final BooleanProperty listenerEnabled = new SimpleBooleanProperty(true);

	// registers invocations for testing
	protected List<String> invocations = new ArrayList<>();

	@FXML
	protected Button actionButton;

	@FXML
	protected TextField textField;

	@FXML
	protected ChoiceBox<String> choiceBox;

	@FXML
	protected ComboBox<String> comboBox;

	@FXML
	protected TableView<String> singleSelectionTable;

	@AFXEnableMultiSelection
	@FXML
	protected TableView<String> multiSelectionTable;

	// order 10: changes on textfield will trigger this method first
	@AFXOnValueChanged(controlId = "textField", order = 10)
	public void onTextFieldValueChange(final String value) {
		invocations.add("onTextFieldValueChange('" + value + "')");
	}

	// order 30: changes on textfield will trigger this method third
	@AFXOnValueChanged(controlId = "textField", order = 30, listenerActiveBooleanProperty = "listenerEnabled")
	public void onTextFieldValueChangeWithAnnotatedArguments(@AFXArgHint(ArgumentHint.OLD_VALUE) final String oldValue,
			@AFXArgHint(ArgumentHint.NEW_VALUE) final String newValue, final ObservableValue<String> observable) {
		invocations.add("onTextFieldValueChangeWithAnnotatedArguments('" + oldValue + "', '" + newValue + "', "
				+ (observable != null ? "ObservableValue" : "null") + ")");
	}

	// order 20: changes on textfield will trigger this method second
	@AFXOnValueChanged(controlId = "textField", order = 20, listenerActiveBooleanProperty = "listenerEnabled")
	public void onTextFieldValueChangeWithNewAndOldValue(final String newValue, final String oldValue,
			final ObservableValue<String> observable) {
		invocations.add("onTextFieldValueChangeWithNewAndOldValue('" + newValue + "', '" + oldValue + "', "
				+ (observable != null ? "ObservableValue" : "null") + ")");
	}

	@AFXOnValueChanged(controlId = "choiceBox")
	public void onChoiceBoxValueChange(final String value) {
		invocations.add("onChoiceBoxValueChange('" + value + "')");
	}

	@AFXOnValueChanged(controlId = "comboBox")
	public void onComboBoxValueChange(final String value) {
		invocations.add("onComboBoxValueChange('" + value + "')");
	}

}