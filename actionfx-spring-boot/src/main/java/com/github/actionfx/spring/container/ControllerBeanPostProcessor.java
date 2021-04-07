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
package com.github.actionfx.spring.container;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.container.instantiation.ControllerInstancePostProcessor;
import com.github.actionfx.core.utils.AnnotationUtils;

/**
 * Post-processor that is invoked after Spring's dependency injection in order
 * to wire annotated methods to JavaFX components.
 * <p>
 * The actual wiring logic is implemented in
 * {@link ControllerInstancePostProcessor}, so please refer to this class for
 * further information.
 *
 * @author koster
 *
 */
public class ControllerBeanPostProcessor implements BeanPostProcessor {

	private final ControllerInstancePostProcessor controllerInstancePostProcessor;

	public ControllerBeanPostProcessor(final List<Consumer<Object>> customControllerExtension) {
		controllerInstancePostProcessor = new ControllerInstancePostProcessor(customControllerExtension);
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
		// wire JavaFX components to annotated methods
		if (AnnotationUtils.findAnnotation(bean.getClass(), AFXController.class) != null) {
			controllerInstancePostProcessor.postProcess(bean);
		}
		return bean;
	}
}
