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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.view.View;
import com.github.actionfx.spring.test.app.MainController;
import com.github.actionfx.spring.test.app.PrototypeScopedController;
import com.github.actionfx.spring.test.app.SampleApp;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit integration test case for {@link SpringBeanContainer}.
 * 
 * @author koster
 *
 */
@ExtendWith({ FxThreadForAllMonocleExtension.class, SpringExtension.class })
@ContextConfiguration(classes = SampleApp.class)
class SpringBeanContainerIntegrationTest implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@BeforeAll
	static void onSetup() {
		ActionFX.builder().build();
	}

	@Test
	void testContainer() {
		// GIVEN
		SpringBeanContainer container = new SpringBeanContainer((BeanDefinitionRegistry) applicationContext,
				applicationContext);

		// WHEN
		container.populateContainer(SampleApp.class.getPackageName());

		// THEN
		MainController controller = container.getBean("mainController");
		View view = container.getBean("mainView");
		ActionFX actionFX = container.getBean(ActionFX.class);
		PrototypeScopedController prototypeScopedController = container.getBean(PrototypeScopedController.class);

		assertThat(controller, notNullValue());
		assertThat(controller.getMainView(), notNullValue());
		assertThat(controller.getMainView(), sameInstance(view)); // view is a singleton!
		assertThat(controller.getPrototypeScopedController(), notNullValue());
		// controller is prototyped! so instances must be different!
		assertThat(controller.getPrototypeScopedController(), not(sameInstance(prototypeScopedController)));

		// check, that @Autowired-annotated field in abstract base class is resolved
		assertThat(controller.getActionFX(), notNullValue());
		assertThat(controller.getActionFX(), sameInstance(actionFX)); // type is still a singleton!
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
