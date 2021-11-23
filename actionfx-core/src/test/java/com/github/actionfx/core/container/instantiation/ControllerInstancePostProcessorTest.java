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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.extension.beans.SubscribeMethodControllerExtension;
import com.github.actionfx.core.extension.controller.CellValueConfigControllerExtension;
import com.github.actionfx.core.extension.controller.ConverterControllerExtension;
import com.github.actionfx.core.extension.controller.DisableNodeControllerExtension;
import com.github.actionfx.core.extension.controller.EnableMultiSelectionControllerExtension;
import com.github.actionfx.core.extension.controller.EnableNodeControllerExtension;
import com.github.actionfx.core.extension.controller.FormBindingControllerExtension;
import com.github.actionfx.core.extension.controller.NestedViewControllerExtension;
import com.github.actionfx.core.extension.controller.OnActionMethodControllerExtension;
import com.github.actionfx.core.extension.controller.OnControlValueChangeMethodControllerExtension;
import com.github.actionfx.core.extension.controller.OnLoadControlDataMethodControllerExtension;
import com.github.actionfx.core.extension.controller.UseFilteredListControllerExtension;

/**
 * JUnit test for {@link ControllerInstancePostProcessor}.
 *
 * @author koster
 *
 */
class ControllerInstancePostProcessorTest {

	private final Class<?>[] EXPECTED_EXTENSION = new Class[] { //
			NestedViewControllerExtension.class, //
			EnableMultiSelectionControllerExtension.class, //
			UseFilteredListControllerExtension.class, //
			OnActionMethodControllerExtension.class, //
			ConverterControllerExtension.class, //
			CellValueConfigControllerExtension.class, //
			EnableNodeControllerExtension.class, //
			DisableNodeControllerExtension.class, //
			OnLoadControlDataMethodControllerExtension.class, //
			OnControlValueChangeMethodControllerExtension.class, //
			SubscribeMethodControllerExtension.class, //
			FormBindingControllerExtension.class };

	@Test
	void testRegisteredExtensions() {
		// GIVEN
		final ControllerInstancePostProcessor postProcessor = new ControllerInstancePostProcessor();

		// WHEN and THEN
		assertThat(
				postProcessor.getControllerExtensions().stream().map(Consumer::getClass).collect(Collectors.toList()),
				contains(EXPECTED_EXTENSION));
	}
}
