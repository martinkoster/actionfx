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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javafx.application.Preloader;

/**
 * Annotation that can be applied to the main class of an ActionFX application.
 * 
 * @author koster
 *
 */
@Retention(RUNTIME)
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
	 * Optional preloader class that is used to be displayed during startup of the
	 * actual application.
	 * 
	 * @return the preloader class to use during startup
	 */
	public Class<? extends Preloader> preloaderClass() default Preloader.class;
}
