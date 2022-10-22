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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents the result of a performed validation. In case the validation status {@link #getStatus()} is a different
 * status than {@link ValidationStatus#OK}, there will be validation messages present in {@link #getMessages()}.
 *
 * @author koster
 */
public class ValidationResult {

    private List<ValidationMessage> messages;

    public ValidationResult(final List<ValidationMessage> messages) {
        this.messages = messages;
    }

    public ValidationStatus getStatus() {
        final Optional<ValidationMessage> highestMessage = messages.stream().max(ValidationMessage.COMPARATOR);
        return highestMessage.isPresent() ? highestMessage.get().getSeverity() : ValidationStatus.OK;
    }

    /**
     * Retrieve errors represented by validation result
     *
     * @return an unmodifiable collection of errors
     */
    public Collection<ValidationMessage> getErrors() {
        return getMessages(ValidationStatus.ERROR);
    }

    /**
     * Retrieve warnings represented by validation result
     *
     * @return an unmodifiable collection of warnings
     */
    public Collection<ValidationMessage> getWarnings() {
        return getMessages(ValidationStatus.WARNING);
    }

    /**
     * Retrieve infos represented by validation result
     *
     * @return an unmodifiable collection of infos
     */
    public Collection<ValidationMessage> getInfos() {
        return getMessages(ValidationStatus.INFO);
    }

    /**
     * Retrieve all messages represented by validation result
     *
     * @return an unmodifiable collection of messages
     */
    public Collection<ValidationMessage> getMessages() {
        return getMessages(null);
    }

    /**
     * Helper method to get all messages for a given severity. A null severity will return all messages.
     *
     * @return an unmodifiable collection of messages
     */
    private Collection<ValidationMessage> getMessages(final ValidationStatus severity) {
        final List<ValidationMessage> filteredMessages = severity == null ? messages : messages.stream()
                .filter(msg -> msg.getSeverity() == severity)
                .collect(Collectors.toList());

        return Collections.unmodifiableList(filteredMessages);
    }

}
