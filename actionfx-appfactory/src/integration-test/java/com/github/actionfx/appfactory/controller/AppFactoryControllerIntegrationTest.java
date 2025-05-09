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
package com.github.actionfx.appfactory.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.appfactory.ActionFXAppFactory.ActionFXAppFactoryApplication;
import com.github.actionfx.appfactory.config.MainAppFactoryConfig;
import com.github.actionfx.appfactory.text.TestUtils;
import com.github.actionfx.core.ActionFX;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit integration test for {@link AppFactoryController}.
 *
 * @author koster
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class AppFactoryControllerIntegrationTest {

    @AfterEach
    void resetActionFX() {
        ActionFX.getInstance().reset();
    }

    @Test
    void testGenerateProject_emptyView_withDefaultContainer() throws IOException {
        // GIVEN
        final ActionFX actionFX = ActionFX.builder().configurationClass(ActionFXAppFactoryApplication.class).build();
        actionFX.scanForActionFXComponents();
        final AppFactoryController controller = actionFX.getBean(AppFactoryController.class);
        final Path tmpFolder = Files.createTempDirectory("project");
        controller.rootDirectoryTextField.setText(tmpFolder.toString());
        // compile against the previous version, as new version is not yet necessarily published to Maven Central.
        controller.actionFXVersionComboBox.setValue(MainAppFactoryConfig.PREVIOUS_ACTIONFX_VERSION);

        // WHEN
        controller.createNewProjectButtonAction();

        // THEN
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertThat(runGradleWrapper(tmpFolder, "assemble")).isZero();
    }

    @Test
    void testGenerateProject_emptyView_withSpringContainer() throws IOException {
        // GIVEN
        final ActionFX actionFX = ActionFX.builder().configurationClass(ActionFXAppFactoryApplication.class).build();
        actionFX.scanForActionFXComponents();
        final AppFactoryController controller = actionFX.getBean(AppFactoryController.class);
        final Path tmpFolder = Files.createTempDirectory("project");
        controller.rootDirectoryTextField.setText(tmpFolder.toString());
        // compile against the previous version, as new version is not yet necessarily published to Maven Central.
        controller.actionFXVersionComboBox.setValue(MainAppFactoryConfig.PREVIOUS_ACTIONFX_VERSION);

        // activate Spring
        controller.useSpringCheckBox.setSelected(true);

        // WHEN
        controller.createNewProjectButtonAction();

        // THEN
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertThat(runGradleWrapper(tmpFolder, "assemble")).isZero();
    }

    @Test
    void testGenerateProject_existingView_withDefaultContainer() throws IOException {
        // GIVEN
        final ActionFX actionFX = ActionFX.builder().configurationClass(ActionFXAppFactoryApplication.class).build();
        actionFX.scanForActionFXComponents();
        final AppFactoryController controller = actionFX.getBean(AppFactoryController.class);
        final Path tmpFolder = Files.createTempDirectory("project");
        controller.rootDirectoryTextField.setText(tmpFolder.toString());
        // compile against the previous version, as new version is not yet necessarily published to Maven Central.
        controller.actionFXVersionComboBox.setValue(MainAppFactoryConfig.PREVIOUS_ACTIONFX_VERSION);

        // use existing view
        TestUtils.copyClasspathFile("/fxml/ExistingFxmlView.fxml", tmpFolder);
        controller.projectFxmlTextField.setText(Path.of(tmpFolder.toString(), "ExistingFxmlView.fxml").toString());
        controller.existingFxmlRadioButton.setSelected(true);

        // WHEN
        controller.createNewProjectButtonAction();

        // THEN
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertThat(runGradleWrapper(tmpFolder, "assemble")).isZero();
    }

    @Test
    void testGenerateProject_existingView_withSpringContainer() throws IOException {
        // GIVEN
        final ActionFX actionFX = ActionFX.builder().configurationClass(ActionFXAppFactoryApplication.class).build();
        actionFX.scanForActionFXComponents();
        final AppFactoryController controller = actionFX.getBean(AppFactoryController.class);
        final Path tmpFolder = Files.createTempDirectory("project");
        controller.rootDirectoryTextField.setText(tmpFolder.toString());
        // compile against the previous version, as new version is not yet necessarily published to Maven Central.
        controller.actionFXVersionComboBox.setValue(MainAppFactoryConfig.PREVIOUS_ACTIONFX_VERSION);

        // activate Spring
        controller.useSpringCheckBox.setSelected(true);

        // use existing view
        TestUtils.copyClasspathFile("/fxml/ExistingFxmlView.fxml", tmpFolder);
        controller.projectFxmlTextField.setText(Path.of(tmpFolder.toString(), "ExistingFxmlView.fxml").toString());
        controller.existingFxmlRadioButton.setSelected(true);

        // WHEN
        controller.createNewProjectButtonAction();

        // THEN
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertThat(runGradleWrapper(tmpFolder, "assemble")).isZero();
    }

    private static int runGradleWrapper(final Path projectDir, final String gradleTask) {
        final List<String> commands = new ArrayList<>();
        commands.add(Path.of(projectDir.toString(), getGradleWrapperExecutableName()).toString());
        commands.add(gradleTask);
        final ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(projectDir.toFile());
        processBuilder.redirectErrorStream(true);
        try {
            final Process process = processBuilder.start();
            final StreamGobbler gobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            gobbler.run();
            process.waitFor();
            return process.exitValue();
        } catch (final IOException | InterruptedException e) {
            throw new IllegalStateException("Unable to start command '" + commands.get(0) + "'!", e);
        }
    }

    private static String getGradleWrapperExecutableName() {
        final String os = System.getProperty("os.name");
        return os.toLowerCase().contains("win") ? "gradlew.bat" : "gradlew";
    }

    public static class StreamGobbler extends Thread {

        private final InputStream is;

        private final Consumer<String> logConsumer;

        /**
         * Standard constructor.
         *
         * @param is
         *            the input stream to read from
         * @param logConsumer
         *            the log consumer to pass on the read log messages
         */
        public StreamGobbler(final InputStream is, final Consumer<String> logConsumer) {
            this.is = is;
            this.logConsumer = logConsumer;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                final BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    logConsumer.accept(line);
                }
            } catch (final IOException ioe) {

            }
        }

    }
}
