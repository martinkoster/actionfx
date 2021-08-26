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
package com.github.actionfx.core.bind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlProperties;

import javafx.scene.control.Control;

/**
 * Implementation of a {@link BindingTargetResolver} that uses an internal
 * mapping for field names (also nested fields are supported) to control IDs for
 * resolving to a control.
 * <p>
 * In case there is no explicit mapping specified, the control can be looked up
 * by matching the field name.
 *
 * @author koster
 *
 */
public class MappingBasedBindingTargetResolver extends NameBasedBindindTargetResolver {

	private final boolean disableNameBasedMapping;

	private final Map<String, List<PropertyMapping>> controlToPropertyListMap = new HashMap<>();

	/**
	 * Constructor allowing to disable/enable the name-based mapping as fallback.
	 * <p>
	 * Please note that you need to call
	 * {@link #registerMapping(String, String, ControlProperties)} after instance
	 * creation for registering new mappings in this instance.
	 *
	 * @param disableNameBasedMapping If set to {@code true}, all mappings are
	 *                                solely taken from {@code fieldToControlMap}.
	 *                                If set to {@code false}, name-based resolution
	 *                                is applied as fallback.
	 */
	public MappingBasedBindingTargetResolver(final boolean disableNameBasedMapping) {
		this(disableNameBasedMapping, "", "");
	}

	/**
	 * Constructor allowing to disable/enable the name-based mapping as fallback,
	 * plus a control prefix or suffix for resolution.
	 * <p>
	 * Please note that you need to call
	 * {@link #registerMapping(String, String, ControlProperties)} after instance
	 * creation for registering new mappings in this instance.
	 *
	 * @param controlToFieldMap       map containing the control ID to field
	 *                                mappings (key is the control ID, value the
	 *                                field name or a nested path to a field)
	 * @param disableNameBasedMapping If set to {@code true}, all mappings are
	 *                                solely taken from {@code fieldToControlMap}.
	 *                                If set to {@code false}, name-based resolution
	 *                                is applied as fallback.
	 * @param controlPrefix           a control prefix
	 * @param controlSuffix           a control suffix
	 */
	public MappingBasedBindingTargetResolver(final boolean disableNameBasedMapping, final String controlPrefix,
			final String controlSuffix) {
		super(controlPrefix, controlSuffix);
		this.disableNameBasedMapping = disableNameBasedMapping;
	}

	/**
	 * Registers a new mapping for a given control identified by {@code controlId},
	 * where the {@code controlProperty} of the control shall be bound to the given
	 * {@code propertyName} inside a Java bean.
	 *
	 * @param controlId      the ID of the control
	 * @param targetProperty the property of the control that shall be bound
	 * @param propertyName   the name of the property inside the Java bean, which
	 *                       can be a nested path using the "." operator
	 */
	public void registerMapping(final String controlId, final ControlProperties targetProperty,
			final String propertyName) {
		final List<PropertyMapping> propertyMappings = controlToPropertyListMap.computeIfAbsent(controlId,
				id -> new ArrayList<>());
		propertyMappings.add(new PropertyMapping(propertyName, targetProperty));
	}

	@Override
	public List<BindingTarget> resolveInternal(final Control control, final Object bean, final View view) {
		final List<PropertyMapping> propertyMappings = controlToPropertyListMap.get(control.getId());
		if (propertyMappings == null || propertyMappings.isEmpty()) {
			// mapping did not bring a resolution, shall we try the name based mapping?
			return disableNameBasedMapping ? Collections.emptyList() : super.resolveInternal(control, bean, view);
		}
		return propertyMappings.stream().map(mapping -> new BindingTarget(control, mapping.getTargetProperty(),
				bean.getClass(), mapping.getPropertyName())).collect(Collectors.toList());
	}

	/**
	 * Internal class describing a mapping between a control's property and a Java
	 * bean property.
	 *
	 * @author koster
	 *
	 */
	private static class PropertyMapping {

		private final String propertyName;

		private final ControlProperties targetProperty;

		public PropertyMapping(final String propertyName, final ControlProperties targetProperty) {
			this.propertyName = propertyName;
			this.targetProperty = targetProperty;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public ControlProperties getTargetProperty() {
			return targetProperty;
		}

	}

}
