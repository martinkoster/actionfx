/*
 * Copyright (c) 2020 Martin Koster
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
package com.github.actionfx.testing.junit5;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.testfx.framework.junit5.ApplicationExtension;

import com.github.actionfx.testing.annotation.TestInFxThread;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Extension to TestFX's {@link ApplicationExtension} that sets the test
 * platform "Monocle" to "headless".
 * <p>
 * In case the annotation {@link TestInFxThread} is applied at class level, it
 * is ensured that all test methods are executed within the JavaFX thread,
 * <p>
 * In case the annotation {@link TestInFxThread} is only applied to a test
 * method, then only the annotated test method is executed inside the JavaFX
 * thread.
 * 
 *
 * @author MartinKoster
 */
public class HeadlessMonocleExtension extends ApplicationExtension implements BeforeAllCallback, InvocationInterceptor {

	private boolean executeAllMethodsInFxThread = false;

	@Override
	public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
		super.postProcessTestInstance(testInstance, context);

		// check if @TestInFxThread annotation is present on class level
		final Class<?> testClass = testInstance.getClass();
		if (isTestInFxThreadAnnotationPresent(testClass)) {
			executeAllMethodsInFxThread = true;
		}
	}

	@Override
	public void beforeAll(final ExtensionContext context) throws Exception {
		System.setProperty("glass.platform", "Monocle");
		System.setProperty("monocle.platform", "Headless");
		System.setProperty("prism.order", "sw");
		System.setProperty("prism.useFontConfig", "true");
		System.setProperty("prism.text", "native");
		System.setProperty("prism.lcdtext", "false");
	}

	@Override
	public void interceptTestMethod(final Invocation<Void> invocation,
			final ReflectiveInvocationContext<Method> invocationContext, final ExtensionContext extensionContext)
			throws Throwable {

		// check if method has a @TestInFxThread annotation
		final Method method = invocationContext.getExecutable();
		if (executeAllMethodsInFxThread || isTestInFxThreadAnnotationPresent(method)) {
			proceedInFxThread(invocation);
		} else {
			// proceed normally outside the JavaFX thread
			invocation.proceed();
		}
	}

	/**
	 * Proceeds the test method invocation in the JavaFX thread and waits for its
	 * termination.
	 *
	 * @param invocation the intercepted test method invocation
	 */
	private void proceedInFxThread(final Invocation<Void> invocation)
			throws InterruptedException, ExecutionException, Throwable {
		final AtomicReference<Throwable> throwable = new AtomicReference<>();

		// explicit run and wait since the test should only continue
		// if the previous JavaFX access as been finished.
		runInFxThreadAndWait(new FxTestTask(() -> {
			try {
				// executes function after @Test
				invocation.proceed();
			} catch (final Throwable t) {
				throwable.set(t);
			}
		}));
		final Throwable t = throwable.get();
		if (t != null) {
			throw t;
		}
	}

	/**
	 * Invokes a <tt>javafx.concurrent.Task</tt> in the JavaFX Thread and waits
	 * while it's finished. Like SwingUtilities.invokeAndWait does for EDT.
	 *
	 * @param task the runnable that has to be called on JavaFX thread.
	 * @throws InterruptedException f the execution is interrupted.
	 * @throws ExecutionException   If a exception is occurred in the run method of
	 *                              the Runnable
	 */
	private static <T> T runInFxThreadAndWait(final Task<T> task) throws InterruptedException, ExecutionException {
		if (Platform.isFxApplicationThread()) {
			try {
				task.run();
			} catch (final Exception e) {
				throw new ExecutionException(e);
			}
		} else {
			Platform.runLater(task::run);
		}
		return task.get();
	}

	/**
	 * Checks if the {@link TestInFxThread} annotation is present on the given
	 * element.
	 *
	 * @param element the element to check
	 * @return {@code true}, if and only if the given element carries the
	 *         {@link TestInFxThread} annotation.
	 */
	private boolean isTestInFxThreadAnnotationPresent(final AnnotatedElement element) {
		return element.isAnnotationPresent(TestInFxThread.class);
	}

	/**
	 * Wrapper for a runnable to execute.
	 *
	 * @author MartinKoster
	 *
	 */
	private static class FxTestTask extends Task<Void> {

		private final Runnable runnable;

		public FxTestTask(final Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		protected Void call() throws Exception {
			runnable.run();
			return null;
		}

	}

}
