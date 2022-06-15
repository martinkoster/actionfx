/*
 * Copyright (c) 2020 Martin Koster
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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that can be applied to the main class of an ActionFX application for configuring it.
 *
 * @author koster
 *
 */
@Retention(RUNTIME)
@Documented
@Target(TYPE)
public @interface AFXApplication {

    /**
     * Package name to scan for view components.
     *
     * @return the package name to scan (sub-packages are included in the scan)
     */
    public String scanPackage();

    /**
     * The ID of the main view that shall be display in the JavaFX primary stage.
     *
     * @return the ID of the main view.
     */
    public String mainViewId();

    /**
     * Flag that determines whether the best bean container implementation shall be determined automatically.
     * Autodetection is enabled by default. If the flag is disabled, ActionFX default bean container will be internally
     * used. In case autodetection is enabled and the module {@code afx-spring-boot} is on the classpath, the Spring
     * container is used as ActionFX's bean container.
     *
     * @return flag that determines whether the bean container implementation shall be autodetected or not. Default is
     *         {@code true}.
     */
    public boolean enableBeanContainerAutodetection() default true;

    /**
     * Specifies the global validation mode that shall be applied on JavaFX controls that carry an validation-related
     * annotation like {@link com.github.actionfx.core.annotation.AFXValidateRequired}. In case a global validation mode
     * is set via this builder, the annotations do not need to specify a validation mode anymore. This is helpful for
     * reducing the number of attributes in validation-related annotations and ActionFX controllers.
     *
     * @return the global validation mode. Default value is {@link ValidationMode#GLOBAL_VALIDATION_MDOE_UNSPECIFIED},
     *         means, the specification of a validation mode is in responsibility of any validation-related annotation.s
     */
    public ValidationMode globalValidationMode() default ValidationMode.GLOBAL_VALIDATION_MDOE_UNSPECIFIED;
}
