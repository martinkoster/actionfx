package com.github.actionfx.core.container.instantiation;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXOnAction;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

@AFXController(viewId = "testId", fxml = "/testfxml/SampleViewWithListener.fxml", icon = "icon.png", singleton = true, maximized = true, modal = false, title = "Hello World", width = 100, height = 50, posX = 10, posY = 20, stylesheets = {
		"cssClass1", "cssClass2" })
public class SampleViewControllerWithWrongAFXOnAction {

	@FXML
	protected TableView<String> singleSelectionTable;

	/**
	 * Control "singleSelectionTable" does not have an "onAction" property
	 */
	@AFXOnAction(controlId = "singleSelectionTable")
	public void willNeverBeCalledAsOnActionIsWrong() {

	}
}