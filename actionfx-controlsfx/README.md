# Module "actionfx-controlsfx"

This module contains an integration of the [ControlsFX](https://github.com/controlsfx/controlsfx) framework into ActionFX. 

Module | Description | API Documentation 
------ | ----------- | ----------------- 
[actionfx-controlsfx](README.md) | This module integrates the components and controls of [ControlsFX](https://github.com/controlsfx/controlsfx) into ActionFX. | [Javadoc](https://martinkoster.github.io/actionfx/1.3.0/actionfx-controls/index.html) 

**Gradle Dependency**

```
implementation group: "com.github.martinkoster", name: "actionfx-controlsfx", version: "1.3.0"
```

**Maven Dependency**

```xml
<dependency>
    <groupId>com.github.martinkoster</groupId>
    <artifactId>actionfx-controlsfx</artifactId>
    <version>1.3.0</version>
</dependency>
```

## Using ControlsFX in an ActionFX application

All ControlsFX controls are usable together with ActionFX's annotations. This is achieved by providing properties for ActionFX's [ControlWrapper](../actionfx-core/src/main/java/com/github/actionfx/core/view/graph/ControlWrapper.java).

The properties for ControlsFX' controls can be found [here](src/main/resources/afxcontrolwrapper).

Please note that ControlsFX `org.controlsfx.control.CheckModel` is wrapped with [CheckModelWrapper](src/main/java/com/github/actionfx/controlsfx/selection/CheckModelWrapper.java) so that ActionFX is able to access the `CheckModel` like a `org.controlsfx.control.SelectionModel`.  

## User Values of ControlsFX controls

The following table explains, which property is accessed to retrieve the "user value" from (e.g. for annotations `@AFXControlValue`):

JavaFX Control 											| User Value (as path to the corresponding property / observable list)
------------------------------------------------------- | -----------------------------------------------------
org.controlsfx.control.BreadCrumbBar.BreadCrumbButton 	| textProperty()
org.controlsfx.control.BreadCrumbBar					| selectedCrumbProperty()
org.controlsfx.control.CheckComboBox					| getCheckModel().getCheckedItems()
org.controlsfx.control.CheckListView					| getCheckModel().getCheckedItems()
org.controlsfx.control.CheckTreeView					| getCheckModel().getCheckedItems()
org.controlsfx.control.GridView							| itemsProperty()
org.controlsfx.control.HiddenSidesPane					| (unsupported)
org.controlsfx.control.HyperlinkLabel					| textProperty()
org.controlsfx.control.InfoOverlay						| textProperty()
org.controlsfx.control.ListActionView					| textProperty()
org.controlsfx.control.ListSelectionView				| itemsProperty()
org.controlsfx.control.MaskerPane						| progressProperty()
org.controlsfx.control.MasterDetailPane					| (unsupported)
org.controlsfx.control.NotificationPane					| textProperty()
org.controlsfx.control.PlusMinusSlider					| valueProperty()
org.controlsfx.control.PrefixSelectionChoiceBox			| getSelectionModel().getSelectedItems()
org.controlsfx.control.PrefixSelectionComboBox			| getSelectionModel().getSelectedItems()
org.controlsfx.control.PropertySheet					| itemsProperty()
org.controlsfx.control.RangeSlider						| lowValueProperty(), highValueProperty() (as an observable list, with the 2 properties "lowValue" and "highValue")
org.controlsfx.control.Rating							| ratingProperty()
org.controlsfx.control.SearchableComboBox				| getSelectionModel().getSelectedItems()
org.controlsfx.control.SegmentedBar						| getSegments()
org.controlsfx.control.SegmentedButton					| getButtons()
org.controlsfx.control.SnapshotView						| (unsupported)
org.controlsfx.control.StatusBar						| progressProperty()
org.controlsfx.control.TaskProgressView					| taskProperty()
org.controlsfx.control.textfield.CustomPasswordField	| textProperty()
org.controlsfx.control.textfield.CustomTextField		| textProperty()
org.controlsfx.control.ToggleSwitch						| selectedProperty()
org.controlsfx.control.WorldMapView						| selectedCountriesProperty(), selectedLocationsProperty() (as list with 2 observable lists, namely the country list and the locations list)
