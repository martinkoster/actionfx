/*
 * Copyright (c) 2021 Martin Koster
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
package com.github.actionfx.core.extension.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.test.ViewCreator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

/**
 * JUnit test case for {@link OnLoadControlDataMethodControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class OnLoadControlDataMethodControllerExtensionTest {

	@Test
	@TestInFxThread
	void testAccept_valueIsObservableList() {
		// GIVEN
		final TableView<String> tableView = new TableView<>();
		final ControllerWithTable controller = Mockito
				.spy(new ControllerWithTable(ViewCreator.create(tableView, "tableView")));
		final OnLoadControlDataMethodControllerExtension extension = new OnLoadControlDataMethodControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN (verify that table has the items loaded from the
		// method "loadData")
		assertThat(tableView.getItems()).containsExactly("Loaded 1", "Loaded 2", "Loaded 3");
	}

	@Test
	void testAccept_valueIsObservableList_dataIsLoadedAsynchronously() {
		// GIVEN
		final TableView<String> tableView = new TableView<>();
		final ControllerWithTableAsync controller = Mockito
				.spy(new ControllerWithTableAsync(ViewCreator.create(tableView, "tableView")));
		final OnLoadControlDataMethodControllerExtension extension = new OnLoadControlDataMethodControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN (initially, data is empty, because the data loading flag is set to false
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
        assertThat(tableView.getItems()).isEmpty();

		// and WHEN (we switch the loading flag to "true")
		controller.listenerEnabled.set(true);

		// and THEN
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
		assertThat(tableView.getItems()).containsExactly("Loaded 1", "Loaded 2", "Loaded 3");
	}

	@Test
	@TestInFxThread
	void testAccept_valueIsWritableValue() {
		// GIVEN
		final TreeTableView<String> treeTableView = new TreeTableView<>();
		final ControllerWithTree controller = Mockito
				.spy(new ControllerWithTree(ViewCreator.create(treeTableView, "treeTableView")));
		final OnLoadControlDataMethodControllerExtension extension = new OnLoadControlDataMethodControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN (
		assertThat(treeTableView.getRoot()).isNotNull();
		assertThat(treeTableView.getRoot().getValue()).isEqualTo("root");
	}

	@Test
	void testAccept_valueIsWritableValue_dataIsLoadedAsynchronously() {
		// GIVEN
		final TreeTableView<String> treeTableView = new TreeTableView<>();
		final ControllerWithTreeAsync controller = Mockito
				.spy(new ControllerWithTreeAsync(ViewCreator.create(treeTableView, "treeTableView")));
		final OnLoadControlDataMethodControllerExtension extension = new OnLoadControlDataMethodControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN (initially, root value is empty, because the data loading flag is set to
		// false
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
		assertThat(treeTableView.getRoot()).isNull();

		// and WHEN (we switch the loading flag to "true")
		controller.listenerEnabled.set(true);

		// and THEN
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
		assertThat(treeTableView.getRoot()).isNotNull();
		assertThat(treeTableView.getRoot().getValue()).isEqualTo("root");
	}

	public class ControllerWithTable {

		public View _view;

		// switching this boolean property to false will deactivate all method
		// invocation
		// that reference this listenerActiveProperty
		protected final BooleanProperty listenerEnabled = new SimpleBooleanProperty(true);

		public ControllerWithTable(final View view) {
			_view = view;
		}

		@AFXLoadControlData(controlId = "tableView")
		public List<String> loadTableViewData() {
			return Arrays.asList("Loaded 1", "Loaded 2", "Loaded 3");
		}
	}

	public class ControllerWithTableAsync {

		public View _view;

		protected final BooleanProperty listenerEnabled = new SimpleBooleanProperty(false);

		public ControllerWithTableAsync(final View view) {
			_view = view;
		}

		@AFXLoadControlData(controlId = "tableView", async = true, loadingActiveBooleanProperty = "listenerEnabled")
		public List<String> loadTableViewDataAsynchronously() {
			return Arrays.asList("Loaded 1", "Loaded 2", "Loaded 3");
		}
	}

	public class ControllerWithTree {

		public View _view;

		public ControllerWithTree(final View view) {
			_view = view;
		}

		@AFXLoadControlData(controlId = "treeTableView")
		public TreeItem<String> loadTreeViewData() {
			return new TreeItem<>("root");
		}
	}

	public class ControllerWithTreeAsync {

		public View _view;

		// switching this boolean property to false will deactivate all method
		// invocation
		// that reference this listenerActiveProperty
		protected final BooleanProperty listenerEnabled = new SimpleBooleanProperty(false);

		public ControllerWithTreeAsync(final View view) {
			_view = view;
		}

		@AFXLoadControlData(controlId = "treeTableView", async = true, loadingActiveBooleanProperty = "listenerEnabled")
		public TreeItem<String> loadTreeViewDataAsynchronously() {
			return new TreeItem<>("root");
		}
	}

}
