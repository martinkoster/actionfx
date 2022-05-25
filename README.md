# ActionFX

[![Gitlab pipeline status](https://gitlab.com/martinkoster/actionfx/badges/master/pipeline.svg)](https://gitlab.com/martinkoster/actionfx/-/pipelines)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=martinkoster_actionfx&metric=coverage)](https://sonarcloud.io/dashboard?id=martinkoster_actionfx)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=martinkoster_actionfx&metric=alert_status)](https://sonarcloud.io/dashboard?id=martinkoster_actionfx)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=martinkoster_actionfx&metric=bugs)](https://sonarcloud.io/dashboard?id=martinkoster_actionfx)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=martinkoster_actionfx&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=martinkoster_actionfx)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=martinkoster_actionfx&metric=security_rating)](https://sonarcloud.io/dashboard?id=martinkoster_actionfx)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Maven Central](https://img.shields.io/maven-central/v/com.github.martinkoster/actionfx-core)

ActionFX is a declarative, less-intrusive MVC framework based on annotations and dependency injection that aims at simplifying the development of JavaFX applications by reducing the amount of boilerplate code a developer has to provide. The focus in using "ActionFX" lies on providing the "action" and not on tedious coding of wiring together of components, callbacks and change listener. The result is less and cleaner code, which is easier to read, maintain and especially test.

For a quick introduction to ActionFX, you can refer to the [sample applications](actionfx-app-sample/README.md). For a more detailed overview on the features of ActionFX, please refer to the [core documentation](actionfx-core/README.md).

This document contains the following chapters:

- [Design Philosophies](#design-philosophies)
- [Feature Overview](#feature-overview)
- [Module Overview](#module-overview)

## Design Philosophies
- **Support and encourage the implementation of the MVC pattern**: Controllers are light-weight, loosely-coupled components that are not dependent on any framework base classes. Views on the other hand are preferably defined via FXML and other concerns like view behavior, main-window/popup-window considerations are configuration aspects defined via annotations (and not as tons of code).
- **Lightweight and Less-intrusive**: ActionFX does not force you to build your classes on top of framework classes. At no point in time, you need to derive your classes from ActionFX classes. Classes supplied by the framework can be used e.g. to speed up the setup process, but this is not required. At all point in time, it is possible to make normal use of plain JavaFX development style.
- **Use Dependency Injection to wire controllers**: Make use of dependency injection to use a controller from another controller. ActionFX comes with an integrated dependency injection container where you can define whether your controller and associated views are singleton-scoped beans (default) or prototyped-scoped beans that are instantiated each time you need a particular controller / view (e.g. for dialogues requesting user information it might make sense to have them prototype-scoped). Although ActionFX has its own bean container, you are free to decide for a full Spring integration with ActionFX (see next design philosophy).
- **Choose the container technique on your own**: It is up to the developer to decide whether to use a Spring container or not. For smaller applications with just a few views it might be an overkill to integrate Spring. However, for larger applications Spring can be useful for dependency injection and its great integration technologies. Thus, Spring or Spring Boot can be easily used within ActionFX to manage controller and views as Spring beans. None of the features of ActionFX itself will force you to!
- **Extensibility**: ActionFX features are implemented as "controller extension", where each annotation and extension is encapsulated in its own controller extension class. Developers can register and extend ActionFX by their own controller extensions. Controller extensions are applied to the controller after instantiation, after dependency injection, but before methods annotated with `@PostConstruct` are invoked. 

## Feature Overview

ActionFX provides the following features in a declarative fashion without requiring you to write code: 
- Use Dependency Injection with ActionFX directly or with a Spring container
- Define controllers using FXML or statically implemented views
- View handling through annotations e.g. injecting nested views into a scene graph or navigate to a certain view
- Declarative form-binding of custom domain objects either with JavaFX properties or plain Java types
- Inject control values e.g. selected items from a `javafx.scene.control.TableView` into methods by using annotations on method arguments
- Support for docking and undocking parts of the JavaFX scenegraph and display a part of a view into an own `javafx.stage.Stage`
- Loose coupling of controller using a  basic publish/subscribe mechanism for ActionFX controller methods using `@AFXSubscribe`
- Declaratively wire the `onActionProperty` of scene graph nodes to methods 
- Configure complex table views or even tree table views by just using annotations, which avoids a lot of code lines compared to pure JavaFX
- Enable or disable nodes like a `javafx.scene.control.Button` depending on whether the user supplied input in other controls like text fields or table views
- Integrate user confirmation dialogues or simple text input dialogs in a declarative fashion 
- Out-of-the-box internationalization support through resource bundle integration (for Spring a `org.springframework.context.MessageSource` can be leveraged)

## Module Overview

ActionFX is split up into several sub-modules that can be optionally included in your application:

Module | Description | API Documentation | Dependency 
------ | ----------- | ----------------- | ----------
[actionfx-core](actionfx-core/README.md) | The core routines around ActionFX. It contains the central class [ActionFX](actionfx-core/src/main/java/com/github/actionfx/core/ActionFX.java) for accessing controllers and views. As ActionFX uses an internal bean container with dependency injection support, it is recommended to wire all controllers with @Inject instead of accessing them through this class (please note that there is also support of Spring's bean container through ActionFX's `actionfx-spring-boot` module). | [Javadoc](https://martinkoster.github.io/actionfx/1.5.1/actionfx-core/index.html) | `implementation group: "com.github.martinkoster", name: "actionfx-core", version: "1.5.1"`
[actionfx-testing](actionfx-testing/README.md) | This module contains JUnit 5 classes for unit- and integration testing of JavaFX and ActionFX components. This is achieved by JUnit 5 extensions [FxThreadForAllMonocleExtension](actionfx-testing/src/main/java/com/github/actionfx/testing/junit5/FxThreadForAllMonocleExtension.java) and [FxThreadForEachMonocleExtension](actionfx-testing/src/main/java/com/github/actionfx/testing/junit5/FxThreadForEachMonocleExtension.java) to run tests inside the JavaFX thread. | [Javadoc](https://martinkoster.github.io/actionfx/1.5.1/actionfx-testing/index.html) | `implementation group: "com.github.martinkoster", name: "actionfx-testing", version: "1.5.1"`
[actionfx-spring-boot](actionfx-spring-boot/README.md) | This module contains Spring factories to use a Spring Bean container together with ActionFX. Additional Spring Boot is supported with the ActionFX autoconfiguration class [AFXAutoconfiguration](actionfx-spring-boot/src/main/java/com/github/actionfx/spring/autoconfigure/AFXAutoconfiguration.java). When using this module, Spring @Autowired can be used instead of @Inject to autowire views and controllers (and even more services and components managed by the Spring bean container). | [Javadoc](https://martinkoster.github.io/actionfx/1.5.1/actionfx-spring-boot/index.html) | `implementation group: "com.github.martinkoster", name: "actionfx-spring-boot", version: "1.5.1"`
[actionfx-controlsfx](actionfx-controlsfx/README.md) | This module integrates the components and controls of [ControlsFX](https://github.com/controlsfx/controlsfx) into ActionFX. | [Javadoc](https://martinkoster.github.io/actionfx/1.5.1/actionfx-controlsfx/index.html) | `implementation group: "com.github.martinkoster", name: "actionfx-controlsfx", version: "1.5.1"`
[actionfx-app-sample](actionfx-app-sample/README.md) | This module contains small sample applications how to use ActionFX with the default bean container using just the actionfx-core module and how to use it with a Spring bean container. | [Javadoc](https://martinkoster.github.io/actionfx/1.5.1/actionfx-app-sample/index.html) | -
[actionfx-appfactory](actionfx-appfactory/README.md) | This module contains an executable ActionFX application to easily scaffold new, Gradle-based ActionFX projects. Aside from generating the required build files, it generates ActionFX controller from existing FXML files for enhanced productivity. | [Javadoc](https://martinkoster.github.io/actionfx/1.5.1/actionfx-appfactory/index.html) | -
