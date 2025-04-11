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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.github.actionfx.spring.test.app.ViewWithButtonController;

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
		container = new SpringBeanContainer();
	}

	@Test
	void testRunComponentScan() {
		// GIVEN
		container.onSpringContextAvailable(registry, appContext);

		// WHEN
		container.runComponentScan(SampleApp.class.getPackageName());

		// THEN (6 beans are registered (3 x controller, 3 x views)
		verify(registry, times(6)).registerBeanDefinition(beanNameCaptor.capture(), beanDefinitionCaptor.capture());
		final List<String> beanNames = beanNameCaptor.getAllValues();
		final List<BeanDefinition> beanDefinitions = beanDefinitionCaptor.getAllValues();
		assertThat(beanNames).containsExactly("mainController", "mainView", "prototypeScopedController", "prototypeView", "viewWithButtonController", "viewWithButton");
		assertBeanDefinitionFor(beanDefinitions.get(0), MainController.class, true, false);
		assertBeanDefinitionFor(beanDefinitions.get(1), View.class, true, false);
		assertBeanDefinitionFor(beanDefinitions.get(2), PrototypeScopedController.class, false, true);
		assertBeanDefinitionFor(beanDefinitions.get(3), View.class, false, true);
		assertBeanDefinitionFor(beanDefinitions.get(4), ViewWithButtonController.class, true, true);
		assertBeanDefinitionFor(beanDefinitions.get(5), View.class, true, true);
	}

	@Test
	void testRunComponentScan_springNotYetInitialized() {
		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> container.runComponentScan(SampleApp.class.getPackageName()));

		assertThat(ex.getMessage()).contains("Spring context is not available yet!");
	}

	@Test
	void addBeanDefinition() {
		// GIVEN
		container.onSpringContextAvailable(registry, appContext);

		// WHEN
		container.addBeanDefinition("mainController", MainController.class, true, true, MainController::new);

		// THEN
		verify(registry, times(1)).registerBeanDefinition(beanNameCaptor.capture(), beanDefinitionCaptor.capture());
		assertThat(beanNameCaptor.getValue()).isEqualTo("mainController");
		assertBeanDefinitionFor(beanDefinitionCaptor.getValue(), MainController.class, true, true);
	}

	@Test
	void testAddBeanDefinition_springNotYetInitialized() {
		// WHEN Spring is not yet initionilized
		container.addBeanDefinition("mainController", MainController.class, true, true, MainController::new);

		// AND WHEN Spring is initialized
		container.onSpringContextAvailable(registry, appContext);

		// THEN
		verify(registry, times(1)).registerBeanDefinition(eq("mainController"), any());
	}

	@Test
	void addControllerBeanDefinition() {
		// GIVEN
		container.onSpringContextAvailable(registry, appContext);

		// WHEN
		container.addControllerBeanDefinition(MainController.class);

		// THEN
		verify(registry, times(2)).registerBeanDefinition(beanNameCaptor.capture(), beanDefinitionCaptor.capture());
		assertThat(beanNameCaptor.getAllValues().get(0)).isEqualTo("mainController");
		assertBeanDefinitionFor(beanDefinitionCaptor.getAllValues().get(0), MainController.class, true, false);
		assertThat(beanNameCaptor.getAllValues().get(1)).isEqualTo("mainView");
	}

	@Test
	void testAddControllerBeanDefinition_springNotYetInitialized() {
		// GIVEN
		final ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

		// WHEN Spring is not yet initialized
		container.addControllerBeanDefinition(MainController.class);

		// AND WHEN Spring is initialized
		container.onSpringContextAvailable(registry, appContext);

		// THEN
		verify(registry, times(2)).registerBeanDefinition(nameCaptor.capture(), any());
		assertThat(nameCaptor.getAllValues()).containsExactly("mainController", "mainView");
	}

	@Test
	void addControllerBeanDefinition_classIsNotAController() {
		// GIVEN
		container.onSpringContextAvailable(registry, appContext);

		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> container.addControllerBeanDefinition(NonController.class));

		// THEN
		assertThat(ex.getMessage()).contains("is not annotated by @AFXController!");
	}

	@Test
	void getBean_byName() {
		// GIVEN
		container.onSpringContextAvailable(registry, appContext);
		final MainController controller = new MainController();
		when(appContext.getBean(ArgumentMatchers.eq("mainController"))).thenReturn(controller);

		// WHEN and THEN
		assertThat((Object) container.getBean("mainController")).isSameAs(controller);
		verify(appContext, times(1)).getBean(ArgumentMatchers.eq("mainController"));
	}

	@Test
	void testGetBean_byName_springNotYetInitialized() {
		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> container.getBean("mainController"));

		assertThat(ex.getMessage()).contains("Spring context is not available yet!");
	}

	@Test
	void getBean_byClass() {
		// GIVEN
		container.onSpringContextAvailable(registry, appContext);
		final MainController controller = new MainController();
		when(appContext.getBean(ArgumentMatchers.eq(MainController.class))).thenReturn(controller);

		// WHEN and THEN
		assertThat((Object) container.getBean(MainController.class)).isSameAs(controller);
		verify(appContext, times(1)).getBean(ArgumentMatchers.eq(MainController.class));
	}

	@Test
	void testGetBean_byClass_springNotYetInitialized() {
		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> container.getBean(MainController.class));

		assertThat(ex.getMessage()).contains("Spring context is not available yet!");
	}

	private void assertBeanDefinitionFor(final BeanDefinition definition, final Class<?> beanClass,
			final boolean singleton, final boolean lazyInit) {
		assertThat(definition.getBeanClassName()).isEqualTo(beanClass.getCanonicalName());
		assertThat(definition.isSingleton()).isEqualTo(singleton);
		assertThat(definition.isLazyInit()).isEqualTo(lazyInit);
	}

	public static class NonController {

	}
}
