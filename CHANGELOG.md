# Changelog
All notable changes to this project will be documented in this file.

## 1.5.0 (24.05.2022)

### New Features
- Introduced new module "actionfx-appfactory" with JavaFX app to generate new ActionFX projects and generate code for ActionFX controller based on existing FXML
- Support for a basic publish/subscribe mechanism for ActionFX controller methods using @AFXSubscribe
- Refactored usage of ActionFX' bean container - Method "ActionFX.scanForActionFXComponents()" no longer accepts a bean container instance.
- Added autodetection for bean container to use - in case the "actionfx-spring-boot" module is on the classpath, ActionFX' "SpringBeanContainer" is automatically used (except autodetection is disabled via configuration or the "AFXApplication" annotation).

### Bugfixes 
- Fixed life-cycle bug for ActionFX controller in Spring (lazy initialization was not possible with Spring)
- Fixed children lookup bug for Accordion (which does not have the "javafx.beans.DefaultProperty" annotation for some reason)
- Fixed file extension filter issue in open file / save file dialogue: Extensions were not displayed.

### Technical Upgrades
- Upgraded build system to Gradle 7.4.2
- Added Gitpod development support via own Dockerfile
- Updated dependencies:
  * JavaFX 17.0.1
  * Spring Boot to 2.6.7
  * Spring to 5.3.20
 
## 1.4.0 (12.09.2021)
- Support for mapping a custom domain object to form controls via @AFXFormBinding and @AFXFormMapping.
- Added convenience methods for accessing the view from a controller to class "ActionFX" (showView, showViewAndWait, hideView)
- Extended annotation @AFXControlValue to specify the `sourceProperty` from where the value shall be taken and `formatPattern` for type conversions to floating point numbers or dates
- Updated dependencies:
  * ByteBuddy to 1.11.5
  * New dependency commons-beanutils 1.9.4
  
## 1.3.0 (16.06.2021)
- Added attribute 'filterPredicateProperty' to annotation @AFXUseFilteredList in order to bind a predicate to the filtered list directly
- Extended ControllerWrapper class to retrieve the java.util.ResourceBundle from the controller instance

## 1.2.0 (12.06.2021)
- Annotation @AFXOnAction now supports asynchronous method invocation via attribute 'async'.
- Support for working with simple dialogs like confirmation-, information-, warning-, error-, file handling- and text input dialogs.
- Added annotation @AFXRequiresUserConfirmation: Methods are only executed after the user confirms via a confirmation dialog.
- Added method argument annotations @AFXFromFileOpenDialog, @AFXFromFileSaveDialog, @AFXFromFileChooserDialog and @AFXFromTextInputDialog to request values from the user via dialogs.
- Changed @AFXOnAction#controlId attribute to @AFXOnAction#nodeId as also regular nodes (not controls) can have an "onAction" property (e.g. "MenuItem").
- Added field annotations @AFXEnableNode and @AFXDisableNode to control a node's disabledProperty() depending on other control values
- Added new sample application "TextEditor"

## 1.1.0 (01.05.2021)
- Extended AFXCellValueConfig annotation by 'editable' attribute for making cells in TableView, TreeTableView, TreeView and ListView editable by the user

## 1.0.0 (10.04.2021)
- ActionFX' annotation-based features are implemented as "controller extensions"
- Users can add controller extension when building the ActionFX instance at startup.
- Statically coded, non-FXML views are now supported by controllers via the @AFXController.viewClass() attribute.
- Added support for configuring cell value factories via annotation @AFXCellValueConfig.
- Added support for adding converter to components via annotation @AFXConverter.
- Upgraded dependencies:
  * JavaFX to version 16 (minor versions starting from 11 are also possible)
  * Classgraph to version 4.8.104
  
## 0.1.0 (19.03.2021)
- Added support for internationalization (resource bundles are used for actionfx-core and Spring's org.springframework.context.MessageSource can be used in Spring environments as alternative)#
- Default byte-code enhancement strategy is SUBCLASSING

## 0.0.2 (10.01.2021)
- Renamed library modules from "afx-" to "actionfx-" (e.g. "afx-core" to "actionfx-core".
- Support for wrapping controls in order to uniquely access e.g. the "user value".
- Added further annotations for ActionFX controllers: @AFXArgHint, @AFXControlValue, @AFXEnableMultiSelection, @AFXLoadControlData. @AFXOnAction, @AFXOnControlValueChange
- Added support for asynchronous method invocations (also used from the above mentioned annotations)
- Updated documentation

## 0.0.1 (23.12.2020)
- Initial project setup
