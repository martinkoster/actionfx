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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * JUnit test case for {@link MappingBasedBindindTargetResolver}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class MappingBasedBindindTargetResolverTest {

	@Test
	void testResolve() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Model model = new Model();
		final MappingBasedBindingTargetResolver resolver = new MappingBasedBindingTargetResolver(false);
		resolver.registerMapping("someControlId", ControlProperties.USER_VALUE_OBSERVABLE, "username");

		// WHEN
		final List<BindingTarget> targets = resolver.resolve(model, view);

		// THEN
		assertThat(targets).hasSize(2);
		assertBindingTarget(targets, 0, "someControlId", Model.class, "username");
		assertBindingTarget(targets, 1, "password", Model.class, "password");
	}

	@Test
	void testResolve_withDisableNameBasedMappingAsFallback() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Model model = new Model();
		final MappingBasedBindingTargetResolver resolver = new MappingBasedBindingTargetResolver(true);
		resolver.registerMapping("someControlId", ControlProperties.USER_VALUE_OBSERVABLE, "username");

		// WHEN
		final List<BindingTarget> targets = resolver.resolve(model, view);

		// THEN
		assertThat(targets).hasSize(1);
		assertBindingTarget(targets, 0, "someControlId", Model.class, "username");
	}

	@Test
	void testResolve_usingNameBasedResolutionAsFallback_withPrefixAndSuffix() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClassWithPrefixAndSuffix(), new Controller());
		final Model model = new Model();
		final MappingBasedBindingTargetResolver resolver = new MappingBasedBindingTargetResolver(false, "prefix",
				"Suffix");
		resolver.registerMapping("someControlId", ControlProperties.USER_VALUE_OBSERVABLE, "username");

		// WHEN
		final List<BindingTarget> targets = resolver.resolve(model, view);

		// THEN
		assertThat(targets).hasSize(2);
		assertBindingTarget(targets, 0, "someControlId", Model.class, "username");
		assertBindingTarget(targets, 1, "prefixPasswordSuffix", Model.class, "password");
	}

	private static void assertBindingTarget(final List<BindingTarget> targets, final int index, final String controlId,
			final Class<?> beanClass, final String beanPathExpression) {
		final BindingTarget target = targets.get(index);
		assertThat(target.getControl().getId()).isEqualTo(controlId);
		assertThat(target.getBeanClass()).isEqualTo(beanClass);
		assertThat(target.getBeanPathExpression()).isEqualTo(beanPathExpression);
	}

	/**
	 * Model class with fields.
	 *
	 * @author koster
	 *
	 */
	public static class Model {
		@SuppressWarnings("unused")
		private String username;
		@SuppressWarnings("unused")
		private String password;
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

			final TextField userNameTextField = new TextField();
			userNameTextField.setId("someControlId");

			final TextField textField = new TextField();
			textField.setId("password");
			hbox.getChildren().addAll(userNameTextField, textField);

			getChildren().add(hbox);
		}
	}

	/**
	 * View class with prefix and suffix.
	 *
	 * @author koster
	 *
	 */
	public static class ViewClassWithPrefixAndSuffix extends AnchorPane {

		public ViewClassWithPrefixAndSuffix() {
			final HBox hbox = new HBox();

			final TextField userNameTextField = new TextField();
			userNameTextField.setId("someControlId");

			final TextField textField = new TextField();
			textField.setId("prefixPasswordSuffix");
			hbox.getChildren().addAll(userNameTextField, textField);

			getChildren().add(hbox);
		}
	}

	public static class Controller {

	}
}
