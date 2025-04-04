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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.github.actionfx.core.utils.ExceptionUtils;

/**
 * Instantiation supplier that uses defined constructors of given class.
 *
 * @author koster
 *
 * @param <T>
 *            the type to be instantiated
 */
public final class ConstructorBasedInstantiationSupplier<T> extends AbstractInstantiationSupplier<T> {

    private final Class<T> clazz;

    private Constructor<?> constructor;

    private Object[] resolvedConstructorArguments;

    /**
     * Accepts the class to instantiate together with {@code constructorArguments}. Leave the
     * {@code constructorArguments} empty to invoke the default no-argument constructor.
     *
     * @param clazz
     *            the class to instantiate
     * @param constructorArguments
     *            the constructor arguments
     */
    public ConstructorBasedInstantiationSupplier(final Class<T> clazz, final Object... constructorArguments) {
        this.clazz = clazz;
        resolveConstructorAndArguments(constructorArguments);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T createInstance() {
        try {
            return (T) constructor.newInstance(resolvedConstructorArguments);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            throw new IllegalStateException("Can not instantiate class '" + clazz.getCanonicalName() + "'!", e);
        }
    }

    /**
     * Find the best fitting constructor for the supplied constructor arguments.
     *
     * @param constructorArguments
     *            the available constructor arguments
     */
    protected void resolveConstructorAndArguments(final Object... constructorArguments) {// NOSONAR
        if (findConstructorWithExactlyMatchingTypes(constructorArguments)) {
            // we found an exact match, no further checking required.
            return;
        }
        // check all available constructors for finding the best fit with the supplied
        // constructor arguments
        final int argCount = constructorArguments.length;
        final Constructor<?>[] candidates = clazz.getDeclaredConstructors();
        int minTypeDiffWeight = Integer.MAX_VALUE;
        Constructor<?> matchingConstructor = null;
        Object[] matchingConstructorArguments = null;
        int foundConstructorWithNumArguments = 0; // constructor with more arguments is preferred
        for (final Constructor<?> candidate : candidates) {
            final Class<?>[] paramTypes = candidate.getParameterTypes();
            // the optimal solution is the constructor that is able to consume all arguments
            if (candidate.getParameterCount() == argCount) {
                final int typeDiffWeight = getTypeDifferenceWeight(paramTypes, constructorArguments);
                // in case a typeDiffWeight for less constructor arguments has been already
                // computed,
                // we will set the following value to Integer.MAX_VALUE, because we prefer
                // to have the maximum constructor invoked
                final int typeDiffWeightWithMaxArgumentCountPreferred = foundConstructorWithNumArguments > 0
                        && foundConstructorWithNumArguments < argCount ? Integer.MAX_VALUE : minTypeDiffWeight;
                if (typeDiffWeight < typeDiffWeightWithMaxArgumentCountPreferred) {
                    minTypeDiffWeight = typeDiffWeight;
                    matchingConstructor = candidate;
                    matchingConstructorArguments = constructorArguments;
                    foundConstructorWithNumArguments = argCount;
                }
            } else if (foundConstructorWithNumArguments <= candidate.getParameterCount()
                    && candidate.getParameterCount() < argCount) {
                final Object[] matchedValues = matchConstructorArguments(paramTypes, constructorArguments);
                final int typeDiffWeight = getTypeDifferenceWeight(paramTypes, matchedValues);
                if (typeDiffWeight < minTypeDiffWeight) {
                    minTypeDiffWeight = typeDiffWeight;
                    matchingConstructor = candidate;
                    matchingConstructorArguments = matchedValues;
                    foundConstructorWithNumArguments = candidate.getParameterCount();
                }
            }
        }
        if (matchingConstructor == null) {
            throw new IllegalStateException("Unable to locate a matching constructor in class '"
                    + clazz.getCanonicalName() + "' with the supplied arguments: "
                    + ExceptionUtils.toPrintableString(constructorArguments));
        }
        constructor = matchingConstructor;
        resolvedConstructorArguments = matchingConstructorArguments;
    }

    /**
     * Tries to find the constructor that exactly matches the supplied {@code constructorArguments}.
     *
     * @param constructorArguments
     *            the constructor arguments that need to be accepted by the constructor
     * @return {@code true}, if a constructor with exactly matching types could be found, {@code false} otherwise.
     */
    private boolean findConstructorWithExactlyMatchingTypes(final Object... constructorArguments) {
        // we try to find a direct match with the supplied constructor arguments
        final Class<?>[] constructorArgumentTypes = new Class[constructorArguments.length];
        int index = 0;
        for (final Object argument : constructorArguments) {
            constructorArgumentTypes[index++] = argument.getClass();
        }
        try {
            constructor = clazz.getDeclaredConstructor(constructorArgumentTypes);
            resolvedConstructorArguments = constructorArguments;
            return true;
        } catch (final NoSuchMethodException e) {
            // no direct match found, we continue our search
        }
        return false;
    }

    /**
     * Matches the given {@code values} to the expected constructor arguments.
     *
     * @param constructorArgs
     *            the type of constructor arguments
     * @param values
     *            the values to be matched to the constructor arguments
     * @return the matched values
     */
    private Object[] matchConstructorArguments(final Class<?>[] constructorArgs, final Object[] values) {
        final Object[] matched = new Object[constructorArgs.length];
        for (int i = 0; i < constructorArgs.length; i++) {
            final Object selectedValue = selectValueByType(constructorArgs[i], values);
            if (selectedValue == null) {
                // not all values can be match to constructor arguments, so we can cancel here
                return new Object[0];
            }
            matched[i] = selectedValue;
        }
        return matched;
    }

    /**
     * Selects a value for the supplied {@code values} by {@code type}. This method checks simply, if the value is
     * assignable from the given {@code type}.
     *
     * @param type
     *            the type to select
     * @param values
     *            the values to select from
     * @return the selected value, or {@code null}, if no value exists for the given {@code type}
     */
    private Object selectValueByType(final Class<?> type, final Object[] values) {
        for (int i = 0; i < values.length; i++) {
            if (type.isAssignableFrom(values[i].getClass())) {
                return values[i];
            }
        }
        return null;
    }

    /**
     * Algorithm that judges the match between the declared parameter types of a candidate method and a specific list of
     * arguments that this method is supposed to be invoked with.
     * <p>
     * Determines a weight that represents the class hierarchy difference between types and arguments. A direct match,
     * i.e. type Integer -> arg of class Integer, does not increase the result - all direct matches means weight 0. A
     * match between type Object and arg of class Integer would increase the weight by 2, due to the superclass 2 steps
     * up in the hierarchy (i.e. Object) being the last one that still matches the required type Object. Type Number and
     * class Integer would increase the weight by 1 accordingly, due to the superclass 1 step up the hierarchy (i.e.
     * Number) still matching the required type Number. Therefore, with an arg of type Integer, a constructor (Integer)
     * would be preferred to a constructor (Number) which would in turn be preferred to a constructor (Object). All
     * argument weights get accumulated.
     *
     * @param paramTypes
     *            the parameter types to match
     * @param args
     *            the arguments to match
     * @return the accumulated weight for all arguments
     */
    private static int getTypeDifferenceWeight(final Class<?>[] paramTypes, final Object[] args) {
        if (args.length == 0) {
            return Integer.MAX_VALUE;
        }
        int result = 0;
        for (int i = 0; i < paramTypes.length; i++) {
            if (!paramTypes[i].isAssignableFrom(args[i].getClass())) {
                return Integer.MAX_VALUE;
            }
            final Class<?> paramType = paramTypes[i];
            Class<?> superClass = args[i].getClass().getSuperclass();
            while (superClass != null) {
                if (paramType.equals(superClass)) {
                    result = result + 2;
                    superClass = null;
                } else if (paramType.isAssignableFrom(superClass)) {
                    result = result + 2;
                    superClass = superClass.getSuperclass();
                } else {
                    superClass = null;
                }
            }
            if (paramType.isInterface()) {
                result = result + 1;
            }
        }
        return result;
    }

}
