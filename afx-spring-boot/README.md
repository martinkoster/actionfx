# Module "afx-spring-boot"

This module contains glue classes for plugging in a Spring-container into ActionFX.

Module | Description | API Documentation | Dependency 
------ | ----------- | ----------------- | ----------
[afx-spring-boot](afx-spring-boot/README.md) | This module contains Spring factories to use a Spring Bean container together with ActionFX. Additional Spring Boot is supported with the ActionFX autoconfiguration class [AFXAutoconfiguration](afx-spring-boot/src/main/java/com/github/actionfx/spring/autoconfigure/AFXAutoconfiguration.java). When using this module, Spring @Autowired can be used instead of @Inject to autowire views and controllers (and even more services and components managed by the Spring bean container). | [Javadoc](https://martinkoster.github.io/actionfx/afx-spring-boot/index.html) | `implementation group: "com.github.martinkoster", name: "afx-spring-boot", version: "0.0.1"`
