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

import static org.assertj.core.api.Assertions.assertThat;
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
		assertThat(supplier.get()).isNotNull();
	}

	@Test
	void testGet_classHasNoDefaultConstructor() {
		// WHEN and THEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> new ConstructorBasedInstantiationSupplier<>(ClassWithoutDefaultConstructor.class));
		assertThat(ex.getMessage()).contains("Unable to locate a matching constructor in class");
	}

	@Test
	void testGet_constructorMatchesMaximumNumberOfArguments_allTypesDirectlyMatch() {
		// GIVEN
		final ConstructorBasedInstantiationSupplier<ClassWithMultipleConstructor> supplier = new ConstructorBasedInstantiationSupplier<>(
				ClassWithMultipleConstructor.class, Integer.valueOf(42), "Hello World", Long.valueOf(1000));

		// WHEN
		final ClassWithMultipleConstructor instance = supplier.get();

		// THEN
		assertThat(instance).isNotNull();
		assertThat(instance.getConstructorInvoked()).isEqualTo("ClassWithMultipleConstructor(42, Hello World, 1000)");
	}

	@Test
	void testGet_constructorMatchesMaximumNumberOfArguments_superTypeMatchesArgument() {
		// GIVEN
		final ConstructorBasedInstantiationSupplier<ClassWithMultipleConstructor> supplier = new ConstructorBasedInstantiationSupplier<>(
				ClassWithMultipleConstructor.class, Integer.valueOf(42), "Hello World", Integer.valueOf(500));

		// WHEN
		final ClassWithMultipleConstructor instance = supplier.get();

		// THEN
		assertThat(instance).isNotNull();
		assertThat(instance.getConstructorInvoked()).isEqualTo("ClassWithMultipleConstructorWithNumVal(42, Hello World, 500)");
	}

	@Test
	void testGet_constructorWithLessArgumentsSelected() {
		// GIVEN
		final ConstructorBasedInstantiationSupplier<ClassWithMultipleConstructor> supplier = new ConstructorBasedInstantiationSupplier<>(
				ClassWithMultipleConstructor.class, Integer.valueOf(42), "Hello World", new Object[] {});

		// WHEN
		final ClassWithMultipleConstructor instance = supplier.get();

		// THEN
		assertThat(instance).isNotNull();
		assertThat(instance.getConstructorInvoked()).isEqualTo("ClassWithMultipleConstructor(42, Hello World)");
	}

	@Test
	void testGet_constructorHasMoreArgumentsThanProvided() {
		// WHEN and THEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> new ConstructorBasedInstantiationSupplier<>(ClassWithoutDefaultConstructor.class,
						Integer.valueOf(42)));
		assertThat(ex.getMessage()).contains("Unable to locate a matching constructor in class");
	}

	public static class ClassWithDefaultConstructor {
		public ClassWithDefaultConstructor() {
		}
	}

	public static class ClassWithoutDefaultConstructor {

		private final String value;

		public String getValue() {
			return value;
		}

		public ClassWithoutDefaultConstructor(final String value) {
			this.value = value;
		}
	}

	public static class ClassWithMultipleConstructor {

		private String constructorInvoked = "";

		public ClassWithMultipleConstructor(final Integer intVal, final String strVal, final Long longVal) {
			constructorInvoked = "ClassWithMultipleConstructor(" + intVal + ", " + strVal + ", " + longVal + ")";
		}

		public ClassWithMultipleConstructor(final Integer intVal, final String strVal, final Number numVal) {
			constructorInvoked = "ClassWithMultipleConstructorWithNumVal(" + intVal + ", " + strVal + ", " + numVal
					+ ")";
		}

		public ClassWithMultipleConstructor(final Integer intVal, final String strVal) {
			constructorInvoked = "ClassWithMultipleConstructor(" + intVal + ", " + strVal + ")";
		}

		public String getConstructorInvoked() {
			return constructorInvoked;
		}
	}
}
