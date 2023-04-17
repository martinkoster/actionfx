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

import javafx.scene.Node;

/**
 * StyleClassDecoration is a {@link Decoration} designed to add a CSS style class to a node (for example, to show a
 * warning style when the field is incorrectly set). StyleClassDecoration is applied as part of the ControlsFX
 * {@link DecorationUtils} API - refer to the {@link DecorationUtils} javadoc for more details.
 *
 * @see Decoration
 * @see DecorationUtils
 */
public class StyleClassDecoration extends Decoration {

    private final String[] styleClasses;

    /**
     * Constructs a new StyleClassDecoration with the given var-args array of style classes set to be applied to any
     * node that has this decoration applied to it.
     *
     * @param styleClass
     *            A var-args array of style classes to apply to any node.
     * @throws IllegalArgumentException
     *             if the styleClass varargs array is null or empty.
     */
    public StyleClassDecoration(final String... styleClass) {
        if (styleClass == null || styleClass.length == 0) {
            throw new IllegalArgumentException("var-arg style class array must not be null or empty");
        }
        styleClasses = styleClass;
    }

    /** {@inheritDoc} */
    @Override
    public void applyDecoration(final Node targetNode) {
        final List<String> styleClassList = targetNode.getStyleClass();

        for (final String styleClass : styleClasses) {
            if (styleClassList.contains(styleClass)) {
                continue;
            }

            styleClassList.add(styleClass);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeDecoration(final Node targetNode) {
        return targetNode.getStyleClass().removeAll(styleClasses);
    }
}
