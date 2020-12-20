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

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.ViewBuilder;

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

	public ControllerInstantiationSupplier(final Class<T> controllerClass) {
		this.controllerClass = prepareControllerClass(controllerClass);
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
	protected Class<T> prepareControllerClass(final Class<T> controllerClass) {
		final ActionFX actionFX = ActionFX.getInstance();
		return actionFX.getEnhancementStrategy() == EnhancementStrategy.SUBCLASSING
				? (Class<T>) actionFX.getEnhancer().enhanceClass(controllerClass)
				: controllerClass;
	}

	@Override
	protected T createInstance() {
		try {
			final T controller = controllerClass.getDeclaredConstructor().newInstance();
			final FxmlView fxmlView = createFxmlViewInstance(controller);
			injectView(controller, fxmlView);
			return controller;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Can not instaniate class '" + controllerClass.getCanonicalName()
					+ "'! Is there a no-arg constructor present?");
		}
	}

	protected FxmlView createFxmlViewInstance(final Object controller) {
		final AFXController afxController = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);
		final FxmlView fxmlView = new FxmlView(afxController.viewId(), afxController.fxml(), controller);
		final ViewBuilder<FxmlView> builder = new ViewBuilder<>(fxmlView);
		return builder.posX(afxController.posX()).posY(afxController.posY()).width(afxController.width())
				.height(afxController.height()).maximized(afxController.maximized())
				.modalDialogue(afxController.modal()).icon(afxController.icon())
				.stylesheets(afxController.stylesheets()).nestedViews(afxController.nestedViews())
				.windowTitle(afxController.title()).getView();
	}

	/**
	 * Injects the view into the supplied controller. Please note that the
	 * controller is expected to be enhanced with a field of name "_view".
	 *
	 * @param controller the controller
	 * @param view       the view
	 */
	protected void injectView(final T controller, final View view) {
		ControllerWrapper.setViewOn(controller, view);
	}
}
