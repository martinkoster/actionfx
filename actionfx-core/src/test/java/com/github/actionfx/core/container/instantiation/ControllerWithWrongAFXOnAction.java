package com.github.actionfx.core.container.instantiation;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXOnAction;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

@AFXController(viewId = "controllerWithWrongAFXOnActionView", fxml = "/testfxml/SampleViewWithListener.fxml")
public class ControllerWithWrongAFXOnAction {

	@FXML
	protected TableView<String> singleSelectionTable;

	/**
	 * Control "singleSelectionTable" does not have an "onAction" property
	 */
	@AFXOnAction(controlId = "singleSelectionTable")
	public void willNeverBeCalledAsOnActionIsWrong() {

	}
}