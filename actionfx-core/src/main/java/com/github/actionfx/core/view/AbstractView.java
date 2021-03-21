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
package com.github.actionfx.core.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.actionfx.core.utils.AFXUtils;
import com.github.actionfx.core.view.graph.NodeWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper.NodeAttacher;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Abstract base class for view implementations.
 *
 * @author koster
 *
 */
public abstract class AbstractView implements View {

	protected String id;

	protected Object rootNode;

	protected String windowTitle;

	protected int width = -1;

	protected int height = -1;

	protected int posX = -1;

	protected int posY = -1;

	protected boolean maximized = false;

	protected boolean modalDialogue = false;

	protected String icon;

	protected final List<String> stylesheets = new ArrayList<>();

	// each view instance holds its lookup cache, so that consecutive lookups are
	// not expensive (requires a tree traversal each time otherwise)
	protected final Map<String, NodeWrapper> lookupCache = Collections.synchronizedMap(new HashMap<>());

	@Override
	public String getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getRootNode() {
		return (T) rootNode;
	}

	/**
	 * Applies the set list of stylesheets to this view.
	 *
	 */
	protected void applyStylesheets() {
		// views can exist without being part of a scenegraph. We need to apply the
		// stylesheets, once the view is part of a scene
		if (Parent.class.isAssignableFrom(rootNode.getClass())) {
			((Parent) rootNode).sceneProperty().addListener((observable, oldScene, newScene) -> {
				for (final String location : stylesheets) {
					newScene.getStylesheets().add(AbstractView.class.getResource(location).toExternalForm());
				}
			});
		}
	}

	/**
	 * Shows the view in the supplied {@link Stage}.
	 *
	 * @param stage the stage to show the view inside
	 */
	@Override
	public void show(final Stage stage) {
		final Parent parent = getRootNode();
		// re-use a potentially existing scene
		final Scene scene = parent.getScene() != null ? parent.getScene() : new Scene(getRootNode());
		stage.setScene(scene);
		initializeStage(stage);
		if (!isHeadlessMode()) {
			// this test is required for unit testing.
			stage.show();
		}
	}

	/**
	 * Shows the view in the supplied {@link Stage}.
	 *
	 * @param popup the popup to show the view inside
	 * @param owner the window owning the popup
	 *
	 */
	@Override
	public void show(final Popup popup, final Window owner) {
		popup.getContent().clear();
		popup.getContent().add(getRootNode());
		popup.show(owner);
	}

	/**
	 * Shows the view in a new stage / new window.
	 */
	@Override
	public void show() {
		show(new Stage());
	}

	/**
	 * Shows the view and waits in a modal fashion.
	 */
	@Override
	public void showAndWait() {
		final Stage stage = new Stage();
		final Scene scene = new Scene(getRootNode());
		stage.setScene(scene);
		initializeStage(stage);
		if (!isHeadlessMode()) {
			// this test is required for unit testing.
			stage.showAndWait();
		}
	}

	@Override
	public void hide() {
		final Window window = getWindow();
		if (window != null) {
			window.hide();
		}
	}

	@Override
	public void attachViewToParent(final Parent parent, final NodeAttacher attacher) {
		final NodeWrapper wrapper = new NodeWrapper(parent);
		wrapper.attachNode(getRootNode(), attacher);
	}

	@Override
	public void detachView() {
		final Parent view = getRootNode();
		if (view.getParent() == null) {
			// view is not attached, so we can not detach
			return;
		}
		final NodeWrapper wrapper = new NodeWrapper(view.getParent());
		if (wrapper.supportsMultipleChildren()) {
			wrapper.getChildren().remove(view);
		} else if (wrapper.supportsSingleChild()) {
			wrapper.getSingleChildProperty().setValue(null);
		} else {
			throw new IllegalStateException("Removing view from node type '"
					+ view.getParent().getClass().getCanonicalName() + "' not possible!");
		}
	}

	@Override
	public Window getWindow() {
		return NodeWrapper.of(rootNode).getWindow();
	}

	@Override
	public Scene getScene() {
		return NodeWrapper.of(rootNode).getScene();
	}

	@Override
	public NodeWrapper lookupNode(final String nodeId) {
		return lookupCache.computeIfAbsent(nodeId, key -> {
			final NodeWrapper wrappedRootNode = NodeWrapper.of(getRootNode());
			return wrappedRootNode.lookup(key);
		});
	}

	/**
	 * Initializes the given {@link Stage} with the parameters defined for this
	 * view.
	 *
	 * @param stage the stage to initialize
	 */
	protected void initializeStage(final Stage stage) {
		// modality can be only set once
		if (modalDialogue) {
			stage.initModality(Modality.APPLICATION_MODAL);
		}
		if (icon != null && !"".equals(icon.trim())) {
			stage.getIcons().add(AFXUtils.loadImage(icon));
		}
		if (getWidth() != -1 && getHeight() != -1) {
			stage.setWidth(width);
			stage.setHeight(height);
		}
		if (getPosX() != -1 && getPosY() != -1) {
			stage.setX(getPosX());
			stage.setY(getPosY());
		}
		if (getWindowTitle() != null && !"".equals(getWindowTitle())) {
			stage.setTitle(getWindowTitle());
		}
		stage.setMaximized(maximized);
	}

	/**
	 * Checks, whether JavaFX is driven in "headless" mode.
	 *
	 * @return the flag that determines, whether JavaFX is run in headless mode.
	 */
	private boolean isHeadlessMode() {
		final String property = System.getProperty("monocle.platform");
		return property != null && property.equals("Headless");
	}

	public String getWindowTitle() {
		return windowTitle;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public boolean isMaximized() {
		return maximized;
	}

	public boolean isModalDialogue() {
		return modalDialogue;
	}

	public String getIcon() {
		return icon;
	}

	public List<String> getStylesheets() {
		return stylesheets;
	}

}
