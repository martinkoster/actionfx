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
package com.github.actionfx.core.container;

import java.util.Locale;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.container.instantiation.ControllerInstantiationSupplier;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.View;

/**
 * Abstract base class for ActionFX' bean container implementation providing
 * some basic bean handling code for ActionFX.
 *
 * @author koster
 *
 */
public abstract class AbstractActionFXBeanContainer implements BeanContainerFacade {

	@Override
	public void addControllerBeanDefinition(final Class<?> controllerClass) {
		final AFXController afxController = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);
		if (afxController == null) {
			throw new IllegalArgumentException(controllerClass + " is not annotated by @AFXController!");
		}
		final ControllerInstantiationSupplier<?> controllerSupplier = new ControllerInstantiationSupplier<>(
				controllerClass, () -> resolveResourceBundle(controllerClass, getBean(Locale.class)));
		final String controllerId = deriveBeanId(controllerClass);

		// add a bean definition for the controller
		addBeanDefinition(controllerId, controllerClass, afxController.singleton(), afxController.lazyInit(),
				controllerSupplier);

		// and add a bean definition for the view (view itself is injected into the
		// controller instance)
		addBeanDefinition(afxController.viewId(), View.class, afxController.singleton(), afxController.lazyInit(),
				() -> ControllerWrapper.getViewFrom(getBean(controllerId)));
	}

}
