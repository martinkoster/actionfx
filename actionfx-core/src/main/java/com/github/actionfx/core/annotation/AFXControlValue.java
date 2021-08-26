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

import com.github.actionfx.core.view.graph.ControlProperties;

/**
 * Annotation to be applied to method arguments to retrieve the user value from
 * the specified control.
 * <p>
 * This annotation can be applied to method arguments of methods that are called
 * from the ActionFX framework.
 * <p>
 * Following methods are eligible for arguments to be annotated by
 * {@link AFXControlValue}:
 * <ul>
 * <li>methods annotated by {@link AFXOnAction} (these methods are wired to an
 * "onAction" property of a control like a
 * {@link javafx.scene.control.Button})</li>
 * <li>methods annotated by {@link AFXLoadControlData} (these methods load data
 * for a control inside the scene graph)</li>
 * <li></li>
 * </ul>
 *
 * @author koster
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AFXControlValue {

	/**
	 * ID of the control whose value shall be bound to the annotated method argument
	 *
	 * @return the node ID of a control whose value shall be taken for the method
	 *         argument
	 */
	public String value();

	/**
	 * The control's property where the value shall be retrieved from. Default is
	 * the user value of the control
	 * ({@link ControlProperties#USER_VALUE_OBSERVABLE}).
	 *
	 * @return the property where the value shall be retrieved from
	 */
	public ControlProperties sourceProperty() default ControlProperties.USER_VALUE_OBSERVABLE;

}
