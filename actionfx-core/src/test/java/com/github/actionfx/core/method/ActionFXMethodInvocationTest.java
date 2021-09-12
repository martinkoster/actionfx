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
package com.github.actionfx.core.method;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * JUnit test case for {@link ActionFXMethodInvocation}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ActionFXMethodInvocationTest {

	private final MethodHolder holder = Mockito.spy(new MethodHolder());

	@BeforeAll
	static void beforeAll() {
		ActionFX.builder().scanPackage("dummy.package").build().scanForActionFXComponents();
	}

	@Test
	void testCall_voidMethod() {
		// GIVEN
		final ActionFXMethodInvocation invocation = new ActionFXMethodInvocation(holder, "voidMethod");

		// WHEN
		invocation.call();

		// THEN
		verify(holder, times(1)).voidMethod();
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCallAsync_intMethod() {
		// GIVEN
		final ActionFXMethodInvocation invocation = new ActionFXMethodInvocation(holder, "intMethod", 42);
		final Consumer<Integer> consumer = Mockito.mock(Consumer.class);

		// WHEN
		invocation.callAsync(consumer);

		// THEN
		WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
		verify(holder, times(1)).intMethod(eq(42));
		verify(consumer, times(1)).accept(eq(Integer.valueOf(43)));
	}

	@Test
	void testCall_methodWithActionEvent() {
		// GIVEN
		final ActionEvent actionEvent = new ActionEvent();
		final ActionFXMethodInvocation invocation = new ActionFXMethodInvocation(holder, "methodWithArgs",
				"Hello World", 42, actionEvent);

		// WHEN
		invocation.call();

		// THEN
		verify(holder, times(1)).methodWithArgs(eq("Hello World"), eq(Integer.valueOf(42)), eq(actionEvent));
	}

	@Test
	void testCall_methodWithAnnotation() {
		// GIVEN
		final ActionFXMethodInvocation invocation = new ActionFXMethodInvocation(holder, "methodWithArgsAndAnnotation",
				"Hello World", 42);

		// WHEN
		invocation.call();

		// THEN
		verify(holder, times(1)).methodWithArgsAndAnnotation(eq("Hello World"), eq(Integer.valueOf(42)),
				eq("john.doe"));
	}

	@Test
	void testCall_ambiguousMethod() {
		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new ActionFXMethodInvocation(holder, "ambigiousMethod", "Hello World", 42));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"has ambiguously matching methods with name 'ambigiousMethod' that accept the supplied arguments"));
	}

	@Test
	void testCall_noMatchingMethod() {
		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new ActionFXMethodInvocation(holder, "voidMethod", "Hello World", 42));

		// THEN
		assertThat(ex.getMessage(),
				containsString("does not have method with name 'voidMethod' that accepts the supplied arguments"));
	}

	@Test
	void testForOnActionProperty() {
		// GIVEN
		final ActionEvent actionEvent = new ActionEvent();
		final EventHandler<ActionEvent> handler = ActionFXMethodInvocation.forOnActionProperty(holder, "methodWithArgs",
				"Hello World", 42);

		// WHEN
		handler.handle(actionEvent);

		// THEN
		verify(holder, times(1)).methodWithArgs(eq("Hello World"), eq(Integer.valueOf(42)), eq(actionEvent));
	}

	@Test
	@SuppressWarnings("unchecked")
	void testForOnActionPropertyWithAsyncCall() {
		// GIVEN
		final ActionEvent actionEvent = new ActionEvent();
		final Consumer<Integer> consumer = Mockito.mock(Consumer.class);
		final EventHandler<ActionEvent> handler = ActionFXMethodInvocation.forOnActionPropertyWithAsyncCall(consumer,
				holder, "intMethod", 42);

		// WHEN
		handler.handle(actionEvent);

		// THEN
		WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
		verify(holder, times(1)).intMethod(eq(42));
		verify(consumer, times(1)).accept(eq(43));
	}

	@Test
	void testForOnActionProperty_withMethodInstance() throws NoSuchMethodException, SecurityException {
		// GIVEN
		final ActionEvent actionEvent = new ActionEvent();
		final Method method = ReflectionUtils.findMethod(holder.getClass(), "methodWithArgs", String.class,
				Integer.class, ActionEvent.class);
		final EventHandler<ActionEvent> handler = ActionFXMethodInvocation.forOnActionProperty(holder, method,
				"Hello World", 42);

		// WHEN
		handler.handle(actionEvent);

		// THEN
		verify(holder, times(1)).methodWithArgs(eq("Hello World"), eq(Integer.valueOf(42)), eq(actionEvent));
	}

	@Test
	@SuppressWarnings("unchecked")
	void testForOnActionPropertyWithAsyncCall_withMethodInstance() throws NoSuchMethodException, SecurityException {
		// GIVEN
		final ActionEvent actionEvent = new ActionEvent();
		final Consumer<Integer> consumer = Mockito.mock(Consumer.class);
		final Method method = ReflectionUtils.findMethod(holder.getClass(), "intMethod", int.class);
		final EventHandler<ActionEvent> handler = ActionFXMethodInvocation.forOnActionPropertyWithAsyncCall(consumer,
				holder, method, 42);

		// WHEN
		handler.handle(actionEvent);

		// THEN
		WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
		verify(holder, times(1)).intMethod(eq(42));
		verify(consumer, times(1)).accept(eq(43));
	}

	public class MethodHolder {

		public View _view;

		public MethodHolder() {
			_view = new ParentView("viewId", new StaticView(), this);
		}

		protected void voidMethod() {
		}

		protected int intMethod(final int arg) {
			return arg + 1;
		}

		protected void methodWithArgs(final String arg1, final Integer arg2, final ActionEvent actionEvent) {
		}

		protected void methodWithArgsAndAnnotation(final String arg1, final Integer arg2,
				@AFXControlValue("username") final String username) {

		}

		protected void ambigiousMethod(@AFXControlValue("username") final String username, final String arg1,
				final Integer arg2) {
		}

		protected void ambigiousMethod(final String arg1, final Integer arg2,
				@AFXControlValue("username") final String username) {
		}

	}

	/**
	 * A static view class with some elements having an ID.
	 *
	 * @author koster
	 *
	 */
	public class StaticView extends HBox {

		public StaticView() {
			final TextField textField = new TextField();
			textField.setId("username");
			textField.setText("john.doe");
			getChildren().add(textField);
		}
	}
}
