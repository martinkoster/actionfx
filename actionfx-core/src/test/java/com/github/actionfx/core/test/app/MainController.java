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
package com.github.actionfx.core.test.app;

import jakarta.inject.Inject;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.view.FxmlView;

/**
 * Test controller.
 *
 * @author koster
 *
 */
@AFXController(fxml = "/testfxml/SampleView.fxml", viewId = "mainView")
public class MainController extends AbstractController {

	// field has the same name than defined view above. so we can expect to have
	// that view injected here
	@Inject
	private FxmlView mainView;

	// here, a new instance is constructed using the default no-arg constructor
	@Inject
	private ModelWithDefaultConstructor model;

	public FxmlView getMainView() {
		return mainView;
	}

	public ModelWithDefaultConstructor getModel() {
		return model;
	}

}
