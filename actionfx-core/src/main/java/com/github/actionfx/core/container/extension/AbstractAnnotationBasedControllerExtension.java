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
package com.github.actionfx.core.container.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Control;

/**
 * Base class for controller extensions that apply a logic to controller fields
 * and methods that are marked with annotations.
 *
 * @param <T> the type of annotated element, e.g. a field or method.
 *
 * @author koster
 *
 */
public abstract class AbstractAnnotationBasedControllerExtension<A extends Annotation, E extends AnnotatedElement>
		implements Consumer<Object> {

	protected final Class<A> annotationType;

	protected AbstractAnnotationBasedControllerExtension(final Class<A> annotationType) {
		this.annotationType = annotationType;
	}

	/**
	 * This method is the entry to a controller extension that aims at extending the
	 * given {@code controller} instance.
	 *
	 * @param controller the controller instance to extend
	 */
	@Override
	public void accept(final Object controller) {
		final Map<E, List<A>> annotatedElementsMap = lookupAnnotatedElements(controller);
		for (final var entry : annotatedElementsMap.entrySet()) {
			for (final var annotation : entry.getValue()) {
				extend(controller, entry.getKey(), annotation);
			}
		}
	}

	/**
	 * Overriding methods need to return a map, where the key is the annotated
	 * element (e.g. field, method) and the value is a list of (potentially
	 * repeatable) annotations. In case the annotation is not repeatable, than e
	 * list with size 1 is returned, containing the single annotation.
	 *
	 * @param controller the controller instance to extend
	 * @return the map of annotated elements and their corresponding annotations
	 *         (potentially repeatable)
	 */
	protected abstract Map<E, List<A>> lookupAnnotatedElements(Object controller);

	/**
	 * The actual extension logic is implemented in this method, which receives the
	 * controller instance, as well as the annotated element and one annotation.
	 *
	 * @param controller       the controller instance
	 * @param annotatedElement the annotated element (e.g. field or method)
	 * @param annotation       the annotation applied to the annotated element
	 */
	protected abstract void extend(Object controller, final E annotatedElement, final A annotation);

	/**
	 * Creates a {@link ControlWrapper} for a control identified by
	 * {@code controlId}. The control with the given ID is supposed to by inside the
	 * given {@code view}.
	 *
	 * @param controlId the control ID for that the wrapper shall be created
	 * @param view      the view containing the control with id {@code controlId}
	 * @return the control wrapper instance
	 */
	protected ControlWrapper createControlWrapper(final String controlId, final View view) {
		final NodeWrapper wrappedTargetNode = createNodeWrapper(controlId, view);
		if (!Control.class.isAssignableFrom(wrappedTargetNode.getWrappedType())) {
			throw new IllegalStateException(
					"Node with id='" + controlId + "' is not an instance of javafx.scene.control.Control!");
		}
		return ControlWrapper.of(wrappedTargetNode.getWrapped());
	}

	/**
	 * Creates a {@link NodeWrapper} for a node identified by {@code nodeId}. The
	 * node with the given ID is supposed to by inside the given {@code view}.
	 *
	 * @param nodeId the node ID for that the wrapper shall be created
	 * @param view   the view containing the node with id {@code nodeId}
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
	 * Looks up a {@link BooleanProperty} inside the given {@code instance}.
	 *
	 * @param instance            the instance
	 * @param booleanPropertyPath the property path (potentially nested) pointing to
	 *                            a {@link BooleanProperty}
	 * @return the looked-up boolean property, or {@code null}, if the property can
	 *         not be looked up
	 */
	protected BooleanProperty lookupBooleanProperty(final Object instance, final String booleanPropertyPath) {
		BooleanProperty booleanProperty = null;
		if (!"".equals(booleanPropertyPath)) {
			booleanProperty = (BooleanProperty) ReflectionUtils.getNestedFieldValue(booleanPropertyPath, instance);
		}
		return booleanProperty;
	}
}
