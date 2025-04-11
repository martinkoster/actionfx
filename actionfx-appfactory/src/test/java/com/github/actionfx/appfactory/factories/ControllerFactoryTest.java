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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.github.actionfx.appfactory.config.ControllerFactoryConfig;
import com.github.actionfx.appfactory.text.TestUtils;
import com.github.actionfx.appfactory.utils.FileUtils;

/**
 * JUnit test case for {@link ControllerFactory}.
 *
 * @author koster
 *
 */
class ControllerFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testProduce_fxmlFileIsInNotInResourceLocation_itWillBeCopiedThere() throws IOException {
        // GIVEN
        final Consumer<String> logConsumer = Mockito.mock(Consumer.class);
        final ControllerFactoryConfig cfg = new ControllerFactoryConfig();
        final Path tmpFolder = Files.createTempDirectory("project");
        cfg.setAbsoluteProjectRootDirectory(tmpFolder.toAbsolutePath().toString());
        cfg.setAbsoluteFxmlFilePath(Path.of(tmpFolder.toString(), "MainView.fxml").toAbsolutePath().toString());
        cfg.setRelativeJavaSourceDirectory("src/main/java/");
        cfg.setRelativeFXMLResourcesDirectory("src/main/resources/");
        cfg.setControllerPackageName("com.github.actionfx.appfactory.controller");

        // put FXML somewhere outside project resources location
        TestUtils.copyClasspathFile("/fxml/MainView.fxml", tmpFolder);
        final ControllerFactory controllerFactory = new ControllerFactory(cfg, logConsumer);

        // WHEN
        controllerFactory.produce();

        // THEN
        TestUtils.assertFileContentIdentical(cfg.getAbsoluteControllerDirectory() + "/MainController.java",
                "/com/github/actionfx/appfactory/controller/MainController.java.produced");
        // target FXML has been created
        TestUtils.assertFileContentIdentical(cfg.getAbsoluteFXMLResourcesDirectory() + "/MainView.fxml",
                "/fxml/MainView.fxml");
        // source FXML file is unchanged in its location
        TestUtils.assertFileContentIdentical(tmpFolder.toAbsolutePath() + "/MainView.fxml", "/fxml/MainView.fxml");
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logConsumer, times(4)).accept(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(phrase -> phrase.contains("Created ActionFX controller directory"))
                .anyMatch(phrase -> phrase.contains("Created resources folder for FXML file in"))
                .anyMatch(phrase -> phrase.contains("Copied FXML file from"))
                .anyMatch(phrase -> phrase.contains("Created ActionFX controller with name"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testProduce_fxmlFileIsInAlreadyInResourceLocation_noActionTakenOnFxml() throws IOException {
        // GIVEN
        final Consumer<String> logConsumer = Mockito.mock(Consumer.class);
        final ControllerFactoryConfig cfg = new ControllerFactoryConfig();
        final Path tmpFolder = Files.createTempDirectory("project");
        cfg.setAbsoluteProjectRootDirectory(tmpFolder.toAbsolutePath().toString());
        cfg.setAbsoluteFxmlFilePath(
                Path.of(tmpFolder.toString(), "src/main/resources/MainView.fxml").toAbsolutePath().toString());
        cfg.setRelativeJavaSourceDirectory("src/main/java/");
        cfg.setRelativeFXMLResourcesDirectory("src/main/resources/");
        cfg.setControllerPackageName("com.github.actionfx.appfactory.controller");

        // put FXML file in project resources location
        FileUtils.createDirectories(cfg.getAbsoluteFXMLResourcesDirectory());
        TestUtils.copyClasspathFile("/fxml/MainView.fxml", Path.of(cfg.getAbsoluteFXMLResourcesDirectory()));
        final ControllerFactory controllerFactory = new ControllerFactory(cfg, logConsumer);

        // WHEN
        controllerFactory.produce();

        // THEN
        TestUtils.assertFileContentIdentical(cfg.getAbsoluteControllerDirectory() + "/MainController.java",
                "/com/github/actionfx/appfactory/controller/MainController.java.produced");
        TestUtils.assertFileContentIdentical(cfg.getAbsoluteFXMLResourcesDirectory() + "/MainView.fxml",
                "/fxml/MainView.fxml");
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logConsumer, times(2)).accept(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(phrase -> phrase.contains("Created ActionFX controller directory"))
                .anyMatch(phrase -> phrase.contains("Created ActionFX controller with name"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetViewId() {
        // GIVEN
        final Consumer<String> logConsumer = Mockito.mock(Consumer.class);
        final ControllerFactoryConfig cfg = new ControllerFactoryConfig();
        cfg.setAbsoluteFxmlFilePath("/some/folder/MainView.fxml");
        final ControllerFactory controllerFactory = new ControllerFactory(cfg, logConsumer);

        // WHEN and THEN
        assertThat(controllerFactory.getViewId()).isEqualTo("MainView");
    }

    @Test
    void testDeriveControllerName() {
        // WHEN and THEN
        assertThat(ControllerFactory.deriveControllerName("/somepath/somefile.fxml")).isEqualTo("SomefileController");
        assertThat(ControllerFactory.deriveControllerName("/somepath/a.fxml")).isEqualTo("AController");
        assertThat(ControllerFactory.deriveControllerName("/somepath/SomefileView.fxml")).isEqualTo("SomefileController");
        assertThatThrownBy(() -> ControllerFactory.deriveControllerName(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("must not be null");
        assertThatThrownBy(() -> ControllerFactory.deriveControllerName("")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("does not have an extension");
    }
}
