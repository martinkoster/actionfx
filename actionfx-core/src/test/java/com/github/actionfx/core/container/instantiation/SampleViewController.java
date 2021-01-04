package com.github.actionfx.core.container.instantiation;

import com.github.actionfx.core.annotation.AFXController;

@AFXController(viewId = "testId", fxml = "/testfxml/SampleView.fxml", icon = "icon.png", singleton = true, maximized = true, modal = false, title = "Hello World", width = 100, height = 50, posX = 10, posY = 20, stylesheets = {
		"cssClass1", "cssClass2" })
public class SampleViewController {
}