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
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
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

    // in case there is no more specific to-string converter is registered, this
    // converter is used as fallback
    private static final Converter<Object, String> FALLBACK_TO_STRING_CONVERTER = new ObjectToStringConverter();

    // locale used from locale-specific conversion
    private final Property<Locale> localeProperty = new SimpleObjectProperty<>();

    static {
        registerConverter();
    }

    /**
     * Standard constructor that tries to retrieve a {@link Locale} from a potentially setup {@link ActionFX} instance.
     * In case there is no {@link ActionFX} instance is setup, the default system locale is taken.
     */
    public ConversionService() {
        this(ActionFX.isConfigured() ? ActionFX.getInstance().getObservableLocale()
                : new SimpleObjectProperty<>(Locale.getDefault()));
    }

    /**
     * Constructor accepting a {@link Locale} wrapped in an {@link ObservableValue}, so that changes to this
     * {@link ObservableValue} are also affecting the convesion routines of this service.
     *
     * @param localeProperty
     *            the locale obervable value
     */
    public ConversionService(final ObservableValue<Locale> localeProperty) {
        this.localeProperty.bind(localeProperty);
    }

    /**
     * Convert the given {@code source} to the specified {@code targetType}.
     *
     * @param source
     *            the source object to convert (may be {@code null})
     * @param targetType
     *            the target type to convert to (required)
     * @return the converted object, an instance of targetType
     *
     */
    public <T> T convert(final Object source, final Class<T> targetType) {
        return convert(source, source != null ? source.getClass() : null, targetType, null);
    }

    /**
     * Convert the given {@code source} to the specified {@code targetType}.
     *
     * @param source
     *            the source object to convert (may be {@code null})
     * @param targetType
     *            the target type to convert to (required)
     * @param formatPattern
     *            an optional, nullable format pattern (e.g. for {@link java.text.NumberFormat})
     * @return the converted object, an instance of targetType
     *
     */
    public <T> T convert(final Object source, final Class<T> targetType, final String formatPattern) {
        return convert(source, source != null ? source.getClass() : null, targetType, formatPattern);
    }

    /**
     * Convert the given {@code source} to the specified {@code targetType}.
     *
     * @param <T>
     *            the target type
     *
     * @param source
     *            the source object to convert (may be {@code null})
     * @param sourceType
     *            the source type (important, when {@code source} itself is {@code null})
     * @param targetType
     *            the target type to convert to (required)
     * @param formatPattern
     *            an optional, nullable format pattern (e.g. for {@link java.text.NumberFormat} or
     *            {@link java.time.format.DateTimeFormatter})
     * @return the converted object, an instance of targetType
     *
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(final Object source, final Class<?> sourceType, final Class<T> targetType,
            final String formatPattern) {
        if (source == null && sourceType == null) {
            if (!targetType.isPrimitive()) {
                return null;
            }
            throw new IllegalArgumentException("Can not convert 'null' to target type '" + targetType.getCanonicalName()
                    + "' as it is a primitive!");
        }
        if (sourceType != null && targetType.isAssignableFrom(sourceType)) {
            // no conversion required, source is of targetType already
            return (T) source;
        }
        final Converter<Object, T> converter = (Converter<Object, T>) createConverter(sourceType, targetType,
                formatPattern);
        if (converter != null) {
            return converter.apply(source);
        }
        throw new IllegalArgumentException("Unable to convert type '" + sourceType + "' to type '" + targetType + "'!");
    }

    /**
     * Creates a JavaFX string converter for the specified {@code targetType}.
     *
     * @param <T>
     *            the target type parameter
     * @param targetType
     *            the target type class
     * @return the created string converter
     */
    public <T> StringConverter<T> createStringConverter(final Class<T> targetType) {
        return createStringConverter(targetType, null);
    }

    /**
     * Creates a JavaFX string converter for the specified {@code targetType}.
     *
     * @param <T>
     *            the target type parameter
     * @param targetType
     *            the target type class
     * @param formatPattern
     *            an optional, nullable format pattern (e.g. for {@link java.text.NumberFormat})
     * @return the created string converter
     */
    public <T> StringConverter<T> createStringConverter(final Class<T> targetType, final String formatPattern) {
        final Function<T, String> toStringConverter = value -> this.convert(value,
                value != null ? value.getClass() : null, String.class, formatPattern);
        final Function<String, T> fromStringConverter = value -> this.convert(value, String.class, targetType,
                formatPattern);
        return new GenericStringConverter<>(toStringConverter, fromStringConverter);
    }

    /**
     * Creates a {@link BidirectionalConverter} for the given {@code sourceType} and {@code targetType}.
     *
     * @param <S>
     *            the source type
     * @param <T>
     *            the target type
     * @param sourceType
     *            the source type class
     * @param targetType
     *            the target type class
     * @return the created bidirectional converter
     */
    public <S, T> BidirectionalConverter<S, T> createBidirectionalConverter(final Class<S> sourceType,
            final Class<T> targetType) {
        return createBidirectionalConverter(sourceType, targetType, null);
    }

    /**
     * Creates a {@link BidirectionalConverter} for the given {@code sourceType} and {@code targetType}.
     *
     * @param <S>
     *            the source type
     * @param <T>
     *            the target type
     * @param sourceType
     *            the source type class
     * @param targetType
     *            the target type class
     * @param formatPattern
     *            an optional, nullable format pattern (e.g. for {@link java.text.NumberFormat})
     * @return the created bidirectional converter
     */
    public <S, T> BidirectionalConverter<S, T> createBidirectionalConverter(final Class<S> sourceType,
            final Class<T> targetType, final String formatPattern) {
        final Function<T, S> toConverter = value -> this.convert(value, targetType, sourceType, formatPattern);
        final Function<S, T> fromConverter = value -> this.convert(value, sourceType, targetType, formatPattern);
        return new GenericBidirectionalConverter<>(fromConverter, toConverter);
    }

    /**
     * Checks, whether the given {@code sourceType} can be converted to {@code targetType} by this service.
     *
     * @param sourceType
     *            the source type
     * @param targetType
     *            the target type
     * @return {@code true}, if this service is able to convert the given source type to the specified target type
     */
    public boolean canConvert(final Class<?> sourceType, final Class<?> targetType) {
        return targetType.isAssignableFrom(sourceType) || lookupConverterFactory(sourceType, targetType) != null;
    }

    /**
     * Checks, whether the given {@code value} can be converted to {@code targetType} by this service.
     *
     * @param value
     *            the value to convert
     * @param targetType
     *            the target type
     * @param formatPattern
     *            optional format pattern that is used for checking the conversion (can be null)
     * @return {@code true}, if this service is able to convert the given source type to the specified target type
     */
    public boolean canConvert(final Object value, final Class<?> targetType, final String formatPattern) {
        if (value != null && !canConvert(value.getClass(), targetType)) {
            return false;
        }
        try {
            // in order to know, whether a value is really convertible, we have to do it
            // (e.g. converting a string into a number/date/etc. requires the actual parsing of the conversion routine).
            final Object converted = convert(value, targetType, formatPattern);
            return value == null || converted != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Creates a {@link Converter} for converting the given {@code sourceType} to {@code targetType}. In case there is
     * no converter for handling these source and target types, {@code null} will be returned.
     *
     * @param <S>
     *            the source type parameter
     * @param <T>
     *            the target type parameter
     * @param sourceType
     *            the source type
     * @param targetType
     *            the target type
     * @param formatPattern
     *            an optional, nullable format pattern (e.g. for {@link java.text.NumberFormat})
     * @return the created converter, or {@link null}, in case there is no converter available for the specified types
     */
    @SuppressWarnings("unchecked")
    protected <S, T> Function<S, T> createConverter(final Class<S> sourceType, final Class<T> targetType,
            final String formatPattern) {
        final ConverterFactory<S, T> converterFactory = lookupConverterFactory(sourceType, targetType);
        if (converterFactory == null) {
            // in case there is no particular factory for a toString-converter, we return
            // the fallback toString converter
            return targetType == String.class ? (Converter<S, T>) FALLBACK_TO_STRING_CONVERTER : null;
        }
        return converterFactory.create(sourceType, targetType, localeProperty.getValue(), formatPattern);
    }

    /**
     * Looks up the best fit converter factory that is registered in this conversion service. Considers the type
     * hierarchy of {@code sourceType}.
     * <p>
     * This method first tries to lookup the converter factory in cache via a fast lookup. In case the converter factory
     * is not yet cached, the entire class hierarchies of the {@code sourceType} and {@code targetType} are checked.
     *
     * @param sourceType
     *            the source type
     * @param targetType
     *            the target type
     * @return the looked up converter factory, or {@code null}, if no converter factory could be found
     */
    @SuppressWarnings("unchecked")
    protected <S, T> ConverterFactory<S, T> lookupConverterFactory(final Class<S> sourceType,
            final Class<T> targetType) {
        return CONVERTER_FACTORY_ACCESS_CACHE.computeIfAbsent(ConvertiblePair.of(sourceType, targetType),
                this::findConverterFactory);
    }

    /**
     * Finds a suiting converter factory by iterating over the entire class hierarchies of the source and target types
     * of the {@link ConvertiblePair}.
     *
     * @param <S>
     *            the source type
     * @param <T>
     *            the target type
     * @param convertiblePair
     *            the convertible pair
     * @return the found converter factory, or {@code null}, if no converter factory could be found.
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
     * @param type
     *            the type
     * @return an ordered list of all classes that the given type extends or implements
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
                (sourceClass, targetClass, locale, formatPattern) -> new FileToPathConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(File.class, String.class),
                (sourceClass, targetClass, locale, formatPattern) -> new FileToStringConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(File.class, URI.class),
                (sourceClass, targetClass, locale, formatPattern) -> new FileToURIConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Path.class),
                (sourceClass, targetClass, locale, formatPattern) -> new StringToPathConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, File.class),
                (sourceClass, targetClass, locale, formatPattern) -> new StringToFileConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, URI.class),
                (sourceClass, targetClass, locale, formatPattern) -> new StringToURIConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(URI.class, Path.class),
                (sourceClass, targetClass, locale, formatPattern) -> new URIToPathConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(URI.class, File.class),
                (sourceClass, targetClass, locale, formatPattern) -> new URIToFileConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(URI.class, String.class),
                (sourceClass, targetClass, locale, formatPattern) -> new URIToStringConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(Path.class, File.class),
                (sourceClass, targetClass, locale, formatPattern) -> new PathToFileConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(Path.class, URI.class),
                (sourceClass, targetClass, locale, formatPattern) -> new PathToURIConverter());
        CONVERTER_FACTORIES.put(ConvertiblePair.of(Path.class, String.class),
                (sourceClass, targetClass, locale, formatPattern) -> new PathToStringConverter());

        // Number conversions

        // string -> floating point
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Double.class), (sourceClass, targetClass, locale,
                formatPattern) -> new StringToDoubleConverter(formatPattern, locale, true));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, double.class), (sourceClass, targetClass, locale,
                formatPattern) -> new StringToDoubleConverter(formatPattern, locale, false));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Float.class), (sourceClass, targetClass, locale,
                formatPattern) -> new StringToFloatConverter(formatPattern, locale, true));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, float.class), (sourceClass, targetClass, locale,
                formatPattern) -> new StringToFloatConverter(formatPattern, locale, false));

        // floating point -> string
        CONVERTER_FACTORIES.put(ConvertiblePair.of(Double.class, String.class), (sourceClass, targetClass, locale,
                formatPattern) -> new DoubleToStringConverter(formatPattern, locale, true));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(double.class, String.class), (sourceClass, targetClass, locale,
                formatPattern) -> new DoubleToStringConverter(formatPattern, locale, false));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(Float.class, String.class), (sourceClass, targetClass, locale,
                formatPattern) -> new FloatToStringConverter(formatPattern, locale, true));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(float.class, String.class), (sourceClass, targetClass, locale,
                formatPattern) -> new FloatToStringConverter(formatPattern, locale, false));

        CONVERTER_FACTORIES.put(ConvertiblePair.of(Number.class, Number.class),
                (sourceClass, targetClass, locale, formatPattern) -> new NumberToNumberConverter<>(targetClass));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Number.class),
                (sourceClass, targetClass, locale, formatPattern) -> new StringToNumberConverter<>(targetClass));

        // string -> Boolean
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Boolean.class), (sourceClass, targetClass, locale,
                formatPattern) -> new StringToBooleanConverter(true));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, boolean.class), (sourceClass, targetClass, locale,
                formatPattern) -> new StringToBooleanConverter(false));

        // Date converter
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Date.class),
                (sourceClass, targetClass, locale, formatPattern) -> new StringToDateConverter(formatPattern, locale));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(Date.class, String.class),
                (sourceClass, targetClass, locale, formatPattern) -> new DateToStringConverter(formatPattern, locale));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(Date.class, TemporalAccessor.class),
                (sourceClass, targetClass, locale, formatPattern) -> new DateToJavaTimeConverter<>(targetClass));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(TemporalAccessor.class, Date.class),
                (sourceClass, targetClass, locale, formatPattern) -> new JavaTimeToDateConverter());

        // Java Time converter
        CONVERTER_FACTORIES.put(ConvertiblePair.of(TemporalAccessor.class, TemporalAccessor.class),
                (sourceClass, targetClass, locale, formatPattern) -> new JavaTimeToJavaTimeConverter<>(targetClass));

        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, TemporalAccessor.class), (sourceClass, targetClass,
                locale, formatPattern) -> new StringToJavaTimeConverter<>(targetClass, formatPattern, locale));
        CONVERTER_FACTORIES.put(ConvertiblePair.of(TemporalAccessor.class, String.class), (sourceClass, targetClass,
                locale, formatPattern) -> new JavaTimeToStringConverter(formatPattern, locale));

        // Enum converter
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Enum.class),
                (sourceClass, targetClass, locale, formatPattern) -> new StringToEnumConverter<>(targetClass));

        // FALLBACK String to Object conversion
        CONVERTER_FACTORIES.put(ConvertiblePair.of(String.class, Object.class), (sourceClass, targetClass,
                locale, formatPattern) -> new StringToObjectConverter<>(targetClass));
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
     * @param <S>
     *            the source type
     * @param <T>
     *            the target type
     *
     * @author koster
     *
     */
    @FunctionalInterface
    public interface ConverterFactory<S, T> {

        /**
         * Creates a new instance of a {@link Converter} / {@link Function}.
         *
         * @param sourceClass
         *            the source class
         * @param targetClass
         *            the target class
         * @param locale
         *            an optional, nullable locale to use for formatting
         * @param formatPattern
         *            an optional, nullable format pattern (e.g. for {@link java.text.NumberFormat})
         * @return the created converter instance
         */
        public Function<S, T> create(Class<S> sourceClass, Class<T> targetClass, Locale locale, String formatPattern);
    }

    public final Property<Locale> localePropertyProperty() {
        return localeProperty;
    }

    public final Locale getLocaleProperty() {
        return localePropertyProperty().getValue();
    }
}
