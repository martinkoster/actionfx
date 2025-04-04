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
package com.github.actionfx.appfactory.fxparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents a parsed FXML document containing the single XML elements as
 * {@link FxmlElement}s.
 *
 * @author koster
 *
 */
public class FxmlDocument {

	// Simple node name -> fully qualified node name
	private final Map<String, String> imports = new TreeMap<>();

	private final FxmlElement rootElement;

	// ID -> simple node name
	private final Map<String, String> idNodesMap = new TreeMap<>();

	@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Not relevant for AppFactory.")
	public FxmlDocument(final FxmlElement rootElement, final Map<String, String> imports) {
		this.rootElement = rootElement;
		this.imports.putAll(imports);
	}

	@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Not relevant for AppFactory.")
	public FxmlElement getRootElement() {
		return rootElement;
	}

	@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Not relevant for AppFactory.")
	public Map<String, String> getImports() {
		return imports;
	}

	/**
	 * Returns the import statements as fully qualified class names of form
	 *
	 * <pre>
	 * javafx.scene.control.TableView
	 * </pre>
	 *
	 * @return the import statements as fully qualified class names
	 */
	public List<String> getImportsAsFullyQualifiedStatements() {
		return new ArrayList<>(imports.values());
	}

	/**
	 * Returns a subset of {@link #getImportsAsFullyQualifiedStatements()}, namely
	 * import statements for nodes with an "fx:id" attribute.
	 *
	 * @return import statements for nodes to be injected into a controller
	 */
	public List<String> getImportStatementsForIdNodes() {
		return idNodesMap.values().stream().map(imports::get).distinct().collect(Collectors.toList());
	}

	@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Not relevant for AppFactory.")
	public Map<String, String> getIdNodesMap() {
		return idNodesMap;
	}

//

	/**
	 * Returns the FXML elements of this document as a stream.
	 *
	 * @return the FXML elements as a stream
	 */
	public Stream<FxmlElement> getFxmlElementsAsStream() {
		return rootElement.getFxmlElementsAsStream();
	}
}
