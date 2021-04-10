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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.AFXUtils;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.AbstractView;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.ViewBuilder;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Parent;

/**
 * Instantiation supplier for controller instances. This class is responsible
 * for instantiating controller classes. In case that ActionFX is configured to
 * use {@link EnhancementStrategy#SUBCLASSING} as enhancement strategy, a
 * dynamic sub-class is created for the supplied controller class by using the
 * configured {@link ActionFXEnhancer}.
 *
 * @author koster
 *
 */
public class ControllerInstantiationSupplier<T> extends AbstractInstantiationSupplier<T> {

	private final Class<T> controllerClass;

	private final ResourceBundle resourceBundle;

	public ControllerInstantiationSupplier(final Class<T> controllerClass) {
		this(controllerClass, null);
	}

	public ControllerInstantiationSupplier(final Class<T> controllerClass, final ResourceBundle resourceBundle) {
		this.controllerClass = prepareControllerClass(controllerClass);
		this.resourceBundle = resourceBundle;
	}

	/**
	 * Creates a new, fresh instance based on the supplied bean definition. This
	 * method ensures that instantiation is performed in the JavaFX thread, as this
	 * is required for certain view components (e.g. a WebView).
	 *
	 * @param <T>            the bean type
	 * @param beanDefinition the bean definition
	 * @return the created bean instance
	 */
	@Override
	protected T createInstance() {
		// instance is create in JavaFX thread, because certain node e.g. WebView
		// requires it.
		if (Platform.isFxApplicationThread()) {
			return createControllerInstance();
		} else {
			try {
				final Task<T> instantiationTask = new Task<>() {
					@Override
					protected T call() throws Exception {
						return createControllerInstance();
					}
				};
				// execute the task in the JavaFX thread and wait for the result
				return AFXUtils.runInFxThreadAndWait(instantiationTask);
			} catch (InterruptedException | ExecutionException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Failed to instantiate class in JavaFX thread!", e);
			}
		}
	}

	/**
	 * Prepares the {@code controllerClass} to use. In case the sub-classing
	 * enhancement strategy is configured, a dynamic sub-class is created by using
	 * the configure {@link ActionFXEnhancer}.
	 *
	 * @param controllerClass the controller class to prepare
	 * @return the prepared controller class, potentially sub-classes depending on
	 *         the ActionFX configuration
	 */
	@SuppressWarnings("unchecked")
	private Class<T> prepareControllerClass(final Class<T> controllerClass) {
		final ActionFX actionFX = ActionFX.getInstance();
		return actionFX.getEnhancementStrategy() == EnhancementStrategy.SUBCLASSING
				? (Class<T>) actionFX.getEnhancer().enhanceClass(controllerClass)
				: controllerClass;
	}

	/**
	 * Creates the controller instance and wires all applied annotations to the
	 * controller.
	 *
	 * @return the instantiated controller
	 */
	private T createControllerInstance() {
		try {
			final T controller = controllerClass.getDeclaredConstructor().newInstance();
			final View view = createViewInstance(controller);
			injectView(controller, view);
			return controller;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Can not instaniate class '" + controllerClass.getCanonicalName()
					+ "'! Is there a no-arg constructor present?");
		}
	}

	/**
	 * Creates an {@link View} instance for the given {@code controller}.
	 *
	 * @param controller the controller for that the view shall be created
	 * @return the created view
	 */
	private View createViewInstance(final Object controller) {
		final AFXController afxController = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);
		final AbstractView view = instantiateView(controller, afxController);
		final ViewBuilder<AbstractView> builder = new ViewBuilder<>(view);
		final List<AFXNestedView> nestedViews = AnnotationUtils.findAllAnnotations(controllerClass,
				AFXNestedView.class);
		return builder.posX(afxController.posX()).posY(afxController.posY()).width(afxController.width())
				.height(afxController.height()).maximized(afxController.maximized())
				.modalDialogue(afxController.modal()).icon(afxController.icon())
				.stylesheets(afxController.stylesheets()).nestedViews(nestedViews).windowTitle(afxController.title())
				.getView();
	}

	/**
	 * Creates an {@link AbstractView} instance for the given {@code controller},
	 * using the parameters in {@link AFXController}.
	 *
	 * @param controller the controller for that the view shall be created
	 * @return the created view
	 */
	private AbstractView instantiateView(final Object controller, final AFXController afxController) {
		if (!"".equals(afxController.fxml())) {
			return new FxmlView(afxController.viewId(), afxController.fxml(), controller, resourceBundle);
		}
		if (!Parent.class.equals(afxController.viewClass())) {
			return new ParentView(afxController.viewId(), afxController.viewClass(), controller, resourceBundle);
		}
		throw new IllegalStateException("Controller class '" + controller.getClass()
				+ "' has @AFXController annotation, which does not specify 'fxml()' or 'viewClass()' attribute!");
	}

	/**
	 * Injects the view into the supplied controller. Please note that the
	 * controller is expected to be enhanced with a field of name "_view".
	 *
	 * @param controller the controller
	 * @param view       the view
	 */
	private void injectView(final T controller, final View view) {
		ControllerWrapper.setViewOn(controller, view);
	}

}
