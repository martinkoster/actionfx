/*
 * Copyright (c) 2022 Martin Koster
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
package com.github.actionfx.core.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.ValidationMode;

/**
 * JUnit test case for {@link ValidationOptions}.
 *
 * @author MartinKoster
 */
class ValidationOptionsTest {

	@AfterEach
	void onTearDown() {
		ActionFX.getInstance().reset();
	}

	@Test
	void testBuild_withGlobalValidationSet() {
		// GIVEN
		ActionFX.builder().scanPackage("dummy.package").validationGlobalMode(ValidationMode.ONCHANGE)
				.validationApplyResultDecoration(false).validationStartTimeoutMs(300).build()
				.scanForActionFXComponents();

		// WHEN
		final ValidationOptions options = ValidationOptions.options().required(true);

		// THEN
		assertThat(options.getValidationMode()).isEqualTo(ValidationMode.ONCHANGE);
		assertThat(options.isRequired()).isTrue();
		assertThat(options.getValidationStartTimeoutMs()).isEqualTo(300);
		assertThat(options.isApplyValidationResultDecorations()).isFalse();
	}

	@Test
	void testBuild_withGlobalValidationSet_validationStartTimeoutOfMinusOneIsNotOverridden() {
		// GIVEN
		ActionFX.builder().scanPackage("dummy.package").validationGlobalMode(ValidationMode.ONCHANGE)
				.validationApplyResultDecoration(false).validationApplyRequiredDecoration(false)
				.validationStartTimeoutMs(300).build().scanForActionFXComponents();

		// WHEN
		final ValidationOptions options = ValidationOptions.options().required(true).validationStartTimeoutMs(-1);

		// THEN
		assertThat(options.getValidationMode()).isEqualTo(ValidationMode.ONCHANGE);
		assertThat(options.isRequired()).isTrue();
		assertThat(options.getValidationStartTimeoutMs()).isEqualTo(300);
		assertThat(options.isApplyValidationResultDecorations()).isFalse();
	}

	@Test
	void testBuild_withGlobalValidationSet_butOverriddenWithExpliciteOption() {
		// GIVEN
		ActionFX.builder().scanPackage("dummy.package").validationGlobalMode(ValidationMode.MANUAL)
				.validationApplyResultDecoration(true).validationApplyRequiredDecoration(true)
				.validationStartTimeoutMs(100).build().scanForActionFXComponents();

		// WHEN
		final ValidationOptions options = ValidationOptions.options().required(true)
				.validationMode(ValidationMode.ONCHANGE).validationStartTimeoutMs(300)
				.applyValidationResultDecorations(false);

		// THEN
		assertThat(options.getValidationMode()).isEqualTo(ValidationMode.ONCHANGE);
		assertThat(options.isRequired()).isTrue();
		assertThat(options.getValidationStartTimeoutMs()).isEqualTo(300);
		assertThat(options.isApplyValidationResultDecorations()).isFalse();
	}

	@Test
	void testBuild_noGlobalValidationSet_noExpliciteValidationSettingsGiven_defaultIsTaken() {

		// GIVEN
		ActionFX.builder().scanPackage("dummy.package").build().scanForActionFXComponents();

		// WHEN
		final ValidationOptions options = ValidationOptions.options().required(true);

		// THEN
		assertThat(options.getValidationMode()).isEqualTo(ValidationMode.MANUAL); // default is manual
		assertThat(options.isRequired()).isTrue();
		assertThat(options.getValidationStartTimeoutMs()).isEqualTo(-1);
		assertThat(options.isApplyValidationResultDecorations()).isTrue();
	}
}
