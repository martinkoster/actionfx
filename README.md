# ActionFX

ActionFX aims at simplifying the development of JavaFX applications by reducing the amount of boilerplate code a developer has to provide. 

## Design Philosophies
- Support and encourage the implementation of the MVC pattern: Controllers are light-weight, loosely-coupled components that are not dependent on any framework base classes. Views on the other hand are preferably defined via FXML and other concerns like view behavior, main-window/pop-window considerations are configuration aspects defined via annotations (and not as tons of code).
- Lightweight and Less-intrusive: ActionFX does not force you to build your classes on top of framework classes. At no point in time, you need to derive your classes from ActionFX classes. Classes supplied by the framework can be used e.g. to speed up the setup process, but this is not required. At all point in time, it is possible to make normal use of plain JavaFX development style.
- Choose the container technique on your own: It is up to the developer to decide whether to use a Spring container or not. For smaller applications with just a few views it might be an overkill to integrate Spring. However, for larger applications Spring can be useful for dependency injection and its great integration technologies. Thus, Spring or Spring Boot can be easily used within ActionFX to manage controller and views as Spring beans. None of the features of ActionFX itself will force you to!
- Leverage Aspect-oriented Programming (AOP) for reducing the amount of boilerplate code. ActionFX uses byte-code enhancement for JavaFX controllers in order to implement the cross-cutting concern "view flows" outside of the controller classes themselves. [ByteBuddy](https://bytebuddy.net/#/) is used as framework of choice for realizing AOP in ActionFX. The user can decide whether to use byte code instrumentation via ByteBuddy agent attached to the JVM at runtime, or to perform dynamic sub-classing of the controllers.

## Module Overview

ActionFX is split up into several sub-modules that can be optionally included in your application:

