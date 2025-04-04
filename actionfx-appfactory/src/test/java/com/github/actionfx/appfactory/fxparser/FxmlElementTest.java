/*
 * Copyright (c) 2021,2022 Martin Koster
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import javafx.scene.layout.BorderPane;

/**
 * JUnit test case for {@link FxmlElement}.
 *
 * @author koster
 *
 */
class FxmlElementTest {

	@Test
	void testGetFxmlElementsAsStream() {
		// GIVEN
		final FxmlElement root = new FxmlElement(null, "root", null);
		final FxmlElement child1 = new FxmlElement(root, "child1", null);
		final FxmlElement child2 = new FxmlElement(root, "child2", null);
		final FxmlElement child3 = new FxmlElement(child2, "child3", null);

		root.getChildren().add(child1);
		root.getChildren().add(child2);
		child2.getChildren().add(child3);

		// WHEN
		final List<String> names = root.getFxmlElementsAsStream().map(FxmlElement::getName)
				.collect(Collectors.toList());

		// THEN
		assertThat(names).containsExactly("root", "child1", "child2", "child3");
	}

	@Test
	void testAsResolvedClass() {
		// GIVEN
		final FxmlElement element = new FxmlElement(null, "BorderPane", "javafx.scene.layout.BorderPane");

		// WHEN
		final Class<?> borderPaneClass = element.asResolvedClass();

		// THEN
		assertThat(borderPaneClass).isEqualTo(BorderPane.class);
	}

	@Test
	void testAsResolvedClass_importStatementIsNull() {
		// GIVEN
		final FxmlElement element = new FxmlElement(null, "BorderPane", null);

		// WHEN and THEN
		assertThat(element.asResolvedClass()).isNull();
	}
}
