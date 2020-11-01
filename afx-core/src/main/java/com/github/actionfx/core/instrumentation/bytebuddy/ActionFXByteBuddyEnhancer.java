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
package com.github.actionfx.core.instrumentation.bytebuddy;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.github.actionfx.core.annotation.AFXAction;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.interceptors.AFXActionMethodInterceptor;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

public class ActionFXByteBuddyEnhancer implements ActionFXEnhancer {

	// Cache of already enhanced classes, where the key is the canonical name of the
	// class
	private Map<String, Class<?>> ENHANCED_CLASSES_CACHE = new HashMap<>();

	// enhanced classes have this fragment in its canonical name. by that, we can
	// recognized whether the given class is already enhanced or not
	protected static final String BYTE_BUDDY_ENHANCED_CLASS_NAME_MARKER = "$ByteBuddy";

	// field is static, because the installation of the runtime agent is independent
	// of this instance, but JVM-dependent
	private static boolean agentInstalled = false;

	@Override
	public void installAgent() {
		Instrumentation instrumentation = ByteBuddyAgent.install();
		new AgentBuilder.Default().with(RedefinitionStrategy.RETRANSFORMATION)
				.type(ElementMatchers.isAnnotatedWith(AFXController.class))
				.transform(
						(builder, typeDescription, classLoader,
								module) -> builder.method(ElementMatchers.isAnnotatedWith(AFXAction.class))
										.intercept(MethodDelegation.withDefaultConfiguration()
												.to(AFXActionMethodInterceptorDelegator.class)))
				.installOn(instrumentation);
		agentInstalled = true;
	}

	@Override
	public boolean agentInstalled() {
		return agentInstalled;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class<?> enhanceClass(Class<?> originalClass) {
		// is the class already enhanced?
		String canonicalName = originalClass.getCanonicalName();
		if (canonicalName.contains(BYTE_BUDDY_ENHANCED_CLASS_NAME_MARKER)) {
			// class is already enhanced!
			return originalClass;
		}
		Class<?> enhancedClassFromCache = ENHANCED_CLASSES_CACHE.get(originalClass.getCanonicalName());
		if (enhancedClassFromCache != null) {
			return enhancedClassFromCache;
		}
		DynamicType.Unloaded unloadedType = new ByteBuddy().subclass(originalClass)
				.method(ElementMatchers.isAnnotatedWith(AFXAction.class))
				.intercept(MethodDelegation.withDefaultConfiguration().to(AFXActionMethodInterceptorDelegator.class))
				.make();

		Class<?> enhancedClass = unloadedType.load(getClass().getClassLoader()).getLoaded();
		ENHANCED_CLASSES_CACHE.put(originalClass.getCanonicalName(), enhancedClass);
		return enhancedClass;
	}

	/**
	 * Interceptor implementation for handling method invocation of methods
	 * annotated with the {@link AFXAction} annotation. This helper class just
	 * delegates the interception to a technology-neutral interceptor.
	 * 
	 * @author koster
	 *
	 */
	public static class AFXActionMethodInterceptorDelegator {

		@RuntimeType
		public static Object intercept(@SuperCall Callable<?> callable, @This Object instance, @Origin Method method,
				@AllArguments Object... args) throws Exception {
			AFXAction afxAction = method.getAnnotation(AFXAction.class);
			return AFXActionMethodInterceptor.interceptAFXAction(afxAction, callable, instance, method, args);
		}
	}

}
