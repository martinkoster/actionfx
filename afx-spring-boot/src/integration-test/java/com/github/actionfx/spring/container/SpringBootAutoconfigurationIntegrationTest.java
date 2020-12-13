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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.view.View;
import com.github.actionfx.spring.test.app.MainController;
import com.github.actionfx.spring.test.app.PrototypeScopedController;
import com.github.actionfx.spring.test.app.SampleApp;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit integration test case for Spring Boot autoconfiguration and especially
 * for adding {@link AFXApplicationContextInitializer} to Spring's context
 * initializers defined via META-INF/spring.factories.
 *
 * @author koster
 *
 */
@ExtendWith({ FxThreadForAllMonocleExtension.class })
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ContextConfiguration(classes = SampleApp.class)
class SpringBootAutoconfigurationIntegrationTest implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@BeforeAll
	static void initializeActionFX() {
		ActionFX.builder().scanPackage(SampleApp.class.getPackageName()).build();
	}

	@AfterAll
	static void resetActionFX() {
		ActionFX.getInstance().reset();
	}

	/**
	 * Due to autoconfiguration and application context intialization, we expect to
	 * have a fully setup ActionFX instance in the Spring context.
	 */
	@Test
	void testAFXApplicationContextInitializer() {

		// WHEN (setup in AFXAutoconfiguration works fine)
		final ActionFX actionFX = applicationContext.getBean(ActionFX.class);

		// THEN (make sure that also the applicationContextInitializer has been setup
		// properly)
		final MainController controller = actionFX.getController("mainController");
		final View view = actionFX.getView("mainView");
		final PrototypeScopedController prototypeScopedController = actionFX
				.getController(PrototypeScopedController.class);

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
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
