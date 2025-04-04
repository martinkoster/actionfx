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

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.github.actionfx.appfactory.config.ControllerFactoryConfig;
import com.github.actionfx.appfactory.config.MainAppFactoryConfig;
import com.github.actionfx.appfactory.utils.FileUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Factory implementation for producing ActionFX main classes.
 *
 * @author koster
 *
 */
public class MainAppFactory {

    private final MainAppFactoryConfig mainAppFactoryConfig;

    private final ControllerFactoryConfig controllerFactoryConfig;

    private final Consumer<String> logConsumer;

    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Not relevant for AppFactory.")
    public MainAppFactory(final MainAppFactoryConfig factoryConfig,
            final ControllerFactoryConfig controllerFactoryConfig, final Consumer<String> logConsumer) {
        mainAppFactoryConfig = factoryConfig;
        this.controllerFactoryConfig = controllerFactoryConfig;
        this.logConsumer = logConsumer;
    }

    public void produce() {
        createRootProjectFolder();
        setupProjectStructure();
        createBuildGradle();
        final ControllerFactory controllerFactory = new ControllerFactory(controllerFactoryConfig, logConsumer);
        prepareFxmlView();
        createMainApplicationClass(controllerFactory);
        createActionFXMainController(controllerFactory);
    }

    private void createBuildGradle() {
        final GradleBuildModel gradleBuildModel = createBuildGradleModel();
        FreemarkerConfiguration.getInstance().writeTemplate("gradle/build.gradle.ftl", gradleBuildModel,
                new File(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "build.gradle"));
        FreemarkerConfiguration.getInstance().writeTemplate("gradle/settings.gradle.ftl", gradleBuildModel,
                new File(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "settings.gradle"));
        FreemarkerConfiguration.getInstance().writeTemplate("gradle/gradle.properties.ftl", gradleBuildModel,
                new File(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "gradle.properties"));
        logConsumer.accept(
                "Created Gradle Build (build.gradle, settings.gradle, gradle.properties) in project root directory '"
                        + mainAppFactoryConfig.getAbsoluteProjectRootDirectory() + "'");
    }

    private void prepareFxmlView() {
        FileUtils.createDirectories(mainAppFactoryConfig.getAbsoluteFxmlRootDirectory());
        logConsumer.accept("Created directory for FXML resources under '"
                + mainAppFactoryConfig.getAbsoluteFxmlRootDirectory() + "'");
        controllerFactoryConfig.setAbsoluteProjectRootDirectory(mainAppFactoryConfig.getAbsoluteProjectRootDirectory());
        controllerFactoryConfig.setControllerPackageName(mainAppFactoryConfig.getControllerPackageName());
        FileUtils.createDirectories(controllerFactoryConfig.getAbsoluteControllerDirectory());
        logConsumer.accept("Created directory for ActionFX controller under '"
                + controllerFactoryConfig.getAbsoluteControllerDirectory() + "'");
        if (mainAppFactoryConfig.isCreateEmptyMainView()) {
            FileUtils.copyClasspathFile("/templates/fxml/MainView.fxml",
                    controllerFactoryConfig.getAbsoluteFXMLResourcesDirectory());
            controllerFactoryConfig.setAbsoluteFxmlFilePath(
                    Path.of(controllerFactoryConfig.getAbsoluteFXMLResourcesDirectory(), "MainView.fxml").toString());
            logConsumer.accept("Created empty view with name 'MainView.fxml'");
        }
    }

    private void createMainApplicationClass(final ControllerFactory controllerFactory) {
        FileUtils.createDirectories(mainAppFactoryConfig.getAbsoluteMainAppDirectory());
        final MainAppModel mainAppModel = createMainAppModel(controllerFactory.getViewId());
        if (mainAppFactoryConfig.isUseSpring()) {
            FreemarkerConfiguration.getInstance().writeTemplate("classes/main-spring.ftl", mainAppModel,
                    new File(mainAppFactoryConfig.getAbsoluteMainAppDirectory(),
                            mainAppFactoryConfig.getMainAppClassName() + ".java"));
        } else {
            FreemarkerConfiguration.getInstance().writeTemplate("classes/main-default.ftl", mainAppModel,
                    new File(mainAppFactoryConfig.getAbsoluteMainAppDirectory(),
                            mainAppFactoryConfig.getMainAppClassName() + ".java"));
        }
        logConsumer.accept("Created main application class in folder '"
                + mainAppFactoryConfig.getAbsoluteMainAppDirectory() + "'");
    }

    private void createActionFXMainController(final ControllerFactory controllerFactory) {
        controllerFactory.produce();
        logConsumer.accept("Created FXML-based main view with controller in package '"
                + controllerFactoryConfig.getControllerPackageName() + "'");
    }

    private void createRootProjectFolder() {
        FileUtils.createDirectories(mainAppFactoryConfig.getAbsoluteProjectRootDirectory());
        logConsumer.accept(
                "Created project root directory '" + mainAppFactoryConfig.getAbsoluteProjectRootDirectory() + "'");
    }

    private void setupProjectStructure() {
        final File projectFolder = new File(mainAppFactoryConfig.getAbsoluteProjectRootDirectory());
        FileUtils.unzip("/templates/project/project.zip", projectFolder);
        FileUtils.makeExecutable(Path.of(mainAppFactoryConfig.getAbsoluteProjectRootDirectory(), "gradlew").toString());
        logConsumer.accept("Setup folder structure for project '" + mainAppFactoryConfig.getName() + "' in '"
                + mainAppFactoryConfig.getAbsoluteProjectRootDirectory() + "'");
    }

    private GradleBuildModel createBuildGradleModel() {
        final GradleBuildModel model = new GradleBuildModel();
        model.setGroupId(mainAppFactoryConfig.getGroupId());
        model.setProjectName(mainAppFactoryConfig.getName());
        model.setUseSpring(mainAppFactoryConfig.isUseSpring());
        model.setPackageName(mainAppFactoryConfig.getRootPackageName());
        model.setMainAppClassName(mainAppFactoryConfig.getMainAppClassName());
        model.setActionFXVersion(mainAppFactoryConfig.getActionFXVersion());
        return model;
    }

    private MainAppModel createMainAppModel(final String mainViewId) {
        final MainAppModel model = new MainAppModel();
        model.setApplicationName(mainAppFactoryConfig.getName());
        model.setMainAppClassName(mainAppFactoryConfig.getMainAppClassName());
        model.setPackageName(mainAppFactoryConfig.getRootPackageName());
        model.setMainViewId(mainViewId);
        return model;
    }

    public static class MainAppModel {

        private String applicationName;

        private String mainAppClassName;

        private String mainViewId;

        private String packageName;

        public String getMainViewId() {
            return mainViewId;
        }

        public void setMainViewId(final String mainViewId) {
            this.mainViewId = mainViewId;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(final String packageName) {
            this.packageName = packageName;
        }

        public String getMainAppClassName() {
            return mainAppClassName;
        }

        public void setMainAppClassName(final String mainAppClassName) {
            this.mainAppClassName = mainAppClassName;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(final String applicationName) {
            this.applicationName = applicationName;
        }
    }

    public static class GradleBuildModel {

        private String groupId;

        private String projectName;

        private boolean useSpring;

        private String packageName;

        private String mainAppClassName;

        private String actionFXVersion;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(final String groupId) {
            this.groupId = groupId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(final String projectName) {
            this.projectName = projectName;
        }

        public boolean isUseSpring() {
            return useSpring;
        }

        public void setUseSpring(final boolean useSpring) {
            this.useSpring = useSpring;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(final String packageName) {
            this.packageName = packageName;
        }

        public String getMainAppClassName() {
            return mainAppClassName;
        }

        public void setMainAppClassName(final String mainAppClassName) {
            this.mainAppClassName = mainAppClassName;
        }

        public String getActionFXVersion() {
            return actionFXVersion;
        }

        public void setActionFXVersion(final String actionFXVersion) {
            this.actionFXVersion = actionFXVersion;
        }
    }

}
