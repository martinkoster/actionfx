# Module "actionfx-spring-boot"

This module contains glue classes for plugging in a Spring-container into ActionFX.

Module | Description | API Documentation | Gradle Dependency 
------ | ----------- | ----------------- | ----------
[actionfx-spring-boot](README.md) | This module contains Spring factories to use a Spring Bean container together with ActionFX. Additional Spring Boot is supported with the ActionFX autoconfiguration class [AFXAutoconfiguration](actionfx-spring-boot/src/main/java/com/github/actionfx/spring/autoconfigure/AFXAutoconfiguration.java). When using this module, Spring @Autowired can be used instead of @Inject to autowire views and controllers (and even more services and components managed by the Spring bean container). | [Javadoc](https://martinkoster.github.io/actionfx/actionfx-spring-boot/index.html) | `implementation group: "com.github.martinkoster", name: "actionfx-spring-boot", version: "1.0.0"`

### Wiring a SpringBeanContainer with an ApplicationContextInitializer

When including this module on your application's classpath, class [AFXApplicationContextInitializer](src/main/java/com/github/actionfx/spring/container/AFXApplicationContextInitializer.java) is automatically added as a Spring `ApplicationContextInitializer` via [spring.factories](src/main/resources/META-INF/spring.factories), registering all ActionFX controllers as regular Spring beans. Additionally, an auto-configuration class is registered (see [AFXAutoconfiguration](src/main/java/com/github/actionfx/spring/autoconfigure/AFXAutoconfiguration.java)).
