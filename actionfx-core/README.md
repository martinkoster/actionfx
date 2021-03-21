# Module "actionfx-core"

The "actionfx-core" module consists of the core functionality of ActionFX.

Module | Description | API Documentation | Gradle Dependency 
------ | ----------- | ----------------- | ----------
[actionfx-core](README.md) | The core routines around ActionFX. It contains the central class [ActionFX](actionfx-core/src/main/java/com/github/actionfx/core/ActionFX.java) for accessing controllers and views. As ActionFX uses an internal bean container with dependency injection support, it is recommended to wire all controllers with @Inject instead of accessing them through this class (please note that there is also support of Spring's bean container through ActionFX's `actionfx-spring-boot` module). | [Javadoc](https://martinkoster.github.io/actionfx/actionfx-core/index.html) | `implementation group: "com.github.martinkoster", name: "actionfx-core", version: "0.0.2"`

## Table of Contents

- [Module "actionfx-core"](#module--actionfx-core-)
  * [Table of Contents](#table-of-contents)
  * [Overview](#overview)
  * [Setting up ActionFX](#setting-up-actionfx)
    + [Derive your JavaFX application from AbstractAFXApplication](#derive-your-javafx-application-from-abstractafxapplication)
    + [Build your own instance of ActionFX during application startup](#build-your-own-instance-of-actionfx-during-application-startup)
  * [Implementing ActionFX controllers](#implementing-actionfx-controllers)
    + [Example of a Controller Definition with Nested Views](#example-of-a-controller-definition-with-nested-views)
    + [Annotations inside an ActionFX controller](#annotations-inside-an-actionfx-controller)
      - [Annotation @AFXShowView (Method Annotation)](#annotation--afxshowview--method-annotation-)
      - [Annotation @AFXOnAction (Method Annotation)](#annotation-afxonaction--method-annotation-)
      - [Annotation @AFXLoadControlValue (Method Annotation)](#annotation--afxloadcontrolvalue--method-annotation-)
      - [Annotation @AFXOnControlValueChange (Method Annotation)](#annotation--afxoncontrolvaluechange--method-annotation-)
      - [Annotation @AFXArgHint (Method Argument Annotation)](#annotation--afxarghint--method-argument-annotation-)
      - [Annotation @AFXControlValue (Method Argument Annotation)](#annotation--afxcontrolvalue--method-argument-annotation-)
      - [Annotation @AFXNestedView (Field Annotation for fields annotated with @FXML)](#annotation--afxnestedview--field-annotation-for-fields-annotated-with--fxml-)
      - [Annotation @AFXEnableMultiSelection (Field Annotation for fields annotated with @FXML)](#annotation--afxenablemultiselection--field-annotation-for-fields-annotated-with--fxml-)
    + [User Value of Controls](#user-value-of-controls)

## Overview

This module provides:
- The central class [ActionFX](src/main/java/com/github/actionfx/core/ActionFX.java) for retrieving views and controllers (however, retrieving controllers and views via dependency injection via fields annotated by @Inject should be preferred). An instance of the ActionFX class can be retrieved after successful setup via `ActionFX.getInstance()`
- A bean container that supports dependency injections via @Inject and supports post construction method invocation via @PostConstruct.
- Controller definitions via the [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) annotation, declaring FXML-based views.
- Support for nested views: It is possible to embed further views into a view via the [@AFXNestedView](src/main/java/com/github/actionfx/core/annotation/AFXNestedView.java) annotation, either as part of the controller definition in [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) or by applying the [@AFXNestedView](src/main/java/com/github/actionfx/core/annotation/AFXNestedView.java) annotation on @FXML annotated view components.
- Byte-code enhancement via [ActionFXByteBuddyEnhancer](src/main/java/com/github/actionfx/core/instrumentation/bytebuddy/ActionFXByteBuddyEnhancer.java) facilities in order to enhance controller classes and to allow aspect-oriented programming. Two strategies are supported: byte-code enhancement via a Java agent installed at runtime, or enhancement by sub-classing. 
- Annotations are provided that can be used inside ActionFX controllers to wire JavaFX controls to controller methods. No more tons of code like `tableView.getSelectionModel().getSelectedItem().addChangeListener((observable, oldValue, newValue) -> onTableViewSelect(newValue));`. This makes the code more readable and maintainable.
- Methods annotated by [@AFXShowView](src/main/java/com/github/actionfx/core/annotation/AFXShowView.java) can be used to implement a flow between view. Those annotated methods are intercepted and after successful method invocation, the desired view is shown (either as nested view in the current scene graph by attaching the sub-view or by displaying the view in a new stage).


## Setting up ActionFX

There are two ways to setup ActionFX for your JavaFX application:

### Derive your JavaFX application from AbstractAFXApplication

When deriving your JavaFX class from [AbstractAFXApplication](src/main/java/com/github/actionfx/core/app/AbstractAFXApplication.java), all you need to do is to put an additional [@AFXApplication](src/main/java/com/github/actionfx/core/annotation/AFXApplication.java) annotation on your class in order to specify the main view ID and the package to scan for controllers:

```java
public class MainApp {

	public static void main(String[] argv) {
		Application.launch(SampleActionFXApplication.class);
	}

	@AFXApplication(mainViewId = "mainView", scanPackage = "com.github.actionfx.sampleapp.core.app")
	public static class SampleActionFXApplication extends AbstractAFXApplication {

	}

}
```

In the example above, the package `com.github.actionfx.sampleapp.core.app` is scanned for controller annotated with the [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) annotation. One of the controller is expected to declare the main view with id `mainView` which is displayed as soon as JavaFX's primary stage is ready.


### Build your own instance of ActionFX during application startup

Instead of using the base class mentioned in the previous section, an own customized ActionFX instance can be setup with:

```java
	@Override
	public void init() throws Exception {
		ActionFX.builder().configurationClass(getClass()).build();
	}
```

The builder for the ActionFX instance offers the following configuration options:

Builder Method | Description
-------------- | -----------
`configurationClass(final Class<?> configurationClass)` | Reads out the `AFXApplication` annotation that is expected to be  present on the given `configurationClass`. In case the annotation is not present on the given class (or on its super-classes), an `IllegalArgumentException` is thrown.
`mainViewId(final String mainViewId)` | Sets the ID / name of the view that is used to be displayed in JavaFX's primary `Stage`. Please note that this ID must of course exist inside ActionFX's container e.g. by annotating a controller with `AFXController` and defining this view ID there.
`scanPackage(final String scanPackage)` | The package name with dot-notation "." that shall be scanned for ActionFX components.
`enhancementStrategy(final EnhancementStrategy enhancementStrategy)` | The byte-code enhancement strategy to use within ActionFX. Currently the following enhancement strategies are available:1.) `EnhancementStrategy.RUNTIME_INSTRUMENTATION_AGENT}`: A byte-code instrumentation agent is installed/attached at runtime. Methods of controllerclasses are directly enhanced via method interceptors. 2.) `EnhancementStrategy.SUBCLASSING`: Controller classes are sub-classed, while controller methods are overriden and method interceptors are attached.
`actionFXEnhancer(final ActionFXEnhancer actionFXEnhancer)` | Sets the implementation of interface `ActionFXEnhancer` to use within ActionFX. In case there is no instance set, the default enhancer `ActionFXByteBuddyEnhancer` is used. Please note that implementations of interface `ActionFXEnhancer` must provide the possibility of both, byte code instrumentation via a runtime agent and byte code enhancement via sub-classing.
`uncaughtExceptionHandler(final UncaughtExceptionHandler uncaughtExceptionHandler)` | Configures an exception handler for uncaught exceptions.

Once the ActionFX instance is setup with all configuration parameters, it is required to scan for components / controllers with

```java
	ActionFX.getInstance().scanForActionFXComponents();
```

Once the ActionFX instance is configured and initialized with components, you can display the main view with:


```java
	@Override
	public void start(Stage primaryStage) throws Exception {
		ActionFX.getInstance().displayMainView(primaryStage);
	}
```

## Implementing ActionFX controllers

By using annotation [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) on a class, this class becomes an ActionFX controller. A controller is responsible for holding the actions that can be triggered from a view, preferably defined in an FXML document.

The following attributes are available as part of the [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) annotation:

Attribute | Description | Default Value
--------- | ----------- | -------------
`viewId` | The ID of the view. Must be unique among all views of this application. | -
`fxml` | Path to the FXML file to load for this view. Path is relative to the application's classpath. | -
`modal` | Specifies whether this view is a modal dialog or a regular window. | `false` 
`maximized` | Specifies whether this view shall be displayed maxized or not. | `false`
`width` | The width of the view (however `maximized` has a higher priority) | `200`
`height` | The height of the window (however `maximized` has a higher priority). | `100`
`title` | The title to be displayed for the given view/window. | `""`
`posX` | The X position of the window on the screen. | `0`
`poxY` | The Y position of the window on the screen. | `0`
`icon` | The icon to be displayed in case the view is displayed in its own stage. | `""`
`singleton` | Determines whether the view (and by that the controller) is managed as singleton or not. If the view is not a singleton, the view is newly created whenever it is requested. | `true`
`lazyInit` |  Flag that controls the initialization of the view and controller. If set to `true`, view components are lazily initialized at the point the view is really required and requested from the bean container. If set to `false`, the view components are initialized at the startup of the ActionFX application, when the bean container of ActionFX is initialized. Although lazy loading should be preferred, disabling of lazy loading makes sense, when you want to have a fail-early fail-fast pattern and exceptions during view initializations should/must be thrown at application startup (and not later, when you already work with the application). | `true`
`stylesheets` | Which stylesheets shall be applied to the scene? This array contains a list of classpath locations to CSS files. | `{}`

After defining the controller and scanning for ActionFX components (see previous chapters on how to setup ActionFX), an instance of the controller is retrieved by:

```java
	SomeController someController = ActionFX.getInstance().getController(SomeController.class);
```

Preferably, you can just annotate a field with `@Inject` (if the controller is called from another controller):

```java
@AFXController(viewId = "mainView", fxml = "/fxml/MainView.fxml", maximized = true)
public class MainController {
	
	// Controller "SomeController" is injected into the "MainController"
	@Inject
	private SomeController someController;
}
```

Please note that controller **must not be instantiated directly** via the `new` keyword, because ActionFX is enhancing the class in the background and is evaluating the [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) annotation.

### Example of a Controller Definition with Nested Views

The following code snippet show how a controller definition can look like, while other controllers and their views are wired into the FXML-bassed view. 

```java
@AFXController(viewId = "mainView", fxml = "/fxml/MainView.fxml", maximized = true)
public class MainController {

	@AFXNestedView(refViewId = "productCatalogListView")
	@FXML
	private ScrollPane productListScrollPane;

	@AFXNestedView(refViewId = "productDetailsView", attachToAnchorBottom = 0.0, attachToAnchorLeft = 0.0, attachToAnchorRight = 0.0, attachToAnchorTop = 0.0)
	@FXML
	private AnchorPane productDetailsAnchorPane;

	@AFXNestedView(refViewId = "shoppingCartView", attachToAnchorBottom = 0.0, attachToAnchorLeft = 0.0, attachToAnchorRight = 0.0, attachToAnchorTop = 0.0)
	@FXML
	private AnchorPane shoppingCartAnchorPane;
	
	@PostConstruct
	public void initialize() {
		// some custom initialization goes here
	}
}
```

Please note that the fields annotated by [@AFXNestedView](src/main/java/com/github/actionfx/core/annotation/AFXNestedView.java) are required to carry the @FXML annotation. It would be also possible to define the nested views as part of the [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) annotation. In the latter case, the fields are not required to be injected via @FXML (they don't even need to be present in the class). However, it is required that the FXML view components can be referenced by an unique ID.

The following example shows how to use nested views in the [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) annotation:

```java
@AFXNestedView(refViewId = "productCatalogListView", attachToNodeWithId="productListScrollPane")
@AFXNestedView(refViewId = "productDetailsView", attachToNodeWithId="productDetailsAnchorPane", attachToAnchorBottom = 0.0, attachToAnchorLeft = 0.0, attachToAnchorRight = 0.0, attachToAnchorTop = 0.0)
@AFXNestedView(refViewId = "shoppingCartView", attachToNodeWithId="shoppingCartAnchorPane", attachToAnchorBottom = 0.0, attachToAnchorLeft = 0.0, attachToAnchorRight = 0.0, attachToAnchorTop = 0.0)
@AFXController(viewId = "mainView", fxml = "/fxml/MainView.fxml", maximized = true)
public class MainController {

	@PostConstruct
	public void initialize() {
		// some custom initialization goes here
	}
}
```

Please note in the example above, the additional attribute `attachToNodeWithId` needs to be provided, so that ActionFX knows to which node the nested view needs to be attached to.


### Annotations inside an ActionFX controller

There are various annotations that you can apply to controller methods and fields that are reducing the amount of code that you need for wiring your controls and methods. In the following sections, an overview on the available annotations is provided.


#### Annotation @AFXShowView (Method Annotation)

The [@AFXShowView](src/main/java/com/github/actionfx/core/annotation/AFXShowView.java) can be applied at method-level to navigate between different views from inside a controller class.

The annotation provides different options how the new view shall be displayed. The following attributes are available in [@AFXShowView](src/main/java/com/github/actionfx/core/annotation/AFXShowView.java):

Attribute 					| Description 
--------------------------- | -------------------------------------------------
`showView` 				|  The view to be displayed, when the method successfully terminates. This attribute competes with attribute `showNestedViews()`, while this attribute has higher precedence than `showNestedViews()` 
`showInNewWindow` 		|  Determines whether the view defined in `showView()` shall be displayed in its own `Stage`. The specification of this attribute does not affect view transition in case the attribute `showNestedViews()` is given.
`showNestedViews` 		| The nested views to be displayed, when the method successfully terminates. This attribute allows to embed view into the current scene graph and `Stage`. Please take note, that this attribute must not be used together with `showView()` and `showInNewWindow()`.

Please note that only *one* attribute must be used at the same time (they can not be combined).

**Example:**
```java
	@AFXShowView(showViewInNewWindow="detailsView")
	public void actionMethod() {
		// some further initialization goes here - or leave it just empty
	}
```

#### Annotation @AFXOnAction (Method Annotation)

The [@AFXOnAction](src/main/java/com/github/actionfx/core/annotation/AFXOnAction.java) is wiring the annotated method to the "onAction" property of the specified control. This annotation can be e.g. used to execute the annotated method, when the user clicks on a button.

Annotated methods can be of the following signature:
- `void methodName()`
- `void methodName(javafx.event.ActionEvent event)`

You can also combine this annotation with annotation `@AFXControlValue`:

`void methodName(@AFXControlValue("usernameTextField") String username)`

In this case, the user value entered in text field with ID usernameTextField' will be injected as method argument.

The following attributes are available inside the annotation:

Attribute 					| Description 
--------------------------- | -------------------------------------------------
`controlId`				| ID of the control whose action property shall be set to execute the annotated method.

**Example:**
```java
	// for the @AFXOnAction annotation to work, it is not required that the button is injected via @FXML
	@FXML
	private Button actionButton;
	...
	// for the @AFXControlValue annotation to work, it is not required that the text field is injected via @FXML
	@FXML
 	private TextField usernameTextField;
 	...
	@AFXOnAction(controlId = "actionButton")
	public void onButtonClicked(@AFXControlValue("usernameTextField") final String username) {
		// do some action stuff
	}
```

#### Annotation @AFXLoadControlValue (Method Annotation)

The [@AFXLoadControlValue](src/main/java/com/github/actionfx/core/annotation/AFXLoadControlValue.java) annotation can  be applied to methods that return a value that is usable as value inside a referenced control (e.g. load all entities to be displayed inside a TableView or load a text to be displayed in a text area)

It is possible perform the loading of data in an asynchronous fashion in a separate thread outside the JavaFX thread by using the attribute `async` (see below). 

The following attributes are available inside the annotation:

Attribute                           | Description 
----------------------------------- | -------------------------------------------------
`controlId`						| ID of the control whose values shall be loaded for by the annotated method
`async`								| Optional flag that determines, whether the data shall be loaded in an asynchronous fashion. When set to `true`, the annotated method is not executed inside the JavaFX-thread, but in its own thread in order not to block the JavaFX thread. The data itself however is set again to the referenced control from inside the JavaFX thread. Default is `false`.
`loadingActiveBooleanProperty`	| An optional expression that must resolve to a field of type `BooleanProperty`, if specified. The annotated method is only called after construction (before `@PostConstruct` initialization methods), the boolean property holds the value `true`. After construction time, the data is loaded, whenever the boolean property switches its value from `false` to `true`.
`order`								| An optional order that can be specified to define the order of execution of the annotated method, in case more than one data loading routine is present inside the ActionFX controller.

**Example:**
```java
	// for the @AFXLoadControlData annotation to work, it is not required that the table view is injected via @FXML
	@FXML
 	private TableView<Product> productsTableView;
 	...
	private BooleanProperty loadProductsBooleanProperty = new SimpleBooleanProperty(true);
	...
	@AFXLoadControlData(controlId = "productsTableView", async = true, loadingActiveBooleanProperty = "loadProductsBooleanProperty")
	public List<Product> loadProductsAsynchronously() {
		// loading logic for products goes here. E.g. load it from the data base
	}
```

#### Annotation @AFXOnControlValueChange (Method Annotation)

The [@AFXOnControlValueChange](src/main/java/com/github/actionfx/core/annotation/AFXOnControlValueChange.java) annotation is applied to methods, which are then invoked, when the user changes a value in the referenced control identified by the attribute `controlId`.

Annotated methods can be of the following signature:

For controls with a single-value (e.g. for texts in a `TextField` or a single-selection inside a `TableView`):

- `void methodName()`
- `void methodName(TYPE newValue)`
- `void methodName(TYPE newValue, TYPE oldValue, ObservableValue<TYPE> observableValue)`

For controls with multi-values (e.g. for a multi-selection inside a`TableView`):

- `void methodName()`
- `void methodName(ObservableList<TYPE> selectedValue)`
- `void methodName(ObservableList<TYPE> selectedValue, List<TYPE> addedList, List<TYPE> removedList, javafx.collections.ListChangeListener.Change change)`

The above signatures are supported without requiring the use of the [@AFXArgHint](src/main/java/com/github/actionfx/core/annotation/AFXArgHint.java) annotation. In case you need to change the order of the arguments, you will need to specify hints for defining, which argument is e.g. the "new" value (use `@AFXArgHint` with `ArgumentHint#NEW_VALUE`) and which argument is the "old" value
  (use `AFXArgHint` with `ArgumentHint#OLD_VALUE`).

The following attributes are available inside the annotation:

Attribute 							| Description 
----------------------------------- | -------------------------------------------------
`controlId`						| ID of the control whose value shall be observed for changes. Please note that the given Id needs to be an existing node ID in the scene graph that evaluates to a `javafx.scene.control.Control`.
`timeoutMs`						| An optional timeout in milliseconds that is waited after the value change in the control occurs. The default value is 0, i.e. means the method is immediately executed after the value change occurs. In case there is a positive value specified, there is only one method invocation for the last change event that occurred in the time between first change event and the given number of timeout milliseconds. This value can be used e.g. for reducing the number of method invocation (e.g. for a `TextField` you might not want to have this method invoked on every key stroke, but you might want to wait for multiple changes).
`listenerActiveBooleanProperty`	| An optional expression that must resolve to a field of type `BooleanProperty`, if specified. The annotated method is only called, when the boolean property holds the value `true`. This attribute can be useful, when you want to activate the callback methods after a complete initialization of the JavaFX dialogue with values (and not before that).
`order`								| An optional order that can be specified to define the order of execution of the annotated method, in case more than one method listens to changes of the same specific control. Lower order values will be executed before higher order values.

For more details on how the attribute `timeoutMs` is realized, please refer to class [TimedChangeListener](src/main/java/com/github/actionfx/core/listener/TimedChangeListener.java) for single-value changes and class [TimedListChangeListener](src/main/java/com/github/actionfx/core/listener/TimedListChangeListener.java) for list changes. These classes can be also directly used for wiring change listeners to controls.

**Example:**
```java
	// for the @AFXOnControlValueChange annotation to work, it is not required that the text field is injected via @FXML
	@FXML
 	private TextField usernameTextField;
 	...
	private BooleanProperty listenerEnabled = new SimpleBooleanProperty(true);
	...
	@AFXOnControlValueChange(controlId = "usernameTextField", order = 20, timeoutMs = 300, listenerActiveBooleanProperty = "listenerEnabled")
	public void onUsernameChange(final String newValue, final String oldValue, final ObservableValue<String> observable) {
		// action on user name change goes here
	}
```

#### Annotation @AFXArgHint (Method Argument Annotation)

The [@AFXArgHint](src/main/java/com/github/actionfx/core/annotation/AFXArgHint.java) annotation helps other method-level annotations to recognize the "meaning" of a method argument. The value specified in this annotation is a "hint", which is required, when two method arguments have the same type. 

The following attributes are available inside the annotation:

Attribute 					| Description 
--------------------------- | -------------------------------------------------
`value`						| The hint value defines which semantic the annotated method parameter has. Supported values are `ArgumentHint.OLD_VALUE`, `ArgumentHint.NEW_VALUE`, `ArgumentHint.TYPE_BASED` (default) ,`ArgumentHint.ADDED_VALUES`, `ArgumentHint.REMOVED_VALUES`, `ArgumentHint.ALL_SELECTED`

**Example:**
```java
	@AFXOnControlValueChange(controlId = "usernameTextField")
	public void onUsernameChange(@AFXArgHint(ArgumentHint.OLD_VALUE) final String oldValue,
			@AFXArgHint(ArgumentHint.NEW_VALUE) final String newValue, final ObservableValue<String> observable) {
		// action on user name change goes here
	}
```

#### Annotation @AFXControlValue (Method Argument Annotation)

The [@AFXControlValue](src/main/java/com/github/actionfx/core/annotation/AFXControlValue.java) annotation is applied to method arguments to retrieve the user value from the specified control.

This annotation can be applied to method arguments of methods that are called from the ActionFX framework.

Following methods are eligible for arguments to be annotated by `@AFXControlValue`:
- methods annotated by `AFXOnAction` (these methods are wired to an "onAction" property of a control like a `javafx.scene.control.Button`
- methods annotated by `AFXLoadControlData` (these methods load data for a control inside the scene graph)
- methods annotated by `AFXOnControlValueChange` (these methods are executed when a change of control's user value occurs)

The following attributes are available inside the annotation:

Attribute 					| Description 
--------------------------- | -------------------------------------------------
`value`						| ID of the control whose value shall be bound to the annotated method argument

**Example:**
```java
	@AFXOnAction(controlId = "actionButton")
	public void onButtonClicked(@AFXControlValue("usernameTextField") final String username) {
		// do some action stuff
	}
```

#### Annotation @AFXNestedView (Field Annotation for fields annotated with @FXML, Class Annotation for classes annotated with @AFXController)

The [@AFXNestedView](src/main/java/com/github/actionfx/core/annotation/AFXNestedView.java) annotation defines a nested view to be embedded into a scene graph. Nested views can be used to composite the overall scene graph view.

You can either use this annotation as part of an `AFXController` annotation, or you can apply it to a field that is also annotated by `@FXML`.

The following attributes are available inside the annotation:

Attribute 							| Description 
----------------------------------- | -------------------------------------------------
`refViewId` 						| The referenced view ID that shall nested in the view.
`attachToNodeWithId`				| The parent node ID where the nested view shall be attached to. Please note that this ID is a real node ID in the JavaFX scene graph. This field is mandatory, if this annotation is used inside a `@AFXController` annotation. If used on a field annotated by `@FXML`, this value is irrelevant.
`attachToIndex`					| Optional index referring to the target node's children list, where the view shall be attached to.
`attachToColumn`					| Optional column index in case the target node is a `javafx.scene.layout.GridPane`. Must be used together with `attachInRow`.
`attachToRow`						| Optional row index in case the target node is a `javafx.scene.layout.GridPane`. Must be used together with `attachInColum`.
`attachToBorderPanePosition`		| Optional border pane position in case the target node is a `javafx.scene.layout.BorderPane`.
`attachToAnchorLeft`				| Optional anchor left in case the target node is an `javafx.scene.layout.AnchorPane`. Must be used together with `attachToAnchorTop`, `attachToAnchorRight` and `attachToAnchorBottom`.
`attachToAnchorTop`				| Optional anchor top in case the target node is an `javafx.scene.layout.AnchorPane`. Must be used together with `attachToAnchorLeft`, `attachToAnchorRight` and `attachToAnchorBottom`.
`attachToAnchorRight`				|  Optional anchor right in case the target node is an `javafx.scene.layout.AnchorPane`. Must be used together with `attachToAnchorTop`, `attachToAnchorLeft` and `attachToAnchorBottom`.
`attachToAnchorBottom`			| Optional anchor bottom in case the target node is an `javafx.scene.layout.AnchorPane`. Must be used together with `attachToAnchorTop`, `attachToAnchorRight` and `attachToAnchorLeft`.

**Example:**
```java
	@AFXNestedView(refViewId = "productCatalogListView")
	@FXML
	private ScrollPane productListScrollPane;
	...
	@AFXNestedView(refViewId = "productDetailsView", attachToAnchorBottom = 0.0, attachToAnchorLeft = 0.0, attachToAnchorRight = 0.0, attachToAnchorTop = 0.0)
	@FXML
	private AnchorPane productDetailsAnchorPane;
	...
	@AFXNestedView(refViewId = "productDetailsView", attachToBorderPanePosition = BorderPanePosition.CENTER)
	@FXML
	private BorderPane shopingCartAnchorPane;
	...
	@AFXNestedView(refViewId = "productFeedbackView", attachToColumn = 3, attachInRow = 2)
	@FXML
	private GridPane productFeedbackGridPane;
```


#### Annotation @AFXEnableMultiSelection (Field Annotation for fields annotated with @FXML)

The [@AFXEnableMultiSelection](src/main/java/com/github/actionfx/core/annotation/AFXEnableMultiSelection.java) annotation can be applied at field level on a `javafx.scene.control.Control`, in order to enable a multi-selection on that annotated control.

This annotation can be e.g. applied to a field of type `javafx.scene.control.TableView`, so that the user can select multiple entries in that table view.

**Example:**
```java
	@AFXEnableMultiSelection
	@FXML
	private TableView<String> multiSelectionTable;
```

#### Annotation @AFXUseFilteredList (Field Annotation for fields annotated with @FXML)

The [@AFXUseFilteredList](src/main/java/com/github/actionfx/core/annotation/AFXUseFilteredList.java) annotation can be applied at field level on a `javafx.scene.control.Control`,so that ActionFX instructs the control to use a `javafx.collections.transformation.FilteredList` as items. Please note that the control must support multiple values in form of an `javafx.collections.transformation.ObservableList`.

This annotation can be e.g. applied to a field of type `javafx.scene.control.TableView`, so that table view items can be filtered. Additionally, the filtered list can be wrapped in a `javafx.collections.transformation.SortedList`, if desired.

**Example:**
```java
	@AFXUseFilteredList
	@FXML
	private TableView<String> filteredTable;
...
	@AFXUseFilteredList(wrapInSortedList=true)
	@FXML
	private TableView<String> filteredAndSortedTable;
```


### User Value of Controls

In the previous sections, it was shown that user selected values can be injected into methods e.g. by using the `@AFXControlValue` annotation or that you can listen to changes of control values via the `@AFXOnControlValueChange` annotation.

But what exactly does "user value" mean? Unlike other UI-technologies, JavaFX does not have a consistent class hierarchy to retrieve a "user value". Also, there is no "value property" in the controls.

ActionFX tries to mitigate this shortcoming by providing properties configuration files for all JavaFX controls in package `javafx.scene.control`. This configuration is interpreted by a central class in ActionFX: the [ControlWrapper](src/main/java/com/github/actionfx/core/view/graph/ControlWrapper.java) class.

The properties file for an `javafx.scene.control.TreeView` is shown here as an example (see other examples in folder [src/main/resources/afxcontrolwrapper](src/main/resources/afxcontrolwrapper) ):
```
# Defines how to access a JavaFX control.
#
# In case you have a custom control not taken directly taken from JavaFX, you can create the following file:
#
# Filename: /afxcontrolwrapper/<full-qualified-class-name>.properties
# Supported property entries:
# Property Key                  | Property Value                                             | Example Values
# -------------------------------------------------------------------------------------------------------------------------------------------------------
# valueProperty                 | Property name holding a single "value"                     | "text" (e.g. for TextField), "value" (e.g. for ComboBox)
# valuesObservableList          | Name of an observable list holding all possible "values"   | "items" (e.g. for ComboBox)
# selectionModelProperty        | Property name holding the "selectionModel"                 | "selectionModel" (e.g. for TableView)
#
#  In case a certain property is not supported by the control (e.g. no support for a "SelectionModel"), then the property must be left empty.
#
valueProperty=root
valuesObservableList=
selectionModelProperty=selectionModel
```
In case you have a custom control implemented or you are using a 3rd party controls library, you can provide a properties file in the following location in the classpath: `/afxcontrolwrapper/<full-qualified-class-name>.properties`

As mentioned, for all JavaFX controls this works out-of-the box with ActionFX, as there are configurations provided for each JavaFX control.

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