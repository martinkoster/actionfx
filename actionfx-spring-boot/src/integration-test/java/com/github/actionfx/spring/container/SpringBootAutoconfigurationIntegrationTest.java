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
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

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
import com.github.actionfx.core.container.extension.ControllerExtensionBean;
import com.github.actionfx.spring.test.app.MainController;
import com.github.actionfx.spring.test.app.PrototypeScopedController;
import com.github.actionfx.spring.test.app.SampleApp;
import com.github.actionfx.spring.test.app.ViewWithButtonController;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.event.ActionEvent;
import javafx.event.Event;

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

	@SuppressWarnings("unchecked")
	@BeforeAll
	static void initializeActionFX() {
		ActionFX.builder().scanPackage(SampleApp.class.getPackageName())
				.controllerExtension(CustomControllerExtension.class, AnotherCustomControllerExtension.class).build();
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
		final MainController controller = actionFX.getBean("mainController");
		final PrototypeScopedController prototypeScopedController = actionFX.getBean(PrototypeScopedController.class);

		assertThat(controller, notNullValue());

		assertThat(controller.getPrototypeScopedController(), notNullValue());
		// controller is prototyped! so instances must be different!
		assertThat(controller.getPrototypeScopedController(), not(sameInstance(prototypeScopedController)));

		// check, that @Autowired-annotated field in abstract base class is resolved
		assertThat(controller.getActionFX(), notNullValue());
		assertThat(controller.getActionFX(), sameInstance(actionFX)); // type is still a singleton!
	}

	@Test
	void testControllerBeanPostProcessor() {
		// GIVEN
		final ViewWithButtonController controller = applicationContext.getBean(ViewWithButtonController.class);

		// WHEN (actionMethod is wired to ActionFX button by
		// ControllerBeanPostProcessor)
		Event.fireEvent(controller.getActionFXButton(), new ActionEvent());

		// THEN
		assertThat(controller.isActionFired(), equalTo(true));
	}

	@Test
	void testControllerBeanPostProcessor_customControllerExtensions() {

		// WHEN (controller is retrieved from Spring context)
		applicationContext.getBean(ViewWithButtonController.class);

		// THEN (custom controller extensions have been applied)
		final ControllerExtensionBean ceb = applicationContext.getBean(ControllerExtensionBean.class);
		assertThat(ceb, notNullValue());
		assertThat(ceb.getCustomControllerExtensions(), hasSize(2));
		final Consumer<Object> ext1 = ceb.getCustomControllerExtensions().get(0);
		final Consumer<Object> ext2 = ceb.getCustomControllerExtensions().get(1);
		assertThat(ext1, instanceOf(CustomControllerExtension.class));
		assertThat(ext2, instanceOf(AnotherCustomControllerExtension.class));

		assertThat(((CustomControllerExtension) ext1).getExtendedControllerList(),
				hasItems(ViewWithButtonController.class));
		assertThat(((AnotherCustomControllerExtension) ext2).getExtendedControllerList(),
				hasItems(ViewWithButtonController.class));
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public static class CustomControllerExtension implements Consumer<Object> {

		private final Set<Class<?>> extendedControllerList = new HashSet<>();

		@Override
		public void accept(final Object t) {
			// take the super class, not the class that ByteBuddy has generated
			extendedControllerList.add(t.getClass().getSuperclass());
		}

		public Set<Class<?>> getExtendedControllerList() {
			return extendedControllerList;
		}
	}

	public static class AnotherCustomControllerExtension extends CustomControllerExtension {
	}

}
