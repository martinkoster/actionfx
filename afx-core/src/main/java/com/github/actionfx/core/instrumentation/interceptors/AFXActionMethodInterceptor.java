package com.github.actionfx.core.instrumentation.interceptors;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXShowView;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.ViewBuilder;

import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Interceptor implementation for handling method invocation of methods
 * annotated with the {@link AFXShowView} annotation.
 *
 * @author koster
 *
 */
public class AFXActionMethodInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(AFXActionMethodInterceptor.class);

	private AFXActionMethodInterceptor() {
		// class can not be instantiated
	}

	public static Object interceptAFXAction(final AFXShowView afxAction, final Callable<?> callable,
			final Object instance, final Method method, final Object... args) throws Exception {
		try {
			final Object result = callable.call();
			doOnSuccess(afxAction, instance, method, result);
			return result;
		} finally {

		}
	}

	/**
	 * Executes the on-success action defined in {@link AFXShowView}.
	 *
	 * @param afxAction              the {@link AFXShowView} annotation
	 * @param instance               the instance holding the intercepted method
	 * @param method                 the intercepted method
	 * @param methodInvocationResult the result of the intercepted method
	 */
	private static void doOnSuccess(final AFXShowView afxAction, final Object instance, final Method method,
			final Object methodInvocationResult) {
		if (!"".equals(afxAction.showView())) {
			showView(afxAction, instance, method, methodInvocationResult);
		} else if (afxAction.showNestedViews().length > 0) {
			showNestedViews(afxAction, instance, method, methodInvocationResult);
		}
	}

	/**
	 * Shows the view inside a window, either freshly created or inside the
	 * currently displayed one.
	 *
	 * @param afxAction              the {@link AFXShowView} annotation that is
	 *                               intercepted
	 * @param instance               the controller instance
	 * @param method                 the intercepted method
	 * @param methodInvocationResult the result of the intercepted method
	 */
	private static void showView(final AFXShowView afxAction, final Object instance, final Method method,
			final Object methodInvocationResult) {
		final String viewId = afxAction.showView();
		final View view = getViewById(viewId);
		if (afxAction.showInNewWindow()) {
			// display in a new fresh stage
			view.show(new Stage());
		} else {
			// use the current window to display the content
			final Window window = ControllerWrapper.of(instance).getWindow();
			if (window == null) {
				throw new IllegalStateException(
						"The view is not part of a window? Did you forget to display your view inside the primary stage?");
			}
			final Class<?> windowType = window.getClass();
			if (Stage.class.isAssignableFrom(windowType)) {
				view.show((Stage) window);
			} else if (Popup.class.isAssignableFrom(windowType)) {
				final Popup popup = (Popup) window;
				view.show(popup, popup.getOwnerWindow());
			} else {
				throw new IllegalStateException("Display of view with id '" + viewId
						+ "' is not supported for window of type '" + windowType.getCanonicalName() + "'!");
			}
		}
	}

	/**
	 * Shows one or multiple nested views inside the currently displayed scene graph
	 * by attaching the new views.
	 *
	 * @param afxAction              the {@link AFXShowView} annotation that is
	 *                               intercepted
	 * @param instance               the controller instance
	 * @param method                 the intercepted method
	 * @param methodInvocationResult the result of the intercepted method
	 */
	private static void showNestedViews(final AFXShowView afxAction, final Object instance, final Method method,
			final Object methodInvocationResult) {
		final View currentView = ControllerWrapper.getViewFrom(instance);
		ViewBuilder.embedNestedViews(currentView, afxAction.showNestedViews());
	}

	/**
	 * Gets a view by its id.
	 *
	 * @param viewId the id of the view
	 * @return the retrieved view
	 */
	private static View getViewById(final String viewId) {
		final ActionFX actionFX = ActionFX.getInstance();
		final View view = actionFX.getView(viewId);
		if (view == null) {
			throw new IllegalStateException("View with id '" + viewId + "' does not exist!");
		}
		return view;
	}
}