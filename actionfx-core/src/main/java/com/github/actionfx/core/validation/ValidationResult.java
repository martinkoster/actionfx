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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.scene.control.Control;

/**
 * Represents the result of a performed validation. In case the validation
 * status {@link #getStatus()} is a different status than
 * {@link ValidationStatus#OK}, there will be validation messages present in
 * {@link #getMessages()}.
 *
 * @author koster
 */
public class ValidationResult {

	private final List<ValidationMessage> messages;

	/**
	 * Constructor accepting a list of validation messages
	 *
	 * @param messages the list of validation messages
	 */
	public ValidationResult(final List<ValidationMessage> messages) {
		this.messages = new ArrayList<>();
		if (messages != null) {
			this.messages.addAll(messages);
		}
	}

	/**
	 * Internal constructor for builder.
	 */
	protected ValidationResult() {
		messages = new ArrayList<>();
	}

	/**
	 * Overrides and sets the flag
	 * {@link ValidationMessage#isApplyValidationDecoration()} in all messages that
	 * are part of this validation result.
	 * <p>
	 * In general, {@link com.github.actionfx.core.validation.Validator} instance
	 * can decide whether to display a validation decoration or not. However, the
	 * user of the validation method
	 * {@link com.github.actionfx.core.view.View#validate(boolean)} can decide to
	 * override this decision and not to display any decoration.
	 * <p>
	 * Important: The supplied boolean {@code applyValidationDecoration} is ANDed
	 * with the current flag, so that decoration for messages that are already
	 * disabled can not be enabled by this override flag.
	 *
	 * @param applyValidationResultDecoration flag that will be set in all
	 *                                        validation messages inside this
	 *                                        validation result
	 */
	public void overrideApplyValidationResultDecoration(final boolean applyValidationResultDecoration) {
		messages.forEach(msg -> msg.setApplyValidationResultDecoration(
				applyValidationResultDecoration && msg.isApplyValidationResultDecoration()));
	}

	/**
	 * Builder method for contructing a validation result via a builder pattern.
	 *
	 * @return the validation result instance to construct
	 */
	public static ValidationResult builder() {
		return new ValidationResult();
	}

	/**
	 * Convenient method for return a {@link ValidationStatus} with status
	 * {@link ValidationStatus#OK}.
	 *
	 * @return a validation result with status OK
	 */
	public static ValidationResult ok() {
		return builder();
	}

	/**
	 * Adds a new validation message with the given {@code status} and {@code text}
	 * associated to the supplied {@code control}.
	 *
	 * @param status  the validation status
	 * @param text    the validation message
	 * @param control the affected control
	 * @return this instance for building
	 */
	public ValidationResult addMessage(final ValidationStatus status, final String text, final Control control) {
		return addMessage(status, text, control, true);
	}

	/**
	 * Adds a new validation message with the given {@code status} and {@code text}
	 * associated to the supplied {@code control}.
	 *
	 * @param status                    the validation status
	 * @param text                      the validation message
	 * @param control                   the affected control
	 * @param applyValidationDecoration shall validation decorations be applied for
	 *                                  this validation message?
	 * @return this instance for building
	 */
	public ValidationResult addMessage(final ValidationStatus status, final String text, final Control control,
			final boolean applyValidationDecoration) {
		messages.add(new ValidationMessage(status, text, control, applyValidationDecoration));
		return this;
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#OK} and
	 * {@code text} associated to the supplied {@code control}.
	 *
	 * @param text    the validation message
	 * @param control the affected control
	 * @return this instance for building
	 */
	public ValidationResult addOKMessage(final String text, final Control control) {
		return addOKMessage(text, control, true);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#OK} and
	 * {@code text} associated to the supplied {@code control}.
	 *
	 * @param text                      the validation message
	 * @param control                   the affected control
	 * @param applyValidationDecoration shall validation decorations be applied for
	 *                                  this validation message?
	 * @return this instance for building
	 */
	public ValidationResult addOKMessage(final String text, final Control control,
			final boolean applyValidationDecoration) {
		return addMessage(ValidationStatus.OK, text, control, applyValidationDecoration);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#INFO} and
	 * {@code text} associated to the supplied {@code control}.
	 *
	 * @param text    the validation message
	 * @param control the affected control
	 * @return this instance for building
	 */
	public ValidationResult addInfoMessage(final String text, final Control control) {
		return addInfoMessage(text, control, true);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#INFO} and
	 * {@code text} associated to the supplied {@code control}.
	 *
	 * @param text                      the validation message
	 * @param control                   the affected control
	 * @param applyValidationDecoration shall validation decorations be applied for
	 *                                  this validation message?
	 * @return this instance for building
	 */
	public ValidationResult addInfoMessage(final String text, final Control control,
			final boolean applyValidationDecoration) {
		return addMessage(ValidationStatus.INFO, text, control, applyValidationDecoration);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#WARNING} and
	 * {@code text} associated to the supplied {@code control}.
	 *
	 * @param text    the validation message
	 * @param control the affected control
	 * @return this instance for building
	 */
	public ValidationResult addWarningMessage(final String text, final Control control) {
		return addWarningMessage(text, control, true);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#WARNING} and
	 * {@code text} associated to the supplied {@code control}.
	 *
	 * @param text                      the validation message
	 * @param control                   the affected control
	 * @param applyValidationDecoration shall validation decorations be applied for
	 *                                  this validation message?
	 * @return this instance for building
	 */
	public ValidationResult addWarningMessage(final String text, final Control control,
			final boolean applyValidationDecoration) {
		return addMessage(ValidationStatus.WARNING, text, control, applyValidationDecoration);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#ERROR} and
	 * {@code text} associated to the supplied {@code control}.
	 *
	 * @param text    the validation message
	 * @param control the affected control
	 * @return this instance for building
	 */
	public ValidationResult addErrorMessage(final String text, final Control control) {
		return addErrorMessage(text, control, true);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#ERROR} and
	 * {@code text} associated to the supplied {@code control}.
	 *
	 * @param text                      the validation message
	 * @param control                   the affected control
	 * @param applyValidationDecoration shall validation decorations be applied for
	 *                                  this validation message?
	 * @return this instance for building
	 */
	public ValidationResult addErrorMessage(final String text, final Control control,
			final boolean applyValidationDecoration) {
		return addMessage(ValidationStatus.ERROR, text, control, applyValidationDecoration);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#ERROR} and
	 * {@code text} associated to the supplied {@code control}, if and only if
	 * {@code condition} is {@code true}.
	 *
	 * @param text      the validation message
	 * @param control   the affected control
	 * @param condition the condition (must be {@code true} for having a validation
	 *                  result of status ERROR)
	 * @return this instance for building
	 */
	public ValidationResult addErrorMessageIf(final String text, final Control control, final boolean condition) {
		return addErrorMessageIf(text, control, condition, true);
	}

	/**
	 * Adds a new validation message of status {@link ValidationStatus#ERROR} and
	 * {@code text} associated to the supplied {@code control}, if and only if
	 * {@code condition} is {@code true}.
	 *
	 * @param text                      the validation message
	 * @param control                   the affected control
	 * @param condition                 the condition (must be {@code true} for
	 *                                  having a validation result of status ERROR)
	 * @param applyValidationDecoration shall validation decorations be applied for
	 *                                  this validation message?
	 * @return this instance for building
	 */
	public ValidationResult addErrorMessageIf(final String text, final Control control, final boolean condition,
			final boolean applyValidationDecoration) {
		if (condition) {
			return addErrorMessage(text, control, applyValidationDecoration);
		}
		return this;
	}

	/**
	 * Factory method to create a combined {@link ValidationResult} from a list of
	 * {@link ValidationResult}s.
	 *
	 * @param results the validation results to retrieve the validation messages
	 *                from
	 * @return the combined validation result instance
	 */
	public static ValidationResult from(final Collection<ValidationResult> results) {
		return new ValidationResult(results.stream().map(ValidationResult::getMessages).flatMap(Collection::stream)
				.collect(Collectors.toList()));
	}

	/**
	 * Gets the status of this validation result from considering the underlying
	 * messages
	 *
	 * @return the overall status of this validation result
	 */
	public ValidationStatus getStatus() {
		final Optional<ValidationMessage> highestMessage = messages.stream().max(ValidationMessage.COMPARATOR);
		return highestMessage.isPresent() ? highestMessage.get().getStatus() : ValidationStatus.OK;
	}

	/**
	 * Retrieve errors represented by validation result
	 *
	 * @return an unmodifiable collection of errors
	 */
	public List<ValidationMessage> getErrors() {
		return getMessages(ValidationStatus.ERROR);
	}

	/**
	 * Retrieve warnings represented by validation result
	 *
	 * @return an unmodifiable collection of warnings
	 */
	public List<ValidationMessage> getWarnings() {
		return getMessages(ValidationStatus.WARNING);
	}

	/**
	 * Retrieve infos represented by validation result
	 *
	 * @return an unmodifiable collection of infos
	 */
	public List<ValidationMessage> getInfos() {
		return getMessages(ValidationStatus.INFO);
	}

	/**
	 * Retrieve all messages represented by validation result
	 *
	 * @return an unmodifiable collection of messages
	 */
	public List<ValidationMessage> getMessages() {
		return getMessages(null);
	}

	/**
	 * Helper method to get all messages for a given severity. A null severity will
	 * return all messages.
	 *
	 * @return an unmodifiable collection of messages
	 */
	private List<ValidationMessage> getMessages(final ValidationStatus severity) {
		final List<ValidationMessage> filteredMessages = severity == null ? messages
				: messages.stream().filter(msg -> msg.getStatus() == severity).collect(Collectors.toList());
		return Collections.unmodifiableList(filteredMessages);
	}

}
