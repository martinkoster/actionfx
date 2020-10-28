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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.test.ClassDerivedFromClassWithSomeAnnotation;
import com.github.actionfx.core.test.ClassWithPostConstructAnnotation;
import com.github.actionfx.core.test.ClassWithPostConstructDerivedFromClassWithPostConstructAnnotation;
import com.github.actionfx.core.test.ClassWithSomeAnnotation;
import com.github.actionfx.core.test.ClassWithoutSomeAnnotation;
import com.github.actionfx.core.test.SomeAnnotation;

/**
 * JUnit test case for {@link AnnotationUtils}.
 * 
 * @author koster
 *
 */
class AnnotationUtilsTest {

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

}
