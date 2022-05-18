/*
 * Copyright (c) 2021 Martin Koster
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.actionfx.appfactory.factories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.actionfx.appfactory.config.ControllerFactoryConfig;
import com.github.actionfx.appfactory.config.MainAppFactoryConfig;
import com.github.actionfx.appfactory.text.TestUtils;

/**
 * JUnit test case for {@link MainAppFactory}.
 *
 * @author koster
 *
 */
class MainAppFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testProduce_withEmptyView_withDefaultContainer() throws IOException {
        // GIVEN
        final Consumer<String> logConsumer = Mockito.mock(Consumer.class);
        final MainAppFactoryConfig mainAppFactoryConfig = new MainAppFactoryConfig();
        final Path tmpFolder = Files.createTempDirectory("project");
        mainAppFactoryConfig.setAbsoluteProjectRootDirectory(tmpFolder.toString());

        // empty view with default container
        mainAppFactoryConfig.setCreateEmptyMainView(true);
        mainAppFactoryConfig.setUseSpring(false);

        mainAppFactoryConfig.setGroupId("com.github.martinkoster");
        mainAppFactoryConfig.setMainAppClassName("TestApplication");
        mainAppFactoryConfig.setName("test-project");
        mainAppFactoryConfig.setRootPackageName("com.github.testproject");

        final ControllerFactoryConfig controllerFactoryConfig = new ControllerFactoryConfig();
        controllerFactoryConfig.setAbsoluteProjectRootDirectory(tmpFolder.toString());
        controllerFactoryConfig.setControllerPackageName("com.github.testproject.controller");

        final MainAppFactory mainAppFactory = new MainAppFactory(mainAppFactoryConfig, controllerFactoryConfig,
                logConsumer);

        // WHEN
        mainAppFactory.produce();

        // THEN
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "build.gradle").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "settings.gradle").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "gradle.properties").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteMainAppDirectory(), "TestApplication.java").toString());
        TestUtils.assertFileContentIdentical(
                Path.of(controllerFactoryConfig.getAbsoluteFXMLResourcesDirectory(), "MainView.fxml").toString(),
                "/templates/fxml/MainView.fxml");
        TestUtils.assertFileExists(
                Path.of(controllerFactoryConfig.getAbsoluteControllerDirectory(), "MainController.java").toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testProduce_withEmptyView_withSpringContainer() throws IOException {
        // GIVEN
        final Consumer<String> logConsumer = Mockito.mock(Consumer.class);
        final MainAppFactoryConfig mainAppFactoryConfig = new MainAppFactoryConfig();
        final Path tmpFolder = Files.createTempDirectory("project");
        mainAppFactoryConfig.setAbsoluteProjectRootDirectory(tmpFolder.toString());

        // empty view with Spring container
        mainAppFactoryConfig.setCreateEmptyMainView(true);
        mainAppFactoryConfig.setUseSpring(true);

        mainAppFactoryConfig.setGroupId("com.github.martinkoster");
        mainAppFactoryConfig.setMainAppClassName("TestApplication");
        mainAppFactoryConfig.setName("test-project");
        mainAppFactoryConfig.setRootPackageName("com.github.testproject");

        final ControllerFactoryConfig controllerFactoryConfig = new ControllerFactoryConfig();
        controllerFactoryConfig.setAbsoluteProjectRootDirectory(tmpFolder.toString());
        controllerFactoryConfig.setControllerPackageName("com.github.testproject.controller");

        final MainAppFactory mainAppFactory = new MainAppFactory(mainAppFactoryConfig, controllerFactoryConfig,
                logConsumer);

        // WHEN
        mainAppFactory.produce();

        // THEN
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "build.gradle").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "settings.gradle").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "gradle.properties").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteMainAppDirectory(), "TestApplication.java").toString());
        TestUtils.assertFileContentIdentical(
                Path.of(controllerFactoryConfig.getAbsoluteFXMLResourcesDirectory(), "MainView.fxml").toString(),
                "/templates/fxml/MainView.fxml");
        TestUtils.assertFileExists(
                Path.of(controllerFactoryConfig.getAbsoluteControllerDirectory(), "MainController.java").toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testProduce_withExistingFXMLView_withDefaultContainer() throws IOException {
        // GIVEN
        final Consumer<String> logConsumer = Mockito.mock(Consumer.class);
        final MainAppFactoryConfig mainAppFactoryConfig = new MainAppFactoryConfig();
        final Path tmpFolder = Files.createTempDirectory("project");
        mainAppFactoryConfig.setAbsoluteProjectRootDirectory(tmpFolder.toString());
        TestUtils.copyClasspathFile("/fxml/ExistingFxmlView.fxml", tmpFolder);

        // empty existing FXML view with default container
        mainAppFactoryConfig.setCreateEmptyMainView(false);
        mainAppFactoryConfig.setUseSpring(false);

        mainAppFactoryConfig.setGroupId("com.github.martinkoster");
        mainAppFactoryConfig.setMainAppClassName("TestApplication");
        mainAppFactoryConfig.setName("test-project");
        mainAppFactoryConfig.setRootPackageName("com.github.testproject");

        final ControllerFactoryConfig controllerFactoryConfig = new ControllerFactoryConfig();
        controllerFactoryConfig.setAbsoluteProjectRootDirectory(tmpFolder.toString());
        controllerFactoryConfig.setControllerPackageName("com.github.testproject.controller");
        controllerFactoryConfig
                .setAbsoluteFxmlFilePath(Path.of(tmpFolder.toString(), "ExistingFxmlView.fxml").toString());

        final MainAppFactory mainAppFactory = new MainAppFactory(mainAppFactoryConfig, controllerFactoryConfig,
                logConsumer);

        // WHEN
        mainAppFactory.produce();

        // THEN
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "build.gradle").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "settings.gradle").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "gradle.properties").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteMainAppDirectory(), "TestApplication.java").toString());
        TestUtils.assertFileContentIdentical(Path
                .of(controllerFactoryConfig.getAbsoluteFXMLResourcesDirectory(), "ExistingFxmlView.fxml").toString(),
                "/fxml/ExistingFxmlView.fxml");
        TestUtils.assertFileExists(
                Path.of(controllerFactoryConfig.getAbsoluteControllerDirectory(), "ExistingFxmlController.java")
                        .toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testProduce_withExistingFXMLView_withSpringContainer() throws IOException {
        // GIVEN
        final Consumer<String> logConsumer = Mockito.mock(Consumer.class);
        final MainAppFactoryConfig mainAppFactoryConfig = new MainAppFactoryConfig();
        final Path tmpFolder = Files.createTempDirectory("project");
        mainAppFactoryConfig.setAbsoluteProjectRootDirectory(tmpFolder.toString());
        TestUtils.copyClasspathFile("/fxml/ExistingFxmlView.fxml", tmpFolder);

        // empty existing FXML view with Spring container
        mainAppFactoryConfig.setCreateEmptyMainView(false);
        mainAppFactoryConfig.setUseSpring(true);

        mainAppFactoryConfig.setGroupId("com.github.martinkoster");
        mainAppFactoryConfig.setMainAppClassName("TestApplication");
        mainAppFactoryConfig.setName("test-project");
        mainAppFactoryConfig.setRootPackageName("com.github.testproject");

        final ControllerFactoryConfig controllerFactoryConfig = new ControllerFactoryConfig();
        controllerFactoryConfig.setAbsoluteProjectRootDirectory(tmpFolder.toString());
        controllerFactoryConfig.setControllerPackageName("com.github.testproject.controller");
        controllerFactoryConfig
                .setAbsoluteFxmlFilePath(Path.of(tmpFolder.toString(), "ExistingFxmlView.fxml").toString());

        final MainAppFactory mainAppFactory = new MainAppFactory(mainAppFactoryConfig, controllerFactoryConfig,
                logConsumer);

        // WHEN
        mainAppFactory.produce();

        // THEN
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "build.gradle").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "settings.gradle").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "gradle.properties").toString());
        TestUtils.assertFileExists(
                Path.of(mainAppFactoryConfig.getAbsoluteMainAppDirectory(), "TestApplication.java").toString());
        TestUtils.assertFileContentIdentical(Path
                .of(controllerFactoryConfig.getAbsoluteFXMLResourcesDirectory(), "ExistingFxmlView.fxml").toString(),
                "/fxml/ExistingFxmlView.fxml");
        TestUtils.assertFileExists(
                Path.of(controllerFactoryConfig.getAbsoluteControllerDirectory(), "ExistingFxmlController.java")
                        .toString());
    }

}
