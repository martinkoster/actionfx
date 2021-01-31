# Module "actionfx-controlsfx"

This module contains an integration of the [ControlsFX](https://github.com/controlsfx/controlsfx) framework into ActionFX. 

Module | Description | API Documentation | Gradle Dependency 
------ | ----------- | ----------------- | ----------
[actionfx-controlsfx](README.md) | This module integrates the components and controls of [ControlsFX](https://github.com/controlsfx/controlsfx) into ActionFX. | [Javadoc](https://martinkoster.github.io/actionfx/actionfx-controls/index.html) | -

## Using ControlsFX in an ActionFX application

All ControlsFX controls are usable together with ActionFX's annotations. This is achieved by providing properties for ActionFX's [ControlWrapper](../actionfx-core/src/main/java/com/github/actionfx/core/view/graph/ControlWrapper.java).

The properties for ControlsFX' controls can be found [here](src/main/resources/afxcontrolwrapper).

Please note that ControlsFX `org.controlsfx.control.CheckModel` is wrapped with [CheckModelWrapper](src/main/java/com/github/actionfx/controlsfx/selection/CheckModelWrapper.java) so that ActionFX is able to access the `CheckModel` like a `javafx.scene.control.SelectionModel`.  