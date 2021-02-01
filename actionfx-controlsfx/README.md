# Module "actionfx-controlsfx"

This module contains an integration of the [ControlsFX](https://github.com/controlsfx/controlsfx) framework into ActionFX. 

Module | Description | API Documentation | Gradle Dependency 
------ | ----------- | ----------------- | ----------
[actionfx-controlsfx](README.md) | This module integrates the components and controls of [ControlsFX](https://github.com/controlsfx/controlsfx) into ActionFX. | [Javadoc](https://martinkoster.github.io/actionfx/actionfx-controls/index.html) | -

## Using ControlsFX in an ActionFX application

All ControlsFX controls are usable together with ActionFX's annotations. This is achieved by providing properties for ActionFX's [ControlWrapper](../actionfx-core/src/main/java/com/github/actionfx/core/view/graph/ControlWrapper.java).

The properties for ControlsFX' controls can be found [here](src/main/resources/afxcontrolwrapper).

Please note that ControlsFX `org.controlsfx.control.CheckModel` is wrapped with [CheckModelWrapper](src/main/java/com/github/actionfx/controlsfx/selection/CheckModelWrapper.java) so that ActionFX is able to access the `CheckModel` like a `javafx.scene.control.SelectionModel`.  

## User Values of ControlsFX controls

The following table explains, which property is accessed to retrieve the "user value" from:

JavaFX Control 								| User Value (as path to the corresponding property / observable list)
------------------------------------------- | -----------------------------------------------------
javafx.scene.control.Accordion 				| expandedPaneProperty()
javafx.scene.control.Button					| textProperty()
javafx.scene.control.ButtonBar				| (unsupported)
javafx.scene.control.CheckBox				| selectedProperty()
javafx.scene.control.ChoiceBox				| getSelectionModel().selectedItemProperty()
javafx.scene.control.ColorPicker			| valueProperty()
javafx.scene.control.ComboBox				| getSelectionModel().selectedItemProperty() (when user sets the "valueProperty", the selected item is set accordingly by JavaFX)
javafx.scene.control.DatePicker				| valueProperty()
javafx.scene.control.Hyperlink				| textProperty()
javafx.scene.control.Label					| textProperty()
javafx.scene.control.ListView				| getSelectionModel().selectedItemProperty() (for single-selection), getSelectionModel().getSelectedItems() (for multi-selection)
javafx.scene.control.MenuBar				| (unsupported)
javafx.scene.control.MenuButton				| textProperty()
javafx.scene.control.Pagination				| (unsupported)
javafx.scene.control.PasswordField			| textProperty()
javafx.scene.control.ProgressBar			| progressProperty()
javafx.scene.control.ProgressIndicator		| progressProperty()
javafx.scene.control.RadioButton			| selectedProperty()
javafx.scene.control.ScrollBar				| valueProperty()
javafx.scene.control.ScrollPane				| (unsupported)
javafx.scene.control.Separator				| (unsupported)
javafx.scene.control.Slider					| valueProperty()
javafx.scene.control.Spinner				| valueProperty()
javafx.scene.control.SplitMenuButton		| textProperty()
javafx.scene.control.SplitPane				| itemsProperty()
javafx.scene.control.TableView				| getSelectionModel().selectedItemProperty() (for single-selection), getSelectionModel().getSelectedItems() (for multi-selection)
javafx.scene.control.TabPane				| getSelectionModel().selectedItemProperty()
javafx.scene.control.TextArea				| textProperty()
javafx.scene.control.TextField				| textProperty()
javafx.scene.control.TitledPane				| textProperty()
javafx.scene.control.ToggleButton			| selectedProperty()
javafx.scene.control.ToolBar				| itemsProperty()
javafx.scene.control.TreeTableView			| getSelectionModel().selectedItemProperty() (for single-selection), getSelectionModel().getSelectedItems() (for multi-selection)
javafx.scene.control.TreeView				| getSelectionModel().selectedItemProperty() (for single-selection), getSelectionModel().getSelectedItems() (for multi-selection)