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
package com.github.actionfx.core.bind;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

/**
 * JUnit test case for {@link ObservableListBinding}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ObservableListBindingTest {

	@Test
	void testBind_bidirectionally_withSelectionModel() {
		// GIVEN
		final ObservableList<String> bindingSource = FXCollections.observableArrayList("Choice 2");
		final ListView<String> listView = listView();
		final ObservableListBinding<String> binding = new ObservableListBinding<>(bindingSource,
				listView.getSelectionModel().getSelectedItems(), listView.getSelectionModel());

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(binding.isBound()).isTrue();
		assertThat(listView.getSelectionModel().getSelectedItems()).containsExactly("Choice 2");
		listView.getSelectionModel().select("Choice 1");
		assertThat(bindingSource).containsExactly("Choice 1", "Choice 2");
		bindingSource.add("Choice 3");
		assertThat(listView.getSelectionModel().getSelectedItems()).containsExactly("Choice 1", "Choice 2", "Choice 3");
	}

	@Test
	void testBind_bidirectionally_withObservableLists() {
		// GIVEN
		final ObservableList<String> bindingSource = FXCollections.observableArrayList("Choice 2");
		final ObservableList<String> bindingTarget = FXCollections.observableArrayList();
		final ObservableListBinding<String> binding = new ObservableListBinding<>(bindingSource, bindingTarget);

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(binding.isBound()).isTrue();
		assertThat(bindingTarget).containsExactly("Choice 2");
		bindingTarget.add("Choice 1");
		assertThat(bindingSource).containsExactly("Choice 2", "Choice 1");
		bindingSource.add("Choice 3");
		assertThat(bindingTarget).containsExactly("Choice 2", "Choice 1", "Choice 3");
	}

	@Test
	void testBind_unidirectionally_withSelectionModel() {
		// GIVEN
		final List<String> bindingSource = new ArrayList<>(Arrays.asList("Choice 2"));
		final ListView<String> listView = listView();
		final ObservableListBinding<String> binding = new ObservableListBinding<>(bindingSource,
				listView.getSelectionModel().getSelectedItems(), listView.getSelectionModel());

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(binding.isBound()).isTrue();
		assertThat(listView.getSelectionModel().getSelectedItems()).containsExactly("Choice 2");
		listView.getSelectionModel().select("Choice 1");
		assertThat(bindingSource).containsExactly("Choice 1", "Choice 2");
	}

	@Test
	void testBind_unidirectionally_withObservableList() {
		// GIVEN
		final List<String> bindingSource = new ArrayList<>(Arrays.asList("Choice 2"));
		final ObservableList<String> bindingTarget = FXCollections.observableArrayList();
		final ObservableListBinding<String> binding = new ObservableListBinding<>(bindingSource, bindingTarget);

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(binding.isBound()).isTrue();
		assertThat(bindingTarget).containsExactly("Choice 2");
		bindingTarget.add("Choice 1");
		assertThat(bindingSource).containsExactly("Choice 2", "Choice 1");
	}

	@Test
	void testUnbind_bidirectionallyBound_withSelectionModel() {
		// GIVEN
		final ObservableList<String> bindingSource = FXCollections.observableArrayList("Choice 2");
		final ListView<String> listView = listView();
		final ObservableListBinding<String> binding = new ObservableListBinding<>(bindingSource,
				listView.getSelectionModel().getSelectedItems(), listView.getSelectionModel());
		binding.bind();

		// WHEN
		binding.unbind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(binding.isBound()).isFalse();
		assertThat(listView.getSelectionModel().getSelectedItems()).containsExactly("Choice 2");
		listView.getSelectionModel().select("Choice 1");
		assertThat(bindingSource).containsExactly("Choice 2"); // no change, as it is not bound
	}

	@Test
	void testUnbind_bidirectionallyBound() {
		// GIVEN
		final ObservableList<String> bindingSource = FXCollections.observableArrayList("Choice 2");
		final ObservableList<String> bindingTarget = FXCollections.observableArrayList();
		final ObservableListBinding<String> binding = new ObservableListBinding<>(bindingSource, bindingTarget);
		binding.bind();

		// WHEN
		binding.unbind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(binding.isBound()).isFalse();
		assertThat(bindingTarget).containsExactly("Choice 2");
		bindingTarget.add("Choice 1");
		assertThat(bindingSource).containsExactly("Choice 2"); // no change, as it is not bound
	}

	@Test
	void testUnbind_unidirectionallyBound() {
		// GIVEN
		final List<String> bindingSource = new ArrayList<>(Arrays.asList("Choice 2"));
		final ObservableList<String> bindingTarget = FXCollections.observableArrayList();
		final ObservableListBinding<String> binding = new ObservableListBinding<>(bindingSource, bindingTarget);
		binding.bind();

		// WHEN
		binding.unbind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(binding.isBound()).isFalse();
		assertThat(bindingTarget).containsExactly("Choice 2");
		bindingTarget.add("Choice 1");
		assertThat(bindingSource).containsExactly("Choice 2"); // no change, as it is not bound
	}

	private static ListView<String> listView() {
		final ListView<String> c = new ListView<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		c.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		return c;
	}

}
