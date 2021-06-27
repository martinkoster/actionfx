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
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.Control;
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
		final Field field = ReflectionUtils.findField(Model.class, "username");
		final var fieldToControlMap = new HashMap<String, String>();
		fieldToControlMap.put("username", "someControlId");
		final MappingBasedBindingTargetResolver resolver = new MappingBasedBindingTargetResolver(fieldToControlMap,
				false);

		// WHEN
		final Control control = resolver.resolve(view, field);

		// THEN
		assertThat(control, notNullValue());
		assertThat(control, instanceOf(TextField.class));
		assertThat(((TextField) control).getId(), equalTo("someControlId"));
	}

	@Test
	void testResolve_withDisableNameBasedMappingAsFallback() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Field field = ReflectionUtils.findField(Model.class, "password");
		final var fieldToControlMap = new HashMap<String, String>();
		fieldToControlMap.put("username", "someControlId");
		final MappingBasedBindingTargetResolver resolver = new MappingBasedBindingTargetResolver(fieldToControlMap,
				true);

		// WHEN
		final Control control = resolver.resolve(view, field);

		// THEN
		assertThat(control, nullValue());
	}

	@Test
	void testResolve_usingNameBasedResolutionAsFallback_withPrefixAndSuffix() {
		// GIVEN
		final View view = new ParentView("viewId", new ViewClass(), new Controller());
		final Field field = ReflectionUtils.findField(Model.class, "password");
		final var fieldToControlMap = new HashMap<String, String>();
		fieldToControlMap.put("userName", "someControlId");
		final MappingBasedBindingTargetResolver resolver = new MappingBasedBindingTargetResolver(fieldToControlMap,
				false, "prefix", "suffix");

		// WHEN
		final Control control = resolver.resolve(view, field);

		// THEN
		assertThat(control, notNullValue());
		assertThat(control, instanceOf(TextField.class));
		assertThat(((TextField) control).getId(), equalTo("prefixPasswordSuffix"));
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
			textField.setId("prefixPasswordSuffix");
			hbox.getChildren().addAll(userNameTextField, textField);

			getChildren().add(hbox);
		}
	}

	public static class Controller {

	}
}
