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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFXMock;
import com.github.actionfx.core.annotation.AFXFromTextInputDialog;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

/**
 * JUnit test for {@link FromFileSaveDialogParameterResolver}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class FromTextInputDialogParameterResolverTest {

	private FromTextInputDialogParameterResolver resolver;

	private DialogController dialogController;

	private ActionFXMock actionFX;

	@BeforeEach
	void onSetup() {
		resolver = new FromTextInputDialogParameterResolver();
		actionFX = new ActionFXMock();
		dialogController = Mockito.mock(DialogController.class);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		actionFX.addBean(BeanContainerFacade.CONVERSION_SERVICE_BEANNAME, new ConversionService());
	}

	@AfterEach
	void onTearDown() {
		actionFX.reset();
	}

	@Test
	void testResolve() throws IOException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithString");
		final Parameter parameter = method.getParameters()[0];
		final AFXFromTextInputDialog annotation = parameter.getAnnotation(AFXFromTextInputDialog.class);
		when(dialogController.showTextInputDialog(eq("Music File Name"), eq("Enter a music file name"),
				eq("Please enter a file name"), eq("default.wav"))).thenReturn("music.wav");

		// WHEN
		final Object result = resolver.resolve(instance, method, parameter, annotation, parameter.getType());

		// THEN
		assertThat(result).isNotNull().isEqualTo("music.wav");
        assertThat(resolver.continueMethodInvocation()).isTrue();
	}

	@Test
	void testResolve_doNotContinueOnCancel() throws IOException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class,
				"methodWithFileAndNotContinueOnCancel");
		final Parameter parameter = method.getParameters()[0];
		final AFXFromTextInputDialog annotation = parameter.getAnnotation(AFXFromTextInputDialog.class);
		when(dialogController.showTextInputDialog(eq("Music File Name"), eq("Enter a music file name"),
				eq("Please enter a file name"), eq("default.wav"))).thenReturn(null);

		// WHEN
		final Object result = resolver.resolve(instance, method, parameter, annotation, parameter.getType());

		// THEN
		assertThat(result).isNull();
        assertThat(resolver.continueMethodInvocation()).isFalse();
	}

	public class ClassWithMethods {

		public View _view;

		public void methodWithString(
				@AFXFromTextInputDialog(title = "Music File Name", header = "Enter a music file name", content = "Please enter a file name", defaultValue = "default.wav") final String string) {
		}

		public void methodWithFileAndNotContinueOnCancel(
				@AFXFromTextInputDialog(title = "Music File Name", header = "Enter a music file name", content = "Please enter a file name", defaultValue = "default.wav", continueOnCancel = false) final String string) {
		}

	}
}
