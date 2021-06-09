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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Converts a string to a JDK supported number.
 * <p>
 * The following JDK types are supported as target:
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
 * @param <T> the target type
 */
public class StringToNumberConverter<T extends Number> implements Converter<String, T> {

	private static final Map<Class<?>, Function<String, Number>> STRING_TO_NUMBER_FUNCTIONS = new HashMap<>();

	static {
		initializeStringConversionFunctions();
	}

	private final Class<T> targetNumberClass;

	public StringToNumberConverter(final Class<T> targetNumberClass) {
		this.targetNumberClass = targetNumberClass;
	}

	/**
	 * Convenient factory method for creating an instance of
	 * {@link StringToNumberConverter}.
	 *
	 * @param targetClass the target class
	 * @return the created converter
	 */
	public static <B extends Number> StringToNumberConverter<B> to(final Class<B> targetClass) {
		return new StringToNumberConverter<>(targetClass);
	}

	@Override
	public T convert(final String source) {
		return convertNumber(source, targetNumberClass);
	}

	/**
	 * Parse the given {@code text} into a {@link Number} instance of the given
	 * target class, using the corresponding {@code decode} / {@code valueOf}
	 * method.
	 * <p>
	 * Supports numbers in hex format (with leading "0x", "0X", or "#") as well.
	 *
	 * @param text        the text to convert
	 * @param targetClass the target class to parse into
	 * @return the parsed number
	 * @throws IllegalArgumentException if the target class is not supported
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T convertNumber(final String text, final Class<T> targetClass) {
		final String trimmed = trimAllWhitespace(text);
		final Function<String, Number> conversionFunction = STRING_TO_NUMBER_FUNCTIONS.get(targetClass);
		if (conversionFunction != null) {
			return (T) conversionFunction.apply(trimmed);
		} else {
			throw new IllegalArgumentException(
					"Cannot convert String '" + text + "' to target class '" + targetClass.getName() + "'");
		}
	}

	/**
	 * Parses the given {@code text} into a {@code Number}, by using the supplied
	 * {@link NumberFormat}. In case {@code numberFormat} is {@code null}, a default
	 * number parsing is applied.
	 *
	 * @param <T>          the number type
	 * @param text         the text to parse
	 * @param targetClass  the target class
	 * @param numberFormat the number format to use for parsing, can be
	 *                     {@code null}.
	 * @return the parsed number
	 */
	public static <T extends Number> T convertNumber(final String text, final Class<T> targetClass,
			final NumberFormat numberFormat) {
		if (numberFormat != null) {
			return convertNumberUsingNumberFormat(text, targetClass, numberFormat);
		} else {
			return convertNumber(text, targetClass);
		}
	}

	/**
	 * Parses the given text using the supplied {@link NumberFormat}, which must not
	 * be {@code null}.
	 *
	 * @param <T>          the number type
	 * @param text         the text to parse
	 * @param targetClass  the target class
	 * @param numberFormat the number format to use for parsing, must not be
	 *                     {@code null}.
	 * @return the parsed number
	 */
	private static <T extends Number> T convertNumberUsingNumberFormat(final String text, final Class<T> targetClass,
			final NumberFormat numberFormat) {
		DecimalFormat decimalFormat = null;
		boolean resetBigDecimal = false;
		if (numberFormat instanceof DecimalFormat) {
			decimalFormat = (DecimalFormat) numberFormat;
			if (BigDecimal.class == targetClass && !decimalFormat.isParseBigDecimal()) {
				decimalFormat.setParseBigDecimal(true);
				resetBigDecimal = true;
			}
		}
		try {
			final Number number = numberFormat.parse(trimAllWhitespace(text));
			return NumberToNumberConverter.convertNumberToTargetClass(number, targetClass);
		} catch (final ParseException ex) {
			throw new IllegalArgumentException("Can not parse number: " + ex.getMessage());
		} finally {
			if (resetBigDecimal) {
				decimalFormat.setParseBigDecimal(false);
			}
		}
	}

	/**
	 * Converts the given string value to a {@link BigDecimal}.
	 *
	 * @param trimmed the string to convert
	 * @return the converted number
	 */
	private static BigDecimal toBigDecimal(final String trimmed) {
		return new BigDecimal(trimmed);
	}

	/**
	 * Converts the given string value to a {@link Double}.
	 *
	 * @param trimmed the string to convert
	 * @return the converted number
	 */
	private static Double toDouble(final String trimmed) {
		return Double.valueOf(trimmed);
	}

	/**
	 * Converts the given string value to a {@link Float}.
	 *
	 * @param trimmed the string to convert
	 * @return the converted number
	 */
	private static Float toFloat(final String trimmed) {
		return Float.valueOf(trimmed);
	}

	/**
	 * Converts the given string value to a {@link BigInteger}.
	 *
	 * @param trimmed the string to convert
	 * @return the converted number
	 */
	private static BigInteger toBigInteger(final String trimmed) {
		return isHexNumber(trimmed) ? decodeBigInteger(trimmed) : new BigInteger(trimmed);
	}

	/**
	 * Converts the given string value to a {@link Long}.
	 *
	 * @param trimmed the string to convert
	 * @return the converted number
	 */
	private static Long toLong(final String trimmed) {
		return isHexNumber(trimmed) ? Long.decode(trimmed) : Long.valueOf(trimmed);
	}

	/**
	 * Converts the given string value to a {@link Integer}.
	 *
	 * @param trimmed the string to convert
	 * @return the converted number
	 */
	private static Integer toInteger(final String trimmed) {
		return isHexNumber(trimmed) ? Integer.decode(trimmed) : Integer.valueOf(trimmed);
	}

	/**
	 * Converts the given string value to a {@link Short}.
	 *
	 * @param trimmed the string to convert
	 * @return the converted number
	 */
	private static Short toShort(final String trimmed) {
		return isHexNumber(trimmed) ? Short.decode(trimmed) : Short.valueOf(trimmed);
	}

	/**
	 * Converts the given string value to a {@link Byte}.
	 *
	 * @param trimmed the string to convert
	 * @return the converted number
	 */
	private static Byte toByte(final String trimmed) {
		return isHexNumber(trimmed) ? Byte.decode(trimmed) : Byte.valueOf(trimmed);
	}

	/**
	 * Checks, whether the supplied string value represents a hexadecimal number.
	 *
	 * @param value the string value to check
	 * @return {@code true}, if the string value represents a hexadecimal number,
	 *         {@code false} otherwise.
	 */
	private static boolean isHexNumber(final String value) {
		final int index = value.startsWith("-") ? 1 : 0;
		return value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index);
	}

	/**
	 * Removes all white spaces from the given string.
	 *
	 * @param str the string to remove white-spaces from
	 * @return the trimmed string
	 */
	private static String trimAllWhitespace(final String str) {
		if (!hasLength(str)) {
			return str;
		}

		final int len = str.length();
		final StringBuilder sb = new StringBuilder(str.length());
		for (int i = 0; i < len; i++) {
			final char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Simple checker, if the given string has a length.
	 *
	 * @param str the string to check
	 * @return {@code true}, if the string has a length, {@code false} otherwise.
	 */
	private static boolean hasLength(final CharSequence str) {
		return str != null && str.length() > 0;
	}

	/**
	 * Decode a {@link java.math.BigInteger} from the supplied {@link String} value.
	 * <p>
	 * Supports decimal, hex, and octal notation.
	 *
	 * @see BigInteger#BigInteger(String, int)
	 */
	private static BigInteger decodeBigInteger(final String value) {
		int radix = 10;
		int index = 0;
		boolean negative = false;

		// Handle minus sign, if present.
		if (value.startsWith("-")) {
			negative = true;
			index++;
		}

		// Handle radix specifier, if present.
		if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
			index += 2;
			radix = 16;
		} else if (value.startsWith("#", index)) {
			index++;
			radix = 16;
		} else if (value.startsWith("0", index) && value.length() > 1 + index) {
			index++;
			radix = 8;
		}

		final BigInteger result = new BigInteger(value.substring(index), radix);
		return negative ? result.negate() : result;
	}

	/**
	 * Initializes the internal string-to-number and string-to-number conversion
	 * functions.
	 */
	private static void initializeStringConversionFunctions() {
		STRING_TO_NUMBER_FUNCTIONS.put(Byte.class, StringToNumberConverter::toByte);
		STRING_TO_NUMBER_FUNCTIONS.put(Short.class, StringToNumberConverter::toShort);
		STRING_TO_NUMBER_FUNCTIONS.put(Integer.class, StringToNumberConverter::toInteger);
		STRING_TO_NUMBER_FUNCTIONS.put(Long.class, StringToNumberConverter::toLong);
		STRING_TO_NUMBER_FUNCTIONS.put(BigInteger.class, StringToNumberConverter::toBigInteger);
		STRING_TO_NUMBER_FUNCTIONS.put(Float.class, StringToNumberConverter::toFloat);
		STRING_TO_NUMBER_FUNCTIONS.put(Double.class, StringToNumberConverter::toDouble);
		STRING_TO_NUMBER_FUNCTIONS.put(BigDecimal.class, StringToNumberConverter::toBigDecimal);
		STRING_TO_NUMBER_FUNCTIONS.put(Number.class, StringToNumberConverter::toBigDecimal);
	}
}
