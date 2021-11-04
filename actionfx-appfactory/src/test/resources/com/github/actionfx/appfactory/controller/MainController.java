package com.github.actionfx.appfactory.controller;

import com.github.actionfx.core.annotation.AFXController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 * ActionFX controller for FXML view "MainView.fxml"
 *
 * @author actionfx-appfactory
 *
 */
@AFXController(viewId = "MainView", fxml = "/MainView.fxml", title = "MainView")
public class MainController {

	@FXML
	protected Button okButton;

	@FXML
	protected TableView tableView;

}
