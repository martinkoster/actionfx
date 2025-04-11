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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link NumberToNumberConverter}.
 *
 * @author koster
 *
 */
class NumberToNumberConverterTest {

	@Test
	void testConvertDoubleToBigInteger() {
		final Double decimal = Double.valueOf(3.14d);
		assertThat(NumberToNumberConverter.to(BigInteger.class).convert(decimal)).isEqualTo(new BigInteger("3"));
	}

	@Test
	void testConvertBigDecimalToBigInteger() {
		final String number = "3654959856783750387355346";
		final BigDecimal decimal = new BigDecimal(number);
		assertThat(NumberToNumberConverter.to(BigInteger.class).convert(decimal)).isEqualTo(new BigInteger(number));
	}

	@Test
	void testConvertNonExactBigDecimalToBigInteger() {
		final BigDecimal decimal = new BigDecimal("3654959856783750387355346.14");
		assertThat(NumberToNumberConverter.to(BigInteger.class).convert(decimal)).isEqualTo(new BigInteger("3654959856783750387355346"));
	}

	@Test
	void testConvertToInteger() {
		final NumberToNumberConverter<Number, Integer> converter = NumberToNumberConverter.to(Integer.class);
		assertThat(converter.convert(BigInteger.valueOf(-1))).isEqualTo(-1);
		assertThat(converter.convert(BigInteger.valueOf(0))).isZero();
		assertThat(converter.convert(BigInteger.valueOf(1))).isEqualTo(1);
		assertThat(converter.convert(BigInteger.valueOf(Integer.MAX_VALUE))).isEqualTo(Integer.MAX_VALUE);
		assertThat(converter.convert(BigInteger.valueOf(Integer.MAX_VALUE + 1))).isEqualTo(Integer.MIN_VALUE);
		assertThat(converter.convert(BigInteger.valueOf(Integer.MIN_VALUE))).isEqualTo(Integer.MIN_VALUE);
		assertThat(converter.convert(BigInteger.valueOf(Integer.MIN_VALUE - 1))).isEqualTo(Integer.MAX_VALUE);

		assertThat(converter.convert(Long.valueOf(-1))).isEqualTo(-1);
		assertThat(converter.convert(Long.valueOf(0))).isZero();
		assertThat(converter.convert(Long.valueOf(1))).isEqualTo(1);
		assertThat(converter.convert(Long.valueOf(Integer.MAX_VALUE))).isEqualTo(Integer.MAX_VALUE);
		assertThat(converter.convert(Long.valueOf(Integer.MAX_VALUE + 1))).isEqualTo(Integer.valueOf(Integer.MIN_VALUE));
		assertThat(converter.convert(Long.valueOf(Integer.MIN_VALUE))).isEqualTo(Integer.MIN_VALUE);
		assertThat(converter.convert(Long.valueOf(Integer.MIN_VALUE - 1))).isEqualTo(Integer.MAX_VALUE);

		assertThat(converter.convert(Integer.valueOf(-1))).isEqualTo(-1);
		assertThat(converter.convert(Integer.valueOf(0))).isZero();
		assertThat(converter.convert(Integer.valueOf(1))).isEqualTo(1);
		assertThat(converter.convert(Integer.valueOf(Integer.MAX_VALUE))).isEqualTo(Integer.MAX_VALUE);
		assertThat(converter.convert(Integer.valueOf(Integer.MAX_VALUE + 1))).isEqualTo(Integer.valueOf(Integer.MIN_VALUE));
		assertThat(converter.convert(Integer.valueOf(Integer.MIN_VALUE))).isEqualTo(Integer.MIN_VALUE);
		assertThat(converter.convert(Integer.valueOf(Integer.MIN_VALUE - 1))).isEqualTo(Integer.MAX_VALUE);

		assertThat(converter.convert(Short.valueOf((short) -1))).isEqualTo(Integer.valueOf(-1));
		assertThat(converter.convert(Short.valueOf((short) 0))).isEqualTo(Integer.valueOf(0));
		assertThat(converter.convert(Short.valueOf((short) 1))).isEqualTo(Integer.valueOf(1));
		assertThat(converter.convert(Short.valueOf(Short.MAX_VALUE))).isEqualTo(Integer.valueOf(Short.MAX_VALUE));
		assertThat(converter.convert(Short.valueOf((short) (Short.MAX_VALUE + 1)))).isEqualTo(Integer.valueOf(Short.MIN_VALUE));
		assertThat(converter.convert(Short.valueOf(Short.MIN_VALUE))).isEqualTo(Integer.valueOf(Short.MIN_VALUE));
		assertThat(converter.convert(Short.valueOf((short) (Short.MIN_VALUE - 1)))).isEqualTo(Integer.valueOf(Short.MAX_VALUE));

		assertThat(converter.convert(Byte.valueOf((byte) -1))).isEqualTo(Integer.valueOf(-1));
		assertThat(converter.convert(Byte.valueOf((byte) 0))).isEqualTo(Integer.valueOf(0));
		assertThat(converter.convert(Byte.valueOf((byte) 1))).isEqualTo(Integer.valueOf(1));
		assertThat(converter.convert(Byte.valueOf(Byte.MAX_VALUE))).isEqualTo(Integer.valueOf(Byte.MAX_VALUE));
		assertThat(converter.convert(Byte.valueOf((byte) (Byte.MAX_VALUE + 1)))).isEqualTo(Integer.valueOf(Byte.MIN_VALUE));
		assertThat(converter.convert(Byte.valueOf(Byte.MIN_VALUE))).isEqualTo(Integer.valueOf(Byte.MIN_VALUE));
		assertThat(converter.convert(Byte.valueOf((byte) (Byte.MIN_VALUE - 1)))).isEqualTo(Integer.valueOf(Byte.MAX_VALUE));

		assertOverflow(Long.valueOf(Long.MAX_VALUE + 1), Integer.class);
		assertOverflow(Long.valueOf(Long.MIN_VALUE - 1), Integer.class);
		assertOverflow(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE), Integer.class);
		assertOverflow(BigInteger.valueOf(Integer.MIN_VALUE).subtract(BigInteger.ONE), Integer.class);
		assertOverflow(new BigDecimal("18446744073709551611"), Integer.class);
	}

	@Test
	void testConvertToLong() {
		final NumberToNumberConverter<Number, Long> converter = NumberToNumberConverter.to(Long.class);
		assertThat(converter.convert(BigInteger.valueOf(-1))).isEqualTo(Long.valueOf(-1));
		assertThat(converter.convert(BigInteger.valueOf(0))).isEqualTo(Long.valueOf(0));
		assertThat(converter.convert(BigInteger.valueOf(1))).isEqualTo(Long.valueOf(1));
		assertThat(converter.convert(BigInteger.valueOf(Long.MAX_VALUE))).isEqualTo(Long.MAX_VALUE);
		assertThat(converter.convert(BigInteger.valueOf(Long.MAX_VALUE + 1))).isEqualTo(Long.MIN_VALUE);
		assertThat(converter.convert(BigInteger.valueOf(Long.MIN_VALUE))).isEqualTo(Long.MIN_VALUE);
		assertThat(converter.convert(BigInteger.valueOf(Long.MIN_VALUE - 1))).isEqualTo(Long.MAX_VALUE);

		assertThat(converter.convert(Long.valueOf(-1))).isEqualTo(Long.valueOf(-1));
		assertThat(converter.convert(Long.valueOf(0))).isEqualTo(Long.valueOf(0));
		assertThat(converter.convert(Long.valueOf(1))).isEqualTo(Long.valueOf(1));
		assertThat(converter.convert(Long.valueOf(Long.MAX_VALUE))).isEqualTo(Long.MAX_VALUE);
		assertThat(converter.convert(Long.valueOf(Long.MAX_VALUE + 1))).isEqualTo(Long.MIN_VALUE);
		assertThat(converter.convert(Long.valueOf(Long.MIN_VALUE))).isEqualTo(Long.MIN_VALUE);
		assertThat(converter.convert(Long.valueOf(Long.MIN_VALUE - 1))).isEqualTo(Long.MAX_VALUE);

		assertThat(converter.convert(Integer.valueOf(-1))).isEqualTo(Long.valueOf(-1));
		assertThat(converter.convert(Integer.valueOf(0))).isEqualTo(Long.valueOf(0));
		assertThat(converter.convert(Integer.valueOf(1))).isEqualTo(Long.valueOf(1));
		assertThat(converter.convert(Integer.valueOf(Integer.MAX_VALUE))).isEqualTo(Long.valueOf(Integer.MAX_VALUE));
		assertThat(converter.convert(Integer.valueOf(Integer.MAX_VALUE + 1))).isEqualTo(Long.valueOf(Integer.MIN_VALUE));
		assertThat(converter.convert(Integer.valueOf(Integer.MIN_VALUE))).isEqualTo(Long.valueOf(Integer.MIN_VALUE));
		assertThat(converter.convert(Integer.valueOf(Integer.MIN_VALUE - 1))).isEqualTo(Long.valueOf(Integer.MAX_VALUE));

		assertThat(converter.convert(Short.valueOf((short) -1))).isEqualTo(Long.valueOf(Integer.valueOf(-1)));
		assertThat(converter.convert(Short.valueOf((short) 0))).isEqualTo(Long.valueOf(Integer.valueOf(0)));
		assertThat(converter.convert(Short.valueOf((short) 1))).isEqualTo(Long.valueOf(Integer.valueOf(1)));
		assertThat(converter.convert(Short.valueOf(Short.MAX_VALUE))).isEqualTo(Long.valueOf(Short.MAX_VALUE));
		assertThat(converter.convert(Short.valueOf((short) (Short.MAX_VALUE + 1)))).isEqualTo(Long.valueOf(Short.MIN_VALUE));
		assertThat(converter.convert(Short.valueOf(Short.MIN_VALUE))).isEqualTo(Long.valueOf(Short.MIN_VALUE));
		assertThat(converter.convert(Short.valueOf((short) (Short.MIN_VALUE - 1)))).isEqualTo(Long.valueOf(Short.MAX_VALUE));

		assertThat(converter.convert(Byte.valueOf((byte) -1))).isEqualTo(Long.valueOf(Integer.valueOf(-1)));
		assertThat(converter.convert(Byte.valueOf((byte) 0))).isEqualTo(Long.valueOf(Integer.valueOf(0)));
		assertThat(converter.convert(Byte.valueOf((byte) 1))).isEqualTo(Long.valueOf(Integer.valueOf(1)));
		assertThat(converter.convert(Byte.valueOf(Byte.MAX_VALUE))).isEqualTo(Long.valueOf(Byte.MAX_VALUE));
		assertThat(converter.convert(Byte.valueOf((byte) (Byte.MAX_VALUE + 1)))).isEqualTo(Long.valueOf(Byte.MIN_VALUE));
		assertThat(converter.convert(Byte.valueOf(Byte.MIN_VALUE))).isEqualTo(Long.valueOf(Byte.MIN_VALUE));
		assertThat(converter.convert(Byte.valueOf((byte) (Byte.MIN_VALUE - 1)))).isEqualTo(Long.valueOf(Byte.MAX_VALUE));

		assertOverflow(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), Long.class);
		assertOverflow(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE), Long.class);
		assertOverflow(new BigDecimal("18446744073709551611"), Long.class);
	}

	@Test
	void testConvert_primitiveTypes() {
		// WHEN and THEN
		assertThat(NumberToNumberConverter.to(long.class).convert(42)).isEqualTo((long) 42);
		assertThat(NumberToNumberConverter.to(byte.class).convert(42)).isEqualTo((byte) 42);
		assertThat(NumberToNumberConverter.to(short.class).convert(42)).isEqualTo((short) 42);
		assertThat(NumberToNumberConverter.to(double.class).convert(42)).isEqualTo((double) 42);
		assertThat(NumberToNumberConverter.to(float.class).convert(42)).isEqualTo((float) 42);
		assertThat(NumberToNumberConverter.to(int.class).convert(42)).isEqualTo(42);
	}

	private void assertOverflow(final Number number, final Class<? extends Number> targetClass) {
		final NumberToNumberConverter<Number, ? extends Number> converter = NumberToNumberConverter.to(targetClass);
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> converter.convert(number));
		assertThat(ex.getMessage()).contains("Overflow occured while converting number");
	}

}
