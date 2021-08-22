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
package com.github.actionfx.core.bind;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * JUnit test case for {@link NameBasedBindindTargetResolver}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class NameBasedBindindTargetResolverTest {

	@Test
	void testResolve_controlHasFieldSuffix() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Model model = new Model();
		final NameBasedBindindTargetResolver resolver = new NameBasedBindindTargetResolver("", "Label");

		// WHEN
		final List<BindingTarget> targets = resolver.resolve(model, view);

		// THEN
		assertThat(targets, hasSize(2));
		assertBindingTarget(targets, 0, "helloWorldLabel", Model.class, "helloWorld");
		assertBindingTarget(targets, 1, "tableView", Model.class, "tableView");
	}

	@Test
	void testResolve_controlMatchesFieldName() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Model model = new Model();
		final NameBasedBindindTargetResolver resolver = new NameBasedBindindTargetResolver();

		// WHEN
		final List<BindingTarget> targets = resolver.resolve(model, view);

		// THEN
		assertThat(targets, hasSize(1));
		assertBindingTarget(targets, 0, "tableView", Model.class, "tableView");
	}

	@Test
	void testResolve_usingControlPrefixAndSuffix() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClassWithPrefixAndSuffix(), new Controller());
		final Model model = new Model();
		final NameBasedBindindTargetResolver resolver = new NameBasedBindindTargetResolver("prefix", "Suffix");

		// WHEN
		final List<BindingTarget> targets = resolver.resolve(model, view);

		// THEN
		assertThat(targets, hasSize(1));
		assertBindingTarget(targets, 0, "prefixHelloWorldSuffix", Model.class, "helloWorld");
	}

	private static void assertBindingTarget(final List<BindingTarget> targets, final int index, final String controlId,
			final Class<?> beanClass, final String beanPathExpression) {
		final BindingTarget target = targets.get(index);
		assertThat(target.getControl().getId(), equalTo(controlId));
		assertThat(target.getBeanClass(), equalTo(beanClass));
		assertThat(target.getBeanPathExpression(), equalTo(beanPathExpression));
	}

	/**
	 * Model class with fields.
	 *
	 * @author koster
	 *
	 */
	public static class Model {
		@SuppressWarnings("unused")
		private String helloWorld;
		@SuppressWarnings("unused")
		private String tableView;
		@SuppressWarnings("unused")
		private String nonMatching;
	}

	/**
	 * View class.
	 *
	 * @author koster
	 *
	 */
	public static class ViewClass extends AnchorPane {

		public ViewClass() {
			final HBox hbox = new HBox();

			final Label label = new Label("Hello World");
			label.setId("helloWorldLabel");

			final TableView<String> tableView = new TableView<>();
			tableView.setId("tableView");

			hbox.getChildren().addAll(label, tableView);

			getChildren().add(hbox);
		}
	}

	/**
	 * View class with controls having prefixes and suffixes.
	 *
	 * @author koster
	 *
	 */
	public static class ViewClassWithPrefixAndSuffix extends AnchorPane {

		public ViewClassWithPrefixAndSuffix() {
			final HBox hbox = new HBox();

			final Label label = new Label("Hello World");
			label.setId("prefixHelloWorldSuffix");

			hbox.getChildren().addAll(label);

			getChildren().add(hbox);
		}
	}

	public static class Controller {

	}
}
