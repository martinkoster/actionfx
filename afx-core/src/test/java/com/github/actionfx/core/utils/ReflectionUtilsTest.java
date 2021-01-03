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
package com.github.actionfx.core.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.test.ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation;
import com.github.actionfx.core.utils.ReflectionUtils.FieldFilter;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JUnit test case for {@link ReflectionUtils}.
 *
 * @author koster
 *
 */
class ReflectionUtilsTest {
	private static final List<String> ALL_PUBLIC_METHOD_NAMES = Arrays.asList("initialize1", "initialize2",
			"isIninitialized1Invoked", "isIninitialized2Invoked");

	private static final List<String> ALL_PUBLIC_METHOD_WITH_RETURN_TYPE_NAMES = Arrays
			.asList("isIninitialized1Invoked", "isIninitialized2Invoked");

	@Test
	void getNestedFieldValue_pathIsNotNested() {
		// GIVEN
		final NestedElement nestedElement = new NestedElement(null, null);
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(nestedElement, null);

		// WHEN and THEN
		assertThat(ReflectionUtils.getNestedFieldValue("nestedElement", classWithNestedElement),
				sameInstance(nestedElement));
	}

	@Test
	void getNestedFieldValue_pathIsNotNested_endsWithDot() {
		// GIVEN
		final NestedElement nestedElement = new NestedElement(null, null);
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(nestedElement, null);

		// WHEN and THEN
		assertThat(ReflectionUtils.getNestedFieldValue("nestedElement.", classWithNestedElement),
				sameInstance(nestedElement));
	}

	@Test
	void getNestedFieldValue_isNested() {
		// GIVEN
		final NestedElement nestedElement = new NestedElement(new StringValueType("hello"),
				new StringValueType("world"));
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(nestedElement, null);

		// WHEN and THEN
		assertThat(ReflectionUtils.getNestedFieldValue("nestedElement.fieldWithGetter.value", classWithNestedElement),
				equalTo("hello"));
	}

	@Test
	void getNestedFieldValue_onePathElementIsNull() {
		// GIVEN
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(null, null);

		// WHEN and THEN
		assertThat(ReflectionUtils.getNestedFieldValue("nestedElement.fieldWithGetter.value", classWithNestedElement),
				nullValue());
	}

	@Test
	void getNestedFieldValue_onePathElementHasNoGetter_directFieldAccessIsExecuted() {
		// GIVEN
		final NestedElement nestedElement = new NestedElement(new StringValueType("hello"),
				new StringValueType("world"));
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(nestedElement, null);

		// WHEN and THEN
		assertThat(
				ReflectionUtils.getNestedFieldValue("nestedElement.fieldWithoutGetter.value", classWithNestedElement),
				equalTo("world"));
	}

	@Test
	void getNestedFieldProperty_pathIsNotNested() {
		// GIVEN
		final StringPropertyType stringPropertyType = new StringPropertyType("hello world");

		// WHEN and THEN
		assertThat(ReflectionUtils.getNestedFieldProperty("value", stringPropertyType),
				sameInstance(stringPropertyType.valueProperty()));
	}

	@Test
	void getNestedFieldProperty_pathIsNotNested_endsWithDot() {
		// GIVEN
		final StringPropertyType stringPropertyType = new StringPropertyType("hello world");

		// WHEN and THEN
		assertThat(ReflectionUtils.getNestedFieldProperty("value.", stringPropertyType),
				sameInstance(stringPropertyType.valueProperty()));
	}

	@Test
	void getNestedFieldProperty_isNested() {
		// GIVEN
		final StringPropertyType helloPropertyType = new StringPropertyType("hello");
		final StringPropertyType worldPropertyType = new StringPropertyType("world");

		final NestedElementWithProperties nestedElementWithProperties = new NestedElementWithProperties(
				helloPropertyType, worldPropertyType);
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(null,
				nestedElementWithProperties);

		// WHEN and THEN
		assertThat(ReflectionUtils.getNestedFieldProperty("nestedElementWithProperties.fieldWithGetter.value",
				classWithNestedElement), sameInstance(helloPropertyType.valueProperty()));
	}

	@Test
	void getNestedFieldProperty_onePathElementIsNull() {
		// GIVEN
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(null, null);

		// WHEN and THEN
		assertThat(
				ReflectionUtils.getNestedFieldProperty("nestedElement.fieldWithGetter.value", classWithNestedElement),
				nullValue());
	}

	@Test
	void getNestedFieldProperty_onePathElementHasNoGetter_directFieldAccessIsExecuted() {
		// GIVEN
		final StringPropertyType helloPropertyType = new StringPropertyType("hello");
		final StringPropertyType worldPropertyType = new StringPropertyType("world");

		final NestedElementWithProperties nestedElementWithProperties = new NestedElementWithProperties(
				helloPropertyType, worldPropertyType);
		final ClassWithNestedElement classWithNestedElement = new ClassWithNestedElement(null,
				nestedElementWithProperties);

		// WHEN and THEN
		assertThat(ReflectionUtils.getNestedFieldProperty("nestedElementWithProperties.fieldWithoutGetter.value",
				classWithNestedElement), sameInstance(worldPropertyType.valueProperty()));
	}

	@Test
	void testGetFieldValue() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final ClassWithField instance = new ClassWithField();

		// WHEN and THEN
		assertThat(ReflectionUtils.getFieldValue(ClassWithField.class.getDeclaredField("field1"), instance),
				equalTo("Hello"));
	}

	@Test
	void testGetFieldValueByGetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field helloField = ClassWithMethods.class.getDeclaredField("hello");

		// WHEN
		final String hello = (String) ReflectionUtils.getFieldValueByGetter(helloField, new ClassWithMethods());

		// THEN
		assertThat(hello, equalTo("Hello"));
	}

	@Test
	void testGetFieldValueByGetter_fieldNameIsOnlyOneLetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field aField = ClassWithMethods.class.getDeclaredField("a");

		// WHEN
		final Integer value = (Integer) ReflectionUtils.getFieldValueByGetter(aField, new ClassWithMethods());

		// THEN
		assertThat(value, equalTo(Integer.valueOf(42)));
	}

	@Test
	void testGetFieldValueByGetter_booleanType() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field voidMethodExecutedField = ClassWithMethods.class.getDeclaredField("voidMethodExecuted");

		// WHEN
		final boolean invoked = (boolean) ReflectionUtils.getFieldValueByGetter(voidMethodExecutedField,
				new ClassWithMethods());

		// THEN
		assertThat(invoked, equalTo(false));
	}

	@Test
	void testGetFieldValueByPropertyGetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field stringField = ClassWithMethods.class.getDeclaredField("string");

		// WHEN
		final StringProperty stringProperty = (StringProperty) ReflectionUtils
				.getFieldValueByPropertyGetter(stringField, new ClassWithMethods());

		// THEN
		assertThat(stringProperty, notNullValue());
	}

	@Test
	void testSetFieldValue() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final ClassWithField instance = new ClassWithField();

		// WHEN
		ReflectionUtils.setFieldValue(ClassWithField.class.getDeclaredField("field1"), instance, "Yahoo");

		// THEN
		assertThat(instance.getField1(), equalTo("Yahoo"));
	}

	@Test
	void testFindAllDeclaredFields() {
		// WHEN
		final List<Field> fields = ReflectionUtils.findAllDeclaredFields(DerivedFromClassWithField.class);

		// THEN
		thenAssertFieldsInAnyOrder(fields, "field1", "field2");
	}

	@Test
	void testFindField() {
		assertThat(ReflectionUtils.findField(ClassWithField.class, "field1"), notNullValue());
		assertThat(ReflectionUtils.findField(DerivedFromClassWithField.class, "field1"), notNullValue());
	}

	@Test
	void testFindFields() {
		// GIVEN
		final FieldFilter filter = field -> field.getName().equals("field1");

		// WHEN
		final List<Field> fields = ReflectionUtils.findFields(DerivedFromClassWithField.class, filter);

		// THEN
		assertThat(fields, hasSize(1));
		assertThat(fields.get(0).getName(), equalTo("field1"));
	}

	@Test
	void testInvokeMethod_returningVoid() throws NoSuchMethodException, SecurityException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method voidMethod = ClassWithMethods.class.getDeclaredMethod("voidMethod");

		// WHEN
		ReflectionUtils.invokeMethod(voidMethod, instance, Void.class);

		// THEN
		assertThat(instance.isVoidMethodExecuted(), equalTo(true));
	}

	@Test
	void testInvokeMethod_argumentString_returningString() throws NoSuchMethodException, SecurityException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method sayHelloMethod = ClassWithMethods.class.getDeclaredMethod("sayHello", String.class);

		// WHEN and THEN
		assertThat(ReflectionUtils.invokeMethod(sayHelloMethod, instance, String.class, "World"),
				equalTo("Hello World"));
	}

	@Test
	void testFindAllPublicMethods() {
		// WHEN
		final List<Method> methodList = ReflectionUtils
				.findAllPublicMethods(ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class);

		// THEN
		thenAssertMethodNamesList(methodList, ALL_PUBLIC_METHOD_NAMES);
	}

	@Test
	void testFindPublicMethodsWithReturnType() {
		// WHEN
		final List<Method> methodList = ReflectionUtils.findPublicMethodsWithReturnType(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class);

		// THEN
		thenAssertMethodNamesList(methodList, ALL_PUBLIC_METHOD_WITH_RETURN_TYPE_NAMES);
	}

	@Test
	void testFindMethod() {
		// WHEN
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "voidMethod");

		// THEN
		assertThat(method, notNullValue());
		assertThat(method.getName(), equalTo("voidMethod"));
	}

	@Test
	void testFindMethod_withArguments() {
		// WHEN
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "sayHello", String.class);

		// THEN
		assertThat(method, notNullValue());
		assertThat(method.getName(), equalTo("sayHello"));
	}

	private void thenAssertFieldsInAnyOrder(final Collection<Field> methods, final String... exptectedFieldNames) {
		assertThat(methods, notNullValue());
		assertThat(methods.stream().map(Field::getName).collect(Collectors.toList()), hasItems(exptectedFieldNames)); // hasItems
	}

	private void thenAssertMethodNamesList(final List<Method> methods, final List<String> exptectedMethodNames) {
		assertThat(methods, notNullValue());
		assertThat(methods.stream().map(Method::getName).collect(Collectors.toList()),
				containsInAnyOrder(exptectedMethodNames.toArray()));
	}

	public static class ClassWithField {

		private String field1 = "Hello";

		public String getField1() {
			return field1;
		}

		public void setField1(final String value) {
			field1 = value;
		}
	}

	public static class DerivedFromClassWithField extends ClassWithField {

		private final String field2 = "World";

		public String getField2() {
			return field2;
		}
	}

	public static class ClassWithMethods {

		private boolean voidMethodExecuted = false;

		private final String hello = "Hello";

		// member field with just one letter
		private final Integer a = Integer.valueOf(42);

		private StringProperty string;

		public void voidMethod() {
			voidMethodExecuted = true;
		}

		public String sayHello(final String name) {
			return "Hello " + name;
		}

		public boolean isVoidMethodExecuted() {
			return voidMethodExecuted;
		}

		public String getHello() {
			return hello;
		}

		public Integer getA() {
			return a;
		}

		/**
		 * Typical lazy-initialization of a property.
		 *
		 * @return the property
		 */
		public StringProperty stringProperty() {
			if (string == null) {
				string = new SimpleStringProperty();
			}
			return string;
		}
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

}
