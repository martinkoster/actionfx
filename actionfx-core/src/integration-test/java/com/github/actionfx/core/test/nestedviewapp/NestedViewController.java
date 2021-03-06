package com.github.actionfx.core.test.nestedviewapp;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.view.BorderPanePosition;

@AFXNestedView(refViewId = "nestedTabPaneView", attachToNodeWithId = "mainBorderPane", attachToBorderPanePosition = BorderPanePosition.CENTER)
@AFXController(viewId = "mainId", fxml = "/testfxml/ViewWithNestedView.fxml")
public class NestedViewController {
}