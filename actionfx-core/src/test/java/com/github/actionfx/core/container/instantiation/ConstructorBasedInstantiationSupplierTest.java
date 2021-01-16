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
package com.github.actionfx.core.container.instantiation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case {@link ConstructorBasedInstantiationSupplier}.
 *
 * @author koster
 *
 */
class ConstructorBasedInstantiationSupplierTest {

	@Test
	void testGet_withDefaultConstructor() {
		// GIVEN
		final ConstructorBasedInstantiationSupplier<ClassWithDefaultConstructor> supplier = new ConstructorBasedInstantiationSupplier<>(
				ClassWithDefaultConstructor.class);

		// WHEN and THEN
		assertThat(supplier.get(), notNullValue());
	}

	@Test
	void testGet_classHasNoDefaultConstructor() {
		// GIVEN
		final ConstructorBasedInstantiationSupplier<ClassWithoutDefaultConstructor> supplier = new ConstructorBasedInstantiationSupplier<>(
				ClassWithoutDefaultConstructor.class);

		// WHEN and THEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> supplier.get());
		assertThat(ex.getMessage(), containsString("Is there a no-arg constructor present?"));
	}

	public static class ClassWithDefaultConstructor {
		public ClassWithDefaultConstructor() {
		}
	}

	public static class ClassWithoutDefaultConstructor {

		private final String value;

		public ClassWithoutDefaultConstructor(final String value) {
			this.value = value;
		}
	}
}
