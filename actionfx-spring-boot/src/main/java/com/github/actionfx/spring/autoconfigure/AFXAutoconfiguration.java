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
package com.github.actionfx.spring.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.actionfx.core.container.instantiation.ControllerInstancePostProcessor;
import com.github.actionfx.spring.container.ControllerBeanPostProcessor;

/**
 * Spring auto configuration for ActionFX.
 *
 * @author koster
 *
 */
@Configuration
public class AFXAutoconfiguration {

	/**
	 * Wraps ActionFX' {@link ControllerInstancePostProcessor} into the
	 * Spring-specific bean post processor. The Spring bean post processor merely
	 * delegates the processing call to ActionFX' post processor.
	 *
	 * @param controllerInstancePostProcessor ActionFX' controller post processor
	 *                                        for enhancing ActionFX controller
	 * @return a Spring bean post processor
	 */
	@Bean
	public ControllerBeanPostProcessor controllerBeanPostProcessor(
			final ControllerInstancePostProcessor controllerInstancePostProcessor) {
		return new ControllerBeanPostProcessor(controllerInstancePostProcessor);
	}

}
