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
package com.github.actionfx.core;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.lang.Thread.UncaughtExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX.ActionFXBuilder;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.container.DefaultBeanContainer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;
import com.github.actionfx.core.instrumentation.bytebuddy.ActionFXByteBuddyEnhancer;
import com.github.actionfx.core.test.app.MainController;
import com.github.actionfx.core.test.app.SampleApp;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit test case for {@link ActionFX}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ActionFXTest {

	@BeforeEach
	void onSetup() {
		// set view manager instance to 'null' in order to force the creation of a
		// ViewManager instance for each test
		ActionFX.instance = null;
	}

	@Test
	void testBuilder_minimal_withConfigurationClass_sampleApp() {
		// WHEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();

		// THEN
		assertThat(actionFX.getEnhancementStrategy(), equalTo(EnhancementStrategy.RUNTIME_INSTRUMENTATION_AGENT));
		assertThat(actionFX.getEnhancer(), instanceOf(ActionFXByteBuddyEnhancer.class));
		assertThat(actionFX.getMainViewId(), equalTo("mainView"));
		assertThat(actionFX.getScanPackage(), equalTo(SampleApp.class.getPackage().getName()));
		assertThat(actionFX.getBeanContainer(), instanceOf(DefaultBeanContainer.class));
		assertThat(actionFX, equalTo(ActionFX.getInstance()));
	}

	@Test
	void testBuilder_minimal_withConfigurationClass_invalidConfigurationClass() {
		// GIVEN
		final ActionFXBuilder builder = ActionFX.builder();

		// WHEN and THEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> builder.configurationClass(AppClassWithoutAFXApplicationAnnotation.class));
		assertThat(ex.getMessage(), containsString("or its super-classes are not annotated with @AFXApplication"));
	}

	@Test
	void testBuilder_configurative() {
		// GIVEN
		final ActionFXEnhancer enhancer = Mockito.mock(ActionFXEnhancer.class);

		// WHEN
		final ActionFX actionFX = ActionFX.builder().scanPackage(SampleApp.class.getPackage().getName())
				.mainViewId("mainView").actionFXEnhancer(enhancer).enhancementStrategy(EnhancementStrategy.SUBCLASSING)
				.build();

		// THEN
		assertThat(actionFX.getEnhancementStrategy(), equalTo(EnhancementStrategy.SUBCLASSING));
		assertThat(actionFX.getEnhancer(), equalTo(enhancer));
		assertThat(actionFX.getMainViewId(), equalTo("mainView"));
		assertThat(actionFX.getScanPackage(), equalTo(SampleApp.class.getPackage().getName()));
		assertThat(actionFX.getBeanContainer(), instanceOf(DefaultBeanContainer.class));
		assertThat(actionFX, equalTo(ActionFX.getInstance()));
	}

	@Test
	void testBuilder_notYetBuilt() {

		// WHEN and THEN (
		assertThrows(IllegalStateException.class, ActionFX::getInstance);
	}

	@Test
	void testBuilder_alreadyBuilt() {

		// GIVEN (instance is built)
		final ActionFX actionFX = ActionFX.builder().build();

		// WHEN and THEN (
		assertThat(actionFX, notNullValue());
		assertThrows(IllegalStateException.class, () -> ActionFX.builder().build());
	}

	@Test
	void testBuilder_uncaughtExceptionHandler() {
		// GIVEN
		final UncaughtExceptionHandler handler = (t, e) -> {
		};

		// WHEN
		final ActionFX actionFX = ActionFX.builder().uncaughtExceptionHandler(handler).build();

		// THEN
		assertThat(actionFX, notNullValue());
		assertThat(Thread.getDefaultUncaughtExceptionHandler(), sameInstance(handler));

	}

	@Test
	void testScanComponents_usingDefaultBeanContainer() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();

		// WHEN
		actionFX.scanForActionFXComponents();

		// THEN
		final View view = actionFX.getView("mainView");
		final MainController mainControllerById = actionFX.getController("mainController");
		final MainController mainControllerByClassName = actionFX.getController(MainController.class);

		assertThat(view, notNullValue());
		assertThat(mainControllerById, notNullValue());
		assertThat(mainControllerByClassName, notNullValue());
		assertThat(mainControllerById, sameInstance(mainControllerByClassName));

	}

	@Test
	void testScanComponents_usingCustomBeanContainer() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();
		final BeanContainerFacade customBeanContainer = Mockito.mock(BeanContainerFacade.class);
		final ArgumentCaptor<String> rootPackageCaptor = ArgumentCaptor.forClass(String.class);

		// WHEN
		actionFX.scanForActionFXComponents(customBeanContainer);

		// THEN (custom container has be asked to populate container with the
		// rootPackage of SampleApp)
		verify(customBeanContainer).populateContainer(rootPackageCaptor.capture());
		assertThat(rootPackageCaptor.getValue(), equalTo(SampleApp.class.getPackageName()));
	}

	@Test
	void testScanComponents_scanAlreadyPerformed_illegalState() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();

		// WHEN
		actionFX.scanForActionFXComponents();

		// THEN (another call to scanComponents results in an exception)
		assertThrows(IllegalStateException.class, () -> actionFX.scanForActionFXComponents());
	}

	public static class AppClassWithoutAFXApplicationAnnotation {

	}

}
