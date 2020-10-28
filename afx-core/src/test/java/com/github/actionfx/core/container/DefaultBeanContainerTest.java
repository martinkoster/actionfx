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
package com.github.actionfx.core.container;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.test.DerivedFromTestView;
import com.github.actionfx.core.test.TestView;
import com.github.actionfx.testing.junit5.HeadlessMonocleExtension;

/**
 * JUnit test case for {@link DefaultBeanContainer}.
 * 
 * @author koster
 *
 */
@ExtendWith(HeadlessMonocleExtension.class)
class DefaultBeanContainerTest {

	@Test
	void testGetBean_singletonById() {
		// GIVEN
		DefaultBeanContainer container = new DefaultBeanContainer();
		container.addBeanDefinition("beanId", TestView.class, true, () -> new TestView());

		// WHEN
		TestView view1 = container.getBean("beanId");
		TestView view2 = container.getBean("beanId");

		// THEN
		assertThat(view1, notNullValue());
		assertThat(view1.isInitializeInvoked(), equalTo(true));
		assertThat(view2, notNullValue());
		assertThat(view2.isInitializeInvoked(), equalTo(true));
		assertThat(view1, sameInstance(view2)); // consecutive calls to getBean yield the same instance
	}

	@Test
	void testGetBean_singletonByType() {
		// GIVEN
		DefaultBeanContainer container = new DefaultBeanContainer();
		container.addBeanDefinition("beanId", TestView.class, true, () -> new TestView());

		// WHEN
		TestView view1 = container.getBean(TestView.class);
		TestView view2 = container.getBean(TestView.class);

		// THEN
		assertThat(view1, notNullValue());
		assertThat(view1.isInitializeInvoked(), equalTo(true));
		assertThat(view2, notNullValue());
		assertThat(view2.isInitializeInvoked(), equalTo(true));
		assertThat(view1, sameInstance(view2)); // consecutive calls to getBean yield the same instance
	}

	@Test
	void testGetBean_singletonByType_superTypeIsRequested() {
		// GIVEN
		DefaultBeanContainer container = new DefaultBeanContainer();
		container.addBeanDefinition("beanId", DerivedFromTestView.class, true, () -> new DerivedFromTestView());

		// WHEN (request the super type 'TestView', although 'DerivedFromTestView' is
		// registered)
		TestView view1 = container.getBean(TestView.class);
		TestView view2 = container.getBean(TestView.class);

		// THEN
		assertThat(view1, notNullValue());
		assertThat(view1.isInitializeInvoked(), equalTo(true));
		assertThat(view2, notNullValue());
		assertThat(view2.isInitializeInvoked(), equalTo(true));
		assertThat(view1, sameInstance(view2)); // consecutive calls to getBean yield the same instance
		assertThat(view1, instanceOf(DerivedFromTestView.class));
	}

	@Test
	void testGetBean_byId_idDoesNotExist() {
		// GIVEN
		DefaultBeanContainer container = new DefaultBeanContainer();
		container.addBeanDefinition("beanId", TestView.class, true, () -> new TestView());

		// WHEN and THEN
		assertThat(container.getBean("someNonExistingId"), nullValue());
	}

	@Test
	void testGetBean_byType_idDoesNotExist() {
		// GIVEN
		DefaultBeanContainer container = new DefaultBeanContainer();
		container.addBeanDefinition("beanId", TestView.class, true, () -> new TestView());

		// WHEN and THEN
		assertThat(container.getBean(String.class), nullValue());
	}

	@Test
	void testGetBean_nonSingletonById() {
		// GIVEN
		DefaultBeanContainer container = new DefaultBeanContainer();
		container.addBeanDefinition("beanId", TestView.class, false, () -> new TestView());

		// WHEN
		TestView view1 = container.getBean("beanId");
		TestView view2 = container.getBean("beanId");

		// THEN
		assertThat(view1, notNullValue());
		assertThat(view1.isInitializeInvoked(), equalTo(true));
		assertThat(view2, notNullValue());
		assertThat(view2.isInitializeInvoked(), equalTo(true));
		assertThat(view1, not(sameInstance(view2))); // consecutive calls to getBean yield the different instance
	}

	@Test
	void testGetBean_nonSingletonByType() {
		// GIVEN
		DefaultBeanContainer container = new DefaultBeanContainer();
		container.addBeanDefinition("beanId", TestView.class, false, () -> new TestView());

		// WHEN
		TestView view1 = container.getBean(TestView.class);
		TestView view2 = container.getBean(TestView.class);

		// THEN
		assertThat(view1, notNullValue());
		assertThat(view1.isInitializeInvoked(), equalTo(true));
		assertThat(view2, notNullValue());
		assertThat(view2.isInitializeInvoked(), equalTo(true));
		assertThat(view1, not(sameInstance(view2))); // consecutive calls to getBean yield the different instances
	}

}
