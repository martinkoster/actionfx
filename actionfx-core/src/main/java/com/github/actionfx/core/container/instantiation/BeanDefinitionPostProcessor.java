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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.extension.beans.BeanExtension;
import com.github.actionfx.core.extension.beans.SubscribeMethodBeanExtension;

/**
 * Post-processor for bean definitions after these are added to the bean
 * container.
 *
 * @author koster
 *
 */
public class BeanDefinitionPostProcessor {

	private final List<BeanExtension> beanExtensions = new ArrayList<>();

	public BeanDefinitionPostProcessor(final PriorityAwareEventBus eventBus) {
		this(eventBus, Collections.emptyList());
	}

	public BeanDefinitionPostProcessor(final PriorityAwareEventBus eventBus,
			final List<BeanExtension> customBeanExtensions) {
		beanExtensions.add(new SubscribeMethodBeanExtension(eventBus));

		// add the custom controller extensions
		beanExtensions.addAll(customBeanExtensions);
	}

	/**
	 * Performs a post-processing on the supplied bean definition for
	 * {@code beanClass}
	 *
	 * @param beanClass the bean class / type
	 * @param beanId    the bean Id / name
	 * @param flag      that indicates whether the given bean is a singleton or not
	 * @param flag      that indicates whether the given bean is lazily initialized
	 *                  or not
	 *
	 */
	public void postProcess(final Class<?> beanClass, final String beanId, final boolean singleton,
			final boolean lazyInit) {
		beanExtensions.forEach(extension -> extension.extendBean(beanClass, beanId, singleton, lazyInit));
	}

	public List<BeanExtension> getUnmodifiableBeanExtensions() {
		return Collections.unmodifiableList(beanExtensions);
	}
}
