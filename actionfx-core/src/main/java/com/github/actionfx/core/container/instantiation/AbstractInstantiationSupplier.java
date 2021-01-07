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
package com.github.actionfx.core.container.instantiation;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.github.actionfx.core.utils.AFXUtils;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Base class for specialized {@link Supplier} implementation that take care of
 * the instantiation of an ActionFX component (controller, view, etc.).
 *
 * @author koster
 *
 */
public abstract class AbstractInstantiationSupplier<T> implements Supplier<T> {

	@Override
	public T get() {
		return createInstanceInJavaFXThread();
	}

	/**
	 * Method to implement for
	 *
	 * @return
	 */
	protected abstract T createInstance();

	/**
	 * Creates a new, fresh instance based on the supplied bean definition. This
	 * method ensures that instantiation is performed in the JavaFX thread, as this
	 * is required for certain view components (e.g. a WebView).
	 *
	 * @param <T>            the bean type
	 * @param beanDefinition the bean definition
	 * @return the created bean instance
	 */
	protected T createInstanceInJavaFXThread() {
		// instance is create in JavaFX thread, because certain node e.g. WebView
		// requires it.
		if (Platform.isFxApplicationThread()) {
			return createInstance();
		} else {
			try {
				final Task<T> instantiationTask = new Task<>() {
					@Override
					protected T call() throws Exception {
						return createInstance();
					}
				};
				// execute the task in the JavaFX thread and wait for the result
				return AFXUtils.runInFxThreadAndWait(instantiationTask);
			} catch (InterruptedException | ExecutionException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Failed to instantiate class in JavaFX thread!", e);
			}
		}
	}
}
