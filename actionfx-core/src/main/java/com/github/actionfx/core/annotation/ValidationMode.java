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

/**
 * Defines the validation mode of controls, used in various validation-related annotations.
 * <p>
 * Possible options are:
 * <p>
 * <ul>
 * <li>ONCHANGE: Validation is applied, when a change occurs in a control that holds any validation annotation.</li>
 * <li>MANUAL: Validation occurs only, when an explicit call to
 * {@link com.github.actionfx.core.ActionFX#validate(Object)} is performed.
 * </ul>
 *
 * @author koster
 */
public enum ValidationMode {

    /**
     * Validation is applied, when a change occurs in a control that holds any validation annotation.
     */
    ONCHANGE,

    /**
     * Validation occurs only, when an explicit call to {@link com.github.actionfx.core.ActionFX#validate(Object)} is
     * performed.
     */
    MANUAL,

    /**
     * Dummy enum for specifying a global validation mode e.g. in
     * {@link com.github.actionfx.core.annotation.AFXApplication#globalValidationMode()} as "unspecified" (means
     * validation mode must be specified as part of any validation-related annotations).
     */
    GLOBAL_VALIDATION_MDOE_UNSPECIFIED
}
