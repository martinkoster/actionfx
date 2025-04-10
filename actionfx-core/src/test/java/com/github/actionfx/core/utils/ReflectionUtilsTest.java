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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
		assertThat(instance).isNotNull();
	}

	@Test
	void testInstantiateClass_defaultNoArgConstructor_emptyArraySupplied() {
		// WHEN
		final ClassWithDefaultConstructor instance = ReflectionUtils.instantiateClass(ClassWithDefaultConstructor.class,
				new Object[0]);

		// THEN
		assertThat(instance).isNotNull();
	}

	@Test
	void testInstantiateClass_defaultNoArgConstructorNotPresent() {
		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ReflectionUtils.instantiateClass(ClassWithConstructorArguments.class));

		// THEN
		assertThat(ex.getMessage()).contains("Can not invoke default no-arg constructor for class");
	}

	@Test
	void testInstantiateClass_constructorWithArguments() {
		// WHEN
		final ClassWithConstructorArguments instance = ReflectionUtils
				.instantiateClass(ClassWithConstructorArguments.class, "Hello World", Integer.valueOf(42));

		// THEN
		assertThat(instance).isNotNull();
		assertThat(instance.getString()).isEqualTo("Hello World");
		assertThat(instance.getInteger()).isEqualTo(Integer.valueOf(42));
	}

	@Test
	void testInstantiateClass_constructorWithArgumentsNotPresent() {
		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ReflectionUtils.instantiateClass(ClassWithConstructorArguments.class, Integer.valueOf(42),
						"Wrong order of arguments"));

		// THEN
		assertThat(ex.getMessage()).contains("Can not invoke constructor for class");

	}

	@Test
	void testGetAllSuperClassesAndInterfaces() {
		// WHEN
		final Set<Class<?>> result = ReflectionUtils
				.getAllSuperClassesAndInterfaces(ClassWithSuperClassAndInterface.class);

		// THEN
		assertThat(result).containsExactlyInAnyOrder(ClassWithSuperClassAndInterface.class, SuperClassWithInterface.class, Interface1.class, Interface2.class, Interface3.class, Object.class);
	}

	@Test
	void testGetFieldValue() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final ClassWithField instance = new ClassWithField();

		// WHEN and THEN
		assertThat(ReflectionUtils.getFieldValue(ClassWithField.class.getDeclaredField("field1"), instance)).isEqualTo("Hello");
	}

	@Test
	void testGetFieldValueByGetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field helloField = ClassWithMethods.class.getDeclaredField("hello");

		// WHEN
		final String hello = (String) ReflectionUtils.getFieldValueByGetter(helloField, new ClassWithMethods());

		// THEN
		assertThat(hello).isEqualTo("Hello");
	}

	@Test
	void testGetFieldValueByGetter_fieldNameIsOnlyOneLetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field aField = ClassWithMethods.class.getDeclaredField("a");

		// WHEN
		final Integer value = (Integer) ReflectionUtils.getFieldValueByGetter(aField, new ClassWithMethods());

		// THEN
		assertThat(value).isEqualTo(Integer.valueOf(42));
	}

	@Test
	void testGetFieldValueByGetter_booleanType() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field voidMethodExecutedField = ClassWithMethods.class.getDeclaredField("voidMethodExecuted");

		// WHEN
		final boolean invoked = (boolean) ReflectionUtils.getFieldValueByGetter(voidMethodExecutedField,
				new ClassWithMethods());

		// THEN
		assertThat(invoked).isFalse();
	}

	@Test
	void testGetFieldValueByGetter_noGetterExists() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field fieldWithoutGetterSetter = ClassWithMethods.class.getDeclaredField("fieldWithoutGetterSetter");

		// WHEN and THEN
		assertThatThrownBy(() -> ReflectionUtils.getFieldValueByGetter(fieldWithoutGetterSetter, new ClassWithMethods()))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Getter method not found");
	}


	@Test
	void testGetFieldValueByPropertyGetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field stringField = ClassWithMethods.class.getDeclaredField("string");

		// WHEN
		final StringProperty stringProperty = (StringProperty) ReflectionUtils
				.getFieldValueByPropertyGetter(stringField, new ClassWithMethods());

		// THEN
		assertThat(stringProperty).isNotNull();
	}

	@Test
	void testGetFieldValueByPropertyGetter_noPropertyGetterExists() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field fieldWithoutGetterSetter = ClassWithMethods.class.getDeclaredField("fieldWithoutGetterSetter");

		// WHEN and THEN
		assertThatThrownBy(() -> ReflectionUtils.getFieldValueByPropertyGetter(fieldWithoutGetterSetter, new ClassWithMethods()))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Property-Getter method not found");
	}

	@Test
	void testSetFieldValue() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final ClassWithField instance = new ClassWithField();

		// WHEN
		ReflectionUtils.setFieldValue(ClassWithField.class.getDeclaredField("field1"), instance, "Yahoo");

		// THEN
		assertThat(instance.getField1()).isEqualTo("Yahoo");
	}

	@Test
	void testSetFieldValueBySetter() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final ClassWithField instance = new ClassWithField();

		// WHEN
		ReflectionUtils.setFieldValueBySetter(ClassWithField.class.getDeclaredField("field1"), instance, "Yahoo");

		// THEN
		assertThat(instance.getField1()).isEqualTo("Yahoo");
	}

	@Test
	void testSetFieldValueBySetter_noSetterExists() throws NoSuchFieldException, SecurityException {
		// GIVEN
		final Field fieldWithoutGetterSetter = ClassWithMethods.class.getDeclaredField("fieldWithoutGetterSetter");

		// WHEN and THEN
		assertThatThrownBy(() -> ReflectionUtils.setFieldValueBySetter(fieldWithoutGetterSetter, new ClassWithMethods(), 42))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Setter method not found");
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
		assertThat(ReflectionUtils.findField(ClassWithField.class, "field1")).isNotNull();
		assertThat(ReflectionUtils.findField(DerivedFromClassWithField.class, "field1")).isNotNull();
	}

	@Test
	void testFindFields() {
		// GIVEN
		final FieldFilter filter = field -> "field1".equals(field.getName());

		// WHEN
		final List<Field> fields = ReflectionUtils.findFields(DerivedFromClassWithField.class, filter);

		// THEN
		assertThat(fields).hasSize(1);
		assertThat(fields.get(0).getName()).isEqualTo("field1");
	}

	@Test
	void testInvokeMethod_returningVoid() throws NoSuchMethodException, SecurityException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method voidMethod = ClassWithMethods.class.getDeclaredMethod("voidMethod");

		// WHEN
		ReflectionUtils.invokeMethod(voidMethod, instance);

		// THEN
		assertThat(instance.isVoidMethodExecuted()).isTrue();
	}

	@Test
	void testInvokeMethod_argumentString_returningString() throws NoSuchMethodException, SecurityException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method sayHelloMethod = ClassWithMethods.class.getDeclaredMethod("sayHello", String.class);

		// WHEN and THEN
		assertThat((Object) ReflectionUtils.invokeMethod(sayHelloMethod, instance, "World")).isEqualTo("Hello World");
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
		assertThat(method).isNotNull();
		assertThat(method.getName()).isEqualTo("voidMethod");
	}

	@Test
	void testFindMethod_withArguments() {
		// WHEN
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "sayHello", String.class);

		// THEN
		assertThat(method).isNotNull();
		assertThat(method.getName()).isEqualTo("sayHello");
	}

	@Test
	void testFindMethod_inSuperClass() {
		// WHEN
		final Method method = ReflectionUtils.findMethod(DerivedFromClassWithField.class, "getField1");

		// THEN
		assertThat(method).isNotNull();
		assertThat(method.getName()).isEqualTo("getField1");

	}

	@Test
	void testFindMethodByFilter() throws NoSuchMethodException, SecurityException {
		// WHEN
		final List<Method> methods = ReflectionUtils.findMethods(DerivedClassOverridingMethod.class,
				method -> "method".equals(method.getName()));

		// THEN (overridden method in base class is not part of the result list)
		assertThat(methods).hasSize(2);
		assertThat(methods).containsExactlyInAnyOrder(DerivedClassOverridingMethod.class.getMethod("method", String.class, Integer.class), BaseClass.class.getMethod("method", Integer.class));
	}

	@Test
	void testIsPrimitiveWrapper() {
		// WHEN and THEN
		assertThat(ReflectionUtils.isPrimitiveWrapper(Boolean.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveWrapper(Byte.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveWrapper(Character.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveWrapper(Double.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveWrapper(Float.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveWrapper(Integer.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveWrapper(Long.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveWrapper(Short.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveWrapper(Void.class)).isTrue();

		assertThat(ReflectionUtils.isPrimitiveWrapper(boolean.class)).isFalse();
		assertThat(ReflectionUtils.isPrimitiveWrapper(byte.class)).isFalse();
		assertThat(ReflectionUtils.isPrimitiveWrapper(char.class)).isFalse();
		assertThat(ReflectionUtils.isPrimitiveWrapper(double.class)).isFalse();
		assertThat(ReflectionUtils.isPrimitiveWrapper(float.class)).isFalse();
		assertThat(ReflectionUtils.isPrimitiveWrapper(int.class)).isFalse();
		assertThat(ReflectionUtils.isPrimitiveWrapper(long.class)).isFalse();
		assertThat(ReflectionUtils.isPrimitiveWrapper(short.class)).isFalse();
		assertThat(ReflectionUtils.isPrimitiveWrapper(void.class)).isFalse();
	}

	@Test
	void testIsPrimitiveOrWrapperWrapper() {
		// WHEN and THEN
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Boolean.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Byte.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Character.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Double.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Float.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Integer.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Long.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Short.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(Void.class)).isTrue();

		assertThat(ReflectionUtils.isPrimitiveOrWrapper(boolean.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(byte.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(char.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(double.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(float.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(int.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(long.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(short.class)).isTrue();
		assertThat(ReflectionUtils.isPrimitiveOrWrapper(void.class)).isTrue();

		assertThat(ReflectionUtils.isPrimitiveOrWrapper(String.class)).isFalse();
	}

	@Test
	void testResolvePrimitiveIfNecessary() {
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(boolean.class)).isEqualTo(Boolean.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(byte.class)).isEqualTo(Byte.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(char.class)).isEqualTo(Character.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(double.class)).isEqualTo(Double.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(float.class)).isEqualTo(Float.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(int.class)).isEqualTo(Integer.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(long.class)).isEqualTo(Long.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(short.class)).isEqualTo(Short.class);

		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Boolean.class)).isEqualTo(Boolean.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Byte.class)).isEqualTo(Byte.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Character.class)).isEqualTo(Character.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Double.class)).isEqualTo(Double.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Float.class)).isEqualTo(Float.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Integer.class)).isEqualTo(Integer.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Long.class)).isEqualTo(Long.class);
		assertThat(ReflectionUtils.resolvePrimitiveIfNecessary(Short.class)).isEqualTo(Short.class);
	}

	@Test
	void testDecapitalizeBeanProperty() {
		assertThat(ReflectionUtils.decapitalizeBeanProperty("JavaBeanProperty")).isEqualTo("javaBeanProperty");
		assertThat(ReflectionUtils.decapitalizeBeanProperty("JProperty")).isEqualTo("JProperty");
	}

	@Test
	void testResolveClassName_classExists_noClassLoaderProvided_useThreadContextClassLoader() {
		// WHEN
		final Class<?> clazz = ReflectionUtils.resolveClassName("com.github.actionfx.core.utils.ReflectionUtils", null);

		// THEN
		assertThat(clazz).isNotNull();
		assertThat(clazz).isEqualTo(ReflectionUtils.class);
	}

	@Test
	void testResolveClassName_classExists_classLoaderProvided() {
		// WHEN
		final Class<?> clazz = ReflectionUtils.resolveClassName("com.github.actionfx.core.utils.ReflectionUtils",
				ReflectionUtilsTest.class.getClassLoader());

		// THEN
		assertThat(clazz).isNotNull();
		assertThat(clazz).isEqualTo(ReflectionUtils.class);
	}

	@Test
	void testResolveClassName_classDoesNotExist() {
		// WHEN
		final Class<?> clazz = ReflectionUtils.resolveClassName("com.fantasy.NonExistingClass", null);

		// THEN
		assertThat(clazz).isNull();
	}

	@Test
	void testResolveClassName_internalClassExists() {
		// WHEN
		final Class<?> clazz = ReflectionUtils
				.resolveClassName("com.github.actionfx.core.utils.ReflectionUtilsTest$ClassWithField", null);

		// THEN
		assertThat(clazz).isNotNull();
		assertThat(clazz).isEqualTo(ClassWithField.class);
	}

	@Test
	void testGetDefaultClassLoader_contextClassLoader() {
		assertThat(ReflectionUtils.getDefaultClassLoader()).isEqualTo(Thread.currentThread().getContextClassLoader());
	}

	@Test
	void testGetDefaultClassLoader_currentClassLoader() {
		// GIVEN
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(null);

		// WHEN and THEN
		assertThat(ReflectionUtils.getDefaultClassLoader()).isEqualTo(ReflectionUtilsTest.class.getClassLoader());

		// set the context loader back to the current thread
		Thread.currentThread().setContextClassLoader(cl);
	}

	private void thenAssertFieldsInAnyOrder(final Collection<Field> methods, final String... exptectedFieldNames) {
		assertThat(methods).isNotNull();
        assertThat(methods.stream().map(Field::getName).toList()).contains(exptectedFieldNames); // hasItems
	}

	private void thenAssertMethodNamesList(final List<Method> methods, final List<String> exptectedMethodNames) {
		assertThat(methods).isNotNull();
		assertThat(methods.stream().map(Method::getName).toList()).containsExactlyInAnyOrderElementsOf(exptectedMethodNames);
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

		private boolean voidMethodExecuted;

		private final String hello = "Hello";

		// member field with just one letter
		private final Integer a = Integer.valueOf(42);

		// member field with no getter and setter
		private final Integer fieldWithoutGetterSetter = Integer.valueOf(1);

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
