/*  **************************************************************
 *   Projekt         : actionfx-core (java)
 *  --------------------------------------------------------------
 *   Autor(en)       : MartinKoster
 *   Beginn-Datum    : 21.10.2022
 *  --------------------------------------------------------------
 *   copyright (c) 2015-18   DB Station&Service AG
 *   Alle Rechte vorbehalten.
 *  **************************************************************
 */
package com.github.actionfx.core.validation;

import org.junit.jupiter.api.Test;

/**
 * JUnit test for {@link ValidationResult}.
 *
 * @author MartinKoster
 */
class ValidationResultTest {

    @Test
    void testGetStatus() {

    }

    private static ValidationMessage message(final ValidationStatus status, final String text) {
        return new ValidationMessage(null, text, status)
    }

}
