/**
 * Copyright (c) 2014, 2019, ControlsFX
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

import java.util.Arrays;
import java.util.Collection;

import com.github.actionfx.core.validation.ValidationMessage;
import com.github.actionfx.core.validation.ValidationStatus;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Validation decorator to decorate validation state using images. <br>
 * Validation icons are shown in the bottom-left corner of the control as it is seems to be the most logical location
 * for such information. Required components are marked at the top-left corner with small red triangle. Here is example
 * of such decoration <br>
 * <br>
 * <img src="GraphicValidationDecorationWithTooltip.png" alt="Screenshot of GraphicValidationDecoration">
 *
 */
public class GraphicValidationDecoration extends AbstractValidationDecoration {

    private static final Image ERROR_IMAGE = new Image(GraphicValidationDecoration.class
            .getResource("/validation/decoration-error.png").toExternalForm());

    private static final Image WARNING_IMAGE = new Image(GraphicValidationDecoration.class
            .getResource("/validation/decoration-warning.png").toExternalForm());

    private static final Image INFO_IMAGE = new Image(GraphicValidationDecoration.class
            .getResource("/validation/decoration-info.png").toExternalForm());

    private static final Image REQUIRED_IMAGE = new Image(GraphicValidationDecoration.class
            .getResource("/validation/required-indicator.png").toExternalForm());

    private static final String SHADOW_EFFECT_CLASS = "shadowEffect";

    private static final String ERROR_TOOLTIP_EFFECT_CLASS = "errorTooltipEffect";

    private static final String WARNING_TOOLTIP_EFFECT_CLASS = "warningTooltipEffect";

    private static final String INFO_TOOLTIP_EFFECT_CLASS = "infoTooltipEffect";

    protected Node createDecorationNode(final ValidationMessage message) {
        final Node graphic = getGraphicByStatus(message.getStatus());
        graphic.getStyleClass().add(SHADOW_EFFECT_CLASS);
        final Label label = new Label();
        label.setGraphic(graphic);
        label.setTooltip(createTooltip(message));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    protected Node getGraphicByStatus(final com.github.actionfx.core.validation.ValidationStatus status) {
        switch (status) {
        case ERROR:
            return new ImageView(ERROR_IMAGE);
        case WARNING:
            return new ImageView(WARNING_IMAGE);
        default:
            return new ImageView(INFO_IMAGE);
        }
    }

    protected Tooltip createTooltip(final ValidationMessage message) {
        final Tooltip tooltip = new Tooltip(message.getText());
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.getStyleClass().add(getStyleBySeverity(message.getStatus()));
        return tooltip;
    }

    protected String getStyleBySeverity(final ValidationStatus severity) {
        switch (severity) {
        case ERROR:
            return ERROR_TOOLTIP_EFFECT_CLASS;
        case WARNING:
            return WARNING_TOOLTIP_EFFECT_CLASS;
        default:
            return INFO_TOOLTIP_EFFECT_CLASS;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Decoration> createValidationDecorations(final ValidationMessage message) {
        return Arrays.asList(new GraphicDecoration(createDecorationNode(message), Pos.BOTTOM_LEFT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Decoration> createRequiredDecorations(final Control target) {
        return Arrays.asList(new GraphicDecoration(new ImageView(REQUIRED_IMAGE), Pos.TOP_LEFT,
                REQUIRED_IMAGE.getWidth() / 2, REQUIRED_IMAGE.getHeight() / 2));
    }

}
