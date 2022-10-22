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

import com.github.actionfx.core.annotation.AFXValidateCustom.AFXValidateCustoms;
import com.github.actionfx.core.view.graph.ControlProperties;

/**
 * Field annotation that can be applied to a {@link javafx.scene.control.Control} for validating user input that has
 * been entered inside the annotated control. The value itself is validated inside a specified method inside the
 * ActionFX controller.
 *
 * @author koster
 *
 */
@Repeatable(AFXValidateCustoms.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXValidateCustom {

    /**
     * The custom validation method inside the ActionFX controller to be invoked for the annotated control's value. It
     * is required that the referenced method needs to return a boolean value, indicating whether validation was
     * successful ({@code true} or failed ({@code false}).
     * <p>
     * Referenced methods can be of the following signature (analog to {@link AFXOnControlValueChange}, but here, the
     * method requires to return a boolean value):
     * <p>
     * For controls with a single-value:
     * <ul>
     * <li><tt>boolean methodName()</tt></li>
     * <li><tt>boolean methodName(TYPE newValue)</tt></li>
     * <li><tt>boolean methodName(TYPE newValue, TYPE oldValue, ObservableValue&lt;TYPE&gt;
     * observableValue)</tt></li>
     * </ul>
     * <p>
     * For controls with multi-values:
     * <ul>
     * <li><tt>boolean methodName()</tt></li>
     * <li><tt>boolean methodName(ObservableList&lt;TYPE&gt; selectedValue)</tt></li>
     * <li><tt>boolean methodName(ObservableList&lt;TYPE&gt; selectedValue, List&lt;TYPE&gt; addedList, List&lt;TYPE&gt; removedList, javafx.collections.ListChangeListener.Change change)</tt></li>
     * </ul>
     * Alternatively to the method signatures above that return a {@code boolean}, it is also possible to return a
     * single {@code ValidationMessage} or a list of {@code ValidationMessage}s. In that case, the attributes
     * {@link #message()} and {@link #messageKey()} are ignored, if specified:
     * <p>
     * <ul>
     * <li>ValidationMessage methodName()</li>
     * <li>List&lt;ValidationMessage&gt; methodName()</li>
     * </ul>
     *
     * The above signatures are supported without requiring the use of the {@link AFXArgHint} annotation. In case you
     * need to change the order of the arguments, you will need to specify hints for defining, which argument is e.g.
     * the "new" value (use @{@link AFXArgHint} with {@link ArgumentHint#NEW_VALUE}) and which argument is the "old"
     * value (use @{@link AFXArgHint} with {@link ArgumentHint#OLD_VALUE})
     *
     *
     * @return the method name inside the ActionFX controller
     */
    public String validationMethod();

    /**
     * Indicates whether the annotated control is mandatory to be filled out. By using this attribute, the annotation
     * {@link AFXValidateRequired} does not to be added to the control.
     *
     * @return {@code true}, if the annotated control needs to have a user value, {@code false} otherwise. Default is
     *         {@code false}.
     */
    public boolean required() default false;

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
     *
     *
     * @return the validation mode. Default is {@link ValidationMode#ONCHANGE}.
     */
    public ValidationMode validationMode() default ValidationMode.ONCHANGE;

    /**
     * Annotation to make {@link AFXValidateCustom} repeatable.
     *
     * @author koster
     *
     */
    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AFXValidateCustoms {

        AFXValidateCustom[] value();
    }
}
