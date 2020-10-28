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
package com.github.actionfx.core.view.manager;

import java.util.List;

import com.github.actionfx.core.annotation.AFXApplication;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.app.AbstractAFXApplication;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ClassPathScanningUtils;

/**
 * View manager for working with JavaFX views and controller.
 * 
 * @author koster
 *
 */
public class ViewManager {

	// system property name to look for a bean container implementation
	public static final String BEAN_CONTAINER_IMPLEMENTATION_SYSTEM_PROPERTY = "beanContainerImpl";

	// 'protected' visibility to manipulate instance for unit testing
	protected static ViewManager instance;

	private BeanContainerFacade beanContainer;

	private String mainViewId;

	protected ViewManager() {
	}

	public static synchronized ViewManager getInstance() {
		if (instance == null) {
			instance = new ViewManager();
		}
		return instance;
	}

	/**
	 * Initializes the ActionFX application and this view manager by reading out the
	 * annotated class that is expected to carry the annotation
	 * {@link AbstractAFXApplication}.
	 * 
	 * @param annotatedMainClassName
	 */
	public void initializeAFXApplication(Class<?> annotatedMainClassName) {
		AFXApplication afx = AnnotationUtils.findAnnotation(annotatedMainClassName, AFXApplication.class);
		if (afx == null) {
			throw new IllegalArgumentException("Class '" + annotatedMainClassName.getCanonicalName()
					+ "' is not annotated by @AFXApplication! View manager can not be initialized!");
		}
		mainViewId = afx.mainViewId();
		scanForViewController(afx.scanPackage());
	}

	public String getMainViewId() {
		return mainViewId;
	}

	/**
	 * Grants access to the bean container. Mainly for testing purposes.
	 * 
	 * @return the underlying bean container implementation
	 */
	protected BeanContainerFacade getBeanContainer() {
		return beanContainer;
	}

	/**
	 * Builder for setting up the singleton instance of this {@link ViewManager}.
	 * 
	 * @author koster
	 *
	 */
	public static class ViewManagerBuilder {

		// class under construction.
		private ViewManager viewManager;

		public ViewManagerBuilder() {
			viewManager = new ViewManager();
		}

		/**
		 * Sets up the bean container to use for managing the view and controller
		 * instances.
		 * 
		 * @param beanContainer the bean container implementation
		 * @return this builder
		 */
		public ViewManagerBuilder beanContainer(BeanContainerFacade beanContainer) {
			viewManager.beanContainer = beanContainer;
			return this;
		}

		/**
		 * Scans the given {@code rootPackage} and its sub-packages for annotated view
		 * controller and makes them and their view accessible in this view manager.
		 * 
		 * @param rootPackage the root package to scan. Sub-packages are also included
		 *                    in the scan.
		 */
		public ViewManagerBuilder scanPackageForComponents(String rootPackage) {
			List<Class<?>> controllerClasses = ClassPathScanningUtils.findClassesWithAnnotation(rootPackage,
					AFXController.class);
			for (Class<?> controllerClass : controllerClasses) {
				AFXController afxView = AnnotationUtils.findAnnotation(controllerClass, AFXController.class);

			}
			return this;
		}

		public ViewManager build() {
			return viewManager;
		}
	}
}
