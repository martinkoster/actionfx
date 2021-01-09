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
package com.github.actionfx.core.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.core.annotation.AFXArgHint;
import com.github.actionfx.core.annotation.ArgumentHint;
import com.github.actionfx.core.utils.MethodInvocationAdapter.ParameterValue;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * JUnit test case for {@link MethodInvocationAdapter}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class MethodInvocationAdapterTest {

	@Test
	void testInvoke_withVoidMethod() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("voidMethod");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		assertThat(instance.getInvokedArguments(), hasSize(0));
		assertThat(result, nullValue());
	}

	@Test
	void testInvoke_withmethodReturningInt() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("methodReturningInt");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		assertThat(instance.getInvokedArguments(), hasSize(0));
		assertThat(result, equalTo(Integer.valueOf(42)));
	}

	@Test
	void testInvoke_withVoidMethodWithStringAndIntParameter() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithStringAndIntParameter");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		assertThat(instance.getInvokedArguments(), contains("Type-based Value 1", Integer.valueOf(4711)));
		assertThat(result, nullValue());
	}

	@Test
	void testInvoke_withVoidMethodWithOldAndNewValue() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithOldAndNewValue");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		// first "old", then "new"
		assertThat(instance.getInvokedArguments(), contains(Integer.valueOf(100), Integer.valueOf(1000)));
		assertThat(result, nullValue());

	}

	@Test
	void testInvoke_withVoidMethodWithNewAndOldValue() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithNewAndOldValue");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		// first "new", then "old"
		assertThat(instance.getInvokedArguments(), contains(Integer.valueOf(1000), Integer.valueOf(100)));
		assertThat(result, nullValue());
	}

	@Test
	void testInvoke_withVoidMethodWithWithTwoStrings_checkParameterConsumption() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithWithTwoStrings");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		// check that the string arguments have taken in order (and not repeated)
		assertThat(instance.getInvokedArguments(), contains("Type-based Value 1", "Type-based Value 2"));
		assertThat(result, nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testInvoke_withVoidMethodWithSelected() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithSelected");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		// first "new", then "old"
		assertThat(instance.getInvokedArguments(), hasSize(1)); // the list of invoked arguments now contains a list
		assertThat((List<String>) instance.getInvokedArguments().get(0), contains("selected"));
		assertThat(result, nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testInvoke_withVoidMethodWithSelectedAddedRemovedLists() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithSelectedAddedRemovedLists");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		// first "new", then "old"
		assertThat(instance.getInvokedArguments(), hasSize(3)); // the list of invoked arguments now contains 3 lists
		assertThat((List<String>) instance.getInvokedArguments().get(0), contains("selected"));
		assertThat((List<String>) instance.getInvokedArguments().get(1), contains("added"));
		assertThat((List<String>) instance.getInvokedArguments().get(2), contains("removed"));
		assertThat(result, nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testInvoke_withVoidMethodWithRemovedAddedSelectedLists() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithRemovedAddedSelectedLists");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
		assertThat(instance.isExecuted(), equalTo(true));
		// first "new", then "old"
		assertThat(instance.getInvokedArguments(), hasSize(3)); // the list of invoked arguments now contains 3 lists
		assertThat((List<String>) instance.getInvokedArguments().get(0), contains("removed"));
		assertThat((List<String>) instance.getInvokedArguments().get(1), contains("added"));
		assertThat((List<String>) instance.getInvokedArguments().get(2), contains("selected"));
		assertThat(result, nullValue());
	}

	@Test
	void testInvokeAsynchronously_withMethodReturningInt() {
		// GIVEN
		final MethodInvocationAdapter adapter = methodInvocationAdapter("methodReturningInt");
		final IntegerProperty result = new SimpleIntegerProperty(0);

		// WHEN
		adapter.invokeAsynchronously(value -> result.set((int) value));

		// THEN
		WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
		assertThat(result.get(), equalTo(42));
	}

	private static MethodInvocationAdapter methodInvocationAdapter(final String methodName) {
		final ClassWithPublicMethods instance = new ClassWithPublicMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithPublicMethods.class, methodName, (Class<?>[]) null);
		return new MethodInvocationAdapter(instance, method, ParameterValue.of("Type-based Value 1"),
				ParameterValue.of("Type-based Value 2"), ParameterValue.of(Integer.valueOf(4711)),
				ParameterValue.ofOldValue(Integer.valueOf(100)), ParameterValue.ofNewValue(Integer.valueOf(1000)),
				ParameterValue.ofAllSelectedValues(List.of("selected")), ParameterValue.ofAddedValues(List.of("added")),
				ParameterValue.ofRemovedValues(List.of("removed")));
	}

	/**
	 * Test class with public methods.
	 *
	 * @author koster
	 *
	 */
	public static class ClassWithPublicMethods {

		private final List<Object> invokedArguments = new ArrayList<>();

		private boolean executed = false;

		public void voidMethod() {
			setExecuted();
		}

		public int methodReturningInt() {
			setExecuted();
			return 42;
		}

		public void voidMethodWithStringAndIntParameter(final String string, final Integer integer) {
			invokedArguments.add(string);
			invokedArguments.add(integer);
			setExecuted();
		}

		public void voidMethodWithOldAndNewValue(@AFXArgHint(ArgumentHint.OLD_VALUE) final Integer oldValue,
				@AFXArgHint(ArgumentHint.NEW_VALUE) final Integer newValue) {
			invokedArguments.add(oldValue);
			invokedArguments.add(newValue);
			setExecuted();
		}

		public void voidMethodWithNewAndOldValue(@AFXArgHint(ArgumentHint.NEW_VALUE) final Integer newValue,
				@AFXArgHint(ArgumentHint.OLD_VALUE) final Integer oldValue) {
			invokedArguments.add(newValue);
			invokedArguments.add(oldValue);
			setExecuted();
		}

		public void voidMethodWithWithTwoStrings(final String value1, final String value2) {
			invokedArguments.add(value1);
			invokedArguments.add(value2);
			setExecuted();
		}

		public void voidMethodWithSelected(final List<String> selected) {
			invokedArguments.add(selected);
			setExecuted();
		}

		public void voidMethodWithSelectedAddedRemovedLists(final List<String> selected, final List<String> added,
				final List<String> removed) {
			invokedArguments.add(selected);
			invokedArguments.add(added);
			invokedArguments.add(removed);
			setExecuted();
		}

		public void voidMethodWithRemovedAddedSelectedLists(
				@AFXArgHint(ArgumentHint.REMOVED_VALUES) final List<String> removed,
				@AFXArgHint(ArgumentHint.ADDED_VALUES) final List<String> added,
				@AFXArgHint(ArgumentHint.ALL_SELECTED) final List<String> selected) {
			invokedArguments.add(removed);
			invokedArguments.add(added);
			invokedArguments.add(selected);
			setExecuted();
		}

		private void setExecuted() {
			executed = true;
		}

		public boolean isExecuted() {
			return executed;
		}

		public List<Object> getInvokedArguments() {
			return invokedArguments;
		}
	}

}
