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
package com.github.actionfx.core;

import com.github.actionfx.core.annotation.AFXApplication;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.container.DefaultBeanContainer;
import com.github.actionfx.core.utils.AnnotationUtils;

import javafx.application.Preloader;
import javafx.stage.Stage;

/**
 * Central handler for working with ActionFX. It provides routines to get views
 * and controllers.
 * <p>
 * Before using this class, it needs to be setup for the specific application.
 * <p>
 * <b>Expample:</b>
 * 
 * <pre>
 * </pre>
 * <p>
 * After the setup, an instance of this class is retrieved via:
 * 
 * <pre>
 * ActionFX instance = ActionFX.getInstance();
 * </pre>
 * 
 * @author koster
 *
 */
public class ActionFX {

	// 'protected' visibility to manipulate instance for unit testing
	protected static ActionFX instance;

	private BeanContainerFacade beanContainer;

	// the ID of the mainView that is displayed in JavaFXs primary Stage
	private String mainViewId;

	// an optional preloader class that shall be displayed during application
	// startup
	private Class<? extends Preloader> preloaderClass;

	// the package name to scan for ActionFX components
	private String scanPackage;

	/**
	 * Private constructor. Use {@link #build()} method to create your
	 * application-specific instance of {@link ActionFX}.
	 */
	private ActionFX() {
		instance = this;
	}

	/**
	 * Creates a builder that is used for setting up the application-specific
	 * instance of {@link ActionFX}.
	 * 
	 * @return the ActionFX builder instance that is used to setup the configuration
	 */
	public static ActionFXBuilder builder() {
		if (instance != null) {
			throw new IllegalStateException("ActionFX instance has already been built.");
		}
		return new ActionFXBuilder();
	}

	/**
	 * Gets an instance of {@link ActionFX}. Before calling this method, you need to
	 * setup your application-specific configuration that ActionFX shall handle by
	 * using the {@link #build()} method, returning an instance of
	 * {@link ActionFXBuilder}.
	 * 
	 * @return
	 */
	public static ActionFX getInstance() {
		if (instance == null) {
			throw new IllegalStateException("ActionFX instance has not been built yet. Call ActionFX.build() first!");
		}
		return instance;
	}

	/**
	 * The ID / name of the main view that is displayed in JavaFX's primary
	 * {@link Stage}.
	 * 
	 * @return the main view ID
	 */
	public String getMainViewId() {
		return mainViewId;
	}

	/**
	 * An optional {@link Preloader} class that shall be displayed during
	 * application start-up.
	 * 
	 * @return the preloader class to be displayed during application start-up.
	 */
	public Class<? extends Preloader> getPreloaderClass() {
		return preloaderClass;
	}

	public String getScanPackage() {
		return scanPackage;
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
	 * Builder for setting up the singleton instance of {@link ActionFX}.
	 * 
	 * @author koster
	 *
	 */
	public static class ActionFXBuilder {

		private BeanContainerFacade beanContainer;

		private String mainViewId;

		private Class<? extends Preloader> preloaderClass;

		private String scanPackage;

		public ActionFXBuilder() {
		}

		/**
		 * Creates the instance of {@link ActionFX} ready to use.
		 * 
		 * @return the ActionFX instance (which is a singleton from then on)
		 */
		public ActionFX build() {
			ActionFX actionFX = new ActionFX();
			actionFX.beanContainer = beanContainer != null ? beanContainer : new DefaultBeanContainer();
			actionFX.mainViewId = mainViewId;
			actionFX.preloaderClass = preloaderClass;
			actionFX.scanPackage = scanPackage;
			return actionFX;
		}

		/**
		 * Sets up the bean container to use for managing the view and controller
		 * instances. Users of ActionFX do not need to call this method, unless they
		 * want to use a different container implementation than
		 * {@link DefaultBeanContainer}.
		 * <p>
		 * Calling this method is required for using a different bean container, e.g.
		 * the Spring bean container.
		 * <p>
		 * If this method is not called, the {@link DefaultBeanContainer} is internally
		 * used to manage ActionFX components.
		 * 
		 * @param beanContainer the bean container implementation
		 * @return this builder
		 */
		public ActionFXBuilder beanContainer(BeanContainerFacade beanContainer) {
			this.beanContainer = beanContainer;
			return this;
		}

		/**
		 * Reads out the {@link AFXApplication} annotation that is expected to be
		 * present on the given {@code configurationClass}. In case the annotation is
		 * not present on the given class (or on its super-classes), an
		 * {@link IllegalArgumentException} is thrown.
		 * <p>
		 * Setting the {@link AFXApplication} annotation on a class is not mandatory.
		 * Instead, the configuration can be also done with the builder methods
		 * {@link #mainViewId}, {@link #scanPackage} and {@link #preloaderClass}.
		 * 
		 * @param configurationClass the configuration class that is expected to have an
		 *                           {@link AFXApplication} annotation.
		 * @return this builder
		 */
		public ActionFXBuilder configurationClass(Class<?> configurationClass) {
			AFXApplication afxApplication = AnnotationUtils.findAnnotation(configurationClass, AFXApplication.class);
			if (afxApplication == null) {
				throw new IllegalArgumentException("Class '" + configurationClass.getCanonicalName()
						+ "' or its super-classes are not annotated with @" + AFXApplication.class.getSimpleName()
						+ "!");
			}
			mainViewId = afxApplication.mainViewId();
			preloaderClass = afxApplication.preloaderClass();
			scanPackage = afxApplication.scanPackage();
			return this;
		}

		/**
		 * Sets the ID / name of the view that is used to be displayed in JavaFX's
		 * primary {@link Stage}. Please note that this ID must of course exist inside
		 * ActionFX's container e.g. by annotating a controller with
		 * {@link AFXController} and defining this view ID there.
		 * 
		 * @param mainViewId
		 * @return this builder
		 */
		public ActionFXBuilder mainViewId(String mainViewId) {
			this.mainViewId = mainViewId;
			return this;
		}

		/**
		 * The package name with dot-notation "." that shall be scanned for ActionFX
		 * components.
		 * 
		 * @param scanPackage the package name that shall be scanned for ActionFX
		 *                    componets
		 * @return this builder
		 */
		public ActionFXBuilder scanPackage(String scanPackage) {
			this.scanPackage = scanPackage;
			return this;
		}

		/**
		 * The {@link Preloader} class to use for displaying during application
		 * start-up. Using a preloader is optional.
		 * 
		 * @param preloaderClass the preloader class
		 * @return this builder
		 */
		public ActionFXBuilder preloaderClass(Class<? extends Preloader> preloaderClass) {
			this.preloaderClass = preloaderClass;
			return this;
		}
	}
}
