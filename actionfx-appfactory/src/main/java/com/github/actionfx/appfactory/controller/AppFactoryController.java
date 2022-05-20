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
package com.github.actionfx.appfactory.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.actionfx.appfactory.config.ControllerFactoryConfig;
import com.github.actionfx.appfactory.config.MainAppFactoryConfig;
import com.github.actionfx.appfactory.factories.ControllerFactory;
import com.github.actionfx.appfactory.factories.MainAppFactory;
import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableNode;
import com.github.actionfx.core.annotation.AFXFormBinding;
import com.github.actionfx.core.annotation.AFXFormMapping;
import com.github.actionfx.core.annotation.AFXFromDirectoryChooserDialog;
import com.github.actionfx.core.annotation.AFXFromFileOpenDialog;
import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXOnControlValueChange;
import com.github.actionfx.core.utils.AFXUtils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * ActionFX controller for FXML view "/fxml/AppFactoryView.fxml"
 *
 * @author koster
 *
 */
@AFXController(viewId = "appFactoryView", fxml = "/fxml/AppFactoryView.fxml", title = "ActionFX AppFactory", width = 600, height = 640)
public class AppFactoryController {

    @FXML
    private Accordion mainAppAccordion;

    @AFXEnableNode(whenAllContolsHaveUserValues = { "groupIdTextField", "projectNameTextField",
            "rootDirectoryTextField", "mainAppClassNameTextField", "rootPackageNameTextField" })
    @FXML
    protected Button createNewProjectButton;

    @AFXEnableNode(whenAllContolsHaveUserValues = { "controllerFxmlTextField", "rootDirectoryControllerTextField",
            "javaSourcesRootTextField", "classpathResourceRootTextField", "controllerPackageNameTextField" })
    @FXML
    protected Button createControllerSourcesButton;

    @FXML
    protected TextField rootDirectoryTextField;

    @FXML
    protected Button rootProjectDirectoryButton;

    @FXML
    protected Button loadProjectFxmlButton;

    @FXML
    protected TextField projectFxmlTextField;

    @FXML
    protected RadioButton existingFxmlRadioButton;

    @FXML
    protected ComboBox<String> actionFXVersionComboBox;

    @FXML
    protected CheckBox useSpringCheckBox;

    @FXML
    protected TextField controllerFxmlTextField;

    @FXML
    protected TextField rootDirectoryControllerTextField;

    @FXML
    protected TextArea logTextArea;

    @Inject
    private ActionFX actionFX;

    @AFXFormBinding
    @AFXFormMapping(controlId = "rootDirectoryTextField", propertyName = "absoluteProjectRootDirectory")
    @AFXFormMapping(controlId = "groupIdTextField", propertyName = "groupId")
    @AFXFormMapping(controlId = "projectNameTextField", propertyName = "name")
    @AFXFormMapping(controlId = "rootPackageNameTextField", propertyName = "rootPackageName")
    @AFXFormMapping(controlId = "mainAppClassNameTextField", propertyName = "mainAppClassName")
    @AFXFormMapping(controlId = "actionFXVersionComboBox", propertyName = "actionFXVersion")
    @AFXFormMapping(controlId = "useSpringCheckBox", propertyName = "useSpring")
    @AFXFormMapping(controlId = "emptyViewRadioButton", propertyName = "createEmptyMainView")
    @AFXFormMapping(controlId = "existingFxmlRadioButton", propertyName = "useExistingFxmlFile")
    private ObjectProperty<MainAppFactoryConfig> mainAppFactoryConfigProperty = new SimpleObjectProperty<>(
            new MainAppFactoryConfig());

    @AFXFormBinding
    @AFXFormMapping(controlId = "rootDirectoryTextField", propertyName = "absoluteProjectRootDirectory")
    @AFXFormMapping(controlId = "projectFxmlTextField", propertyName = "absoluteFxmlFilePath")
    private ObjectProperty<ControllerFactoryConfig> mainAppControllerFactoryConfigProperty = new SimpleObjectProperty<>(
            new ControllerFactoryConfig());

    @AFXFormBinding
    @AFXFormMapping(controlId = "rootDirectoryControllerTextField", propertyName = "absoluteProjectRootDirectory")
    @AFXFormMapping(controlId = "controllerFxmlTextField", propertyName = "absoluteFxmlFilePath")
    @AFXFormMapping(controlId = "javaSourcesRootTextField", propertyName = "relativeJavaSourceDirectory")
    @AFXFormMapping(controlId = "classpathResourceRootTextField", propertyName = "relativeFXMLResourcesDirectory")
    @AFXFormMapping(controlId = "controllerPackageNameTextField", propertyName = "controllerPackageName")
    private ObjectProperty<ControllerFactoryConfig> standaloneControllerFactoryConfigProperty = new SimpleObjectProperty<>(
            new ControllerFactoryConfig());

    @PostConstruct
    public void initializeView() {
        mainAppAccordion.setExpandedPane(mainAppAccordion.getPanes().get(0));
    }

    @AFXLoadControlData(controlId = "actionFXVersionComboBox")
    public List<String> availableActionFXVersions() {
        return Arrays.asList(MainAppFactoryConfig.CURRENT_ACTIONFX_VERSION,
                MainAppFactoryConfig.PREVIOUS_ACTIONFX_VERSION);
    }

    @AFXOnControlValueChange(controlId = "existingFxmlRadioButton")
    public void onExistingFxmlRadioButtonChange(final boolean value) {
        loadProjectFxmlButton.setDisable(!value);
        projectFxmlTextField.setDisable(!value);
    }

    @AFXOnAction(nodeId = "createNewProjectButton", async = true)
    public void createNewProjectButtonAction() {
        final MainAppFactoryConfig mainAppFactoryConfig = mainAppFactoryConfigProperty.get();
        final ControllerFactoryConfig controllerFactoryConfig = mainAppControllerFactoryConfigProperty.get();
        final MainAppFactory appFactory = new MainAppFactory(mainAppFactoryConfig, controllerFactoryConfig,
                this::logMessage);
        try {
            appFactory.produce();
            AFXUtils.runInFxThread(
                    () -> actionFX.showInformationDialog(
                            "Success", "Successfully created project '" + mainAppFactoryConfig.getName()
                                    + "' in folder '" + mainAppFactoryConfig.getAbsoluteProjectRootDirectory() + "'!",
                            ""));
        } catch (final Exception e) {
            AFXUtils.runInFxThread(() -> actionFX.showErrorDialog("Error",
                    "Error generating project, error message: " + e.getMessage(), ""));
        }
    }

    @AFXOnAction(nodeId = "createControllerSourcesButton", async = true)
    public void createControllerSourcesButtonAction() {
        final ControllerFactoryConfig standaloneControllerConfig = standaloneControllerFactoryConfigProperty.get();
        final ControllerFactory controllerFactory = new ControllerFactory(standaloneControllerConfig, this::logMessage);
        try {
            controllerFactory.produce();
            AFXUtils.runInFxThread(
                    () -> actionFX.showInformationDialog("Success", "Successfully created controller in directory '"
                            + standaloneControllerConfig.getAbsoluteControllerDirectory() + "'!", ""));
        } catch (final Exception e) {
            AFXUtils.runInFxThread(() -> actionFX.showErrorDialog("Error",
                    "Error generating controller, error message: " + e.getMessage(), ""));
        }
    }

    @AFXOnAction(nodeId = "rootProjectDirectoryButton", async = false)
    public void rootProjectDirectoryButtonAction(
            @AFXFromDirectoryChooserDialog(title = "Choose Directory for Project") final String directory) {
        rootDirectoryTextField.setText(directory);
    }

    @AFXOnAction(nodeId = "loadProjectFxmlButton", async = false)
    public void loadProjectFxmlButtonAction(@AFXFromFileOpenDialog(title = "Choose FXML file", extensionFilter = {
            "JavaFX FXML file", "*.fxml" }) final String fxmlFile) {
        projectFxmlTextField.setText(fxmlFile);
    }

    @AFXOnAction(nodeId = "loadFxmlButton", async = false)
    public void loadFxmlButtonAction(@AFXFromFileOpenDialog(title = "Choose FXML file", extensionFilter = {
            "JavaFX FXML file", "*.fxml" }) final String fxmlFile) {
        controllerFxmlTextField.setText(fxmlFile);
    }

    @AFXOnAction(nodeId = "rootProjectDirectoryControllerButton", async = false)
    public void rootProjectDirectoryControllerButtonAction(
            @AFXFromDirectoryChooserDialog(title = "Choose Directory for Project") final String directory) {
        rootDirectoryControllerTextField.setText(directory);
    }

    private void logMessage(final String message) {
        AFXUtils.runInFxThread(() -> {
            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            logTextArea.appendText("[" + sdf.format(new Date()) + "] " + message + "\n");
        });
    }
}
