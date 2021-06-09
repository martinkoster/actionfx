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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.actionfx.core.annotation.BooleanOp;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Control;

/**
 * Builder class for creating a {@link BooleanBinding} for one or many
 * {@link Control}s.
 *
 * @author koster
 *
 */
public class ControlBasedBooleanBindingBuilder {

	// chain of predicates that are linked with each other via a booleanOp
	private final List<PredicateElement> predicateChain;

	private PredicateElement predicateElementUnderConstruction;

	// flag that determines, whether the result of the entire predicate chain shall
	// be negated or not
	private boolean negateChainResult = false;

	protected ControlBasedBooleanBindingBuilder() {
		predicateChain = new ArrayList<>();
		stackPredicateElement(BooleanOp.AND);
	}

	/**
	 * Creates a new {@link ControlBasedBooleanBindingBuilder}.
	 *
	 * @return the create instance
	 */
	public static ControlBasedBooleanBindingBuilder create() {
		return new ControlBasedBooleanBindingBuilder();
	}

	/**
	 * Builds the actual {@link BooleanBinding} with the parameters specified in
	 * this {@link ControlBasedBooleanBindingBuilder} instance.
	 *
	 * @return the constructed boolean binding
	 */
	public BooleanBinding build() {
		predicateElementUnderConstruction.verifyPredicateElementIsComplete();
		final Observable[] observables = predicateChain.stream().map(PredicateElement::extractObservables)
				.flatMap(List::stream).toArray(size -> new Observable[size]);

		return Bindings.createBooleanBinding(() -> {
			boolean result = true;
			for (final PredicateElement element : predicateChain) {
				result = combinedPredicateResults(result, element);
			}
			return negateChainResult ? !result : result;
		}, observables);
	}

	/**
	 * Combines the result of the predicates by applying the boolean operation
	 * defined by the {@code element}
	 *
	 * @param currentResult the current result of the chain evaluation
	 * @param element       the predicate element to evaluate and to "add" to the
	 *                      current result
	 * @return the combined result
	 */
	private boolean combinedPredicateResults(final boolean currentResult, final PredicateElement element) {
		boolean combined;
		switch (element.getBooleanOp()) {
		case OR:
			combined = currentResult || element.evaluatePredicate();
			break;
		default:
		case AND:
			combined = currentResult && element.evaluatePredicate();
		}
		return combined;
	}

	/**
	 * Adds one or multiple new {@link Control}s to this boolean binding builder.
	 *
	 * @param controls the control to add
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder forControl(final Control... controls) {
		return forControl(Arrays.stream(controls).map(ControlWrapper::of).toArray(size -> new ControlWrapper[size]));
	}

	/**
	 * Adds a new array of {@link ControlWrapper}s to this boolean binding builder.
	 *
	 * @param wrappers the control wrappers to add
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder forControl(final ControlWrapper... wrappers) {
		predicateElementUnderConstruction.forControl(wrappers);
		return this;
	}

	/**
	 * Defines a function for extracting the {@link Observable} from a
	 * {@link Control}. The observable is monitored for invalidation, which in turn
	 * triggers the {@link } to be evaluated again.
	 *
	 * @param observableExtractorFunction the function for extracting the observable
	 *                                    from a {@link Control}.
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder observableExtractorFunction(
			final Function<ControlWrapper, Observable> observableExtractorFunction) {
		predicateElementUnderConstruction.observableExtractorFunction(observableExtractorFunction);
		return this;
	}

	/**
	 * Defines a predicate that all controls need to fulfill, i.e. the function
	 * needs to evaluate to {@code true} for all controls.
	 *
	 * @param allFulfilledFunction the predicate
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder allFulfilledFunction(
			final Predicate<ControlWrapper> allFulfilledFunction) {
		predicateElementUnderConstruction.allFulfilledFunction(allFulfilledFunction);
		return this;
	}

	/**
	 * Defines a predicate that at least one control needs to fulfill, i.e. the
	 * function needs to evaluate to {@code true} for at least one control.
	 *
	 * @param atLeastFulfilledFunction the predicate
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder atLeastOneFulfilledFunction(
			final Predicate<ControlWrapper> atLeastOneFulfilledFunction) {
		predicateElementUnderConstruction.atLeastOneFulfilledFunction(atLeastOneFulfilledFunction);
		return this;
	}

	/**
	 * Stacks a new predicate element for construction.
	 *
	 * @param booleanOp the boolean operation how the new element shall be logically
	 *                  linked to previews chained elements
	 */
	private void stackPredicateElement(final BooleanOp booleanOp) {
		predicateElementUnderConstruction = new PredicateElement(booleanOp);
		predicateChain.add(predicateElementUnderConstruction);
	}

	/**
	 * Adds another predicate together with controls and an observable extractor
	 * function to the chain, while the element is logically ANDed with previous
	 * predicates.
	 *
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder and() {
		predicateElementUnderConstruction.verifyPredicateElementIsComplete();
		stackPredicateElement(BooleanOp.AND);
		return this;
	}

	/**
	 * Adds another predicate together with controls and an observable extractor
	 * function to the chain, while the element is logically ORed with previous
	 * predicates.
	 *
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder or() {
		predicateElementUnderConstruction.verifyPredicateElementIsComplete();
		stackPredicateElement(BooleanOp.OR);
		return this;
	}

	/**
	 * Negates the current predicate under construction.
	 *
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder negate() {
		predicateElementUnderConstruction.negate();
		return this;
	}

	/**
	 * Negates the complete chain of predicates in this boolean binding builder.
	 * <p>
	 * The method behaves like:
	 *
	 * <pre>
	 * boolen result = ! (predicate1 (AND|OR) predicate2 (AND|OR) predicate3 ... )
	 * </pre>
	 *
	 * @return this builder instance
	 */
	public ControlBasedBooleanBindingBuilder negateAll() {
		negateChainResult = !negateChainResult;
		return this;
	}

	/**
	 * Enum describes whether a predicate must match all controls or at least on
	 * controls.
	 *
	 * @author koster
	 *
	 */
	private enum MatchingOp {
		ALL_MATCH, AT_LEAST_ONE_MATCH
	}

	/**
	 * Represents a single predicate that can be chained together with other
	 * predicates.
	 *
	 * @author koster
	 *
	 */
	private static class PredicateElement {

		private final List<ControlWrapper> controlWrapper;

		// function that extracts the observable for a control, which in turn is
		// monitored for invalidation
		private Function<ControlWrapper, Observable> observableExtractorFunction;

		// function that is evaluated against all controls in this builder
		private Predicate<ControlWrapper> predicate;

		// boolean operator that describes how the single boolean bindings in the chain
		// shall be linked with each other
		private BooleanOp booleanOp = BooleanOp.AND;

		// matching operation (all match or at least one match)
		private MatchingOp matchingOp;

		// optionally negate the computed result
		private boolean negateResult = false;

		public PredicateElement(final BooleanOp booleanOp) {
			this.booleanOp = booleanOp;
			controlWrapper = new ArrayList<>();
		}

		/**
		 * Adds a new array of {@link ControlWrapper}s to this boolean binding builder.
		 *
		 * @param wrappers the control wrappers to add
		 * @return this builder instance
		 */
		public void forControl(final ControlWrapper... wrappers) {
			controlWrapper.addAll(Arrays.asList(wrappers));
		}

		/**
		 * Defines a function for extracting the {@link Observable} from a
		 * {@link Control}. The observable is monitored for invalidation, which in turn
		 * triggers the {@link } to be evaluated again.
		 *
		 * @param observableExtractorFunction the function for extracting the observable
		 *                                    from a {@link Control}.
		 * @return this builder instance
		 */
		public void observableExtractorFunction(
				final Function<ControlWrapper, Observable> observableExtractorFunction) {
			this.observableExtractorFunction = observableExtractorFunction;
		}

		/**
		 * Defines a predicate that all controls need to fulfill, i.e. the function
		 * needs to evaluate to {@code true} for all controls.
		 *
		 * @param allFulfilledFunction the predicate
		 */
		public void allFulfilledFunction(final Predicate<ControlWrapper> allFulfilledFunction) {
			verifyPredicateNotSet();
			predicate = allFulfilledFunction;
			matchingOp = MatchingOp.ALL_MATCH;
		}

		/**
		 * Defines a predicate that at least one control needs to fulfill, i.e. the
		 * function needs to evaluate to {@code true} for at least one control.
		 *
		 * @param atLeastFulfilledFunction the predicate
		 */
		public void atLeastOneFulfilledFunction(final Predicate<ControlWrapper> atLeastOneFulfilledFunction) {
			verifyPredicateNotSet();
			predicate = atLeastOneFulfilledFunction;
			matchingOp = MatchingOp.AT_LEAST_ONE_MATCH;
		}

		/**
		 * Negates the result yielded with {@link #allFulfilledFunction} and
		 * {@link #atLeastOneFulfilledFunction}. This function is toggeling negations,
		 * i.e. if you negate two times, the result is not negated anymore.
		 *
		 * @return this builder instance
		 */
		public void negate() {
			negateResult = !negateResult;
		}

		/**
		 * Evaluates the specified predicate against the supplied list of controls.
		 *
		 * @return the evaluation result
		 */
		public boolean evaluatePredicate() {
			boolean result;
			switch (matchingOp) {
			case AT_LEAST_ONE_MATCH:
				result = controlWrapper.stream().anyMatch(predicate);
				break;
			default:
			case ALL_MATCH:
				result = controlWrapper.stream().allMatch(predicate);
			}
			return negateResult ? !result : result;
		}

		/**
		 * Applies the observable extractor function and retrieves the "observable" from
		 * each control.
		 *
		 * @return the list of observables
		 */
		public List<Observable> extractObservables() {
			return controlWrapper.stream().map(observableExtractorFunction).collect(Collectors.toList());
		}

		/**
		 * Checks that the predicate is not yet set. Otherwise an
		 * {@link IllegalStateException} is thrown.
		 */
		private void verifyPredicateNotSet() {
			if (predicate != null) {
				throw new IllegalStateException(
						"A predicate has been already set via 'allFulfilledFunction' or 'atLeastOneFulfilledFunction'. Can not accept a second attribute. Please use 'and()' or 'or()' operations to chain bindings.");
			}
		}

		/**
		 * Verifies that the construction of an element in the chain is complete. Throws
		 * an {@link IllegalStateException} otherwise.
		 */
		public void verifyPredicateElementIsComplete() {
			if (observableExtractorFunction == null) {
				throw new IllegalStateException("'observableExtractorFunction' must be specified!");
			}
			if (predicate == null) {
				throw new IllegalStateException(
						"A predicate needs to be defined via 'allFulfilledFunction' or 'atLeastOneFulfilledFunction'!");
			}
			if (controlWrapper.isEmpty()) {
				throw new IllegalStateException("At least one control needs to be added via 'forControl'!");

			}
		}

		public BooleanOp getBooleanOp() {
			return booleanOp;
		}
	}

}
