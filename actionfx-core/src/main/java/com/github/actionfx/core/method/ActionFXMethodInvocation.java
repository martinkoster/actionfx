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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;

import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXRequiresUserConfirmation;
import com.github.actionfx.core.utils.ExceptionUtils;
import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Convenience class for calling a method that can contain ActionFX annotations (like
 * {@link AFXRequiresUserConfirmation} or method argument annotations like {@link AFXControlValue}.
 * <p>
 * This class is used within the ActionFX framework for calling {@link AFXOnAction} annotated methods, however, it can
 * be also used in application code, in case an "onAction" property event handler needs to be programmatically set by
 * the developer.
 *
 * @author koster
 *
 */
public class ActionFXMethodInvocation {

    private final ControllerMethodInvocationAdapter controllerMethodInvocationAdapter;

    /**
     * Constructor receiving all required information including the {@code methodName} as a string for issuing a method
     * invocation. Take care when using this constructor as the {@code methodName} might not exist or methods with that
     * name might be ambiguous for the supplied {@code arguments}.
     *
     * @param instance
     *            the instance holding the method
     * @param methodName
     *            the method name itself
     * @param allowLessOrNoMethodArguments
     *            in case there is no method that supports the full set of {@code arguments}, this flag determines
     *            whether it is OK to "downgrade" to a method that only accepts a subset of arguments, or even no
     *            arguments at all.
     * @param arguments
     *            the arguments that shall be passed to the method. Please note that there is an internal type-matching
     *            based on assigning the supplied arguments to the parameters of the method. The actual method can have
     *            additional parameter that are e.g. annotated by {@link AFXControlValue} that is internally looked up
     *            and of course, does not need to be provided as part of {@code arguments}.
     */
    public ActionFXMethodInvocation(final Object instance, final String methodName,
            final boolean allowLessOrNoMethodArguments, final Object... arguments) {
        this(instance,
                identifyBestMatchingMethod(instance.getClass(), methodName, allowLessOrNoMethodArguments, arguments),
                arguments);
    }

    /**
     * Constructor receiving all required information for issuing a method invocation.
     *
     * @param instance
     *            the instance holding the method
     * @param method
     *            the method to invoke
     * @param arguments
     *            the arguments that shall be passed to the method. Please note that there is an internal type-matching
     *            based on assigning the supplied arguments to the parameters of the method. The actual method can have
     *            additional parameter that are e.g. annotated by {@link AFXControlValue} that is internally looked up
     *            and of course, does not need to be provided as part of {@code arguments}.
     */
    public ActionFXMethodInvocation(final Object instance, final Method method, final Object... arguments) {
        controllerMethodInvocationAdapter = new ControllerMethodInvocationAdapter(instance, method, arguments);
    }

    /**
     * Performs a synchronous method call.
     *
     * @return the returned value from the called method, {@code null} for {@code void} methods.
     */
    public <T> T call() {
        return controllerMethodInvocationAdapter.invoke();
    }

    /**
     * Performs an asynchronous method call, where the return value of the method is passed on to the supplied
     * {@link Consumer}. The consumer is guaranteed to be executed inside the JavaFX-thread.
     *
     * @param <T>
     *            the return type
     * @param consumer
     *            the consumer
     */
    public <T> void callAsync(final Consumer<T> consumer) {
        controllerMethodInvocationAdapter.invokeAsynchronously(consumer);
    }

    /**
     * Returns the method on that the invocation is applied to.
     *
     * @return the method to be invoked
     */
    public Method getMethod() {
        return controllerMethodInvocationAdapter.getMethod();
    }

    /**
     * Tries to identify the best match of methods with nane {@code methodName} and the supplied {@code arguments},
     * considering that there can be also additionally ActionFX-annotated method arguments. In case there is no match or
     * matches are ambiguous, then an {@link IllegalArgumentException} is thrown.
     *
     * @param clazz
     *            the class that is supposed to hold the method
     * @param methodName
     *            the method name to search for
     * @param allowLessOrNoMethodArguments
     *            in case there is no method that supports the full set of {@code arguments}, this flag determines
     *            whether it is OK to "downgrade" to a method that only accepts a subset of arguments, or even no
     *            arguments at all.
     * @param arguments
     *            the method arguments
     * @return the best matching method
     * @throws IllegalArgumentException
     *             in case there is no match or the matches are ambiguous
     */
    private static Method identifyBestMatchingMethod(final Class<?> clazz, final String methodName,
            final boolean allowLessOrNoMethodArguments,
            final Object[] arguments) {
        final List<Method> methods = ReflectionUtils.findMethods(clazz,
                method -> method.getName().equals(methodName)
                        && parameterMatches(method, false, arguments));
        if (methods.isEmpty()) {
            // no method with all arguments found - shall we try to "downgrade" the method?
            if (allowLessOrNoMethodArguments) {
                final List<Method> fallbackMethods = ReflectionUtils.findMethods(clazz,
                        method -> method.getName().equals(methodName)
                                && parameterMatches(method, true, arguments));
                if (!fallbackMethods.isEmpty() && fallbackMethods.size() == 1) {
                    return fallbackMethods.get(0);
                }
            }
            throw new IllegalArgumentException("Class '" + clazz.getCanonicalName()
                    + "' does not have method with name '" + methodName + "' that accepts the supplied arguments '"
                    + ExceptionUtils.toPrintableString(arguments) + "'!");
        } else if (methods.size() > 1) {
            throw new IllegalArgumentException("Class '" + clazz.getCanonicalName()
                    + "' has ambiguously matching methods with name '" + methodName
                    + "' that accept the supplied arguments '" + ExceptionUtils.toPrintableString(arguments) + "'!");
        } else {
            // perfect, we found one match
            return methods.get(0);
        }
    }

    /**
     * Checks, whether the supplied {@code method} accepts the supplied {@code arguments}, ignoring method parameters
     * carrying ActionFX annotations that do resolve their values on their own.
     *
     * @param method
     *            the method to check its parameters against the supplied {@code arguments}.
     * @param allowLessOrNoMethodArguments
     *            in case there is no method that supports the full set of {@code arguments}, this flag determines
     *            whether it is OK to "downgrade" to a method that only accepts a subset of arguments, or even no
     *            arguments at all.
     * @param arguments
     *            the arguments
     * @return {@code true}, if the supplied {@code method} is able to accept the supplied {@code arguments},
     *         {@code false} otherwise.
     */
    private static boolean parameterMatches(final Method method, final boolean allowLessOrNoMethodArguments,
            final Object[] arguments) {
        // remove all parameters that carry an ActionFX method argument annotation or
        // ActionEvent parameters
        final List<Parameter> parameterList = new ArrayList<>(Arrays.asList(method.getParameters())).stream().filter(
                param -> !containsActionFXAnnotations(param.getAnnotations()) && !isActionEventType(param.getType()))
                .collect(Collectors.toList());
        // from the arguments, we have to remove the ActionEvent (if present), because
        // we can rely on the correct position
        final List<Object> argumentList = new ArrayList<>(Arrays.asList(arguments)).stream()
                .filter(arg -> arg == null || !isActionEventType(arg.getClass())).collect(Collectors.toList());
        if (parameterList.isEmpty() && argumentList.isEmpty()) {
            // method does not have parameters and no parameters are expected...found the best match
            return true;
        }
        // for all remaining parameters, they need to accept the argument types
        if (!allowLessOrNoMethodArguments && parameterList.size() != argumentList.size()) {
            return false;
        }
        for (int i = 0; i < parameterList.size(); i++) {
            final Parameter parameter = parameterList.get(i);
            if (argumentList.size() > i) {
                final Object argument = argumentList.get(i);
                if (argument == null && parameter.getType().isPrimitive()
                        || argument != null && !ClassUtils.isAssignable(parameter.getType(), argument.getClass())) {
                    return false;
                }
            }
        }
        // all challenges mastered....so we have a match
        return true;
    }

    /**
     * Checks, if the supplied array of {@link Annotation} contains an ActionFX annotation.
     *
     * @param annotations
     *            the array to check
     * @return {@code true}, if and only if the array contains an ActionFX annotation.
     */
    private static boolean containsActionFXAnnotations(final Annotation[] annotations) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType().getPackage().getName()
                    .equals(AFXControlValue.class.getPackage().getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks, whether the given type is of type {@link ActionEvent}.
     *
     * @param type
     *            the type parameter to check
     * @return {@code true},if and only if the given type is of type {@link ActionEvent}.
     */
    private static boolean isActionEventType(final Class<?> type) {
        return ActionEvent.class.isAssignableFrom(type);
    }

    /**
     * Creates an {@link EventHandler} that can be directly set to an "onAction" property of a JavaFX node. The method
     * is called in a synchronous fashion.
     *
     * @param instance
     *            the instance holding the method to invoke
     * @param methodName
     *            the name of the method to invoke
     * @param arguments
     *            the method arguments to additionally provide to the method.
     * @return the created event handler instance
     */
    public static EventHandler<ActionEvent> forOnActionProperty(final Object instance, final String methodName,
            final Object... arguments) {
        return actionEvent -> new ActionFXMethodInvocation(instance, methodName, true, merge(actionEvent, arguments))
                .call();
    }

    /**
     * Creates an {@link EventHandler} that can be directly set to an "onAction" property of a JavaFX node. The method
     * is called in a synchronous fashion.
     *
     * @param instance
     *            the instance holding the method to invoke
     * @param method
     *            the method to invoke
     * @param arguments
     *            the method arguments to additionally provide to the method.
     * @return the created event handler instance
     */
    public static EventHandler<ActionEvent> forOnActionProperty(final Object instance, final Method method,
            final Object... arguments) {
        return actionEvent -> new ActionFXMethodInvocation(instance, method, merge(actionEvent, arguments))
                .call();
    }

    /**
     * Creates an {@link EventHandler} that can be directly set to an "onAction" property of a JavaFX node. The method
     * is called in a asynchronous fashion.
     *
     * @param <T>
     *            the return type of the method to invoke
     * @param consumer
     *            the consumer
     * @param instance
     *            the instance holding the method to invoke
     * @param methodName
     *            the name of the method to invoke
     * @param arguments
     *            the method arguments to additionally provide to the method.
     * @return the created event handler instance
     */
    public static <T> EventHandler<ActionEvent> forOnActionPropertyWithAsyncCall(final Consumer<T> consumer,
            final Object instance, final String methodName, final Object... arguments) {
        return actionEvent -> new ActionFXMethodInvocation(instance, methodName, true, merge(actionEvent, arguments))
                .callAsync(consumer);
    }

    /**
     * Creates an {@link EventHandler} that can be directly set to an "onAction" property of a JavaFX node. The method
     * is called in a asynchronous fashion.
     *
     * @param <T>
     *            the return type of the method to invoke
     * @param consumer
     *            the consumer
     * @param instance
     *            the instance holding the method to invoke
     * @param method
     *            the method to invoke
     * @param arguments
     *            the method arguments to additionally provide to the method.
     * @return the created event handler instance
     */
    public static <T> EventHandler<ActionEvent> forOnActionPropertyWithAsyncCall(final Consumer<T> consumer,
            final Object instance, final Method method, final Object... arguments) {
        return actionEvent -> new ActionFXMethodInvocation(instance, method, merge(actionEvent, arguments))
                .callAsync(consumer);
    }

    /**
     * Creates a subscriber that executes the given {@code method} in the supplied {@code instance}, while a published
     * event is passed on to the method invocation.
     * <p>
     * This method can be used to create a subscriber for ActionFX' internal event bus (see
     * {@link com.github.actionfx.core.events.PriorityAwareEventBus} and
     * {@link com.github.actionfx.core.ActionFX#publishEvent(Object)}).
     *
     * @param <T>
     *            the event type
     * @param instance
     *            the instance hosting the supplied method
     * @param method
     *            the method to execute
     * @return the subscriber that can be handed over to the event bus for performing a subscription
     */
    public static <T> Consumer<T> forSubscriber(final Object instance, final Method method) {
        return event -> new ActionFXMethodInvocation(instance, method, event).call();
    }

    /**
     * Creates a subscriber that executes the given {@code method} asynchronously in the supplied {@code instance},
     * while a published event is passed on to the method invocation.
     * <p>
     * This method can be used to create a subscriber for ActionFX' internal event bus (see
     * {@link com.github.actionfx.core.events.PriorityAwareEventBus} and
     * {@link com.github.actionfx.core.ActionFX#publishEvent(Object)}).
     *
     * @param <T>
     *            the event type
     * @param instance
     *            the instance hosting the supplied method
     * @param method
     *            the method to execute
     * @return the subscriber that can be handed over to the event bus for performing a subscription
     */
    public static <T> Consumer<T> forSubscriberWithAsyncCall(final Object instance, final Method method) {
        return event -> new ActionFXMethodInvocation(instance, method, event).callAsync(retValue -> {
        });
    }

    /**
     * Adds an argument to a var-arg.
     *
     * @param o
     *            the object to add
     * @param arr
     *            the var-arg
     * @return the combination out of {@code o} and {@code arr}.
     */
    private static Object[] merge(final Object o, final Object... arr) {
        final Object[] newArray = new Object[arr.length + 1];
        newArray[0] = o;
        System.arraycopy(arr, 0, newArray, 1, arr.length);
        return newArray;
    }
}
