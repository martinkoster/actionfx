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

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.annotation.AFXApplication;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXSubscribe;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.container.DefaultBeanContainer;
import com.github.actionfx.core.container.extension.ControllerExtensionBean;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.events.SimplePriorityAwareEventBus;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.instrumentation.bytebuddy.ActionFXByteBuddyEnhancer;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Central handler for working with ActionFX. It provides routines to get views
 * and controllers.
 * <p>
 * As ActionFX uses an internal bean container with dependency injection
 * support, it is recommended to wire all controllers with {@code @Inject}
 * instead of accessing them through this class (please note that there is also
 * support of Spring's bean container through ActionFX's
 * {@code actionfx-spring-boot} module).
 * <p>
 * <b>Using ActionFX:</b>
 * <p>
 * Before using this class, it needs to be setup in the {@code main()} or
 * {@link Application#init()} method for the specific application.
 * <p>
 * <b>Expample:</b>
 *
 * <pre>
 * ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();
 * </pre>
 *
 * After the setup, an instance of this class is retrieved throughout the
 * application via:
 *
 * <pre>
 * ActionFX instance = ActionFX.getInstance();
 * </pre>
 *
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
	protected BeanContainerFacade beanContainer;

	// the ID of the mainView that is displayed in JavaFXs primary Stage
	protected String mainViewId;

	// the package name to scan for ActionFX components
	protected String scanPackage;

	// byte code enhancement strategy to use within ActionFX (runtime agent or
	// sub-classing)
	protected EnhancementStrategy enhancementStrategy;

	// the byte-code enhancer to use
	protected ActionFXEnhancer enhancer;

	// the primary stage of the JavaFX application
	protected Stage primaryStage;

	// central exception handler
	protected UncaughtExceptionHandler uncaughtExceptionHandler;

	// observable property for a locale for internationalization
	protected ObservableValue<Locale> observableLocale;

	// holds a list of custom controller extensions added by the user during
	// ActionFX setup
	protected ControllerExtensionBean controllerExtensionBean;

	/**
	 * Internal constructor. Use {@link #build()} method to create your
	 * application-specific instance of {@link ActionFX}.
	 */
	@SuppressFBWarnings(justification = "Design Decision")
	ActionFX() {
		instance = this; // NOSONAR
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
	 * using the {@link #builder()} method, returning an instance of
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
	 * Returns {@code true}, in case the ActionFX instance is available and the
	 * state is either {@link ActionFXState#INITIALIZED} or
	 * {@link ActionFXState#CONFIGURED} , {@code false}, if the instance is not yet
	 * setup or the state is {@link ActionFXState#UNINITIALIZED}.
	 *
	 * @return {@code true}, in case the ActionFX instance is properly available and
	 *         setup for use, {@code false} otherwise
	 */
	public static boolean isConfigured() {
		return instance != null
				&& (actionFXState == ActionFXState.INITIALIZED || actionFXState == ActionFXState.CONFIGURED);
	}

	/**
	 * Returns {@code true}, in case the ActionFX instance is available and the
	 * state is {@link ActionFXState#INITIALIZED} , {@code false}, if the instance
	 * is not yet setup or the state is {@link ActionFXState#UNINITIALIZED} or
	 * {@link ActionFXState#CONFIGURED}
	 *
	 * @return {@code true}, in case the ActionFX instance is properly available and
	 *         initialized, {@code false} otherwise
	 */
	public static boolean isInitialized() {
		return instance != null && actionFXState == ActionFXState.INITIALIZED;
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
		// let's register some ActionFX-specific beans in the container before we do the
		// component scan
		addActionFXBeans();

		// let's let the bean container implementation do the work
		if (StringUtils.isNotEmpty(scanPackage)) {
			beanContainer.runComponentScan(scanPackage);
		}
		actionFXState = ActionFXState.INITIALIZED;// NOSONAR
	}

	/**
	 * Adds ActionFX beans that are available to the developer independent of the
	 * used bean container.
	 */
	private void addActionFXBeans() {
		// register the locale as "ObservableValue" (singleton and lazy-initialisation)
		beanContainer.addBeanDefinition(BeanContainerFacade.LOCALE_PROPERTY_BEANNAME, observableLocale.getClass(), true,
				true, () -> observableLocale);
		// register the locale itself (non-singleton - request locale each time it is
		// needed)
		beanContainer.addBeanDefinition(BeanContainerFacade.LOCALE_BEANNAME, Locale.class, false, true,
				() -> observableLocale.getValue());

		// make ActionFX class itself available as bean
		beanContainer.addBeanDefinition(BeanContainerFacade.ACTIONFX_BEANNAME, ActionFX.class, true, false, () -> this);

		// add the event bus to the bean container
		beanContainer.addBeanDefinition(BeanContainerFacade.EVENT_BUS_BEAN, PriorityAwareEventBus.class, true, true,
				SimplePriorityAwareEventBus::new);

		// add controller extensions to the bean container
		beanContainer.addBeanDefinition(BeanContainerFacade.CONTROLLER_EXTENSION_BEANNAME,
				ControllerExtensionBean.class, true, false, () -> controllerExtensionBean);

		// add the dialog controller to the bean container
		beanContainer.addBeanDefinition(BeanContainerFacade.DIALOG_CONTROLLER_BEAN, DialogController.class, true, true,
				DialogController::new);

		// add the conversion service that is listening to the locale
		beanContainer.addBeanDefinition(BeanContainerFacade.CONVERSION_SERVICE_BEAN, ConversionService.class, true,
				true, () -> new ConversionService(getObservableLocale()));
	}

	/**
	 * Adds a new controller to ActionFX' internal bean container. It is expected
	 * that the controller class is annotated by {@link AFXControlller}.
	 * <p>
	 * Please prefer scanning for controller classes via
	 * {@link #scanForActionFXComponents()} and a set {@code scanPackage} in the
	 * internal {@link ActionFXBuilder}.
	 *
	 * @param beanClass the controller bean type
	 */
	public void addController(final Class<?> controllerClass) {
		beanContainer.addControllerBeanDefinition(controllerClass);
	}

	/**
	 * Gets the view by the supplied {@code viewId}.
	 *
	 * @param viewId the view ID
	 * @return the view instance. If the {@code viewId} does not exists, an
	 *         {@code IllegalArgumentException} is thrown.
	 */
	public View getView(final String viewId) {
		final Object candidate = beanContainer.getBean(viewId);
		if (candidate == null) {
			throw new IllegalArgumentException("There is no view with ID='" + viewId
					+ "' in the bean container of type '" + beanContainer.getClass().getCanonicalName() + "'!");
		}
		if (!View.class.isAssignableFrom(candidate.getClass())) {
			throw new IllegalArgumentException(
					"Bean with ID='" + viewId + "' is not of type '" + View.class.getCanonicalName()
							+ "', but was of type '" + candidate.getClass().getCanonicalName() + "'!");
		}
		return (View) candidate;
	}

	/**
	 * Gets the controller defined by the supplied {@code controllerClass}.
	 *
	 * @param controllerClass the controller class for that a controller instance
	 *                        shall be retrieved.
	 * @return the retrieved controller instance.If the {@code controllerClass} does
	 *         not exists, {@code null} is returned.
	 */
	public <T> T getBean(final Class<T> controllerClass) {
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
	public <T> T getBean(final String controllerId) {
		return beanContainer.getBean(controllerId);
	}

	/**
	 * Gets the {@link ResourceBundle} that is used internationalization of the view
	 * associated with the controller identified by {@code controllerId}.
	 * <p>
	 * The locale is retrieved from the {@link #localeSupplier} that has been
	 * configured with this instance of {@link ActionFX}.
	 *
	 * @param controllerId the ID of the controller
	 * @return the resource bundle, or {@code null}, in case there is no resource
	 *         bundle associated with the given controller
	 */
	public ResourceBundle getControllerResourceBundle(final String controllerId) {
		final Object controller = getBean(controllerId);
		if (controller == null) {
			return null;
		}
		return getControllerResourceBundle(controller.getClass());
	}

	/**
	 * Gets the {@link ResourceBundle} that is used internationalization of the view
	 * associated with the given {@code controllerClass}.
	 * <p>
	 * The locale is retrieved from the {@link #localeSupplier} that has been
	 * configured with this instance of {@link ActionFX}.
	 *
	 * @param controllerId the ID of the controller
	 * @return the resource bundle, or {@code null}, in case there is no resource
	 *         bundle associated with the given controller
	 */
	public ResourceBundle getControllerResourceBundle(final Class<?> controllerClass) {
		return beanContainer.resolveResourceBundle(controllerClass, observableLocale.getValue());
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
	 * Returns the {@link View} associated with the supplied {@code controller}.
	 *
	 * @param controller the controller for that the view shall be retrieved
	 * @return the view associated with the supplied controller
	 */
	public View getView(final Object controller) {
		return ControllerWrapper.of(controller).getView();
	}

	/**
	 * Hides the view associated with the supplied {@code controller}. This method
	 * is useful for closing (modal) dialogs from its controller.
	 *
	 * @param controller the controller thats view shall be hidden
	 */
	public void hideView(final Object controller) {
		getView(controller).hide();
	}

	/**
	 * Displays the main view in the primary stage.
	 *
	 * @param primaryStage the primary stage
	 */
	public void showMainView(final Stage primaryStage) {
		setPrimaryStage(primaryStage);
		final View view = getMainView();
		view.show(primaryStage);
	}

	/**
	 * Shows the view of the supplied {@code controller}.
	 *
	 * @param controller the controller thats view shall be shown
	 */
	public void showView(final Object controller) {
		getView(controller).show();
	}

	/**
	 * Shows the view of the supplied {@code controller} inside the given
	 * {@link Stage}.
	 *
	 * @param controller the controller thats view shall be shown
	 * @param stage      the stage where the view shall be displayed inside
	 */
	public void showView(final Object controller, final Stage stage) {
		getView(controller).show(stage);
	}

	/**
	 * Shows the view of the supplied {@code controller} in a modal dialog.
	 *
	 * @param controller the controller thats view shall be shown
	 */
	public void showViewAndWait(final Object controller) {
		getView(controller).showAndWait();
	}

	/**
	 * Retrieves ActionFX' conversion service.
	 *
	 * @return the conversion service
	 */
	public ConversionService getConversionService() {
		return getBean(ConversionService.class);
	}

	/**
	 * Retrieves ActionFX' internal event bus singleton.
	 *
	 * @return the event bus
	 */
	public PriorityAwareEventBus getEventBus() {
		return getBean(PriorityAwareEventBus.class);
	}

	/**
	 * Displays a modal confirmation dialogue with the specified {@code title} and
	 * {@code message}.
	 *
	 * @param title       the title of the dialog
	 * @param headerText  the header text to be displayed in the dialog
	 * @param contentText the content text to be displayed in the dialog
	 * @return {@code true}, when the OK button has been pressed, {@code false}
	 *         otherwise.
	 */
	public boolean showConfirmationDialog(final String title, final String headerText, final String contentText) {
		return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showConfirmationDialog(title,
				headerText, contentText);
	}

	/**
	 * Displays a modal warning dialogue with the specified {@code title} and
	 * {@code message}.
	 *
	 * @param title       the title of the dialog
	 * @param headerText  the header text to be displayed in the dialog
	 * @param contentText the content text to be displayed in the dialog
	 */
	public void showWarningDialog(final String title, final String headerText, final String contentText) {
		((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showWarningDialog(title, headerText,
				contentText);
	}

	/**
	 * Displays a modal information dialogue with the specified {@code title} and
	 * {@code message}.
	 *
	 * @param title       the title of the dialog
	 * @param headerText  the header text to be displayed in the dialog
	 * @param contentText the content text to be displayed in the dialog
	 */
	public void showInformationDialog(final String title, final String headerText, final String contentText) {
		((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showInformationDialog(title,
				headerText, contentText);
	}

	/**
	 * Displays a modal error dialogue with the specified {@code title} and
	 * {@code message}.
	 *
	 * @param title       the title of the dialog
	 * @param headerText  the header text to be displayed in the dialog
	 * @param contentText the content text to be displayed in the dialog
	 */
	public void showErrorDialog(final String title, final String headerText, final String contentText) {
		((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showErrorDialog(title, headerText,
				contentText);
	}

	/**
	 * Displays a directory chooser and returns the selected {@link File}
	 * descriptor.
	 *
	 * @param title            the dialog title
	 * @param defaultDirectory the directory to show, when the dialog is opened
	 * @param owner            the window owner
	 * @return the selected directory, or {@code null}, if no directory has been
	 *         selected
	 */
	public File showDirectoryChooserDialog(final String title, final File defaultDirectory, final Window owner) {
		return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))
				.showDirectoryChooserDialog(title, defaultDirectory, owner);
	}

	/**
	 * Displays a file chooser and returns the selected <tt>File</tt> descriptor.
	 *
	 * @param title               the dialog title
	 * @param defaultDirectory    the directory to show, when the dialog is opened
	 * @param initialFileName     the initially suggested file name
	 * @param fileExtensionFilter an optional file extension filter
	 * @param owner               the window owner
	 * @return the selected file, or {@code null} if no file has been selected
	 */
	public File showFileOpenDialog(final String title, final File defaultDirectory, final String initialFileName,
			final ExtensionFilter fileExtensionFilter, final Window owner) {
		return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showFileOpenDialog(title,
				defaultDirectory, initialFileName, fileExtensionFilter, owner);
	}

	/**
	 * Displays a file chooser and returns the selected {@link File} descriptor.
	 *
	 * @param title            the dialog title
	 * @param defaultDirectory the directory to show, when the dialog is opened
	 * @param owner            the window owner
	 * @return the selected file, or {@code null}, if no file has been selected
	 */
	public File showFileOpenDialog(final String title, final File defaultDirectory, final Window owner) {
		return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showFileOpenDialog(title,
				defaultDirectory, owner);
	}

	/**
	 * Displays a file chooser and returns the selected {@link File} descriptor.
	 *
	 * @param title            the dialog title
	 * @param defaultDirectory the directory to show, when the dialog is opened
	 * @param owner            the window owner
	 * @return the selected file, or {@code null}, if no file has been selected
	 */
	public File showFileSaveDialog(final String title, final File defaultDirectory, final Window owner) {
		return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showFileSaveDialog(title,
				defaultDirectory, owner);
	}

	/**
	 * Displays a file chooser and returns the selected {@link File} descriptor.
	 *
	 * @param title               the dialog title
	 * @param defaultDirectory    the directory to show, when the dialog is opened
	 * @param initialFileName     the initially suggested file name
	 * @param fileExtensionFilter an optional file extension filter
	 * @param owner               the window owner
	 * @return the selected file, or {@code null}, if no file has been selected
	 */
	public File showFileSaveDialog(final String title, final File defaultDirectory, final String initialFileName,
			final ExtensionFilter fileExtensionFilter, final Window owner) {
		return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showFileSaveDialog(title,
				defaultDirectory, initialFileName, fileExtensionFilter, owner);
	}

	/**
	 * Displays a modal <tt>TextInputDialog</tt> that lets the user enter a single
	 * string value.
	 *
	 * @param title       the title of the input dialog
	 * @param headerText  a header text to be displayed inside the dialogue
	 * @param contentText a content text displayed in front of the input text field
	 * @return the entered string value, or <tt>null</tt>, if no value has been
	 *         entered.
	 */
	public String showTextInputDialog(final String title, final String headerText, final String contentText) {
		return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showTextInputDialog(title,
				headerText, contentText);
	}

	/**
	 * Displays a modal <tt>TextInputDialog</tt> that lets the user enter a single
	 * string value. A default text is already pre-set.
	 *
	 * @param title        the title of the input dialog
	 * @param headerText   a header text to be displayed inside the dialogue
	 * @param contentText  a content text displayed in front of the input text field
	 * @param defaultValue the pre-set default text
	 * @return the entered string value, or <tt>null</tt>, if no value has been
	 *         entered.
	 */
	public String showTextInputDialog(final String title, final String headerText, final String contentText,
			final String defaultValue) {
		return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEAN)).showTextInputDialog(title,
				headerText, contentText, defaultValue);
	}

	/**
	 * Publishes the given {@code event} to all methods that are annotated by
	 * {@link AFXSubscribe} and which are listening to the type of {@code event}.
	 * <p>
	 * In case the annotated method has a method argument that is of the same type
	 * than {@code event}, then the {@code event} is used as method argument to that
	 * method.
	 * <p>
	 * Please note that methods annotated by {@link AFXSubscribe} can also have
	 * additional method argument, that are e.g. annotated by
	 * {@link AFXControlValue}.
	 *
	 * @param event the event to publish
	 */
	public void publishNotification(final Object event) {
		getEventBus().publish(event);
	}

	/**
	 * The package name with dot-notation "." that shall be scanned for ActionFX
	 * components.
	 *
	 * @param scanPackage the package name that shall be scanned for ActionFX
	 *                    components
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
	 * Gets the {@link UncaughtExceptionHandler}, if set.
	 *
	 * @return the {@link UncaughtExceptionHandler}.
	 */
	public UncaughtExceptionHandler getUncaughtExceptionHandler() {
		return uncaughtExceptionHandler;
	}

	/**
	 * Gets the locale as an {@link ObservableValue} If not set by
	 * {@link ActionFXBuilder#observableLocale}, the default implementation will
	 * return the {@link java.util.Locale#getDefault()} locale.
	 *
	 * @return the observable locale
	 */
	public ObservableValue<Locale> getObservableLocale() {
		return observableLocale;
	}

	/**
	 * Checks, whether ActionFX is currently in {@code expectedState}. If ActionFX's
	 * state is different from the expected state, an {@link IllegalStateException}
	 * is thrown.
	 *
	 * @param expectedState the expected state to check.
	 */
	static void checkActionFXState(final ActionFXState expectedState) {
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
		instance = null;// NOSONAR
		actionFXState = ActionFXState.UNINITIALIZED;// NOSONAR
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

		private ObservableValue<Locale> observableLocale;

		private final List<Consumer<Object>> controllerExtensions = new ArrayList<>();

		/**
		 * Creates the instance of {@link ActionFX} ready to use.
		 *
		 * @return the ActionFX instance (which is a singleton from then on)
		 */
		public ActionFX build() {
			final ActionFX actionFX = new ActionFX();
			actionFX.beanContainer = new DefaultBeanContainer(controllerExtensions);
			actionFX.mainViewId = mainViewId;
			actionFX.scanPackage = scanPackage;
			actionFX.enhancementStrategy = enhancementStrategy != null ? enhancementStrategy
					: EnhancementStrategy.SUBCLASSING;
			actionFX.enhancer = actionFXEnhancer != null ? actionFXEnhancer : new ActionFXByteBuddyEnhancer();
			actionFX.uncaughtExceptionHandler = uncaughtExceptionHandler != null ? uncaughtExceptionHandler
					: (thread, exception) -> LOG.error("[Thread {}] Uncaught exception", thread.getName(), exception);
			actionFX.observableLocale = observableLocale != null ? observableLocale
					: new SimpleObjectProperty<>(Locale.getDefault());
			actionFX.controllerExtensionBean = new ControllerExtensionBean(controllerExtensions);
			postConstruct(actionFX);
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

		/**
		 * Configures an {@code ObservableValue} that holds a proper
		 * {@code java.util.Locale} for internationalization.
		 *
		 * @param observableLocale the observable locale
		 * @return this builder
		 */
		public ActionFXBuilder observableLocale(final ObservableValue<Locale> observableLocale) {
			this.observableLocale = observableLocale;
			return this;
		}

		/**
		 * Configures an {@code Locale} for internationalization.
		 *
		 * @param locale the locale
		 * @return this builder
		 */
		public ActionFXBuilder locale(final Locale locale) {
			return observableLocale(new SimpleObjectProperty<>(locale));
		}

		/**
		 * Registers custom controller extensions instances implemented by the user.
		 * Controller extensions are applied to the controller after instantiation,
		 * after dependency injection, but before methods annotated with
		 * {@code @PostConstruct} are invoked.
		 *
		 * @param controllerExtensions the controller extensions
		 * @return this builder
		 */
		@SuppressWarnings("unchecked")
		public ActionFXBuilder controllerExtension(final Consumer<Object>... extensions) {
			if (extensions.length > 0) {
				controllerExtensions.addAll(Arrays.asList(extensions));
			}
			return this;
		}

		/**
		 * Registers custom controller extensions implemented by the user. It is
		 * expected that supplied classes have a default, no-argument constructor.
		 * Controller extensions are applied to the controller after instantiation,
		 * after dependency injection, but before methods annotated with
		 * {@code @PostConstruct} are invoked.
		 *
		 * @param controllerExtensions the controller extension classes
		 * @return this builder
		 */
		@SuppressWarnings("unchecked")
		public ActionFXBuilder controllerExtension(final Class<? extends Consumer<Object>>... extensionClasses) {
			final Consumer<Object>[] extensions = new Consumer[extensionClasses.length];
			if (extensionClasses.length > 0) {
				for (int i = 0; i < extensionClasses.length; i++) {
					extensions[i] = ReflectionUtils.instantiateClass(extensionClasses[i]);
				}
			}
			return controllerExtension(extensions);
		}

		/**
		 * Post construct routine that is executed by the {@link ActionFXBuilder} after
		 * the instance of {@link ActionFX} has been created.
		 */
		private void postConstruct(final ActionFX actionFX) {
			// if the agent shall be used and it is not yet installed then we install it
			// here
			if (!actionFX.enhancer.agentInstalled()
					&& actionFX.enhancementStrategy == EnhancementStrategy.RUNTIME_INSTRUMENTATION_AGENT) {
				actionFX.enhancer.installAgent();
			}
			// initialize exception handling
			Thread.setDefaultUncaughtExceptionHandler(actionFX.uncaughtExceptionHandler);
			// after configuration and instance creation, the state transfers to CONFIGURED
			ActionFX.actionFXState = ActionFXState.CONFIGURED;// NOSONAR
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
