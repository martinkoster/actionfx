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
package com.github.actionfx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.actionfx.core.annotation.AFXValidateTemporal.AFXValidateTemporals;
import com.github.actionfx.core.view.graph.ControlProperties;

/**
 * Field annotation that can be applied to a {@link javafx.scene.control.Control} for validating user input that has
 * been entered inside the annotated control and is expected to represent a temporal value (e.g. {@link java.util.Date}
 * or {@link java.time.LocalDate}) fulfilling certain criteria.
 *
 * @author koster
 *
 */
@Repeatable(AFXValidateTemporals.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXValidateTemporal {

    /**
     * Set this attribute to {@code true}, if the value of the annotated control needs to hold a temporal value in the
     * past.
     *
     * @return {@code true}, if the value of the control must be in the past. Default is {@code false}.
     */
    public boolean past() default false;

    /**
     * Set this attribute to {@code true}, if the value of the annotated control needs to hold a temporal value in the
     * past or present.
     *
     * @return {@code true}, if the value of the control must be in the past or present. Default is {@code false}.
     */
    public boolean pastOrPresent() default false;

    /**
     * Set this attribute to {@code true}, if the value of the annotated control needs to hold a temporal value in the
     * future.
     *
     * @return {@code true}, if the value of the control must be in the future. Default is {@code false}.
     */
    public boolean future() default false;

    /**
     * Set this attribute to {@code true}, if the value of the annotated control needs to hold a temporal value in the
     * future or present.
     *
     * @return {@code true}, if the value of the control must be in the future or present. Default is {@code false}.
     */
    public boolean futureOrPresent() default false;

    /**
     * Indicates whether the annotated control is mandatory to be filled out. By using this attribute, the annotation
     * {@link AFXValidateRequired} does not to be added to the control.
     *
     * @return {@code true}, if the annotated control needs to have a user value, {@code false} otherwise. Default is
     *         {@code false}.
     */
    public boolean required() default false;

    /**
     * An optional format pattern that is used for type conversion.
     * <p>
     * This parameter can be used for validating numerical values taken from text fields.
     *
     * @return the format pattern.
     */
    public String formatPattern() default "";

    /**
     * Validation error message to be displayed, in case the control failed validation.
     *
     * @return the validation error message
     */
    public String message() default "";

    /**
     * Properties key to a validation error message to be displayed, in case the control failed validation. A value in
     * this attribute has a higher priority than the value specified in {@link #message()}.
     *
     * @return the properties key to the validation error message
     */
    public String messageKey() default "";

    /**
     * Defines, which control value shall be validated. Default is {@link ControlProperties#USER_VALUE_OBSERVABLE}.
     *
     * @return the target property to validate. Default is {@link ControlProperties#USER_VALUE_OBSERVABLE}.
     */
    public ControlProperties validationTargeProperty() default ControlProperties.USER_VALUE_OBSERVABLE;

    /**
     * Defines the timeout in milliseconds that has to pass after changing a control value, before the actual validation
     * is applied to.
     * <p>
     * This value is only used, if validation mode {@link ValidationMode#ONCHANGE} is supplied in attribute
     * {@link #validationMode()}.
     *
     * @return the timeout value in milliseconds. Default is 100.
     */
    public int validationStartTimeoutMs() default 100;

    /**
     * Defines, when the actual validation is performed.
     * <p>
     * Possible options are:
     * <p>
     * <ul>
     * <li>{@link ValidationMode#ONCHANGE}: Validation is applied, when a change occurs in a control that holds any
     * validation annotation.</li>
     * <li>{@link ValidationMode#MANUAL}: Validation occurs only, when an explicit call to
     * {@link com.github.actionfx.core.ActionFX#validate(Object)} is performed.
     * </ul>
     * *
     *
     * @return the validation mode. Default is {@link ValidationMode#ONCHANGE}.
     */
    public ValidationMode validationMode() default ValidationMode.ONCHANGE;

    /**
     * Annotation to make {@link AFXValidateTemporal} repeatable.
     *
     * @author koster
     *
     */
    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AFXValidateTemporals {

        AFXValidateTemporal[] value();
    }
}
