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
package com.github.actionfx.core.container.instantiation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForEachMonocleExtension;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * JUnit test case for {@link FxmlViewInstantiationSupplier}
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForEachMonocleExtension.class)
class ControllerInstantiationSupplierTest {

	@BeforeAll
	static void beforeAll() {
		ActionFX.builder().build();
	}

	@Test
	void testCreateInstance_fxmlView_viewCreationTest() {
		// GIVEN
		final ControllerInstantiationSupplier<SampleViewController> supplier = new ControllerInstantiationSupplier<>(
				SampleViewController.class);

		// WHEN
		final SampleViewController controller = supplier.get();

		// THEN
		assertThat(controller, notNullValue());
		final View view = ControllerWrapper.getViewFrom(controller);
		assertThat(view, notNullValue());
		assertThat(view.getRootNode(), notNullValue());
		assertThat(view.getId(), equalTo("testId"));

		assertThat(view, instanceOf(FxmlView.class));
		final FxmlView fxmlView = (FxmlView) view;
		assertThat(fxmlView.getWidth(), equalTo(100));
		assertThat(fxmlView.getHeight(), equalTo(50));
		assertThat(fxmlView.getIcon(), equalTo("icon.png"));
		assertThat(fxmlView.getPosX(), equalTo(10));
		assertThat(fxmlView.getPosY(), equalTo(20));
		assertThat(fxmlView.isMaximized(), equalTo(true));
		assertThat(fxmlView.isModalDialogue(), equalTo(false));
		assertThat(fxmlView.getWindowTitle(), equalTo("Hello World"));
		assertThat(fxmlView.getStylesheets(), hasItems(equalTo("cssClass1"), equalTo("cssClass2")));
	}

	@Test
	void testCreateInstance_fxmlView_internationalizationTest() {
		// GIVEN
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.TestResources", Locale.US);
		final ControllerInstantiationSupplier<MultilingualViewController> supplier = new ControllerInstantiationSupplier<>(
				MultilingualViewController.class, resourceBundle);

		// WHEN
		final MultilingualViewController controller = supplier.get();

		// THEN
		assertThat(controller, notNullValue());
		final View view = ControllerWrapper.getViewFrom(controller);
		assertThat(view, notNullValue());
		assertThat(view.getRootNode(), notNullValue());
		assertThat(view.getId(), equalTo("multilingualView"));

		assertThat(view, instanceOf(FxmlView.class));
		final FxmlView fxmlView = (FxmlView) view;
		final Node node = fxmlView.getRootNode();
		assertNotNull(node);
		assertTrue(node instanceof VBox);
		final VBox vbox = (VBox) node;
		assertThat(vbox.getChildren(), hasSize(1));
		assertThat(vbox.getChildren().get(0), instanceOf(Label.class));
		final Label label = (Label) vbox.getChildren().get(0);
		assertThat(label.getText(), equalTo("Hello World"));
	}

	@Test
	void testCreateInstance_parentView() {
		// GIVEN
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.TestResources", Locale.US);
		final ControllerInstantiationSupplier<ViewController> supplier = new ControllerInstantiationSupplier<>(
				ViewController.class, resourceBundle);

		// WHEN
		final ViewController controller = supplier.get();

		// THEN
		assertThat(controller, notNullValue());
		final View view = ControllerWrapper.getViewFrom(controller);
		assertThat(view, notNullValue());
		assertThat(view.getRootNode(), notNullValue());
		assertThat(view.getId(), equalTo("parentView"));

		assertThat(view, instanceOf(ParentView.class));
		assertThat(controller.label, notNullValue());
		assertThat(controller.label.getText(), equalTo("Hello World"));
		assertThat(controller.tableView, notNullValue());
	}

	@Test
	void testCreateInstance_invalidAFXControllerAnnotation() {
		// GIVEN
		final ControllerInstantiationSupplier<ViewControllerWithInvalidAFXControllerAnnotation> supplier = new ControllerInstantiationSupplier<>(
				ViewControllerWithInvalidAFXControllerAnnotation.class);

		// WHEN and THEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> supplier.get());

		assertThat(ex.getMessage(), equalTo("Failed to instantiate class in JavaFX thread!"));
	}

	/**
	 * View class with constructor accepting a {@link ResourceBundle}.
	 *
	 * @author koster
	 *
	 */
	public static class ViewClassWithResourceBundle extends AnchorPane {

		public ViewClassWithResourceBundle(final ResourceBundle resourceBundle) {
			final HBox hbox = new HBox();

			// lets get the internationalized text from the bundle
			final Label label = new Label(resourceBundle.getString("label.text"));
			label.setId("label");

			final TableView<String> tableView = new TableView<>();
			tableView.setId("tableView");

			hbox.getChildren().addAll(label, tableView);

			getChildren().add(hbox);
		}
	}

	/**
	 * Controller class for {@link ViewClassWithResourceBundle}.
	 *
	 * @author koster
	 *
	 */
	@AFXController(viewId = "parentView", viewClass = ViewClassWithResourceBundle.class, resourcesBasename = "i18n.TestResources")
	public static class ViewController {

		@FXML
		protected Label label;

		@FXML
		protected TableView<String> tableView;
	}

	/**
	 * Controller class for {@link ViewClassWithResourceBundle}.
	 *
	 * @author koster
	 *
	 */
	@AFXController(viewId = "parentView", resourcesBasename = "i18n.TestResources")
	public static class ViewControllerWithInvalidAFXControllerAnnotation {

	}
}
