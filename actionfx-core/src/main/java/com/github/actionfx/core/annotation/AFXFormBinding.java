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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javafx.beans.property.ObjectProperty;

/**
 * Annotation that can be applied at field level on an {@link ObjectProperty}, which holds a model object that will be
 * bound to controls inside the view.
 * <p>
 * When changing the value of the {@link ObjectProperty}, then the previously bound object is unbound and the new value
 * is freshly bound to the view's controls.
 *
 * @author koster
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXFormBinding {

    /**
     * Optional prefix for identifying controls in the scene graph that are prefixed by a certain token.
     * <p>
     * For example, when a field name in the model class is called "userName", then a corresponding control of name
     * "selectedUserName", when "selected" is set as {@link #controlPrefix}.
     *
     * @return the control prefix, default is "".
     */
    public String controlPrefix() default "";

    /**
     * Optional suffix for identifying controls in the scene graph that are suffixed by a certain token.
     * <p>
     * For example, when a field name in the model class is called "userName", then a corresponding control of name
     * "userNameOld", when "old" is set as {@link #controlSuffix}.
     *
     * @return the control suffix, default is "".
     */
    public String controlSuffix() default "";

    /**
     * Flag that determines whether name-based field to control mapping shall be disabled or not.
     * <p>
     * Example: In case a field in the model class is named {@code userName}, then the expected control ID inside the
     * view is e.g. {@code userName} or {@code userNameTextField} (that is even supported without specifying a suffix).
     * <p>
     * If {@link #disableNameBasedMapping()} is set to {@code true}, then all mappings need to be explicitly defined via
     * additional {@link AFXFormMapping} annotations on the same field.
     *
     * @return {@code true}, if field control mappings shall be solely taken from additionally applied
     *         {@link AFXFormMapping} annotations, {@code false}, in case control names shall be derived based on field
     *         names in the model class (while using additionally applied {@link AFXFormMapping} annotation on top of
     *         the name-based mapping). Default is {@code false}.
     */
    public boolean disableNameBasedMapping() default false;
}
