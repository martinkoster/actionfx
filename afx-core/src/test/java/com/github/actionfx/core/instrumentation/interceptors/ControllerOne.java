/*
 * Copyright (c) 2020 Martin Koster
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.actionfx.core.instrumentation.interceptors;

import com.github.actionfx.core.annotation.AFXAction;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.view.BorderPanePosition;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

/**
 * Test controller.
 *
 * @author koster
 *
 */
@AFXController(viewId = "mainId", fxml = "/testfxml/ViewWithNestedView.fxml")
public class ControllerOne {

	/**
	 * Target for nested views (in case "showViewAsNested" is called). It is not
	 * required that the field is there for nested views to work. But for testing it
	 * is handy to access this value.
	 */
	@FXML
	private BorderPane mainBorderPane;

	/**
	 * displays the view of ControllerTwo in a new stage
	 */
	@AFXAction(showView = "nestedTabPaneView", showInNewWindow = true)
	public void showViewTwoInNewStage() {

	}

	/**
	 * displays the view of ControllerTwo in the current stage
	 */
	@AFXAction(showView = "nestedTabPaneView", showInNewWindow = false)
	public void showViewTwoInSameWindow() {

	}

	/**
	 * displays the view of ControllerTwo in "mainBorderPane" at center position
	 */
	@AFXAction(showNestedViews = @AFXNestedView(refViewId = "nestedTabPaneView", attachToNodeWithId = "mainBorderPane", attachToBorderPanePosition = BorderPanePosition.CENTER))
	public void showViewAsNested() {

	}

	public BorderPane getMainBorderPane() {
		return mainBorderPane;
	}
}
