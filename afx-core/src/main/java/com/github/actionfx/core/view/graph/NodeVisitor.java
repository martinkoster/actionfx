/*
 * Copyright (c) 2020 Martin Koster
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
package com.github.actionfx.core.view.graph;

import javafx.scene.control.Tab;

/**
 * Functional handler for applying some logic during scene graph traversal. This
 * interface uses {@link NodeWrapper} for describing elements in the scene graph
 * and not directly {@link Node}s, because not all elements in the JavaFX scene
 * graph do inherit from {@link Node} (e.g. {@link Tab}s).
 *
 * @author koster
 *
 */
@FunctionalInterface
public interface NodeVisitor {

	/**
	 * Method is applied to the given {@code child} during scene graph traversal.
	 *
	 * @param parent the parent
	 * @param child  the child
	 * @return {@code true}, if the scene graph traversal shall be continued,
	 *         {@code false}, if the scene graph traversal shall be stopped.
	 */
	boolean visit(NodeWrapper parent, NodeWrapper child);

}
