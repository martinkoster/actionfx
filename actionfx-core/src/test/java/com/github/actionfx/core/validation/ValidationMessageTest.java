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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.validation.ValidationMessage.ValidationMessageComparator;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.TextField;

/**
 * JUnit test for {@link ValidationMessage}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ValidationMessageTest {

    @Test
    void testConstruct_defaultValues() {
        // GIVEN
        final ValidationMessage msg = new ValidationMessage(null, null, null);

		// WHEN and THEN
		assertThat(msg.getStatus()).isEqualTo(ValidationStatus.ERROR);
		assertThat(msg.getText()).isEqualTo("");
		assertThat(msg.getTarget()).isNull();
        assertThat(msg.isApplyValidationResultDecoration()).isTrue();
    }

    @Test
    void testConstruct_equals() {
        // GIVEN
        final TextField tf1 = new TextField();
        final TextField tf2 = new TextField();
        final ValidationMessage msg1 = new ValidationMessage(ValidationStatus.ERROR, "ERROR1", tf1);
        final ValidationMessage msg2 = new ValidationMessage(ValidationStatus.ERROR, "ERROR1", tf2);
        final ValidationMessage msg3 = new ValidationMessage(ValidationStatus.ERROR, "ERROR1", tf1);
        final ValidationMessage msg4 = new ValidationMessage(ValidationStatus.INFO, "ERROR1", tf1);

		// WHEN and THEN
        assertThat(msg1.equals(msg1)).isTrue();
        assertThat(msg1.equals(msg3)).isTrue();

        assertThat(msg1.equals(msg2)).isFalse();
        assertThat(msg1.equals(msg4)).isFalse();
        assertThat(msg2.equals(msg3)).isFalse();
    }

    @Test
    void testConstruct_hashCode() {
        // GIVEN
        final TextField tf1 = new TextField();
        final TextField tf2 = new TextField();
        final ValidationMessage msg1 = new ValidationMessage(ValidationStatus.ERROR, "ERROR1", tf1);
        final ValidationMessage msg2 = new ValidationMessage(ValidationStatus.ERROR, "ERROR1", tf2);
        final ValidationMessage msg3 = new ValidationMessage(ValidationStatus.ERROR, "ERROR1", tf1);

		// WHEN and THEN
		assertThat(msg1.hashCode()).isEqualTo(msg3.hashCode());
        assertNotEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    void testCompare_sortByStatus() {
        // GIVEN
        final TextField tf1 = new TextField();
        final TextField tf2 = new TextField();

        final List<ValidationMessage> list = new ArrayList<>();
        list.add(new ValidationMessage(null, null, null));
        list.add(new ValidationMessage(ValidationStatus.INFO, "INFO", null));
        list.add(new ValidationMessage(ValidationStatus.OK, "OK", null));
        list.add(new ValidationMessage(ValidationStatus.ERROR, "ERROR1", tf1));
        list.add(new ValidationMessage(ValidationStatus.WARNING, "WARNING", null));
        list.add(new ValidationMessage(ValidationStatus.ERROR, "ERROR2", tf2));
        list.add(new ValidationMessage(ValidationStatus.ERROR, "ERROR3", tf1));

        // WHEN
        list.sort(new ValidationMessageComparator());

		// THEN
		assertThat(list.stream().map(ValidationMessage::getText).collect(Collectors.toList())).containsExactly("OK", "INFO", "WARNING", "", "ERROR1", "ERROR2", "ERROR3");
    }

}
