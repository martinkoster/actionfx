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
package com.github.actionfx.core.view;

import java.util.ResourceBundle;

import com.github.actionfx.core.utils.AFXUtils;

/**
 * {@link View} implementation that uses FXML for describing the view.
 *
 * @author koster
 *
 */
public class FxmlView extends AbstractBindingView {

	private final Object controller;

	public FxmlView(final String id, final String fxmlLocation, final Object controller) {
		this(id, fxmlLocation, controller, null);
	}

	public FxmlView(final String id, final String fxmlLocation, final Object controller,
			final ResourceBundle resourceBundle) {
		super();
		this.id = id;
		rootNode = AFXUtils.loadFxml(fxmlLocation, controller, resourceBundle);
		this.controller = controller;
		this.resourceBundle = resourceBundle;
	}

	@Override
	public Object getController() {
		return controller;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}
}
