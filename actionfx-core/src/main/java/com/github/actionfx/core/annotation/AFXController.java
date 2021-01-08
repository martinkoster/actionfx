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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for ActionFX controllers to associate and manage a view together
 * with it.
 *
 * @author MartinKoster
 *
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXController {

	/**
	 * The ID of the view. Must be unique among all views of this application.
	 *
	 * @return the view ID
	 */
	public String viewId();

	/**
	 * Path to the FXML file to load for this view. Path is relative to the
	 * application's classpath.
	 *
	 * @return the path to the FXML file
	 */
	public String fxml();

	/**
	 * Specifies whether this view is a modal dialog or a regular window. Default is
	 * <tt>false</tt>.
	 *
	 * @return <tt>true</tt>, if this view is a modal dialogue, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean modal() default false;

	/**
	 * Specifies whether this view shall be displayed maxized or not. Default is
	 * <tt>false</tt>.
	 *
	 * @return <tt>true</tt>, whether the view shall be maximized, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean maximized() default false;

	/**
	 * The width of the view (however <tt>maximized</tt> has a higher priority).
	 *
	 * @return the width of the view
	 */
	public int width() default 200;

	/**
	 * The height of the window (however <tt>maximized</tt> has a higher priority).
	 *
	 * @return the height of the view
	 */
	public int height() default 100;

	/**
	 * The title to be displayed for the given view/window.
	 *
	 * @return the title of the window
	 */
	public String title() default "";

	/**
	 * The X position of the window on the screen.
	 *
	 * @return the X position
	 */
	public int posX() default 0;

	/**
	 * The Y position of the window on the screen.
	 *
	 * @return the Y position
	 */
	public int posY() default 0;

	/**
	 * The icon to be displayed in case the view is displayed in its own stage.
	 *
	 * @return the location of the icon
	 */
	public String icon() default "";

	/**
	 * Determines whether the view is managed as singleton or not. If the view is
	 * not a singleton, the view is newly created whenever it is requested.
	 *
	 * @return {@code true}, if the view is managed as singleton, {@code false}, if
	 *         there is a new view created every time the view is requested.
	 */
	public boolean singleton() default true;

	/**
	 * Flag that controls the initialization of the view and controller. If set to
	 * {@code true}, view components are lazily initialized at the point the view is
	 * really required and requested to be used vie the view manager. If set to
	 * {@link false}, the view components are initialized at the startup of the
	 * ActionFX application, when the view manager is initialized. Although lazy
	 * loading should be preferred, disabling of lazy loading makes sense, when you
	 * want to have a fail-early fail-fast pattern and exceptions during view
	 * initializations should/must be thrown at application startup (and not later,
	 * when you already work with the application).
	 *
	 * @return {@code true} (default), when view components should be lazily
	 *         initialized, {@code false} otherwise
	 */
	public boolean lazyInit() default true;

	/**
	 * Which stylesheets shall be applied to the scene? This array contains a list
	 * of classpath locations to CSS files.
	 *
	 * @return a list of CSS classpath locations. Default is an empty array.
	 */
	public String[] stylesheets() default {};

	/**
	 * An optional list of nested views that shall be embedded in the view.
	 *
	 * @return the nested views to be embedded
	 */
	public AFXNestedView[] nestedViews() default {};
}
