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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
        final CustomMethodValidator validator = new CustomMethodValidator(controller, "validateMethod");

        // WHEN
        final ValidationResult vr = validator.validate(controller.getTextField(),
                ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThat(vr, nullValue());
        assertThat(controller.getTextField().getText(), equalTo("TextField is validated"));
    }

    @Test
    @TestInFxThread
    void testValidate_withParameterValue() {
        // GIVEN
        final ControllerWithMethodAndValueParameter controller = new ControllerWithMethodAndValueParameter();
        controller.getTextField().setText("Hello there");
        final CustomMethodValidator validator = new CustomMethodValidator(controller, "validateMethod");

        // WHEN
        final ValidationResult vr = validator.validate(controller.getTextField(),
                ControlProperties.USER_VALUE_OBSERVABLE);

        // THEN
        assertThat(vr, notNullValue());
        assertThat(vr.getMessages(), hasSize(1));
        assertThat(vr.getMessages().get(0).getStatus(), equalTo(ValidationStatus.ERROR));
        assertThat(vr.getMessages().get(0).getText(), equalTo("Hello there"));
        assertThat(vr.getMessages().get(0).getTarget(), equalTo(controller.getTextField()));

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
