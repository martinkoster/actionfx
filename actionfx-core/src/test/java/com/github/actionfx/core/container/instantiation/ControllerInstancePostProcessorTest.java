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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
			FormBindingControllerExtension.class };

	@Test
	void testRegisteredExtensions() {
		// GIVEN
		final ControllerInstancePostProcessor postProcessor = new ControllerInstancePostProcessor();

		// WHEN and THEN
		assertThat(postProcessor.getUnmodifiableControllerExtensions().stream().map(Consumer::getClass)
				.collect(Collectors.toList()), contains(EXPECTED_EXTENSION));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testPostProcess_customControllerExtensions() {
		// GIVEN
		final Object controller = Mockito.mock(Object.class);
		final Consumer<Object> extension1 = Mockito.mock(Consumer.class);
		final Consumer<Object> extension2 = Mockito.mock(Consumer.class);
		final ControllerInstancePostProcessor postProcessor = new ControllerInstancePostProcessor(
				Arrays.asList(extension1, extension2));

		// WHEN
		postProcessor.postProcess(controller);

		// THEN
		verify(extension1, times(1)).accept(controller);
		verify(extension2, times(1)).accept(controller);
	}

}
