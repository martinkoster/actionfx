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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.github.actionfx.appfactory.config.ControllerFactoryConfig;
import com.github.actionfx.appfactory.config.MainAppFactoryConfig;
import com.github.actionfx.appfactory.fxparser.FxmlDocument;
import com.github.actionfx.appfactory.fxparser.FxmlElement;
import com.github.actionfx.appfactory.fxparser.FxmlParser;
import com.github.actionfx.appfactory.utils.FileUtils;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.graph.NodeWrapper;

/**
 * Factory implementation for producing ActionFX controller.
 *
 * @author koster
 *
 */
public class ControllerFactory {

    public static final String CONTROLLER_SUFFIX = "Controller";
    public static final String VIEW_SUFFIX = "View";
    private final ControllerFactoryConfig factoryConfig;

	private final Consumer<String> logConsumer;

    public ControllerFactory(final ControllerFactoryConfig factoryConfig, final Consumer<String> logConsumer) {
        this.factoryConfig = factoryConfig;
        this.logConsumer = logConsumer;
    }

    public void produce() {
        String fxmlFile = factoryConfig.getAbsoluteFxmlFilePath();
        if (!factoryConfig.getAbsoluteFxmlFilePath().contains(factoryConfig.getAbsoluteFXMLResourcesDirectory())) {
            // copy FXML file to project resource
            createFxmlResourcesDirectory();

            // FXML is relocated into the project directory structure
            fxmlFile = copyFxmlFileToResourcesDirectory(fxmlFile);
        }

        final FxmlDocument fxmlDocument = readFxmlDocument(fxmlFile);
        createControllerDirectory();
        final ControllerModel controllerModel = createControllerModel(fxmlDocument, fxmlFile);

        final Path controllerPath = Path.of(factoryConfig.getAbsoluteControllerDirectory(),
                controllerModel.getControllerName() + ".java");
        createController(controllerModel, controllerPath.toFile());
    }

    public String getViewId() {
        return deriveViewId(factoryConfig.getAbsoluteFxmlFilePath());
    }

    private void createControllerDirectory() {
        FileUtils.createDirectories(factoryConfig.getAbsoluteControllerDirectory());
        logConsumer.accept(
                "Created ActionFX controller directory '" + factoryConfig.getAbsoluteControllerDirectory() + "'");
    }

    private void createController(final ControllerModel controllerModel, final File targetFile) {
        FreemarkerConfiguration.getInstance().writeTemplate("classes/controller.ftl", controllerModel, targetFile);
        logConsumer.accept("Created ActionFX controller with name '" + controllerModel.getControllerName()
                + "' in folder '" + factoryConfig.getAbsoluteControllerDirectory() + "'");
    }

    private void createFxmlResourcesDirectory() {
        FileUtils.createDirectories(factoryConfig.getAbsoluteFXMLResourcesDirectory());
        logConsumer.accept("Created resources folder for FXML file in '"
                + factoryConfig.getAbsoluteFXMLResourcesDirectory() + "'");
    }

    private String copyFxmlFileToResourcesDirectory(final String fxmlFile) {
        FileUtils.copyFile(fxmlFile, factoryConfig.getAbsoluteFXMLResourcesDirectory());
        logConsumer.accept("Copied FXML file from '" + fxmlFile + "' to '"
                + factoryConfig.getAbsoluteFXMLResourcesDirectory() + "'");
        return factoryConfig.getAbsoluteFXMLResourcesDirectory() + "/" + Path.of(fxmlFile).getFileName();
    }

    private ControllerModel createControllerModel(final FxmlDocument fxmlDocument, final String fxmlFile) {
        final ControllerModel controllerModel = new ControllerModel();
        controllerModel.setControllerName(deriveControllerName(fxmlFile));
        controllerModel.setPackageName(factoryConfig.getControllerPackageName());
        controllerModel.getImportStatements().addAll(fxmlDocument.getImportStatementsForIdNodes());
        controllerModel.setFxmlFile(deriveFxmlClasspathLocation(fxmlFile));
        controllerModel.setViewId(getViewId());
        controllerModel.setTitle(deriveViewId(fxmlFile));
        controllerModel.getNodes().addAll(createFxmlNodes(fxmlDocument));
        controllerModel.getActionMethods().addAll(createActionMethods(fxmlDocument));
        return controllerModel;
    }

    private List<FxmlNode> createFxmlNodes(final FxmlDocument fxmlDocument) {
        return fxmlDocument.getIdNodesMap().entrySet().stream()
                .map(entry -> new FxmlNode(entry.getKey(), entry.getValue())).toList();
    }

    private List<ActionMethod> createActionMethods(final FxmlDocument fxmlDocument) {
        final List<ActionMethod> actionMethods = new ArrayList<>();

        // Methods that have an "onAction" property set
        actionMethods.addAll(
                fxmlDocument.getFxmlElementsAsStream().filter(elem -> !StringUtils.isBlank(elem.getOnActionProperty()))
                        .map(elem -> mapToActionMethod(elem, false)).toList());

        // Methods that have no "onAction" property set, but have the property itself +
        // ID
        actionMethods.addAll(fxmlDocument.getFxmlElementsAsStream()
                .filter(elem -> !StringUtils.isBlank(elem.getId()) && StringUtils.isBlank(elem.getOnActionProperty())
                        && supportsOnAction(elem))
                .map(elem -> mapToActionMethod(elem, true)).toList());

        // make method names unique
        return actionMethods.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Maps the given {@code fxmlElement} to the {@link ActionMethod}
     *
     * @param fxmlElement
     *            the FXML element
     * @param useAFXOnActionAnnotation
     *            flag that indicates whether to use the {@link AFXOnAction} annotation or not
     * @return the mapped action method
     */
    private ActionMethod mapToActionMethod(final FxmlElement fxmlElement, final boolean useAFXOnActionAnnotation) {
        final String nodeId = fxmlElement.getId();
        final String methodName = useAFXOnActionAnnotation ? createOnActionMethodName(nodeId)
                : stripHash(fxmlElement.getOnActionProperty());
        return new ActionMethod(nodeId, methodName, useAFXOnActionAnnotation);
    }

    /**
     * Strips away a potential "#" at the beginning of the onAction name from FXML.
     *
     * @param onActionName
     *            the "onAction" name, potentially starting with a "#"
     * @return the stripped "onAction" name.
     */
    private String stripHash(final String onActionName) {
        return onActionName != null && onActionName.startsWith("#") ? onActionName.substring(1) : onActionName;
    }

    /**
     * Creates an "onAction" method name for a given JavaFX node with {@code nodeId}.
     *
     * @param nodeId
     *            the node ID
     * @return the suggested "onAction" method name.
     */
    private String createOnActionMethodName(final String nodeId) {
        return ReflectionUtils.decapitalizeBeanProperty(nodeId) + "Action";
    }

    /**
     * Checks, whether the given {@code fxmlElement} has an "onAction" property.
     *
     * @param fxmlElement
     *            the node type to check
     * @return {@code true}, if the node type supports the "onAction" property, {@code false} otherwise.
     */
    private boolean supportsOnAction(final FxmlElement fxmlElement) {
        final Class<?> node = fxmlElement.asResolvedClass();
        return node != null && NodeWrapper.getOnActionPropertyField(node) != null;
    }

    private FxmlDocument readFxmlDocument(final String fxmlFile) {
        final FxmlParser parser = new FxmlParser();
        try (FileInputStream fileInputStream = new FileInputStream(fxmlFile)) {
            return parser.parseFxml(fileInputStream);
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot read file " + fxmlFile + "!", e);
        }
    }

    /**
     * Derives the name of the ActionFX controller from the given {@code fxmlFile} name.
     *
     * @param fxmlFile
     *            the FXML file name
     * @return the derived controller name
     */
    private String deriveControllerName(final String fxmlFile) {
        final String nameWithoutExtension = getFilenameWithoutExtension(fxmlFile);
        return nameWithoutExtension.endsWith(VIEW_SUFFIX)
                ? deriveControllerNameByViewNameEndingWithView(nameWithoutExtension)
                : deriveControllerNameByViewName(nameWithoutExtension);
    }

    private static String deriveControllerNameByViewName(final String nameWithoutExtension) {
        return nameWithoutExtension.length() == 1 ? nameWithoutExtension.substring(0, 1).toUpperCase() + CONTROLLER_SUFFIX :
                nameWithoutExtension.substring(0, 1).toUpperCase() + nameWithoutExtension.substring(1, nameWithoutExtension.length()) + CONTROLLER_SUFFIX;
    }

    private static String deriveControllerNameByViewNameEndingWithView(final String nameWithoutExtension) {
        return nameWithoutExtension.substring(0, nameWithoutExtension.indexOf(VIEW_SUFFIX)) + CONTROLLER_SUFFIX;
    }

    /**
     * Derives the view ID from given {@code fxmlFile}. The view ID is the name of the FXML file without extension,
     * extended by "View" (in case the FXML file is not ended by "View" anyway).
     *
     * @param fxmlFile
     *            the FXML file name
     * @return the derived view ID
     */
    private String deriveViewId(final String fxmlFile) {
        final String nameWithoutExtension = getFilenameWithoutExtension(fxmlFile);
        return nameWithoutExtension.endsWith("View") ? nameWithoutExtension : nameWithoutExtension + "View";
    }

    /**
     * Derives the classpath location to the given {@code fxmlFile}.
     *
     * @param fxmlFile
     *            the FXML file name
     * @return the classpath location to the {@code fxmlFile}
     */
    private String deriveFxmlClasspathLocation(final String fxmlFile) {
        if (fxmlFile == null) {
            throw new IllegalArgumentException("Argument 'fxmlFile' must not be null");
        }
        String classpathLocation = null;
        final int resourceIdx = fxmlFile.indexOf(MainAppFactoryConfig.DEFAULT_RESOURCES_DIR);
        final int relativeFxmlIdx = fxmlFile.indexOf(factoryConfig.getRelativeFXMLResourcesDirectory());
        if (resourceIdx >= 0) {
            classpathLocation = fxmlFile.substring(resourceIdx + MainAppFactoryConfig.DEFAULT_RESOURCES_DIR.length());
        } else if (relativeFxmlIdx >= 0) {
            classpathLocation = fxmlFile
                    .substring(relativeFxmlIdx + factoryConfig.getRelativeFXMLResourcesDirectory().length());
        } else {
            final Path fileName = Path.of(fxmlFile).getFileName();
            if (fileName == null) {
                throw new IllegalArgumentException("Argument 'fxmlFile' does not point to a valid file.");
            }
            classpathLocation = fileName.toString();
        }
        classpathLocation = classpathLocation.replace('\\', '/');
        return classpathLocation.startsWith("/") ? classpathLocation : "/" + classpathLocation;
    }

    private String getFilenameWithoutExtension(final String fxmlFile) {
        if (fxmlFile == null) {
            throw new IllegalArgumentException("Argument 'fxmlFile' must not be null");
        }
        final Path fxmlPath = Path.of(fxmlFile);
        final Path fileName = fxmlPath.getFileName();
        if (fileName == null) {
            throw new IllegalArgumentException("Argument 'fxmlFile' does not point to a valid file.");
        }
        final String filename = fileName.toString();
        return filename.substring(0, filename.lastIndexOf("."));
    }

    /**
     * Data model that contains information about the ActionFX controller to create.
     *
     * @author koster
     *
     */
    public static class ControllerModel {

        private String packageName;

        private final List<String> importStatements = new ArrayList<>();

        private String fxmlFile;

        private String viewId;

        private String controllerName;

        private String title;

        private final List<FxmlNode> nodes = new ArrayList<>();

        private final List<ActionMethod> actionMethods = new ArrayList<>();

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(final String packageName) {
            this.packageName = packageName;
        }

        public String getFxmlFile() {
            return fxmlFile;
        }

        public void setFxmlFile(final String fxmlFile) {
            this.fxmlFile = fxmlFile;
        }

        public String getViewId() {
            return viewId;
        }

        public void setViewId(final String viewId) {
            this.viewId = viewId;
        }

        public List<String> getImportStatements() {
            return importStatements;
        }

        public List<FxmlNode> getNodes() {
            return nodes;
        }

        public List<ActionMethod> getActionMethods() {
            return actionMethods;
        }

        public String getControllerName() {
            return controllerName;
        }

        public void setControllerName(final String controllerName) {
            this.controllerName = controllerName;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }
    }

    /**
     * Data model for a FXML node to be injected into a controller.
     *
     * @author koster
     *
     */
    public static class FxmlNode {

        private final String id;

        public FxmlNode(final String id, final String type) {
            this.id = id;
            this.type = type;
        }

        private final String type;

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * Data model for action methods inside a controller.
     *
     * @author koster
     *
     */
    public static class ActionMethod {

        private final String nodeId;

        private final String name;

        private final boolean useAFXOnActionAnnotation;

        public ActionMethod(final String nodeId, final String name, final boolean useAFXOnActionAnnotation) {
            this.nodeId = nodeId;
            this.name = name;
            this.useAFXOnActionAnnotation = useAFXOnActionAnnotation;
        }

        public String getName() {
            return name;
        }

        public boolean isUseAFXOnActionAnnotation() {
            return useAFXOnActionAnnotation;
        }

        public String getNodeId() {
            return nodeId;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ActionMethod other = (ActionMethod) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (name == null ? 0 : name.hashCode());
            return result;
        }

    }

}
