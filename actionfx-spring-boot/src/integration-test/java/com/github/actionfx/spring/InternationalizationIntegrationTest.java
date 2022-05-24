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
package com.github.actionfx.spring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.spring.test.i18n.I18NApp;
import com.github.actionfx.spring.test.i18n.I18NController;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * Tests the internationalization of dialogs with properties files.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ContextConfiguration(classes = I18NApp.class)
class InternationalizationIntegrationTest {

	@BeforeAll
	static void initializeActionFX() {
		ActionFX.builder().configurationClass(I18NApp.class).locale(Locale.GERMANY).build();
	}

	@AfterAll
	static void resetActionFX() {
		ActionFX.getInstance().reset();
	}

	@Test
	void testInternationalization_german() {
		// WHEN
		final I18NController controller = ActionFX.getInstance().getBean("i18NController");

		// THEN
		assertThat(controller.textLabel.getText(), equalTo("Hallo Welt"));
	}

}
