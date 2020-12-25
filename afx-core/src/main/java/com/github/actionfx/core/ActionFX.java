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

import java.lang.Thread.UncaughtExceptionHandler;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.annotation.AFXApplication;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.container.DefaultBeanContainer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;
import com.github.actionfx.core.instrumentation.bytebuddy.ActionFXByteBuddyEnhancer;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.view.View;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Central handler for working with ActionFX. It provides routines to get views
 * and controllers.
 * <p>
 * As ActionFX uses an internal bean container with dependency injection
 * support, it is recommended to wire all controllers with {@code @Inject}
 * instead of accessing them through this class (please note that there is also
 * support of Spring's bean container through ActionFX's {@code afx-spring-boot}
 * module).
 * <p>
 * <b>Using ActionFX:</b>
 * <p>
 * Before using this class, it needs to be setup in the {@code main()} or
 * {@link Application#init()} method for the specific application.
 * <p>
 * <b>Expample:</b> ActionFX actionFX =
 * ActionFX.builder().configurationClass(SampleApp.class).build();
 *
 * <pre>
 * <p>
 * After the setup, an instance of this class is retrieved throughout the application via:
 *
 * <pre>
 * ActionFX instance = ActionFX.getInstance();
 * </pre>
 * <p>
 * After the ActionFX instance is built, a component scan can be performed:
 *
 * <pre>
 * ActionFX.getInstance().scanForActionFXComponents();
 * </pre>
 *
 * This will populate ActionFX internal bean container and makes controller and
 * views available for usage. Please note, that this step is not required, if
 * you use Spring Boot and included module {@code afx-spring-boot} in your
 * application classpath. The {@code afx-spring-boot} provides
 * auto-configuration and scans for ActionFX components with its Spring bean
 * container during start-up.
 * <p>
 * In case you need access to the ActionFX instance in a controller, you can
 * alternatively inject the ActionFX instance via {@link Inject}. This might be
 * helpful for unit testing your controller independently of a setup ActionFX
 * instance (you can use e.g. Mockito to mock the {@link ActionFX} instance for
 * unit-testing).
 *
 * <pre>
 * &#64;AFXController(viewId="mainView", fxml="some.fxml">
 * public class MainController {
 *
 * &#64;Inject
 * private ActionFX actionFX;
 *
 * }
 * </pre>
 *
 * @author koster
 *
 */
public class ActionFX {

	private static final Logger LOG = LoggerFactory.getLogger(ActionFX.class);

	// 'protected' visibility to manipulate instance for unit testing
	protected static ActionFX instance;

	// internal framework state, starting with UNINITIALIZED. Further states are
	// CONFIGURED and finally INITIALIZED
	// BUILT
	protected static ActionFXState actionFXState = ActionFXState.UNINITIALIZED;

	// the internal bean container for managing ActionFX components
	private BeanContainerFacade beanContainer;

	// the ID of the mainView that is displayed in JavaFXs primary Stage
	private String mainViewId;

	// the package name to scan for ActionFX components
	private String scanPackage;

	// byte code enhancement strategy to use within ActionFX (runtime agent or
	// sub-classing)
	private EnhancementStrategy enhancementStrategy;

	// the byte-code enhancer to use
	private ActionFXEnhancer enhancer;

	// the primary stage of the JavaFX application
	private Stage primaryStage;

	// central exception handler
	private UncaughtExceptionHandler uncaughtExceptionHandler;

	/**
	 * Private constructor. Use {@link #build()} method to create your
	 * application-specific instance of {@link ActionFX}.
	 */
	private ActionFX() {
		instance = this;
	}

	/**
	 * Post construct routine that is executed by the {@link ActionFXBuilder} after
	 * the instance of {@link ActionFX} has been created.
	 */
	private void postConstruct() {
		// if the agent shall be used and it is not yet installed then we install it
		// here
		if (!enhancer.agentInstalled() && enhancementStrategy == EnhancementStrategy.RUNTIME_INSTRUMENTATION_AGENT) {
			enhancer.installAgent();
		}
		// initialize exception handling
		Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
		// after configuration and instance creation, the state transfers to CONFIGURED
		actionFXState = ActionFXState.CONFIGURED;
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
	 * Scans for ActionFX components, depending on the configured
	 * {@link #scanPackage}. In case an alternative bean container shall be used for
	 * ActionFX, please use {@link #scanForActionFXComponents(BeanContainerFacade)}
	 * instead of this method.
	 */
	public void scanForActionFXComponents() {
		checkActionFXState(ActionFXState.CONFIGURED);
		scanForActionFXComponentsInternal();
	}

	/**
	 * Scans for ActionFX components, depending on the configured
	 * {@link #scanPackage}. This routine allows to specify an alternative
	 * {@link BeanContainerFacade} to use, instead of the internal
	 * {@link DefaultBeanContainer}. The definition of the
	 * {@link BeanContainerFacade} type is not part of the ActionFX configuration,
	 * because the configuration and setup is performed at the soonest possible time
	 * in the application, preferable in the {@code main()} method of the
	 * Application. The bean container implementation (e.g. when using Spring as
	 * bean container) is not yet setup at this point in time!
	 *
	 * @param beanContainer the
	 */
	public void scanForActionFXComponents(final BeanContainerFacade beanContainer) {
		checkActionFXState(ActionFXState.CONFIGURED);
		this.beanContainer = beanContainer;
		scanForActionFXComponentsInternal();
	}

	/**
	 * Internal component scanning routine.
	 */
	private void scanForActionFXComponentsInternal() {
		// let's let the bean container implementation do the work
		beanContainer.populateContainer(scanPackage);
		actionFXState = ActionFXState.INITIALIZED;
	}

	/**
	 * Gets the view by the supplied {@code viewId}.
	 *
	 * @param viewId the view ID
	 * @return the view instance. If the {@code viewId} does not exists,
	 *         {@code null} is returned.
	 */
	public View getView(final String viewId) {
		return beanContainer.getBean(viewId);
	}

	/**
	 * Gets the controller defined by the supplied {@code controllerClass}.
	 *
	 * @param controllerClass the controller class for that a controller instance
	 *                        shall be retrieved.
	 * @return the retrieved controller instance.If the {@code controllerClass} does
	 *         not exists, {@code null} is returned.
	 */
	public <T> T getController(final Class<T> controllerClass) {
		return beanContainer.getBean(controllerClass);
	}

	/**
	 * Gets the controller by the supplied {@code controllerId}.
	 *
	 * @param controllerId the controller ID for that a controller instance shall be
	 *                     retrieved.
	 * @return the retrieved controller instance.If the {@code controllerId} does
	 *         not exists, {@code null} is returned.
	 */
	public <T> T getController(final String controllerId) {
		return beanContainer.getBean(controllerId);
	}

	/**
	 * Returns the main view of the application.
	 *
	 * @return the main view
	 */
	public View getMainView() {
		return getView(getMainViewId());
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
	 * Displays the main view in the primary stage.
	 *
	 * @param primaryStage the primary stage
	 */
	public void displayMainView(final Stage primaryStage) {
		setPrimaryStage(primaryStage);
		final View view = getMainView();
		view.show(primaryStage);
	}

	/**
	 * The package name with dot-notation "." that shall be scanned for ActionFX
	 * components.
	 *
	 * @param scanPackage the package name that shall be scanned for ActionFX
	 *                    componets
	 * @return this builder
	 */
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
	 * The enhancement strategy to use within ActionFX.
	 *
	 * @return the enhancement strategy
	 */
	public EnhancementStrategy getEnhancementStrategy() {
		return enhancementStrategy;
	}

	/**
	 * The enhancer to use within ActionFX.
	 *
	 * @return the enhancer
	 */
	public ActionFXEnhancer getEnhancer() {
		return enhancer;
	}

	/**
	 * Checks, whether ActionFX is currently in {@code expectedState}. If ActionFX's
	 * state is different from the expected state, an {@link IllegalStateException}
	 * is thrown.
	 *
	 * @param expectedState the expected state to check.
	 */
	private static void checkActionFXState(final ActionFXState expectedState) {
		if (actionFXState != expectedState) {
			throw new IllegalStateException(
					"ActionFX is in state '" + actionFXState + "', while expected state was '" + expectedState + "'!");
		}
	}

	/**
	 * Gets the primary stage, if it was set be the user.
	 *
	 * @return the primary stage
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * Users can set the primary stage here so that it is available throughout the
	 * application.
	 *
	 * @param primaryStage the primary stage
	 */
	public void setPrimaryStage(final Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	/**
	 * Resets ActionFX to its initial state. Can be used in unit test. Use this
	 * method in productive code only when you know exactly what you are doing.
	 */
	@SuppressFBWarnings(justification = "Design Decision")
	public void reset() {
		instance = null;
		actionFXState = ActionFXState.UNINITIALIZED;
	}

	/**
	 * Builder for setting up the singleton instance of {@link ActionFX}.
	 *
	 * @author koster
	 *
	 */
	public static class ActionFXBuilder {

		private String mainViewId;

		private String scanPackage;

		private EnhancementStrategy enhancementStrategy;

		private ActionFXEnhancer actionFXEnhancer;

		private UncaughtExceptionHandler uncaughtExceptionHandler;

		/**
		 * Creates the instance of {@link ActionFX} ready to use.
		 *
		 * @return the ActionFX instance (which is a singleton from then on)
		 */
		public ActionFX build() {
			final ActionFX actionFX = new ActionFX();
			actionFX.beanContainer = new DefaultBeanContainer();
			actionFX.mainViewId = mainViewId;
			actionFX.scanPackage = scanPackage;
			actionFX.enhancementStrategy = enhancementStrategy != null ? enhancementStrategy
					: EnhancementStrategy.RUNTIME_INSTRUMENTATION_AGENT;
			actionFX.enhancer = actionFXEnhancer != null ? actionFXEnhancer : new ActionFXByteBuddyEnhancer();
			actionFX.uncaughtExceptionHandler = uncaughtExceptionHandler != null ? uncaughtExceptionHandler
					: (thread, exception) -> LOG.error("[Thread {}] Uncaught exception", thread.getName(), exception);
			actionFX.postConstruct();
			return actionFX;
		}

		/**
		 * Reads out the {@link AFXApplication} annotation that is expected to be
		 * present on the given {@code configurationClass}. In case the annotation is
		 * not present on the given class (or on its super-classes), an
		 * {@link IllegalArgumentException} is thrown.
		 * <p>
		 * Setting the {@link AFXApplication} annotation on a class is not mandatory.
		 * Instead, the configuration can be also done with the builder methods
		 * {@link #mainViewId}, {@link #scanPackage}, etc.
		 *
		 * @param configurationClass the configuration class that is expected to have an
		 *                           {@link AFXApplication} annotation.
		 * @return this builder
		 */
		public ActionFXBuilder configurationClass(final Class<?> configurationClass) {
			final AFXApplication afxApplication = AnnotationUtils.findAnnotation(configurationClass,
					AFXApplication.class);
			if (afxApplication == null) {
				throw new IllegalArgumentException("Class '" + configurationClass.getCanonicalName()
						+ "' or its super-classes are not annotated with @" + AFXApplication.class.getSimpleName()
						+ "!");
			}
			mainViewId = afxApplication.mainViewId();
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
		public ActionFXBuilder mainViewId(final String mainViewId) {
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
		public ActionFXBuilder scanPackage(final String scanPackage) {
			this.scanPackage = scanPackage;
			return this;
		}

		/**
		 * The byte-code enhancement strategy to use within ActionFX. Currently the
		 * following enhancement strategies are available:
		 * <ul>
		 * <li>{@link EnhancementStrategy#RUNTIME_INSTRUMENTATION_AGENT}: A byte-code
		 * instrumentation agent is installed/attached at runtime. Methods of controller
		 * classes are directly enhanced via method interceptors.</li>
		 * <li>{@link EnhancementStrategy#SUBCLASSING}: Controller classes are
		 * sub-classed, while controller methods are overriden and method interceptors
		 * are attached.</li>
		 * </ul>
		 *
		 * @param enhancementStrategy the enhancement strategy to use
		 * @return this builder
		 */
		public ActionFXBuilder enhancementStrategy(final EnhancementStrategy enhancementStrategy) {
			this.enhancementStrategy = enhancementStrategy;
			return this;
		}

		/**
		 * Sets the implementation of interface {@link ActionFXEnhancer} to use within
		 * ActionFX. In case there is no instance set, the default enhancer
		 * {@link ActionFXByteBuddyEnhancer} is used.
		 * <p>
		 * Please note that implementations of interface {@link ActionFXEnhancer} must
		 * provide the possibility of both, byte code instrumentation via a runtime
		 * agent and byte code enhancement via sub-classing.
		 *
		 * @param actionFXEnhancer the enhancer implementation to use
		 * @return this builder
		 */
		public ActionFXBuilder actionFXEnhancer(final ActionFXEnhancer actionFXEnhancer) {
			this.actionFXEnhancer = actionFXEnhancer;
			return this;
		}

		/**
		 * Configures an exception handler for uncaught exceptions.
		 *
		 * @param uncaughtExceptionHandler the exception handler for uncaught exceptions
		 * @return this builder
		 */
		public ActionFXBuilder uncaughtExceptionHandler(final UncaughtExceptionHandler uncaughtExceptionHandler) {
			this.uncaughtExceptionHandler = uncaughtExceptionHandler;
			return this;
		}
	}

	/**
	 * Enumeration describing the state of ActionFX.
	 *
	 * @author koster
	 *
	 */
	public enum ActionFXState {
		/**
		 * ActionFX is not yet initialized.
		 */
		UNINITIALIZED,

		/**
		 * ActionFX instance is configured and built, but no component scan is
		 * performed.
		 */
		CONFIGURED,

		/**
		 * ActionFX component scan is performed and instance is fully ready for use.
		 */
		INITIALIZED
	}
}
