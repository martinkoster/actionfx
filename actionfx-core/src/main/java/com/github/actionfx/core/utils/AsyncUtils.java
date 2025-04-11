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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.application.Platform;

/**
 * Utils for performing tasks in an asynchronous fashion.
 *
 * @author koster
 *
 */
public final class AsyncUtils {

    private static final ExecutorService AFX_EXECUTOR_SERVICE = getExecutorService();

    private AsyncUtils() {
        // class can not be instantiated
    }

    /**
     * Executes the given {@link Supplier} in a separate thread asynchronously. Once the supplier is executed, the given
     * {@link Consumer} is called with the result of the {@link Supplier}. The call of the consumer is guaranteed to be
     * inside the JavaFX thread.
     *
     * @param <V>
     *            the returned value of the supplier
     * @param supplier
     *            the supplier
     * @param consumer
     *            the consumer - called inside the JavaFX thread
     */
    public static <V> void executeAsynchronously(final Supplier<V> supplier, final Consumer<V> consumer) {
        CompletableFuture.supplyAsync(supplier, AFX_EXECUTOR_SERVICE).thenAcceptAsync(consumer, Platform::runLater);
    }

    /**
     * Creates a new {@link ExecutorService} that executes {@link Runnable} as a daemon thread.
     *
     * @return the created executor service
     */
    private static ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool(r -> {
            final Thread thread = Executors.defaultThreadFactory().newThread(r);
            final String name = thread.getName();
            thread.setName("actionfx-" + name);
            thread.setDaemon(true);
            return thread;
        });
    }
}
