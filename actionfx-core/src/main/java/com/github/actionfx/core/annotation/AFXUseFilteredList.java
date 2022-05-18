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
package com.github.actionfx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

/**
 * Annotation that can be applied at field level on a {@link javafx.scene.control.Control}, so that ActionFX instructs
 * the control to use a {@link javafx.collections.transformation.FilteredList} as items. Please note that the control
 * must support multiple values in form of an {@link ObservableList}.
 * <p>
 * This annotation can be e.g. applied to a {@link javafx.scene.control.TableView} field, so that table view items can
 * be filtered.
 *
 * @author koster
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXUseFilteredList {

    /**
     * Optionally the {@link javafx.collections.transformation.FilteredList} can be additionally wrapped inside a
     * {@link javafx.collections.transformation.SortedList}. Default is however {@code false}.
     *
     * @return flag that indicates whether a {@link javafx.collections.transformation.FilteredList} shall be wrapped
     *         inside a {@link javafx.collections.transformation.SortedList}.
     */
    public boolean wrapInSortedList() default false;

    /**
     * Optional expression that is resolved to an {@link ObservableValue} holding a filter
     * {@link java.util.function.Predicate}, that is set as filter predicate in the
     * {@link javafx.collections.transformation.FilteredList} and is observed for changes.
     *
     * @return an expression that points to a field of type {@link ObservableValue} and holds a
     *         {@link java.util.function.Predicate}.
     */
    public String filterPredicateProperty() default "";
}
