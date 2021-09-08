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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Converts a number to a number.
 * <p>
 * The following JDK types are supported:
 * <ul>
 * <li>Byte</li>
 * <li>Short</li>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Double</li>
 * <li>Float</li>
 * <li>BigInteger</li>
 * <li>BigDecimal</li>
 * </ul>
 *
 * @author koster
 *
 * @param <S> the source type
 * @param <T> the target type
 */
public class NumberToNumberConverter<S extends Number, T extends Number> implements Converter<S, T> {

	private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

	private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

	private static final Map<Class<?>, UnaryOperator<Number>> NUMBER_TO_NUMBER_FUNCTIONS = new HashMap<>();

	static {
		initializeNumberConversionFunctions();
	}

	private final Class<T> targetNumberClass;

	public NumberToNumberConverter(final Class<T> targetNumberClass) {
		this.targetNumberClass = targetNumberClass;
	}

	/**
	 * Convenient factory method for creating an instance of
	 * {@link NumberToNumberConverter}.
	 *
	 * @param targetClass the target class
	 * @return the created converter
	 */
	public static <A extends Number, B extends Number> NumberToNumberConverter<A, B> to(final Class<B> targetClass) {
		return new NumberToNumberConverter<>(targetClass);
	}

	@Override
	public T convert(final S source) {
		return convertNumberToTargetClass(source, targetNumberClass);
	}

	/**
	 * Convert the given number into an instance of the given target class. Can be
	 * used in a static way.
	 *
	 * @param number      the number to convert
	 * @param targetClass the target class to convert to
	 * @return the converted number
	 * @throws IllegalArgumentException if the target class is not supported (i.e.
	 *                                  not a standard Number subclass as included
	 *                                  in the JDK)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T convertNumberToTargetClass(final Number number, final Class<T> targetClass)
			throws IllegalArgumentException {
		final UnaryOperator<Number> conversionFunction = NUMBER_TO_NUMBER_FUNCTIONS.get(targetClass);
		if (conversionFunction != null) {
			return (T) conversionFunction.apply(number);
		} else {
			throw new IllegalArgumentException(
					"Can not convert number [" + number + "] of type [" + number.getClass().getCanonicalName()
							+ "] to unsupported target class [" + targetClass.getCanonicalName() + "]");
		}
	}

	/**
	 * Converts the given number to a {@link BigDecimal}.
	 *
	 * @param number the number to convert
	 * @return the converted number
	 */
	private static BigDecimal toBigDecimal(final Number number) {
		return new BigDecimal(number.toString());
	}

	/**
	 * Converts the given number to a {@link Double}.
	 *
	 * @param number the number to convert
	 * @return the converted number
	 */
	private static Double toDouble(final Number number) {
		return Double.valueOf(number.doubleValue());
	}

	/**
	 * Converts the given number to a {@link Float}.
	 *
	 * @param number the number to convert
	 * @return the converted number
	 */
	private static Float toFloat(final Number number) {
		return Float.valueOf(number.floatValue());
	}

	/**
	 * Converts the given number to a {@link Integer}.
	 *
	 * @param number the number to convert
	 * @return the converted number
	 */
	private static BigInteger toBigInteger(final Number number) {
		if (number instanceof BigDecimal) {
			return ((BigDecimal) number).toBigInteger();
		} else {
			return BigInteger.valueOf(number.longValue());
		}
	}

	/**
	 * Converts the given number to a {@link Long}.
	 *
	 * @param number the number to convert
	 * @return the converted number
	 */
	private static Long toLong(final Number number) {
		final long value = toCheckedLongValue(number, Long.class);
		return Long.valueOf(value);
	}

	/**
	 * Converts the given number to a {@link Integer}.
	 *
	 * @param number the number to convert
	 * @return the converted number
	 */
	private static Integer toInteger(final Number number) {
		final long value = toCheckedLongValue(number, Integer.class);
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			throwOverflowException(number, Integer.class);
		}
		return Integer.valueOf(number.intValue());
	}

	/**
	 * Converts the given number to a {@link Short}.
	 *
	 * @param number the number to convert
	 * @return the converted number
	 */
	private static Short toShort(final Number number) {
		final long value = toCheckedLongValue(number, Short.class);
		if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
			throwOverflowException(number, Short.class);
		}
		return Short.valueOf(number.shortValue());
	}

	/**
	 * Converts the given number to a {@link Byte}.
	 *
	 * @param number the number to convert
	 * @return the converted number
	 */
	private static Byte toByte(final Number number) {
		final long value = toCheckedLongValue(number, Byte.class);
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
			throwOverflowException(number, Byte.class);
		}
		return Byte.valueOf(number.byteValue());
	}

	/**
	 * Just throws an {@link IllegalArgumentException}.
	 *
	 * @param number      the number that should be converted
	 * @param targetClass the target class that the number shall be converted to
	 */
	private static void throwOverflowException(final Number number, final Class<?> targetClass) {
		throw new IllegalArgumentException("Overflow occured while converting number '" + number + "' of type '"
				+ number.getClass().getName() + "' to target class '" + targetClass.getName());
	}

	/**
	 * Converts the given {@code number} to a long value. As part of this function,
	 * it is checked whether the range within {@link Long} is sufficient to display
	 * the given {@code number}. In case, the type {@link Long} is not sufficient,
	 * an {@link IllegalArgumentException} is thrown.
	 *
	 * @param number      the number to convert
	 * @param targetClass the target class that the long is intended to be finally
	 *                    converted to
	 * @return the converted value as long
	 */
	private static long toCheckedLongValue(final Number number, final Class<? extends Number> targetClass) {
		BigInteger bigInt = null;
		if (number instanceof BigInteger) {
			bigInt = (BigInteger) number;
		} else if (number instanceof BigDecimal) {
			bigInt = ((BigDecimal) number).toBigInteger();
		}
		if (bigInt != null && (bigInt.compareTo(LONG_MIN) < 0 || bigInt.compareTo(LONG_MAX) > 0)) {
			throwOverflowException(number, targetClass);
		}
		return number.longValue();
	}

	/**
	 * Initializes the internal number-to-number conversion functions.
	 */
	private static void initializeNumberConversionFunctions() {
		NUMBER_TO_NUMBER_FUNCTIONS.put(Byte.class, NumberToNumberConverter::toByte);
		NUMBER_TO_NUMBER_FUNCTIONS.put(byte.class, NumberToNumberConverter::toByte);
		NUMBER_TO_NUMBER_FUNCTIONS.put(Short.class, NumberToNumberConverter::toShort);
		NUMBER_TO_NUMBER_FUNCTIONS.put(short.class, NumberToNumberConverter::toShort);
		NUMBER_TO_NUMBER_FUNCTIONS.put(Integer.class, NumberToNumberConverter::toInteger);
		NUMBER_TO_NUMBER_FUNCTIONS.put(int.class, NumberToNumberConverter::toInteger);
		NUMBER_TO_NUMBER_FUNCTIONS.put(Long.class, NumberToNumberConverter::toLong);
		NUMBER_TO_NUMBER_FUNCTIONS.put(long.class, NumberToNumberConverter::toLong);
		NUMBER_TO_NUMBER_FUNCTIONS.put(BigInteger.class, NumberToNumberConverter::toBigInteger);
		NUMBER_TO_NUMBER_FUNCTIONS.put(Float.class, NumberToNumberConverter::toFloat);
		NUMBER_TO_NUMBER_FUNCTIONS.put(float.class, NumberToNumberConverter::toFloat);
		NUMBER_TO_NUMBER_FUNCTIONS.put(Double.class, NumberToNumberConverter::toDouble);
		NUMBER_TO_NUMBER_FUNCTIONS.put(double.class, NumberToNumberConverter::toDouble);
		NUMBER_TO_NUMBER_FUNCTIONS.put(BigDecimal.class, NumberToNumberConverter::toBigDecimal);
	}
}
