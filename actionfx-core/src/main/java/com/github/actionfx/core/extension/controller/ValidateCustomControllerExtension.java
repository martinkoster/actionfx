/*  **************************************************************
 *   Projekt         : actionfx-core (java)
 *  --------------------------------------------------------------
 *   Autor(en)       : MartinKoster
 *   Beginn-Datum    : 21.09.2022
 *  --------------------------------------------------------------
 *   copyright (c) 2015-18   DB Station&Service AG
 *   Alle Rechte vorbehalten.
 *  **************************************************************
 */
package com.github.actionfx.core.extension.controller;

import java.lang.reflect.Field;

import com.github.actionfx.core.annotation.AFXValidateCustom;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.scene.control.Control;

/**
 * @author MartinKoster
 */
public class ValidateCustomControllerExtension extends AbstractAnnotatedFieldControllerExtension<AFXValidateCustom> {

    protected ValidateCustomControllerExtension() {
        super(AFXValidateCustom.class);
    }

    @Override
    protected void extend(final Object controller, final Field annotatedElement, final AFXValidateCustom annotation) {
        final ControlWrapper controlWrapper = ControlWrapper
                .of(getFieldValue(controller, annotatedElement, Control.class));
    }

}
