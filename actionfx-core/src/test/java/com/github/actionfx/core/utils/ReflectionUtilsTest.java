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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.test.ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation;
import com.github.actionfx.core.utils.ReflectionUtils.FieldFilter;

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
	void testInstantiateClass_defaultNoArgConstructor() {
		// WHEN
		final ClassWithDefaultConstructor instance = ReflectionUtils
				.instantiateClass(ClassWithDefaultConstructor.class);

		// THEN
		assertThat(instance, notNullValue());
	}

	@Test
	void testInstantiateClass_defaultNoArgConstructor_emptyArraySupplied() {
		// WHEN
		final ClassWithDefaultConstructor instance = ReflectionUtils.instantiateClass(ClassWithDefaultConstructor.class,
				new Object[0]);

		// THEN
		assertThat(instance, notNullValue());
	}

	@Test
	void testInstantiateClass_defaultNoArgConstructorNotPresent() {
		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ReflectionUtils.instantiateClass(ClassWithConstructorArguments.class));

		// THEN
		assertThat(ex.getMessage(), containsString("Can not invoke default no-arg constructor for class"));
	}

	@Test
	void testInstantiateClass_constructorWithArguments() {
		// WHEN
		final ClassWithConstructorArguments instance = ReflectionUtils
				.instantiateClass(ClassWithConstructorArguments.class, "Hello World", Integer.valueOf(42));

		// THEN
		assertThat(instance, notNullValue());
		assertThat(instance.getString(), equalTo("Hello World"));
		assertThat(instance.getInteger(), equalTo(Integer.valueOf(42)));
	}

	@Test
	void testInstantiateClass_constructorWithArgumentsNotPresent() {
		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ReflectionUtils.instantiateClass(ClassWithConstructorArguments.class, Integer.valueOf(42),
						"Wrong order of arguments"));

		// THEN
		assertThat(ex.getMessage(), containsString("Can not invoke constructor for class"));

	}

	@Test
	void testGetAllSuperClassesAndInterfaces() {
		// WHEN
		final Set<Class<?>> result = ReflectionUtils
				.getAllSuperClassesAndInterfaces(ClassWithSuperClassAndInterface.class);

		// THEN
		assertThat(result, containsInAnyOrder(ClassWithSuperClassAndInterface.class, SuperClassWithInterface.class,
				Interface1.class, Interface2.class, Interface3.class, Object.class));
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
	void testSetFieldValueBySetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final ClassWithField instance = new ClassWithField();

		// WHEN
		ReflectionUtils.setFieldValueBySetter(ClassWithField.class.getDeclaredField("field1"), instance, "Yahoo");

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
		ReflectionUtils.invokeMethod(voidMethod, instance);

		// THEN
		assertThat(instance.isVoidMethodExecuted(), equalTo(true));
	}

	@Test
	void testInvokeMethod_argumentString_returningString() throws NoSuchMethodException, SecurityException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method sayHelloMethod = ClassWithMethods.class.getDeclaredMethod("sayHello", String.class);

		// WHEN and THEN
		assertThat(ReflectionUtils.invokeMethod(sayHelloMethod, instance, "World"), equalTo("Hello World"));
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

	@Test
	void testFindMethod_inSuperClass() {
		// WHEN
		final Method method = ReflectionUtils.findMethod(DerivedFromClassWithField.class, "getField1");

		// THEN
		assertThat(method, notNullValue());
		assertThat(method.getName(), equalTo("getField1"));

	}

	@Test
	void testFindMethodByFilter() throws NoSuchMethodException, SecurityException {
		// WHEN
		final List<Method> methods = ReflectionUtils.findMethods(DerivedClassOverridingMethod.class,
				method -> method.getName().equals("method"));

		// THEN (overridden method in base class is not part of the result list)
		assertThat(methods, hasSize(2));
		assertThat(methods,
				containsInAnyOrder(DerivedClassOverridingMethod.class.getMethod("method", String.class, Integer.class),
						BaseClass.class.getMethod("method", Integer.class)));
	}

	@Test
	void testIsPrimitiveWrapper() {
		// WHEN and THEN
		assertThat(ReflectionUtils.isPrimitiveWrapper(Boolean.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveWrapper(Byte.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveWrapper(Character.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveWrapper(Double.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveWrapper(Float.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveWrapper(Integer.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveWrapper(Long.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveWrapper(Short.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveWrapper(Void.class), equalTo(true));

		assertThat(ReflectionUtils.isPrimitiveWrapper(boolean.class), equalTo(false));
		assertThat(ReflectionUtils.isPrimitiveWrapper(byte.class), equalTo(false));
		assertThat(ReflectionUtils.isPrimitiveWrapper(char.class), equalTo(false));
		assertThat(ReflectionUtils.isPrimitiveWrapper(double.class), equalTo(false));
		assertThat(ReflectionUtils.isPrimitiveWrapper(float.class), equalTo(false));
		assertThat(ReflectionUtils.isPrimitiveWrapper(int.class), equalTo(false));
		assertThat(ReflectionUtils.isPrimitiveWrapper(long.class), equalTo(false));
		assertThat(ReflectionUtils.isPrimitiveWrapper(short.class), equalTo(false));
		assertThat(ReflectionUtils.isPrimitiveWrapper(void.class), equalTo(false));
	}

	@Test
	void testIsPrimitiveOrWrapperWrapper() {
		// WHEN and THEN
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Boolean.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Byte.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Character.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Double.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Float.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Integer.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Long.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Short.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Void.class), equalTo(true));

		assertThat(ReflectionUtils.isPrimitiveOrWrapper(boolean.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(byte.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(char.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(double.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(float.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(int.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(long.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(short.class), equalTo(true));
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(void.class), equalTo(true));

		assertThat(ReflectionUtils.isPrimitiveOrWrapper(String.class), equalTo(false));
	}

	@Test
	void testResolvePrimitiveIfNecessary() {
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(boolean.class), equalTo(Boolean.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(byte.class), equalTo(Byte.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(char.class), equalTo(Character.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(double.class), equalTo(Double.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(float.class), equalTo(Float.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(int.class), equalTo(Integer.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(long.class), equalTo(Long.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(short.class), equalTo(Short.class));

		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Boolean.class), equalTo(Boolean.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Byte.class), equalTo(Byte.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Character.class), equalTo(Character.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Double.class), equalTo(Double.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Float.class), equalTo(Float.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Integer.class), equalTo(Integer.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Long.class), equalTo(Long.class));
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Short.class), equalTo(Short.class));
	}

	@Test
	void testDecapitalizeBeanProperty() {
		assertThat(ReflectionUtils.decapitalizeBeanProperty("JavaBeanProperty"), equalTo("javaBeanProperty"));
		assertThat(ReflectionUtils.decapitalizeBeanProperty("JProperty"), equalTo("JProperty"));
	}

	@Test
	void testResolveClassName_classExists_noClassLoaderProvided_useThreadContextClassLoader() {
		// WHEN
		final Class<?> clazz = ReflectionUtils.resolveClassName("com.github.actionfx.core.utils.ReflectionUtils", null);

		// THEN
		assertThat(clazz, notNullValue());
		assertThat(clazz, equalTo(ReflectionUtils.class));
	}

	@Test
	void testResolveClassName_classExists_classLoaderProvided() {
		// WHEN
		final Class<?> clazz = ReflectionUtils.resolveClassName("com.github.actionfx.core.utils.ReflectionUtils",
				ReflectionUtilsTest.class.getClassLoader());

		// THEN
		assertThat(clazz, notNullValue());
		assertThat(clazz, equalTo(ReflectionUtils.class));
	}

	@Test
	void testResolveClassName_classDoesNotExist() {
		// WHEN
		final Class<?> clazz = ReflectionUtils.resolveClassName("com.fantasy.NonExistingClass", null);

		// THEN
		assertThat(clazz, nullValue());
	}

	@Test
	void testResolveClassName_internalClassExists() {
		// WHEN
		final Class<?> clazz = ReflectionUtils
				.resolveClassName("com.github.actionfx.core.utils.ReflectionUtilsTest$ClassWithField", null);

		// THEN
		assertThat(clazz, notNullValue());
		assertThat(clazz, equalTo(ClassWithField.class));
	}

	@Test
	void testGetDefaultClassLoader_contextClassLoader() {
		assertThat(ReflectionUtils.getDefaultClassLoader(), equalTo(Thread.currentThread().getContextClassLoader()));
	}

	@Test
	void testGetDefaultClassLoader_currentClassLoader() {
		// GIVEN
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(null);

		// WHEN and THEN
		assertThat(ReflectionUtils.getDefaultClassLoader(), equalTo(ReflectionUtilsTest.class.getClassLoader()));

		// set the context loader back to the current thread
		Thread.currentThread().setContextClassLoader(cl);
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
	 * Class that has the default no-arg constructor.
	 *
	 * @author koster
	 *
	 */
	public static class ClassWithDefaultConstructor {
	}

	/**
	 * Class that has a constructor that requires arguments.
	 *
	 * @author koster
	 *
	 */
	public static class ClassWithConstructorArguments {

		private final String string;

		private final Integer integer;

		public ClassWithConstructorArguments(final String string, final Integer integer) {
			this.string = string;
			this.integer = integer;
		}

		public String getString() {
			return string;
		}

		public Integer getInteger() {
			return integer;
		}
	}

	/**
	 * Simple interface.
	 *
	 * @author koster
	 *
	 */
	public static interface Interface1 {

	}

	/**
	 * Simple interface extending another interface.
	 *
	 * @author koster
	 *
	 */
	public static interface Interface2 extends Interface3 {

	}

	/**
	 * Simple interface.
	 *
	 * @author koster
	 *
	 */
	public static interface Interface3 {

	}

	/**
	 * Class that extends a super class and also implements an interface.
	 *
	 * @author koster
	 *
	 */
	public static class ClassWithSuperClassAndInterface extends SuperClassWithInterface implements Interface1 {

	}

	/**
	 * A class acting as a super class that also implements an interface.
	 *
	 * @author koster
	 *
	 */
	public static class SuperClassWithInterface implements Interface2 {

	}

	public static class BaseClass {

		public void method(final String string, final Integer integer) {

		}

		public void method(final Integer integer) {

		}
	}

	public static class DerivedClassOverridingMethod extends BaseClass {

		@Override
		public void method(final String string, final Integer integer) {

		}

	}

}
