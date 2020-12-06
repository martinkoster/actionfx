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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.test.TestView;

/**
 * JUnit test case for {@link ViewBuilder}.
 * 
 * @author koster
 *
 */
class ViewBuilderTest {

	@Test
	void testBuilder_instanceSupplied() {
		// WHEN
		ViewBuilder<TestView> builder = new ViewBuilder<>(new TestView());
		TestView view = builder.id("testId").width(100).height(50).icon("icon.png").posX(10).posY(20).maximized(true)
				.modalDialogue(false).windowTitle("Title").stylesheets(new String[] { "cssClass1", "cssClass2" })
				.getView();

		// THEN
		assertThat(view.getId(), equalTo("testId"));
		assertThat(view.getWidth(), equalTo(100));
		assertThat(view.getHeight(), equalTo(50));
		assertThat(view.getIcon(), equalTo("icon.png"));
		assertThat(view.getPosX(), equalTo(10));
		assertThat(view.getPosY(), equalTo(20));
		assertThat(view.isMaximized(), equalTo(true));
		assertThat(view.isModalDialogue(), equalTo(false));
		assertThat(view.getWindowTitle(), equalTo("Title"));
		assertThat(view.getStylesheets(), hasItems(equalTo("cssClass1"), equalTo("cssClass2")));
	}

	@Test
	void testBuilder_classSupplied() {
		// WHEN
		ViewBuilder<TestView> builder = new ViewBuilder<>(TestView.class);
		TestView view = builder.id("testId").width(100).height(50).icon("icon.png").posX(10).posY(20).maximized(true)
				.modalDialogue(false).windowTitle("Title").stylesheets(new String[] { "cssClass1", "cssClass2" })
				.getView();

		// THEN
		assertThat(view.getId(), equalTo("testId"));
		assertThat(view.getWidth(), equalTo(100));
		assertThat(view.getHeight(), equalTo(50));
		assertThat(view.getIcon(), equalTo("icon.png"));
		assertThat(view.getPosX(), equalTo(10));
		assertThat(view.getPosY(), equalTo(20));
		assertThat(view.isMaximized(), equalTo(true));
		assertThat(view.isModalDialogue(), equalTo(false));
		assertThat(view.getWindowTitle(), equalTo("Title"));
		assertThat(view.getStylesheets(), hasItems(equalTo("cssClass1"), equalTo("cssClass2")));
	}

}
