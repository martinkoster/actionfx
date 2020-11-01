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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.HeadlessMonocleExtension;

/**
 * JUnit test case for {@link FxmlViewInstantiationSupplier}
 * 
 * @author koster
 *
 */
@ExtendWith(HeadlessMonocleExtension.class)
class FxmlViewInstantiationSupplierTest {

	@TestInFxThread
	@Test
	void testCreateInstance() {
		// GIVEN
		AnnotatedController controller = new AnnotatedController();
		AFXController controllerAnnotation = AnnotationUtils.findAnnotation(AnnotatedController.class,
				AFXController.class);
		FxmlViewInstantiationSupplier supplier = new FxmlViewInstantiationSupplier(controller, controllerAnnotation);

		// WHEN
		FxmlView view = supplier.get();

		// THEN
		assertThat(view.getRootNode(), notNullValue());
		assertThat(view.getId(), equalTo("testId"));
		assertThat(view.getWidth(), equalTo(100));
		assertThat(view.getHeight(), equalTo(50));
		assertThat(view.getIcon(), equalTo("icon.png"));
		assertThat(view.getPosX(), equalTo(10));
		assertThat(view.getPosY(), equalTo(20));
		assertThat(view.isMaximized(), equalTo(true));
		assertThat(view.isModalDialogue(), equalTo(false));
		assertThat(view.getWindowTitle(), equalTo("Hello World"));
		assertThat(view.getStylesheets(), hasItems(equalTo("cssClass1"), equalTo("cssClass2")));
	}

	@AFXController(viewId = "testId", fxml = "/testfxml/SampleView.fxml", icon = "icon.png", singleton = true, maximized = true, modal = false, title = "Hello World", width = 100, height = 50, posX = 10, posY = 20, stylesheets = {
			"cssClass1", "cssClass2" })
	private static class AnnotatedController {
	}

}
