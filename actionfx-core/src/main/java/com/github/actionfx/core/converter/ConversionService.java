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
import java.lang.reflect.Array;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;

/**
 * Service offering conversion routines.
 *
 * @author koster
 *
 */
public class ConversionService {

	// contains all registered converter factories
	@SuppressWarnings("rawtypes")
	private static final Map<ConvertiblePair, ConverterFactory> CONVERTER_FACTORIES = new HashMap<>();

	// the converter cache provides fast access for converter factories. It also
	// holds entries for each class of a class hierarchy, so that when accessing not
	// the entire class hierarchy needs to be traversed again and again
	@SuppressWarnings("rawtypes")
	private static final Map<ConvertiblePair, ConverterFactory> CONVERTER_FACTORY_ACCESS_CACHE = new HashMap<>();

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
		final Converter<Object, T> converter = (Converter<Object, T>) createConverter(source.getClass(), targetType);
		if (converter != null) {
			return converter.convert(source);
		}
		throw new IllegalArgumentException("Unable to convert type '" + source.getClass().getCanonicalName()
				+ "' to type '" + targetType.getCanonicalName() + "'!");
	}

	/**
	 * Creates a JavaFX string converter for the specified
	 *
	 * @param <T>
	 * @param targetType
	 * @return
	 */
	public <T> StringConverter<T> createStringConverter(final Class<T> targetType) {
		final Converter<T, String> toStringConverter = createConverter(targetType, String.class);
		final Converter<String, T> fromStringConverter = createConverter(String.class, targetType);
		return new GenericStringConverter<>(toStringConverter, fromStringConverter);
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
		return targetType.isAssignableFrom(sourceType) || lookupConverterFactory(sourceType, targetType) != null;
	}

	/**
	 * Creates a {@link Converter} for converting the given {@code sourceType} to
	 * {@code targetType}. In case there is no converter for handling these source
	 * and target types, {@code null} will be returned.
	 *
	 * @param <S>        the source type parameter
	 * @param <T>        the target type parameter
	 * @param sourceType the source type
	 * @param targetType the target type
	 * @return the created converter, or {@link null}, in case there is no converter
	 *         available for the specified types
	 */
	private <S, T> Converter<S, T> createConverter(final Class<S> sourceType, final Class<T> targetType) {
		final ConverterFactory<S, T> converterFactory = lookupConverterFactory(sourceType, targetType);
		if (converterFactory == null) {
			return null;
		}
		return converterFactory.create(sourceType, targetType);
	}

	/**
	 * Looks up the best fit converter factory that is registered in this conversion
	 * service. Considers the type hierarchy of {@code sourceType}.
	 * <p>
	 * This method first tries to lookup the converter factory in cache via a fast
	 * lookup. In case the converter factory is not yet cached, the entire class
	 * hierarchies of the {@code sourceType} and {@code targetType} are checked.
	 *
	 * @param sourceType the source type
	 * @param targetType the target type
	 * @return the looked up converter factory, or {@code null}, if no converter
	 *         factory could be found
	 */
	@SuppressWarnings("unchecked")
	protected <S, T> ConverterFactory<S, T> lookupConverterFactory(final Class<S> sourceType,
			final Class<T> targetType) {
		return CONVERTER_FACTORY_ACCESS_CACHE.computeIfAbsent(ConvertiblePair.of(sourceType, targetType),
				this::findConverterFactory);
	}

	/**
	 * Finds a suiting converter factory by iterating over the entire class
	 * hierarchies of the source and target types of the {@link ConvertiblePair}.
	 *
	 * @param <S>             the source type
	 * @param <T>             the target type
	 * @param convertiblePair the convertible pair
	 * @return the found converter factory, or {@code null}, if no converter factory
	 *         could be found.
	 */
	@SuppressWarnings("unchecked")
	protected <S, T> ConverterFactory<S, T> findConverterFactory(final ConvertiblePair convertiblePair) {
		final List<Class<?>> sourceCandidates = getClassHierarchy(convertiblePair.getSource());
		final List<Class<?>> targetCandidates = getClassHierarchy(convertiblePair.getTarget());
		for (final Class<?> sourceCandidate : sourceCandidates) {
			for (final Class<?> targetCandidate : targetCandidates) {
				final ConvertiblePair pairToCheck = new ConvertiblePair(sourceCandidate, targetCandidate);
				final ConverterFactory<S, T> converterFactory = CONVERTER_FACTORIES.get(pairToCheck);
				if (converterFactory != null) {
					return converterFactory;
				}
			}
		}
		return null;
	}

	/**
	 * Returns an ordered class hierarchy for the given type.
	 *
	 * @param type the type
	 * @return an ordered list of all classes that the given type extends or
	 *         implements
	 */
	private List<Class<?>> getClassHierarchy(final Class<?> type) {
		final List<Class<?>> hierarchy = new ArrayList<>(20);
		final Set<Class<?>> visited = new HashSet<>(20);
		addToClassHierarchy(0, ReflectionUtils.resolvePrimitiveIfNecessary(type), false, hierarchy, visited);
		final boolean array = type.isArray();

		int i = 0;
		while (i < hierarchy.size()) {
			Class<?> candidate = hierarchy.get(i);
			candidate = array ? candidate.getComponentType() : ReflectionUtils.resolvePrimitiveIfNecessary(candidate);
			final Class<?> superclass = candidate.getSuperclass();
			if (superclass != null && superclass != Object.class && superclass != Enum.class) {
				addToClassHierarchy(i + 1, candidate.getSuperclass(), array, hierarchy, visited);
			}
			addInterfacesToClassHierarchy(candidate, array, hierarchy, visited);
			i++;
		}

		if (Enum.class.isAssignableFrom(type)) {
			addToClassHierarchy(hierarchy.size(), Enum.class, array, hierarchy, visited);
			addToClassHierarchy(hierarchy.size(), Enum.class, false, hierarchy, visited);
			addInterfacesToClassHierarchy(Enum.class, array, hierarchy, visited);
		}

		addToClassHierarchy(hierarchy.size(), Object.class, array, hierarchy, visited);
		addToClassHierarchy(hierarchy.size(), Object.class, false, hierarchy, visited);
		return hierarchy;
	}

	private void addInterfacesToClassHierarchy(final Class<?> type, final boolean asArray,
			final List<Class<?>> hierarchy, final Set<Class<?>> visited) {

		for (final Class<?> implementedInterface : type.getInterfaces()) {
			addToClassHierarchy(hierarchy.size(), implementedInterface, asArray, hierarchy, visited);
		}
	}

	private void addToClassHierarchy(final int index, Class<?> type, final boolean asArray,
			final List<Class<?>> hierarchy, final Set<Class<?>> visited) {

		if (asArray) {
			type = Array.newInstance(type, 0).getClass();
		}
		if (visited.add(type)) {
			hierarchy.add(index, type);
		}
	}

	@SuppressWarnings("unchecked")
	private static void registerConverter() {
		CONVERTER_FACTORIES.put(ConvertiblePair.of(File.class, Path.class),
				(sourceClass, targetClass) -> new FileToPathConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(File.class, String.class),
				(sourceClass, targetClass) -> new FileToStringConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(File.class, URI.class),
				(sourceClass, targetClass) -> new FileToURIConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Path.class),
				(sourceClass, targetClass) -> new StringToPathConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, File.class),
				(sourceClass, targetClass) -> new StringToFileConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, URI.class),
				(sourceClass, targetClass) -> new StringToURIConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(URI.class, Path.class),
				(sourceClass, targetClass) -> new URIToPathConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(URI.class, File.class),
				(sourceClass, targetClass) -> new URIToFileConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(URI.class, String.class),
				(sourceClass, targetClass) -> new URIToStringConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(Path.class, File.class),
				(sourceClass, targetClass) -> new PathToFileConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(Path.class, URI.class),
				(sourceClass, targetClass) -> new PathToURIConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(Path.class, String.class),
				(sourceClass, targetClass) -> new PathToStringConverter());
		CONVERTER_FACTORIES.put(ConvertiblePair.of(Number.class, Number.class),
				(sourceClass, targetClass) -> new NumberToNumberConverter<>(targetClass));
		CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Number.class),
				(sourceClass, targetClass) -> new StringToNumberConverter<>(targetClass));
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

		public Class<?> getSource() {
			return source;
		}

		public Class<?> getTarget() {
			return target;
		}
	}

	/**
	 * Factory interface for creating instances of converter.
	 *
	 * @param <S> the source type
	 * @param <T> the target type
	 *
	 * @author koster
	 *
	 */
	@FunctionalInterface
	public interface ConverterFactory<S, T> {

		/**
		 * Creates a new instance of a {@link Converter}
		 *
		 * @param <S>         the source type
		 * @param <T>         the target type
		 * @param sourceClass the source class
		 * @param targetClass the target class
		 * @return the created converter instance
		 */
		public Converter<S, T> create(Class<S> sourceClass, Class<T> targetClass);
	}

}
