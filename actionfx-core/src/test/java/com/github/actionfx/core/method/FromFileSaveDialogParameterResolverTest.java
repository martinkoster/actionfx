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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFXMock;
import com.github.actionfx.core.annotation.AFXFromFileSaveDialog;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.layout.HBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * JUnit test for {@link FromFileSaveDialogParameterResolver}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class FromFileSaveDialogParameterResolverTest {

	private FromFileSaveDialogParameterResolver resolver;

	private DialogController dialogController;

	private ActionFXMock actionFX;

	private Window owner;

	@BeforeEach
	void onSetup() {
		resolver = new FromFileSaveDialogParameterResolver();
		actionFX = new ActionFXMock();
		dialogController = Mockito.mock(DialogController.class);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		actionFX.addBean(BeanContainerFacade.CONVERSION_SERVICE_BEANNAME, new ConversionService());
		owner = Mockito.mock(Window.class);
	}

	@AfterEach
	void onTearDown() {
		actionFX.reset();
	}

	@Test
	void testResolve_targetTypeFile() throws IOException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithFile");
		final Parameter parameter = method.getParameters()[0];
		final AFXFromFileSaveDialog annotation = parameter.getAnnotation(AFXFromFileSaveDialog.class);
		final File file = Files.createTempFile("junit", "-tmp").toFile();
		when(dialogController.showFileSaveDialog(anyString(), isNull(), isNull(), any(), eq(owner))).thenReturn(file);

		// WHEN
		final Object result = resolver.resolve(instance, method, parameter, annotation, parameter.getType());

		// THEN
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(File.class));
		assertThat(result, equalTo(file));
		assertThat(resolver.continueMethodInvocation(), equalTo(true));
		final ArgumentCaptor<ExtensionFilter> filterCaptor = ArgumentCaptor.forClass(ExtensionFilter.class);
		verify(dialogController, times(1)).showFileSaveDialog(eq("Save Music File"), isNull(), isNull(),
				filterCaptor.capture(), eq(owner));
		final ExtensionFilter filter = filterCaptor.getValue();
		assertThat(filter.getDescription(), equalTo("Music Files"));
		assertThat(filter.getExtensions(), contains("*.mp3", "*.wav"));
	}

	@Test
	void testResolve_targetTypePath() throws IOException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithPath");
		final Parameter parameter = method.getParameters()[0];
		final AFXFromFileSaveDialog annotation = parameter.getAnnotation(AFXFromFileSaveDialog.class);
		final File file = Files.createTempFile("junit", "-tmp").toFile();
		when(dialogController.showFileSaveDialog(anyString(), isNull(), isNull(), any(), eq(owner))).thenReturn(file);

		// WHEN
		final Object result = resolver.resolve(instance, method, parameter, annotation, parameter.getType());

		// THEN
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(Path.class));
		assertThat(result, equalTo(file.toPath()));
		assertThat(resolver.continueMethodInvocation(), equalTo(true));
	}

	@Test
	void testResolve_targetTypeURI() throws IOException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithURI");
		final Parameter parameter = method.getParameters()[0];
		final AFXFromFileSaveDialog annotation = parameter.getAnnotation(AFXFromFileSaveDialog.class);
		final File file = Files.createTempFile("junit", "-tmp").toFile();
		when(dialogController.showFileSaveDialog(anyString(), isNull(), isNull(), any(), eq(owner))).thenReturn(file);

		// WHEN
		final Object result = resolver.resolve(instance, method, parameter, annotation, parameter.getType());

		// THEN
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(URI.class));
		assertThat(result, equalTo(file.toURI()));
		assertThat(resolver.continueMethodInvocation(), equalTo(true));
	}

	@Test
	void testResolve_targetTypeString() throws IOException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithString");
		final Parameter parameter = method.getParameters()[0];
		final AFXFromFileSaveDialog annotation = parameter.getAnnotation(AFXFromFileSaveDialog.class);
		final File file = Files.createTempFile("junit", "-tmp").toFile();
		when(dialogController.showFileSaveDialog(anyString(), isNull(), isNull(), any(), eq(owner))).thenReturn(file);

		// WHEN
		final Object result = resolver.resolve(instance, method, parameter, annotation, parameter.getType());

		// THEN
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(String.class));
		assertThat(result, equalTo(file.getAbsolutePath()));
		assertThat(resolver.continueMethodInvocation(), equalTo(true));
	}

	@Test
	void testResolve_doNotContinueOnCancel() throws IOException {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class,
				"methodWithFileAndNotContinueOnCancel");
		final Parameter parameter = method.getParameters()[0];
		final AFXFromFileSaveDialog annotation = parameter.getAnnotation(AFXFromFileSaveDialog.class);
		when(dialogController.showFileSaveDialog(anyString(), isNull(), isNull(), any(), eq(owner))).thenReturn(null);

		// WHEN
		final Object result = resolver.resolve(instance, method, parameter, annotation, parameter.getType());

		// THEN
		assertThat(result, nullValue());
		assertThat(resolver.continueMethodInvocation(), equalTo(false));
	}

	public class ClassWithMethods {

		public View _view;

		public ClassWithMethods() {
			_view = Mockito.spy(new ParentView("viewId", new StaticView(), this));
			when(_view.getWindow()).thenReturn(owner);
		}

		public void methodWithFile(@AFXFromFileSaveDialog(title = "Save Music File", extensionFilter = { "Music Files",
				"*.mp3", "*.wav" }) final File file) {
		}

		public void methodWithPath(@AFXFromFileSaveDialog(title = "Save Music File") final Path path) {
		}

		public void methodWithURI(@AFXFromFileSaveDialog(title = "Save Music File") final URI uri) {
		}

		public void methodWithString(@AFXFromFileSaveDialog(title = "Save Music File") final String string) {
		}

		public void methodWithFileAndNotContinueOnCancel(
				@AFXFromFileSaveDialog(title = "Save Music File", continueOnCancel = false) final String string) {
		}

	}

	/**
	 * A static view class with some elements having an ID.
	 *
	 * @author koster
	 *
	 */
	public class StaticView extends HBox {
	}

}
