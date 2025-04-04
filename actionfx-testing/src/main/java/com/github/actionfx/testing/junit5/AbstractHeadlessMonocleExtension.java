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

import com.github.actionfx.testing.annotation.TestInFxThread;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationAdapter;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationFixture;
import org.testfx.framework.junit5.Init;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Extension to TestFX's {@link ApplicationExtension} that sets the test platform "Monocle" to "headless".
 * <p>
 * This base class provides routines to control the creation and shutdown of the JavaFX application and thread
 * explicetly.
 * <p>
 * This extension is abstract and allows derived classes to explicitly control the lifecycle of the JavaFX application
 * and thread. Originally, TestFX's {@link ApplicationExtension} starts the JavaFX application and thread before the
 * test method execution takes place. This behavior however causes issues with test fixtures that require a JavaFX
 * thread and application also outside the test method. When using Spring e.g., Spring might want to instantiate a bean
 * within the JavaFX thread, which would not be possible by plainly using the {@code ApplicationExtesion}.
 * <p>
 * Additionally, the initialization of the Java toolkit must happen way before the instantiation of the class under
 * test, because it can be that there are nodes instantiated outside of the test method itself. This will fail, if the
 * toolkit is not yet initialized.
 *
 *
 * @author MartinKoster
 */
public abstract class AbstractHeadlessMonocleExtension extends FxRobot implements BeforeAllCallback, AfterEachCallback,
        TestInstancePostProcessor, ParameterResolver, TestInstanceFactory, InvocationInterceptor {

    private boolean executeAllMethodsInFxThread;

    private ApplicationFixture applicationFixture;

    @Override
    public Object createTestInstance(final TestInstanceFactoryContext factoryContext,
            final ExtensionContext extensionContext) {
        // initialize toolkit here! we need the JavaFX toolkit in case the construction
        // of the instance instantiates Nodes!
        try {
            FxToolkit.registerPrimaryStage();
        } catch (final TimeoutException e) {
            throw new TestInstantiationException("Can not initialize JavaFX toolkit!", e);
        }
        try {
            final Optional<Object> outerInstance = factoryContext.getOuterInstance();
            final Class<?> testClass = factoryContext.getTestClass();
            if (outerInstance.isPresent()) {
                return newInstance(testClass, outerInstance.get());
            } else {
                return newInstance(testClass);
            }
        } catch (final Exception e) {
            throw new TestInstantiationException(e.getMessage(), e);
        }
    }

    public static <T> T newInstance(Class<T> clazz, Object... args) {
        try {
            Class<?>[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
            return newInstance(clazz.getDeclaredConstructor(parameterTypes), args);
        } catch (Throwable t) {
            throw new IllegalStateException(getUnderlyingCause(t));
        }
    }

    @SuppressWarnings("deprecation") // "AccessibleObject.isAccessible()" is deprecated in Java 9
    public static <T extends AccessibleObject> T makeAccessible(T object) {
        if (!object.isAccessible()) {
            object.setAccessible(true);
        }
        return object;
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            return makeAccessible(constructor).newInstance(args);
        } catch (Throwable t) {
            throw new IllegalStateException(getUnderlyingCause(t));
        }
    }

    private static Throwable getUnderlyingCause(Throwable t) {
        if (t instanceof InvocationTargetException) {
            return getUnderlyingCause(((InvocationTargetException) t).getTargetException());
        }
        return t;
    }

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        final List<Method> init = new ArrayList<>();
        final List<Method> start = new ArrayList<>();
        final List<Method> stop = new ArrayList<>();
        final Class<?> testClass = testInstance.getClass();
        final Method[] methods = testClass.getDeclaredMethods();
        for (final Method method : methods) {
            method.setAccessible(true); // NOSONAR
            if (method.isAnnotationPresent(Init.class)) {
                init.add(validateInitMethod(method));
            }
            if (method.isAnnotationPresent(Start.class)) {
                start.add(validateStartMethod(method));
            }
            if (method.isAnnotationPresent(Stop.class)) {
                stop.add(validateStopMethod(method));
            }
        }
        final Field[] fields = testClass.getDeclaredFields();
        for (final Field field : fields) {
            if (field.getType().isAssignableFrom(FxRobot.class)) {
                setField(testInstance, field, this);
            }
        }

        applicationFixture = new AnnotationBasedApplicationFixture(testInstance, init, start, stop);

        // check if @TestInFxThread annotation is present on class level
        if (isTestInFxThreadAnnotationPresent(testClass)) {
            executeAllMethodsInFxThread = true;
        }
    }

    /**
     * Starts the JavaFX application thread. This method must be called AFTER the test instance is internally created.
     */
    protected void startJavaFxApplication() {
        // launch JavaFX application
        try {
            FxToolkit.setupApplication(() -> new ApplicationAdapter(applicationFixture));
        } catch (final TimeoutException e) {
            throw new IllegalStateException("Unable to start JavaFX application!", e);
        }
    }

    /**
     * Stops the JavaFX application thread.
     */
    protected void stopJavaFxApplication() {
        try {
            // shutdown JavaFX application and thread
            FxToolkit.cleanupApplication(new ApplicationAdapter(applicationFixture));
        } catch (final TimeoutException e) {
            throw new IllegalStateException("Unable to stop JavaFX application!", e);
        }
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("headless.geometry", "1600x1200-32");
        System.setProperty("java.awt.headless", "true");
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

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().isAssignableFrom(FxRobot.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return this;
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        // Cleaning the remaining UI events (e.g. a mouse press that is still waiting
        // for a mouse release)
        // Not cleaning these events may have side-effects on the next UI tests
        release(new KeyCode[0]); // NOSONAR
        release(new MouseButton[0]); // NOSONAR
        // Required to wait for the end of the UI events processing
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Method validateInitMethod(final Method initMethod) {
        if (initMethod.getParameterCount() != 0) {
            throw new IllegalStateException("Method annotated with @Init should have no arguments");
        }
        return initMethod;
    }

    private Method validateStartMethod(final Method startMethod) {
        final Class<?>[] parameterTypes = startMethod.getParameterTypes();
        if (parameterTypes.length != 1 || !parameterTypes[0].isAssignableFrom(javafx.stage.Stage.class)) {
            throw new IllegalStateException(
                    "Method annotated with @Start should have one argument of type " + "javafx.stage.Stage");
        }
        return startMethod;
    }

    private Method validateStopMethod(final Method stopMethod) {
        if (stopMethod.getParameterCount() != 0) {
            throw new IllegalStateException("Method annotated with @Stop should have no arguments");
        }
        return stopMethod;
    }

    private void setField(final Object instance, final Field field, final Object val) throws IllegalAccessException {
        final boolean wasAccessible = field.canAccess(instance);
        try {
            field.setAccessible(true); // NOSONAR
            field.set(instance, val); // NOSONAR
        } finally {
            field.setAccessible(wasAccessible);
        }
    }

	private static final class AnnotationBasedApplicationFixture implements ApplicationFixture {

        private final Object testInstance;

        private final List<Method> init;

        private final List<Method> start;

        private final List<Method> stop;

        private AnnotationBasedApplicationFixture(final Object testInstance, final List<Method> init,
                final List<Method> start, final List<Method> stop) {
            this.testInstance = testInstance;
            this.init = init;
            this.start = start;
            this.stop = stop;
        }

        @Override
        public void init() throws InvocationTargetException, IllegalAccessException {
            for (final Method method : init) {
                method.invoke(testInstance);
            }
        }

        @Override
        public void start(final Stage stage) throws InvocationTargetException, IllegalAccessException {
            for (final Method method : start) {
                method.invoke(testInstance, stage);
            }
        }

        @Override
        public void stop() throws InvocationTargetException, IllegalAccessException {
            for (final Method method : stop) {
                method.invoke(testInstance);
            }
        }

    }

    /**
     * Proceeds the test method invocation in the JavaFX thread and waits for its termination.
     *
     * @param invocation
     *            the intercepted test method invocation
     */
    private void proceedInFxThread(final Invocation<Void> invocation) throws Throwable {
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
     * Invokes a <tt>javafx.concurrent.Task</tt> in the JavaFX Thread and waits while it's finished. Like
     * SwingUtilities.invokeAndWait does for EDT.
     *
     * @param task
     *            the runnable that has to be called on JavaFX thread.
     * @throws InterruptedException
     *             f the execution is interrupted.
     * @throws ExecutionException
     *             If a exception is occurred in the run method of the Runnable
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
     * Checks if the {@link TestInFxThread} annotation is present on the given element.
     *
     * @param element
     *            the element to check
     * @return {@code true}, if and only if the given element carries the {@link TestInFxThread} annotation.
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
