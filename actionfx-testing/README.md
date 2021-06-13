# Module "actionfx-testing"

This module contains JUnit 5 test extensions for testing Javactionfx- and ActionFX components.

Module | Description | API Documentation  
------ | ----------- | ----------------- 
[actionfx-testing](README.md) | This module contains JUnit 5 classes for unit- and integration testing of JavaFX and ActionFX components. This is achieved by JUnit 5 extensions [FxThreadForAllMonocleExtension](actionfx-testing/src/main/java/com/github/actionfx/testing/junit5/FxThreadForAllMonocleExtension.java) and [FxThreadForEachMonocleExtension](actionfx-testing/src/main/java/com/github/actionfx/testing/junit5/FxThreadForEachMonocleExtension.java) to run tests inside the JavaFX thread. | [Javadoc](https://martinkoster.github.io/actionfx/actionfx-testing/index.html) 

**Gradle Dependency**

```
implementation group: "com.github.martinkoster", name: "actionfx-testing", version: "1.3.0"
```

**Maven Dependency**

```xml
<dependency>
    <groupId>com.github.martinkoster</groupId>
    <artifactId>actionfx-testing</artifactId>
    <version>1.3.0</version>
</dependency>
```

## Usage

The following code snippets demonstrate the usage of the JUnit 5 testing framework of ActionFX:

### Using one Javactionfx-application thread through all test methods

In this scenario, one JavaFX thread is created that all test methods can use (but not run inside in), by using [@FxThreadForAllMonocleExtension](src/main/java/com/github/actionfx/testing/junit5/FxThreadForAllMonocleExtension.java):

```java
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ActionFXTest {

	@Test
	void testFirst() {
	}
	
	@Test
	void testSecond() {
	}
	
} 
```

### Using a new Javactionfx-application thread for each test method

In this scenario, each method is executed with a new, fresh JavaFX thread(but not run inside in), by using [@FxThreadForEachMonocleExtension](src/main/java/com/github/actionfx/testing/junit5/FxThreadForEachMonocleExtension.java):

```java
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ActionFXTest {

	@Test
	void testFirst() {
	}
	
	@Test
	void testSecond() {
	}
	
}
```

### Execute a single test method inside the Javactionfx-thread

In the code sample below, method `testFirst()` is annotated by [@TestInFxThread](src/main/java/com/github/actionfx/testing/annotation/TestInFxThread.java) and by that, this method is executed inside the JavaFX thread, while method `testSecond()` is **not** executed in the JavaFX thread.

```java
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ActionFXTest {

	@TestInFxThread
	@Test
	void testFirst() {
	}
	
	@Test
	void testSecond() {
	}
	
}
```

### Execute all test methods inside the JavaFX-thread

By using the annotation [@TestInFxThread](src/main/java/com/github/actionfx/testing/annotation/TestInFxThread.java) on class-level, all test methods are executed inside the JavaFX thread. There is no need to annotate the single test methods themselves.

```java
@TestInFxThread
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ActionFXTest {

	@Test
	void testFirst() {
	}
	
	@Test
	void testSecond() {
	}
	
}
```