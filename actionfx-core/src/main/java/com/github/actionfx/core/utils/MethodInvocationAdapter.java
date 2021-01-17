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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import com.github.actionfx.core.annotation.AFXArgHint;
import com.github.actionfx.core.annotation.ArgumentHint;

/**
 * Adapter for invoking methods with variable method arguments.
 * <p>
 * This adapter tries to find the best match for method arguments based on a
 * supplied list of {@link ParameterValues}. In case two method arguments have
 * the same type, the annotation {@link AFXArgHint} is evaluated for some
 * further hints, which value to take for which method argument.
 *
 * @author koster
 *
 */
public class MethodInvocationAdapter {

	/**
	 * String fragment of a method argument that represents the "old" value.
	 * Requires the compiler to preserve argument names. If this is not the case,
	 * use {@link AFXArgHint} annotation on method arguments.
	 */
	public static final String PARAMETER_NAME_FRAGMENT_OLD = "old";

	/**
	 * String fragment of a method argument that represents the "new" value.
	 * Requires the compiler to preserve argument names. If this is not the case,
	 * use {@link AFXArgHint} annotation on method arguments.
	 */
	public static final String PARAMETER_NAME_FRAGMENT_NEW = "new";

	private final Object instance;

	private final Method method;

	private final Object[] methodArguments;

	/**
	 * Constructor that accepts a method together with the holding {@code instance}
	 * and candidates for method arguments.
	 *
	 * @param instance                 the instance holding the method
	 * @param method                   the method to execute
	 * @param availableParameterValues parameter candidates
	 */
	public MethodInvocationAdapter(final Object instance, final Method method,
			final Object... availableParameterValues) {
		this(instance, method, toParameterValues(availableParameterValues));
	}

	/**
	 * Constructor that accepts a method together with the holding {@code instance}
	 * and candidates for method arguments of type {@link ParameterValue}. Use this
	 * constructor, if you have two method arguments of the same type, while you
	 * want to distinguish between the two values (e.g. "old value" and "new
	 * value").
	 *
	 * @param instance                 the instance holding the method
	 * @param method                   the method to execute
	 * @param availableParameterValues parameter candidates
	 */
	public MethodInvocationAdapter(final Object instance, final Method method,
			final ParameterValue... availableParameterValues) {
		this.instance = instance;
		this.method = method;
		methodArguments = matchValuesToParameters(method.getParameters(),
				availableParameterValues != null ? availableParameterValues : new ParameterValue[0]);
	}

	/**
	 * Invokes the method.
	 *
	 * @param <T> the return type of the method
	 * @return the return value of the method
	 */
	@SuppressWarnings("unchecked")
	public <T> T invoke() {
		try {
			return (T) method.invoke(instance, methodArguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException("Invocation of method '" + method.getName() + "' failed!", e);
		}
	}

	public Object getInstance() {
		return instance;
	}

	public Method getMethod() {
		return method;
	}

	/**
	 * Matches values from {@link #availableParameter} to the given
	 * {@code parameters}.
	 *
	 * @param parameters               the desired parameters
	 * @param availableParameterValues all available parameter value candidates
	 * @return the values that match the desired parameters
	 */
	private Object[] matchValuesToParameters(final Parameter[] parameters,
			final ParameterValue[] availableParameterValues) {
		// create a new list with parameter values - the values will be "consumed"
		final List<ParameterValue> parameterValues = new ArrayList<>(Arrays.asList(availableParameterValues));
		final Object[] values = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			values[i] = determineInstanceByParameter(parameters[i], parameterValues);
		}
		return values;
	}

	/**
	 * Determines an instance for the given {@code parameter} from a list of
	 * {@code candidates}.
	 *
	 * @param parameter  the parameter to search a value for
	 * @param candidates the list of candidates to be searched for the best matching
	 *                   parameter
	 * @return the instance matching the given {@code type}, or {@code null}, if no
	 *         instance matches the given {@code type}
	 */
	private static Object determineInstanceByParameter(final Parameter parameter,
			final List<ParameterValue> candidates) {
		if (candidates.isEmpty()) {
			return null;
		}
		ParameterValue bestMatch = null;

		for (final ListIterator<ParameterValue> iterator = candidates.listIterator(); iterator.hasNext();) {
			final ParameterValue candidate = iterator.next();
			// check for @AFXArgHint annotations, these have the highest priority
			if (candidateMatchesNewValueParameter(parameter, candidate)
					|| candidateMatchesOldValueParameter(parameter, candidate)) {
				// we will not find a better match than this, so we "consume" the parameter and
				// immediately return it
				iterator.remove();
				return candidate.getValue();
			}
			// check if the type matches. in this case, the first arguments have higher
			// priority than the last ones
			if (candidateMatchesTypeParameter(parameter, candidate) && bestMatch == null) {
				// no bestMatch yet found and current matches by type -
				// save this match in a local variable, we still might find a better match
				bestMatch = candidate;
			}
		}
		if (bestMatch == null) {
			// no match found...
			return null;
		} else {
			// we found the proper type, so we consume the match and return its value
			candidates.remove(bestMatch);
			return bestMatch.getValue();
		}
	}

	/**
	 * Checks, whether the given {@code candidate} matches the method
	 * {@link Parameter} AND whether both represent the "new" value.
	 *
	 * @param the       method parameter
	 * @param candidate the candidate
	 * @return {@link true}, if and only if the candidate is suitable for the given
	 *         {@link Parameter}.
	 */
	private static boolean candidateMatchesNewValueParameter(final Parameter parameter,
			final ParameterValue candidate) {
		return isNewValueParameter(parameter) && parameterValueHasHint(candidate, ArgumentHint.NEW_VALUE)
				&& candidateMatchesTypeParameter(parameter, candidate);
	}

	/**
	 * Checks, whether the given method {@link Parameter} represents the "new"
	 * value. This is checked by 1.) if the name contains the string fragment "new"
	 * (compiler must preserve argument names in that case) OR 2.) the method
	 * parameter is annotated by corresponding an @AFXArgHint annotation.
	 *
	 * @param parameter the parameter to check
	 * @return {@code true}, if the parameter represents an "new" value
	 */
	private static boolean isNewValueParameter(final Parameter parameter) {
		final AFXArgHint hint = parameter.getAnnotation(AFXArgHint.class);
		return parameter.getName() != null && parameter.getName().contains(PARAMETER_NAME_FRAGMENT_NEW)
				|| hint != null && hint.value() == ArgumentHint.NEW_VALUE;
	}

	/**
	 * Checks, whether the given {@code candidate} matches the method
	 * {@link Parameter} AND whether both represent the "old" value.
	 *
	 * @param parameter the method parameter
	 * @param candidate the candidate
	 * @return {@link true}, if and only if the candidate is suitable for the given
	 *         {@link Parameter}.
	 */
	private static boolean candidateMatchesOldValueParameter(final Parameter parameter,
			final ParameterValue candidate) {
		return isOldValueParameter(parameter) && parameterValueHasHint(candidate, ArgumentHint.OLD_VALUE)
				&& candidateMatchesTypeParameter(parameter, candidate);
	}

	/**
	 * Checks, whether the given method {@link Parameter} represents the "old"
	 * value. This is checked by 1.) if the name contains the string fragment "old"
	 * (compiler must preserve argument names in that case) OR 2.) the method
	 * parameter is annotated by corresponding an @AFXArgHint annotation.
	 *
	 * @param parameter the parameter to check
	 * @return {@code true}, if the parameter represents an "old" value
	 */
	private static boolean isOldValueParameter(final Parameter parameter) {
		final AFXArgHint hint = parameter.getAnnotation(AFXArgHint.class);
		return parameter.getName() != null && parameter.getName().contains(PARAMETER_NAME_FRAGMENT_OLD)
				|| hint != null && hint.value() == ArgumentHint.OLD_VALUE;
	}

	/**
	 *
	 * @param parameterValue the parameter value to check
	 * @param hint           the expected argument hint
	 * @return {@code true}, if and only if the parameter value holds the expected
	 *         {@code hint}.
	 */
	private static boolean parameterValueHasHint(final ParameterValue parameterValue, final ArgumentHint hint) {
		return parameterValue.getHint() == hint;
	}

	/**
	 * Checks, whether the given {@code candidate} matches the type of the method
	 * {@link Parameter}.
	 *
	 * @param parameter the method parameter
	 * @param candidate the candidate
	 * @return {@code true}, if and only if the candidate has the desired type of
	 *         the method parameter
	 */
	private static boolean candidateMatchesTypeParameter(final Parameter parameter, final ParameterValue candidate) {
		return parameter.getType().isAssignableFrom(candidate.getType());
	}

	/**
	 * Maps the given {@code values} to instances of {@link ParameterValue}. Please
	 * note that this method can only assume that parameter values are type-based
	 * parameter with hint {@link ParameterHint#TYPE_BASED}.
	 *
	 * @param values the values to map
	 * @return the corresponding parameter values
	 */
	private static ParameterValue[] toParameterValues(final Object... values) {
		if (values == null || values.length == 0) {
			return new ParameterValue[0];
		}
		final ParameterValue[] result = new ParameterValue[values.length];
		int index = 0;
		for (final Object value : values) {
			result[index++] = ParameterValue.of(value);
		}
		return result;
	}

	/**
	 * A representation of a method parameter, together with a {@link ParameterHint}
	 *
	 * @author koster
	 *
	 */
	public static class ParameterValue {

		private final ArgumentHint hint;

		private final Object value;

		public ParameterValue(final ArgumentHint hint, final Object value) {
			this.hint = hint;
			this.value = value;
		}

		public ParameterValue(final Object value) {
			this(ArgumentHint.TYPE_BASED, value);
		}

		public static ParameterValue of(final ArgumentHint hint, final Object value) {
			return new ParameterValue(hint, value);
		}

		public static ParameterValue of(final Object value) {
			return new ParameterValue(value);
		}

		public static ParameterValue ofOldValue(final Object value) {
			return new ParameterValue(ArgumentHint.OLD_VALUE, value);
		}

		public static ParameterValue ofNewValue(final Object value) {
			return new ParameterValue(ArgumentHint.NEW_VALUE, value);
		}

		public ArgumentHint getHint() {
			return hint;
		}

		public Object getValue() {
			return value;
		}

		public Class<?> getType() {
			return value.getClass();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (hint == null ? 0 : hint.hashCode());
			result = prime * result + (value == null ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ParameterValue other = (ParameterValue) obj;
			if (hint != other.hint) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}
	}

}
