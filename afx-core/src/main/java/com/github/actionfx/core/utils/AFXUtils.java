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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Util-class for working with JavaFX. This class comes with some handy routines
 * for e.g. loading FXML, working with the JavaFX thread or just controlling the
 * behavior of certain nodes depending on the binding.
 *
 * @author MartinKoster
 */
public class AFXUtils {

	/**
	 * Class is not instantiable.
	 */
	private AFXUtils() {
	}

	/**
	 * Loads an FXML file from the given {@code fxmlPath}, setting
	 * {@code controller} as its backing controller.
	 *
	 * @param fxmlPath   the path to the FXML file
	 * @param controller the controller to set
	 * @return the loaded {@code Node} instance
	 */
	public static final Node loadFxml(final String fxmlPath, final Object controller) {
		try {
			final FXMLLoader loader = new FXMLLoader(AFXUtils.class.getResource(fxmlPath));
			if (controller != null) {
				loader.setController(controller);
			}
			return loader.load();
		} catch (final IOException e) {
			throw new IllegalStateException("Can not load FXML from '" + fxmlPath + "'!", e);
		}
	}

	/**
	 * Converts a {@code Color} into an RGB string of form {@code #rrggbb}.
	 *
	 * @param color the color to convert into an RGB string
	 * @return the RGB string
	 */
	public static String toRgbCode(final Color color) {
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}

	/**
	 * Adds the given {@code node} to the {@code AnchorPane} in order to fully fill
	 * out the pane.
	 *
	 * @param node the node to add to the {@code AnchorPane}
	 * @param pane the {@code AnchorPane}
	 */
	public static void addToAnchorPane(final Node node, final AnchorPane pane) {
		pane.getChildren().add(node);
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
	}

	/**
	 * Adds an accelerator (keyboard shortcut) to the supplied {@code button}.
	 *
	 * @param button             the button to add an accelerator to
	 * @param keyCodeCombination the key combination that will trigger the action
	 *                           behind the button
	 */
	public static void addAccelerator(final ButtonBase button, final KeyCodeCombination keyCodeCombination) {
		final Scene scene = button.getScene();
		if (scene != null) {
			scene.getAccelerators().put(keyCodeCombination, () -> fireButton(button));
		}
		// in case the scene changes, we listen this event and attach the accelerator
		// again
		button.sceneProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				newValue.getAccelerators().put(keyCodeCombination, () -> fireButton(button));
			}
		});
	}

	/**
	 * Removes all accelerators from a given {@code scene}.
	 *
	 * @param scene the scene
	 */
	public static void removeAccelerators(final Scene scene) {
		if (scene == null) {
			throw new IllegalArgumentException("Scene must not be null!");
		}
		scene.getAccelerators().clear();
	}

	/**
	 * Fires a button from code, providing visual feedback that the button is
	 * firing.
	 *
	 * @param button the button to fire
	 */
	private static void fireButton(final ButtonBase button) {
		button.arm();
		final PauseTransition pt = new PauseTransition(Duration.millis(300));
		pt.setOnFinished(event -> {
			button.fire();
			button.disarm();
		});
		pt.play();
	}

	/**
	 * Executes the given {@link Runnable} in the JavaFX thread. In case the current
	 * thread already <b>is</b> the JavaFX thread, the {@link Runnable#run()} method
	 * is directly invoked.
	 *
	 * @param runnable the runnable to execute inside the JavaFX thread
	 */
	public static void runInFxThread(final Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			doPlatformRunLater(runnable);
		}
	}

	/**
	 * Separated {@code Platform.runLater()} to a separate code block for
	 * unit-testing.
	 *
	 * @param runnable the runnable
	 */
	private static void doPlatformRunLater(final Runnable runnable) {
		Platform.runLater(runnable);
	}

	/**
	 * Invokes a {@code javafx.concurrent.Task} in the JavaFX Thread and waits while
	 * it's finished. Like SwingUtilities.invokeAndWait does for EDT.
	 *
	 * @param task the runnable that has to be called on JavaFX thread.
	 * @throws InterruptedException f the execution is interrupted.
	 * @throws ExecutionException   If a exception is occurred in the run method of
	 *                              the Runnable
	 */
	public static <T> T runInFxThreadAndWait(final Task<T> task) throws InterruptedException, ExecutionException {
		if (Platform.isFxApplicationThread()) {
			try {
				task.run();
			} catch (final Exception e) {
				throw new ExecutionException(e);
			}
		} else {
			Platform.runLater(task::run);
		}
		return task.get();
	}

	/**
	 * Creates a binding which enables the given {@code button}, when the
	 * {@code list} contains elements.
	 *
	 * @param button the button to enable
	 * @param list   the list that shall be checked for elements
	 */
	public static void enableButtonWhenListHasElements(final Button button, final ObservableList<?> list) {
		final BooleanBinding hasNoElementsProperty = Bindings.createBooleanBinding(list::isEmpty, list);
		button.disableProperty().bind(hasNoElementsProperty);
	}

	/**
	 * Creates a binding which enables the given {@code button}, when the
	 * {@code list} inside the {@code listProperty}contains elements.
	 *
	 * @param button       the button to enable
	 * @param listProperty the list property that shall be checked for elements
	 */
	public static <T> void enableButtonWhenListHasElements(final Button button,
			final ObjectProperty<ObservableList<T>> listProperty) {
		if (listProperty.get() == null || listProperty.get().isEmpty()) {
			button.setDisable(true);
		}
		listProperty.addListener((observable, oldValue, newValue) -> {
			button.disableProperty().unbind();
			if (newValue != null) {
				enableButtonWhenListHasElements(button, newValue);
			}
		});
	}

	/**
	 * Extracts the observable list from wrappers like {@link FilteredList}.
	 *
	 * @param observableList the list to extract the modifiable list from
	 * @return the extracted list
	 */
	public static <T> ObservableList<? extends T> getNestedObservableList(
			final ObservableList<? extends T> observableList) {
		if (FilteredList.class.isAssignableFrom(observableList.getClass())) {
			return ((FilteredList<? extends T>) observableList).getSource();
		}
		return observableList;
	}

	/**
	 * Creates a binding which enables the given {@code button}, when the
	 * {@code list} contains NO elements.
	 *
	 * @param button the button to enable
	 * @param list   the list that shall be checked for elements
	 */
	public static void enableButtonWhenListHasNoElements(final Button button, final ObservableList<?> list) {
		final BooleanBinding hasElementsProperty = Bindings.createBooleanBinding(() -> !list.isEmpty(), list);
		button.disableProperty().bind(hasElementsProperty);
	}

	/**
	 * Creates a binding which enables the given {@code button}, when the
	 * {@code list} inside the {@code listProperty}contains NO elements.
	 *
	 * @param button       the button to enable
	 * @param listProperty the list property that shall be checked for elements
	 */
	public static <T> void enableButtonWhenListHasNoElements(final Button button,
			final ObjectProperty<ObservableList<T>> listProperty) {
		if (listProperty.get() == null || listProperty.get().isEmpty()) {
			button.setDisable(false);
		}
		listProperty.addListener((observable, oldValue, newValue) -> {
			button.disableProperty().unbind();
			if (newValue != null) {
				enableButtonWhenListHasNoElements(button, newValue);
			}
		});
	}

	/**
	 * Creates a binding which enables the given {@code button}, when the
	 * {@code list} contains elements.
	 *
	 * @param button         the button to enable
	 * @param stringProperty the string property that shall be checked for presence
	 *                       of text
	 */
	public static void enableButtonWhenStringPropertyHasText(final Button button, final StringProperty stringProperty) {
		final BooleanBinding hasNoTextProperty = Bindings
				.createBooleanBinding(() -> StringUtils.isBlank(stringProperty.get()), stringProperty);
		button.disableProperty().unbind();
		button.disableProperty().bind(hasNoTextProperty);
	}

	/**
	 * Enable the given {@code button}, when the controls have values supplied.
	 * <p>
	 * Supported controls are: {@link TextInputControl} (e.g. {@link TextField} and
	 * {@link TextArea}).
	 *
	 * @param button   the button to enable
	 * @param controls the controls to check
	 */
	public static void enableButtonWhenAllControlsHaveValues(final Button button, final Control... controls) {
		final List<Observable> observables = new ArrayList<>();
		// collect the observables so that the binding can react on changes
		for (final Control control : controls) {
			if (TextInputControl.class.isAssignableFrom(control.getClass())) {
				observables.add(((TextInputControl) control).textProperty());
			}
		}
		// create the binding which calculates the disabled status for the button
		final BooleanBinding notAllValuesProvided = Bindings.createBooleanBinding(() -> {
			boolean disabled = false;
			for (final Control control : controls) {
				if (TextInputControl.class.isAssignableFrom(control.getClass())) {
					final TextInputControl textControl = (TextInputControl) control;
					disabled |= StringUtils.isBlank(textControl.getText());
				}
			}
			return disabled;
		}, observables.toArray(new Observable[observables.size()]));
		button.disableProperty().unbind();
		button.disableProperty().bind(notAllValuesProvided);
	}

	/**
	 * Enables the given {@code button}, when the supplied {@code condition}
	 * property changes to {@code true}.
	 *
	 * @param button    the button to enable
	 * @param condition the condition to observe
	 */
	public static void enableButtonWhenConditionIsMet(final Button button, final BooleanProperty condition) {
		final BooleanBinding conditionIsNotMet = Bindings.createBooleanBinding(() -> !condition.get(), condition);
		button.disableProperty().unbind();
		button.disableProperty().bind(conditionIsNotMet);
	}

	/**
	 * Creates a binding which enables the given {@code button}, when the supplied
	 * integer value is equal to an expected value.
	 *
	 * @param button          the button to enable
	 * @param integerProperty the integer property to observe
	 * @param expectedValue   the expected value, when the button shall be enabled
	 */
	public static void enableButtonWhenPropertyHasExpectValue(final Button button,
			final IntegerProperty integerProperty, final int expectedValue) {
		final BooleanBinding expectedValueIsNotMet = Bindings
				.createBooleanBinding(() -> integerProperty.get() != expectedValue, integerProperty);
		button.disableProperty().unbind();
		button.disableProperty().bind(expectedValueIsNotMet);
	}

	/**
	 * Creates a binding which enables the given {@code button}, when the supplied
	 * integer value is NOT equal to an expected value.
	 *
	 * @param button          the button to enable
	 * @param integerProperty the integer property to observe
	 * @param expectedValue   the expected value, when the button shall be enabled
	 */
	public static void enableButtonWhenPropertyHasNotExpectedValue(final Button button,
			final IntegerProperty integerProperty, final int expectedValue) {
		final BooleanBinding expectedValueIsMet = Bindings
				.createBooleanBinding(() -> integerProperty.get() == expectedValue, integerProperty);
		button.disableProperty().unbind();
		button.disableProperty().bind(expectedValueIsMet);
	}

	/**
	 * Converts a JavaFX {@link Color} to an AWT {@link java.awt.Color}.
	 *
	 * @param fxColor the JavaFX color
	 * @return the AWT color
	 */
	public static java.awt.Color toAwtColor(final Color fxColor) {
		return new java.awt.Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue(),
				(float) fxColor.getOpacity());
	}

	/**
	 * Useful method for linking things together when before a property is
	 * necessarily set.
	 */
	public static <T> void executeOnceWhenPropertyIsNonNull(final ObservableValue<T> p, final Consumer<T> consumer) {
		if (p == null) {
			return;
		}
		final T value = p.getValue();
		if (value != null) {
			consumer.accept(value);
		} else {
			final InvalidationListener listener = new InvalidationListener() {
				@Override
				public void invalidated(final Observable observable) {
					final T value = p.getValue();

					if (value != null) {
						p.removeListener(this);
						consumer.accept(value);
					}
				}
			};
			p.addListener(listener);
		}
	}

	/**
	 * Useful method for linking things together when before a property is
	 * necessarily set.
	 */
	public static <T> void executeOnceWhenPropertyHasValue(final ObservableValue<T> p, final T expectedValue,
			final Consumer<T> consumer) {
		if (p == null) {
			return;
		}
		final T value = p.getValue();
		if (value != null && value.equals(expectedValue)) {
			consumer.accept(value);
		} else {
			final ChangeListener<T> listener = new ChangeListener<>() {
				@Override
				public void changed(final ObservableValue<? extends T> observable, final T oldValue, final T newValue) {
					final T value = p.getValue();

					if (value != null && value.equals(expectedValue)) {
						p.removeListener(this);
						consumer.accept(value);
					}
				}
			};
			p.addListener(listener);
		}
	}

	/**
	 * Looks up the first found scrollbar of a given control matching the desired
	 * orientation. If none found, null is returned
	 *
	 * @param control     the control that is supposed to contain the scrollbar to
	 *                    search for
	 * @param orientation the scrollbar orientation (i.e. vertical or horizontal)
	 * @return the found scrollbar, or {@code null}, if there is no scrollbar.
	 */
	public static ScrollBar getScrollbarComponent(final Control control, final Orientation orientation) {
		for (final Node node : control.lookupAll(".scroll-bar")) {
			if (node instanceof ScrollBar) {
				final ScrollBar bar = (ScrollBar) node;
				if (bar.getOrientation().equals(orientation)) {
					return bar;
				}
			}
		}
		return null;
	}

}
