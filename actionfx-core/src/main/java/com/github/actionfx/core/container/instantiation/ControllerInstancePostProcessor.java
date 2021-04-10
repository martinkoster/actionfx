/*
 * Copyright (c) 2021 Martin Koster
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
package com.github.actionfx.core.container.instantiation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.annotation.AFXNestedView.AFXNestedViews;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXOnControlValueChange;
import com.github.actionfx.core.container.extension.CellValueConfigControllerExtension;
import com.github.actionfx.core.container.extension.ConverterControllerExtension;
import com.github.actionfx.core.container.extension.EnableMultiSelectionControllerExtension;
import com.github.actionfx.core.container.extension.NestedViewControllerExtension;
import com.github.actionfx.core.container.extension.OnActionMethodControllerExtension;
import com.github.actionfx.core.container.extension.OnControlValueChangeMethodControllerExtension;
import com.github.actionfx.core.container.extension.OnLoadControlDataMethodControllerExtension;
import com.github.actionfx.core.container.extension.UseFilteredListControllerExtension;

/**
 * Post-processor for controller instances that is invoked after view creation
 * and dependency injection, but before potential
 * {@code @PostConstruct}-annotated methods.
 *
 * @author koster
 *
 */
public class ControllerInstancePostProcessor {

	private final List<Consumer<Object>> controllerExtensions = new ArrayList<>();

	public ControllerInstancePostProcessor() {
		this(Collections.emptyList());
	}

	public ControllerInstancePostProcessor(final List<Consumer<Object>> customControllerExtension) {
		// order of extensions is important - field level-control configuration must be
		// performed before data is populated in the controls
		controllerExtensions.add(new NestedViewControllerExtension());
		controllerExtensions.add(new EnableMultiSelectionControllerExtension());
		controllerExtensions.add(new UseFilteredListControllerExtension());
		controllerExtensions.add(new OnActionMethodControllerExtension());
		controllerExtensions.add(new ConverterControllerExtension());
		controllerExtensions.add(new CellValueConfigControllerExtension());
		controllerExtensions.add(new OnLoadControlDataMethodControllerExtension());
		controllerExtensions.add(new OnControlValueChangeMethodControllerExtension());

		// add the custom controller extensions
		controllerExtensions.addAll(customControllerExtension);
	}

	/**
	 * Performs a post-processing on the supplied {@code controller}, including
	 * parsing of applied ActionFX annotations like {@link AFXNestedViews},
	 * {@link AFXLoadControlData}, {@link AFXOnAction}.
	 *
	 * @param controller the controller instance to post process
	 */
	public void postProcess(final Object controller) {
		applyControllerExtensions(controller);
	}

	/**
	 * Applies method-level annotations (e.g. {@link AFXOnControlValueChange}.
	 *
	 * @param instance the instance that is checked for ActionFX method level
	 *                 annotations
	 * @param view     the view that belongs to the controller
	 */
	private void applyControllerExtensions(final Object instance) {
		controllerExtensions.forEach(extension -> extension.accept(instance));
	}
}
