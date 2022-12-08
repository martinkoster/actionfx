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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.scene.control.Control;

/**
 * {@link Validator} implementation that validates a date / time value.
 *
 * @author MartinKoster
 */
public class TemporalValidator extends AbstractRequiredValidator {

    private static final Set<Class<?>> TYPES_WITH_TIME_COMPONENT = new HashSet<>();

    private static final List<String> TIME_COMPONENT_CHARACTERS = new ArrayList<>();

    private boolean past;

    private boolean pastOrPresent;

    private boolean future;

    private boolean futureOrPresent;

    private String formatPattern;

    static {
        registerTypesWithTimeComponent();
        registerTimeComponentCharacters();
    }

    public TemporalValidator(final String message, final boolean past, final boolean pastOrPresent,
            final boolean future, final boolean futureOrPresent, final String formatPattern, final boolean required) {
        super(message, required);
        this.past = past;
        this.pastOrPresent = pastOrPresent;
        this.future = future;
        this.futureOrPresent = futureOrPresent;
        this.formatPattern = formatPattern;
    }

    @Override
    protected ValidationResult validateAfterRequiredCheck(final ControlWrapper controlWrapper,
            final ControlProperties controlProperty) {
        if (!controlWrapper.hasValue(controlProperty)) {
            return ValidationResult.ok();
        }
        final Object value = controlWrapper.getValue(controlProperty);
        if (hasTimeComponent(value.getClass()) || value instanceof String && hasTimeComponent(formatPattern)) {
            return validateTypeWithTimeComponent(controlWrapper.getWrapped(), value);
        } else {
            return validateTypeWithoutTimeComponent(controlWrapper.getWrapped(), value);
        }
    }

    private ValidationResult validateTypeWithTimeComponent(final Control control, final Object value) {
        if (!this.canConvert(value, LocalDateTime.class, formatPattern)) {
            return ValidationResult.builder().addErrorMessage(getMessage(), control);
        }
        final LocalDateTime ldt = this.convert(value, LocalDateTime.class, formatPattern);
        final LocalDateTime now = getNow();
        return ValidationResult.builder().addErrorMessageIf(getMessage(), control,
                past && !isInPast(ldt, now) || pastOrPresent && !isInPastOrPresent(ldt, now)
                        || future && !isInFuture(ldt, now) || futureOrPresent && !isInFutureOrPresent(ldt, now));
    }

    private ValidationResult validateTypeWithoutTimeComponent(final Control control, final Object value) {
        if (!this.canConvert(value, LocalDate.class, formatPattern)) {
            return ValidationResult.builder().addErrorMessage(getMessage(), control);
        }
        final LocalDate ld = this.convert(value, LocalDate.class, formatPattern);
        final LocalDate now = getToday();
        return ValidationResult.builder().addErrorMessageIf(getMessage(), control,
                past && !isInPast(ld, now) || pastOrPresent && !isInPastOrPresent(ld, now)
                        || future && !isInFuture(ld, now) || futureOrPresent && !isInFutureOrPresent(ld, now));
    }

    private boolean isInPast(final LocalDateTime ldt, final LocalDateTime now) {
        return ldt.isBefore(now);
    }

    private boolean isInPast(final LocalDate ld, final LocalDate now) {
        return ld.isBefore(now);
    }

    private boolean isInPastOrPresent(final LocalDateTime ldt, final LocalDateTime now) {
        return ldt.isEqual(now) || isInPast(ldt, now);
    }

    private boolean isInPastOrPresent(final LocalDate ld, final LocalDate now) {
        return ld.isEqual(now) || isInPast(ld, now);
    }

    private boolean isInFuture(final LocalDateTime ldt, final LocalDateTime now) {
        return ldt.isAfter(now);
    }

    private boolean isInFuture(final LocalDate ld, final LocalDate now) {
        return ld.isAfter(now);
    }

    private boolean isInFutureOrPresent(final LocalDateTime ldt, final LocalDateTime now) {
        return ldt.isEqual(now) || isInFuture(ldt, now);
    }

    private boolean isInFutureOrPresent(final LocalDate ld, final LocalDate now) {
        return ld.isEqual(now) || isInFuture(ld, now);
    }

    /**
     * Checks whether the given date/time instance indeed has a time component.
     *
     * @param datetimeType
     *            the date/time type
     * @return {@code true},if and only if the supplied type holds a time component
     */
    private boolean hasTimeComponent(final Class<?> datetimeType) {
        return TYPES_WITH_TIME_COMPONENT.contains(datetimeType);
    }

    /**
     * Checks whether the format pattern will yield a time component.
     * <p>
     * The following letters are yielding a time-related component:
     *
     * <pre>
     * a       am-pm-of-day                text              PM
     * h       clock-hour-of-am-pm (1-12)  number            12
     * K       hour-of-am-pm (0-11)        number            0
     * k       clock-hour-of-am-pm (1-24)  number            0
     * H       hour-of-day (0-23)          number            0
     * m       minute-of-hour              number            30
     * s       second-of-minute            number            55
     * S       fraction-of-second          fraction          978
     * A       milli-of-day                number            1234
     * n       nano-of-second              number            987654321
     * N       nano-of-day                 number            1234000000
     * </pre>
     *
     * @param formatPattern
     *            the format pattern to check
     * @return {@code true}, if and only if the format pattern will yield a time component
     */
    private boolean hasTimeComponent(final String formatPattern) {
        return !StringUtils.isBlank(formatPattern)
                && TIME_COMPONENT_CHARACTERS.stream().anyMatch(formatPattern::contains);
    }

    // for unit testing externalized
    protected LocalDate getToday() {
        return LocalDate.now();
    }

    // for unit testing externalized
    protected LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    /**
     * Registers types with time component - these are:
     * <li>{@link java.time.Instant}</li>
     * <li>{@link java.time.OffsetDateTime}</li>
     * <li>{@link java.time.OffsetTime}</li>
     * <li>{@link java.time.ZonedDateTime}</li>
     * <li>{@link java.time.LocalDateTime}</li>
     * <li>{@link java.time.LocalTime}</li>
     * <li>{@link java.util.Date}</li>
     *
     */
    private static void registerTypesWithTimeComponent() {
        TYPES_WITH_TIME_COMPONENT.add(Instant.class);
        TYPES_WITH_TIME_COMPONENT.add(OffsetDateTime.class);
        TYPES_WITH_TIME_COMPONENT.add(OffsetTime.class);
        TYPES_WITH_TIME_COMPONENT.add(ZonedDateTime.class);
        TYPES_WITH_TIME_COMPONENT.add(LocalDateTime.class);
        TYPES_WITH_TIME_COMPONENT.add(LocalTime.class);
        TYPES_WITH_TIME_COMPONENT.add(Date.class);
    }

    /**
     * Checks whether the format pattern will yield a time component.
     * <p>
     * The following letters are yielding a time-related component:
     *
     * <pre>
     * a       am-pm-of-day                text              PM
     * h       clock-hour-of-am-pm (1-12)  number            12
     * K       hour-of-am-pm (0-11)        number            0
     * k       clock-hour-of-am-pm (1-24)  number            0
     * H       hour-of-day (0-23)          number            0
     * m       minute-of-hour              number            30
     * s       second-of-minute            number            55
     * S       fraction-of-second          fraction          978
     * A       milli-of-day                number            1234
     * n       nano-of-second              number            987654321
     * N       nano-of-day                 number            1234000000
     * </pre>
     */
    private static void registerTimeComponentCharacters() {
        TIME_COMPONENT_CHARACTERS.addAll(Arrays.asList("a", "h", "K", "k", "H", "m", "s", "S", "A", "n", "N"));
    }
}
