/*
 * Copyright (c) 2021 Martin Koster
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

import com.github.actionfx.core.annotation.AFXFormMapping.AFXFormMappings;
import com.github.actionfx.core.view.graph.ControlProperties;

import javafx.beans.property.ObjectProperty;

/**
 * Repeatable annotation that can be applied at field level on a {@link ObjectProperty} additionally to a
 * {@link AFXFormBinding} annotation to explicitly map a field in a model class to a control name.
 * <p>
 * Please note: Using this annotation on a field without a {@link AFXFormBinding} annotation will have no effect.
 *
 * @author koster
 *
 */
@Repeatable(AFXFormMappings.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXFormMapping {

    /**
     * The name of the field inside the model class. The value can be also a nested path using the "." notation for Java
     * beans.
     *
     * @return the name of the field inside the model class.
     */
    public String propertyName();

    /**
     * The ID of the control that shall be mapped to the field name in the model class.
     *
     * @return the control ID
     */
    public String controlId();

    /**
     * The control's target property that shall be used as binding target. Default is the user value of the control
     * ({@link ControlProperties#USER_VALUE_OBSERVABLE}).
     *
     * @return the target property description of the control's binding target
     */
    public ControlProperties targetProperty() default ControlProperties.USER_VALUE_OBSERVABLE;

    /**
     * An optional format pattern that is used to format.
     * <p>
     * This parameter can be used e.g to convert floating point numbers to/from string with a specific pattern or to
     * convert Java {@code java.time} datetime types to/from string.
     *
     * @return the format pattern.
     */
    public String formatPattern() default "";

    /**
     * The custom validation method inside the ActionFX controller to be invoked for the annotated control's value. It
     * is required that the referenced method needs to return a boolean value, indicating whether validation was
     * successful ({@code true} or failed ({@code false}).
     * <p>
     * Referenced methods can be of the following signatures:
     * <p>
     * For controls with a single-value:
     * <ul>
     * <li><tt>ValidationResult methodName()</tt></li>
     * <li><tt>ValidationResult methodName(TYPE newValue)</tt></li>
     * </ul>
     * <p>
     * For controls with multi-values:
     * <ul>
     * <li><tt>ValidationResult methodName()</tt></li>
     * <li><tt>ValidationResult methodName(ObservableList&lt;TYPE&gt; selectedValue)</tt></li>
     * </ul>
     * Alternatively to the method signatures above that return a
     * {@link com.github.actionfx.core.validation.ValidationResult}, it is also possible invoke void-methods. In that
     * case, developer need to handle the display of validation messages themselves in the validation method.
     * <p>
     * <ul>
     * <li>void methodName()</li>
     * </ul>
     *
     * @return the method name inside the ActionFX controller
     */
    public String validationMethod() default "";

    /**
     * Indicates whether the referenced control is mandatory to be filled out.
     *
     * @return {@code true}, if the annotated control needs to have a user value, {@code false} otherwise. Default is
     *         {@code false}.
     */
    public boolean required() default false;

    /**
     * The minimum value that the control needs to have to pass validation. If there is no value specified for this
     * attribute, no minimum validation is applied.
     *
     * @return the minimum value. Default is {@link Long#MIN_VALUE} (which means, "no minimum specified").
     */
    public long min() default Long.MIN_VALUE;

    /**
     * The maximum value that the control needs to have to pass validation. If there is no value specified for this
     * attribute, no maximum validation is applied.
     *
     * @return the maximum value. Default is {@link Long#MAX_VALUE} (which means, "no maximum specified").
     */
    public long max() default Long.MAX_VALUE;

    /**
     * The regular expression that a contol's value needs to match for passing validation.
     *
     * @return the regular expression that needs to match
     */
    public String regExp() default "";

    /**
     * Set this attribute to {@code true}, if the value of the referenced control needs to hold a temporal value in the
     * past.
     *
     * @return {@code true}, if the value of the control must be in the past. Default is {@code false}.
     */
    public boolean past() default false;

    /**
     * Set this attribute to {@code true}, if the value of the referenced control needs to hold a temporal value in the
     * past or present.
     *
     * @return {@code true}, if the value of the control must be in the past or present. Default is {@code false}.
     */
    public boolean pastOrPresent() default false;

    /**
     * Set this attribute to {@code true}, if the value of the referenced control needs to hold a temporal value in the
     * future.
     *
     * @return {@code true}, if the value of the control must be in the future. Default is {@code false}.
     */
    public boolean future() default false;

    /**
     * Set this attribute to {@code true}, if the value of the referenced control needs to hold a temporal value in the
     * future or present.
     *
     * @return {@code true}, if the value of the control must be in the future or present. Default is {@code false}.
     */
    public boolean futureOrPresent() default false;

    /**
     * Validation error message to be displayed, in case the control failed validation.
     *
     * @return the validation error message
     */
    public String validationMessage() default "";

    /**
     * Properties key to a validation error message to be displayed, in case the control failed validation. A value in
     * this attribute has a higher priority than the value specified in {@link #validationMessage()}.
     *
     * @return the properties key to the validation error message
     */
    public String validationMessageKey() default "";

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
     * Annotation to make {@link AFXFormMapping} repeatable.
     *
     * @author MartinKoster
     *
     */
    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AFXFormMappings {

        AFXFormMapping[] value();
    }
}
