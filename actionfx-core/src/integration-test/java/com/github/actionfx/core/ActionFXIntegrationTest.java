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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.test.nestedviewapp.ControllerWithNestedviewOnField;
import com.github.actionfx.core.test.nestedviewapp.NestedViewApp;
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
		final ControllerWithNestedviewOnField controller = actionFX
				.getController(ControllerWithNestedviewOnField.class);

		// THEN
		assertThat(controller.mainBorderPane, notNullValue());
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
		actionFX.displayMainView(stage);

		// THEN (stage holds scene which in turn holds the view)
		assertThat(stage.getScene(), notNullValue());
		assertThat(stage.getScene().getRoot(), notNullValue());
		assertThat(stage.getScene().getRoot(), instanceOf(BorderPane.class));
		assertThat(actionFX.getPrimaryStage(), sameInstance(stage));
	}

}
