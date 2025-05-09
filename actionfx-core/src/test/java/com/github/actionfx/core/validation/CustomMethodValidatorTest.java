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

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.TextField;

/**
 * JUnit test case {@link CustomMethodValidator}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class CustomMethodValidatorTest {

    @Test
    @TestInFxThread
    void testValidate_withVoidMethod() {
        // GIVEN
        final ControllerWithVoidMethod controller = new ControllerWithVoidMethod();
        controller.getTextField().setText("Hello there");
        final Method method = ReflectionUtils.findMethod(ControllerWithVoidMethod.class, "validateMethod");
        final CustomMethodValidator validator = new CustomMethodValidator(controller, method);

        // WHEN
        final ValidationResult vr = validator.validate(controller.getTextField(),
                ControlProperties.USER_VALUE_OBSERVABLE);

		// THEN
		assertThat(vr).isNull();
		assertThat(controller.getTextField().getText()).isEqualTo("TextField is validated");
    }

    @Test
    @TestInFxThread
    void testValidate_withParameterValue() {
        // GIVEN
        final ControllerWithMethodAndValueParameter controller = new ControllerWithMethodAndValueParameter();
        controller.getTextField().setText("Hello there");
        final Method method = ReflectionUtils.findMethod(ControllerWithMethodAndValueParameter.class, "validateMethod",
                String.class);
        final CustomMethodValidator validator = new CustomMethodValidator(controller, method);

        // WHEN
        final ValidationResult vr = validator.validate(controller.getTextField(),
                ControlProperties.USER_VALUE_OBSERVABLE);

		// THEN
		assertThat(vr).isNotNull();
		assertThat(vr.getMessages()).hasSize(1);
		assertThat(vr.getMessages().get(0).getStatus()).isEqualTo(ValidationStatus.ERROR);
		assertThat(vr.getMessages().get(0).getText()).isEqualTo("Hello there");
		assertThat(vr.getMessages().get(0).getTarget()).isEqualTo(controller.getTextField());

    }

    public class ControllerWithMethodAndValueParameter {

        private TextField textField;

        public ControllerWithMethodAndValueParameter() {
            textField = new TextField();
        }

        public void validateMethod() {
            // method should not be selected as there is a better fitting method
        }

        public ValidationResult validateMethod(final String text) {
            return ValidationResult.builder().addErrorMessage(text, textField);
        }

        TextField getTextField() {
            return textField;
        }
    }

    public class ControllerWithVoidMethod {

        private TextField textField;

        public ControllerWithVoidMethod() {
            textField = new TextField();
        }

        public void validateMethod() {
            textField.setText("TextField is validated");
        }

        TextField getTextField() {
            return textField;
        }
    }
}
