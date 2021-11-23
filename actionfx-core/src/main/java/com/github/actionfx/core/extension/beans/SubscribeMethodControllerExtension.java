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
package com.github.actionfx.core.extension.beans;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXSubscribe;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.method.ActionFXMethodInvocation;

/**
 * Extends beans for methods annotated by {@link AFXSubscribe}, which allows a
 * simple publish/subscribe pattern and a loose coupling of ActionFX
 * controllers.
 * <p>
 * This extension is realized as "beans" extension and not as "controller"
 * extension, as this extension needs to call methods on beans that may not be
 * instantiated yet on "publish" time (e.g. lazily created beans), while
 * respecting also the bean lifecycle of prototype-scope beans. In order to
 * guarantee this, the bean itself is retrieved with
 * {@link ActionFX#getBean(Class)} inside the consumer.
 *
 * @author koster
 *
 */
public class SubscribeMethodControllerExtension extends AbstractAnnotatedMethodBeansExtension<AFXSubscribe> {

	public SubscribeMethodControllerExtension() {
		super(AFXSubscribe.class);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void extend(final Class<?> beanClass, final String beanId, final boolean singleton,
			final boolean lazyInit, final Method annotatedElement, final AFXSubscribe annotation) {
		final PriorityAwareEventBus eventBus = ActionFX.getInstance().getEventBus();
		final Consumer subscriber = createSubscriber(annotation.async(), beanId, annotatedElement);
		eventBus.subscribe(annotation.value(), subscriber, annotation.order());
	}

	/**
	 * Creates a subscriber that retrieves the correct instance from the bean
	 * container via {@link ActionFX#getBean(Class)} each time an event is fired.
	 *
	 * @param async            {@code true}, if the method invocation shall be
	 *                         executed in an asynchronous fashion and {@code false}
	 *                         for a synchronous fashion.
	 * @param beanId           the ID of the bean that shall be retrieved from the
	 *                         bean container
	 * @param annotatedElement the annotated element
	 * @return the consumer that can be used as subscriber for the event bus
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Consumer createSubscriber(final boolean async, final String beanId, final Method annotatedElement) {
		return event -> {
			final Object bean = ActionFX.getInstance().getBean(beanId);
			final Consumer methodInvocationConsumer = async
					? ActionFXMethodInvocation.forSubscriberWithAsyncCall(bean, annotatedElement)
					: ActionFXMethodInvocation.forSubscriber(bean, annotatedElement);
			methodInvocationConsumer.accept(bean);
		};
	}

}
