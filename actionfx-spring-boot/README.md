# Module "actionfx-spring-boot"

This module contains glue classes for plugging in a Spring-container into ActionFX.

Module | Description | API Documentation | Gradle Dependency 
------ | ----------- | ----------------- | ----------
[actionfx-spring-boot](README.md) | This module contains Spring factories to use a Spring Bean container together with ActionFX. Additional Spring Boot is supported with the ActionFX autoconfiguration class [AFXAutoconfiguration](actionfx-spring-boot/src/main/java/com/github/actionfx/spring/autoconfigure/AFXAutoconfiguration.java). When using this module, Spring @Autowired can be used instead of @Inject to autowire views and controllers (and even more services and components managed by the Spring bean container). | [Javadoc](https://martinkoster.github.io/actionfx/1.1.0/actionfx-spring-boot/index.html) | `implementation group: "com.github.martinkoster", name: "actionfx-spring-boot", version: "1.1.0"`

### Hooking in Spring into ActionFX with an ApplicationContextInitializer

When including this module on your application's classpath, class [AFXApplicationContextInitializer](src/main/java/com/github/actionfx/spring/container/AFXApplicationContextInitializer.java) is automatically added as a Spring `ApplicationContextInitializer` via [spring.factories](src/main/resources/META-INF/spring.factories), registering all ActionFX controllers as regular Spring beans. Additionally, an auto-configuration class is registered (see [AFXAutoconfiguration](src/main/java/com/github/actionfx/spring/autoconfigure/AFXAutoconfiguration.java)).

### Main Application combining ActionFX with Spring Boot

The following code snippet shows how Spring Boot can be integrated with ActionFX.

```java
@SpringBootApplication
public class MainAppWithSpringBeanContainer {

	public static void main(final String[] argv) {
		ActionFX.builder().configurationClass(SampleActionFXApplication.class).build();
		Application.launch(SampleActionFXApplication.class);
	}

	@AFXApplication(mainViewId = "mainView", scanPackage = "com.github.actionfx.sampleapp.controller")
	public static class SampleActionFXApplication extends Application {

		@Override
		public void init() throws Exception {
			SpringApplication.run(MainAppWithSpringBeanContainer.class);
		}

		@Override
		public void start(final Stage primaryStage) throws Exception {
			ActionFX.getInstance().displayMainView(primaryStage);
		}
	}
}
```

The class `SampleActionFXApplication` is derived from JavaFX `javafx.application.Application` which provides the `init` and `start` methods to override for our demo application.
 
In the `init` method, we start the Spring container. Please remember that `init` is not called from inside the JavaFX-thread. ActionFX however is interacting the JavaFX-thread to initialize its controllers in the JavaFX thread (there are view components like `javafx.scene.web.WebView` that can only be instantiated inside the JavaFX thread).

In the `start` method, we use the supplied `primaryStage` to display the main view inside by calling `ActionFX.getInstance().displayMainView(primaryStage)`.

### Using Spring Features in ActionFX controllers

Once your ActionFX controllers are managed by Spring, you can leverage all Spring annotations on method- and field-level like `@Autowired` (instead of `@Inject`). Please note that you can **not** use class-level annotations like `@Component` or `@Controller`, because ActionFX requires more information that is only given by the `@AFXController` annotation. However, all classes annotated by `@AFXController` are registered as Spring-managed beans, either singleton- or prototype-scoped, depending on the attribute `@AFXController(..., singleton=true/false)`.

### Use Spring's MessageSource for internationalization

For Spring environments, it is recommended to register the properties files as part of a central Spring-managed `MessageSource`:

```java
@Configuration
public class SpringConfig {

    @Bean
    public ResourceBundleMessageSource messageSource() {

        var source = new ResourceBundleMessageSource();
        source.setBasenames("i18n/TextResources");
        return source;
    }
}
```

If doing so, the controller configuration itself must omit the `resourceBasename` attribute in `@AFXController`:

```java
@AFXController(viewId = "multilingualView", fxml = "/testfxml/MultilingualView.fxml")
public class MultilingualViewController {
	
	// The locale can be injected into the controller in the following forms, if desired and needed...

	@Autowired
	private Locale locale:
	
	@Autowired
	private ObservableValue<Locale> observableLocale;
}
```
