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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.Control;
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
	void testResolve_controlHasFieldPrefix() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Field field = ReflectionUtils.findField(Model.class, "helloWorld");
		final NameBasedBindindTargetResolver resolver = new NameBasedBindindTargetResolver();

		// WHEN
		final Control control = resolver.resolve(view, field);

		// THEN
		assertThat(control, notNullValue());
		assertThat(control, instanceOf(Label.class));
		assertThat(((Label) control).getId(), equalTo("helloWorldLabel"));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testResolve_controlMatchesFieldName() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Field field = ReflectionUtils.findField(Model.class, "tableView");
		final NameBasedBindindTargetResolver resolver = new NameBasedBindindTargetResolver();

		// WHEN
		final Control control = resolver.resolve(view, field);

		// THEN
		assertThat(control, notNullValue());
		assertThat(control, instanceOf(TableView.class));
		assertThat(((TableView<String>) control).getId(), equalTo("tableView"));
	}

	@Test
	void testResolve_noMatchingControl() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Field field = ReflectionUtils.findField(Model.class, "nonMatching");
		final NameBasedBindindTargetResolver resolver = new NameBasedBindindTargetResolver();

		// WHEN
		final Control control = resolver.resolve(view, field);

		// THEN
		assertThat(control, nullValue());
	}

	@Test
	void testResolve_usingControlPrefixAndSuffix() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClassWithPrefixAndSuffix(), new Controller());
		final Field field = ReflectionUtils.findField(Model.class, "helloWorld");
		final NameBasedBindindTargetResolver resolver = new NameBasedBindindTargetResolver("prefix", "suffix");

		// WHEN
		final Control control = resolver.resolve(view, field);

		// THEN
		assertThat(control, notNullValue());
		assertThat(control, instanceOf(Label.class));
		assertThat(((Label) control).getId(), equalTo("prefixHelloWorldSuffixLabel"));
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
			label.setId("prefixHelloWorldSuffixLabel");

			hbox.getChildren().addAll(label);

			getChildren().add(hbox);
		}
	}

	public static class Controller {

	}
}
