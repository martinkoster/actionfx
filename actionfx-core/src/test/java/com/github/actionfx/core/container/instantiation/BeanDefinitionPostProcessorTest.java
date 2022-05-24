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
package com.github.actionfx.core.container.instantiation;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.extension.beans.BeanExtension;

/**
 * JUnit test case for {@link BeanDefinitionPostProcessor}.
 *
 * @author koster
 *
 */
class BeanDefinitionPostProcessorTest {

	@Test
	void testPostProcess() {
		// GIVEN
		final PriorityAwareEventBus eventBus = Mockito.mock(PriorityAwareEventBus.class);
		final BeanExtension extension1 = Mockito.mock(BeanExtension.class);
		final BeanExtension extension2 = Mockito.mock(BeanExtension.class);
		final BeanDefinitionPostProcessor processor = new BeanDefinitionPostProcessor(eventBus,
				Arrays.asList(extension1, extension2));

		// WHEN
		processor.postProcess(BeanDefinitionPostProcessorTest.class, "beanId", true, false);

		// THEN
		verify(extension1, times(1)).extendBean(eq(BeanDefinitionPostProcessorTest.class), eq("beanId"), eq(true),
				eq(false));
		verify(extension2, times(1)).extendBean(eq(BeanDefinitionPostProcessorTest.class), eq("beanId"), eq(true),
				eq(false));
	}

}
