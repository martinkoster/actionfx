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

import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;

import javafx.scene.control.Control;
import net.bytebuddy.implementation.bind.MethodDelegationBinder.BindingResolver;

/**
 * Implementation of a {@link BindingTargetResolver} that looks for controls
 * whose ID is starting with the field name to resolve.
 * <p>
 * For example, a field with name "userName" can be resolved to a control with
 * IDs "userName", "userNameTextField", etc.
 * <p>
 * Please note: This {@link BindingResolver} is not suitable for nested
 * properties inside a Java hierarchy. For nested properties please use
 * {@link MappingBasedBindingTargetResolver}.
 *
 *
 * @author koster
 *
 */
public class NameBasedBindindTargetResolver extends AbstractCachingBindingTargetResolver {

	private final String controlPrefix;

	private final String controlSuffix;

	/**
	 * Default constructor without using a v prefix and suffix.
	 */
	public NameBasedBindindTargetResolver() {
		this("", "");
	}

	/**
	 * Constructor accepting a control prefix or suffix for resolution.
	 * <p>
	 * For example, when a field name is called "userName", then it can be resolved
	 * to a control of name "selectedUserName", when "selected" is set as
	 * {@code controlPrefix}.
	 *
	 * @param controlPrefix a control prefix
	 * @param controlSuffix a control suffix
	 */
	public NameBasedBindindTargetResolver(final String controlPrefix, final String controlSuffix) {
		this.controlPrefix = controlPrefix;
		this.controlSuffix = controlSuffix;
	}

	@Override
	public BindingTarget resolveInternal(final Control control, final Object bean, final View view) {
		final String controlId = control.getId();
		final String fieldName = guessFieldName(controlId);
		// check, if Java bean has a property with this name
		if (ReflectionUtils.findField(bean.getClass(), fieldName) != null) {
			return new BindingTarget(control, bean.getClass(), fieldName);
		} else {
			return null;
		}
	}

	/**
	 * Tries to guess the field name based on the supplied {@code controlId},
	 * removing potential control pre- and suffixes.s
	 *
	 * @param controlId the ID of a control
	 * @return the field name that would be expected to match the given
	 *         {@link controlId}
	 */
	private String guessFieldName(final String controlId) {
		String fieldName = controlId;
		if (controlPrefix != null && !"".equals(controlPrefix) && fieldName.startsWith(controlPrefix)) {
			fieldName = ReflectionUtils.decapitalizeBeanProperty(fieldName.substring(controlPrefix.length()));
		}
		if (controlSuffix != null && !"".equals(controlSuffix) && fieldName.endsWith(controlSuffix)) {
			fieldName = fieldName.substring(0, fieldName.length() - controlSuffix.length());
		}
		return fieldName;
	}
}
