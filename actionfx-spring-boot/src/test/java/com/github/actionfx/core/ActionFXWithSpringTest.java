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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.actionfx.core.container.DefaultActionFXBeanContainer;
import com.github.actionfx.spring.container.SpringBeanContainer;

class ActionFXWithSpringTest {

	@BeforeEach
	void onSetup() {
		// reset instance to 'null' in order to force the creation of a
		// new ActionFX instance for each test
		ActionFX.instance = null;
	}

	@Test
	void testBuilder_enableBeanContainerAutodetection_springContainerIsFound() {
		// WHEN
		final ActionFX actionFX = ActionFX.builder().enableBeanContainerAutodetection(true).build();

		// THEN
		assertThat(actionFX.getBeanContainer(), instanceOf(SpringBeanContainer.class));
	}

	@Test
	void testBuilder_disableBeanContainerAutodetection_defaultContainerIsUsed() {
		// WHEN
		final ActionFX actionFX = ActionFX.builder().enableBeanContainerAutodetection(false).build();

		// THEN
		assertThat(actionFX.getBeanContainer(), instanceOf(DefaultActionFXBeanContainer.class));
	}
}
