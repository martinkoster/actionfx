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

import static org.assertj.core.api.Assertions.assertThat;

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
import com.github.actionfx.core.extension.ActionFXExtensionsBean;
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
		assertThat(view).isNotNull();
		assertThat((Object) view.getRootNode()).isInstanceOf(BorderPane.class);
		final BorderPane borderPane = (BorderPane) view.getRootNode();

		// check that view is properly nested
		assertThat(borderPane.getCenter()).isNotNull();
		assertThat(borderPane.getCenter()).isInstanceOf(TabPane.class);
		final TabPane tabPane = (TabPane) borderPane.getCenter();

		assertThat(tabPane.getTabs()).hasSize(2);
		assertThat(tabPane.getTabs().get(0).getText()).isEqualTo("Tab 1");
		assertThat(tabPane.getTabs().get(1).getText()).isEqualTo("Tab 2");

		// check that tab pane 1 has the titled pane inside its anchor pane
		assertThat(tabPane.getTabs().get(0).getContent()).isInstanceOf(AnchorPane.class);
		final AnchorPane anchorPane = (AnchorPane) tabPane.getTabs().get(0).getContent();
		// inside the anchor pane, we expect an embedded titledpane
		assertThat(anchorPane.getChildren()).hasSize(1);
		assertThat(anchorPane.getChildren().get(0)).isInstanceOf(TitledPane.class);
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
		assertThat(controller.mainBorderPane).isNotNull();
		assertThat(controller.mainBorderPane.getTop()).isNotNull();
		assertThat(controller.mainBorderPane.getTop()).isInstanceOf(BorderPane.class);

		assertThat(controller.mainBorderPane.getCenter()).isNotNull();
		assertThat(controller.mainBorderPane.getCenter()).isInstanceOf(TitledPane.class);
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
		assertThat(stage.getScene()).isNotNull();
		assertThat(stage.getScene().getRoot()).isNotNull();
		assertThat(stage.getScene().getRoot()).isInstanceOf(BorderPane.class);
		assertThat(actionFX.getPrimaryStage()).isSameAs(stage);
	}

	@Test
	void testGetControllerResourceBundle_byControllerId() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ResourceBundle bundle = actionFX.getControllerResourceBundle("nestedViewController");

		// THEN
		assertThat(bundle).isNotNull();
		assertThat(bundle.getString("label.text")).isEqualTo("Hello World");
	}

	@Test
	void testGetControllerResourceBundle_byControllerId_butControllerIdDoesNotExist() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ResourceBundle bundle = actionFX.getControllerResourceBundle("fantasyController");

		// THEN
		assertThat(bundle).isNull();
	}

	@Test
	void testGetControllerResourceBundle_byControllerClass() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ResourceBundle bundle = actionFX.getControllerResourceBundle(NestedViewController.class);

		// THEN
		assertThat(bundle).isNotNull();
		assertThat(bundle.getString("label.text")).isEqualTo("Hello World");
	}

	@Test
	void testGetMessage_messageKeyExists() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final String message = actionFX.getMessage(NestedViewController.class, "label.text", "Default message");

		// THEN
		assertThat(message).isEqualTo("Hello World");
	}

	@Test
	void testGetMessage_messageKeyDoesNotExist() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final String message = actionFX.getMessage(NestedViewController.class, "fantasy key", "Default message");

		// THEN
		assertThat(message).isEqualTo("Default message");
	}

	@Test
	void testGetMessage_messageKeyIsBlank() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final String message = actionFX.getMessage(NestedViewController.class, "", "Default message");

		// THEN
		assertThat(message).isEqualTo("Default message");
	}

	@Test
	void testGetMessage_messageKeyIsNull() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final String message = actionFX.getMessage(NestedViewController.class, null, "Default message");

		// THEN
		assertThat(message).isEqualTo("Default message");
	}

	@Test
	void testGetMessage_resourceBundleDoesNotExist() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final String message = actionFX.getMessage(String.class, "label.text", "Default message");

		// THEN
		assertThat(message).isEqualTo("Default message");
	}

	@Test
	void testCheckActionFXBeanInjection() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.US).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final NestedViewController controller = actionFX.getBean(NestedViewController.class);

		// THEN
		assertThat(controller).isNotNull();
		assertThat(controller.getLocale()).isNotNull();
		assertThat(controller.getLocale()).isEqualTo(Locale.US);
		assertThat(controller.getObservableLocale()).isNotNull();
		assertThat(controller.getObservableLocale().getValue()).isEqualTo(Locale.US);
		assertThat(controller.getActionFX()).isSameAs(ActionFX.getInstance());
		assertThat(controller.getDialogController()).isNotNull();
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
		final ActionFXExtensionsBean ceb = ActionFX.getInstance().getBean(ActionFXExtensionsBean.class);
		assertThat(ceb).isNotNull();
		assertThat(ceb.getCustomControllerExtensions()).hasSize(2);
		final Consumer<Object> ext1 = ceb.getCustomControllerExtensions().get(0);
		final Consumer<Object> ext2 = ceb.getCustomControllerExtensions().get(1);
		assertThat(ext1).isInstanceOf(CustomControllerExtension.class);
		assertThat(ext2).isInstanceOf(AnotherCustomControllerExtension.class);

		assertThat(((CustomControllerExtension) ext1).getExtendedControllerList()).contains(NestedViewController.class, NestedTabPaneController.class);
		assertThat(((AnotherCustomControllerExtension) ext2).getExtendedControllerList()).contains(NestedViewController.class, NestedTabPaneController.class);
	}

	@Test
	void testPublishNotification() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().build();
		actionFX.scanForActionFXComponents();
		actionFX.addController(ControllerWithAFXSubscribe.class);
		final ControllerWithAFXSubscribe controller = actionFX.getBean(ControllerWithAFXSubscribe.class);

		// WHEN
		actionFX.publishEvent("Hello World");

		// THEN
		WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
		assertThat(controller.executionOrder).containsExactly(1, 2, 3, 4);
		assertThat(controller.executionArguments).containsExactly("Hello World", "Hello World", "Hello World");
	}

	@Test
	@TestInFxThread
	void testDocking() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(NestedViewApp.class).locale(Locale.US).build();
		actionFX.scanForActionFXComponents();
		final ControllerWithNestedviewOnField controller = actionFX.getBean(ControllerWithNestedviewOnField.class);

		// WHEN
		assertThat(actionFX.isNestedViewDocked("borderPaneTopView")).isEqualTo(true);
		assertThat(actionFX.isNestedViewDocked("borderPaneCenterView")).isEqualTo(true);
		assertThat(controller.mainBorderPane.getTop()).isNotNull();
		assertThat(controller.mainBorderPane.getCenter()).isNotNull();

		actionFX.undockNestedView("borderPaneTopView");
		assertThat(actionFX.isNestedViewDocked("borderPaneTopView")).isEqualTo(false);
		assertThat(actionFX.isNestedViewDocked("borderPaneCenterView")).isEqualTo(true);
		assertThat(controller.mainBorderPane.getTop()).isNull();
		assertThat(controller.mainBorderPane.getCenter()).isNotNull();

		actionFX.undockNestedView("borderPaneCenterView");
		assertThat(actionFX.isNestedViewDocked("borderPaneTopView")).isEqualTo(false);
		assertThat(actionFX.isNestedViewDocked("borderPaneCenterView")).isEqualTo(false);
		assertThat(controller.mainBorderPane.getTop()).isNull();
		assertThat(controller.mainBorderPane.getCenter()).isNull();

		actionFX.dockNestedView("borderPaneTopView");
		actionFX.dockNestedView("borderPaneCenterView");
		assertThat(actionFX.isNestedViewDocked("borderPaneTopView")).isEqualTo(true);
		assertThat(actionFX.isNestedViewDocked("borderPaneCenterView")).isEqualTo(true);
		assertThat(controller.mainBorderPane.getTop()).isNotNull();
		assertThat(controller.mainBorderPane.getCenter()).isNotNull();

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
