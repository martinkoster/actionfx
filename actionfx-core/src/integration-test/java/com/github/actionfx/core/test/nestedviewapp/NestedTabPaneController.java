package com.github.actionfx.core.test.nestedviewapp;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXNestedView;

@AFXNestedView(refViewId = "titledPaneView", attachToNodeWithId = "tab1AnchorPane")
@AFXController(viewId = "nestedTabPaneView", fxml = "/testfxml/NestedTabPaneView.fxml")
public class NestedTabPaneController {
}