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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.test.TestView;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit test case for {@link ViewBuilder}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ViewBuilderTest {

    @Test
    void testBuilder_instanceSupplied() {
        // WHEN
        final ViewBuilder<TestView> builder = new ViewBuilder<>(new TestView());
        final TestView view = builder.id("testId").width(100).height(50).icon("icon.png").posX(10).posY(20)
                .maximized(true)
                .modalDialogue(false).windowTitle("Title").stylesheets(new String[] { "cssClass1", "cssClass2" })
                .getView();

		// THEN
		assertThat(view.getId()).isEqualTo("testId");
		assertThat(view.getWidth()).isEqualTo(100);
		assertThat(view.getHeight()).isEqualTo(50);
		assertThat(view.getIcon()).isEqualTo("icon.png");
		assertThat(view.getPosX()).isEqualTo(10);
		assertThat(view.getPosY()).isEqualTo(20);
		assertThat(view.isMaximized()).isEqualTo(true);
		assertThat(view.isModalDialogue()).isEqualTo(false);
		assertThat(view.getWindowTitle()).isEqualTo("Title");
		assertThat(view.getStylesheets()).containsExactly("cssClass1", "cssClass2");
    }

    @Test
    void testBuilder_classSupplied() {
        // WHEN
        final ViewBuilder<TestView> builder = new ViewBuilder<>(TestView.class);
        final TestView view = builder.id("testId").width(100).height(50).icon("icon.png").posX(10).posY(20)
                .maximized(true)
                .modalDialogue(false).windowTitle("Title").stylesheets(new String[] { "cssClass1", "cssClass2" })
                .getView();

		// THEN
		assertThat(view.getId()).isEqualTo("testId");
		assertThat(view.getWidth()).isEqualTo(100);
		assertThat(view.getHeight()).isEqualTo(50);
		assertThat(view.getIcon()).isEqualTo("icon.png");
		assertThat(view.getPosX()).isEqualTo(10);
		assertThat(view.getPosY()).isEqualTo(20);
		assertThat(view.isMaximized()).isEqualTo(true);
		assertThat(view.isModalDialogue()).isEqualTo(false);
		assertThat(view.getWindowTitle()).isEqualTo("Title");
		assertThat(view.getStylesheets()).containsExactly("cssClass1", "cssClass2");
    }

}
