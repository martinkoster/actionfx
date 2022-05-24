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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.actionfx.core.utils.ExceptionUtils;
import com.github.actionfx.core.utils.ReflectionUtils;

/**
 * Straight forward implementation of the {@link PriorityAwareEventBus}
 * implementation that considers a priority assigned to a registered subscriber,
 * when publishing an event to its subscriber.
 * <p>
 * Please note that this class does not implement any sophisticated
 * publish/subscribe strategies like backpressure, etc. as this is not required
 * for inter-controller eventing as of the moment.
 * <p>
 * Furthermore, asynchronous invocations need to be implemented in the provided
 * {@link Consumer} itself and is not provided by this event bus implementation.
 *
 * @author koster
 *
 */
public class SimplePriorityAwareEventBus implements PriorityAwareEventBus {

	private final PrioritizedSubscriberMap subscriberMap = new PrioritizedSubscriberMap();

	public static final Consumer<Exception> RETHROW_EXCEPTION_CALLBACK = ex -> {
		throw ExceptionUtils.wrapInRuntimeExceptionIfNecessary(ex);
	};

	@Override
	public void publish(final Object event) {
		publish(event, RETHROW_EXCEPTION_CALLBACK);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void publish(final Object event, final Consumer<Exception> exceptionCallback) {
		try {
			final List<Consumer> subscribers = subscriberMap.lookup(event.getClass());
			for (final Consumer subscriber : subscribers) {
				subscriber.accept(event);
			}
		} catch (final Exception throwable) { // NOSONAR
			exceptionCallback.accept(throwable);
		}
	}

	@Override
	public <T> void subscribe(final Class<T> eventType, final Consumer<? super T> eventConsumer, final int priority) {
		subscriberMap.register(eventType, eventConsumer, priority);
	}

	/**
	 * Internal structure for looking up subscribers.
	 *
	 * @author koster
	 *
	 */
	private static class PrioritizedSubscriberMap {

		@SuppressWarnings("rawtypes")
		private final Map<Class, List<PrioritizedConsumer>> typeSubscriberMap = new HashMap<>();

		/**
		 * Registers a single subscriber to the given {@code clazz}.
		 *
		 * @param clazz      the clazz to subscribe to
		 * @param subscriber the subscriber
		 * @param priority   the priority (lower value means "higher" priority)
		 */
		@SuppressWarnings("rawtypes")
		public void register(final Class clazz, final Consumer subscriber, final int priority) {
			final List<PrioritizedConsumer> subscriberList = typeSubscriberMap.computeIfAbsent(clazz,
					cls -> new ArrayList<>());
			subscriberList.add(PrioritizedConsumer.of(priority, subscriber));
		}

		/**
		 * Looks up all subscriber for the given {@code clazz}, while the order in the
		 * returned list is impacted by the priority provided during subscription time.
		 *
		 * @param clazz the class to lookup subscribers for
		 * @return the subscribers for the given {@code clazz}, or an empty list, in
		 *         case there are no subscribers.
		 */
		@SuppressWarnings("rawtypes")
		public List<Consumer> lookup(final Class clazz) {
			final Set<Class<?>> superTypes = ReflectionUtils.getAllSuperClassesAndInterfaces(clazz);
			return superTypes.stream().map(typeSubscriberMap::get).filter(Objects::nonNull).flatMap(Collection::stream)
					.sorted((c1, c2) -> Integer.compare(c1.getPriority(), c2.getPriority()))
					.map(PrioritizedConsumer::getConsumer).collect(Collectors.toList());
		}

		/**
		 * Consumer with an assigned priority.
		 *
		 * @author koster
		 *
		 */
		private static class PrioritizedConsumer {

			private final int priority;

			@SuppressWarnings("rawtypes")
			private final Consumer consumer;

			@SuppressWarnings("rawtypes")
			public PrioritizedConsumer(final int priority, final Consumer consumer) {
				this.priority = priority;
				this.consumer = consumer;
			}

			@SuppressWarnings("rawtypes")
			public static PrioritizedConsumer of(final int priority, final Consumer consumer) {
				return new PrioritizedConsumer(priority, consumer);
			}

			public int getPriority() {
				return priority;
			}

			@SuppressWarnings("rawtypes")
			public Consumer getConsumer() {
				return consumer;
			}

		}
	}
}
