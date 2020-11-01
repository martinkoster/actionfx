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
package com.github.actionfx.core.view.instantiation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import com.github.actionfx.core.annotation.AFXAction;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Instantiation supplier for controller instances. This class is responsible
 * for byte instrumentation of the controller.
 * 
 * @author koster
 *
 */
public class ControllerInstantiationSupplier<T> extends AbstractInstantiationSupplier<T> {

	private static final Set<Class<?>> INSTRUMENTED_CLASSES = new HashSet<>();

	private Class<?> controllerClass;

	public ControllerInstantiationSupplier(Class<T> controllerClass) {
		this.controllerClass = controllerClass;
		if (!INSTRUMENTED_CLASSES.contains(controllerClass)) {
			instrumentControllerClass(controllerClass);
			INSTRUMENTED_CLASSES.add(controllerClass);
		}
	}

	/**
	 * Applies byte code instrumentation on the supplied controller class.
	 * <p>
	 * Following changes are applied to the class:
	 * <ul>
	 * <li>Methods annotated by {@link AFXAction} are intercepted, calls are
	 * delegate to {@link AFXActionMethodInterceptor}.
	 * </ul>
	 * 
	 * @param controllerClass the controller class to instrument
	 */
	protected void instrumentControllerClass(Class<T> controllerClass) {
		AFXActionMethodInterceptor actionMethodInterceptor = new AFXActionMethodInterceptor();
		new ByteBuddy().rebase(controllerClass).method(ElementMatchers.isAnnotatedWith(AFXAction.class))
				.intercept(MethodDelegation.withDefaultConfiguration().to(AFXActionMethodInterceptor.class)).make()
				.load(controllerClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T createInstance() {
		try {
			return (T) controllerClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Can not instaniate class '" + controllerClass.getCanonicalName()
					+ "'! Is there a no-arg constructor present?");
		}
	}

	/**
	 * Interceptor implementation for handling method invocation of methods
	 * annotated with the {@link AFXAction} annotation.
	 * 
	 * @author koster
	 *
	 */
	public static class AFXActionMethodInterceptor {

		@BindingPriority(1)
		@RuntimeType
		public static Object intercept(@This Object instance, @Origin Method interceptedMethod,
				@AllArguments Object... args) throws Exception {
			long before = System.currentTimeMillis();
			try {
				return interceptedMethod.invoke(instance, args);
			} finally {
				System.out.println("Took: " + (System.currentTimeMillis() - before));
			}
		}

		public static Object intercept(@SuperCall Callable<?> callable, @This Object instance) throws Exception {
			long before = System.currentTimeMillis();
			try {
				return callable.call();
			} finally {
				System.out.println("Took: " + (System.currentTimeMillis() - before));
			}
		}
	}

}
