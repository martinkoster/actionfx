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
package com.github.actionfx.core.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link MessageUtils}.
 *
 * @author koster
 *
 */
class MessageUtilsTest {

	@Test
	void testGetMessage() {
		// GIVEN
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.TestResources", Locale.US);

		// WHEN
		assertThat(MessageUtils.getMessage(resourceBundle, "label.text", "Default Text"), equalTo("Hello World"));
	}

	@Test
	void testGetMessage_keyDoesNotExist_useDefaultMessage() {
		// GIVEN
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.TestResources", Locale.US);

		// WHEN and THEN
		assertThat(MessageUtils.getMessage(resourceBundle, "fantasy.key", "Default Text"), equalTo("Default Text"));
	}

	@Test
	void testGetMessage_resourceBundleIsNull() {
		// WHEN and THEN
		assertThat(MessageUtils.getMessage(null, "fantasy.key", "Default Text"), equalTo("Default Text"));
	}

	@Test
	void testGetMessage_keyIsNull() {
		// GIVEN
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.TestResources", Locale.US);

		// WHEN and THEN
		assertThat(MessageUtils.getMessage(resourceBundle, null, "Default Text"), equalTo("Default Text"));
	}

}
