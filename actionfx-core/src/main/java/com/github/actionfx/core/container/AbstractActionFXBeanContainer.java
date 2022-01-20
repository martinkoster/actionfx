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

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.container.instantiation.BeanDefinitionPostProcessor;
import com.github.actionfx.core.container.instantiation.ControllerInstancePostProcessor;
import com.github.actionfx.core.container.instantiation.ControllerInstantiationSupplier;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.events.SimplePriorityAwareEventBus;
import com.github.actionfx.core.extension.ActionFXExtensionsBean;
import com.github.actionfx.core.extension.beans.BeanExtension;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.View;

import javafx.beans.value.ObservableValue;

/**
 * Abstract base class for ActionFX' bean container implementation providing
 * some basic bean handling code for ActionFX.
 *
 * @author koster
 *
 */
public abstract class AbstractActionFXBeanContainer implements BeanContainerFacade {

	protected ControllerInstancePostProcessor controllerInstancePostProcessor;

	protected BeanDefinitionPostProcessor beanDefinitionPostProcessor;

	protected PriorityAwareEventBus eventBus;

	/**
	 * Constructor that accepts custom ActionFX extensions (controller and beans
	 * extensions).
	 *
	 * @param extensionsBean the bean holding the extensions.
	 */
	protected AbstractActionFXBeanContainer(final ActionFXExtensionsBean extensionsBean) {
		eventBus = new SimplePriorityAwareEventBus();
		controllerInstancePostProcessor = extensionsBean != null
				? new ControllerInstancePostProcessor(extensionsBean.getCustomControllerExtensions())
				: new ControllerInstancePostProcessor();
		beanDefinitionPostProcessor = extensionsBean != null
				? new BeanDefinitionPostProcessor(eventBus, extensionsBean.getCustomBeanExtensions())
				: new BeanDefinitionPostProcessor(eventBus);
	}

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

	/**
	 * Adds ActionFX beans that are available to the developer independent of the
	 * used bean container.
	 */
	@Override
	public void addActionFXBeans(final ActionFX actionFX) {
		addBeanDefinition(BeanContainerFacade.ACTIONFX_EXTENSION_BEANNAME, ActionFXExtensionsBean.class, true, false,
				actionFX::getActionFXExtensionsBean);

		addBeanDefinition(BeanContainerFacade.CONTROLLER_INSTANCE_POSTPROCESSOR_BEANNAME,
				ControllerInstancePostProcessor.class, true, false, () -> controllerInstancePostProcessor);

		addBeanDefinition(BeanContainerFacade.BEAN_DEFINITION_POSTPROCESSOR_BEANNAME, BeanDefinitionPostProcessor.class,
				true, false, () -> beanDefinitionPostProcessor);

		addBeanDefinition(BeanContainerFacade.EVENT_BUS_BEANNAME, PriorityAwareEventBus.class, true, false,
				() -> eventBus);

		// register the locale as "ObservableValue" (singleton and lazy-initialisation)
		final ObservableValue<Locale> observableLocale = actionFX.getObservableLocale();
		addBeanDefinition(BeanContainerFacade.LOCALE_PROPERTY_BEANNAME, observableLocale.getClass(), true, true,
				() -> observableLocale);

		// register the locale itself (non-singleton - request locale each time it is
		// needed)
		addBeanDefinition(BeanContainerFacade.LOCALE_BEANNAME, Locale.class, false, true, observableLocale::getValue);

		// make ActionFX class itself available as bean
		addBeanDefinition(BeanContainerFacade.ACTIONFX_BEANNAME, ActionFX.class, true, false, () -> actionFX);

		// add the dialog controller to the bean container
		addBeanDefinition(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, DialogController.class, true, true,
				DialogController::new);

		// add the conversion service that is listening to the locale
		addBeanDefinition(BeanContainerFacade.CONVERSION_SERVICE_BEANNAME, ConversionService.class, true, true,
				() -> new ConversionService(observableLocale));
	}

	/**
	 * Performs a post-processing of bean definitions when these are added to the
	 * bean container. Calls are delegated to registered bean extensions of type
	 * {@link BeanExtension}.
	 *
	 * @param beanClass the bean class that is added to the bean container
	 * @param beanId    the bean ID
	 * @param singleton flag that indicates whether the bean is a singleton or not
	 * @param lazyInit  flag that indicates whether the bean is lazily initialized
	 *                  or not
	 */
	protected void postProcessBeanDefinition(final Class<?> beanClass, final String beanId, final boolean singleton,
			final boolean lazyInit) {
		getBeanDefinitionPostProcessor().postProcess(beanClass, beanId, singleton, lazyInit);
	}

	protected ControllerInstancePostProcessor getControllerInstancePostProcessor() {
		return controllerInstancePostProcessor;
	}

	protected BeanDefinitionPostProcessor getBeanDefinitionPostProcessor() {
		return beanDefinitionPostProcessor;
	}
}
