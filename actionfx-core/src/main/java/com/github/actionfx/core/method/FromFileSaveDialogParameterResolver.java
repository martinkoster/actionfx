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

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXFromFileSaveDialog;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.utils.MessageUtils;
import com.github.actionfx.core.view.View;

import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Parameter resolver implementation that resolves a method argument by taking
 * its value from a "file save" dialog.
 *
 * @author koster
 *
 */
public class FromFileSaveDialogParameterResolver implements AnnotatedParameterResolver<AFXFromFileSaveDialog> {

	private boolean continueMethodInvocation = true;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T resolve(final Object controller, final Method method, final Parameter parameter,
			final AFXFromFileSaveDialog annotation, final Class<T> expectedType) {
		final ActionFX actionFX = ActionFX.getInstance();
		final ResourceBundle resourceBundle = actionFX.getControllerResourceBundle(controller.getClass());
		final List<String> extensionList = Arrays.asList(annotation.extensionFilter());
		final ExtensionFilter extensionFilter = extensionList.size() > 1
				? new ExtensionFilter(extensionList.get(0), extensionList.subList(1, extensionList.size()))
				: null;
		final View view = ControllerWrapper.of(controller).getView();
		final File selectedFile = actionFX.showFileSaveDialog(
				MessageUtils.getMessage(resourceBundle, annotation.titleKey(), annotation.title()), null, null,
				extensionFilter, view.getWindow());
		if (selectedFile == null && !annotation.continueOnCancel()) {
			// user cancelled selection and annotation tells not to continue
			continueMethodInvocation = false;
		}
		return (T) actionFX.getConversionService().convert(selectedFile, parameter.getType());
	}

	@Override
	public boolean continueMethodInvocation() {
		return continueMethodInvocation;
	}
}
