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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link StringToNumberConverter}.
 *
 * @author koster
 *
 */
class StringToNumberConverterTest {
	@Test
	void testConvertNumber_nonPrimitiveTypes() {
		final String aByte = "" + Byte.MAX_VALUE;
		final String aShort = "" + Short.MAX_VALUE;
		final String anInteger = "" + Integer.MAX_VALUE;
		final String aLong = "" + Long.MAX_VALUE;
		final String aFloat = "" + Float.MAX_VALUE;
		final String aDouble = "" + Double.MAX_VALUE;

		assertThat(StringToNumberConverter.to(Byte.class).convert(aByte), equalTo(Byte.valueOf(Byte.MAX_VALUE)));
		assertThat(StringToNumberConverter.to(Short.class).convert(aShort), equalTo(Short.valueOf(Short.MAX_VALUE)));
		assertThat(StringToNumberConverter.to(Integer.class).convert(anInteger),
				equalTo(Integer.valueOf(Integer.MAX_VALUE)));
		assertThat(StringToNumberConverter.to(Long.class).convert(aLong), equalTo(Long.valueOf(Long.MAX_VALUE)));
		assertThat(StringToNumberConverter.to(Float.class).convert(aFloat), equalTo(Float.valueOf(Float.MAX_VALUE)));
		assertThat(StringToNumberConverter.to(Double.class).convert(aDouble),
				equalTo(Double.valueOf(Double.MAX_VALUE)));
	}

	@Test
	void testConvertNumber_primitiveTypes() {
		final String aByte = "" + Byte.MAX_VALUE;
		final String aShort = "" + Short.MAX_VALUE;
		final String anInteger = "" + Integer.MAX_VALUE;
		final String aLong = "" + Long.MAX_VALUE;
		final String aFloat = "" + Float.MAX_VALUE;
		final String aDouble = "" + Double.MAX_VALUE;

		assertThat(StringToNumberConverter.to(byte.class).convert(aByte),
				equalTo(Byte.valueOf(Byte.MAX_VALUE).byteValue()));
		assertThat(StringToNumberConverter.to(short.class).convert(aShort),
				equalTo(Short.valueOf(Short.MAX_VALUE).shortValue()));
		assertThat(StringToNumberConverter.to(int.class).convert(anInteger),
				equalTo(Integer.valueOf(Integer.MAX_VALUE).intValue()));
		assertThat(StringToNumberConverter.to(long.class).convert(aLong),
				equalTo(Long.valueOf(Long.MAX_VALUE).longValue()));
		assertThat(StringToNumberConverter.to(float.class).convert(aFloat),
				equalTo(Float.valueOf(Float.MAX_VALUE).floatValue()));
		assertThat(StringToNumberConverter.to(double.class).convert(aDouble),
				equalTo(Double.valueOf(Double.MAX_VALUE).doubleValue()));
	}

	@Test
	void testConvertNumberUsingNumberFormat() {
		final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		final String aByte = "" + Byte.MAX_VALUE;
		final String aShort = "" + Short.MAX_VALUE;
		final String anInteger = "" + Integer.MAX_VALUE;
		final String aLong = "" + Long.MAX_VALUE;
		final String aFloat = "" + Float.MAX_VALUE;
		final String aDouble = "" + Double.MAX_VALUE;

		assertThat(StringToNumberConverter.convertNumber(aByte, Byte.class, nf), equalTo(Byte.valueOf(Byte.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aShort, Short.class, nf),
				equalTo(Short.valueOf(Short.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(anInteger, Integer.class, nf),
				equalTo(Integer.valueOf(Integer.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class, nf), equalTo(Long.valueOf(Long.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aFloat, Float.class, nf),
				equalTo(Float.valueOf(Float.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aDouble, Double.class, nf),
				equalTo(Double.valueOf(Double.MAX_VALUE)));
	}

	@Test
	void testConvertNumberRequiringTrim() {
		final String aByte = " " + Byte.MAX_VALUE + " ";
		final String aShort = " " + Short.MAX_VALUE + " ";
		final String anInteger = " " + Integer.MAX_VALUE + " ";
		final String aLong = " " + Long.MAX_VALUE + " ";
		final String aFloat = " " + Float.MAX_VALUE + " ";
		final String aDouble = " " + Double.MAX_VALUE + " ";

		assertThat(StringToNumberConverter.convertNumber(aByte, Byte.class), equalTo(Byte.valueOf(Byte.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aShort, Short.class), equalTo(Short.valueOf(Short.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(anInteger, Integer.class),
				equalTo(Integer.valueOf(Integer.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class), equalTo(Long.valueOf(Long.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aFloat, Float.class), equalTo(Float.valueOf(Float.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aDouble, Double.class),
				equalTo(Double.valueOf(Double.MAX_VALUE)));
	}

	@Test
	void testConvertNumberRequiringTrimUsingNumberFormat() {
		final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		final String aByte = " " + Byte.MAX_VALUE + " ";
		final String aShort = " " + Short.MAX_VALUE + " ";
		final String anInteger = " " + Integer.MAX_VALUE + " ";
		final String aLong = " " + Long.MAX_VALUE + " ";
		final String aFloat = " " + Float.MAX_VALUE + " ";
		final String aDouble = " " + Double.MAX_VALUE + " ";

		assertThat(StringToNumberConverter.convertNumber(aByte, Byte.class, nf), equalTo(Byte.valueOf(Byte.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aShort, Short.class, nf),
				equalTo(Short.valueOf(Short.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(anInteger, Integer.class, nf),
				equalTo(Integer.valueOf(Integer.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class, nf), equalTo(Long.valueOf(Long.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aFloat, Float.class, nf),
				equalTo(Float.valueOf(Float.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aDouble, Double.class, nf),
				equalTo(Double.valueOf(Double.MAX_VALUE)));
	}

	@Test
	void testConvertNumberAsHex() {
		final String aByte = "0x" + Integer.toHexString(Byte.valueOf(Byte.MAX_VALUE).intValue());
		final String aShort = "0x" + Integer.toHexString(Short.valueOf(Short.MAX_VALUE).intValue());
		final String anInteger = "0x" + Integer.toHexString(Integer.MAX_VALUE);
		final String aLong = "0x" + Long.toHexString(Long.MAX_VALUE);
		final String aReallyBigInt = "FEBD4E677898DFEBFFEE44";

		assertByteEquals(aByte);
		assertShortEquals(aShort);
		assertIntegerEquals(anInteger);
		assertLongEquals(aLong);
		assertThat(StringToNumberConverter.to(BigInteger.class).convert("0x" + aReallyBigInt),
				equalTo(new BigInteger(aReallyBigInt, 16)));
	}

	@Test
	void testConvertNumberAsNegativeHex() {
		final String aByte = "-0x80";
		final String aShort = "-0x8000";
		final String anInteger = "-0x80000000";
		final String aLong = "-0x8000000000000000";
		final String aReallyBigInt = "FEBD4E677898DFEBFFEE44";

		assertNegativeByteEquals(aByte);
		assertNegativeShortEquals(aShort);
		assertNegativeIntegerEquals(anInteger);
		assertNegativeLongEquals(aLong);
		assertThat(StringToNumberConverter.to(BigInteger.class).convert("-0x" + aReallyBigInt),
				equalTo(new BigInteger(aReallyBigInt, 16).negate()));
	}

	@Test
	void testParseBigDecimalNumber() {
		final String bigDecimalAsString = "3.14159265358979323846";
		final Number bigDecimal = StringToNumberConverter.to(BigDecimal.class).convert(bigDecimalAsString);
		assertThat(bigDecimal, equalTo(new BigDecimal(bigDecimalAsString)));
	}

	@Test
	void testParseLocalizedBigDecimalNumber3() {
		final String bigDecimalAsString = "3.14159265358979323846";
		final NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
		final Number bigDecimal = StringToNumberConverter.convertNumber(bigDecimalAsString, BigDecimal.class,
				numberFormat);
		assertThat(bigDecimal, equalTo(new BigDecimal(bigDecimalAsString)));
	}

	@Test
	void testParseOverflow() {
		final String aLong = "" + Long.MAX_VALUE;
		final String aDouble = "" + Double.MAX_VALUE;

		assertThrows(IllegalArgumentException.class, () -> StringToNumberConverter.convertNumber(aLong, Byte.class));
		assertThrows(IllegalArgumentException.class, () -> StringToNumberConverter.convertNumber(aLong, Short.class));
		assertThrows(IllegalArgumentException.class, () -> StringToNumberConverter.convertNumber(aLong, Integer.class));

		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class), equalTo(Long.valueOf(Long.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aDouble, Double.class),
				equalTo(Double.valueOf(Double.MAX_VALUE)));
	}

	@Test
	void testParseNegativeOverflow() {
		final String aLong = "" + Long.MIN_VALUE;
		final String aDouble = "" + Double.MIN_VALUE;

		assertThrows(IllegalArgumentException.class, () -> StringToNumberConverter.convertNumber(aLong, Byte.class));

		assertThrows(IllegalArgumentException.class, () -> StringToNumberConverter.convertNumber(aLong, Short.class));

		assertThrows(IllegalArgumentException.class, () -> StringToNumberConverter.convertNumber(aLong, Integer.class));

		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class), equalTo(Long.valueOf(Long.MIN_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aDouble, Double.class),
				equalTo(Double.valueOf(Double.MIN_VALUE)));
	}

	@Test
	void testParseOverflowUsingNumberFormat() {
		final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		final String aLong = "" + Long.MAX_VALUE;
		final String aDouble = "" + Double.MAX_VALUE;

		assertThrows(IllegalArgumentException.class,
				() -> StringToNumberConverter.convertNumber(aLong, Byte.class, nf));

		assertThrows(IllegalArgumentException.class,
				() -> StringToNumberConverter.convertNumber(aLong, Short.class, nf));

		assertThrows(IllegalArgumentException.class,
				() -> StringToNumberConverter.convertNumber(aLong, Integer.class, nf));

		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class, nf), equalTo(Long.valueOf(Long.MAX_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aDouble, Double.class, nf),
				equalTo(Double.valueOf(Double.MAX_VALUE)));
	}

	@Test
	void testParseNegativeOverflowUsingNumberFormat() {
		final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		final String aLong = "" + Long.MIN_VALUE;
		final String aDouble = "" + Double.MIN_VALUE;

		assertThrows(IllegalArgumentException.class,
				() -> StringToNumberConverter.convertNumber(aLong, Byte.class, nf));

		assertThrows(IllegalArgumentException.class,
				() -> StringToNumberConverter.convertNumber(aLong, Short.class, nf));

		assertThrows(IllegalArgumentException.class,
				() -> StringToNumberConverter.convertNumber(aLong, Integer.class, nf));

		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class, nf), equalTo(Long.valueOf(Long.MIN_VALUE)));
		assertThat(StringToNumberConverter.convertNumber(aDouble, Double.class, nf),
				equalTo(Double.valueOf(Double.MIN_VALUE)));
	}

	private void assertLongEquals(final String aLong) {
		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class).longValue(), equalTo(Long.MAX_VALUE));
	}

	private void assertIntegerEquals(final String anInteger) {
		assertThat(StringToNumberConverter.convertNumber(anInteger, Integer.class).intValue(),
				equalTo(Integer.MAX_VALUE));
	}

	private void assertShortEquals(final String aShort) {
		assertThat(StringToNumberConverter.convertNumber(aShort, Short.class).shortValue(), equalTo(Short.MAX_VALUE));
	}

	private void assertByteEquals(final String aByte) {
		assertThat(StringToNumberConverter.convertNumber(aByte, Byte.class).byteValue(), equalTo(Byte.MAX_VALUE));
	}

	private void assertNegativeLongEquals(final String aLong) {
		assertThat(StringToNumberConverter.convertNumber(aLong, Long.class).longValue(), equalTo(Long.MIN_VALUE));
	}

	private void assertNegativeIntegerEquals(final String anInteger) {
		assertThat(StringToNumberConverter.convertNumber(anInteger, Integer.class).intValue(),
				equalTo(Integer.MIN_VALUE));
	}

	private void assertNegativeShortEquals(final String aShort) {
		assertThat(StringToNumberConverter.convertNumber(aShort, Short.class).shortValue(), equalTo(Short.MIN_VALUE));
	}

	private void assertNegativeByteEquals(final String aByte) {
		assertThat(StringToNumberConverter.convertNumber(aByte, Byte.class).byteValue(), equalTo(Byte.MIN_VALUE));
	}

}
