package com.github.actionfx.core.container.instantiation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.actionfx.core.annotation.AFXArgHint;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXOnControlValueChange;
import com.github.actionfx.core.annotation.AFXUseFilteredList;
import com.github.actionfx.core.annotation.ArgumentHint;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

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
	protected TextField actionButtonTextField;

	@FXML
	protected Button actionWithSubmissionButton;

	@FXML
	protected TextField textField;

	@FXML
	protected ChoiceBox<String> choiceBox;

	@FXML
	protected ComboBox<String> comboBox;

	@AFXUseFilteredList
	@FXML
	protected TableView<String> singleSelectionTable;

	@AFXUseFilteredList(wrapInSortedList = true)
	@AFXEnableMultiSelection
	@FXML
	protected TableView<String> multiSelectionTable;

	@FXML
	protected TableView<String> dataLoadedSelectionTable;

	@FXML
	protected TableView<String> asyncDataLoadedSelectionTable;

	// data loading initially deactivated
	protected BooleanProperty loadDataForTableViewActivated = new SimpleBooleanProperty(false);

	@FXML
	protected TreeView<String> dataLoadedTreeView;

	@FXML
	protected TreeView<String> asyncDataLoadedTreeView;

	// data loading initially deactivated
	protected BooleanProperty loadDataForTreeViewActivated = new SimpleBooleanProperty(false);

	@AFXOnAction(controlId = "actionButton")
	public void onActionButtonClicked() {
		invocations.add("onActionButtonClicked()");
	}

	@AFXOnAction(controlId = "actionWithSubmissionButton")
	public void onActionWithSubmissionButtonClicked(final ActionEvent actionEvent,
			@AFXControlValue("actionButtonTextField") final String text) {
		invocations.add("onActionWithSubmissionButtonClicked(" + (actionEvent != null ? "ActionEvent" : "null") + ", '"
				+ text + "')");
	}

	@AFXLoadControlData(controlId = "dataLoadedSelectionTable")
	public List<String> loadTableViewData() {
		return Arrays.asList("Loaded 1", "Loaded 2", "Loaded 3");
	}

	@AFXLoadControlData(controlId = "asyncDataLoadedSelectionTable", async = true, loadingActiveBooleanProperty = "loadDataForTableViewActivated")
	public List<String> loadTableViewDataAsynchronously() {
		return Arrays.asList("Loaded 1", "Loaded 2", "Loaded 3");
	}

	@AFXLoadControlData(controlId = "dataLoadedTreeView")
	public TreeItem<String> loadTreeViewData() {
		return new TreeItem<>("root");
	}

	@AFXLoadControlData(controlId = "asyncDataLoadedTreeView", async = true, loadingActiveBooleanProperty = "loadDataForTreeViewActivated")
	public TreeItem<String> loadTreeViewDataAsynchronously() {
		return new TreeItem<>("root");
	}

	// order 10: changes on textfield will trigger this method first
	@AFXOnControlValueChange(controlId = "textField", order = 10)
	public void onTextFieldValueChange(final String value) {
		invocations.add("onTextFieldValueChange('" + value + "')");
	}

	// order 30: changes on textfield will trigger this method third
	@AFXOnControlValueChange(controlId = "textField", order = 30, listenerActiveBooleanProperty = "listenerEnabled")
	public void onTextFieldValueChangeWithAnnotatedArguments(@AFXArgHint(ArgumentHint.OLD_VALUE) final String oldValue,
			@AFXArgHint(ArgumentHint.NEW_VALUE) final String newValue, final ObservableValue<String> observable) {
		invocations.add("onTextFieldValueChangeWithAnnotatedArguments('" + oldValue + "', '" + newValue + "', "
				+ (observable != null ? "ObservableValue" : "null") + ")");
	}

	// order 20: changes on textfield will trigger this method second
	@AFXOnControlValueChange(controlId = "textField", order = 20, listenerActiveBooleanProperty = "listenerEnabled")
	public void onTextFieldValueChangeWithNewAndOldValue(final String newValue, final String oldValue,
			final ObservableValue<String> observable) {
		invocations.add("onTextFieldValueChangeWithNewAndOldValue('" + newValue + "', '" + oldValue + "', "
				+ (observable != null ? "ObservableValue" : "null") + ")");
	}

	@AFXOnControlValueChange(controlId = "choiceBox")
	public void onChoiceBoxValueChange(final String value) {
		invocations.add("onChoiceBoxValueChange('" + value + "')");
	}

	@AFXOnControlValueChange(controlId = "comboBox")
	public void onComboBoxValueChange(final String value) {
		invocations.add("onComboBoxValueChange('" + value + "')");
	}

	@AFXOnControlValueChange(controlId = "singleSelectionTable", order = 1)
	public void onSelectValueInSingleSelectionTable(final String selected) {
		invocations.add("onSelectValueInSingleSelectionTable('" + selected + "')");
	}

	@AFXOnControlValueChange(controlId = "singleSelectionTable", order = 2)
	public void onSelectValueInSingleSelectionTableWithList(final List<String> selected) {
		invocations.add("onSelectValueInSingleSelectionTableWithList([" + String.join(",", selected) + "])");
	}

	@AFXOnControlValueChange(controlId = "multiSelectionTable", order = 1)
	public void onSelectValueInMultiSelectionTable(final List<String> selected) {
		invocations.add("onSelectValueInMultiSelectionTable('" + String.join("','", selected) + "')");
	}

	@AFXOnControlValueChange(controlId = "multiSelectionTable", order = 2)
	public void onSelectValueInMultiSelectionTableWithFullArguments(final List<String> selected,
			final List<String> added, final List<String> removed, final String lastSelected,
			final ListChangeListener.Change<String> change) {
		invocations.add("onSelectValueInMultiSelectionTableWithFullArguments([" + String.join(",", selected) + "],["
				+ String.join(",", added) + "],[" + String.join(",", removed) + "],'" + lastSelected + "',change)");
	}

	@AFXOnControlValueChange(controlId = "multiSelectionTable", order = 3)
	public void onSelectValueInMultiSelectionTableWithAnnotatedArguments(
			@AFXArgHint(ArgumentHint.ADDED_VALUES) final List<String> added,
			@AFXArgHint(ArgumentHint.ALL_SELECTED) final List<String> selected,
			@AFXArgHint(ArgumentHint.REMOVED_VALUES) final List<String> removed, final String lastSelected,
			final ListChangeListener.Change<String> change) {
		invocations.add("onSelectValueInMultiSelectionTableWithAnnotatedArguments([" + String.join(",", added) + "],["
				+ String.join(",", selected) + "],[" + String.join(",", removed) + "],'" + lastSelected + "',change)");
	}

}