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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.test.ClassDerivedFromClassWithSomeAnnotation;
import com.github.actionfx.core.test.ClassWithPostConstructAnnotation;
import com.github.actionfx.core.test.ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation;
import com.github.actionfx.core.test.ClassWithSomeAnnotation;
import com.github.actionfx.core.test.ClassWithoutSomeAnnotation;
import com.github.actionfx.core.test.SomeAnnotation;
import com.github.actionfx.core.test.SomeFieldAnnotation;

/**
 * JUnit test case for {@link AnnotationUtils}.
 * 
 * @author koster
 *
 */
class AnnotationUtilsTest {

	private static final List<String> ALL_PUBLIC_METHOD_NAMES = Arrays.asList("initialize1", "initialize2",
			"isIninitialized1Invoked", "isIninitialized2Invoked");

	private static final List<String> ALL_PUBLIC_METHOD_WITH_RETURN_TYPE_NAMES = Arrays
			.asList("isIninitialized1Invoked", "isIninitialized2Invoked");

	private static final List<String> ALL_DECLARED_FIELDS = Arrays.asList("ininitialized1Invoked",
			"ininitialized2Invoked");

	private static final List<String> ANNOTATED_FIELDS_WITH_SUPER_CLASS = Arrays.asList("ininitialized1Invoked",
			"ininitialized2Invoked");

	private static final List<String> ANNOTATED_FIELDS_WITHOUT_SUPER_CLASS = Arrays.asList("ininitialized2Invoked");

	@Test
	void testFindAnnotation_annotationPresentOnGivenClass() {
		// WHEN and THEN
		assertThat(AnnotationUtils.findAnnotation(ClassWithSomeAnnotation.class, SomeAnnotation.class), notNullValue());
	}

	@Test
	void testFindAnnotation_annotationPresentOnSuperClass() {
		// WHEN and THEN
		assertThat(AnnotationUtils.findAnnotation(ClassDerivedFromClassWithSomeAnnotation.class, SomeAnnotation.class),
				notNullValue());
	}

	@Test
	void testFindAnnotation_annotationNotPresent() {
		// WHEN and THEN
		assertThat(AnnotationUtils.findAnnotation(ClassWithoutSomeAnnotation.class, SomeAnnotation.class), nullValue());
	}

	@Test
	void testInvokeMethodWithAnnotation_withoutClassHierarchy() {
		// GIVEN
		ClassWithPostConstructAnnotation instance = new ClassWithPostConstructAnnotation();

		// WHEN
		AnnotationUtils.invokeMethodWithAnnotation(ClassWithPostConstructAnnotation.class, instance,
				PostConstruct.class);

		// THEN
		assertThat(instance.isIninitialized1Invoked(), equalTo(true));
	}

	@Test
	void testInvokeMethodWithAnnotation_withClassHierarchy() {
		// GIVEN
		ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation instance = new ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation();

		// WHEN
		AnnotationUtils.invokeMethodWithAnnotation(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, instance, PostConstruct.class);

		// THEN
		assertThat(instance.isIninitialized1Invoked(), equalTo(true));
		assertThat(instance.isIninitialized2Invoked(), equalTo(true));
	}

	@Test
	public void testFindAllPublicMethods() {
		// WHEN
		final List<Method> methodList = AnnotationUtils
				.findAllPublicMethods(ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class);

		// THEN
		thenAssertMethodNamesList(methodList, ALL_PUBLIC_METHOD_NAMES);
	}

	@Test
	public void testFindPublicMethodsWithReturnType() {
		// WHEN
		final List<Method> methodList = AnnotationUtils.findPublicMethodsWithReturnType(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class);

		// THEN
		thenAssertMethodNamesList(methodList, ALL_PUBLIC_METHOD_WITH_RETURN_TYPE_NAMES);
	}

	private void thenAssertMethodNamesList(final List<Method> methods, final List<String> exptectedMethodNames) {
		assertThat(methods, notNullValue());
		assertThat(methods.stream().map(Method::getName).collect(Collectors.toList()),
				containsInAnyOrder(exptectedMethodNames.toArray()));
	}

	@Test
	public void testFindAnnotatedFields_noSuperclassScanning() {
		// WHEN
		final Map<SomeFieldAnnotation, Field> fieldMap = AnnotationUtils.findAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				false);

		// THEN
		thenAssertFieldsInAnyOrder(fieldMap.values(), ANNOTATED_FIELDS_WITHOUT_SUPER_CLASS);
	}

	@Test
	public void testFindAnnotatedFields_withSuperclassScanning() {
		// WHEN
		final Map<SomeFieldAnnotation, Field> fieldMap = AnnotationUtils.findAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				true);

		// THEN
		thenAssertFieldsInAnyOrder(fieldMap.values(), ANNOTATED_FIELDS_WITH_SUPER_CLASS);
	}

	@Test
	public void testFindAnnotatedFields_withComparatorAscending() {
		// WHEN
		final Map<SomeFieldAnnotation, Field> fieldMap = AnnotationUtils.findAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				true, (o1, o2) -> o1.value().compareTo(o2.value()));

		// THEN
		thenAssertTestAnnotationInSortedOrder(fieldMap.keySet(), "1", "2");
	}

	@Test
	public void testFindAnnotatedFields_withComparatorDescending() {
		// WHEN
		final Map<SomeFieldAnnotation, Field> fieldMap = AnnotationUtils.findAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				true, (o1, o2) -> o2.value().compareTo(o1.value()));

		// THEN
		thenAssertTestAnnotationInSortedOrder(fieldMap.keySet(), "2", "1");
	}

	@Test
	public void testFindAllDeclaredFields() {
		// WHEN
		final List<Field> fields = AnnotationUtils
				.findAllDeclaredFields(ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class);

		// THEN
		thenAssertFieldsInAnyOrder(fields, ALL_DECLARED_FIELDS);
	}

	@Test
	public void testFindAllAnnotatedFields_noSuperclassScanning() {
		// WHEN
		final List<Field> fields = AnnotationUtils.findAllAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				false);

		// THEN
		thenAssertFieldsInAnyOrder(fields, ANNOTATED_FIELDS_WITHOUT_SUPER_CLASS);
	}

	@Test
	public void testFindAllAnnotatedFields_withSuperclassScanning() {
		// WHEN
		final List<Field> fields = AnnotationUtils.findAllAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				true);

		// THEN
		thenAssertFieldsInAnyOrder(fields, ANNOTATED_FIELDS_WITH_SUPER_CLASS);
	}

	private void thenAssertFieldsInAnyOrder(final Collection<Field> methods, final List<String> exptectedFieldNames) {
		assertThat(methods, notNullValue());
		assertThat(methods.stream().map(Field::getName).collect(Collectors.toList()),
				hasItems(exptectedFieldNames.toArray())); // hasItems instead of containsInAnyOrder, because Jacoco
															// dynamically adds fields :(
	}

	private void thenAssertTestAnnotationInSortedOrder(final Collection<SomeFieldAnnotation> testAnnotations,
			final String... exptectedOrderValues) {
		assertThat(testAnnotations, notNullValue());
		assertThat(testAnnotations.stream().map(SomeFieldAnnotation::value).collect(Collectors.toList()),
				contains(exptectedOrderValues));
	}
}
