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
package com.github.actionfx.core.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;

import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * JUnit test case for {@link AFXUtils}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
@TestInFxThread
class AFXUtilsTest {

	@Test
	void testLoadFxml() {
		final Node node = AFXUtils.loadFxml("/testfxml/SampleView.fxml", this);
		assertNotNull(node);
		assertTrue(node instanceof GridPane);
	}

	@Test
	void testToRgbCode() {
		assertEquals("#FF0000", AFXUtils.toRgbCode(Color.RED));
		assertEquals("#0000FF", AFXUtils.toRgbCode(Color.BLUE));
		assertEquals("#FFFF00", AFXUtils.toRgbCode(Color.YELLOW));
	}

	@Test
	void testAddToAnchorPane() {
		final AnchorPane pane = new AnchorPane();
		final VBox node = new VBox();
		AFXUtils.addToAnchorPane(node, pane);
		assertEquals(1, pane.getChildren().size());
		assertSame(pane.getChildren().get(0), node);
		assertEquals(0.0, AnchorPane.getTopAnchor(node), 0.1);
		assertEquals(0.0, AnchorPane.getRightAnchor(node), 0.1);
		assertEquals(0.0, AnchorPane.getBottomAnchor(node), 0.1);
		assertEquals(0.0, AnchorPane.getLeftAnchor(node), 0.1);
	}

	@Test
	void testRemoveAccelerator() {
		final Pane pane = new Pane();
		final Scene scene = new Scene(pane);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), () -> {
		});
		AFXUtils.removeAccelerators(scene);
		assertTrue(scene.getAccelerators().isEmpty());
	}

	@Test
	void runInFxThread_calledInsideFxThread(final FxRobot robot) throws Exception {
		robot.interact(() -> {
			// GIVEN
			final BooleanProperty booleanProperty = new SimpleBooleanProperty(false);

			// WHEN
			AFXUtils.runInFxThread(() -> booleanProperty.set(true));

			// THEN (runnable is immediately executed, because this test runs in the JavaFX
			// thread -> result available)
			assertThat(booleanProperty.get(), equalTo(true));
		});
	}

	@Test
	void runInFxThread_calledFromOtherTreadThanFxThread() throws Exception {
		// GIVEN
		// intercept calls to Platform.runLater(..)
		final Runnable runnable = () -> {
			// do nothing here, we are only interested that this runnable is passed to
			// Platform.runLater(..)
		};
		// create a new thread than runs our method under test
		final Thread thread = new Thread(() -> AFXUtils.runInFxThread(runnable));

		// WHEN
		// execute the thread and wait for the result
		thread.start();
		thread.join(); // wait for the thread to terminate
	}

	@Test
	void runInFxThreadAndWait() throws InterruptedException, ExecutionException {
		final String value = "Hello World";
		final Task<String> task = new Task<>() {

			@Override
			protected String call() throws Exception {
				return value;
			}

		};
		final String actual = AFXUtils.runInFxThreadAndWait(task);
		assertThat(actual, Matchers.equalTo(value));
	}

	@Test
	void testRunAndWait_ExecutionExceptionExpected() throws InterruptedException, ExecutionException {
		assertThrows(ExecutionException.class, () -> {
			final Task<String> task = new Task<>() {

				@Override
				protected String call() throws Exception {
					throw new IllegalStateException("Expected exception");
				}
			};
			AFXUtils.runInFxThreadAndWait(task);
		});
	}

	@Test
	void testEnableButtonWhenListHasElements() {
		final ObservableList<String> list = FXCollections.observableArrayList();
		final Button button = new Button();
		button.setDisable(true);

		// create a binding between button's disable state and the list
		AFXUtils.enableButtonWhenListHasElements(button, list);

		assertThat(button.isDisabled(), equalTo(true));

		// add one element to the list
		list.add("test");

		// button is now enabled
		assertThat(button.isDisabled(), equalTo(false));

		// remove elements again
		list.clear();

		// button is disabled again
		assertThat(button.isDisabled(), equalTo(true));
	}

	@Test
	void testEnableButtonWhenListHasElements_listProperty() {
		// GIVEN
		final ObjectProperty<ObservableList<String>> listProperty = new SimpleObjectProperty<>();
		final ObservableList<String> list = FXCollections.observableArrayList();
		final Button button = new Button();
		button.setDisable(true);

		// WHEN
		// create a binding between button's disable state and the list
		AFXUtils.enableButtonWhenListHasElements(button, listProperty);

		// THEN (list property not yet set, so button is disabled)
		assertThat(button.isDisabled(), equalTo(true));

		// AND WHEN
		listProperty.set(list);

		// AND THEN (still empty, still disabled)
		assertThat(button.isDisabled(), equalTo(true));

		// AND WHEN
		// add one element to the list
		list.add("test");

		// AND THEN
		// button is now enabled
		assertThat(button.isDisabled(), equalTo(false));

		// AND WHEN
		// remove elements again
		list.clear();

		// AND THEN
		// button is disabled again
		assertThat(button.isDisabled(), equalTo(true));
	}

	@Test
	void enableButtonWhenListHasNoElements() {
		final ObservableList<String> list = FXCollections.observableArrayList();
		final Button button = new Button();
		button.setDisable(true);

		// create a binding between button's disable state and the list
		AFXUtils.enableButtonWhenListHasNoElements(button, list);

		// button is not disabled, because list has no elements
		assertThat(button.isDisabled(), equalTo(false));

		// add one element to the list
		list.add("test");

		// button is now disabled
		assertThat(button.isDisabled(), equalTo(true));

		// remove elements again
		list.clear();

		// button is enabled again
		assertThat(button.isDisabled(), equalTo(false));
	}

	@Test
	void enableButtonWhenListHasNoElements_listProperty() {
		// GIVEN
		final ObjectProperty<ObservableList<String>> listProperty = new SimpleObjectProperty<>();
		final ObservableList<String> list = FXCollections.observableArrayList();
		final Button button = new Button();
		button.setDisable(true);

		// WHEN
		// create a binding between button's disable state and the list
		AFXUtils.enableButtonWhenListHasNoElements(button, listProperty);

		// THEN
		// button is not disabled, because list has no elements
		assertThat(button.isDisabled(), equalTo(false));

		// AND WHEN
		listProperty.set(list);
		// add one element to the list
		list.add("test");

		// AND THEN
		// button is now disabled
		assertThat(button.isDisabled(), equalTo(true));

		// AND WHEN
		// remove elements again
		list.clear();

		// AND THEN
		// button is enabled again
		assertThat(button.isDisabled(), equalTo(false));
	}

	@Test
	void testEnableButtonWhenStringPropertyHasText() {
		final TextField textField = new TextField();

		final Button button = new Button();
		button.setDisable(true);

		// create a binding between button's disable state and the list
		AFXUtils.enableButtonWhenStringPropertyHasText(button, textField.textProperty());

		assertThat(button.isDisabled(), equalTo(true));

		// set a text
		textField.setText("test");

		// button is now enabled
		assertThat(button.isDisabled(), equalTo(false));

		// remove text again
		textField.setText("");

		// button is disabled again
		assertThat(button.isDisabled(), equalTo(true));

	}

	@Test
	void testEnableButtonWhenControlsHaveValues_singleControl() {
		final TextField tf = new TextField();

		final Button button = new Button();

		// create a binding between button and control
		AFXUtils.enableButtonWhenAllControlsHaveValues(button, tf);

		// field is empty, button is expected to be empty
		assertThat(button.isDisabled(), equalTo(true));

		// set text
		tf.setText("hello world");

		// button is enabled now
		assertThat(button.isDisabled(), equalTo(false));

		// remove text again
		tf.setText("");

		// and button is expected to be disabled again
		assertThat(button.isDisabled(), equalTo(true));
	}

	@Test
	void testEnableButtonWhenControlsHaveValues_multipleControls() {
		final TextField tf = new TextField();
		final TextArea ta = new TextArea();

		final Button button = new Button();

		// create a binding between button and control
		AFXUtils.enableButtonWhenAllControlsHaveValues(button, tf, ta);

		// field is empty, button is expected to be empty
		assertThat(button.isDisabled(), equalTo(true));

		// set text in text area
		ta.setText("hello world");

		// button is still disbled
		assertThat(button.isDisabled(), equalTo(true));

		// set text in text field now
		tf.setText("hello from me too");

		// now both registered textinputcontrols have values, so button is enabled
		assertThat(button.isDisabled(), equalTo(false));

		// remove text from the area again
		ta.setText("");

		// and button is expected to be disabled again
		assertThat(button.isDisabled(), equalTo(true));
	}

	@Test
	void testEnableButtonWhenConditionIsMet() {
		// GIVEN
		final BooleanProperty condition = new SimpleBooleanProperty(false);
		final Button button = new Button();

		// WHEN
		AFXUtils.enableButtonWhenConditionIsMet(button, condition);

		// THEN
		// initial state was "false", so button is expected to be disabled
		assertThat(button.isDisabled(), equalTo(true));

		// AND WHEN
		condition.set(true);

		// AND THEN
		assertThat(button.isDisabled(), equalTo(false));
	}

	@Test
	void testEnableButtonWhenPropertyHasExpectValue() {
		// GIVEN
		final IntegerProperty property = new SimpleIntegerProperty(10);
		final Button button = new Button();

		// WHEN
		AFXUtils.enableButtonWhenPropertyHasExpectValue(button, property, 0);

		// THEN
		// disabled, value is 10, expected is 0
		assertThat(button.isDisabled(), equalTo(true));

		// AND WHEN
		property.set(0);

		// AND THEN
		// enabled, value is 0, expected is 0
		assertThat(button.isDisabled(), equalTo(false));
	}

	@Test
	void testEnableButtonWhenPropertyHasNotExpectValue() {
		// GIVEN
		final IntegerProperty property = new SimpleIntegerProperty(10);
		final Button button = new Button();

		// WHEN
		AFXUtils.enableButtonWhenPropertyHasNotExpectedValue(button, property, 0);

		// THEN
		// enabled, value is 10, expected is 0
		assertThat(button.isDisabled(), equalTo(false));

		// AND WHEN
		property.set(0);

		// AND THEN
		// disabled, value is 0 expected is 0
		assertThat(button.isDisabled(), equalTo(true));
	}

	@Test
	void testToAwtColor() {
		final java.awt.Color awtColor = AFXUtils.toAwtColor(Color.rgb(1, 2, 3, 1.0));
		assertThat(awtColor.getRed(), equalTo(1));
		assertThat(awtColor.getGreen(), equalTo(2));
		assertThat(awtColor.getBlue(), equalTo(3));
		assertThat(awtColor.getAlpha(), equalTo(255));
	}

	@Test
	void testAddAccelerator_button_sceneIsAlreadyPresent() {
		// GIVEN
		final AnchorPane root = new AnchorPane();
		final Scene scene = new Scene(root);
		final Button b = new Button();
		root.getChildren().add(b); // button is added to a scene
		final KeyCodeCombination kcc = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

		// WHEN
		AFXUtils.addAccelerator(b, kcc);

		// THEN
		// scene now contains the accelerator added for the button
		final ObservableMap<KeyCombination, Runnable> accelerators = scene.getAccelerators();
		assertThat(accelerators.keySet(), hasSize(1));
		assertThat(accelerators.containsKey(kcc), equalTo(true));
	}

	@Test
	void testAddAccelerator_button_sceneIsChangedAfterAcceleratorsHaveBeenAdded() {
		// GIVEN
		final AnchorPane root = new AnchorPane();
		final Scene scene = new Scene(root);
		final Button b = new Button();
		final KeyCodeCombination kcc = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

		// WHEN
		AFXUtils.addAccelerator(b, kcc);
		root.getChildren().add(b); // scene is changed after accelerator has been added

		// THEN
		// scene now contains the accelerator added for the button
		final ObservableMap<KeyCombination, Runnable> accelerators = scene.getAccelerators();
		assertThat(accelerators.keySet(), hasSize(1));
		assertThat(accelerators.containsKey(kcc), equalTo(true));
	}

	@Test
	void testExecuteOnceWhenPropertyIsNonNull_propertyIsNotNull() {
		// GIVEN
		final ObjectProperty<String> observable = new SimpleObjectProperty<>("Hello");

		// WHEN
		AFXUtils.executeOnceWhenPropertyIsNonNull(observable, item -> observable.set("Hello World"));

		// THEN (immediate execution expected, because the value is not null)
		assertThat(observable.get(), equalTo("Hello World"));
	}

	@Test
	void testExecuteOnceWhenPropertyIsNonNull_propertyIsNull() {
		// GIVEN
		final ObjectProperty<String> observable = new SimpleObjectProperty<>(null);

		// WHEN
		AFXUtils.executeOnceWhenPropertyIsNonNull(observable, item -> observable.set("Hello World"));

		// THEN (consumer is not yet executed, because property is null)
		assertThat(observable.get(), nullValue());

		// AND WHEN (non-null property is set)
		observable.set("Hello");

		// THEN (consumer is now executed, after the property value has been set)
		assertThat(observable.get(), equalTo("Hello World"));
	}

	@Test
	void testExecuteOnceWhenPropertyHasValue_directExecution() {
		// GIVEN
		final ObjectProperty<String> observable = new SimpleObjectProperty<>("Hello");

		// WHEN
		AFXUtils.executeOnceWhenPropertyHasValue(observable, "Hello", item -> observable.set("Hello World"));

		// THEN (immediate execution expected, because the value is not null)
		assertThat(observable.get(), equalTo("Hello World"));
	}

	@Test
	void testExecuteOnceWhenPropertyHasValue_delayedExecutionViaChangeListener_propertyIsNull() {
		// GIVEN
		final ObjectProperty<String> observable = new SimpleObjectProperty<>(null);

		// WHEN
		AFXUtils.executeOnceWhenPropertyHasValue(observable, "Hello", item -> observable.set("Hello World"));

		// THEN (consumer is not yet executed, because property is null and not "Hello")
		assertThat(observable.get(), nullValue());

		// AND WHEN (non-null property is set)
		observable.set("Hello");

		// THEN (immediate execution expected, because the value is not null)
		assertThat(observable.get(), equalTo("Hello World"));
	}

	@Test
	void testExecuteOnceWhenPropertyHasValue_delayedExecutionViaChangeListener_propertyIsNotNull() {
		// GIVEN
		final ObjectProperty<String> observable = new SimpleObjectProperty<>("Some Value");

		// WHEN
		AFXUtils.executeOnceWhenPropertyHasValue(observable, "Hello", item -> observable.set("Hello World"));

		// THEN (consumer is not yet executed, because property is "Some Value" and not
		// "Hello")
		assertThat(observable.get(), equalTo("Some Value"));

		// AND WHEN (non-null property is set)
		observable.set("Hello");

		// THEN (immediate execution expected, because the value is not null)
		assertThat(observable.get(), equalTo("Hello World"));
	}

	@Test
	void testGetModifiableObservableList() {
		// GIVEN
		final ObservableList<String> sourceList = FXCollections.observableArrayList();
		final FilteredList<String> filteredList = new FilteredList<>(sourceList);

		// WHEN and THEN
		assertThat(AFXUtils.getNestedObservableList(sourceList), equalTo(sourceList));
		assertThat(AFXUtils.getNestedObservableList(filteredList), equalTo(sourceList));
	}
}
