/*
 * Copyright (c) 2021,2022 Martin Koster
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
package com.github.actionfx.core.extension.controller;

import static com.github.actionfx.core.extension.controller.ValidationAsserter.assertRegisterValidator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXValidateTemporal;
import com.github.actionfx.core.annotation.ValidationMode;
import com.github.actionfx.core.validation.TemporalValidator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link ValidateTemporalControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ValidateTemporalControllerExtensionTest {

	@BeforeEach
	void onSetup() {
		ActionFX.builder().build().scanForActionFXComponents();
	}

	@AfterEach
	void tearDown() {
		ActionFX.getInstance().reset();
	}

	@Test
	void testAccept() {
		// GIVEN
		final Controller controller = new Controller();
		final ValidateTemporalControllerExtension extension = new ValidateTemporalControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertRegisterValidator(controller._view, controller.textField, TemporalValidator.class,
				ValidationMode.ONCHANGE, true, 450, true);
	}

	public class Controller {

		public View _view;

		public Controller() {
			_view = Mockito.spy(View.class);
		}

		@AFXValidateTemporal(required = true, validationStartTimeoutMs = 450, validationMode = ValidationMode.ONCHANGE)
		public TextField textField = new TextField();
	}

}
