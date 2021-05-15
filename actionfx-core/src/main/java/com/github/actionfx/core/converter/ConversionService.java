/*
 * Copyright (c) 2021 Martin Koster
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.actionfx.core.converter;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import javafx.beans.value.ObservableValue;

/**
 * Service offering conversion routines.
 *
 * @author koster
 *
 */
public class ConversionService {

	private static final Map<ConvertiblePair, Converter<?, ?>> CONVERTER_REGISTRY = new HashMap<>();

	static {
		registerConverter();
	}

	private final ObservableValue<Locale> observableLocale;

	/**
	 * Convert the given {@code source} to the specified {@code targetType}.
	 *
	 * @param <S>
	 *
	 * @param source     the source object to convert (may be {@code null})
	 * @param targetType the target type to convert to (required)
	 * @return the converted object, an instance of targetType
	 * @throws ConversionException      if a conversion exception occurred
	 * @throws IllegalArgumentException if targetType is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public <T> T convert(final Object source, final Class<T> targetType) {
		if (source == null) {
			if (!targetType.isPrimitive()) {
				return null;
			}
			throw new IllegalArgumentException("Can not convert 'null' to target type '" + targetType.getCanonicalName()
					+ "' as it is a primitive!");
		}
		if (targetType.isAssignableFrom(source.getClass())) {
			return (T) source;
		}
		final Converter<Object, T> converter = lookupConverter(source.getClass(), targetType);
		if (converter != null) {
			return converter.convert(source);
		}
		throw new IllegalArgumentException("Unable to convert type '" + source.getClass().getCanonicalName()
				+ "' to type '" + targetType.getCanonicalName() + "'!");
	}

	/**
	 * Checks, whether the given {@code sourceType} can be converted to
	 * {@code targetType} by this service.
	 *
	 * @param sourceType the source type
	 * @param targetType the target type
	 * @return {@code true}, if this service is able to convert the given source
	 *         type to the specified target type
	 */
	public boolean canConvert(final Class<?> sourceType, final Class<?> targetType) {
		return targetType.isAssignableFrom(sourceType) || lookupConverter(sourceType, targetType) != null;
	}

	/**
	 * Looks up the best fit converter that is registered in this conversion
	 * service. Considers the type hierarchy of {@code sourceType}.
	 *
	 * @param <T>        the target type parameter
	 * @param sourceType the source type
	 * @param targetType the target type
	 * @return the looked up converter, or {@code null}, if no converter could be
	 *         found
	 */
	@SuppressWarnings("unchecked")
	protected <T> Converter<Object, T> lookupConverter(final Class<?> sourceType, final Class<T> targetType) {
		final Stream<Class<?>> stream = generateStream(sourceType);
		return (Converter<Object, T>) stream.distinct()
				.filter(clazz -> CONVERTER_REGISTRY.containsKey(ConvertiblePair.of(clazz, targetType)))
				.map(clazz -> CONVERTER_REGISTRY.get(ConvertiblePair.of(clazz, targetType))).findFirst()
				.orElseGet(() -> null);
	}

	private static void registerConverter() {
		CONVERTER_REGISTRY.put(ConvertiblePair.of(File.class, Path.class), new FileToPathConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(File.class, String.class), new FileToStringConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(File.class, URI.class), new FileToURIConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(String.class, Path.class), new StringToPathConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(String.class, File.class), new StringToFileConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(String.class, URI.class), new StringToURIConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(URI.class, Path.class), new URIToPathConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(URI.class, File.class), new URIToFileConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(URI.class, String.class), new URIToStringConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(Path.class, File.class), new PathToFileConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(Path.class, URI.class), new PathToURIConverter());
		CONVERTER_REGISTRY.put(ConvertiblePair.of(Path.class, String.class), new PathToStringConverter());
	}

	/**
	 * Creates a stream on the class hierarchy of {@code clazz}, including all
	 * implemented interfaces.
	 *
	 * @param clazz the class to create a hierarchy stream for
	 * @return the created stream
	 */
	private Stream<Class<?>> generateStream(final Class<?> clazz) {
		if (clazz == null) {
			return Stream.empty();
		}
		return Stream.concat(Stream.concat(Stream.of(clazz), generateStream(clazz.getSuperclass())),
				Arrays.stream(clazz.getInterfaces()).flatMap(this::generateStream));
	}

	/**
	 * Constructor accepting an observable locale that this service can react on on
	 * changes.
	 *
	 * @param observableLocale the locale as an observable value
	 */
	public ConversionService(final ObservableValue<Locale> observableLocale) {
		this.observableLocale = observableLocale;
	}

	public ObservableValue<Locale> getObservableLocale() {
		return observableLocale;
	}

	/**
	 * A pair of source and target type, used as key for hashmap lookup.
	 *
	 * @author koster
	 *
	 */
	public static class ConvertiblePair {

		private final Class<?> source;

		private final Class<?> target;

		public ConvertiblePair(final Class<?> source, final Class<?> target) {
			this.source = source;
			this.target = target;
		}

		public static ConvertiblePair of(final Class<?> source, final Class<?> target) {
			return new ConvertiblePair(source, target);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (source == null ? 0 : source.hashCode());
			result = prime * result + (target == null ? 0 : target.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ConvertiblePair other = (ConvertiblePair) obj;
			if (source == null) {
				if (other.source != null) {
					return false;
				}
			} else if (!source.equals(other.source)) {
				return false;
			}
			if (target == null) {
				if (other.target != null) {
					return false;
				}
			} else if (!target.equals(other.target)) {
				return false;
			}
			return true;
		}
	}

}
