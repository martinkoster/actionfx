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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.scene.control.Control;

/**
 * Implementation of a {@link BindingTargetResolver} that looks for controls
 * whose ID is starting with the field name to resolve.
 * <p>
 * For example, a field with name "userName" can be resolved to a control with
 * IDs "userName", "userNameTextField", etc.
 *
 *
 * @author koster
 *
 */
public class NameBasedBindindTargetResolver extends AbstractCachingBindingTargetResolver {

	private static final Logger LOG = LoggerFactory.getLogger(NameBasedBindindTargetResolver.class);

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
	public Control resolveInternal(final View view, final Field field) {
		final String fieldName = field.getName();
		final Optional<NodeWrapper> foundControl = view.getViewNodesAsStream()
				.filter(nodeWrapper -> nodeWrapper.isControl()
						&& nodeWrapper.getId().toLowerCase().startsWith(controlNameToken(fieldName)))
				.findFirst();
		if (foundControl.isEmpty()) {
			return null;
		} else {
			return foundControl.get().getWrapped();
		}
	}

	/**
	 * Generates the expected control name token that is used for matching.
	 *
	 * @param fieldName the field name
	 * @return the control name used for matching
	 */
	private String controlNameToken(final String fieldName) {
		return (controlPrefix + fieldName + controlSuffix).toLowerCase();
	}

}
