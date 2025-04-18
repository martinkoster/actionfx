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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.core.ActionFXMock;
import com.github.actionfx.core.annotation.AFXArgHint;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXFromDirectoryChooserDialog;
import com.github.actionfx.core.annotation.AFXFromFileOpenDialog;
import com.github.actionfx.core.annotation.AFXFromFileSaveDialog;
import com.github.actionfx.core.annotation.AFXFromTextInputDialog;
import com.github.actionfx.core.annotation.AFXRequiresUserConfirmation;
import com.github.actionfx.core.annotation.ArgumentHint;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.instrumentation.ControllerWrapper;
import com.github.actionfx.core.method.ControllerMethodInvocationAdapter.ParameterValue;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

/**
 * JUnit test case for {@link ControllerMethodInvocationAdapter}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ControllerMethodInvocationAdapterTest {

	private ActionFXMock actionFX;

	@BeforeEach
	void onSetup() {
		actionFX = new ActionFXMock();
		actionFX.addBean(BeanContainerFacade.CONVERSION_SERVICE_BEANNAME, new ConversionService());
	}

	@Test
	void testInvoke_withVoidMethod() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("voidMethod");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
        assertThat(instance.getInvokedArguments()).isEmpty();
		assertThat(result).isNull();
	}

	@Test
	void testInvoke_withmethodReturningInt() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("methodReturningInt");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
        assertThat(instance.getInvokedArguments()).isEmpty();
		assertThat(result).isEqualTo(Integer.valueOf(42));
	}

	@Test
	void testInvoke_withVoidMethodWithStringAndIntParameter() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter(
				"voidMethodWithStringAndIntParameter");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
		assertThat(instance.getInvokedArguments()).containsExactly("Type-based Value 1", Integer.valueOf(4711));
		assertThat(result).isNull();
	}

	@Test
	void testInvoke_withVoidMethodWithOldAndNewValue() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithOldAndNewValue");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
		// first "old", then "new"
		assertThat(instance.getInvokedArguments()).containsExactly(Integer.valueOf(100), Integer.valueOf(1000));
		assertThat(result).isNull();

	}

	@Test
	void testInvoke_withVoidMethodWithNewAndOldValue() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithNewAndOldValue");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
		// first "new", then "old"
		assertThat(instance.getInvokedArguments()).containsExactly(Integer.valueOf(1000), Integer.valueOf(100));
		assertThat(result).isNull();
	}

	@Test
	void testInvoke_withVoidMethodWithWithTwoStrings_checkParameterConsumption() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithWithTwoStrings");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
		// check that the string arguments have taken in order (and not repeated)
		assertThat(instance.getInvokedArguments()).containsExactly("Type-based Value 1", "Type-based Value 2");
		assertThat(result).isNull();
	}

	@SuppressWarnings("unchecked")
	@Test
	void testInvoke_withVoidMethodWithSelected() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("voidMethodWithSelected");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
		// first "new", then "old"
		assertThat(instance.getInvokedArguments()).hasSize(1); // the list of invoked arguments now contains a list
		assertThat((List<String>) instance.getInvokedArguments().get(0)).containsExactly("selected");
		assertThat(result).isNull();
	}

	@SuppressWarnings("unchecked")
	@Test
	void testInvoke_withVoidMethodWithSelectedAddedRemovedLists() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter(
				"voidMethodWithSelectedAddedRemovedLists");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
		// first "new", then "old"
		assertThat(instance.getInvokedArguments()).hasSize(3); // the list of invoked arguments now contains 3 lists
		assertThat((List<String>) instance.getInvokedArguments().get(0)).containsExactly("selected");
		assertThat((List<String>) instance.getInvokedArguments().get(1)).containsExactly("added");
		assertThat((List<String>) instance.getInvokedArguments().get(2)).containsExactly("removed");
		assertThat(result).isNull();
	}

	@SuppressWarnings("unchecked")
	@Test
	void testInvoke_withVoidMethodWithRemovedAddedSelectedLists() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter(
				"voidMethodWithRemovedAddedSelectedLists");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
		// first "new", then "old"
		assertThat(instance.getInvokedArguments()).hasSize(3); // the list of invoked arguments now contains 3 lists
		assertThat((List<String>) instance.getInvokedArguments().get(0)).containsExactly("removed");
		assertThat((List<String>) instance.getInvokedArguments().get(1)).containsExactly("added");
		assertThat((List<String>) instance.getInvokedArguments().get(2)).containsExactly("selected");
		assertThat(result).isNull();
	}

	@Test
	void testInvokeAsynchronously_withMethodReturningInt() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("methodReturningInt");
		final IntegerProperty result = new SimpleIntegerProperty(0);

		// WHEN
		adapter.invokeAsynchronously(value -> result.set((int) value));

		// THEN
		WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
		assertThat(result.get()).isEqualTo(42);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testInvoke_withVoidMethodWithInjectedControlArguments() {
		// GIVEN
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter(
				"voidMethodWithInjectedControlArguments");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.isExecuted()).isTrue();
		assertThat(instance.getInvokedArguments()).hasSize(2);
		assertThat(instance.getInvokedArguments().get(0)).isEqualTo("Hello World");
		assertThat(instance.getInvokedArguments().get(1)).isInstanceOf(List.class);
		assertThat((List<String>) instance.getInvokedArguments().get(1)).containsExactly("List Item 2", "List Item 3");
		assertThat(result).isNull();
	}

	@Test
	void testInvoke_withVoidMethodWithInjectedControlArguments_butControllerHasNoView() {
		// WHEN and THEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> methodInvocationAdapterWithoutView("voidMethodWithInjectedControlArguments"));
		assertThat(ex.getMessage()).contains("There is no view associated with controller of type");
	}

	@Test
	void testInvoke_withVoidMethodWithInjectedControlArguments_butMethodArgumentHasUnknownControlId() {
		// WHEN and THEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> methodInvocationAdapter("voidMethodWithUnknownControlIdArguments"));
		assertThat(ex.getMessage()).contains("There is no node with ID='someUnknownControl' inside the view associated with controller");
	}

	@Test
	void testInvoke_withVoidMethodWithInjectedControlArguments_butReferencedNodeIsNotOfTypeControl() {
		// WHEN and THEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> methodInvocationAdapter("voidMethodWithTypeIsNotControl"));
		assertThat(ex.getMessage()).contains("Node with ID='vbox' inside the view hosted by controller");
		assertThat(ex.getMessage()).contains("is not a javafx.scene.control.Control!");
	}

	@Test
	void testInvoke_withVoidMethodWithInjectedControlArguments_butMethodArgumentIsOfIncompatibleType() {
		// WHEN and THEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> methodInvocationAdapter("voidMethodWithInjectedControlArgumentsOfIncompatibleType"));
		assertThat(ex.getMessage()).contains("Value retrieved for control with ID='textField'");
		assertThat(ex.getMessage()).contains(" is not compatible with the method argument of type 'interface java.util.List'!");
	}

	@Test
	void testInvoke_withUserConfirmation_userConfirms() {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		when(dialogController.showConfirmationDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn(Boolean.TRUE);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("requiresUserConfirmation");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		verify(dialogController, times(1)).showConfirmationDialog(ArgumentMatchers.eq("Title"),
				ArgumentMatchers.endsWith("Header"), ArgumentMatchers.eq("Content"));
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isTrue();
		assertThat(result).isEqualTo("Hello World");
	}

	@Test
	void testInvoke_withUserConfirmation_userCancels() {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		when(dialogController.showConfirmationDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn(Boolean.FALSE);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("requiresUserConfirmation");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		verify(dialogController, times(1)).showConfirmationDialog(ArgumentMatchers.eq("Title"),
				ArgumentMatchers.endsWith("Header"), ArgumentMatchers.eq("Content"));
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isFalse();
		assertThat(result).isNull();
	}

	@Test
	void testInvoke_withUserConfirmation_dialogValuesAreTakenFromResourceBundle() {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		when(dialogController.showConfirmationDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn(Boolean.TRUE);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter(
				"requiresUserConfirmationWithResourceBundle");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		verify(dialogController, times(1)).showConfirmationDialog(ArgumentMatchers.eq("Title"),
				ArgumentMatchers.endsWith("Header"), ArgumentMatchers.eq("Content"));
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isTrue();
		assertThat(result).isEqualTo("Hello World");
	}

	@Test
	void testInvoke_withUserConfirmation_keysDoNotExist_defaultValuesAreTaken() {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		when(dialogController.showConfirmationDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn(Boolean.TRUE);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter(
				"requiresUserConfirmationWithResourceBundleButKeysDoNotExist");

		// WHEN
		final Object result = adapter.invoke();

		// THEN
		verify(dialogController, times(1)).showConfirmationDialog(ArgumentMatchers.eq("Title"),
				ArgumentMatchers.endsWith("Header"), ArgumentMatchers.eq("Content"));
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isTrue();
		assertThat(result).isEqualTo("Hello World");
	}

	@Test
	void testInvokeAsynchronously_withUserConfirmation_userConfirms() {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		when(dialogController.showConfirmationDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn(Boolean.TRUE);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("requiresUserConfirmation");
		final StringProperty result = new SimpleStringProperty(null);

		// WHEN
		adapter.invokeAsynchronously(value -> result.set((String) value));

		// THEN
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
		verify(dialogController, times(1)).showConfirmationDialog(ArgumentMatchers.eq("Title"),
				ArgumentMatchers.eq("Header"), ArgumentMatchers.eq("Content"));
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isTrue();
		assertThat(result.get()).isEqualTo("Hello World");
	}

	@Test
	void testInvokeAsynchronously_withUserConfirmation_userCancels() {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		when(dialogController.showConfirmationDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn(Boolean.FALSE);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("requiresUserConfirmation");
		final StringProperty result = new SimpleStringProperty(null);

		// WHEN
		adapter.invokeAsynchronously(value -> result.set((String) value));

		// THEN
		WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
		verify(dialogController, times(1)).showConfirmationDialog(ArgumentMatchers.eq("Title"),
				ArgumentMatchers.endsWith("Header"), ArgumentMatchers.eq("Content"));
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isFalse();
		assertThat(result.get()).isNull();
	}

	@Test
	void testInvoke_fromFileOpenDialog() throws IOException {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		final File file = Files.createTempFile("junit", "-tmp").toFile();
		when(dialogController.showFileOpenDialog(ArgumentMatchers.anyString(), isNull(), isNull(), isNull(), isNull()))
				.thenReturn(file);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("openFile");

		// WHEN
		final File result = adapter.invoke();

		// THEN
		assertThat(result).isEqualTo(file);
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isTrue();
	}

	@Test
	void testInvoke_fromFileOpenDialog_userCancels() {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		when(dialogController.showFileOpenDialog(ArgumentMatchers.anyString(), isNull(), isNull(), isNull(), isNull()))
				.thenReturn(null);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("openFile");

		// WHEN
		final File result = adapter.invoke();

		// THEN
		assertThat(result).isNull();
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isFalse();
	}

	@Test
	void testInvoke_fromFileSaveDialog() throws IOException {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		final File file = Files.createTempFile("junit", "-tmp").toFile();
		when(dialogController.showFileSaveDialog(ArgumentMatchers.anyString(), isNull(), isNull(), isNull(), isNull()))
				.thenReturn(file);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("saveFile");

		// WHEN
		final File result = adapter.invoke();

		// THEN
		assertThat(result).isEqualTo(file);
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isTrue();
	}

	@Test
	void testInvoke_fromDirectoryChooserDialog() throws IOException {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		final File file = Files.createTempFile("junit", "-tmp").toFile();
		when(dialogController.showDirectoryChooserDialog(ArgumentMatchers.anyString(), isNull(), isNull()))
				.thenReturn(file);
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("openDirectory");

		// WHEN
		final File result = adapter.invoke();

		// THEN
		assertThat(result).isEqualTo(file);
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isTrue();
	}

	@Test
	void testInvoke_fromTextInputDialog() throws IOException {
		// GIVEN
		final ActionFXMock actionFX = new ActionFXMock();
		final DialogController dialogController = Mockito.mock(DialogController.class);
		when(dialogController.showTextInputDialog(anyString(), anyString(), anyString(), anyString()))
				.thenReturn("Hello World");
		actionFX.addBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME, dialogController);
		final ControllerMethodInvocationAdapter adapter = methodInvocationAdapter("textInput");

		// WHEN
		final String result = adapter.invoke();

		// THEN
		assertThat(result).isEqualTo("Hello World");
		final ClassWithPublicMethods instance = (ClassWithPublicMethods) adapter.getInstance();
        assertThat(instance.executed).isTrue();
	}

	private static ControllerMethodInvocationAdapter methodInvocationAdapter(final String methodName) {
		final ClassWithPublicMethods instance = createEnhancedInstance(true);
		final Method method = ReflectionUtils.findMethod(ClassWithPublicMethods.class, methodName, (Class<?>[]) null);
		return new ControllerMethodInvocationAdapter(instance, method, ParameterValue.of("Type-based Value 1"),
				ParameterValue.of("Type-based Value 2"), ParameterValue.of(Integer.valueOf(4711)),
				ParameterValue.ofOldValue(Integer.valueOf(100)), ParameterValue.ofNewValue(Integer.valueOf(1000)),
				ParameterValue.ofAllSelectedValues(List.of("selected")), ParameterValue.ofAddedValues(List.of("added")),
				ParameterValue.ofRemovedValues(List.of("removed")));
	}

	private static ControllerMethodInvocationAdapter methodInvocationAdapterWithoutView(final String methodName) {
		final ClassWithPublicMethods instance = createEnhancedInstance(false);
		final Method method = ReflectionUtils.findMethod(ClassWithPublicMethods.class, methodName, (Class<?>[]) null);
		return new ControllerMethodInvocationAdapter(instance, method);
	}

	/**
	 * We need to inject a new field with name "_view", so that we can test or
	 * invocation adapter.
	 *
	 * @param createView {@code true}, if a view shall be created and
	 *                   injected,{@code false}, if the view shall be set to
	 *                   {@code null}.
	 * @return the instance of the enhanced class
	 */
	private static ClassWithPublicMethods createEnhancedInstance(final boolean createView) {
		final ClassWithPublicMethods instance = new ClassWithPublicMethods();
		if (createView) {
			final FxmlView view = new FxmlView("testId", "/testfxml/ControllerMethodInvocationAdapterTestView.fxml",
					instance);
			final ListView<String> listView = view.lookupNode("listView").getWrapped();
			listView.getItems().add("List Item 1");
			listView.getItems().add("List Item 2");
			listView.getItems().add("List Item 3");
			// select 2 and 3
			listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			listView.getSelectionModel().select("List Item 2");
			listView.getSelectionModel().select("List Item 3");
			ControllerWrapper.setViewOn(instance, view);
		}
		return instance;
	}

	/**
	 * Test class with public methods.
	 *
	 * @author koster
	 *
	 */
	@AFXController(viewId = "viewId", resourcesBasename = "i18n.TestResources")
	public static class ClassWithPublicMethods {

		public View _view;

		private final List<Object> invokedArguments = new ArrayList<>();

		private boolean executed;

		public void voidMethod() {
			setExecuted();
		}

		public int methodReturningInt() {
			setExecuted();
			return 42;
		}

		public void voidMethodWithStringAndIntParameter(final String string, final Integer integer) {
			invokedArguments.add(string);
			invokedArguments.add(integer);
			setExecuted();
		}

		public void voidMethodWithOldAndNewValue(@AFXArgHint(ArgumentHint.OLD_VALUE) final Integer oldValue,
				@AFXArgHint(ArgumentHint.NEW_VALUE) final Integer newValue) {
			invokedArguments.add(oldValue);
			invokedArguments.add(newValue);
			setExecuted();
		}

		public void voidMethodWithNewAndOldValue(@AFXArgHint(ArgumentHint.NEW_VALUE) final Integer newValue,
				@AFXArgHint(ArgumentHint.OLD_VALUE) final Integer oldValue) {
			invokedArguments.add(newValue);
			invokedArguments.add(oldValue);
			setExecuted();
		}

		public void voidMethodWithWithTwoStrings(final String value1, final String value2) {
			invokedArguments.add(value1);
			invokedArguments.add(value2);
			setExecuted();
		}

		public void voidMethodWithSelected(final List<String> selected) {
			invokedArguments.add(selected);
			setExecuted();
		}

		public void voidMethodWithSelectedAddedRemovedLists(final List<String> selected, final List<String> added,
				final List<String> removed) {
			invokedArguments.add(selected);
			invokedArguments.add(added);
			invokedArguments.add(removed);
			setExecuted();
		}

		public void voidMethodWithRemovedAddedSelectedLists(
				@AFXArgHint(ArgumentHint.REMOVED_VALUES) final List<String> removed,
				@AFXArgHint(ArgumentHint.ADDED_VALUES) final List<String> added,
				@AFXArgHint(ArgumentHint.ALL_SELECTED) final List<String> selected) {
			invokedArguments.add(removed);
			invokedArguments.add(added);
			invokedArguments.add(selected);
			setExecuted();
		}

		public void voidMethodWithInjectedControlArguments(@AFXControlValue("textField") final String textValue,
				@AFXControlValue("listView") final List<String> selectedEntries) {
			invokedArguments.add(textValue);
			invokedArguments.add(selectedEntries);
			setExecuted();
		}

		public void voidMethodWithUnknownControlIdArguments(
				@AFXControlValue("someUnknownControl") final String textValue) {
			setExecuted();
		}

		public void voidMethodWithTypeIsNotControl(@AFXControlValue("vbox") final String textValue) {
			setExecuted();
		}

		public void voidMethodWithInjectedControlArgumentsOfIncompatibleType(
				@AFXControlValue("textField") final List<String> selectedEntries) {
			setExecuted();
		}

		@AFXRequiresUserConfirmation(title = "Title", header = "Header", content = "Content")
		public String requiresUserConfirmation() {
			setExecuted();
			return "Hello World";
		}

		@AFXRequiresUserConfirmation(titleKey = "title", headerKey = "header", contentKey = "content")
		public String requiresUserConfirmationWithResourceBundle() {
			setExecuted();
			return "Hello World";
		}

		@AFXRequiresUserConfirmation(title = "Title", header = "Header", content = "Content", titleKey = "fantasyTitleKey", headerKey = "fantasyHeaderKey", contentKey = "fantasyContentKey")
		public String requiresUserConfirmationWithResourceBundleButKeysDoNotExist() {
			setExecuted();
			return "Hello World";
		}

		public File saveFile(@AFXFromFileSaveDialog(continueOnCancel = false) final File file) {
			setExecuted();
			return file;
		}

		public File openFile(@AFXFromFileOpenDialog final File file) {
			setExecuted();
			return file;
		}

		public File openDirectory(@AFXFromDirectoryChooserDialog final File file) {
			setExecuted();
			return file;
		}

		public String textInput(@AFXFromTextInputDialog final String text) {
			setExecuted();
			return text;
		}

		private void setExecuted() {
			executed = true;
		}

		public boolean isExecuted() {
			return executed;
		}

		public List<Object> getInvokedArguments() {
			return invokedArguments;
		}
	}

}
