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
package com.github.actionfx.spring.container;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.view.View;
import com.github.actionfx.spring.test.app.MainController;
import com.github.actionfx.spring.test.app.PrototypeScopedController;
import com.github.actionfx.spring.test.app.SampleApp;

/**
 * JUnit test case for {@link SpringBeanContainer}.
 *
 * @author koster
 *
 */
@ExtendWith(MockitoExtension.class)
class SpringBeanContainerTest {

	private SpringBeanContainer container;

	@Mock
	private BeanDefinitionRegistry registry;

	@Mock
	private ApplicationContext appContext;

	@Captor
	private ArgumentCaptor<BeanDefinition> beanDefinitionCaptor;

	@Captor
	private ArgumentCaptor<String> beanNameCaptor;

	@BeforeAll
	static void initializeActionFX() {
		ActionFX.builder().build();
	}

	@AfterAll
	static void resetActionFX() {
		ActionFX.getInstance().reset();
	}

	@BeforeEach
	void onSetup() {
		container = new SpringBeanContainer(registry, appContext);
	}

	@Test
	void testPopulateContainer() {
		// WHEN
		container.populateContainer(SampleApp.class.getPackageName());

		// THEN (4 beans are registered (2 x controller, 2 x views)
		verify(registry, times(4)).registerBeanDefinition(beanNameCaptor.capture(), beanDefinitionCaptor.capture());
		final List<String> beanNames = beanNameCaptor.getAllValues();
		final List<BeanDefinition> beanDefinitions = beanDefinitionCaptor.getAllValues();
		assertThat(beanNames, contains("mainController", "mainView", "prototypeScopedController", "prototypeView"));
		assertBeanDefinitionFor(beanDefinitions.get(0), MainController.class, true);
		assertBeanDefinitionFor(beanDefinitions.get(1), View.class, true);
		assertBeanDefinitionFor(beanDefinitions.get(2), PrototypeScopedController.class, false);
		assertBeanDefinitionFor(beanDefinitions.get(3), View.class, false);
	}

	@Test
	void addBeanDefinition() {
		// WHEN
		container.addBeanDefinition("mainController", MainController.class, true, true, MainController::new);

		// THEN
		verify(registry, times(1)).registerBeanDefinition(beanNameCaptor.capture(), beanDefinitionCaptor.capture());
		assertThat(beanNameCaptor.getValue(), equalTo("mainController"));
		assertBeanDefinitionFor(beanDefinitionCaptor.getValue(), MainController.class, true);
	}

	@Test
	void getBean_byName() {
		// GIVEN
		final MainController controller = new MainController();
		when(appContext.getBean(ArgumentMatchers.eq("mainController"))).thenReturn(controller);

		// WHEN and THEN
		assertThat(container.getBean("mainController"), sameInstance(controller));
		verify(appContext, times(1)).getBean(ArgumentMatchers.eq("mainController"));
	}

	@Test
	void getBean_byClass() {
		// GIVEN
		final MainController controller = new MainController();
		when(appContext.getBean(ArgumentMatchers.eq(MainController.class))).thenReturn(controller);

		// WHEN and THEN
		assertThat(container.getBean(MainController.class), sameInstance(controller));
		verify(appContext, times(1)).getBean(ArgumentMatchers.eq(MainController.class));
	}

	private void assertBeanDefinitionFor(final BeanDefinition definition, final Class<?> beanClass,
			final boolean singleton) {
		assertThat(definition.getBeanClassName(), equalTo(beanClass.getCanonicalName()));
		assertThat(definition.isSingleton(), equalTo(singleton));
	}
}
