package com.github.actionfx.common.converter;

import javafx.util.StringConverter;

/**
 * Demo converter for converting a double value to a string with currency
 * symbol.
 *
 * @author koster
 *
 */
public class DoubleCurrencyStringConverter extends StringConverter<Double> {

	@Override
	public String toString(final Double object) {
		return String.valueOf(object) + " $";
	}

	@Override
	public Double fromString(final String string) {
		return Double.parseDouble(string);
	}
}