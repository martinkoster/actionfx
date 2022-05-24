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
package com.github.actionfx.core.extension.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.actionfx.core.beans.BeanWrapper;
import com.github.actionfx.core.extension.AbstractAnnotationBasedExtension;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;

/**
 * Base class for controller extensions that apply a logic to controller fields and methods that are marked with
 * annotations.
 *
 * @param <A>
 *            the annotation type
 * @param <E>
 *            the type of annotated element, e.g. a field or method.
 *
 * @author koster
 *
 */
public abstract class AbstractAnnotationBasedControllerExtension<A extends Annotation, E extends AnnotatedElement>
        extends AbstractAnnotationBasedExtension<A, E> implements Consumer<Object> {

    protected AbstractAnnotationBasedControllerExtension(final Class<A> annotationType) {
        super(annotationType);
    }

    /**
     * This method is the entry to a controller extension that aims at extending the given {@code controller} instance.
     *
     * @param controller
     *            the controller instance to extend
     */
    @Override
    public void accept(final Object controller) {
        final Map<E, List<A>> annotatedElementsMap = lookupAnnotatedElements(controller.getClass());
        for (final var entry : annotatedElementsMap.entrySet()) {
            for (final var annotation : entry.getValue()) {
                extend(controller, entry.getKey(), annotation);
            }
        }
    }

    /**
     * The actual extension logic is implemented in this method, which receives the controller instance, as well as the
     * annotated element and one annotation.
     *
     * @param controller
     *            the controller instance
     * @param annotatedElement
     *            the annotated element (e.g. field or method)
     * @param annotation
     *            the annotation applied to the annotated element
     */
    protected abstract void extend(Object controller, final E annotatedElement, final A annotation);

    /**
     * Creates a {@link ControlWrapper} for a control identified by {@code controlId}. The control with the given ID is
     * supposed to by inside the given {@code view}.
     *
     * @param view
     *            the view containing the control with id {@code controlId}
     * @param controlId
     *            the control ID for that the wrapper shall be created
     * @return the control wrapper instance
     */
    protected ControlWrapper createControlWrapper(final View view, final String controlId) {
        final NodeWrapper wrappedTargetNode = createNodeWrapper(controlId, view);
        if (!Control.class.isAssignableFrom(wrappedTargetNode.getWrappedType())) {
            throw new IllegalStateException(
                    "Node with id='" + controlId + "' is not an instance of javafx.scene.control.Control!");
        }
        return ControlWrapper.of(wrappedTargetNode.getWrapped());
    }

    /**
     * Creates an array of {@link ControlWrapper} for controls identified by {@code controlIds}. The controls with the
     * given ID are supposed to by inside the given {@code view}.
     *
     * @param view
     *            the view containing the control with id {@code controlId}
     * @param controlIds
     *            the control IDs for that the wrapper shall be created
     * @return the control wrapper instance
     */
    protected ControlWrapper[] createControlWrapper(final View view, final String... controlIds) {
        return Arrays.stream(controlIds).map(controlId -> createControlWrapper(view, controlId))
                .toArray(size -> new ControlWrapper[size]);
    }

    /**
     * Creates a {@link NodeWrapper} for a node identified by {@code nodeId}. The node with the given ID is supposed to
     * by inside the given {@code view}.
     *
     * @param nodeId
     *            the node ID for that the wrapper shall be created
     * @param view
     *            the view containing the node with id {@code nodeId}
     * @return the control wrapper instance
     */
    protected NodeWrapper createNodeWrapper(final String nodeId, final View view) {
        final NodeWrapper wrappedTargetNode = view.lookupNode(nodeId);
        if (wrappedTargetNode == null) {
            throw new IllegalStateException(
                    "Node with id='" + nodeId + "' does not exist in view with ID='" + view.getId() + "'!");
        }
        return wrappedTargetNode;
    }

    /**
     * Looks up a {@link ObservableValue} inside the given {@code instance}.
     *
     * @param instance
     *            the instance
     * @param propertyPath
     *            the property path (potentially nested) pointing to a {@link ObservableValue}
     * @return the looked-up observable value, or {@code null}, if the property can not be looked up
     */
    @SuppressWarnings("unchecked")
    protected <T extends ObservableValue<?>> T lookupObservableValue(final Object instance, final String propertyPath,
            final Class<T> expectedType) {
        if (!"".equals(propertyPath)) {
            final Object fieldValue = BeanWrapper.of(instance).getPropertyValue(propertyPath);
            if (fieldValue != null && !expectedType.isAssignableFrom(fieldValue.getClass())) {
                throw new IllegalArgumentException(
                        "Property '" + propertyPath + "' in class '" + instance.getClass().getCanonicalName()
                                + "' is not of expected type '" + expectedType.getCanonicalName() + "'!");
            }
            return (T) fieldValue;
        }
        return null;
    }

}
