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
package com.github.actionfx.core.test;

import java.util.ResourceBundle;

import com.github.actionfx.core.view.AbstractView;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 * Util class for creating views for testing.
 *
 * @author koster
 *
 */
public class ViewCreator {

	private ViewCreator() {
	}

	public static StaticView create(final Node node, final String nodeId) {
		node.setId(nodeId);
		return new StaticView("staticView", node);
	}

	public static StaticView create() {
		return new StaticView("staticView");
	}

	public static class StaticView extends AbstractView {

		public StaticView(final String viewId, final Node... node) {
			data.id = viewId;
			final AnchorPane pane = new AnchorPane();
			pane.getChildren().addAll(node);
			data.rootNode = pane;
		}

		@Override
		public Object getController() {
			return null;
		}

		public StaticView appendNode(final Node node, final String nodeId) {
			node.setId(nodeId);
			((AnchorPane) getRootNode()).getChildren().add(node);
			return this;
		}

		public StaticView resourceBundle(final ResourceBundle resourceBundle) {
			this.data.resourceBundle = resourceBundle;
			return this;
		}
	}
}
