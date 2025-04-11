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
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.annotation.AFXApplication;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXSubscribe;
import com.github.actionfx.core.annotation.ValidationMode;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.container.DefaultActionFXBeanContainer;
import com.github.actionfx.core.container.instantiation.ConstructorBasedInstantiationSupplier;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.extension.ActionFXExtensionsBean;
import com.github.actionfx.core.extension.beans.BeanExtension;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.instrumentation.bytebuddy.ActionFXByteBuddyEnhancer;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.validation.ValidationResult;
import com.github.actionfx.core.view.View;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Central handler for working with ActionFX. It provides routines to get views and controllers.
 * <p>
 * As ActionFX uses an internal bean container with dependency injection support, it is recommended to wire all
 * controllers with {@code @Inject} instead of accessing them through this class (please note that there is also support
 * of Spring's bean container through ActionFX's {@code actionfx-spring-boot} module).
 * <p>
 * <b>Using ActionFX:</b>
 * <p>
 * Before using this class, it needs to be setup in the {@code main()} or {@link Application#init()} method for the
 * specific application.
 * <p>
 * <b>Expample:</b>
 *
 * <pre>
 * ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();
 * </pre>
 *
 * After the setup, an instance of this class is retrieved throughout the application via:
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
 * This will populate ActionFX internal bean container and makes controller and views available for usage. Please note,
 * that this step is not required, if you use Spring Boot and included module {@code afx-spring-boot} in your
 * application classpath. The {@code afx-spring-boot} provides auto-configuration and scans for ActionFX components with
 * its Spring bean container during start-up.
 * <p>
 * In case you need access to the ActionFX instance in a controller, you can alternatively inject the ActionFX instance
 * via {@link Inject}. This might be helpful for unit testing your controller independently of a setup ActionFX instance
 * (you can use e.g. Mockito to mock the {@link ActionFX} instance for unit-testing).
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
    protected ActionFXExtensionsBean actionFXExtensionsBean;

    // the global validation mode to be applied to controls, in case these
    // carry validation-related annotations. in that case, annotation do not need
    // to mention a validation mode
    protected ValidationMode validationGlobalMode;

    // shall validation decorations be applied to scenegraph nodes in case of validation errors?
    protected boolean validationApplyResultDecoration;

    // shall decorations be applied to required fields in the scenegraph?
    protected boolean validationApplyRequiredDecoration;

    // what is the timeout in milliseconds ActionFX shall wait before starting to trigger a validation?
    protected int validationStartTimeoutMs;

    /**
     * Internal constructor. Use {@link #builder()} method to create your application-specific instance of
     * {@link ActionFX}.
     */
    @SuppressFBWarnings(justification = "Design Decision")
    ActionFX() {
        instance = this; // NOSONAR
    }

    /**
     * Creates a builder that is used for setting up the application-specific instance of {@link ActionFX}.
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
     * Gets an instance of {@link ActionFX}. Before calling this method, you need to setup your application-specific
     * configuration that ActionFX shall handle by using the {@link #builder()} method, returning an instance of
     * {@link ActionFXBuilder}.
     *
     * @return the built ActionFX instance
     */
    public static ActionFX getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ActionFX instance has not been built yet. Call ActionFX.build() first!");
        }
        return instance;
    }

    /**
     * Returns {@code true}, in case the ActionFX instance is available and the state is either
     * {@link ActionFXState#INITIALIZED} or {@link ActionFXState#CONFIGURED} , {@code false}, if the instance is not yet
     * setup or the state is {@link ActionFXState#UNINITIALIZED}.
     *
     * @return {@code true}, in case the ActionFX instance is properly available and setup for use, {@code false}
     *         otherwise
     */
    public static boolean isConfigured() {
        return instance != null
                && (actionFXState == ActionFXState.INITIALIZED || actionFXState == ActionFXState.CONFIGURED);
    }

    /**
     * Returns {@code true}, in case the ActionFX instance is available and the state is
     * {@link ActionFXState#INITIALIZED} , {@code false}, if the instance is not yet setup or the state is
     * {@link ActionFXState#UNINITIALIZED} or {@link ActionFXState#CONFIGURED}
     *
     * @return {@code true}, in case the ActionFX instance is properly available and initialized, {@code false}
     *         otherwise
     */
    public static boolean isInitialized() {
        return instance != null && actionFXState == ActionFXState.INITIALIZED;
    }

    /**
     * Scans for ActionFX components, depending on the configured {@link #getScanPackage()}.
     * <p>
     * This method requires that the JavaFX thread is up-and-running, as certain components (e.g.
     * {@link javafx.scene.web.WebView}) can only be instantiated within the JavaFX thread.
     */
    public void scanForActionFXComponents() {
        checkActionFXState(ActionFXState.CONFIGURED);
        scanForActionFXComponentsInternal();
    }

    /**
     * Internal component scanning routine.
     */
    private void scanForActionFXComponentsInternal() {
        // let's register some ActionFX-specific beans in the container before we do the
        // component scan
        beanContainer.addActionFXBeans(this);

        // let's let the bean container implementation do the work
        if (StringUtils.isNotEmpty(scanPackage)) {
            beanContainer.runComponentScan(scanPackage);
        }
        actionFXState = ActionFXState.INITIALIZED;// NOSONAR
    }

    /**
     * Adds a new controller to ActionFX' internal bean container. It is expected that the controller class is annotated
     * by {@link AFXController}.
     * <p>
     * Please prefer scanning for controller classes via {@link #scanForActionFXComponents()} and a set
     * {@code scanPackage} in the internal {@link ActionFXBuilder}.
     * <p>
     * Additionally, please keep in mind that you need to take care for controller dependencies on your own, i.e. if you
     * inject a controller B into controller A, you need to add the controller class for B before you add the controller
     * class for A.
     *
     * @param controllerClass
     *            the controller bean class
     */
    public void addController(final Class<?> controllerClass) {
        beanContainer.addControllerBeanDefinition(controllerClass);
    }

    /**
     * Gets the view by the supplied {@code viewId}.
     *
     * @param viewId
     *            the view ID
     * @return the view instance. If the {@code viewId} does not exists, an {@code IllegalArgumentException} is thrown.
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
     * @param beanClass
     *            the bean class for that an instance shall be retrieved.
     * @return the retrieved controller instance.If the {@code controllerClass} does not exists, {@code null} is
     *         returned.
     */
    public <T> T getBean(final Class<T> beanClass) {
        return beanContainer.getBean(beanClass);
    }

    /**
     * Gets the controller by the supplied {@code controllerId}.
     *
     * @param beanId
     *            the bean ID for that an instance shall be retrieved.
     * @return the retrieved controller instance.If the {@code controllerId} does not exists, {@code null} is returned.
     */
    public <T> T getBean(final String beanId) {
        return beanContainer.getBean(beanId);
    }

    /**
     * Gets the {@link ResourceBundle} that is used internationalization of the view associated with the controller
     * identified by {@code controllerId}.
     * <p>
     * The locale is retrieved from the {@link #getObservableLocale()} that has been configured with this instance of
     * {@link ActionFX}.
     *
     * @param controllerId
     *            the ID of the controller
     * @return the resource bundle, or {@code null}, in case there is no resource bundle associated with the given
     *         controller
     */
    public ResourceBundle getControllerResourceBundle(final String controllerId) {
        final Object controller = getBean(controllerId);
        if (controller == null) {
            return null;
        }
        return getControllerResourceBundle(controller.getClass());
    }

    /**
     * Gets the {@link ResourceBundle} that is used internationalization of the view associated with the given
     * {@code controllerClass}.
     * <p>
     * The locale is retrieved from the {@link #getObservableLocale()} that has been configured with this instance of
     * {@link ActionFX}.
     *
     * @param controllerClass
     *            the controller class
     * @return the resource bundle, or {@code null}, in case there is no resource bundle associated with the given
     *         controller
     */
    public ResourceBundle getControllerResourceBundle(final Class<?> controllerClass) {
        return beanContainer.resolveResourceBundle(controllerClass, observableLocale.getValue());
    }

    /**
     * Gets an internationalized message from an underlying resource bundle that is registered for the supplied
     * {@code controllerClass}.
     * <p>
     * In case the {@code messageKey} can not be resolved with the looked up resource bundle, the {@code defaultMessage}
     * will be returned.
     *
     * @param controllerClass
     *            the controller class for that a resource bundle was defined
     * @param messageKey
     *            the message key to look up
     * @param defaultMessage
     *            the default message that is returned in case the look up for the supplied {@code messageKey} fails
     * @return the internationalized message
     */
    public String getMessage(final Class<?> controllerClass, final String messageKey, final String defaultMessage) {
        final ResourceBundle resourceBundle = getControllerResourceBundle(controllerClass);
        if (resourceBundle == null || StringUtils.isBlank(messageKey)) {
            return defaultMessage;
        }
        try {
            return resourceBundle.getString(messageKey);
        } catch (final MissingResourceException e) {
            return defaultMessage;
        }
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
     * The ID / name of the main view that is displayed in JavaFX's primary {@link Stage}.
     *
     * @return the main view ID
     */
    public String getMainViewId() {
        return mainViewId;
    }

    /**
     * Returns the {@link View} associated with the supplied {@code controller}.
     *
     * @param controller
     *            the controller for that the view shall be retrieved
     * @return the view associated with the supplied controller
     */
    public View getView(final Object controller) {
        return ControllerWrapper.of(controller).getView();
    }

    /**
     * Hides the view associated with the supplied {@code controller}. This method is useful for closing (modal) dialogs
     * from its controller.
     *
     * @param controller
     *            the controller thats view shall be hidden
     */
    public void hideView(final Object controller) {
        getView(controller).hide();
    }

    /**
     * Displays the main view in the primary stage.
     *
     * @param primaryStage
     *            the primary stage
     */
    public void showMainView(final Stage primaryStage) {
        setPrimaryStage(primaryStage);
        final View view = getMainView();
        view.show(primaryStage);
    }

    /**
     * Shows the view of the supplied {@code controller}.
     *
     * @param controller
     *            the controller thats view shall be shown
     */
    public void showView(final Object controller) {
        getView(controller).show();
    }

    /**
     * Shows the view of identified by the supplied {@code viewId}.
     *
     * @param viewId
     *            that viewId identifying the view to show
     */
    public void showView(final String viewId) {
        getView(viewId).show();
    }

    /**
     * Shows the view of the supplied {@code controller} inside the given {@link Stage}.
     *
     * @param controller
     *            the controller thats view shall be shown
     * @param stage
     *            the stage where the view shall be displayed inside
     */
    public void showView(final Object controller, final Stage stage) {
        getView(controller).show(stage);
    }

    /**
     * Shows the view identified by the supplied {@code viewId} inside the given {@link Stage}.
     *
     * @param viewId
     *            that viewId identifying the view to show
     * @param stage
     *            the stage where the view shall be displayed inside
     */
    public void showView(final String viewId, final Stage stage) {
        getView(viewId).show(stage);
    }

    /**
     * Shows the view of the supplied {@code controller} in a modal dialog.
     *
     * @param controller
     *            the controller thats view shall be shown
     */
    public void showViewAndWait(final Object controller) {
        getView(controller).showAndWait();
    }

    /**
     * Shows the view identified by the supplied {@code viewId} in a modal dialog.
     *
     * @param viewId
     *            that viewId identifying the view to show
     */
    public void showViewAndWait(final String viewId) {
        getView(viewId).showAndWait();
    }

    /**
     * Performs an dock operation for a nested view injected into a controller via
     * {@link com.github.actionfx.core.annotation.AFXNestedView}, in case the nested view is undocked.
     *
     * @param nestedViewId
     *            the view ID of the nested view (must be injected into a view via
     *            {@link com.github.actionfx.core.annotation.AFXNestedView}).
     */
    public void dockNestedView(final String nestedViewId) {
        if (isNestedViewDocked(nestedViewId)) {
            return;
        }
        final View view = getView(nestedViewId);
        view.reattachView();
    }

    /**
     * Performs an undock operation for a nested view injected into a controller via
     * {@link com.github.actionfx.core.annotation.AFXNestedView}, in case the nested view is docked.
     *
     * @param nestedViewId
     *            the view ID of the nested view (must be injected into a view via
     *            {@link com.github.actionfx.core.annotation.AFXNestedView}).
     */
    public void undockNestedView(final String nestedViewId) {
        if (!isNestedViewDocked(nestedViewId)) {
            return;
        }
        final View view = getView(nestedViewId);
        view.detachView();
        view.show(new Stage());
    }

    /**
     * Determines whether the nested view identfied by {@code nestedViewId} is currently docked into a parent
     * scenegraph.
     *
     * @param nestedViewId
     *            the view ID of the nested view (must be injected into a view via
     *            {@link com.github.actionfx.core.annotation.AFXNestedView}).
     * @return {@code true}, if the given nested view is currently docked into the parent scene graph, {@code false}, if
     *         the view is displayed undocked in its own stage.
     */
    public boolean isNestedViewDocked(final String nestedViewId) {
        final View view = getView(nestedViewId);
        final Parent node = view.getRootNode();
        return node.getParent() != null;
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
     * Gets the custom ActionFX extensions (controller and bean definition extensions)
     *
     * @return the bean holding custom ActionFX extensions
     */
    public ActionFXExtensionsBean getActionFXExtensionsBean() {
        return actionFXExtensionsBean;
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
     * Displays a modal confirmation dialogue with the specified {@code title} and {@code message}.
     *
     * @param title
     *            the title of the dialog
     * @param headerText
     *            the header text to be displayed in the dialog
     * @param contentText
     *            the content text to be displayed in the dialog
     * @return {@code true}, when the OK button has been pressed, {@code false} otherwise.
     */
    public boolean showConfirmationDialog(final String title, final String headerText, final String contentText) {
        return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME))
                .showConfirmationDialog(title, headerText, contentText);
    }

    /**
     * Displays a modal warning dialogue with the specified {@code title} and {@code message}.
     *
     * @param title
     *            the title of the dialog
     * @param headerText
     *            the header text to be displayed in the dialog
     * @param contentText
     *            the content text to be displayed in the dialog
     */
    public void showWarningDialog(final String title, final String headerText, final String contentText) {
        ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showWarningDialog(title,
                headerText, contentText);
    }

    /**
     * Displays a modal information dialogue with the specified {@code title} and {@code message}.
     *
     * @param title
     *            the title of the dialog
     * @param headerText
     *            the header text to be displayed in the dialog
     * @param contentText
     *            the content text to be displayed in the dialog
     */
    public void showInformationDialog(final String title, final String headerText, final String contentText) {
        ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showInformationDialog(title,
                headerText, contentText);
    }

    /**
     * Displays a modal error dialogue with the specified {@code title} and {@code message}.
     *
     * @param title
     *            the title of the dialog
     * @param headerText
     *            the header text to be displayed in the dialog
     * @param contentText
     *            the content text to be displayed in the dialog
     */
    public void showErrorDialog(final String title, final String headerText, final String contentText) {
        ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showErrorDialog(title, headerText,
                contentText);
    }

    /**
     * Displays a directory chooser and returns the selected {@link File} descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param owner
     *            the window owner
     * @return the selected directory, or {@code null}, if no directory has been selected
     */
    public File showDirectoryChooserDialog(final String title, final File defaultDirectory, final Window owner) {
        return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME))
                .showDirectoryChooserDialog(title, defaultDirectory, owner);
    }

    /**
     * Displays a file chooser and returns the selected <tt>File</tt> descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param initialFileName
     *            the initially suggested file name
     * @param fileExtensionFilter
     *            an optional file extension filter
     * @param owner
     *            the window owner
     * @return the selected file, or {@code null} if no file has been selected
     */
    public File showFileOpenDialog(final String title, final File defaultDirectory, final String initialFileName,
            final ExtensionFilter fileExtensionFilter, final Window owner) {
        return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showFileOpenDialog(title,
                defaultDirectory, initialFileName, fileExtensionFilter, owner);
    }

    /**
     * Displays a file chooser and returns the selected {@link File} descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param owner
     *            the window owner
     * @return the selected file, or {@code null}, if no file has been selected
     */
    public File showFileOpenDialog(final String title, final File defaultDirectory, final Window owner) {
        return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showFileOpenDialog(title,
                defaultDirectory, owner);
    }

    /**
     * Displays a file chooser and returns the selected {@link File} descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param owner
     *            the window owner
     * @return the selected file, or {@code null}, if no file has been selected
     */
    public File showFileSaveDialog(final String title, final File defaultDirectory, final Window owner) {
        return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showFileSaveDialog(title,
                defaultDirectory, owner);
    }

    /**
     * Displays a file chooser and returns the selected {@link File} descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param initialFileName
     *            the initially suggested file name
     * @param fileExtensionFilter
     *            an optional file extension filter
     * @param owner
     *            the window owner
     * @return the selected file, or {@code null}, if no file has been selected
     */
    public File showFileSaveDialog(final String title, final File defaultDirectory, final String initialFileName,
            final ExtensionFilter fileExtensionFilter, final Window owner) {
        return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showFileSaveDialog(title,
                defaultDirectory, initialFileName, fileExtensionFilter, owner);
    }

    /**
     * Displays a modal <tt>TextInputDialog</tt> that lets the user enter a single string value.
     *
     * @param title
     *            the title of the input dialog
     * @param headerText
     *            a header text to be displayed inside the dialogue
     * @param contentText
     *            a content text displayed in front of the input text field
     * @return the entered string value, or <tt>null</tt>, if no value has been entered.
     */
    public String showTextInputDialog(final String title, final String headerText, final String contentText) {
        return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showTextInputDialog(title,
                headerText, contentText);
    }

    /**
     * Displays a modal <tt>TextInputDialog</tt> that lets the user enter a single string value. A default text is
     * already pre-set.
     *
     * @param title
     *            the title of the input dialog
     * @param headerText
     *            a header text to be displayed inside the dialogue
     * @param contentText
     *            a content text displayed in front of the input text field
     * @param defaultValue
     *            the pre-set default text
     * @return the entered string value, or <tt>null</tt>, if no value has been entered.
     */
    public String showTextInputDialog(final String title, final String headerText, final String contentText,
            final String defaultValue) {
        return ((DialogController) getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)).showTextInputDialog(title,
                headerText, contentText, defaultValue);
    }

    /**
     * Publishes the given {@code event} to all methods that are annotated by {@link AFXSubscribe} and which are
     * listening to the type of {@code event}.
     * <p>
     * In case the annotated method has a method argument that is of the same type than {@code event}, then the
     * {@code event} is used as method argument to that method.
     * <p>
     * Please note that methods annotated by {@link AFXSubscribe} can also have additional method argument, that are
     * e.g. annotated by {@link AFXControlValue}.
     *
     * @param event
     *            the event to publish
     */
    public void publishEvent(final Object event) {
        getEventBus().publish(event);
    }

    /**
     * Performs a validation on controls inside the given {@code controller}.
     * <p>
     * This requires that {@link com.github.actionfx.core.validation.Validator} instances are registered inside the
     * {@link View} associated with the given {@code controller}, or that the {@code controller} instance itself carries
     * validation related annotations like {@link com.github.actionfx.core.annotation.AFXValidateRequired} or
     * {@link com.github.actionfx.core.annotation.AFXValidateRegExp}.
     *
     * @param controller
     *            the controller referencing the JavaFX controls to validate
     * @param applyValidationDecoration
     *            indicates whether validation errors shall be displayed as decorations, when there is a validation
     *            failure
     * @return {@code true}, if all validations passed successfully, {@code false}, if there are validation errors found
     *         in the annotated controls.
     */
    public ValidationResult validate(final Object controller, final boolean applyValidationDecoration) {
        final View view = getView(controller);
        return view.validate(applyValidationDecoration);
    }

    /**
     * Performs a validation on controls inside the given {@code controller}.
     * <p>
     * This requires that {@link com.github.actionfx.core.validation.Validator} instances are registered inside the
     * {@link View} associated with the given {@code controller}, or that the {@code controller} instance itself carries
     * validation related annotations like {@link com.github.actionfx.core.annotation.AFXValidateRequired} or
     * {@link com.github.actionfx.core.annotation.AFXValidateRegExp}.
     * <p>
     * This method applies validation decorations by default. If you don't want to display validation decoration and
     * want to handle validation messages by yourself, please use {@link #validate(Object, boolean)}.
     *
     * @param controller
     *            the controller referencing the JavaFX controls to validate
     * @return {@code true}, if all validations passed successfully, {@code false}, if there are validation errors found
     *         in the annotated controls.
     */
    public ValidationResult validate(final Object controller) {
        return validate(controller, true);
    }

    /**
     * The package name with dot-notation "." that shall be scanned for ActionFX components.
     *
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
    public BeanContainerFacade getBeanContainer() {
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
     * {@link ActionFXBuilder#observableLocale(ObservableValue)}, the default implementation will return the
     * {@link java.util.Locale#getDefault()} locale.
     *
     * @return the observable locale
     */
    public ObservableValue<Locale> getObservableLocale() {
        return observableLocale;
    }

    /**
     * Gets the global validation mode, if specified. When specifying a global validation mode as part of the setup
     * phase of ActionFX, this global validation mode makes the specification of a particular validation mode inside a
     * validation-related annotation like {@link com.github.actionfx.core.annotation.AFXValidateRequired} obsolete,
     * reducing the number of attributes to be specified in these type of annotations.
     *
     * @return the global validation mode configured for ActionFX.
     */
    public ValidationMode getValidationGlobalMode() {
        return validationGlobalMode;
    }

    /**
     * Flag that indicates, whether validation decorations for validation results shall be displayed for controls under
     * validation.
     *
     * @return {@code true}, if validation decorations shall be applied to controls under validation, {@code false}
     *         otherwise.
     */
    public boolean isValidationApplyResultDecoration() {
        return validationApplyResultDecoration;
    }

    /**
     * Flag that indicates, whether decorations for required fields shall be displayed for controls under validation.
     *
     * @return {@code true}, if validation decorations for required fields shall be applied to controls, {@code false}
     *         otherwise.
     */
    public boolean isValidationApplyRequiredDecoration() {
        return validationApplyRequiredDecoration;
    }

    /**
     * A global timeout setting for staring a control validation after a change in a particular control occurs. If the
     * returned value is {@code -1}, there is no global timeout setting and the timeout value needs to be defined in all
     * validation related annotations directly (this might make more sense in many cases).
     *
     * @return the validation start timeout value in milliseconds, or {@code -1}, if there is no global setting for the
     *         validation start timeout.
     */
    public int getValidationStartTimeoutMs() {
        return validationStartTimeoutMs;
    }

    /**
     * Checks, whether ActionFX is currently in {@code expectedState}. If ActionFX's state is different from the
     * expected state, an {@link IllegalStateException} is thrown.
     *
     * @param expectedState
     *            the expected state to check.
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
     * Users can set the primary stage here so that it is available throughout the application.
     *
     * @param primaryStage
     *            the primary stage
     */
    public void setPrimaryStage(final Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Resets ActionFX to its initial state. Can be used in unit test. Use this method in productive code only when you
     * know exactly what you are doing.
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

        private final List<BeanExtension> beanExtensions = new ArrayList<>();

        private BeanContainerFacade beanContainer;

        private boolean enableBeanContainerAutodetection = true;

        private ValidationMode validationGlobalMode;

        protected boolean validationApplyResultDecoration = true;

        protected boolean validationApplyRequiredDecoration = true;

        private int validationStartTimeoutMs = -1;

        /**
         * Creates the instance of {@link ActionFX} ready to use.
         *
         * @return the ActionFX instance (which is a singleton from then on)
         */
        public ActionFX build() {
            final ActionFX actionFX = new ActionFX();
            actionFX.actionFXExtensionsBean = new ActionFXExtensionsBean(controllerExtensions, beanExtensions);
            initializeBeanContainer(actionFX);
            actionFX.mainViewId = mainViewId;
            actionFX.scanPackage = scanPackage;
            actionFX.enhancementStrategy = enhancementStrategy != null ? enhancementStrategy
                    : EnhancementStrategy.SUBCLASSING;
            actionFX.enhancer = actionFXEnhancer != null ? actionFXEnhancer : new ActionFXByteBuddyEnhancer();
            actionFX.uncaughtExceptionHandler = uncaughtExceptionHandler != null ? uncaughtExceptionHandler
                    : (thread, exception) -> LOG.error("[Thread {}] Uncaught exception", thread.getName(), exception);
            actionFX.observableLocale = observableLocale != null ? observableLocale
                    : new SimpleObjectProperty<>(Locale.getDefault());
            actionFX.validationGlobalMode = validationGlobalMode != null ? validationGlobalMode
                    : ValidationMode.GLOBAL_VALIDATION_MODE_UNSPECIFIED;
            actionFX.validationApplyResultDecoration = validationApplyResultDecoration;
            actionFX.validationApplyRequiredDecoration = validationApplyRequiredDecoration;
            actionFX.validationStartTimeoutMs = validationStartTimeoutMs;
            postConstruct(actionFX);
            return actionFX;
        }

        /**
         * Reads out the {@link AFXApplication} annotation that is expected to be present on the given
         * {@code configurationClass}. In case the annotation is not present on the given class (or on its
         * super-classes), an {@link IllegalArgumentException} is thrown.
         * <p>
         * Setting the {@link AFXApplication} annotation on a class is not mandatory. Instead, the configuration can be
         * also done with the builder methods {@link #mainViewId(String)}, {@link #scanPackage(String)}, etc.
         *
         * @param configurationClass
         *            the configuration class that is expected to have an {@link AFXApplication} annotation.
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
            enableBeanContainerAutodetection = afxApplication.enableBeanContainerAutodetection();
            validationGlobalMode = afxApplication.validationGlobalMode();
            validationApplyResultDecoration = afxApplication.validationApplyResultDecoration();
            validationApplyRequiredDecoration = afxApplication.validationApplyRequiredDecoration();
            validationStartTimeoutMs = afxApplication.validationStartTimeoutMs();
            return this;
        }

        /**
         * Sets the ID / name of the view that is used to be displayed in JavaFX's primary {@link Stage}. Please note
         * that this ID must of course exist inside ActionFX's container e.g. by annotating a controller with
         * {@link AFXController} and defining this view ID there.
         *
         * @param mainViewId
         *            the ID of the main view
         * @return this builder
         */
        public ActionFXBuilder mainViewId(final String mainViewId) {
            this.mainViewId = mainViewId;
            return this;
        }

        /**
         * The package name with dot-notation "." that shall be scanned for ActionFX components.
         *
         * @param scanPackage
         *            the package name that shall be scanned for ActionFX componets
         * @return this builder
         */
        public ActionFXBuilder scanPackage(final String scanPackage) {
            this.scanPackage = scanPackage;
            return this;
        }

        /**
         * The byte-code enhancement strategy to use within ActionFX. Currently the following enhancement strategies are
         * available:
         * <ul>
         * <li>{@link EnhancementStrategy#RUNTIME_INSTRUMENTATION_AGENT}: A byte-code instrumentation agent is
         * installed/attached at runtime. Methods of controller classes are directly enhanced via method
         * interceptors.</li>
         * <li>{@link EnhancementStrategy#SUBCLASSING}: Controller classes are sub-classed, while controller methods are
         * overriden and method interceptors are attached.</li>
         * </ul>
         *
         * @param enhancementStrategy
         *            the enhancement strategy to use
         * @return this builder
         */
        public ActionFXBuilder enhancementStrategy(final EnhancementStrategy enhancementStrategy) {
            this.enhancementStrategy = enhancementStrategy;
            return this;
        }

        /**
         * Sets the implementation of interface {@link ActionFXEnhancer} to use within ActionFX. In case there is no
         * instance set, the default enhancer {@link ActionFXByteBuddyEnhancer} is used.
         * <p>
         * Please note that implementations of interface {@link ActionFXEnhancer} must provide the possibility of both,
         * byte code instrumentation via a runtime agent and byte code enhancement via sub-classing.
         *
         * @param actionFXEnhancer
         *            the enhancer implementation to use
         * @return this builder
         */
        public ActionFXBuilder actionFXEnhancer(final ActionFXEnhancer actionFXEnhancer) {
            this.actionFXEnhancer = actionFXEnhancer;
            return this;
        }

        /**
         * Configures an exception handler for uncaught exceptions.
         *
         * @param uncaughtExceptionHandler
         *            the exception handler for uncaught exceptions
         * @return this builder
         */
        public ActionFXBuilder uncaughtExceptionHandler(final UncaughtExceptionHandler uncaughtExceptionHandler) {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
            return this;
        }

        /**
         * Configures an {@code ObservableValue} that holds a proper {@code java.util.Locale} for internationalization.
         *
         * @param observableLocale
         *            the observable locale
         * @return this builder
         */
        public ActionFXBuilder observableLocale(final ObservableValue<Locale> observableLocale) {
            this.observableLocale = observableLocale;
            return this;
        }

        /**
         * Configures an {@code Locale} for internationalization.
         *
         * @param locale
         *            the locale
         * @return this builder
         */
        public ActionFXBuilder locale(final Locale locale) {
            return observableLocale(new SimpleObjectProperty<>(locale));
        }

        /**
         * Registers custom controller extensions instances implemented by the user. Controller extensions are applied
         * to the controller after instantiation, after dependency injection, but before methods annotated with
         * {@code @PostConstruct} are invoked.
         *
         * @param extensions
         *            the controller extensions
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
         * Registers custom controller extensions implemented by the user. It is expected that supplied classes have a
         * default, no-argument constructor. Controller extensions are applied to the controller after instantiation,
         * after dependency injection, but before methods annotated with {@code @PostConstruct} are invoked.
         *
         * @param extensionClasses
         *            the controller extension classes
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
         * Registers custom bean extensions instances implemented by the user. Bean extensions are applied to the bean
         * definitions after these beans have been discovered by ActionFX during the component scan.
         *
         * @param extensions
         *            the bean definition extensions
         * @return this builder
         */
        public ActionFXBuilder beanExtension(final BeanExtension... extensions) {
            if (extensions.length > 0) {
                beanExtensions.addAll(Arrays.asList(extensions));
            }
            return this;
        }

        /**
         * Registers custom bean extensions instances implemented by the user. Bean extensions are applied to the bean
         * definitions after these beans have been discovered by ActionFX during the component scan.
         *
         * @param extensionClasses
         *            the bean definition extensions as classes array
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public ActionFXBuilder beanExtension(final Class<? extends BeanExtension>... extensionClasses) {
            final BeanExtension[] extensions = new BeanExtension[extensionClasses.length];
            if (extensionClasses.length > 0) {
                for (int i = 0; i < extensionClasses.length; i++) {
                    extensions[i] = ReflectionUtils.instantiateClass(extensionClasses[i]);
                }
            }
            return beanExtension(extensions);
        }

        /**
         * Defines the bean container instance to use for ActionFX. The container classes needs to implement the
         * {@link BeanContainerFacade} interface and need to provide routines for registering bean definitions are
         * retrieving bean instances (singleton, prototypes) from the underlying container.
         *
         * @param beanContainer
         *            the bean container
         * @return this builder
         */
        public ActionFXBuilder beanContainer(final BeanContainerFacade beanContainer) {
            this.beanContainer = beanContainer;
            return this;
        }

        /**
         * Defines the bean container as class to use for ActionFX. The container class needs to implement the
         * {@link BeanContainerFacade} interface and need to provide routines for registering bean definitions are
         * retrieving bean instances (singleton, prototypes) from the underlying container.
         * <p>
         * It is expected that the supplied class as a no-argument default constructor.
         *
         * @param beanContainerClass
         *            the bean container class
         * @return this builder
         */
        public ActionFXBuilder beanContainerClass(final Class<? extends BeanContainerFacade> beanContainerClass) {
            final BeanContainerFacade beanContainerInstance = ReflectionUtils.instantiateClass(beanContainerClass);
            return beanContainer(beanContainerInstance);
        }

        /**
         * Flag that determines whether ActionFX shall try to autodetect the bean container implementation to use.
         * <p>
         * Using autodetection together with directly setting the bean container via
         * {@link #beanContainer(BeanContainerFacade)} or {@link #beanContainerClass(Class)} is pointless. When using an
         * explicit bean container implementation, the autodetection is switched off
         * ({@code enableBeanContainerAutodetection(false)}).
         * <p>
         * The default is {@code true} (i.e. autodetection is enabled).
         *
         * @param enableAutoDetect
         *            flag that indicates whether autodetection shall be attempted or not. If enabled, it is checked
         *            whether a known bean container implementation is present on the classpath (e.g. the container
         *            implementation for Spring). If no container implementation is found on the classpath, ActionFX
         *            default bean container {@link DefaultActionFXBeanContainer} is used.
         * @return this builder
         */
        public ActionFXBuilder enableBeanContainerAutodetection(final boolean enableAutoDetect) {
            enableBeanContainerAutodetection = enableAutoDetect;
            return this;
        }

        /**
         * Specifies the global validation mode that shall be applied on JavaFX controls that carry an
         * validation-related annotation like {@link com.github.actionfx.core.annotation.AFXValidateRequired}. In case a
         * global validation mode is set via this builder, the annotations do not need to specify a validation mode
         * anymore. This is helpful for reducing the number of attributes in validation-related annotations and ActionFX
         * controllers.
         *
         * @param globalValidationMode
         *            the global validation mode
         * @return this builder
         */
        public ActionFXBuilder validationGlobalMode(final ValidationMode globalValidationMode) {
            validationGlobalMode = globalValidationMode;
            return this;
        }

        /**
         * Specifies the flag that indicates, whether validation decorations for validation results shall be applied to
         * controls under validation. Default is {@code true}.
         *
         * @param validationApplyResultDecoration
         *            flag indicating whether decorations for validated controls shall be displayed or not.
         * @return this builder
         */
        public ActionFXBuilder validationApplyResultDecoration(final boolean validationApplyResultDecoration) {
            this.validationApplyResultDecoration = validationApplyResultDecoration;
            return this;
        }

        /**
         * Specifies the flag that indicates, whether validation decorations for required fields shall be applied to
         * controls under validation. Decorations include marking required fields. Default is {@code true}.
         *
         * @param validationApplyRequiredDecoration
         *            flag indicating whether decorations for required fields shall be displayed or not.
         * @return this builder
         */
        public ActionFXBuilder validationApplyRequiredDecoration(final boolean validationApplyRequiredDecoration) {
            this.validationApplyRequiredDecoration = validationApplyRequiredDecoration;
            return this;
        }

        /**
         * A global timeout setting for staring a control validation after a change in a particular control occurs. If
         * the returned value is {@code -1}, there is no global timeout setting and the timeout value needs to be
         * defined in all validation related annotations directly (this might make more sense in many cases). Default is
         * {@code -1} (no global timeout).
         *
         * @param validationStartTimeoutMs
         *            the timeout in milliseconds after that the validations shall be executed on a control's value
         * @return this builder
         */
        public ActionFXBuilder validationStartTimeoutMs(final int validationStartTimeoutMs) {
            this.validationStartTimeoutMs = validationStartTimeoutMs;
            return this;
        }

        /**
         * Initializes the bean container to use for ActionFX.
         *
         * @param actionFX
         *            the actionFX instance
         */
        private void initializeBeanContainer(final ActionFX actionFX) {
            if (beanContainer != null) {
                actionFX.beanContainer = beanContainer;
            } else if (enableBeanContainerAutodetection) {
                actionFX.beanContainer = autodetectBeanContainer(actionFX);
            } else {
                actionFX.beanContainer = new DefaultActionFXBeanContainer(actionFX.actionFXExtensionsBean);
            }
        }

        /**
         * Performs an autodetection of the bean container to use for ActionFX.
         *
         * @param actionFX
         *            the actionFX instance
         * @return the bean container. In case no specialized container is found on the classpath, the default container
         *         of ActionFX is returned.
         */
        @SuppressWarnings("unchecked")
        private BeanContainerFacade autodetectBeanContainer(final ActionFX actionFX) {
            final String[] containerCandidates = { "com.github.actionfx.spring.container.SpringBeanContainer" };
            for (final String containerCandidate : containerCandidates) {
                final Class<?> containerClass = ReflectionUtils.resolveClassName(containerCandidate, null);
                if (containerClass != null && BeanContainerFacade.class.isAssignableFrom(containerClass)) {
                    return instantiateContainerClass((Class<? extends BeanContainerFacade>) containerClass,
                            actionFX.actionFXExtensionsBean);
                }
            }

            // no candidate found? we return ActionFX' default
            return new DefaultActionFXBeanContainer(actionFX.actionFXExtensionsBean);
        }

        /**
         * Instantiates the bean container. The method checks for the availability of a constructor accepting the
         * {@link ActionFXExtensionsBean}. If this constructor is not available, the default no-argument constructor is
         * invoked.
         *
         * @param containerClass
         *            the container class
         * @param actionFXExtensionsBean
         *            custom extensions to ActionFX
         * @return the created instance
         */
        private BeanContainerFacade instantiateContainerClass(final Class<? extends BeanContainerFacade> containerClass,
                final ActionFXExtensionsBean actionFXExtensionsBean) {
            final ConstructorBasedInstantiationSupplier<? extends BeanContainerFacade> instantiationSupplier = new ConstructorBasedInstantiationSupplier<>(
                    containerClass, actionFXExtensionsBean);
            return instantiationSupplier.get();
        }

        /**
         * Post construct routine that is executed by the {@link ActionFXBuilder} after the instance of {@link ActionFX}
         * has been created.
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
         * ActionFX instance is configured and built, but no component scan is performed.
         */
        CONFIGURED,

        /**
         * ActionFX component scan is performed and instance is fully ready for use.
         */
        INITIALIZED
    }

}
