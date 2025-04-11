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

import java.util.Comparator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.scene.control.Control;

/**
 * Internal implementation of simple validation message
 *
 * @author koster
 */
public class ValidationMessage implements Comparable<ValidationMessage> {

    public static final Comparator<ValidationMessage> COMPARATOR = new ValidationMessageComparator();

    private final String text;

    private final ValidationStatus status;

    private final Control target;

    private boolean applyValidationResultDecoration;

    public ValidationMessage(final ValidationStatus status, final String text, final Control target) {
        this(status, text, target, true);
    }

    public ValidationMessage(final ValidationStatus status, final String text, final Control target,
            final boolean applyValidationResultDecoration) {
        this.text = text == null ? "" : text;
        this.status = status == null ? ValidationStatus.ERROR : status;
        this.target = target;
        this.applyValidationResultDecoration = applyValidationResultDecoration;
    }

    public Control getTarget() {
        return target;
    }

    public String getText() {
        return text;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public boolean isApplyValidationResultDecoration() {
        return applyValidationResultDecoration;
    }

    // Package protected as it can be only overridden by class ValidationResult
    void setApplyValidationResultDecoration(final boolean applyValidationResultDecoration) {
        this.applyValidationResultDecoration = applyValidationResultDecoration;
    }

    @Override
    public String toString() {
        return "ValidationMessage [text=" + text + ", status=" + status + ", target=" + target
                + ", applyValidationDecoration=" + applyValidationResultDecoration + "]";
    }

    @Override
    public int compareTo(final ValidationMessage msg) {
        return getStatus().compareTo(msg.getStatus());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (status == null ? 0 : status.hashCode());
        result = prime * result + (target == null ? 0 : target.hashCode());
        result = prime * result + (text == null ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValidationMessage other = (ValidationMessage) obj;
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }

    /**
     * Comparator for sorting validation messages according to their severity.
     *
     * @author koster
     *
     */
    @SuppressFBWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE", justification = "Making the Comparator serializable does not make sense as compared items ValidationMessage are not serializable either (and not planned to be serializable).")
    public static class ValidationMessageComparator implements Comparator<ValidationMessage> {
        @Override
        public int compare(final ValidationMessage vm1, final ValidationMessage vm2) {
            if (vm1 == vm2) {
                return 0;
            }
            if (vm1 == null) {
                return 1;
            }
            if (vm2 == null) {
                return -1;
            }
            return vm1.compareTo(vm2);
        }
    }

}
