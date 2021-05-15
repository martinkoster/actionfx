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
package com.github.actionfx.core.method;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ResourceBundle;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXFromTextInputDialog;
import com.github.actionfx.core.utils.MessageUtils;

/**
 * Parameter resolver implementation that resolves a method argument by taking
 * its value from a "text input" dialog.
 *
 * @author koster
 *
 */
public class FromTextInputDialogParameterResolver implements AnnotatedParameterResolver<AFXFromTextInputDialog> {

	private boolean continueMethodInvocation = true;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T resolve(final Object controller, final Method method, final Parameter parameter,
			final AFXFromTextInputDialog annotation, final Class<T> expectedType) {
		final ActionFX actionFX = ActionFX.getInstance();
		final ResourceBundle resourceBundle = actionFX.getControllerResourceBundle(controller.getClass());
		final String text = actionFX.showTextInputDialog(
				MessageUtils.getMessage(resourceBundle, annotation.titleKey(), annotation.title()),
				MessageUtils.getMessage(resourceBundle, annotation.headerKey(), annotation.header()),
				MessageUtils.getMessage(resourceBundle, annotation.contentKey(), annotation.content()),
				annotation.defaultValue());
		if (text == null && !annotation.continueOnCancel()) {
			// user cancelled selection and annotation tells not to continue
			continueMethodInvocation = false;
		}
		return (T) actionFX.getConversionService().convert(text, parameter.getType());
	}

	@Override
	public boolean continueMethodInvocation() {
		return continueMethodInvocation;
	}
}
