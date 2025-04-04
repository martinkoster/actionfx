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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * JUnit test case for {@link SimplePriorityAwareEventBus}.
 *
 * @author koster
 *
 */
class SimplePriorityAwareEventBusTest {

	@SuppressWarnings("unchecked")
	@Test
	void testSubscribePublish_oneSubscriptionForBaseType_publishWithDerivedType() {
		// GIVEN
		final SimplePriorityAwareEventBus eventBus = new SimplePriorityAwareEventBus();
		final Consumer<BaseType> baseTypeConsumer = Mockito.mock(Consumer.class);
		final DerivedType derivedType = new DerivedType();

		// WHEN
		eventBus.subscribe(BaseType.class, baseTypeConsumer, 1);
		eventBus.publish(derivedType);

		// THEN
		verify(baseTypeConsumer, times(1)).accept(ArgumentMatchers.eq(derivedType));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubscribePublish_twoSubscriptions_publishWithDerivedType() {
		// GIVEN
		final SimplePriorityAwareEventBus eventBus = new SimplePriorityAwareEventBus();
		final Consumer<BaseType> baseTypeConsumer = Mockito.mock(Consumer.class);
		final Consumer<DerivedType> derivedTypeConsumer = Mockito.mock(Consumer.class);
		final DerivedType derivedType = new DerivedType();

		// WHEN
		eventBus.subscribe(BaseType.class, baseTypeConsumer, 1);
		eventBus.subscribe(DerivedType.class, derivedTypeConsumer, 1);
		eventBus.publish(derivedType);

		// THEN
		verify(baseTypeConsumer, times(1)).accept(ArgumentMatchers.eq(derivedType));
		verify(derivedTypeConsumer, times(1)).accept(ArgumentMatchers.eq(derivedType));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubscribePublish_oneSubscriptionForBaseType_usingRethrowHandler() {
		// GIVEN
		final SimplePriorityAwareEventBus eventBus = new SimplePriorityAwareEventBus();
		final Consumer<BaseType> baseTypeConsumer = Mockito.mock(Consumer.class);
		final DerivedType derivedType = new DerivedType();
		final IllegalStateException ex = new IllegalStateException();
		doThrow(ex).when(baseTypeConsumer).accept(ArgumentMatchers.eq(derivedType));

		// WHEN
		eventBus.subscribe(BaseType.class, baseTypeConsumer, 1);
		final IllegalStateException actual = assertThrows(IllegalStateException.class,
				() -> eventBus.publish(derivedType));

		// THEN
		verify(baseTypeConsumer, times(1)).accept(ArgumentMatchers.eq(derivedType));
		assertThat(actual).isSameAs(ex);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubscribePublish_oneSubscriptionForBaseType_usingExceptionConsumer() {
		// GIVEN
		final SimplePriorityAwareEventBus eventBus = new SimplePriorityAwareEventBus();
		final Consumer<BaseType> baseTypeConsumer = Mockito.mock(Consumer.class);
		final DerivedType derivedType = new DerivedType();
		final IllegalStateException ex = new IllegalStateException();
		doThrow(ex).when(baseTypeConsumer).accept(ArgumentMatchers.eq(derivedType));
		final Consumer<Exception> exceptionCallback = Mockito.mock(Consumer.class);

		// WHEN
		eventBus.subscribe(BaseType.class, baseTypeConsumer, 1);
		eventBus.publish(derivedType, exceptionCallback);

		// THEN
		verify(baseTypeConsumer, times(1)).accept(ArgumentMatchers.eq(derivedType));
		verify(exceptionCallback, times(1)).accept(ArgumentMatchers.eq(ex));
	}

	@Test
	void testSubscribePublish_subscriberAreCalledBasedOnTheirPriority() {
		// GIVEN
		final SimplePriorityAwareEventBus eventBus = new SimplePriorityAwareEventBus();
		final List<Integer> invocationOrder = new ArrayList<>();
		final Consumer<BaseType> baseTypeConsumer = event -> invocationOrder.add(2);
		final Consumer<DerivedType> derivedTypeConsumer1 = event -> invocationOrder.add(1);
		final Consumer<DerivedType> derivedTypeConsumer2 = event -> invocationOrder.add(3);

		final DerivedType derivedType = new DerivedType();

		// WHEN
		eventBus.subscribe(BaseType.class, baseTypeConsumer, 2); // medium prio
		eventBus.subscribe(DerivedType.class, derivedTypeConsumer1, 1); // highest prio
		eventBus.subscribe(DerivedType.class, derivedTypeConsumer2, 3); // lowest prio
		eventBus.publish(derivedType);

		// THEN
		assertThat(invocationOrder).containsExactly(1, 2, 3);
	}

	public static class BaseType {

	}

	public static class DerivedType extends BaseType {

	}

}
