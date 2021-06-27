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
package com.github.actionfx.core.view;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.container.instantiation.MultilingualViewController;
import com.github.actionfx.core.test.TestController;
import com.github.actionfx.core.view.graph.NodeWrapper;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Popup;
import javafx.stage.Stage;

@ExtendWith(FxThreadForAllMonocleExtension.class)
class FxmlViewTest {

	@Test
	void testFxmlView() {
		// WHEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());

		// THEN
		assertThat(view.getId(), equalTo("testId"));
		assertThat(view.getController(), instanceOf(TestController.class));
		assertThat(view.getRootNode(), notNullValue());
		assertThat(view.getRootNode(), instanceOf(GridPane.class));
	}

	@Test
	void testFxmlView_internationalized() {
		// GIVEN
		final Locale locale = new Locale("de", "DE");
		final ResourceBundle bundle = ResourceBundle.getBundle("i18n.TestResources", locale);

		// WHEN
		final FxmlView view = new FxmlView("multilingualView", "/testfxml/MultilingualView.fxml",
				new MultilingualViewController(), bundle);

		// THEN
		assertThat(view.getId(), equalTo("multilingualView"));
		assertThat(view.getController(), instanceOf(MultilingualViewController.class));
		final MultilingualViewController controller = (MultilingualViewController) view.getController();
		assertThat(controller.getLabel().getText(), equalTo("Hallo Welt"));
	}

	@Test
	@TestInFxThread
	void testShow() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());

		// WHEN
		view.show();

		// THEN
		assertThat(view.getWindow(), notNullValue());
		assertThat(view.getWindow(), instanceOf(Stage.class));
	}

	@Test
	@TestInFxThread
	void testShow_withStage() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final Stage stage = new Stage();

		// WHEN
		view.show(stage);

		// THEN
		assertThat(view.getWindow(), notNullValue());
		assertThat(view.getWindow(), sameInstance(stage));
		assertThat(stage.getScene(), notNullValue());
		assertThat(stage.getScene().getRoot(), sameInstance(view.getRootNode()));
	}

	@Test
	@TestInFxThread
	void testShow_withPopup() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final Stage owner = new Stage();
		final Popup popup = new Popup();

		// WHEN
		view.show(popup, owner);

		// THEN
		assertThat(view.getWindow(), notNullValue());
		assertThat(view.getWindow(), sameInstance(popup));
		assertThat(popup.getContent(), hasSize(1));
		assertThat(popup.getContent().get(0), sameInstance(view.getRootNode()));
	}

	@Test
	@TestInFxThread
	void testHide() {
		// GIVEN
		final Stage stage = Mockito.spy(Stage.class);
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		view.show(stage);

		// WHEN
		view.hide();

		// THEN
		verify(stage, times(1)).hide();
	}

	@Test
	void testAttachViewToParent() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final AnchorPane parent = new AnchorPane();

		// WHEN
		view.attachViewToParent(parent, NodeWrapper.anchorPaneFillingAttacher());

		// THEN
		assertThat(parent.getChildren(), hasSize(1));
		assertThat(parent.getChildren().get(0), sameInstance(view.getRootNode()));
	}

	@Test
	void testDetachView_parentSupportsMultipleChildren() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final AnchorPane parent = new AnchorPane();
		parent.getChildren().add(view.getRootNode());

		// WHEN
		view.detachView();

		// THEN
		assertThat(parent.getChildren(), hasSize(0));
	}

	@Test
	void testLookupNode_nodeIsCached() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleViewWithNodeId.fxml", new TestController());
		assertThat(view.lookupCache.entrySet(), hasSize(0));

		// WHEN
		final NodeWrapper textFieldWrapper = view.lookupNode("textField");

		// THEN
		assertThat(textFieldWrapper, notNullValue());
		assertThat(textFieldWrapper.getWrapped(), instanceOf(TextField.class));
		// check that the node is now cached!
		assertThat(view.lookupCache.get("textField"), equalTo(textFieldWrapper));
	}

}
