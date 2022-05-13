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
package com.github.actionfx.appfactory.fxparser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link FxmlParser}.
 *
 * @author koster
 *
 */
class FxmlParserTest {

    @Test
    void testParseFxml() {
        // GIVEN
        final FxmlParser parser = new FxmlParser();
        final InputStream inputStream = FxmlParserTest.class.getResourceAsStream("/fxml/MainView.fxml");

        // WHEN
        final FxmlDocument document = parser.parseFxml(inputStream);

        // THEN
        assertThat(document, notNullValue());
        assertThat(document.getRootElement(), notNullValue());
        assertThat(document.getRootElement().getName(), equalTo("BorderPane"));
        assertThat(document.getRootElement().getImportStatement(), equalTo("javafx.scene.layout.BorderPane"));
        assertThat(document.getIdNodesMap().keySet(),
                containsInAnyOrder("tableView", "okButton", "closeMenuItem", "deleteMenuItem", "helpMenuItem"));
        assertThat(document.getImportsAsFullyQualifiedStatements(),
                containsInAnyOrder("javafx.scene.control.Button", "javafx.scene.control.Menu",
                        "javafx.scene.control.MenuBar", "javafx.scene.control.MenuItem",
                        "javafx.scene.control.TableColumn", "javafx.scene.control.TableView",
                        "javafx.scene.layout.BorderPane", "javafx.scene.layout.HBox"));
    }

}
