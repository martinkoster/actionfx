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

import java.util.Map;

import com.github.actionfx.core.view.View;

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

	private final Map<String, String> controlToFieldMap;

	/**
	 * Constructor accepting a map of field name to control ID mappings.
	 *
	 * @param controlToFieldMap       map containing the control ID to field
	 *                                mappings (key is the control ID, value the
	 *                                field name or a nested path to a field)
	 * @param disableNameBasedMapping If set to {@code true}, all mappings are
	 *                                solely taken from {@code fieldToControlMap}.
	 *                                If set to {@code false}, name-based resolution
	 *                                is applied as fallback.
	 */
	public MappingBasedBindingTargetResolver(final Map<String, String> controlToFieldMap,
			final boolean disableNameBasedMapping) {
		this(controlToFieldMap, disableNameBasedMapping, "", "");
	}

	/**
	 * Constructor accepting a map of field name to control ID mappings, plus a
	 * control prefix or suffix for resolution.
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
	public MappingBasedBindingTargetResolver(final Map<String, String> controlToFieldMap,
			final boolean disableNameBasedMapping, final String controlPrefix, final String controlSuffix) {
		super(controlPrefix, controlSuffix);
		this.disableNameBasedMapping = disableNameBasedMapping;
		this.controlToFieldMap = controlToFieldMap;
	}

	@Override
	public BindingTarget resolveInternal(final Control control, final Object bean, final View view) {
		final String fieldPath = controlToFieldMap.get(control.getId());
		if (fieldPath != null) {
			return new BindingTarget(control, bean.getClass(), fieldPath);
		}
		// mapping did not bring a resolution, shall we try the name based mapping?
		return disableNameBasedMapping ? null : super.resolveInternal(control, bean, view);
	}

}
