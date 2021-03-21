package com.github.actionfx.core.container.instantiation;

import com.github.actionfx.core.annotation.AFXController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

@AFXController(viewId = "multilingualView", fxml = "/testfxml/MultilingualView.fxml", resourcesBasename = "i18n.TestResources")
public class MultilingualViewController {

	@FXML
	private Label label;

	public Label getLabel() {
		return label;
	}
}