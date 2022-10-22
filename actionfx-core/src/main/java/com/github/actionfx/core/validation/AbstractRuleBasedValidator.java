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
package com.github.actionfx.core.validation;

/**
 * Abstract base class for validation that apply rules to the value of a control. In case the rule fails, a
 * {@link ValidationMessage} is produced.
 *
 * @author MartinKoster
 */
public abstract class AbstractRuleBasedValidator implements Validator {

    //    @Override
    //    public List<ValidationMessage> validate(final Control control, final ControlProperties controlProperty) {
    //        final ControlWrapper wrapper = ControlWrapper.of(control);
    //        final Object value = wrapper.getValue(controlProperty);
    //        return null;
    //    }
    //
    //    protected abstract List<ValidationMessage> validateInternal(final ControlWrapper controlWrapper, final ControlProperties controlProperty)

}
