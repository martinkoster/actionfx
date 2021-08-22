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
package com.github.actionfx.core.beans;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JUnit test case {@link BeanPropertyReference}.
 *
 * @author koster
 *
 */
class BeanPropertyReferenceTest {

	@Test
	void testGetValue_javaFXProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");
		final BeanPropertyReference<String> propertyValue = new BeanPropertyReference<>(property, model);

		// WHEN and THEN
		assertThat(propertyValue.getValue(), equalTo("Hello"));
	}

	@Test
	void testGetValue_javaBeanProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<Boolean> property = new BeanProperty<>(Model.class, "javaBeanProperty");
		final BeanPropertyReference<Boolean> propertyValue = new BeanPropertyReference<>(property, model);

		// WHEN and THEN
		assertThat(propertyValue.getValue(), equalTo(Boolean.TRUE));
	}

	@Test
	void testSetValue_javaFXProperty() {
		// GIVEN
		final Model model = new Model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");
		final BeanPropertyReference<String> propertyValue = new BeanPropertyReference<>(property, model);

		// WHEN
		propertyValue.setValue("Hello World");

		// THEN
		assertThat(model.getJavaFXProperty(), equalTo("Hello World"));
	}

	@Test
	void testGetFxProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");
		final BeanPropertyReference<String> propertyValue = new BeanPropertyReference<>(property, model);

		// WHEN and THEN
		assertThat(propertyValue.getFxProperty(), equalTo(model.javaFXProperty));
	}

	private Model model() {
		final Model model = new Model();
		model.setJavaFXProperty("Hello");
		model.setJavaBeanProperty(true);
		return model;
	}

	public class Model {

		// JavaFX property with setter, getter and property-getter
		StringProperty javaFXProperty = new SimpleStringProperty();

		// regular Java bean getter and setter
		boolean javaBeanProperty;

		public final StringProperty javaFXPropertyProperty() {
			return javaFXProperty;
		}

		public final String getJavaFXProperty() {
			return javaFXPropertyProperty().get();
		}

		public final void setJavaFXProperty(final String javaFXProperty) {
			javaFXPropertyProperty().set(javaFXProperty);
		}

		public boolean isJavaBeanProperty() {
			return javaBeanProperty;
		}

		public void setJavaBeanProperty(final boolean javaBeanProperty) {
			this.javaBeanProperty = javaBeanProperty;
		}
	}

}
