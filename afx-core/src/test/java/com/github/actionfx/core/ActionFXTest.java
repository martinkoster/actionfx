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
package com.github.actionfx.core;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link ActionFX}.
 * 
 * @author koster
 *
 */
class ActionFXTest {

	@BeforeEach
	void onSetup() {
		// set view manager instance to 'null' in order to force the creation of a
		// ViewManager instance for each test
		ActionFX.instance = null;
	}

	@Test
	void testBuilder() {
		
		// WHEN
		ActionFX actionFX = ActionFX.builder().beanContainer(beanContainer)
	}

	@Test
	void testBuilder_notYetBuilt() {

		// WHEN and THEN (
		assertThrows(IllegalStateException.class, () -> ActionFX.getInstance());
	}

	@Test
	void testBuilder_alreadyBuilt() {

		// GIVEN (instance is built)
		ActionFX actionFX = ActionFX.builder().build();

		// WHEN and THEN (
		assertThat(actionFX, notNullValue());
		assertThrows(IllegalStateException.class, () -> ActionFX.builder().build());

	}

}
