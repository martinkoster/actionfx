/*
 * Copyright (c) 2021 Martin Koster
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
package com.github.actionfx.core.container.extension;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXSubscribe;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.method.ActionFXMethodInvocation;

/**
 * Extends controllers for methods annotated by {@link AFXSubscribe}, which
 * allows a simple publish/subscribe pattern and a loose coupling of ActionFX
 * controllers.
 *
 * @author koster
 *
 */
public class SubscribeMethodControllerExtension extends AbstractAnnotatedMethodControllerExtension<AFXSubscribe> {

	public SubscribeMethodControllerExtension() {
		super(AFXSubscribe.class);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void extend(final Object controller, final Method annotatedElement, final AFXSubscribe annotation) {
		final PriorityAwareEventBus eventBus = ActionFX.getInstance().getEventBus();
		final Consumer subscriber = annotation.async()
				? ActionFXMethodInvocation.forSubscriberWithAsyncCall(controller, annotatedElement)
				: ActionFXMethodInvocation.forSubscriber(controller, annotatedElement);
		eventBus.subscribe(annotation.value(), subscriber, annotation.order());
	}
}
