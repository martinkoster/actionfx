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
package com.github.actionfx.spring.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.container.BeanContainerFacade;

/**
 * Implementation of {@link ApplicationContextInitializer} that delegates the
 * bean initialization to {@link SpringBeanContainer}, which is in turn used
 * again by ActionFX for retrieving the beans from Spring's
 * {@link ApplicationContext}.
 * <p>
 * Please note that you need to build and setup the {@code ActionFX} instance
 * before firing up the Spring container.
 *
 * @author koster
 *
 */
public class AFXApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

	private static final Logger LOG = LoggerFactory.getLogger(AFXApplicationContextInitializer.class);

	@Override
	public void initialize(final GenericApplicationContext applicationContext) {
		final ActionFX actionFX = ActionFX.getInstance();
		final BeanContainerFacade beanContainer = actionFX.getBeanContainer();
		if (beanContainer instanceof SpringBeanContainer) {
			LOG.info("ActionFX will use the Spring Bean Container for bean management.");
			final SpringBeanContainer springBeanContainer = (SpringBeanContainer) beanContainer;
			springBeanContainer.onSpringContextAvailable(applicationContext, applicationContext);
			ActionFX.getInstance().scanForActionFXComponents();
		} else {
			LOG.info("ActionFX will use its own bean container, independent of the Spring container. ");
		}
	}

}
