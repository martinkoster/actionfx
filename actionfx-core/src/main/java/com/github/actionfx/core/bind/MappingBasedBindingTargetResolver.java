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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.scene.control.Control;

/**
 * Implementation of a {@link BindingTargetResolver} that uses an internal
 * mapping for field names to control IDs for resolving to a control.
 * <p>
 * In case there is no explicit mapping specified, the control can be looked up
 * by matching the field name.
 *
 * @author koster
 *
 */
public class MappingBasedBindingTargetResolver extends NameBasedBindindTargetResolver {

	private final boolean disableNameBasedMapping;

	private final Map<String, String> fieldToControlMap;

	/**
	 * Constructor accepting a map of field name to control ID mappings.
	 *
	 * @param fieldToControlMap       map containing field name to control ID
	 *                                mappings
	 * @param disableNameBasedMapping If set to {@code true}, all mappings are
	 *                                solely taken from {@code fieldToControlMap}.
	 *                                If set to {@code false}, name-based resolution
	 *                                is applied as fallback.
	 */
	public MappingBasedBindingTargetResolver(final Map<String, String> fieldToControlMap,
			final boolean disableNameBasedMapping) {
		this(fieldToControlMap, disableNameBasedMapping, "", "");
	}

	/**
	 * Constructor accepting a map of field name to control ID mappings, plus a
	 * control prefix or suffix for resolution.
	 *
	 * @param fieldToControlMap       map containing field name to control ID
	 *                                mappings
	 * @param disableNameBasedMapping If set to {@code true}, all mappings are
	 *                                solely taken from {@code fieldToControlMap}.
	 *                                If set to {@code false}, name-based resolution
	 *                                is applied as fallback.
	 * @param controlPrefix           a control prefix
	 * @param controlSuffix           a control suffix
	 */
	public MappingBasedBindingTargetResolver(final Map<String, String> fieldToControlMap,
			final boolean disableNameBasedMapping, final String controlPrefix, final String controlSuffix) {
		super(controlPrefix, controlSuffix);
		this.disableNameBasedMapping = disableNameBasedMapping;
		this.fieldToControlMap = fieldToControlMap;
	}

	@Override
	public Control resolveInternal(final View view, final Field field) {
		final String fieldName = field.getName();
		final String controlId = fieldToControlMap.get(fieldName);
		if (controlId != null) {
			final Optional<NodeWrapper> foundControl = view.getViewNodesAsStream()
					.filter(nodeWrapper -> nodeWrapper.isControl() && nodeWrapper.getId().equalsIgnoreCase(controlId))
					.findFirst();
			if (foundControl.isPresent()) {
				return foundControl.get().getWrapped();
			}
		}
		// mapping did not bring a resolution, shall we try the name based mapping?
		return disableNameBasedMapping ? null : super.resolveInternal(view, field);
	}

}
