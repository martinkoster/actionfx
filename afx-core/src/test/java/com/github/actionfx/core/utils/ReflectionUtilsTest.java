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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.test.ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation;

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
	void testGetFieldValue() throws NoSuchFieldException, SecurityException {
		// GIVEN
		ClassWithField instance = new ClassWithField();

		// WHEN and THEN
		assertThat(ReflectionUtils.getFieldValue(ClassWithField.class.getDeclaredField("field1"), instance),
				equalTo("Hello"));
	}

	@Test
	void testGetFieldValueByGetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		Field helloField = ClassWithMethods.class.getDeclaredField("hello");

		// WHEN
		String hello = (String) ReflectionUtils.getFieldValueByGetter(helloField, new ClassWithMethods());

		// THEN
		assertThat(hello, equalTo("Hello"));
	}

	@Test
	void testGetFieldValueByGetter_fieldNameIsOnlyOneLetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		Field aField = ClassWithMethods.class.getDeclaredField("a");

		// WHEN
		Integer value = (Integer) ReflectionUtils.getFieldValueByGetter(aField, new ClassWithMethods());

		// THEN
		assertThat(value, equalTo(Integer.valueOf(42)));
	}

	@Test
	void testGetFieldValueByGetter_booleanType() throws NoSuchFieldException, SecurityException {
		// GIVEN
		Field voidMethodExecutedField = ClassWithMethods.class.getDeclaredField("voidMethodExecuted");

		// WHEN
		boolean invoked = (boolean) ReflectionUtils.getFieldValueByGetter(voidMethodExecutedField,
				new ClassWithMethods());

		// THEN
		assertThat(invoked, equalTo(false));
	}

	@Test
	void testGetFieldValueByPropertyGetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		Field stringField = ClassWithMethods.class.getDeclaredField("string");

		// WHEN
		StringProperty stringProperty = (StringProperty) ReflectionUtils.getFieldValueByPropertyGetter(stringField,
				new ClassWithMethods());

		// THEN
		assertThat(stringProperty, notNullValue());
	}

	@Test
	void testSetFieldValue() throws NoSuchFieldException, SecurityException {
		// GIVEN
		ClassWithField instance = new ClassWithField();

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
	void testInvokeMethod_returningVoid() throws NoSuchMethodException, SecurityException {
		// GIVEN
		ClassWithMethods instance = new ClassWithMethods();
		Method voidMethod = ClassWithMethods.class.getDeclaredMethod("voidMethod");

		// WHEN
		ReflectionUtils.invokeMethod(voidMethod, instance, Void.class);

		// THEN
		assertThat(instance.isVoidMethodExecuted(), equalTo(true));
	}

	@Test
	void testInvokeMethod_argumentString_returningString() throws NoSuchMethodException, SecurityException {
		// GIVEN
		ClassWithMethods instance = new ClassWithMethods();
		Method sayHelloMethod = ClassWithMethods.class.getDeclaredMethod("sayHello", String.class);

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
		Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "voidMethod");

		// THEN
		assertThat(method, notNullValue());
		assertThat(method.getName(), equalTo("voidMethod"));
	}

	@Test
	void testFindMethod_withArguments() {
		// WHEN
		Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "sayHello", String.class);

		// THEN
		assertThat(method, notNullValue());
		assertThat(method.getName(), equalTo("sayHello"));
	}

	private void thenAssertFieldsInAnyOrder(final Collection<Field> methods, String... exptectedFieldNames) {
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
	}

	public static class DerivedFromClassWithField extends ClassWithField {

		private String field2 = "World";

		public String getField2() {
			return field2;
		}
	}

	public static class ClassWithMethods {

		private boolean voidMethodExecuted = false;

		private String hello = "Hello";

		// member field with just one letter
		private Integer a = Integer.valueOf(42);

		private StringProperty string;

		public void voidMethod() {
			voidMethodExecuted = true;
		}

		public String sayHello(String name) {
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

}
