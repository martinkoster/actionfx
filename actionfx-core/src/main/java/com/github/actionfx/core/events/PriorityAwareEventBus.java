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
package com.github.actionfx.core.events;

import java.util.function.Consumer;

/**
 * Basic interface of an event bus that allows a publish/subscribe with defining
 * a priority on the subscribers.
 * <p>
 * When registering subscribers, a priority value must be specified. A lower
 * value in the priority means "higher" priority in execution. Or: A consumer
 * with a lower value for priority will be notified on publications before
 * consumer with a higher priority value.
 *
 * @author koster
 *
 */
public interface PriorityAwareEventBus {

	/**
	 * Publish the given {@code event} to subscribed listener.
	 *
	 * @param event the event to publish
	 */
	void publish(Object event);

	/**
	 * Publish the given {@code event} to subscribed listener. In case an exception
	 * occurs while calling the subscribed listener, the given
	 * {@code exceptionCallback} is invoked.
	 *
	 * @param event             the event to publish
	 * @param exceptionCallback the callback that is invoked on exceptions
	 */
	void publish(Object event, Consumer<Exception> exceptionCallback);

	/**
	 * Subscribes the given {@code eventConsumer} to events of type
	 * {@code eventType}.
	 *
	 * @param <T>           the type descriptor
	 * @param eventType     the event type
	 * @param eventConsumer the event consumer when events of type {@code eventType}
	 *                      are published.
	 * @param priority      the priority of the given {@code eventConsumer}. A
	 *                      consumer with a lower value for priority will be
	 *                      notified on publications before consumer with a higher
	 *                      priority value.
	 */
	<T> void subscribe(Class<T> eventType, Consumer<? super T> eventConsumer, int priority);
}
