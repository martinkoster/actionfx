/*  **************************************************************
 *   Projekt         : actionfx-core (java)
 *  --------------------------------------------------------------
 *   Autor(en)       : MartinKoster
 *   Beginn-Datum    : 14.06.2022
 *  --------------------------------------------------------------
 *   copyright (c) 2015-18   DB Station&Service AG
 *   Alle Rechte vorbehalten.
 *  **************************************************************
 */
package com.github.actionfx.core.annotation;

/**
 * Defines the validation mode of controls, used in various validation-related annotations.
 * <p>
 * Possible options are:
 * <p>
 * <ul>
 * <li>ONCHANGE: Validation is applied, when a change occurs in a control that holds any validation annotation.</li>
 * <li>MANUAL: Validation occurs only, when an explicit call to
 * {@link com.github.actionfx.core.ActionFX#validate(Object)} is performed.
 * </ul>
 *
 * @author MartinKoster
 */
public enum ValidationMode {

    /**
     * Validation is applied, when a change occurs in a control that holds any validation annotation.
     */
    ONCHANGE,

    /**
     * Validation occurs only, when an explicit call to {@link com.github.actionfx.core.ActionFX#validate(Object)} is
     * performed.
     */
    MANUAL,

    /**
     * Dummy enum for specifying a global validation mode e.g. in
     * {@link com.github.actionfx.core.annotation.AFXApplication#globalValidationMode()} as "unspecified" (means
     * validation mode must be specified as part of any validation-related annotations).
     */
    GLOBAL_VALIDATION_MDOE_UNSPECIFIED
}
