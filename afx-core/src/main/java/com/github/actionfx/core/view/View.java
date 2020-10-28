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

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Interface for JavaFX views.
 * 
 * @author koster
 *
 */
public interface View {

	/**
	 * The view ID, unique over all views.
	 * 
	 * @return the view ID
	 */
	String getId();

	/**
	 * Returns the root node of the view. The returned type must be {@link Parent}
	 * so that this node can be set to a {@link Scene}.
	 * 
	 * @return the root node of the view
	 */
	Parent getRootNode();

	/**
	 * Shows the view in the supplied {@link Stage}.
	 * 
	 * @param stage the stage to show the view inside
	 */
	public void show(Stage stage);

	/**
	 * Shows the view in a new stage / new window.
	 */
	public void show();

	/**
	 * Shows the view and waits in a modal fashion.
	 */
	public void showAndWait();

	/**
	 * Hides the view. This only works, when the view has its own stage.
	 */
	public void hide();
}
