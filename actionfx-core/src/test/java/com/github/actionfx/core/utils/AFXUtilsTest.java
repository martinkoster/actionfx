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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;

import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
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
        assertThat(node).isNotNull().isInstanceOf(GridPane.class);
    }

    @Test
    void testLoadFxml_internationalized() {
        // GIVEN
        final Locale locale = new Locale("en", "US");
        final ResourceBundle bundle = ResourceBundle.getBundle("i18n.TestResources", locale);

        // WHEN
        final Node node = AFXUtils.loadFxml("/testfxml/MultilingualView.fxml", this, bundle);

        // THEN
        assertThat(node).isNotNull().isInstanceOf(VBox.class);
        final VBox vbox = (VBox) node;
        assertThat(vbox.getChildren()).hasSize(1);
        assertThat(vbox.getChildren().get(0)).isInstanceOf(Label.class);
        final Label label = (Label) vbox.getChildren().get(0);
        assertThat(label.getText()).isEqualTo("Hello World");
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
            assertThat(booleanProperty.get()).isTrue();
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
        assertThat(actual).isEqualTo(value);
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
    void testEnableNodeWhenListHasElements() {
        final ObservableList<String> list = FXCollections.observableArrayList();
        final Button button = new Button();
        button.setDisable(true);

        // create a binding between button's disable state and the list
        AFXUtils.enableNodeWhenListHasElements(button, list);

        assertThat(button.isDisabled()).isTrue();

        // add one element to the list
        list.add("test");

        // button is now enabled
        assertThat(button.isDisabled()).isFalse();

        // remove elements again
        list.clear();

        // button is disabled again
        assertThat(button.isDisabled()).isTrue();
    }

    @Test
    void testEnableNodeWhenListHasElements_listProperty() {
        // GIVEN
        final ObjectProperty<ObservableList<String>> listProperty = new SimpleObjectProperty<>();
        final ObservableList<String> list = FXCollections.observableArrayList();
        final Button button = new Button();
        button.setDisable(true);

        // WHEN
        // create a binding between button's disable state and the list
        AFXUtils.enableNodeWhenListHasElements(button, listProperty);

        // THEN (list property not yet set, so button is disabled)
        assertThat(button.isDisabled()).isTrue();

        // AND WHEN
        listProperty.set(list);

        // AND THEN (still empty, still disabled)
        assertThat(button.isDisabled()).isTrue();

        // AND WHEN
        // add one element to the list
        list.add("test");

        // AND THEN
        // button is now enabled
        assertThat(button.isDisabled()).isFalse();

        // AND WHEN
        // remove elements again
        list.clear();

        // AND THEN
        // button is disabled again
        assertThat(button.isDisabled()).isTrue();
    }

    @Test
    void testEnableNodeWhenListHasNoElements() {
        final ObservableList<String> list = FXCollections.observableArrayList();
        final Button button = new Button();
        button.setDisable(true);

        // create a binding between button's disable state and the list
        AFXUtils.enableNodeWhenListHasNoElements(button, list);

        // button is not disabled, because list has no elements
        assertThat(button.isDisabled()).isFalse();

        // add one element to the list
        list.add("test");

        // button is now disabled
        assertThat(button.isDisabled()).isTrue();

        // remove elements again
        list.clear();

        // button is enabled again
        assertThat(button.isDisabled()).isFalse();
    }

    @Test
    void testEnableNodeWhenListHasNoElements_listProperty() {
        // GIVEN
        final ObjectProperty<ObservableList<String>> listProperty = new SimpleObjectProperty<>();
        final ObservableList<String> list = FXCollections.observableArrayList();
        final Button button = new Button();
        button.setDisable(true);

        // WHEN
        // create a binding between button's disable state and the list
        AFXUtils.enableNodeWhenListHasNoElements(button, listProperty);

        // THEN
        // button is not disabled, because list has no elements
        assertThat(button.isDisabled()).isFalse();

        // AND WHEN
        listProperty.set(list);
        // add one element to the list
        list.add("test");

        // AND THEN
        // button is now disabled
        assertThat(button.isDisabled()).isTrue();

        // AND WHEN
        // remove elements again
        list.clear();

        // AND THEN
        // button is enabled again
        assertThat(button.isDisabled()).isFalse();
    }

    @Test
    void testEnableNodeWhenStringPropertyHasText() {
        final TextField textField = new TextField();

        final Button button = new Button();
        button.setDisable(true);

        // create a binding between button's disable state and the list
        AFXUtils.enableNodeWhenStringPropertyHasText(button, textField.textProperty());

        assertThat(button.isDisabled()).isTrue();

        // set a text
        textField.setText("test");

        // button is now enabled
        assertThat(button.isDisabled()).isFalse();

        // remove text again
        textField.setText("");

        // button is disabled again
        assertThat(button.isDisabled()).isTrue();

    }

    @Test
    void testEnableNodeWhenControlsHaveValues_singleControl() {
        final TextField tf = new TextField();

        final Button button = new Button();

        // create a binding between button and control
        AFXUtils.enableNodeWhenAllControlsHaveUserValues(button, tf);

        // field is empty, button is expected to be empty
        assertThat(button.isDisabled()).isTrue();

        // set text
        tf.setText("hello world");

        // button is enabled now
        assertThat(button.isDisabled()).isFalse();

        // remove text again
        tf.setText("");

        // and button is expected to be disabled again
        assertThat(button.isDisabled()).isTrue();
    }

    @Test
    void testEnableNodeWhenControlsHaveValues_multipleControls() {
        final TextField tf = new TextField();
        final TextArea ta = new TextArea();

        final Button button = new Button();

        // create a binding between button and control
        AFXUtils.enableNodeWhenAllControlsHaveUserValues(button, tf, ta);

        // field is empty, button is expected to be empty
        assertThat(button.isDisabled()).isTrue();

        // set text in text area
        ta.setText("hello world");

        // button is still disbled
        assertThat(button.isDisabled()).isTrue();

        // set text in text field now
        tf.setText("hello from me too");

        // now both registered textinputcontrols have values, so button is enabled
        assertThat(button.isDisabled()).isFalse();

        // remove text from the area again
        ta.setText("");

        // and button is expected to be disabled again
        assertThat(button.isDisabled()).isTrue();
    }

    @Test
    void testEnableNodeWhenConditionIsMet() {
        // GIVEN
        final BooleanProperty condition = new SimpleBooleanProperty(false);
        final Button button = new Button();

        // WHEN
        AFXUtils.enableNodeWhenConditionIsMet(button, condition);

        // THEN
        // initial state was "false", so button is expected to be disabled
        assertThat(button.isDisabled()).isTrue();

        // AND WHEN
        condition.set(true);

        // AND THEN
        assertThat(button.isDisabled()).isFalse();
    }

    @Test
    void testEnableNodeWhenPropertyHasExpectedValue() {
        // GIVEN
        final IntegerProperty property = new SimpleIntegerProperty(10);
        final Button button = new Button();

        // WHEN
        AFXUtils.enableNodeWhenPropertyHasExpectedValue(button, property, 0);

        // THEN
        // disabled, value is 10, expected is 0
        assertThat(button.isDisabled()).isTrue();

        // AND WHEN
        property.set(0);

        // AND THEN
        // enabled, value is 0, expected is 0
        assertThat(button.isDisabled()).isFalse();
    }

    @Test
    void testEnableNodeWhenPropertyHasExpectedValue_expectedValueIsNull() {
        // GIVEN
        final ObjectProperty<String> property = new SimpleObjectProperty<>("hello");
        final Button button = new Button();

        // WHEN
        AFXUtils.enableNodeWhenPropertyHasExpectedValue(button, property, null);

        // THEN
        // disabled, value is "hello", expected is null
        assertThat(button.isDisabled()).isTrue();

        // AND WHEN
        property.set(null);

        // AND THEN
        // enabled, value is null, expected is null
        assertThat(button.isDisabled()).isFalse();
    }

    @Test
    void testEnableNodeWhenPropertyHasNotExpectValue() {
        // GIVEN
        final IntegerProperty property = new SimpleIntegerProperty(10);
        final Button button = new Button();

        // WHEN
        AFXUtils.enableNodeWhenPropertyHasNotExpectedValue(button, property, 0);

        // THEN
        // enabled, value is 10, expected is 0
        assertThat(button.isDisabled()).isFalse();

        // AND WHEN
        property.set(0);

        // AND THEN
        // disabled, value is 0 expected is 0
        assertThat(button.isDisabled()).isTrue();
    }

    @Test
    void testEnableNodeWhenPropertyHasNotExpectValue_expectedValueIsNull() {
        // GIVEN
        final ObjectProperty<String> property = new SimpleObjectProperty<>("hello");
        final Button button = new Button();

        // WHEN
        AFXUtils.enableNodeWhenPropertyHasNotExpectedValue(button, property, null);

        // THEN
        // enabled, value is "hello", expected is null
        assertThat(button.isDisabled()).isFalse();

        // AND WHEN
        property.set(null);

        // AND THEN
        // disabled, value is null expected is null
        assertThat(button.isDisabled()).isTrue();
    }

    @Test
    void testToAwtColor() {
        final java.awt.Color awtColor = AFXUtils.toAwtColor(Color.rgb(1, 2, 3, 1.0));
        assertThat(awtColor.getRed()).isEqualTo(1);
        assertThat(awtColor.getGreen()).isEqualTo(2);
        assertThat(awtColor.getBlue()).isEqualTo(3);
        assertThat(awtColor.getAlpha()).isEqualTo(255);
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
        assertThat(accelerators.keySet()).hasSize(1);
        assertThat(accelerators).containsKey(kcc);
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
        assertThat(accelerators.keySet()).hasSize(1);
        assertThat(accelerators).containsKey(kcc);
    }

    @Test
    void testExecuteOnceWhenPropertyIsNonNull_propertyIsNotNull() {
        // GIVEN
        final ObjectProperty<String> observable = new SimpleObjectProperty<>("Hello");

        // WHEN
        AFXUtils.executeOnceWhenPropertyIsNonNull(observable, item -> observable.set("Hello World"));

        // THEN (immediate execution expected, because the value is not null)
        assertThat(observable.get()).isEqualTo("Hello World");
    }

    @Test
    void testExecuteOnceWhenPropertyIsNonNull_propertyIsNull() {
        // GIVEN
        final ObjectProperty<String> observable = new SimpleObjectProperty<>(null);

        // WHEN
        AFXUtils.executeOnceWhenPropertyIsNonNull(observable, item -> observable.set("Hello World"));

        // THEN (consumer is not yet executed, because property is null)
        assertThat(observable.get()).isNull();

        // AND WHEN (non-null property is set)
        observable.set("Hello");

        // THEN (consumer is now executed, after the property value has been set)
        assertThat(observable.get()).isEqualTo("Hello World");
    }

    @Test
    void testExecuteOnceWhenPropertyHasValue_directExecution() {
        // GIVEN
        final ObjectProperty<String> observable = new SimpleObjectProperty<>("Hello");

        // WHEN
        AFXUtils.executeOnceWhenPropertyHasValue(observable, "Hello", item -> observable.set("Hello World"));

        // THEN (immediate execution expected, because the value is not null)
        assertThat(observable.get()).isEqualTo("Hello World");
    }

    @Test
    void testExecuteOnceWhenPropertyHasValue_delayedExecutionViaChangeListener_propertyIsNull() {
        // GIVEN
        final ObjectProperty<String> observable = new SimpleObjectProperty<>(null);

        // WHEN
        AFXUtils.executeOnceWhenPropertyHasValue(observable, "Hello", item -> observable.set("Hello World"));

        // THEN (consumer is not yet executed, because property is null and not "Hello")
        assertThat(observable.get()).isNull();

        // AND WHEN (non-null property is set)
        observable.set("Hello");

        // THEN (immediate execution expected, because the value is not null)
        assertThat(observable.get()).isEqualTo("Hello World");
    }

    @Test
    void testExecuteOnceWhenPropertyHasValue_delayedExecutionViaChangeListener_propertyIsNotNull() {
        // GIVEN
        final ObjectProperty<String> observable = new SimpleObjectProperty<>("Some Value");

        // WHEN
        AFXUtils.executeOnceWhenPropertyHasValue(observable, "Hello", item -> observable.set("Hello World"));

        // THEN (consumer is not yet executed, because property is "Some Value" and not
        // "Hello")
        assertThat(observable.get()).isEqualTo("Some Value");

        // AND WHEN (non-null property is set)
        observable.set("Hello");

        // THEN (immediate execution expected, because the value is not null)
        assertThat(observable.get()).isEqualTo("Hello World");
    }

    @Test
    void testGetModifiableObservableList() {
        // GIVEN
        final ObservableList<String> sourceList = FXCollections.observableArrayList();
        final FilteredList<String> filteredList = new FilteredList<>(sourceList);

        // WHEN and THEN
        assertThat(AFXUtils.getNestedObservableList(sourceList)).isEqualTo(sourceList);
        assertThat(AFXUtils.getNestedObservableList(filteredList)).isEqualTo(sourceList);
    }

    @Test
    void testDetermineObservableValueType_withValue() {
        assertThat(AFXUtils.determineObservableValueType(new SimpleIntegerProperty(0))).isEqualTo(int.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleStringProperty(""))).isEqualTo(String.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleObjectProperty<>(Integer.valueOf(42)))).isEqualTo(Integer.class);
    }

    @Test
    void testDetermineObservableValueType_withoutValue() {
        assertThat(AFXUtils.determineObservableValueType(new SimpleIntegerProperty())).isEqualTo(int.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleStringProperty(null))).isEqualTo(String.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleBooleanProperty())).isEqualTo(boolean.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleFloatProperty())).isEqualTo(float.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleDoubleProperty())).isEqualTo(double.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleObjectProperty<Integer>(null))).isEqualTo(Object.class); // type erasure, no chance to get the type
        assertThat(AFXUtils.determineObservableValueType(new SimpleMapProperty<>())).isEqualTo(ObservableMap.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleSetProperty<>())).isEqualTo(ObservableSet.class);
        assertThat(AFXUtils.determineObservableValueType(new SimpleListProperty<>())).isEqualTo(ObservableList.class);
    }

    @Test
    void testInjectAsRootPane_newRootIsRegion_andSupportsMultipleChildren() {
        // GIVEN
        final BorderPane oldRoot = new BorderPane();
        final AnchorPane newRoot = new AnchorPane();
        final Scene scene = new Scene(oldRoot);

        // WHEN
        AFXUtils.injectAsRootPane(scene, newRoot);

        // THEN
        assertThat(scene.getRoot()).isEqualTo(newRoot);
        assertThat(newRoot.getChildren()).hasSize(1);
        assertThat(newRoot.getChildren().get(0)).isEqualTo(oldRoot);
        assertThat(oldRoot.getMaxWidth()).isEqualTo(Double.MAX_VALUE);
        assertThat(oldRoot.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
    }

    @Test
    void testInjectAsRootPane_newRootIsRegion_andSupportsSingleChild() {
        // GIVEN
        final BorderPane oldRoot = new BorderPane();
        final ScrollPane newRoot = new ScrollPane();
        final Scene scene = new Scene(oldRoot);

        // WHEN
        AFXUtils.injectAsRootPane(scene, newRoot);

        // THEN
        assertThat(scene.getRoot()).isEqualTo(newRoot);
        assertThat(newRoot.getContent()).isEqualTo(oldRoot);
        assertThat(oldRoot.getMaxWidth()).isEqualTo(Double.MAX_VALUE);
        assertThat(oldRoot.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
    }

    @Test
    void testInjectAsRootPane_newRootIsNotRegion() {
        // GIVEN
        final BorderPane oldRoot = new BorderPane();
        final Group newRoot = new Group();
        final Scene scene = new Scene(oldRoot);

        // WHEN
        AFXUtils.injectAsRootPane(scene, newRoot);

        // THEN
        assertThat(scene.getRoot()).isEqualTo(newRoot);
        assertThat(newRoot.getChildren()).hasSize(1);
        assertThat(newRoot.getChildren().get(0)).isEqualTo(oldRoot);
        assertThat(oldRoot.getMaxWidth()).isNotEqualTo(Double.MAX_VALUE);
        assertThat(oldRoot.getMaxHeight()).isNotEqualTo(Double.MAX_VALUE);
    }
}
