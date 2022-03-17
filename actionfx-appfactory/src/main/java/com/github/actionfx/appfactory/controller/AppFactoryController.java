package com.github.actionfx.appfactory.controller;

import com.github.actionfx.appfactory.config.ControllerFactoryConfig;
import com.github.actionfx.appfactory.config.MainAppFactoryConfig;
import com.github.actionfx.appfactory.factories.MainAppFactory;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableNode;
import com.github.actionfx.core.annotation.AFXFromDirectoryChooserDialog;
import com.github.actionfx.core.annotation.AFXFromFileOpenDialog;
import com.github.actionfx.core.annotation.AFXOnAction;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * ActionFX controller for FXML view "/fxml/AppFactoryView.fxml"
 *
 * @author actionfx-appfactory
 *
 */
@AFXController(viewId = "appFactoryView", fxml = "/fxml/AppFactoryView.fxml", title = "ActionFX AppFactory", width = 600, height = 640)
public class AppFactoryController {

	@AFXEnableNode(whenAllContolsHaveUserValues = { "groupIdTextField", "projectNameTextField",
			"rootDirectoryTextField", "rootPackageNameTextField" })
	@FXML
	protected Button createNewProjectButton;

	@FXML
	protected Button createSourcesButton;

	@FXML
	protected RadioButton emptyViewRadioButton;

	@FXML
	protected RadioButton existingFxmlRadioButton;

	@FXML
	protected TextField groupIdTextField;

	@FXML
	protected CheckBox includeIconsCheckBox;

	@FXML
	protected ToggleGroup mainViewToggleGroup;

	@FXML
	protected TextField projectNameTextField;

	@FXML
	protected TextField rootDirectoryTextField;

	@FXML
	protected TextField rootPackageNameTextField;

	@FXML
	protected Button rootProjectDirectoryButton;

	@FXML
	protected CheckBox useSpringCheckBox;

	@FXML
	protected TextField projectControllerPackageNameTextField;

	@FXML
	protected TextField projectFxmlTextField;

	@FXML
	protected TextField fxmlTextField;

	@FXML
	protected Button loadFxmlButton;

	@FXML
	protected TextField javaSourcesRootTextField;

	@FXML
	protected TextField classpathResourceRootTextField;

	@FXML
	protected TextField controllerPackageNameTextField;

	@AFXOnAction(nodeId = "createNewProjectButton", async = false)
	public void createNewProjectButtonAction(final ActionEvent event) {
		final MainAppFactoryConfig config = new MainAppFactoryConfig();
		config.setAbsoluteProjectRootDirectory(rootDirectoryTextField.getText());
		config.setGroupId(groupIdTextField.getText());
		config.setName(projectNameTextField.getText());
		config.setRootPackageName(rootPackageNameTextField.getText());
		final MainAppFactory appFactory = new MainAppFactory(config);

	}

	@AFXOnAction(nodeId = "createSourcesButton", async = false)
	public void createSourcesButtonAction(final ActionEvent event) {
		final ControllerFactoryConfig config = new ControllerFactoryConfig();

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
		fxmlTextField.setText(fxmlFile);
	}

}
