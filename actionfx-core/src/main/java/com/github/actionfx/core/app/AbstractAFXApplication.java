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
package com.github.actionfx.core.app;

import com.github.actionfx.core.ActionFX;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Base class for ActionFX applications that can be optionally used. This class
 * takes care for building the proper {@link ActionFX} instance that is used for
 * handling the controller, views and their flow.
 *
 * @author koster
 *
 */
public abstract class AbstractAFXApplication extends Application {

	@Override
	public void init() throws Exception {
		ActionFX.builder().configurationClass(getClass()).build().scanForActionFXComponents();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ActionFX.getInstance().displayMainView(primaryStage);
	}

}
