package com.github.actionfx.core.container.instantiation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.actionfx.core.annotation.AFXArgHint;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXOnUserInput;
import com.github.actionfx.core.annotation.ArgumentHint;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
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

	@FXML
	protected TableView<String> dataLoadedSelectionTable;

	@AFXOnAction(controlId = "actionButton")
	public void onActionButtonClicked() {
		invocations.add("onActionButtonClicked()");
	}

	@AFXLoadControlData(controlId = "dataLoadedSelectionTable")
	public List<String> loadData() {
		return Arrays.asList("Loaded 1", "Loaded 2", "Loaded 3");
	}

	// order 10: changes on textfield will trigger this method first
	@AFXOnUserInput(controlId = "textField", order = 10)
	public void onTextFieldValueChange(final String value) {
		invocations.add("onTextFieldValueChange('" + value + "')");
	}

	// order 30: changes on textfield will trigger this method third
	@AFXOnUserInput(controlId = "textField", order = 30, listenerActiveBooleanProperty = "listenerEnabled")
	public void onTextFieldValueChangeWithAnnotatedArguments(@AFXArgHint(ArgumentHint.OLD_VALUE) final String oldValue,
			@AFXArgHint(ArgumentHint.NEW_VALUE) final String newValue, final ObservableValue<String> observable) {
		invocations.add("onTextFieldValueChangeWithAnnotatedArguments('" + oldValue + "', '" + newValue + "', "
				+ (observable != null ? "ObservableValue" : "null") + ")");
	}

	// order 20: changes on textfield will trigger this method second
	@AFXOnUserInput(controlId = "textField", order = 20, listenerActiveBooleanProperty = "listenerEnabled")
	public void onTextFieldValueChangeWithNewAndOldValue(final String newValue, final String oldValue,
			final ObservableValue<String> observable) {
		invocations.add("onTextFieldValueChangeWithNewAndOldValue('" + newValue + "', '" + oldValue + "', "
				+ (observable != null ? "ObservableValue" : "null") + ")");
	}

	@AFXOnUserInput(controlId = "choiceBox")
	public void onChoiceBoxValueChange(final String value) {
		invocations.add("onChoiceBoxValueChange('" + value + "')");
	}

	@AFXOnUserInput(controlId = "comboBox")
	public void onComboBoxValueChange(final String value) {
		invocations.add("onComboBoxValueChange('" + value + "')");
	}

	@AFXOnUserInput(controlId = "singleSelectionTable", order = 1)
	public void onSelectValueInSingleSelectionTable(final String selected) {
		invocations.add("onSelectValueInSingleSelectionTable('" + selected + "')");
	}

	@AFXOnUserInput(controlId = "singleSelectionTable", order = 2)
	public void onSelectValueInSingleSelectionTableWithList(final List<String> selected) {
		invocations.add("onSelectValueInSingleSelectionTableWithList([" + String.join(",", selected) + "])");
	}

	@AFXOnUserInput(controlId = "multiSelectionTable", order = 1)
	public void onSelectValueInMultiSelectionTable(final List<String> selected) {
		invocations.add("onSelectValueInMultiSelectionTable('" + String.join("','", selected) + "')");
	}

	@AFXOnUserInput(controlId = "multiSelectionTable", order = 2)
	public void onSelectValueInMultiSelectionTableWithFullArguments(final List<String> selected,
			final List<String> added, final List<String> removed, final String lastSelected,
			final ListChangeListener.Change<String> change) {
		invocations.add("onSelectValueInMultiSelectionTableWithFullArguments([" + String.join(",", selected) + "],["
				+ String.join(",", added) + "],[" + String.join(",", removed) + "],'" + lastSelected + "',change)");
	}

	@AFXOnUserInput(controlId = "multiSelectionTable", order = 3)
	public void onSelectValueInMultiSelectionTableWithAnnotatedArguments(
			@AFXArgHint(ArgumentHint.ADDED_VALUES) final List<String> added,
			@AFXArgHint(ArgumentHint.ALL_SELECTED) final List<String> selected,
			@AFXArgHint(ArgumentHint.REMOVED_VALUES) final List<String> removed, final String lastSelected,
			final ListChangeListener.Change<String> change) {
		invocations.add("onSelectValueInMultiSelectionTableWithAnnotatedArguments([" + String.join(",", added) + "],["
				+ String.join(",", selected) + "],[" + String.join(",", removed) + "],'" + lastSelected + "',change)");
	}

}