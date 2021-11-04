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

/**
 * Represents a single XML element inside a FXML document, while this element
 * can have again further {@link FxmlElement}s as children.s
 *
 * @author koster
 *
 */
public class FxmlElement {

	private final FxmlElement parent;

	private final List<FxmlElement> children = new ArrayList<>();

	private final String name;

	private final String importStatement;

	private String id = null;

	private String onActionProperty = null;

	public FxmlElement(final FxmlElement parent, final String name, final String importStatement) {
		this.parent = parent;
		this.name = name;
		this.importStatement = importStatement;
	}

	public FxmlElement getParent() {
		return parent;
	}

	public List<FxmlElement> getChildren() {
		return children;
	}

	public boolean hasId() {
		return id != null;
	}

	public boolean hasOnActionProperty() {
		return onActionProperty != null;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getOnActionProperty() {
		return onActionProperty;
	}

	public void setOnActionProperty(final String onActionProperty) {
		this.onActionProperty = onActionProperty;
	}

	public String getName() {
		return name;
	}

	public String getImportStatement() {
		return importStatement;
	}
}
