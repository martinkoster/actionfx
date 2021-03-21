# Changelog
All notable changes to this project will be documented in this file.

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
