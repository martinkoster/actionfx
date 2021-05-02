# Changelog
All notable changes to this project will be documented in this file.

## 1.2.0 (02.05.2021)
- Annotation @AFXOnAction now supports asynchronous method invocation via attribute 'async'.

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
