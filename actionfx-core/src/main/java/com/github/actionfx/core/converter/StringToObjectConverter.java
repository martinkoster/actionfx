/*
 * Copyright (c) 2022 Martin Koster
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Fallback converter for converting a string into an object, in case no other, more specific converter is available.
 * Uses several strategies to convert the value, like e.g. calling a static {@code valueOf} method or a constructor
 * accepting the type to convert.
 *
 * @author koster
 */
public class StringToObjectConverter<T> implements Converter<String, T> {

    private static final String VALUE_OF_METHOD = "valueOf";

    private static final Class<?>[] STRING_CLASS_PARAMETER = new Class[] { String.class };

    private Class<T> targetType;

    public StringToObjectConverter(final Class<T> targetType) {
        this.targetType = targetType;
    }

    /**
     * Converts {@code source} as String parameter to an object of a given type.
     */
    @Override
    @SuppressWarnings("unchecked")
    public T convert(final String source) {
        if (source == null) {
            return null;
        }
        final String value = source.trim();
        if (hasValueOfMethod(targetType)) {
            return convertByValueOfMethod(targetType, value);
        } else if (hasConstructorAcceptingString(targetType)) {
            return convertByConstructorAcceptingString(targetType, value);
        } else if (isOfTypeCharset(targetType)) {
            return (T) convertToCharset(value);
        }
        return null;
    }

    private static boolean isOfTypeCharset(final Class<?> type) {
        return Charset.class.isAssignableFrom(type);
    }

    private static Charset convertToCharset(final String value) {
        try {
            return Charset.forName(value);
        } catch (final UnsupportedCharsetException e) {
            return null;
        }
    }

    private static Method getValueOfMethod(final Class<?> type) {
        try {
            return type.getMethod(VALUE_OF_METHOD, STRING_CLASS_PARAMETER);
        } catch (final NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    private static Constructor<?> getConstructorAcceptingString(final Class<?> type) {
        try {
            return type.getConstructor(STRING_CLASS_PARAMETER);
        } catch (final NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    private static boolean hasValueOfMethod(final Class<?> parameterClass) {
        final Method valueOfMethod = getValueOfMethod(parameterClass);
        if (valueOfMethod == null) {
            return false;
        }

        final int mod = valueOfMethod.getModifiers();
        return Modifier.isStatic(mod);
    }

    private static boolean hasConstructorAcceptingString(final Class<?> parameterClass) {
        final Constructor<?> constructor = getConstructorAcceptingString(parameterClass);
        if (constructor == null) {
            return false;
        }

        final int mod = constructor.getModifiers();
        return Modifier.isPublic(mod);
    }

    @SuppressWarnings("unchecked")
    private T convertByValueOfMethod(final Class<T> type, final String value) {
        try {
            final Method valueOfMethod = type.getMethod(VALUE_OF_METHOD, STRING_CLASS_PARAMETER);
            return (T) valueOfMethod.invoke(null, value);
        } catch (final Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private T convertByConstructorAcceptingString(final Class<T> type, final String value) {
        try {
            final Constructor<?> constructor = type.getConstructor(STRING_CLASS_PARAMETER);
            return (T) constructor.newInstance(value);
        } catch (final Exception e) {
            return null;
        }
    }
}
