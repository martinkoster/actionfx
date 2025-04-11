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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JUnit test case for {@link BeanWrapper}.
 *
 * @author koster
 *
 */
class BeanWrapperTest {

	@Test
	void getPropertyValue_pathIsNotNested() {
		// GIVEN
		final NestedElement nestedElement = new NestedElement(null, null);
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(nestedElement, null);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithNestedElement).getPropertyValue("nestedElement")).isSameAs(nestedElement);
	}

	@Test
	void getPropertyValue_isNested() {
		// GIVEN
		final NestedElement nestedElement = new NestedElement(new StringValueType("hello"),
				new StringValueType("world"));
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(nestedElement, null);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithNestedElement).getPropertyValue("nestedElement.fieldWithGetter.value")).isEqualTo("hello");
	}

	@Test
	void getPropertyValue_isNested_indexedList() {
		// GIVEN
		final List<StringValueType> list = Arrays.asList(new StringValueType("hello"), new StringValueType("world"));
		final ClassWithList classWithList = new ClassWithList(list);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithList).getPropertyValue("list[0].value")).isEqualTo("hello");
		assertThat((Object) BeanWrapper.of(classWithList).getPropertyValue("list[1].value")).isEqualTo("world");
	}

	@Test
	void getPropertyValue_isNested_indexedArray() {
		// GIVEN
		final StringValueType[] array = Arrays.asList(new StringValueType("hello"), new StringValueType("world"))
				.toArray(new StringValueType[2]);
		final ClassWithArray classWithArray = new ClassWithArray(array);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithArray).getPropertyValue("array[0].value")).isEqualTo("hello");
		assertThat((Object) BeanWrapper.of(classWithArray).getPropertyValue("array[1].value")).isEqualTo("world");
	}

	@Test
	void getPropertyValue_isNested_mapped() {
		// GIVEN
		final Map<String, StringValueType> map = new HashMap<>();
		map.put("hello", new StringValueType("hello"));
		map.put("world", new StringValueType("world"));
		final ClassWithMap classWithMap = new ClassWithMap(map);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithMap).getPropertyValue("map(hello).value")).isEqualTo("hello");
		assertThat((Object) BeanWrapper.of(classWithMap).getPropertyValue("map(world).value")).isEqualTo("world");
		assertThat((Object) BeanWrapper.of(classWithMap).getPropertyValue("map(notExisting).value")).isNull();
	}

	@Test
	void getPropertyValue_onePathElementIsNull() {
		// GIVEN
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(null, null);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithNestedElement).getPropertyValue("nestedElement.fieldWithGetter.value")).isNull();
	}

	@Test
	void getPropertyValue_onePathElementHasNoGetter_directFieldAccessIsExecuted() {
		// GIVEN
		final NestedElement nestedElement = new NestedElement(new StringValueType("hello"),
				new StringValueType("world"));
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(nestedElement, null);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithNestedElement).getPropertyValue("nestedElement.fieldWithoutGetter.value")).isEqualTo("world");
	}

	@Test
	void getObservableValue_pathIsNotNested() {
		// GIVEN
		final StringPropertyType stringPropertyType = new StringPropertyType("hello world");

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(stringPropertyType).getFxProperty("value")).isSameAs(stringPropertyType.valueProperty());
	}

	@Test
	void getObservableValue_isNested() {
		// GIVEN
		final StringPropertyType helloPropertyType = new StringPropertyType("hello");
		final StringPropertyType worldPropertyType = new StringPropertyType("world");

		final NestedElementWithProperties nestedElementWithProperties = new NestedElementWithProperties(
				helloPropertyType, worldPropertyType);
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(null,
				nestedElementWithProperties);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithNestedElement)
				.getFxProperty("nestedElementWithProperties.fieldWithGetter.value")).isSameAs(helloPropertyType.valueProperty());
	}

	@Test
	void getObservableValue_onePathElementIsNull() {
		// GIVEN
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(null, null);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithNestedElement).getFxProperty("nestedElement.fieldWithGetter.value")).isNull();
	}

	@Test
	void getObservableValue_onePathElementHasNoGetter_directFieldAccessIsExecuted() {
		// GIVEN
		final StringPropertyType helloPropertyType = new StringPropertyType("hello");
		final StringPropertyType worldPropertyType = new StringPropertyType("world");

		final NestedElementWithProperties nestedElementWithProperties = new NestedElementWithProperties(
				helloPropertyType, worldPropertyType);
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(null,
				nestedElementWithProperties);

		// WHEN and THEN
		assertThat((Object) BeanWrapper.of(classWithNestedElement)
				.getFxProperty("nestedElementWithProperties.fieldWithoutGetter.value")).isSameAs(worldPropertyType.valueProperty());
	}

	/**
	 * Class that holds a nested element
	 *
	 * @author koster
	 *
	 */
	public static class ClassWithNestedElement {

		private final NestedElement nestedElement;

		private final NestedElementWithProperties nestedElementWithProperties;

		public ClassWithNestedElement(final NestedElement nestedElement,
				final NestedElementWithProperties nestedElementWithProperties) {
			this.nestedElement = nestedElement;
			this.nestedElementWithProperties = nestedElementWithProperties;
		}

		public NestedElement getNestedElement() {
			return nestedElement;
		}

		public NestedElementWithProperties getNestedElementWithProperties() {
			return nestedElementWithProperties;
		}

	}

	/**
	 * Class that holds 2 fields, one that can be accessed via a getter and one that
	 * required direct field access.
	 *
	 * @author koster
	 *
	 */
	public static class NestedElement {

		private final StringValueType fieldWithGetter;

		@SuppressWarnings("unused")
		private final StringValueType fieldWithoutGetter;

		public NestedElement(final StringValueType fieldWithGetter, final StringValueType fieldWithoutGetter) {
			this.fieldWithGetter = fieldWithGetter;
			this.fieldWithoutGetter = fieldWithoutGetter;
		}

		public StringValueType getFieldWithGetter() {
			return fieldWithGetter;
		}
	}

	/**
	 * Class that holds 2 fields, one that can be accessed via a getter and one that
	 * required direct field access. The values itself are Property-based.
	 *
	 * @author koster
	 *
	 */
	public static class NestedElementWithProperties {

		private final StringPropertyType fieldWithGetter;

		@SuppressWarnings("unused")
		private final StringPropertyType fieldWithoutGetter;

		public NestedElementWithProperties(final StringPropertyType fieldWithGetter,
				final StringPropertyType fieldWithoutGetter) {
			this.fieldWithGetter = fieldWithGetter;
			this.fieldWithoutGetter = fieldWithoutGetter;
		}

		public StringPropertyType getFieldWithGetter() {
			return fieldWithGetter;
		}
	}

	/**
	 * Class holding a single string value that can be accessed through a getter.
	 *
	 * @author koster
	 *
	 */
	public static class StringValueType {

		private final String value;

		public StringValueType(final String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	/**
	 * Class holding a single string property that can be accessed through a getter.
	 *
	 * @author koster
	 *
	 */
	public static class StringPropertyType {

		private final StringProperty value = new SimpleStringProperty();

		public StringPropertyType(final String value) {
			this.value.set(value);
		}

		public String getValue() {
			return value.get();
		}

		public Property<String> valueProperty() {
			return value;
		}
	}

	public static class ClassWithList {

		private final List<StringValueType> list;

		public ClassWithList(final List<StringValueType> list) {
			this.list = list;
		}

		public List<StringValueType> getList() {
			return list;
		}
	}

	public static class ClassWithArray {

		private final StringValueType[] array;

		public ClassWithArray(final StringValueType[] array) {
			this.array = array;
		}

		public StringValueType[] getArray() {
			return array;
		}
	}

	public static class ClassWithMap {

		private final Map<String, StringValueType> map;

		public ClassWithMap(final Map<String, StringValueType> map) {
			this.map = map;
		}

		public Map<String, StringValueType> getMap() {
			return map;
		}
	}

}
