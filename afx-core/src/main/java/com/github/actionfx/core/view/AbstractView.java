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
import java.util.List;

import com.github.actionfx.core.utils.AFXUtils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Abstract base class for view implementations.
 * 
 * @author koster
 *
 */
public abstract class AbstractView implements View {

	protected String id;

	protected Parent rootNode;

	protected String windowTitle;

	protected int width = -1;

	protected int height = -1;

	protected int posX = -1;

	protected int posY = -1;

	protected boolean maximized = false;

	protected boolean modalDialogue = false;

	protected String icon;

	protected final List<String> stylesheets = new ArrayList<>();

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Parent getRootNode() {
		return rootNode;
	}

	/**
	 * Applies the given list of stylesheets to this view.
	 * 
	 * @param stylesheetLocations the locations to the stylesheets to apply to this
	 *                            view
	 */
	public void applyStylesheets(List<String> stylesheetLocations) {
		// views can exist without being part of a scenegraph. We need to apply the
		// stylesheets, once the view is part of a scene
		rootNode.sceneProperty().addListener((observable, oldScene, newScene) -> {
			for (String location : stylesheetLocations) {
				newScene.getStylesheets().add(AbstractView.class.getResource(location).toExternalForm());
			}
		});
	}

	/**
	 * Shows the view in the supplied {@link Stage}.
	 * 
	 * @param stage the stage to show the view inside
	 */
	@Override
	public void show(Stage stage) {
		Scene scene = new Scene(getRootNode());
		stage.setScene(scene);
		initializeStage(stage);
		if (!isHeadlessMode()) {
			// this test is required for unit testing.
			stage.show();
		}
	}

	/**
	 * Shows the view in a new stage / new window.
	 */
	@Override
	public void show() {
		Stage stage = new Stage();
		Scene scene = new Scene(getRootNode());
		stage.setScene(scene);
		initializeStage(stage);
		if (!isHeadlessMode()) {
			// this test is required for unit testing.
			stage.show();
		}
	}

	/**
	 * Shows the view and waits in a modal fashion.
	 */
	@Override
	public void showAndWait() {
		Stage stage = new Stage();
		Scene scene = new Scene(getRootNode());
		stage.setScene(scene);
		initializeStage(stage);
		if (!isHeadlessMode()) {
			// this test is required for unit testing.
			stage.showAndWait();
		}
	}

	@Override
	public void hide() {
		if (getRootNode().sceneProperty().get() != null && getRootNode().sceneProperty().get().getWindow() != null) {
			getRootNode().sceneProperty().get().getWindow().hide();
		}
	}

	/**
	 * Initializes the given {@link Stage} with the parameters defined for this
	 * view.
	 * 
	 * @param stage the stage to initialize
	 */
	protected void initializeStage(Stage stage) {
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
