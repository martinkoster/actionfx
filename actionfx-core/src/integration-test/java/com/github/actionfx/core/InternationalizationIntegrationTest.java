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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.test.i18n.I18NApp;
import com.github.actionfx.core.test.i18n.I18NController;
import com.github.actionfx.core.validation.ValidationStatus;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * Tests the internationalization of dialogs with properties files.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class InternationalizationIntegrationTest {

	@AfterEach
	void tearDownActionFX() {
		ActionFX.getInstance().reset();
	}

	@Test
	void testInternationalization_english() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(I18NApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final I18NController controller = actionFX.getBean("i18NController");

		// THEN
		assertThat(controller.textLabel.getText(), equalTo("Hello World"));
	}

	@Test
	void testInternationalization_german() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(I18NApp.class).locale(Locale.GERMANY).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final I18NController controller = actionFX.getBean("i18NController");

		// THEN
		assertThat(controller.textLabel.getText(), equalTo("Hallo Welt"));
	}

	@Test
	@TestInFxThread
	void testValidationInternationalization_english() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(I18NApp.class).locale(Locale.UK).build();
		actionFX.scanForActionFXComponents();
		final I18NController controller = actionFX.getBean("i18NController");
		final FxmlView view = (FxmlView) ControllerWrapper.getViewFrom(controller);

		// WHEN
		controller.textLabel.setText("Contains the numerical value 9");

		// THEN
		assertThat(view.getValidationResult(), notNullValue());
		assertThat(view.getValidationResult().getStatus(), equalTo(ValidationStatus.ERROR));
		assertThat(view.getValidationResult().getErrors(), hasSize(1));
		assertThat(view.getValidationResult().getErrors().get(0).getText(),
				equalTo("Only characters allowed, no numerical values or special characters."));
	}

	@Test
	@TestInFxThread
	void testValidationInternationalization_german() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(I18NApp.class).locale(Locale.GERMANY).build();
		actionFX.scanForActionFXComponents();
		final I18NController controller = actionFX.getBean("i18NController");
		final FxmlView view = (FxmlView) ControllerWrapper.getViewFrom(controller);

		// WHEN
		controller.textLabel.setText("Contains the numerical value 9");

		// THEN
		assertThat(view.getValidationResult(), notNullValue());
		assertThat(view.getValidationResult().getStatus(), equalTo(ValidationStatus.ERROR));
		assertThat(view.getValidationResult().getErrors(), hasSize(1));
		assertThat(view.getValidationResult().getErrors().get(0).getText(),
				equalTo("Nur Buchstaben erlaubt, keine numerischen Werte oder Sonderzeichen."));
	}
}
