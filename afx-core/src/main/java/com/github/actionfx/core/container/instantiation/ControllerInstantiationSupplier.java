/*
 * Copyright (c) 2020 Martin Koster
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

import java.lang.reflect.InvocationTargetException;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;

/**
 * Instantiation supplier for controller instances. This class is responsible
 * for instantiating controller classes. In case that ActionFX is configured to
 * use {@link EnhancementStrategy#SUBCLASSING} as enhancement strategy, a
 * dynamic sub-class is created for the supplied controller class by using the
 * configured {@link ActionFXEnhancer}.
 * 
 * @author koster
 *
 */
public class ControllerInstantiationSupplier<T> extends AbstractInstantiationSupplier<T> {

	private Class<?> controllerClass;

	public ControllerInstantiationSupplier(Class<T> controllerClass) {
		this.controllerClass = prepareControllerClass(controllerClass);
	}

	/**
	 * Prepares the {@code controllerClass} to use. In case the sub-classing
	 * enhancement strategy is configured, a dynamic sub-class is created by using
	 * the configure {@link ActionFXEnhancer}.
	 * 
	 * @param controllerClass the controller class to prepare
	 * @return the prepared controller class, potentially sub-classes depending on
	 *         the ActionFX configuration
	 */
	protected Class<?> prepareControllerClass(Class<T> controllerClass) {
		ActionFX actionFX = ActionFX.getInstance();
		return actionFX.getEnhancementStrategy() == EnhancementStrategy.SUBCLASSING
				? actionFX.getEnhancer().enhanceClass(controllerClass)
				: controllerClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T createInstance() {
		try {
			return (T) controllerClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Can not instaniate class '" + controllerClass.getCanonicalName()
					+ "'! Is there a no-arg constructor present?");
		}
	}
}
