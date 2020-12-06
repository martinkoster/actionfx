package com.github.actionfx.core.test.nestedviewapp;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXNestedView;

@AFXController(viewId = "nestedTabPaneView", fxml = "/testfxml/NestedTabPaneView.fxml", nestedViews = {
		@AFXNestedView(refViewId = "titledPaneView", attachToNodeWithId = "tab1AnchorPane") })
public class NestedTabPaneController {
}