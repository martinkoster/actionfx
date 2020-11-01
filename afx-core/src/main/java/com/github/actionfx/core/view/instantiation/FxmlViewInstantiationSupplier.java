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
package com.github.actionfx.core.view.instantiation;

import java.util.Arrays;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.ViewBuilder;

/**
 * Instantiation supplier for {@link FxmlView} instances.
 * 
 * @author koster
 *
 */
public class FxmlViewInstantiationSupplier extends AbstractInstantiationSupplier<FxmlView> {

	private AFXController controllerAnnotation;

	private Object controller;

	public FxmlViewInstantiationSupplier(Object controller, AFXController controllerAnnotation) {
		this.controller = controller;
		this.controllerAnnotation = controllerAnnotation;
	}

	@Override
	protected FxmlView createInstance() {
		FxmlView fxmlView = new FxmlView(controllerAnnotation.viewId(), controllerAnnotation.fxml(), controller);
		ViewBuilder<FxmlView> builder = new ViewBuilder<FxmlView>(fxmlView);
		return builder.posX(controllerAnnotation.posX()).posY(controllerAnnotation.posY())
				.width(controllerAnnotation.width()).height(controllerAnnotation.height())
				.maximized(controllerAnnotation.maximized()).modalDialogue(controllerAnnotation.modal())
				.icon(controllerAnnotation.icon()).stylesheets(Arrays.asList(controllerAnnotation.stylesheets()))
				.windowTitle(controllerAnnotation.title()).getView();
	}
}
