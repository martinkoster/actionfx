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

import com.github.actionfx.core.annotation.BooleanOp;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Control;

/**
 * Builder class for creating a {@link BooleanBinding} for one or many {@link Control}s or {@link Observable}s.
 * <p>
 * This builder allows to create boolean bindings based on chained predicates of form:
 *
 * <pre>
 * boolean_value = NOT? (NOT? predicate1 [AND|OR] NOT? predicate2 [AND|OR] predicate3 ....)
 * </pre>
 *
 * The predicates themselves again are evaluated against a list of "testables", where you can chose, whether the
 * predicate needs to be {@code true} for all testables or only for at least one testable. (see {@link MatchingOp}).
 *
 *
 * @author koster
 *
 */
public class BooleanBindingBuilder {

    /**
     * Enum describes whether a predicate must match all testables or at least one testable.
     *
     * @author koster
     *
     */
    public enum MatchingOp {
        ALL_MATCH,
        AT_LEAST_ONE_MATCH
    }

    private final List<PredicateElement<?>> predicateElements = new ArrayList<>();

    // flag that determines, whether the result of the entire predicate chain shall
    // be negated or not
    private boolean negateChainResult;

    protected BooleanBindingBuilder() {
        // can not be instantiated directly
    }

    /**
     * Creates a new instance of this builder.
     *
     * @return a new builder instance
     */
    public static BooleanBindingBuilder builder() {
        return new BooleanBindingBuilder();
    }

    /**
     * Returns a new predicate builder that uses the supplied {@code predicate}.
     *
     * @param <T>
     *            the type of testables the predicate will be applied on
     * @param predicate
     *            the predicate
     * @return the predicate builder instance. When finished building the predicate, call
     *         {@link PredicateBuilder#build()}.
     */
    public <T> PredicateBuilder<T> forPredicate(final Predicate<T> predicate) {
        final PredicateBuilder<T> b = new PredicateBuilder<>(this);
        b.predicate(predicate);
        return b;
    }

    /**
     * Returns a new predicate builder that uses the supplied {@code predicate} and links with previously added
     * predicates in an AND-fashion.
     *
     * @param <T>
     *            the type of testables the predicate will be applied on
     * @param predicate
     *            the predicate
     * @return the predicate builder instance. When finished building the predicate, call
     *         {@link PredicateBuilder#build()}.
     */
    public <T> PredicateBuilder<T> andForPredicate(final Predicate<T> predicate) {
        final PredicateBuilder<T> b = forPredicate(predicate);
        b.booleanOp(BooleanOp.AND);
        return b;
    }

    /**
     * Returns a new predicate builder that uses the supplied {@code predicate} and links with previously added
     * predicates in an OR-fashion.
     *
     * @param <T>
     *            the type of testables the predicate will be applied on
     * @param predicate
     *            the predicate
     * @return the predicate builder instance. When finished building the predicate, call
     *         {@link PredicateBuilder#build()}.
     */
    public <T> PredicateBuilder<T> orForPredicate(final Predicate<T> predicate) {
        final PredicateBuilder<T> b = forPredicate(predicate);
        b.booleanOp(BooleanOp.OR);
        return b;
    }

    /**
     * Returns a new predicate builder that uses the supplied {@code predicate} and links with previously added
     * predicates in an AND-fashion and where all testables need to match the supplied predicate.
     *
     * @param <T>
     *            the type of testables the predicate will be applied on
     * @param predicate
     *            the predicate
     * @return the predicate builder instance. When finished building the predicate, call
     *         {@link PredicateBuilder#build()}.
     */
    public <T> PredicateBuilder<T> andAllTestablesFulfillPredicate(final Predicate<T> predicate) {
        final PredicateBuilder<T> b = andForPredicate(predicate);
        b.matchingOp(MatchingOp.ALL_MATCH);
        return b;
    }

    /**
     * Returns a new predicate builder that uses the supplied {@code predicate} and links with previously added
     * predicates in an AND-fashion and where at least one testable needs to match the supplied predicate.
     *
     * @param <T>
     *            the type of testables the predicate will be applied on
     * @param predicate
     *            the predicate
     * @return the predicate builder instance. When finished building the predicate, call
     *         {@link PredicateBuilder#build()}.
     */
    public <T> PredicateBuilder<T> andAtLeastOneTestableFulfillsPredicate(final Predicate<T> predicate) {
        final PredicateBuilder<T> b = forPredicate(predicate);
        b.booleanOp(BooleanOp.AND);
        b.matchingOp(MatchingOp.AT_LEAST_ONE_MATCH);
        return b;
    }

    /**
     * Returns a new predicate builder that uses the supplied {@code predicate} and links with previously added
     * predicates in an AND-fashion and where all testables need to match the supplied predicate.
     *
     * @param <T>
     *            the type of testables the predicate will be applied on
     * @param predicate
     *            the predicate
     * @return the predicate builder instance. When finished building the predicate, call
     *         {@link PredicateBuilder#build()}.
     */
    public <T> PredicateBuilder<T> orAllTestablesFulfillPredicate(final Predicate<T> predicate) {
        final PredicateBuilder<T> b = orForPredicate(predicate);
        b.matchingOp(MatchingOp.ALL_MATCH);
        return b;
    }

    /**
     * Returns a new predicate builder that uses the supplied {@code predicate} and links with previously added
     * predicates in an OR-fashion and where at least one testable needs to match the supplied predicate.
     *
     * @param <T>
     *            the type of testables the predicate will be applied on
     * @param predicate
     *            the predicate
     * @return the predicate builder instance. When finished building the predicate, call
     *         {@link PredicateBuilder#build()}.
     */
    public <T> PredicateBuilder<T> orAtLeastOneTestableFulfillsPredicate(final Predicate<T> predicate) {
        final PredicateBuilder<T> b = forPredicate(predicate);
        b.booleanOp(BooleanOp.OR);
        b.matchingOp(MatchingOp.AT_LEAST_ONE_MATCH);
        return b;
    }

    /**
     * Adds a single predicate to this chain builder, using the supplied {@code booleanOp} to connect it to a previously
     * added predicate. This method accepts all types of "testables", as long as you can retrieve the {@link Observable}
     * with a supplied {@code observableExtractorFuntion}.
     *
     * @param <T>
     *            the type of testable
     * @param booleanOp
     *            the boolean operation to link this predicate to a previously added predicate. In case no predicate has
     *            been added before, this value is ignored / not relevant for processing.
     * @param predicate
     *            the predicate that shall be applied to all testables
     * @param observableExtractorFuntion
     *            a function that targets at retrieving the observable value from the testable
     * @param matchingOp
     *            the matching operation, i.e. shall all testables fullfil the predicate, or at least one?
     * @param testables
     *            the testable objects that the predicate will be applied on
     * @param negateResult
     *            flag that determines whether the matching result over all testable shall be negated or not
     * @return the boolean binding builder
     */
    public <T> BooleanBindingBuilder addPredicate(final BooleanOp booleanOp, final Predicate<T> predicate,
            final Function<T, Observable> observableExtractorFuntion, final MatchingOp matchingOp,
            final T[] testables, final boolean negateResult) {
        predicateElements
                .add(new PredicateElement<>(booleanOp, predicate, matchingOp, observableExtractorFuntion, testables,
                        negateResult));
        return this;
    }

    /**
     * Adds a single predicate to this chain builder, using the supplied {@code booleanOp} to connect it to a previously
     * added predicate. This method is specialized on accepting {@link Observable}s as the "testable".
     *
     * @param booleanOp
     *            the boolean operation to link this predicate to a previously added predicate. In case no predicate has
     *            been added before, this value is ignored / not relevant for processing.
     * @param predicate
     *            the predicate that shall be applied to all testables
     * @param matchingOp
     *            the matching operation, i.e. shall all testables fullfil the predicate, or at least one?
     * @param testables
     *            the testable objects that the predicate will be applied on
     * @param negateResult
     *            flag that determines whether the matching result over all testable shall be negated or not
     * @return the boolean binding builder
     */
    public BooleanBindingBuilder addPredicate(final BooleanOp booleanOp, final Predicate<Observable> predicate,
            final MatchingOp matchingOp,
            final Observable[] testables, final boolean negateResult) {
        predicateElements
                .add(new PredicateElement<>(booleanOp, predicate, matchingOp, observable -> observable, testables,
                        negateResult));
        return this;
    }

    public BooleanBindingBuilder negateChainResult(final boolean negateChainResult) {
        this.negateChainResult = negateChainResult;
        return this;
    }

    /**
     * Builds the actual {@link BooleanBinding} with the parameters specified in this {@link BooleanBindingBuilder}
     * instance.
     *
     * @return the constructed boolean binding
     */
    public BooleanBinding build() {

        final Observable[] observables = predicateElements.stream().map(PredicateElement::getObservables)
                .flatMap(List::stream).toArray(size -> new Observable[size]);

        return Bindings.createBooleanBinding(() -> {
            boolean result = true;
            boolean firstLoopRun = true;
            for (final PredicateElement<?> element : predicateElements) {
                result = firstLoopRun ? element.evaluatePredicate() : combinedPredicateResults(result, element);
                firstLoopRun = false;
            }
            return negateChainResult ? !result : result;
        }, observables);
    }

    /**
     * Combines the result of the predicates by applying the boolean operation defined by the {@code element}
     *
     * @param currentResult
     *            the current result of the chain evaluation
     * @param element
     *            the predicate element to evaluate and to "add" to the current result
     * @return the combined result
     */
    private boolean combinedPredicateResults(final boolean currentResult, final PredicateElement<?> element) {
        boolean combined;
        switch (element.getBooleanOp()) {
        case OR:
            combined = currentResult || element.evaluatePredicate();
            break;
        case AND:
            combined = currentResult && element.evaluatePredicate();
            break;
            default:
            combined = currentResult && element.evaluatePredicate();
        }
        return combined;
    }

    /**
     * Builder class for creating a single predicate meant to be addded to a larger predicate chain.
     *
     * @author MartinKoster
     *
     * @param <T>
     *            the type of the testable that is checked against the predicate
     */
    public static final class PredicateBuilder<T> {

        private static final Function<Observable, Observable> DEFAULT_OBSERVABLE_EXTRACTOR_FUNCTION = Function
                .identity();

        // function that is evaluated against all controls in this builder
        private Predicate<T> predicate;

        // instances to be checked by the predicate
        private T[] testables;

        // function to retrieve the observable instances related to the testables
        private Function<T, Observable> observableExtractorFunction;

        // boolean operator that describes how the single boolean bindings in the chain
        // shall be linked with each other
        private BooleanOp booleanOp = BooleanOp.AND;

        // matching operation (all match or at least one match)
        private MatchingOp matchingOp;

        // optionally negate the computed result
        private boolean negateResult;

        private BooleanBindingBuilder parentBuilder;

        public PredicateBuilder(final BooleanBindingBuilder parentBuilder) {
            this.parentBuilder = parentBuilder;
        }

        /**
         * Adds a new predicate with the supplied testables to the chain under construction by
         * {@link BooleanBindingBuilder}
         *
         * @return the boolean binding builder
         */
        public BooleanBindingBuilder finishPredicate() {
            verifyPredicateIsComplete();
            parentBuilder.addPredicate(booleanOp, predicate, observableExtractorFunction, matchingOp, testables,
                    negateResult);
            return parentBuilder;
        }

        @SuppressWarnings("unchecked")
        private void verifyPredicateIsComplete() {
            if (predicate == null) {
                throw new IllegalStateException("No predicate has been supplied");
            }
            if (testables == null || testables.length == 0) {
                throw new IllegalStateException("No testables have been supplied");
            }
            if (observableExtractorFunction == null) {
                // if testables are of instance "Observable", we can set a default observableExtractorFunction
                if (Observable.class.isAssignableFrom(testables[0].getClass())) {
                    observableExtractorFunction = (Function<T, Observable>) DEFAULT_OBSERVABLE_EXTRACTOR_FUNCTION;
                } else {
                    throw new IllegalStateException(
                            "No observableExtractorFunction supplied while testable is not of type javafx.beans.Observable");
                }
            }
            if (matchingOp == null) {
                throw new IllegalStateException("No matchingOp has been supplied");
            }
        }

        public PredicateBuilder<T> predicate(final Predicate<T> predicate) {
            this.predicate = predicate;
            return this;
        }

        public PredicateBuilder<T> testables(final T[] testables) {
            this.testables = testables;
            return this;
        }

        public PredicateBuilder<T> observableExtractorFunction(
                final Function<T, Observable> observableExtractorFunction) {
            this.observableExtractorFunction = observableExtractorFunction;
            return this;
        }

        public PredicateBuilder<T> booleanOp(final BooleanOp booleanOp) {
            this.booleanOp = booleanOp;
            return this;
        }

        public PredicateBuilder<T> matchingOp(final MatchingOp matchingOp) {
            this.matchingOp = matchingOp;
            return this;
        }

        public PredicateBuilder<T> negateResult(final boolean negateResult) {
            this.negateResult = negateResult;
            return this;
        }
    }

    /**
     * Represents a single predicate that can be chained together with other predicates.
     *
     * @author koster
     *
     */
    protected static class PredicateElement<T> {

        // function that is evaluated against all controls in this builder
        protected Predicate<T> predicate;

        // instances to be checked by the predicate
        protected T[] testables;

        // function to retrieve the observable instances related to the testables
        protected Function<T, Observable> observableExtractorFunction;

        // boolean operator that describes how the single boolean bindings in the chain
        // shall be linked with each other
        protected BooleanOp booleanOp = BooleanOp.AND;

        // matching operation (all match or at least one match)
        protected MatchingOp matchingOp;

        // optionally negate the computed result
        protected boolean negateResult;

        public PredicateElement(final BooleanOp booleanOp, final Predicate<T> predicate, final MatchingOp matchingOp,
                final Function<T, Observable> observableExtractorFunction, final T[] testables,
                final boolean negateResult) {
            this.booleanOp = booleanOp;
            this.predicate = predicate;
            this.matchingOp = matchingOp;
            this.observableExtractorFunction = observableExtractorFunction;
            this.testables = testables;
            this.negateResult = negateResult;
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
                result = Arrays.asList(testables).stream().anyMatch(predicate);
                break;
                case ALL_MATCH:
                    result = Arrays.asList(testables).stream().allMatch(predicate);
                    break;
            default:
                result = Arrays.asList(testables).stream().allMatch(predicate);
            }
            return negateResult ? !result : result;
        }

        /**
         * Returns all observables that will trigger a re-evaluation of the predicate.
         *
         * @return observables that trigger the re-evaluation of the predicate
         */
        public List<Observable> getObservables() {
            return Arrays.asList(testables).stream().map(observableExtractorFunction).toList();
        }

        /**
         * The boolean operation that is used to connect this predicate to a potentially previously predicate in the
         * predicate chain.
         *
         * @return the boolean operation
         */
        public BooleanOp getBooleanOp() {
            return booleanOp;
        }
    }

}
