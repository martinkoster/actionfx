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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JUnit test for {@link BeanProperty}.
 *
 * @author koster
 *
 */
class BeanPropertyTest {

	@Test
	void testGetValue_javaFXProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");

		// WHEN and THEN
		assertThat(property.getValue(model), equalTo("Hello"));
	}

	@Test
	void testGetValue_javaBeanProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<Boolean> property = new BeanProperty<>(Model.class, "javaBeanProperty");

		// WHEN and THEN
		assertThat(property.getValue(model), equalTo(Boolean.TRUE));
	}

	@Test
	void testGetValue_fieldProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "fieldProperty");

		// WHEN and THEN
		assertThat(property.getValue(model), equalTo("World"));
	}

	@Test
	void testGetValue_fieldDoesNotExist() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "fantasyProperty");

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> property.getValue(model));

		// THEN
		assertThat(ex.getMessage(),
				containsString("has no getter, no property-getter and can  not be resolved to a field!"));
	}

	@Test
	void testGetValue_javaFXPropertyWithoutGetterSetter_butPropertyGetter() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class,
				"javaFXPropertyWithoutGetterSetter");

		// WHEN and THEN
		assertThat(property.getValue(model), equalTo("Hello World"));
	}

	@Test
	void testGetValue_indexedProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "indexedProperty", 1);

		// WHEN and THEN
		assertThat(property.getValue(model), equalTo("World"));
	}

	@Test
	void testGetValue_mappedProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "mappedProperty", "hello");

		// WHEN and THEN
		assertThat(property.getValue(model), equalTo("world"));
	}

	@Test
	void testSetValue_javaFXProperty() {
		// GIVEN
		final Model model = new Model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");

		// WHEN
		property.setValue(model, "Hello World");

		// THEN
		assertThat(model.getJavaFXProperty(), equalTo("Hello World"));
	}

	@Test
	void testSetValue_javaBeanProperty() {
		// GIVEN
		final Model model = new Model();
		final BeanProperty<Boolean> property = new BeanProperty<>(Model.class, "javaBeanProperty");

		// WHEN
		property.setValue(model, true);

		// THEN
		assertThat(model.isJavaBeanProperty(), equalTo(Boolean.TRUE));
	}

	@Test
	void testSetValue_fieldProperty() {
		// GIVEN
		final Model model = new Model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "fieldProperty");

		// WHEN
		property.setValue(model, "Hello World");

		// THEN
		assertThat(model.fieldProperty, equalTo("Hello World"));
	}

	@Test
	void testSetValue_fieldDoesNotExist() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "fantasyProperty");

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> property.setValue(model, "Hello World"));

		// THEN
		assertThat(ex.getMessage(), containsString("has no setter and can  not be resolved to a field!"));
	}

	@Test
	void testSetValue_indexedProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "indexedProperty", 1);

		// WHEN
		property.setValue(model, "Hello World!");

		// THEN
		assertThat(model.indexedProperty[1], equalTo("Hello World!"));
	}

	@Test
	void testSetValue_mappedProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "mappedProperty", "hello");

		// WHEN
		property.setValue(model, "Hello World!");

		// THEN
		assertThat(model.mappedProperty.get("hello"), equalTo("Hello World!"));
	}

	@Test
	void testIsIndexed() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "indexedProperty", 1);

		// WHEN and THEN
		assertThat(property.isIndexed(), equalTo(true));
	}

	@Test
	void testIsMapped() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "mappedProperty", "hello");

		// WHEN and THEN
		assertThat(property.isMapped(), equalTo(true));
	}

	@Test
	void testIsIndexed_notIndexed() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "fieldProperty");

		// WHEN and THEN
		assertThat(property.isIndexed(), equalTo(false));
	}

	@Test
	void testIsMapped_notIndexed() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "fieldProperty");

		// WHEN and THEN
		assertThat(property.isMapped(), equalTo(false));
	}

	@Test
	void testIsReadable() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");

		// WHEN and THEN
		assertThat(property.isReadable(), equalTo(true));
	}

	@Test
	void testIsReadable_fieldDoesNotExist() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "fantasyProperty");

		// WHEN and THEN
		assertThat(property.isReadable(), equalTo(false));
	}

	@Test
	void testIsWritable() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");

		// WHEN and THEN
		assertThat(property.isWritable(), equalTo(true));
	}

	@Test
	void testIsWritable_fieldDoesNotExist() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "fantasyProperty");

		// WHEN and THEN
		assertThat(property.isWritable(), equalTo(false));
	}

	@Test
	void testHasFxProperty() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");

		// WHEN and THEN
		assertThat(property.hasFxProperty(), equalTo(true));
	}

	@Test
	void testHasFxProperty_noPropertyGetterAvailable() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaBeanProperty");

		// WHEN and THEN
		assertThat(property.hasFxProperty(), equalTo(false));
	}

	@Test
	void testGetFxProperty() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");

		// WHEN and THEN
		assertThat(property.getFxProperty(model), equalTo(model.javaFXProperty));
	}

	@Test
	void testGetFxProperty_noPropertyGetterAvailable() {
		// GIVEN
		final Model model = model();
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaBeanProperty");

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> property.getFxProperty(model));

		// THEN
		assertThat(ex.getMessage(), containsString("has no property-getter!"));
	}

	@Test
	void testGetType_javaFXProperty() {
		// GIVEN
		final BeanProperty<String> property = new BeanProperty<>(Model.class, "javaFXProperty");

		// WHEN and THEN
		assertThat(property.getType(), equalTo(String.class));
	}

	@Test
	void testGetType_javaBeanProperty() {
		// GIVEN
		final BeanProperty<Boolean> property = new BeanProperty<>(Model.class, "javaBeanProperty");

		// WHEN and THEN
		assertThat(property.getType(), equalTo(boolean.class));
	}

	private Model model() {
		final Model model = new Model();
		model.setJavaFXProperty("Hello");
		model.setJavaBeanProperty(true);
		model.fieldProperty = "World";
		model.javaFXPropertyWithoutGetterSetter.set("Hello World");
		model.indexedProperty[0] = "Hello";
		model.indexedProperty[1] = "World";
		model.indexedProperty[2] = "!";
		model.mappedProperty.put("hello", "world");
		return model;
	}

	public class Model {

		// JavaFX property with setter, getter and property-getter
		StringProperty javaFXProperty = new SimpleStringProperty();

		// regular Java bean getter and setter
		boolean javaBeanProperty;

		// no getter, no setter - direct field access
		String fieldProperty;

		// Property without getter and setter, but with property-getter
		StringProperty javaFXPropertyWithoutGetterSetter = new SimpleStringProperty();

		String[] indexedProperty = new String[3];

		Map<String, String> mappedProperty = new HashMap<>();

		public final StringProperty javaFXPropertyProperty() {
			return javaFXProperty;
		}

		public final String getJavaFXProperty() {
			return javaFXPropertyProperty().get();
		}

		public final void setJavaFXProperty(final String javaFXProperty) {
			javaFXPropertyProperty().set(javaFXProperty);
		}

		public final StringProperty javaFXPropertyWithoutGetterSetterProperty() {
			return javaFXPropertyWithoutGetterSetter;
		}

		public boolean isJavaBeanProperty() {
			return javaBeanProperty;
		}

		public void setJavaBeanProperty(final boolean javaBeanProperty) {
			this.javaBeanProperty = javaBeanProperty;
		}

		public String[] getIndexedProperty() {
			return indexedProperty;
		}

		public Map<String, String> getMappedProperty() {
			return mappedProperty;
		}
	}

}
