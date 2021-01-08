# Module "actionfx-core"

The "actionfx-core" module consists of the core functionality of ActionFX.

Module | Description | API Documentation | Gradle Dependency 
------ | ----------- | ----------------- | ----------
[actionfx-core](actionfx-core/README.md) | The core routines around ActionFX. It contains the central class [ActionFX](actionfx-core/src/main/java/com/github/actionfx/core/ActionFX.java) for accessing controllers and views. As ActionFX uses an internal bean container with dependency injection support, it is recommended to wire all controllers with @Inject instead of accessing them through this class (please note that there is also support of Spring's bean container through ActionFX's `actionfx-spring-boot` module). | [Javadoc](https://martinkoster.github.io/actionfx/actionfx-core/index.html) | `implementation group: "com.github.martinkoster", name: "actionfx-core", version: "0.0.1"`

## Overview

This module provides:
- The central class [ActionFX](src/main/java/com/github/actionfx/core/ActionFX.java) for retrieving views and controllers (however, retrieving controllers and views via dependency injection via fields annotated by @Inject should be preferred). An instance of the ActionFX class can be retrieved after successful setup via `ActionFX.getInstance()`
- A bean container that supports dependency injections via @Inject and supports post construction method invocation via @PostConstruct.
- Controller definitions via the [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) annotation, declaring FXML-based views.
- Support for nested views: It is possible to embed further views into a view via the [@AFXNestedView](src/main/java/com/github/actionfx/core/annotation/AFXNestedView.java) annotation, either as part of the controller definition in [@AFXController](src/main/java/com/github/actionfx/core/annotation/AFXController.java) or by applying the [@AFXNestedView](src/main/java/com/github/actionfx/core/annotation/AFXNestedView.java) annotation on @FXML annotated view components.
- Byte-code enhancement via [ActionFXByteBuddyEnhancer](src/main/java/com/github/actionfx/core/instrumentation/bytebuddy/ActionFXByteBuddyEnhancer.java) facilities in order to enhance controller classes and to allow aspect-oriented programming. Two strategies are supported: byte-code enhancement via a Java agent installed at runtime, or enhancement by sub-classing. 
- Methods annotated by [@AFXShowView](src/main/java/com/github/actionfx/core/annotation/AFXShowView.java) are intercepted and after successful method invocation, the desired view is shown (either as nested view in the current scene graph by attaching the sub-view or by displaying the view in a new stage).


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

## Defining an ActionFX controller

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
`nestedViews` | An optional list of nested views that shall be embedded in the view. | `{}`

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

## Example of a Controller Definition with Nested Views

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
@AFXController(viewId = "mainView", fxml = "/fxml/MainView.fxml", maximized = true,
	nestedViews = {
		@AFXNestedView(refViewId = "productCatalogListView", attachToNodeWithId="productListScrollPane"),
		@AFXNestedView(refViewId = "productDetailsView", attachToNodeWithId="productDetailsAnchorPane", attachToAnchorBottom = 0.0, attachToAnchorLeft = 0.0, attachToAnchorRight = 0.0, attachToAnchorTop = 0.0),
		@AFXNestedView(refViewId = "shoppingCartView", attachToNodeWithId="shoppingCartAnchorPane", attachToAnchorBottom = 0.0, attachToAnchorLeft = 0.0, attachToAnchorRight = 0.0, attachToAnchorTop = 0.0)
})
public class MainController {

	@PostConstruct
	public void initialize() {
		// some custom initialization goes here
	}
}
```
Please note in the example above, the additional attribute `attachToNodeWithId` needs to be provided, so that ActionFX knows to which node the nested view needs to be attached to.


## Using @AFXShowView to navigate between views

The [@AFXShowView](src/main/java/com/github/actionfx/core/annotation/AFXShowView.java) can be applied at method-level to navigate between different views from inside a controller class.

The annotation provides different options how the new view shall be displayed. The following attributes are available in [@AFXShowView](src/main/java/com/github/actionfx/core/annotation/AFXShowView.java):

Attribute | Description 
--------- | -----------
`showView` |  The view to be displayed, when the method successfully terminates. This attribute competes with attribute `showNestedViews()`, while this attribute has higher precedence than `showNestedViews()` 
`showInNewWindow` |  Determines whether the view defined in `showView()` shall be displayed in its own `Stage`. The specification of this attribute does not affect view transition in case the attribute `showNestedViews()` is given.
`showNestedViews` | The nested views to be displayed, when the method successfully terminates. This attribute allows to embed view into the current scene graph and `Stage`. Please take note, that this attribute must not be used together with `showView()` and `showInNewWindow()`.

Please note that only *one* attribute must be used at the same time (they can not be combined).