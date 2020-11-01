package com.github.actionfx.core.instrumentation.interceptors;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import com.github.actionfx.core.annotation.AFXAction;

/**
 * Interceptor implementation for handling method invocation of methods
 * annotated with the {@link AFXAction} annotation.
 * 
 * @author koster
 *
 */
public class AFXActionMethodInterceptor {

	public static Object interceptAFXAction(AFXAction afxAction, Callable<?> callable, Object instance, Method method,
			Object... args) throws Exception {
		System.out.println("Entering method " + method.getName() + ", annotation " + afxAction);
		try {
			return callable.call();
		} finally {
			System.out.println("Exiting");
		}
	}
}