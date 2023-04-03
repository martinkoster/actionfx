/*  **************************************************************
 *   Projekt         : actionfx-core (java)
 *  --------------------------------------------------------------
 *   Autor(en)       : MartinKoster
 *   Beginn-Datum    : 03.04.2023
 *  --------------------------------------------------------------
 *   copyright (c) 2015-18   DB Station&Service AG
 *   Alle Rechte vorbehalten.
 *  **************************************************************
 */
package com.github.actionfx.core.annotation;

/**
 * Enumeration representing a boolean value that can be used in an annotation, so that beside the values for
 * {@code true} and {@code false} an "undefined" state is possible.
 *
 * @author MartinKoster
 */
public enum BooleanValue {

    TRUE(Boolean.TRUE),
    FALSE(Boolean.FALSE),
    UNDEFINED(null);

    private final Boolean value;

    private BooleanValue(final Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

}
