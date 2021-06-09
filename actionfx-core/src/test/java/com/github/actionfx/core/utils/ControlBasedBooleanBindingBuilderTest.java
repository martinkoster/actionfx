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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link ControlBasedBooleanBindingBuilder}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ControlBasedBooleanBindingBuilderTest {

	private TextField textField1;

	private TextField textField2;

	private TextField textField3;

	private TextField textField4;

	private final Predicate<ControlWrapper> hasValuePredicate = ControlWrapper::hasUserValueSet;

	@BeforeEach
	void onSetup() {
		textField1 = new TextField();
		textField2 = new TextField();
		textField3 = new TextField();
		textField4 = new TextField();
	}

	@Test
	void testBuild_allFulfilled() {
		// GIVEN

		// WHEN
		final BooleanBinding binding = ControlBasedBooleanBindingBuilder.create().forControl(textField1, textField2)
				.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.allFulfilledFunction(hasValuePredicate).build();

		// THEN (no value set, so binding is false)
		assertThat(binding.getValue(), equalTo(Boolean.FALSE));

		// and WHEN
		textField1.setText("Hello");
		assertThat(binding.getValue(), equalTo(Boolean.FALSE));

		// and THEN (now all fields in the binding have a value)
		textField2.setText("World");
		assertThat(binding.getValue(), equalTo(Boolean.TRUE));
	}

	@Test
	void testBuild_atLeastOneFulfilled() {
		// GIVEN

		// WHEN
		final BooleanBinding binding = ControlBasedBooleanBindingBuilder.create().forControl(textField1, textField2)
				.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.atLeastOneFulfilledFunction(hasValuePredicate).build();

		// THEN (no value set, so binding is false)
		assertThat(binding.getValue(), equalTo(Boolean.FALSE));

		// and WHEN
		textField1.setText("Hello");

		// and THEN (at least one value is supplied)
		assertThat(binding.getValue(), equalTo(Boolean.TRUE));
	}

	@Test
	void testBuild_atLeastOneFulfilled_negated() {
		// GIVEN

		// WHEN
		final BooleanBinding binding = ControlBasedBooleanBindingBuilder.create().forControl(textField1, textField2)
				.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.atLeastOneFulfilledFunction(hasValuePredicate).negate().build();

		// THEN (no value set, value is negated, so binding is true)
		assertThat(binding.getValue(), equalTo(Boolean.TRUE));

		// and WHEN
		textField1.setText("Hello");

		// and THEN (at least one value is supplied, value is negated, so binding is
		// false)
		assertThat(binding.getValue(), equalTo(Boolean.FALSE));
	}

	@Test
	void testBuild_predicatesLinkedWith_AND() {
		// GIVEN

		// WHEN
		final BooleanBinding binding = ControlBasedBooleanBindingBuilder.create().
		// PREDICATE 1
				forControl(textField1, textField2).observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.atLeastOneFulfilledFunction(hasValuePredicate).
				// PREDICATE 2
				and().forControl(textField3, textField4)
				.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.allFulfilledFunction(hasValuePredicate).build();

		// THEN (no predicate fulfilled)
		assertThat(binding.getValue(), equalTo(Boolean.FALSE));

		// and WHEN (predicate 1 is fulfilled)
		textField1.setText("Hello");

		// and THEN (value is still false, as predicate 2 is not yet fulfilled)
		assertThat(binding.getValue(), equalTo(Boolean.FALSE));

		// and WHEN (we are going to fulfill predicate 2 step-by-step)
		textField3.setText("World");
		assertThat(binding.getValue(), equalTo(Boolean.FALSE)); // all needs to be fulfilled,so still false
		textField4.setText("Whazzuuuup");
		assertThat(binding.getValue(), equalTo(Boolean.TRUE)); // predicate 2 is now fulfilled
	}

	@Test
	void testBuild_predicatesLinkedWith_AND_with_negateAll() {
		// GIVEN

		// WHEN
		final BooleanBinding binding = ControlBasedBooleanBindingBuilder.create().
		// PREDICATE 1
				forControl(textField1, textField2).observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.atLeastOneFulfilledFunction(hasValuePredicate).
				// PREDICATE 2
				and().forControl(textField3, textField4)
				.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.allFulfilledFunction(hasValuePredicate).negateAll().build();

		// THEN (no predicate fulfilled, but negated)
		assertThat(binding.getValue(), equalTo(Boolean.TRUE));

		// and WHEN (predicate 1 is fulfilled, but negated)
		textField1.setText("Hello");

		// and THEN (value is still true, as predicate 2 is not yet fulfilled)
		assertThat(binding.getValue(), equalTo(Boolean.TRUE));

		// and WHEN (we are going to fulfill predicate 2 step-by-step)
		textField3.setText("World");
		assertThat(binding.getValue(), equalTo(Boolean.TRUE)); // all needs to be fulfilled,so still true because
																// negated
		textField4.setText("Whazzuuuup");
		assertThat(binding.getValue(), equalTo(Boolean.FALSE)); // predicate 2 is now fulfilled, but negated
																// -> so "false"
	}

	@Test
	void testBuild_predicatesLinkedWith_OR_firstPredicateIsFulfilled() {
		// GIVEN

		// WHEN
		final BooleanBinding binding = ControlBasedBooleanBindingBuilder.create().
		// PREDICATE 1
				forControl(textField1, textField2).observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.atLeastOneFulfilledFunction(hasValuePredicate).
				// PREDICATE 2
				or().forControl(textField3, textField4)
				.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.allFulfilledFunction(hasValuePredicate).build();

		// THEN (no predicate fulfilled)
		assertThat(binding.getValue(), equalTo(Boolean.FALSE));

		// and WHEN (predicate 1 is fulfilled)
		textField1.setText("Hello");

		// and THEN (value is true, as predicate 2 is ORed with predicate 1)
		assertThat(binding.getValue(), equalTo(Boolean.TRUE));
	}

	@Test
	void testBuild_predicatesLinkedWith_OR_secondPredicateIsFulfilled() {
		// GIVEN

		// WHEN
		final BooleanBinding binding = ControlBasedBooleanBindingBuilder.create().
		// PREDICATE 1
				forControl(textField1, textField2).observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.atLeastOneFulfilledFunction(hasValuePredicate).
				// PREDICATE 2
				or().forControl(textField3, textField4)
				.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.allFulfilledFunction(hasValuePredicate).build();

		// THEN (no predicate fulfilled)
		assertThat(binding.getValue(), equalTo(Boolean.FALSE));

		// and WHEN (predicate 2 is fulfilled)
		textField3.setText("Hello");
		textField4.setText("World");

		// and THEN (value is true, as predicate 2 is ORed with predicate 1)
		assertThat(binding.getValue(), equalTo(Boolean.TRUE));
	}

	@Test
	void testBuild_callTo_allFulfilledFunction_then_atLeastOneFulfilledFunction() {
		// GIVEN
		final ControlBasedBooleanBindingBuilder builder = ControlBasedBooleanBindingBuilder.create()
				.forControl(textField1).observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.allFulfilledFunction(hasValuePredicate);

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> builder.atLeastOneFulfilledFunction(hasValuePredicate));

		// THEN
		assertThat(ex.getMessage(), containsString("A predicate has been already set"));
	}

	@Test
	void testBuild_callTo_atLeastOneFulfilledFunction_then_allFulfilledFunction() {
		// GIVEN
		final ControlBasedBooleanBindingBuilder builder = ControlBasedBooleanBindingBuilder.create()
				.forControl(textField1).observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.atLeastOneFulfilledFunction(hasValuePredicate);

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> builder.allFulfilledFunction(hasValuePredicate));

		// THEN
		assertThat(ex.getMessage(), containsString("A predicate has been already set"));
	}

	@Test
	void testBuild_noObservableExtractorFunctionSpecified() {
		// GIVEN
		final ControlBasedBooleanBindingBuilder builder = ControlBasedBooleanBindingBuilder.create()
				.forControl(textField1).atLeastOneFulfilledFunction(hasValuePredicate);

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> builder.build());

		// THEN
		assertThat(ex.getMessage(), containsString("'observableExtractorFunction' must be specified!"));
	}

	@Test
	void testBuild_noPredicateSpecified() {
		// GIVEN
		final ControlBasedBooleanBindingBuilder builder = ControlBasedBooleanBindingBuilder.create()
				.forControl(textField1).observableExtractorFunction(ControlWrapper::getUserValueAsObservable);

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> builder.build());

		// THEN
		assertThat(ex.getMessage(), containsString("A predicate needs to be defined"));
	}

	@Test
	void testBuild_noControlSpecified() {
		// GIVEN
		final ControlBasedBooleanBindingBuilder builder = ControlBasedBooleanBindingBuilder.create()
				.observableExtractorFunction(ControlWrapper::getUserValueAsObservable)
				.atLeastOneFulfilledFunction(hasValuePredicate);
		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> builder.build());

		// THEN
		assertThat(ex.getMessage(), containsString("At least one control needs to be added"));
	}
}
