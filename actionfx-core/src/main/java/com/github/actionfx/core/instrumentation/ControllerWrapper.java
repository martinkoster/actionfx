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
package com.github.actionfx.core.instrumentation;

import java.lang.reflect.Field;
import java.util.ResourceBundle;

import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;

import javafx.scene.Scene;
import javafx.stage.Window;

/**
 * Wrapper class around a JavaFX controller that is enhanced either with an
 * instrumentation agent or via sub-classing. The wrapper offers convenient
 * methods to access e.g. the view field that is added by byte-code enhancement.
 *
 * @author koster
 *
 */
public class ControllerWrapper<T> {

	private final T controller;

	public ControllerWrapper(final T controller) {
		this.controller = controller;
	}

	/**
	 * Convenient factory method for a {@link ControllerWrapper}.
	 *
	 * @param controller the controller wrapper
	 * @return the wrapper instance
	 */
	public static <T> ControllerWrapper<T> of(final T controller) {
		return new ControllerWrapper<>(controller);
	}

	/**
	 * Sets the given {@link View} into the wrapped controller.
	 *
	 * @param view the view to set
	 */
	public void setView(final View view) {
		ReflectionUtils.setFieldValue(getViewField(), controller, view);
	}

	/**
	 * Gets the {@link View} from the wrapped controller.
	 *
	 * @return the view
	 */
	public View getView() {
		return (View) ReflectionUtils.getFieldValue(getViewField(), controller);
	}

	/**
	 * Gets the {@link ResourceBundle} from the wrapped controller.
	 *
	 * @return the resource bundle
	 */
	public ResourceBundle getResourceBundle() {
		return getView().getResourceBundle();
	}

	/**
	 * Gets the {@link Scene} that the controller's view is part of. In case the
	 * view has not yet been added to a scene, {@code null} is returned.
	 *
	 * @return the {@link Scene} that the wrapped node is part of, or {@code null},
	 *         in case the controller's view has not been added to a scene.
	 */
	public Scene getScene() {
		return getView().getScene();
	}

	/**
	 * Gets the {@link Window} where the controller's view is currently displayed
	 * in. In case the view has not yet been displayed, {@code null} is returned.
	 *
	 * @return the {@link Window} in that this view is displayed, or {@code null},
	 *         in case the node has not yet been displayed.
	 */
	public Window getWindow() {
		return getView().getWindow();
	}

	/**
	 * Convenient static method for setting the given {@link View} to the supplied
	 * {@code controller}.
	 *
	 * @param controller the controller to set the view into
	 * @param view       the view to set
	 */
	public static void setViewOn(final Object controller, final View view) {
		of(controller).setView(view);
	}

	/**
	 * Convenient static method for getting the {@link View} from the supplied
	 * {@code controller}.
	 *
	 * @param controller the controller to get the view from
	 * @return the retrieved {@link View}
	 */
	public static View getViewFrom(final Object controller) {
		return of(controller).getView();
	}

	/**
	 * Convenient static method for getting the {@link ResourceBundle} from the
	 * supplied controller.
	 *
	 * @param controller the controller to get the resource bundle from
	 * @return the retrieved {@link ResourceBundle}
	 */
	public static ResourceBundle getResourceBundleFrom(final Object controller) {
		return of(controller).getResourceBundle();
	}

	/**
	 * Retrieves the field that is expected to hold the {@link View}. In case the
	 * class does not hold the expected field or the field is of an unexpected type,
	 * an {@link IllegalStateException} is thrown.
	 *
	 * @return the field that is holding the view
	 */
	public Field getViewField() {
		try {
			final Field field = controller.getClass().getField(ActionFXEnhancer.VIEW_FIELD_NAME);
			if (!View.class.isAssignableFrom(field.getType())) {
				throw new IllegalStateException("Class '" + controller.getClass().getCanonicalName() + "' has field '"
						+ ActionFXEnhancer.VIEW_FIELD_NAME + "', but it is not of type '"
						+ View.class.getCanonicalName() + "'!");
			}
			return field;
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException(
					"Class '" + controller.getClass().getCanonicalName() + "' does not have a field with name '"
							+ ActionFXEnhancer.VIEW_FIELD_NAME + "'. Has the class been enhanced by ActionFX?",
					e);
		}
	}
}
