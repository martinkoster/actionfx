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
package com.github.actionfx.core;

import java.util.Locale;

import com.github.actionfx.core.container.DefaultBeanContainer;

import javafx.beans.property.SimpleObjectProperty;

/**
 * Mock for tests with ActionFX.
 *
 * @author koster
 *
 */
public class ActionFXMock extends ActionFX {

	/**
	 * Constructor for direct usage. No builder required for mocking.
	 */
	public ActionFXMock() {
		instance = this;
		beanContainer = new DefaultBeanContainer();
		observableLocale = new SimpleObjectProperty<>(Locale.US);
		setStateConfigured();
	}

	/**
	 * Overrides a bean in the underlying bean container.
	 *
	 * @param beanName the bean name to override
	 * @param bean     the bean
	 */
	public void addBean(final String beanName, final Object bean) {
		getBeanContainer().addBeanDefinition(beanName, bean.getClass(), true, true, () -> bean);
	}

	/**
	 * Sets the state directly to configured.
	 */
	public void setStateConfigured() {
		actionFXState = ActionFXState.CONFIGURED;
	}
}
