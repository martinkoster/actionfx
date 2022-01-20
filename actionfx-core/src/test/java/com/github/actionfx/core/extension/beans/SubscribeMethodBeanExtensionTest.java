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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXSubscribe;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForEachMonocleExtension;

/**
 * JUnit test case for {@link SubscribeMethodBeanExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForEachMonocleExtension.class)
@TestInFxThread
class SubscribeMethodBeanExtensionTest {

	private final PriorityAwareEventBus eventBus = Mockito.mock(PriorityAwareEventBus.class);

	@BeforeEach
	void onSetup() {
		final BeanContainerFacade container = Mockito.mock(BeanContainerFacade.class);
		final ControllerWithAFXSubscribe controller = new ControllerWithAFXSubscribe();
		when(container.getBean(PriorityAwareEventBus.class)).thenReturn(eventBus);
		when(container.getBean("controllerWithAFXSubscribe")).thenReturn(controller);
		ActionFX.builder().beanContainer(container).build().scanForActionFXComponents();
	}

	@Test
	void testExtend() {
		// GIVEN
		final SubscribeMethodBeanExtension extension = new SubscribeMethodBeanExtension(eventBus);

		// WHEN
		extension.extendBean(ControllerWithAFXSubscribe.class, "controllerWithAFXSubscribe", false, false);

		// THEN
		verify(eventBus, times(2)).subscribe(eq(String.class), any(), eq(1));
		verify(eventBus, times(1)).subscribe(eq(String.class), any(), eq(2));
		verify(eventBus, times(1)).subscribe(eq(String.class), any(), eq(3));
	}

	private static class ControllerWithAFXSubscribe {

		@AFXSubscribe(value = String.class, order = 2)
		public void onPublish() {

		}

		@AFXSubscribe(value = String.class, order = 1)
		public void onPublish(final String message) {

		}

		@AFXSubscribe(value = String.class, order = 3)
		public void anotherOnPublish(final String message) {

		}

		@AFXSubscribe(value = String.class, async = true)
		public void onAsyncPublish(final String message) {

		}
	}

}
