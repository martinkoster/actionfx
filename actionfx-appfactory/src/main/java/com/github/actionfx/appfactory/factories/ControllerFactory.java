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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.github.actionfx.appfactory.config.ControllerFactoryConfig;
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

	private final ControllerFactoryConfig factoryConfig;

	public ControllerFactory(final ControllerFactoryConfig factoryConfig) {
		this.factoryConfig = factoryConfig;
	}

	public void produce(final String fxmlFile) {
		final FxmlDocument fxmlDocument = readFxmlDocument(fxmlFile);
		final ControllerModel controllerModel = createControllerModel(fxmlDocument, fxmlFile);
		FileUtils.createDirectories(factoryConfig.getAbsoluteControllerDirectory());
		final Path controllerPath = Path.of(factoryConfig.getAbsoluteControllerDirectory(),
				controllerModel.getControllerName() + ".java");
		try (final FileWriter fileWriter = new FileWriter(controllerPath.toFile(), StandardCharsets.UTF_8)) {
			FreemarkerConfiguration.getInstance().writeTemplate("classes/controller.ftl", controllerModel, fileWriter);
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot write file " + controllerPath.toAbsolutePath().toString() + "!", e);
		}
	}

	private ControllerModel createControllerModel(final FxmlDocument fxmlDocument, final String fxmlFile) {
		final ControllerModel controllerModel = new ControllerModel();
		controllerModel.setControllerName(deriveControllerName(fxmlFile));
		controllerModel.setPackageName(factoryConfig.getControllerPackageName());
		controllerModel.getImportStatements().addAll(fxmlDocument.getImportStatementsForIdNodes());
		controllerModel.setFxmlFile(deriveFxmlClasspathLocation(fxmlFile));
		controllerModel.setViewId(deriveViewId(fxmlFile));
		controllerModel.setTitle(deriveViewId(fxmlFile));
		controllerModel.getNodes().addAll(createFxmlNodes(fxmlDocument));
		controllerModel.getActionMethods().addAll(createActionMethods(fxmlDocument));
		return controllerModel;
	}

	private List<FxmlNode> createFxmlNodes(final FxmlDocument fxmlDocument) {
		return fxmlDocument.getIdNodesMap().entrySet().stream()
				.map(entry -> new FxmlNode(entry.getKey(), entry.getValue())).collect(Collectors.toList());
	}

	private List<ActionMethod> createActionMethods(final FxmlDocument fxmlDocument) {
		final List<ActionMethod> actionMethods = new ArrayList<>();

		// Methods that have an "onAction" property set
		actionMethods.addAll(
				fxmlDocument.getFxmlElementsAsStream().filter(elem -> !StringUtils.isBlank(elem.getOnActionProperty()))
						.map(elem -> mapToActionMethod(elem, false)).collect(Collectors.toList()));

		// Methods that have no "onAction" property set, but have the property itself +
		// ID
		actionMethods.addAll(fxmlDocument.getFxmlElementsAsStream()
				.filter(elem -> !StringUtils.isBlank(elem.getId()) && StringUtils.isBlank(elem.getOnActionProperty())
						&& supportsOnAction(elem))
				.map(elem -> mapToActionMethod(elem, true)).collect(Collectors.toList()));

		return actionMethods;
	}

	/**
	 * Maps the given {@code fxmlElement} to the {@link ActionMethod}
	 *
	 * @param fxmlElement              the FXML element
	 * @param useAFXOnActionAnnotation flag that indicates whether to use the
	 *                                 {@link AFXOnAction} annotation or not
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
	 * @param onActionName the "onAction" name, potentially starting with a "#"
	 * @return the stripped "onAction" name.
	 */
	private String stripHash(final String onActionName) {
		return onActionName != null && onActionName.startsWith("#") ? onActionName.substring(1) : onActionName;
	}

	/**
	 * Creates an "onAction" method name for a given JavaFX node with
	 * {@code nodeId}.
	 *
	 * @param nodeId the node ID
	 * @return the suggested "onAction" method name.
	 */
	private String createOnActionMethodName(final String nodeId) {
		return ReflectionUtils.decapitalizeBeanProperty(nodeId) + "Action";
	}

	/**
	 * Checks, whether the given {@code fxmlElement} has an "onAction" property.
	 *
	 * @param node the node type to check
	 * @return {@code true}, if the node type supports the "onAction" property,
	 *         {@code false} otherwise.
	 */
	private boolean supportsOnAction(final FxmlElement fxmlElement) {
		final Class<?> node = fxmlElement.asResolvedClass();
		return node != null && NodeWrapper.getOnActionPropertyField(node) != null;
	}

	private FxmlDocument readFxmlDocument(final String fxmlFile) {
		final FxmlParser parser = new FxmlParser();
		try (FileInputStream fileInputStream = new FileInputStream(new File(fxmlFile))) {
			return parser.parseFxml(fileInputStream);
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot read file " + fxmlFile + "!", e);
		}
	}

	/**
	 * Derives the name of the ActionFX controller from the given {@code fxmlFile}
	 * name.
	 *
	 * @param fxmlFile the FXML file name
	 * @return the derived controller name
	 */
	private String deriveControllerName(final String fxmlFile) {
		final String nameWithoutExtension = getFilenameWithoutExtension(fxmlFile);
		return nameWithoutExtension.endsWith("View")
				? nameWithoutExtension.substring(0, nameWithoutExtension.indexOf("View")) + "Controller"
				: nameWithoutExtension;
	}

	/**
	 * Derives the view ID from given {@code fxmlFile}. The view ID is the name of
	 * the FXML file without extension, extended by "View" (in case the FXML file is
	 * not ended by "View" anyway).
	 *
	 * @param fxmlFile the FXML file name
	 * @return the derived view ID
	 */
	private String deriveViewId(final String fxmlFile) {
		final String nameWithoutExtension = getFilenameWithoutExtension(fxmlFile);
		return nameWithoutExtension.endsWith("View") ? nameWithoutExtension : nameWithoutExtension + "View";
	}

	/**
	 * Derives the classpath location to the given {@code fxmlFile}.
	 *
	 * @param fxmlFile the FXML file name
	 * @return the classpath location to the {@code fxmlFile}
	 */
	private String deriveFxmlClasspathLocation(final String fxmlFile) {
		String classpathLocation;
		int idx = fxmlFile.indexOf(factoryConfig.getRelativeResourcesRootDirectory());
		if (idx >= 0) {
			classpathLocation = fxmlFile.substring(idx, factoryConfig.getRelativeResourcesRootDirectory().length());
		} else if (fxmlFile.contains(factoryConfig.getRelativeSourceRootDirectory())) {
			idx = fxmlFile.indexOf(factoryConfig.getRelativeSourceRootDirectory());
			classpathLocation = fxmlFile.substring(idx, factoryConfig.getRelativeSourceRootDirectory().length());
		} else {
			classpathLocation = Path.of(fxmlFile).getFileName().toString();
		}
		classpathLocation = classpathLocation.replace('\\', '/');
		return classpathLocation.startsWith("/") ? classpathLocation : "/" + classpathLocation;
	}

	private String getFilenameWithoutExtension(final String fxmlFile) {
		final Path fxmlPath = Path.of(fxmlFile);
		final String filename = fxmlPath.getFileName().toString();
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
	}

}
