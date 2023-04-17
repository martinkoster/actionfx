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

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * A {@link DecorationPane} is an overlay over an existing scenegraph that can accept decorations on controls inside the
 * overlaid scenegraph.
 * <p>
 * This class is inspired by ControlsFX' <a href=
 * "https://controlsfx.github.io/javadoc/11.1.1/org.controlsfx.controls/impl/org/controlsfx/skin/DecorationPane.html">DecorationPane</a>,
 * but refactored to ActionFX's needs, Java 11 and Clean Code standards.
 *
 * @author koster
 *
 */
public class DecorationPane extends StackPane { // NOSONAR hierarchy depth can not be influenced

    protected ChangeListener<Boolean> visibilityListener = createVisibilityListener();

    public DecorationPane() {
        // Make DecorationPane transparent
        setBackground(null);
    }

    public void setRoot(final Node root) {
        getChildren().setAll(root);
    }

    public void removeDecorationsFromNode(final Node targetNode, final List<Decoration> decorations) {
        decorations.forEach(decoration -> decoration.removeDecoration(targetNode));
    }

    public void addDecorationsToNode(final Node targetNode, final List<Decoration> decorations) {
        for (final Decoration decoration : decorations) {
            showDecoration(targetNode, decoration);
        }
    }

    private void showDecoration(final Node targetNode, final Decoration decoration) {
        decoration.applyDecoration(targetNode);
        targetNode.visibleProperty().addListener(visibilityListener);
    }

    private ChangeListener<Boolean> createVisibilityListener() {
        return (o, wasVisible, isVisible) -> {
            final BooleanProperty p = (BooleanProperty) o;
            final Node n = (Node) p.getBean();
            removeDecorationsFromNode(n, DecorationUtils.getDecorations(n));
            DecorationUtils.removeAllDecorations(n);
        };
    }
}
