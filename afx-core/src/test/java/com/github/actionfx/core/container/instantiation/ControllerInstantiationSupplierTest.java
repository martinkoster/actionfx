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
package com.github.actionfx.core.container.instantiation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit test case for {@link FxmlViewInstantiationSupplier}
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ControllerInstantiationSupplierTest {

	@BeforeEach
	void onSetup() {
		ActionFX.builder().build();
	}

	@Test
	void testCreateInstance_sampleViewWithFxml() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewController> supplier = new ControllerInstantiationSupplier<>(
				SampleViewController.class);

		// WHEN
		final SampleViewController controller = supplier.get();

		// THEN
		assertThat(controller, notNullValue());
		final View view = ControllerWrapper.getViewFrom(controller);
		assertThat(view, notNullValue());
		assertThat(view.getRootNode(), notNullValue());
		assertThat(view.getId(), equalTo("testId"));

		assertThat(view, instanceOf(View.class));
		final FxmlView fxmlView = (FxmlView) view;
		assertThat(fxmlView.getWidth(), equalTo(100));
		assertThat(fxmlView.getHeight(), equalTo(50));
		assertThat(fxmlView.getIcon(), equalTo("icon.png"));
		assertThat(fxmlView.getPosX(), equalTo(10));
		assertThat(fxmlView.getPosY(), equalTo(20));
		assertThat(fxmlView.isMaximized(), equalTo(true));
		assertThat(fxmlView.isModalDialogue(), equalTo(false));
		assertThat(fxmlView.getWindowTitle(), equalTo("Hello World"));
		assertThat(fxmlView.getStylesheets(), hasItems(equalTo("cssClass1"), equalTo("cssClass2")));
	}

}
