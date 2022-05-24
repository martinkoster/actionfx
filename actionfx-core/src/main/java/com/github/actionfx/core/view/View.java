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

import java.util.ResourceBundle;
import java.util.stream.Stream;

import com.github.actionfx.core.bind.BindingTargetResolver;
import com.github.actionfx.core.view.graph.NodeWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper.NodeAttacher;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Interface for ActionFX views. It provides common routines to show or hide a view, or to lookup nodes within the scene
 * graph.
 *
 * @author koster
 *
 */
public interface View {

    /**
     * The view ID, unique over all views.
     *
     * @return the view ID
     */
    String getId();

    /**
     * Returns the root node of the view. The returned type is generic, because not all nodes of the scene graph do
     * inherit from {@link javafx.scene.Node} - e.g. tabs of type {@link javafx.scene.control.Tab} inside a
     * {@link TabPane}. However, in most of the cases, FXML views are based on view components based on type
     * {@link javafx.scene.Node}.
     *
     * @return the root node of the view
     */
    <T> T getRootNode();

    /**
     * Shows the view in the supplied {@link Stage}.
     *
     * @param stage
     *            the stage to show the view inside
     */
    void show(Stage stage);

    /**
     * Shows the view in the supplied {@link Stage}.
     *
     * @param popup
     *            the popup to show the view inside
     * @param owner
     *            the window owning the popup
     *
     */
    void show(Popup popup, Window owner);

    /**
     * Shows the view in a new stage / new window.
     */
    void show();

    /**
     * Shows the view and waits in a modal fashion.
     */
    void showAndWait();

    /**
     * Attaches this view as child to the supplied {@code parent}, using the given {@link NodeAttacher}. This method can
     * be used to compose a larger view out of multiple smaller views. An alternative use case could be to implement a
     * "docking" functionality, together with method {@link #detachView()} as the "undocking" operation.
     *
     * @param parent
     *            the parent to attach this view to
     * @param attacher
     *            the {@link NodeAttacher} that defines the strategy, how to attach the view to a node
     */
    void attachViewToParent(Parent parent, NodeAttacher attacher);

    /**
     * Detaches this view from the view node's parent.
     */
    void detachView();

    /**
     * Reattaches the a previously detached view again. In order for this operation to work, the view needs to have been
     * initally attached via {@link #attachViewToParent(Parent, NodeAttacher)}.
     */
    void reattachView();

    /**
     * Hides the view. This only works, when the view has its own stage.
     */
    void hide();

    /**
     * Gets the {@link Window} where this view is currently displayed in. In case the view has not yet been displayed,
     * {@code null} is returned.
     *
     * @return the {@link Window} in that this view is displayed, or {@code null}, in case the view has not yet been
     *         displayed.
     */
    Window getWindow();

    /**
     * Gets the {@link Scene} that this view is part of. In case the view has not yet been added to a scene,
     * {@code null} is returned.
     *
     * @return the {@link Scene} that this view is part of, or {@code null}, in case the view has not been added to a
     *         scene.
     */
    Scene getScene();

    /**
     * Gets the controller instance that handles actions from this view.
     *
     * @return the controller instance
     */
    Object getController();

    /**
     * Gets the resource bundle associated with this view.
     *
     * @return the resource bundle
     */
    ResourceBundle getResourceBundle();

    /**
     * Looks up a node that carries the supplied {@code nodeId}.
     *
     * @param nodeId
     *            the node ID
     * @return the retrieved node, or {@code null}, in case there is no node with the given {@code nodeId}
     */
    NodeWrapper lookupNode(String nodeId);

    /**
     * Returns a stream to all nodes that participate in this view, starting from the root note
     * (see{@link #getRootNode()}). The order of nodes in the stream corresponds to a depth-first search approach.
     *
     * @return the nodes of this view as a stream
     */
    Stream<NodeWrapper> getViewNodesAsStream();

    /**
     * Performs a data binding between the supplied {@code model} and the controls that are part of this view.
     *
     * @param model
     *            the model to bind (can be a Java domain object with or without JavaFX properties)
     * @param implementation
     *            of {@link BindingTargetResolver} that resolves a field from the {@code model} to a control in this
     *            view.
     */
    void bind(Object model, BindingTargetResolver resolver);

    /**
     * Removes any binding between this view and a model object (usually bound via
     * {@link #bind(Object, BindingTargetResolver)}).
     *
     * @param model
     *            the model to unbind
     */
    void unbind(Object model);

    /**
     * Removes all bindings that have been established via former calls to {@link #bind(Object, BindingTargetResolver)}.
     */
    void unbindAll();
}
