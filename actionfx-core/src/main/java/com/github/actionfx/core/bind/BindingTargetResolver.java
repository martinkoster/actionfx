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

import com.github.actionfx.core.view.View;

import javafx.scene.control.Control;

/**
 * Interface for resolving a field into a control that acts as a binding target.
 *
 * @author koster
 *
 */
public interface BindingTargetResolver {

	/**
	 * Resolves the given {@code field} into a {@link Control} whose value is used
	 * as binding target for the field. In case, resolution is not possible,
	 * {@code null} will be returned.
	 *
	 * @param view  the view holing controls
	 * @param field the field to resolve
	 * @return the resolved {@link Control}, or {@code null}, if resolution is not
	 *         possible.
	 */
	Control resolve(View view, Field field);

}
