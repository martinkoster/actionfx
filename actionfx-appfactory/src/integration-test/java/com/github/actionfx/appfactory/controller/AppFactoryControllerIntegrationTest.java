/*  **************************************************************
 *   Projekt         : actionfx-appfactory (java)
 *  --------------------------------------------------------------
 *   Autor(en)       : MartinKoster
 *   Beginn-Datum    : 18.05.2022
 *  --------------------------------------------------------------
 *   copyright (c) 2015-18   DB Station&Service AG
 *   Alle Rechte vorbehalten.
 *  **************************************************************
 */
package com.github.actionfx.appfactory.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
import com.github.actionfx.appfactory.text.TestUtils;
import com.github.actionfx.core.ActionFX;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit integration test for {@link AppFactoryController}.
 *
 * @author MartinKoster
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
        controller.actionFXVersionComboBox.setValue("1.4.0");

        // WHEN
        controller.createNewProjectButtonAction();

        // THEN
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertThat(runGradleWrapper(tmpFolder, "assemble"), equalTo(0));
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
        controller.actionFXVersionComboBox.setValue("1.4.0");

        // activate Spring
        controller.useSpringCheckBox.setSelected(true);

        // WHEN
        controller.createNewProjectButtonAction();

        // THEN
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertThat(runGradleWrapper(tmpFolder, "assemble"), equalTo(0));
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
        controller.actionFXVersionComboBox.setValue("1.4.0");

        // use existing view
        TestUtils.copyClasspathFile("/fxml/ExistingFxmlView.fxml", tmpFolder);
        controller.projectFxmlTextField.setText(Path.of(tmpFolder.toString(), "ExistingFxmlView.fxml").toString());
        controller.existingFxmlRadioButton.setSelected(true);

        // WHEN
        controller.createNewProjectButtonAction();

        // THEN
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertThat(runGradleWrapper(tmpFolder, "assemble"), equalTo(0));
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
        controller.actionFXVersionComboBox.setValue("1.4.0");

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
        assertThat(runGradleWrapper(tmpFolder, "assemble"), equalTo(0));
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
