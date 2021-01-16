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
import static org.hamcrest.Matchers.hasSize;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.test.ClassDerivedFromClassWithRepeatableAnnotations;
import com.github.actionfx.core.test.ClassDerivedFromClassWithSomeAnnotation;
import com.github.actionfx.core.test.ClassWithPostConstructAnnotation;
import com.github.actionfx.core.test.ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation;
import com.github.actionfx.core.test.ClassWithRepeableAnnotations;
import com.github.actionfx.core.test.ClassWithSomeAnnotation;
import com.github.actionfx.core.test.ClassWithoutSomeAnnotation;
import com.github.actionfx.core.test.RepeatableAnnotation;
import com.github.actionfx.core.test.SomeAnnotation;
import com.github.actionfx.core.test.SomeFieldAnnotation;

/**
 * JUnit test case for {@link AnnotationUtils}.
 *
 * @author koster
 *
 */
class AnnotationUtilsTest {

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
	void testFindAllAnnotations() {
		// WHEN
		final List<RepeatableAnnotation> list = AnnotationUtils.findAllAnnotations(ClassWithRepeableAnnotations.class,
				RepeatableAnnotation.class);

		// THEN
		assertThat(list, hasSize(3));
		assertThat(list.stream().map(RepeatableAnnotation::value).collect(Collectors.toList()),
				containsInAnyOrder(1, 2, 3));
	}

	@Test
	void testFindAllAnnotations_foundOnSuperClass() {
		// WHEN
		final List<RepeatableAnnotation> list = AnnotationUtils
				.findAllAnnotations(ClassDerivedFromClassWithRepeatableAnnotations.class, RepeatableAnnotation.class);

		// THEN
		assertThat(list, hasSize(4));
		assertThat(list.stream().map(RepeatableAnnotation::value).collect(Collectors.toList()),
				containsInAnyOrder(1, 2, 3, 4));
	}

	@Test
	void testFindAllAnnotations_annotationNotPresent() {
		// WHEN
		final List<RepeatableAnnotation> list = AnnotationUtils.findAllAnnotations(ClassWithoutSomeAnnotation.class,
				RepeatableAnnotation.class);

		// THEN
		assertThat(list, hasSize(0));
	}

	@Test
	void testInvokeMethodWithAnnotation_withoutClassHierarchy() {
		// GIVEN
		final ClassWithPostConstructAnnotation instance = new ClassWithPostConstructAnnotation();

		// WHEN
		AnnotationUtils.invokeMethodWithAnnotation(ClassWithPostConstructAnnotation.class, instance,
				PostConstruct.class);

		// THEN
		assertThat(instance.isIninitialized1Invoked(), equalTo(true));
	}

	@Test
	void testInvokeMethodWithAnnotation_withClassHierarchy() {
		// GIVEN
		final ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation instance = new ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation();

		// WHEN
		AnnotationUtils.invokeMethodWithAnnotation(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, instance, PostConstruct.class);

		// THEN
		assertThat(instance.isIninitialized1Invoked(), equalTo(true));
		assertThat(instance.isIninitialized2Invoked(), equalTo(true));
	}

	@Test
	void testFindAnnotatedFields_noSuperclassScanning() {
		// WHEN
		final Map<SomeFieldAnnotation, Field> fieldMap = AnnotationUtils.findAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				false);

		// THEN
		thenAssertFieldsInAnyOrder(fieldMap.values(), ANNOTATED_FIELDS_WITHOUT_SUPER_CLASS);
	}

	@Test
	void testFindAnnotatedFields_withSuperclassScanning() {
		// WHEN
		final Map<SomeFieldAnnotation, Field> fieldMap = AnnotationUtils.findAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				true);

		// THEN
		thenAssertFieldsInAnyOrder(fieldMap.values(), ANNOTATED_FIELDS_WITH_SUPER_CLASS);
	}

	@Test
	void testFindAnnotatedFields_withComparatorAscending() {
		// WHEN
		final Map<SomeFieldAnnotation, Field> fieldMap = AnnotationUtils.findAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				true, (o1, o2) -> o1.value().compareTo(o2.value()));

		// THEN
		thenAssertTestAnnotationInSortedOrder(fieldMap.keySet(), "1", "2");
	}

	@Test
	void testFindAnnotatedFields_withComparatorDescending() {
		// WHEN
		final Map<SomeFieldAnnotation, Field> fieldMap = AnnotationUtils.findAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				true, (o1, o2) -> o2.value().compareTo(o1.value()));

		// THEN
		thenAssertTestAnnotationInSortedOrder(fieldMap.keySet(), "2", "1");
	}

	@Test
	void testFindAllAnnotatedFields_noSuperclassScanning() {
		// WHEN
		final List<Field> fields = AnnotationUtils.findAllAnnotatedFields(
				ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation.class, SomeFieldAnnotation.class,
				false);

		// THEN
		thenAssertFieldsInAnyOrder(fields, ANNOTATED_FIELDS_WITHOUT_SUPER_CLASS);
	}

	@Test
	void testFindAllAnnotatedFields_withSuperclassScanning() {
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
