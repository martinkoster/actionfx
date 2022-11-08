/**
 * Copyright (c) 2014, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.actionfx.core.decoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.actionfx.core.utils.AFXUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * This util class helps to add or remove decorations to nodes in the JavaFX scenegraph.
 *
 * <h3>Example</h3>
 * <p>
 *
 * <pre>
 * {@code
 * TextField textfield = new TextField();
 * Node decoration = ... // could be an ImageView or any Node!
 * DecorationUtils.addDecoration(textfield, new GraphicDecoration(decoration, Pos.CENTER_RIGHT));}
 * </pre>
 *
 * <p>
 * Similarly, if we wanted to add a CSS style class (e.g. because we have some css that knows to make the 'warning'
 * style class turn the TextField a lovely shade of bright red, we would simply do the following:
 *
 * <pre>
 * {@code
 * TextField textfield = new TextField();
 * Decorator.addDecoration(textfield, new StyleClassDecoration("warning");}
 * </pre>
 * <p>
 * This class is inspired by ControlsFX' <a href=
 * "https://controlsfx.github.io/javadoc/11.1.1/org.controlsfx.controls/org/controlsfx/control/decoration/Decorator.html">Decorator</a>,
 * but refactored to ActionFX's needs, Java 11 and Clean Code standards.
 *
 */
public class DecorationUtils {

    private static final String DECORATIONS_PROPERTY_KEY = "actionfx-decorations";

    private DecorationUtils() {
        // class can not be instantiated
    }

    /**
     * Adds the given decoration to the given node.
     *
     * @param target
     *            The node to add the decoration to.
     * @param decoration
     *            The decoration to add to the node.
     */
    public static final void addDecoration(final Node target, final Decoration decoration) {
        getDecorations(target).add(decoration);
        getDecorationPane(target,
                pane -> pane.addDecorationsToNode(target, FXCollections.observableArrayList(decoration)));
    }

    /**
     * Removes the given decoration from the given node.
     *
     * @param target
     *            The node to remove the decoration from.
     * @param decoration
     *            The decoration to remove from the node.
     */
    public static final void removeDecoration(final Node target, final Decoration decoration) {
        getDecorations(target).remove(decoration);
        getDecorationPane(target,
                pane -> pane.removeDecorationsFromNode(target, FXCollections.observableArrayList(decoration)));
    }

    /**
     * Removes all the decorations that have previously been set on the given node.
     *
     * @param target
     *            The node from which all previously set decorations should be removed.
     */
    public static final void removeAllDecorations(final Node target) {
        final List<Decoration> decorations = getDecorations(target);
        final List<Decoration> removed = FXCollections.observableArrayList(decorations);

        target.getProperties().remove(DECORATIONS_PROPERTY_KEY);
        getDecorationPane(target, pane -> pane.removeDecorationsFromNode(target, removed));
    }

    /**
     * Returns all the currently set decorations for the given node.
     *
     * @param target
     *            The node for which all currently set decorations are required.
     * @return an ObservableList of the currently set decorations for the given node.
     */
    @SuppressWarnings("unchecked")
    public static final ObservableList<Decoration> getDecorations(final Node target) {
        return (ObservableList<Decoration>) target.getProperties().computeIfAbsent(DECORATIONS_PROPERTY_KEY,
                key -> FXCollections.observableArrayList());
    }

    private static List<Scene> currentlyInstallingScenes = new ArrayList<>();

    private static Map<Scene, List<Consumer<DecorationPane>>> pendingTasksByScene = new HashMap<>();

    private static void getDecorationPane(final Node target, final Consumer<DecorationPane> task) {
        // find a DecorationPane parent and notify it that a node has updated
        // decorations. If a DecorationPane doesn't exist, we install it into
        // the scene. If a Scene does not exist, we add a listener to try again
        // when a scene is available.

        final DecorationPane pane = getDecorationPaneInParentHierarchy(target);

        if (pane != null) {
            task.accept(pane);
        } else {
            // install decoration pane
            final Consumer<Scene> sceneConsumer = scene -> {
                if (currentlyInstallingScenes.contains(scene)) {
                    List<Consumer<DecorationPane>> pendingTasks = pendingTasksByScene.get(scene);
                    if (pendingTasks == null) {
                        pendingTasks = new LinkedList<>();
                        pendingTasksByScene.put(scene, pendingTasks);
                    }
                    pendingTasks.add(task);
                    return;
                }

                DecorationPane decorationPane = getDecorationPaneInParentHierarchy(target);
                if (decorationPane == null) {
                    currentlyInstallingScenes.add(scene);
                    decorationPane = new DecorationPane();
                    final Parent oldRoot = scene.getRoot();
                    decorationPane.getStylesheets().addAll(oldRoot.getStylesheets());
                    AFXUtils.injectAsRootPane(scene, decorationPane);
                    decorationPane.setRoot(oldRoot);
                    currentlyInstallingScenes.remove(scene);
                }

                task.accept(decorationPane);
                final List<Consumer<DecorationPane>> pendingTasks = pendingTasksByScene.remove(scene);
                if (pendingTasks != null) {
                    for (final Consumer<DecorationPane> pendingTask : pendingTasks) {
                        pendingTask.accept(decorationPane);
                    }
                }
            };

            final Scene scene = target.getScene();
            if (scene != null) {
                sceneConsumer.accept(scene);
            } else {
                AFXUtils.executeOnceWhenPropertyIsNonNull(target.sceneProperty(), sceneConsumer);
            }
        }
    }

    private static DecorationPane getDecorationPaneInParentHierarchy(final Node target) {
        Parent p = target.getParent();
        while (p != null) {
            if (p instanceof DecorationPane) {
                return (DecorationPane) p;
            }
            p = p.getParent();
        }
        return null;
    }
}
