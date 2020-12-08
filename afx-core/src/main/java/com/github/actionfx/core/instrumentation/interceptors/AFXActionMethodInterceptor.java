package com.github.actionfx.core.instrumentation.interceptors;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXAction;

/**
 * Interceptor implementation for handling method invocation of methods
 * annotated with the {@link AFXAction} annotation.
 *
 * @author koster
 *
 */
public class AFXActionMethodInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(AFXActionMethodInterceptor.class);

	private AFXActionMethodInterceptor() {
		// class can not be instantiated
	}

	public static Object interceptAFXAction(final AFXAction afxAction, final Callable<?> callable,
			final Object instance, final Method method, final Object... args) throws Exception {
		try {
			final Object result = callable.call();
			doOnSuccess(afxAction, instance, method, result);
			return result;
		} finally {

		}
	}

	/**
	 * Executes the on-success action defined in {@link AFXAction}.
	 *
	 * @param afxAction              the {@link AFXAction} annotation
	 * @param instance               the instance holding the intercepted method
	 * @param method                 the intercepted method
	 * @param methodInvocationResult the result of the intercepted method
	 */
	private static void doOnSuccess(final AFXAction afxAction, final Object instance, final Method method,
			final Object methodInvocationResult) {
		if (!"".equals(afxAction.showView())) {
			showView(afxAction, instance, method, methodInvocationResult);
		} else if (afxAction.showNestedViews().length > 0) {
			showNestedViews(afxAction, instance, method, methodInvocationResult);
		}
	}

	private static void showView(final AFXAction afxAction, final Object instance, final Method method,
			final Object methodInvocationResult) {
		final ActionFX actionFX = ActionFX.getInstance();

	}

	private static void showNestedViews(final AFXAction afxAction, final Object instance, final Method method,
			final Object methodInvocationResult) {
		final ActionFX actionFX = ActionFX.getInstance();

	}

}