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
 * Repeatable annotation that can be applied at field level on a
 * {@link ObjectProperty} additionally to a {@link AFXFormBinding} annotation to
 * explicitly map a field in a model class to a control name.
 * <p>
 * Please note: Using this annotation on a field without a
 * {@link AFXFormBinding} annotation will have no effect.
 *
 * @author koster
 *
 */
@Repeatable(AFXFormMappings.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXFormMapping {

	/**
	 * The name of the field inside the model class. The value can be also a nested
	 * path using the "." notation for Java beans.
	 *
	 * @return the name of the field inside the model class.
	 */
	public String propertyName();

	/**
	 * The ID of the control that shall be mapped to the field name in the model
	 * class.
	 *
	 * @return the control ID
	 */
	public String controlId();

	/**
	 * The control's target property that shall be used as binding target. Default
	 * is the user value of the control
	 * ({@link ControlProperties#USER_VALUE_OBSERVABLE}).
	 *
	 * @return the target property description of the control's binding target
	 */
	public ControlProperties targetProperty() default ControlProperties.USER_VALUE_OBSERVABLE;

	/**
	 * An optional format pattern that is used to format.
	 * <p>
	 * This parameter can be used e.g to convert floating point numbers to/from
	 * string with a specific pattern or to convert Java {@code java.time} datetime
	 * types to/from string.
	 *
	 * @return the format pattern.
	 */
	public String formatPattern() default "";

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
