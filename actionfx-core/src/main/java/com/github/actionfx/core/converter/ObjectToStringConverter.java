package com.github.actionfx.core.converter;

/**
 * Converter that simple calls {@link Object#toString()} to convert a source
 * Object to a {@link java.lang.String}.
 *
 * @author koster
 */
public class ObjectToStringConverter implements Converter<Object, String> {

	@Override
	public String convert(final Object source) {
		return source.toString();
	}

}