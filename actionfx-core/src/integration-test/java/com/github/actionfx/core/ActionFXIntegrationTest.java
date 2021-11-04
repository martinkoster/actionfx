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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXSubscribe;
import com.github.actionfx.core.container.extension.ControllerExtensionBean;
import com.github.actionfx.core.test.nestedviewapp.ControllerWithNestedviewOnField;
import com.github.actionfx.core.test.nestedviewapp.NestedTabPaneController;
import com.github.actionfx.core.test.nestedviewapp.NestedViewApp;
import com.github.actionfx.core.test.nestedviewapp.NestedViewController;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

@ExtendWith(FxThreadForAllMonocleExtension.class)
class ActionFXIntegrationTest {

	@AfterEach
	void tearDownActionFX() {
		ActionFX.getInstance().reset();
	}

	/**
	 * Tests a more complicated scenario, where a view embedding another view
	 * via @AFXNestedView on class level.
	 */
	@Test
	void testGetView_withNestedViews_onClassLevel() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final View view = actionFX.getView("mainId");

		// THEN
		assertThat(view, notNullValue());
		assertThat(view.getRootNode(), instanceOf(BorderPane.class));
		final BorderPane borderPane = (BorderPane) view.getRootNode();

		// check that view is properly nested
		assertThat(borderPane.getCenter(), notNullValue());
		assertThat(borderPane.getCenter(), instanceOf(TabPane.class));
		final TabPane tabPane = (TabPane) borderPane.getCenter();

		assertThat(tabPane.getTabs(), hasSize(2));
		assertThat(tabPane.getTabs().get(0).getText(), equalTo("Tab 1"));
		assertThat(tabPane.getTabs().get(1).getText(), equalTo("Tab 2"));

		// check that tab pane 1 has the titled pane inside its anchor pane
		assertThat(tabPane.getTabs().get(0).getContent(), instanceOf(AnchorPane.class));
		final AnchorPane anchorPane = (AnchorPane) tabPane.getTabs().get(0).getContent();
		// inside the anchor pane, we expect an embedded titledpane
		assertThat(anchorPane.getChildren(), hasSize(1));
		assertThat(anchorPane.getChildren().get(0), instanceOf(TitledPane.class));
	}

	/**
	 * Tests a more complicated scenario, where a view embedding another view
	 * via @AFXNestedView on field level.
	 */
	@Test
	@TestInFxThread
	void testCreateInstance_nestedViewsOnFieldLevel() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ControllerWithNestedviewOnField controller = actionFX.getBean(ControllerWithNestedviewOnField.class);

		// THEN (2 AFXNestedView annotations are evaluated)
		assertThat(controller.mainBorderPane, notNullValue());
		assertThat(controller.mainBorderPane.getTop(), notNullValue());
		assertThat(controller.mainBorderPane.getTop(), instanceOf(BorderPane.class));

		assertThat(controller.mainBorderPane.getCenter(), notNullValue());
		assertThat(controller.mainBorderPane.getCenter(), instanceOf(TitledPane.class));
	}

	@Test
	@TestInFxThread
	void testDisplayMainView() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).build();
		actionFX.scanForActionFXComponents();
		final Stage stage = new Stage();

		// WHEN
		actionFX.showMainView(stage);

		// THEN (stage holds scene which in turn holds the view)
		assertThat(stage.getScene(), notNullValue());
		assertThat(stage.getScene().getRoot(), notNullValue());
		assertThat(stage.getScene().getRoot(), instanceOf(BorderPane.class));
		assertThat(actionFX.getPrimaryStage(), sameInstance(stage));
	}

	@Test
	void testGetControllerResourceBundle_byControllerId() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ResourceBundle bundle = actionFX.getControllerResourceBundle("nestedViewController");

		// THEN
		assertThat(bundle, notNullValue());
		assertThat(bundle.getString("label.text"), equalTo("Hello Default World"));
	}

	@Test
	void testGetControllerResourceBundle_byControllerId_butControllerIdDoesNotExist() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ResourceBundle bundle = actionFX.getControllerResourceBundle("fantasyController");

		// THEN
		assertThat(bundle, nullValue());
	}

	@Test
	void testGetControllerResourceBundle_byControllerClass() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ResourceBundle bundle = actionFX.getControllerResourceBundle(NestedViewController.class);

		// THEN
		assertThat(bundle, notNullValue());
		assertThat(bundle.getString("label.text"), equalTo("Hello Default World"));
	}

	@Test
	void testCheckActionFXBeanInjection() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.US).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final NestedViewController controller = actionFX.getBean(NestedViewController.class);

		// THEN
		assertThat(controller, notNullValue());
		assertThat(controller.getLocale(), notNullValue());
		assertThat(controller.getLocale(), equalTo(Locale.US));
		assertThat(controller.getObservableLocale(), notNullValue());
		assertThat(controller.getObservableLocale().getValue(), equalTo(Locale.US));
		assertThat(controller.getActionFX(), sameInstance(ActionFX.getInstance()));
		assertThat(controller.getDialogController(), notNullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCheckActionFXCustomControllerExtensions() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class)
				.controllerExtension(CustomControllerExtension.class, AnotherCustomControllerExtension.class).build();
		actionFX.scanForActionFXComponents();

		// WHEN (we need to request the controller beans for triggering the controller
		// extensions)
		ActionFX.getInstance().getBean(NestedViewController.class);
		ActionFX.getInstance().getBean(NestedTabPaneController.class);

		// THEN
		final ControllerExtensionBean ceb = ActionFX.getInstance().getBean(ControllerExtensionBean.class);
		assertThat(ceb, notNullValue());
		assertThat(ceb.getCustomControllerExtensions(), hasSize(2));
		final Consumer<Object> ext1 = ceb.getCustomControllerExtensions().get(0);
		final Consumer<Object> ext2 = ceb.getCustomControllerExtensions().get(1);
		assertThat(ext1, instanceOf(CustomControllerExtension.class));
		assertThat(ext2, instanceOf(AnotherCustomControllerExtension.class));

		assertThat(((CustomControllerExtension) ext1).getExtendedControllerList(),
				hasItems(NestedViewController.class, NestedTabPaneController.class));
		assertThat(((AnotherCustomControllerExtension) ext2).getExtendedControllerList(),
				hasItems(NestedViewController.class, NestedTabPaneController.class));
	}

	@Test
	void testPublishNotification() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().build();
		actionFX.scanForActionFXComponents();
		actionFX.addController(ControllerWithAFXSubscribe.class);
		final ControllerWithAFXSubscribe controller = actionFX.getBean(ControllerWithAFXSubscribe.class);

		// WHEN
		actionFX.publishNotification("Hello World");

		// THEN
		WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
		assertThat(controller.executionOrder, contains(1, 2, 3, 4));
		assertThat(controller.executionArguments, contains("Hello World", "Hello World", "Hello World"));
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

	@AFXController(viewId = "viewId", fxml = "/testfxml/SampleView.fxml")
	public static class ControllerWithAFXSubscribe {

		List<Integer> executionOrder = new ArrayList<>();

		List<String> executionArguments = new ArrayList<>();

		@AFXSubscribe(value = String.class, order = 2)
		public void onPublish() {
			executionOrder.add(2);
		}

		@AFXSubscribe(value = String.class, order = 1)
		public void onPublish(final String message) {
			executionOrder.add(1);
			executionArguments.add(message);
		}

		@AFXSubscribe(value = String.class, order = 3)
		public void anotherOnPublish(final String message) {
			executionOrder.add(3);
			executionArguments.add(message);
		}

		@AFXSubscribe(value = String.class, async = true, order = 4)
		public void onAsyncPublish(final String message) {
			executionOrder.add(4);
			executionArguments.add(message);
		}

		@AFXSubscribe(value = Integer.class, order = 3)
		public void nonInvoked(final Integer integer) {
			executionOrder.add(999);
			executionArguments.add("NOT EXPECTED");
		}
	}

}
