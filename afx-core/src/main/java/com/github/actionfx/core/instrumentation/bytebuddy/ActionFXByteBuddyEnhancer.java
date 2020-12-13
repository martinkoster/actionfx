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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.annotation.AFXAction;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.interceptors.AFXActionMethodInterceptor;
import com.github.actionfx.core.view.View;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Byte-code enhancer using the ByteBuddy framework.
 *
 * @author koster
 *
 */
public class ActionFXByteBuddyEnhancer implements ActionFXEnhancer {

	private static final Logger LOG = LoggerFactory.getLogger(ActionFXByteBuddyEnhancer.class);

	// Cache of already enhanced classes, where the key is the canonical name of the
	// class
	private static final Map<String, Class<?>> ENHANCED_CLASSES_CACHE = new HashMap<>();

	// enhanced classes have this fragment in its canonical name. by that, we can
	// recognized whether the given class is already enhanced or not
	protected static final String BYTE_BUDDY_ENHANCED_CLASS_NAME_MARKER = "$ByteBuddy";

	// field is static, because the installation of the runtime agent is independent
	// of this instance, but JVM-dependent
	private static boolean agentInstalled = false;

	@Override
	public void installAgent() {
		if (agentInstalled) {
			return;
		}
		final Instrumentation instrumentation = ByteBuddyAgent.install();
		new AgentBuilder.Default().with(RedefinitionStrategy.RETRANSFORMATION)
				.type(ElementMatchers.isAnnotatedWith(AFXController.class))
				.transform((builder, typeDescription, classLoader, module) -> addViewField(interceptMethods(builder)))
				.installOn(instrumentation);
		if (LOG.isDebugEnabled()) {
			LOG.debug("ByteBuddy agent successfully installed.");
		}
		agentInstalled = true;
	}

	@Override
	public boolean agentInstalled() {
		return agentInstalled;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class<?> enhanceClass(final Class<?> originalClass) {
		// is the class already enhanced?
		final String canonicalName = originalClass.getCanonicalName();
		if (canonicalName.contains(BYTE_BUDDY_ENHANCED_CLASS_NAME_MARKER)) {
			// class is already enhanced!
			return originalClass;
		}
		final Class<?> enhancedClassFromCache = ENHANCED_CLASSES_CACHE.get(originalClass.getCanonicalName());
		if (enhancedClassFromCache != null) {
			return enhancedClassFromCache;
		}
		final DynamicType.Unloaded unloadedType = addViewField(
				interceptMethods(new ByteBuddy().subclass(originalClass))).make();

		final Class<?> enhancedClass = unloadedType.load(getClass().getClassLoader()).getLoaded();
		ENHANCED_CLASSES_CACHE.put(originalClass.getCanonicalName(), enhancedClass);
		return enhancedClass;
	}

	/**
	 * Adds logic in order to intercept annotated methods.
	 *
	 * @param builder the ByteBuddy dynamic builder
	 * @return the builder
	 */
	private DynamicType.Builder<?> interceptMethods(final DynamicType.Builder<?> builder) {
		return builder.method(ElementMatchers.isAnnotatedWith(AFXAction.class))
				.intercept(MethodDelegation.withDefaultConfiguration().to(AFXActionMethodInterceptorDelegator.class));
	}

	/**
	 * Adds a new field to the class under enhancement.
	 *
	 * @param builder the ByteBuddy dynamic builder
	 * @return the builder
	 */
	private DynamicType.Builder<?> addViewField(final DynamicType.Builder<?> builder) {
		return builder.defineField(VIEW_FIELD_NAME, View.class, Visibility.PUBLIC);
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
		public static Object intercept(@SuperCall final Callable<?> callable, @This final Object instance,
				@Origin final Method method, @AllArguments final Object... args) throws Exception {
			final AFXAction afxAction = method.getAnnotation(AFXAction.class);
			return AFXActionMethodInterceptor.interceptAFXAction(afxAction, callable, instance, method, args);
		}
	}

}
