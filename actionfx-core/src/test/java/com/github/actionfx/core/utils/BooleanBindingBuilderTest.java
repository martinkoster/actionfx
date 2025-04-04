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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.annotation.BooleanOp;
import com.github.actionfx.core.utils.BooleanBindingBuilder.PredicateBuilder;
import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link BooleanBindingBuilder}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class BooleanBindingBuilderTest {

    private TextField textField1;

    private TextField textField2;

    private TextField textField3;

    private TextField textField4;

    private final Predicate<ControlWrapper> hasValuePredicateForControlWrapper = ControlWrapper::hasUserValueSet;

    @SuppressWarnings("unchecked")
    private final Predicate<Observable> hasValuePredicateForObservable = obs -> !StringUtils
            .isEmpty(((ObservableValue<String>) obs).getValue());

    @BeforeEach
    void onSetup() {
        textField1 = new TextField();
        textField2 = new TextField();
        textField3 = new TextField();
        textField4 = new TextField();
    }

    @Test
    void testBuild_allFulfilled_withControlWrapper_and_observableExtractorFunction() {
        // GIVEN

        // WHEN
        final BooleanBinding binding = BooleanBindingBuilder.builder()
                .andAllTestablesFulfillPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate().build();

		// THEN (no value set, so binding is false)
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and WHEN
        textField1.setText("Hello");
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and THEN (now all fields in the binding have a value)
        textField2.setText("World");
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testBuild_allFulfilled_withObservable_noObservableExtractorFunctionRequired() {
        // GIVEN

        // WHEN
        final BooleanBinding binding = BooleanBindingBuilder.builder()
                .andAllTestablesFulfillPredicate(hasValuePredicateForObservable)
                .testables(new Observable[] { textField1.textProperty(), textField2.textProperty() })
                .finishPredicate().build();

		// THEN (no value set, so binding is false)
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and WHEN
        textField1.setText("Hello");
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and THEN (now all fields in the binding have a value)
        textField2.setText("World");
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testBuild_atLeastOneFulfilled() {
        // GIVEN

        // WHEN
        final BooleanBinding binding = BooleanBindingBuilder.builder()
                .andAtLeastOneTestableFulfillsPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate().build();

		// THEN (no value set, so binding is false)
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and WHEN
        textField1.setText("Hello");

		// and THEN (at least one value is supplied)
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testBuild_atLeastOneFulfilled_negated() {
        // GIVEN

        // WHEN
        final BooleanBinding binding = BooleanBindingBuilder.builder()
                .andAtLeastOneTestableFulfillsPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).negateResult(true)
                .finishPredicate().build();

		// THEN (no value set, value is negated, so binding is true)
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE);

        // and WHEN
        textField1.setText("Hello");

		// and THEN (at least one value is supplied, value is negated, so binding is
		// false)
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);
    }

    @Test
    void testBuild_predicatesLinkedWith_AND() {
        // GIVEN

        // WHEN
        final BooleanBinding binding = BooleanBindingBuilder.builder()
                // PREDICATE 1
                .andAtLeastOneTestableFulfillsPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate().
                // PREDICATE 2
                andAllTestablesFulfillPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField3), ControlWrapper.of(textField4) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate()
                .build();

		// THEN (no predicate fulfilled)
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and WHEN (predicate 1 is fulfilled)
        textField1.setText("Hello");

		// and THEN (value is still false, as predicate 2 is not yet fulfilled)
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and WHEN (we are going to fulfill predicate 2 step-by-step)
        textField3.setText("World");
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE); // all needs to be fulfilled,so still false
        textField4.setText("Whazzuuuup");
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE); // predicate 2 is now fulfilled
    }

    @Test
    void testBuild_predicatesLinkedWith_AND_with_negateAll() {
        // GIVEN

        // WHEN
        final BooleanBinding binding = BooleanBindingBuilder.builder()
                // PREDICATE 1
                .andAtLeastOneTestableFulfillsPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate().
                // PREDICATE 2
                andAllTestablesFulfillPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField3), ControlWrapper.of(textField4) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate()
                .negateChainResult(true)
                .build();

		// THEN (no predicate fulfilled, but negated)
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE);

        // and WHEN (predicate 1 is fulfilled, but negated)
        textField1.setText("Hello");

		// and THEN (value is still true, as predicate 2 is not yet fulfilled)
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE);

        // and WHEN (we are going to fulfill predicate 2 step-by-step)
        textField3.setText("World");
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE); // all needs to be fulfilled,so still true because
                                                               // negated
        textField4.setText("Whazzuuuup");
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE); // predicate 2 is now fulfilled, but negated
                                                                // -> so "false"
    }

    @Test
    void testBuild_predicatesLinkedWith_OR_firstPredicateIsFulfilled() {
        // GIVEN

        // WHEN
        final BooleanBinding binding = BooleanBindingBuilder.builder()
                // PREDICATE 1
                .andAtLeastOneTestableFulfillsPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate().
                // PREDICATE 2
                orAllTestablesFulfillPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField3), ControlWrapper.of(textField4) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate()
                .build();

		// THEN (no predicate fulfilled)
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and WHEN (predicate 1 is fulfilled)
        textField1.setText("Hello");

		// and THEN (value is true, as predicate 2 is ORed with predicate 1)
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testBuild_predicatesLinkedWith_OR_secondPredicateIsFulfilled() {
        // GIVEN

        // WHEN
        final BooleanBinding binding = BooleanBindingBuilder.builder()
                // PREDICATE 1
                .andAtLeastOneTestableFulfillsPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate().
                // PREDICATE 2
                orAllTestablesFulfillPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField3), ControlWrapper.of(textField4) })
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable).finishPredicate()
                .build();

		// THEN (no predicate fulfilled)
		assertThat(binding.getValue()).isEqualTo(Boolean.FALSE);

        // and WHEN (predicate 2 is fulfilled)
        textField3.setText("Hello");
        textField4.setText("World");

		// and THEN (value is true, as predicate 2 is ORed with predicate 1)
		assertThat(binding.getValue()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testBuild_allFulfilled_noPredicateSupplied() {
        // GIVEN

        // WHEN
        final PredicateBuilder<Observable> builder = BooleanBindingBuilder.builder()
                .andAllTestablesFulfillPredicate((Predicate<Observable>) null)
                .testables(new Observable[] { textField1.textProperty(), textField2.textProperty() });

        // THEN (no value set, so binding is false)
        final IllegalStateException ex = assertThrows(IllegalStateException.class, builder::finishPredicate);
		assertThat(ex.getMessage()).isEqualTo("No predicate has been supplied");
    }

    @Test
    void testBuild_allFulfilled_noTestableSupplied_testableIsNull() {
        // GIVEN

        // WHEN
        final PredicateBuilder<ControlWrapper> builder = BooleanBindingBuilder.builder()
                .andAllTestablesFulfillPredicate(hasValuePredicateForControlWrapper)
                .testables(null);

        // THEN (no value set, so binding is false)
        final IllegalStateException ex = assertThrows(IllegalStateException.class, builder::finishPredicate);
		assertThat(ex.getMessage()).isEqualTo("No testables have been supplied");
    }

    @Test
    void testBuild_allFulfilled_noTestableSupplied_testableIsEmptyArray() {
        // GIVEN

        // WHEN
        final PredicateBuilder<ControlWrapper> builder = BooleanBindingBuilder.builder()
                .andAllTestablesFulfillPredicate(hasValuePredicateForControlWrapper)
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
                .testables(new ControlWrapper[0]);

        // THEN (no value set, so binding is false)
        final IllegalStateException ex = assertThrows(IllegalStateException.class, builder::finishPredicate);
		assertThat(ex.getMessage()).isEqualTo("No testables have been supplied");
    }

    @Test
    void testBuild_allFulfilled_noObservableExtractorFunctionSupplied_andTestablesAreNotOfTypeObservable() {
        // GIVEN

        // WHEN
        final PredicateBuilder<ControlWrapper> builder = BooleanBindingBuilder.builder()
                .andAllTestablesFulfillPredicate(hasValuePredicateForControlWrapper)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) });

        // THEN (no value set, so binding is false)
        final IllegalStateException ex = assertThrows(IllegalStateException.class, builder::finishPredicate);
		assertThat(ex.getMessage()).isEqualTo("No observableExtractorFunction supplied while testable is not of type javafx.beans.Observable");
    }

    @Test
    void testBuild_allFulfilled_noMatchingOpSupplied() {
        // GIVEN

        // WHEN
        final PredicateBuilder<ControlWrapper> builder = BooleanBindingBuilder.builder()
                .forPredicate(hasValuePredicateForControlWrapper).booleanOp(BooleanOp.AND)
                .observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
                .testables(new ControlWrapper[] { ControlWrapper.of(textField1), ControlWrapper.of(textField2) });

        // THEN (no value set, so binding is false)
        final IllegalStateException ex = assertThrows(IllegalStateException.class, builder::finishPredicate);
		assertThat(ex.getMessage()).isEqualTo("No matchingOp has been supplied");
    }
}
