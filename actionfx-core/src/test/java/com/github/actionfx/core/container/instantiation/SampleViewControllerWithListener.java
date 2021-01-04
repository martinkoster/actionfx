package com.github.actionfx.core.container.instantiation;

import com.github.actionfx.core.annotation.AFXController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

@AFXController(viewId = "testId", fxml = "/testfxml/SampleViewWithListener.fxml", icon = "icon.png", singleton = true, maximized = true, modal = false, title = "Hello World", width = 100, height = 50, posX = 10, posY = 20, stylesheets = {
		"cssClass1", "cssClass2" })
public class SampleViewControllerWithListener {

	@FXML
	private Button actionButton;

	@FXML
	private TextField textField;

	@FXML
	private ChoiceBox<String> choiceBox;

}