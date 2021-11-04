/*
 * Copyright (c) 2020 Martin Koster
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
package com.github.actionfx.core;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX.ActionFXBuilder;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.container.DefaultBeanContainer;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.events.SimplePriorityAwareEventBus;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer;
import com.github.actionfx.core.instrumentation.ActionFXEnhancer.EnhancementStrategy;
import com.github.actionfx.core.instrumentation.bytebuddy.ActionFXByteBuddyEnhancer;
import com.github.actionfx.core.test.TestController;
import com.github.actionfx.core.test.app.MainController;
import com.github.actionfx.core.test.app.SampleApp;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JUnit test case for {@link ActionFX}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ActionFXTest {

	@BeforeEach
	void onSetup() {
		// set view manager instance to 'null' in order to force the creation of a
		// ViewManager instance for each test
		ActionFX.instance = null;
	}

	@Test
	void testBuilder_minimal_withConfigurationClass_sampleApp() {
		// WHEN
		assertThat(ActionFX.isConfigured(), equalTo(false));
		assertThat(ActionFX.isInitialized(), equalTo(false));
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();

		// THEN
		assertThat(ActionFX.isConfigured(), equalTo(true));
		assertThat(ActionFX.isInitialized(), equalTo(false));
		assertThat(actionFX.getEnhancementStrategy(), equalTo(EnhancementStrategy.SUBCLASSING));
		assertThat(actionFX.getEnhancer(), instanceOf(ActionFXByteBuddyEnhancer.class));
		assertThat(actionFX.getMainViewId(), equalTo("mainView"));
		assertThat(actionFX.getScanPackage(), equalTo(SampleApp.class.getPackage().getName()));
		assertThat(actionFX.getBeanContainer(), instanceOf(DefaultBeanContainer.class));
		assertThat(actionFX.getObservableLocale().getValue(), equalTo(Locale.getDefault()));
		assertThat(actionFX, equalTo(ActionFX.getInstance()));
	}

	@Test
	void testBuilder_minimal_withConfigurationClass_invalidConfigurationClass() {
		// GIVEN
		final ActionFXBuilder builder = ActionFX.builder();

		// WHEN and THEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> builder.configurationClass(AppClassWithoutAFXApplicationAnnotation.class));
		assertThat(ex.getMessage(), containsString("or its super-classes are not annotated with @AFXApplication"));
	}

	@Test
	void testBuilder_configurative() {
		// GIVEN
		final ActionFXEnhancer enhancer = Mockito.mock(ActionFXEnhancer.class);

		// WHEN
		final ActionFX actionFX = ActionFX.builder().scanPackage(SampleApp.class.getPackage().getName())
				.mainViewId("mainView").actionFXEnhancer(enhancer).locale(Locale.US)
				.enhancementStrategy(EnhancementStrategy.SUBCLASSING).build();

		// THEN
		assertThat(actionFX.getEnhancementStrategy(), equalTo(EnhancementStrategy.SUBCLASSING));
		assertThat(actionFX.getEnhancer(), equalTo(enhancer));
		assertThat(actionFX.getMainViewId(), equalTo("mainView"));
		assertThat(actionFX.getScanPackage(), equalTo(SampleApp.class.getPackage().getName()));
		assertThat(actionFX.getBeanContainer(), instanceOf(DefaultBeanContainer.class));
		assertThat(actionFX.getObservableLocale().getValue(), equalTo(Locale.US));
		assertThat(actionFX, equalTo(ActionFX.getInstance()));
	}

	@Test
	void testBuilder_notYetBuilt() {

		// WHEN and THEN (
		assertThrows(IllegalStateException.class, ActionFX::getInstance);
	}

	@Test
	void testBuilder_alreadyBuilt() {

		// GIVEN (instance is built)
		final ActionFX actionFX = ActionFX.builder().build();

		// WHEN and THEN (
		assertThat(actionFX, notNullValue());
		assertThrows(IllegalStateException.class, ActionFX::builder);
	}

	@Test
	void testBuilder_uncaughtExceptionHandler() {
		// GIVEN
		final UncaughtExceptionHandler handler = (t, e) -> {
		};

		// WHEN
		final ActionFX actionFX = ActionFX.builder().uncaughtExceptionHandler(handler).build();

		// THEN
		assertThat(actionFX, notNullValue());
		assertThat(Thread.getDefaultUncaughtExceptionHandler(), sameInstance(handler));

	}

	@Test
	void testScanComponents_usingDefaultBeanContainer() {
		// GIVEN
		assertThat(ActionFX.isConfigured(), equalTo(false));
		assertThat(ActionFX.isInitialized(), equalTo(false));
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();

		// WHEN
		actionFX.scanForActionFXComponents();

		// THEN
		assertThat(ActionFX.isConfigured(), equalTo(true));
		assertThat(ActionFX.isInitialized(), equalTo(true));
		final View view = actionFX.getView("mainView");
		final MainController mainControllerById = actionFX.getBean("mainController");
		final MainController mainControllerByClassName = actionFX.getBean(MainController.class);

		assertThat(view, notNullValue());
		assertThat(mainControllerById, notNullValue());
		assertThat(mainControllerByClassName, notNullValue());
		assertThat(mainControllerById, sameInstance(mainControllerByClassName));
	}

	@Test
	void testScanComponents_usingCustomBeanContainer() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();
		final BeanContainerFacade customBeanContainer = Mockito.mock(BeanContainerFacade.class);
		final ArgumentCaptor<String> rootPackageCaptor = ArgumentCaptor.forClass(String.class);

		// WHEN
		actionFX.scanForActionFXComponents(customBeanContainer);

		// THEN (custom container has be asked to populate container with the
		// rootPackage of SampleApp)
		verify(customBeanContainer).runComponentScan(rootPackageCaptor.capture());
		assertThat(rootPackageCaptor.getValue(), equalTo(SampleApp.class.getPackageName()));
	}

	@Test
	void testScanComponents_scanAlreadyPerformed_illegalState() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();

		// WHEN
		actionFX.scanForActionFXComponents();

		// THEN (another call to scanComponents results in an exception)
		assertThrows(IllegalStateException.class, () -> actionFX.scanForActionFXComponents());
	}

	@Test
	void testScanComponents_getView_viewNotFound() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();

		// WHEN
		actionFX.scanForActionFXComponents();
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> actionFX.getView("fantasyView"));

		// THEN
		assertThat(ex.getMessage(), containsString("There is no view with ID='fantasyView'"));
	}

	@Test
	void testAddController() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().build();
		actionFX.scanForActionFXComponents();

		// WHEN
		actionFX.addController(TestController.class);

		// THEN
		assertThat(actionFX.getBean(TestController.class), notNullValue());
	}

	@Test
	void testScanComponents_getView_viewIsNotInstanceOfView() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();

		// WHEN
		actionFX.scanForActionFXComponents();
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> actionFX.getView("mainController"));

		// THEN
		assertThat(ex.getMessage(), containsString("Bean with ID='mainController' is not of type"));
	}

	@Test
	void testGetConversionService() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ConversionService service = actionFX.getConversionService();

		// THEN
		assertThat(service, notNullValue());
	}

	@Test
	void testGetEventBus() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final PriorityAwareEventBus eventBus = actionFX.getEventBus();

		// THEN
		assertThat(eventBus, notNullValue());
		assertThat(eventBus, instanceOf(SimplePriorityAwareEventBus.class));
	}

	@Test
	void testGetView() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final Controller controller = new Controller();
		final View mockView = Mockito.mock(View.class);
		controller._view = mockView;
		when(actionFX.getBean(ArgumentMatchers.eq(Controller.class))).thenReturn(controller);

		// WHEN
		final View view = actionFX.getView(controller);

		// THEN
		assertThat(view, sameInstance(mockView));
	}

	@Test
	void testShowView() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final Controller controller = new Controller();
		final View mockView = Mockito.mock(View.class);
		controller._view = mockView;
		when(actionFX.getBean(ArgumentMatchers.eq(Controller.class))).thenReturn(controller);

		// WHEN
		actionFX.showView(controller);

		// THEN
		verify(mockView, times(1)).show();
	}

	@Test
	@TestInFxThread
	void testShowView_withStageSupplied() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final Controller controller = new Controller();
		final View mockView = Mockito.mock(View.class);
		controller._view = mockView;
		when(actionFX.getBean(ArgumentMatchers.eq(Controller.class))).thenReturn(controller);
		final Stage stage = new Stage();

		// WHEN
		actionFX.showView(controller, stage);

		// THEN
		verify(mockView, times(1)).show(ArgumentMatchers.eq(stage));
	}

	@Test
	void testShowViewAndWait() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final Controller controller = new Controller();
		final View mockView = Mockito.mock(View.class);
		controller._view = mockView;
		when(actionFX.getBean(ArgumentMatchers.eq(Controller.class))).thenReturn(controller);

		// WHEN
		actionFX.showViewAndWait(controller);

		// THEN
		verify(mockView, times(1)).showAndWait();
	}

	@Test
	void testHideView() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final Controller controller = new Controller();
		final View mockView = Mockito.mock(View.class);
		controller._view = mockView;
		when(actionFX.getBean(ArgumentMatchers.eq(Controller.class))).thenReturn(controller);

		// WHEN
		actionFX.hideView(controller);

		// THEN
		verify(mockView, times(1)).hide();
	}

	@Test
	void testShowConfirmationDialog() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);
		when(controller.showConfirmationDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn(Boolean.TRUE);

		// WHEN
		assertThat(actionFX.showConfirmationDialog("Title", "HeaderText", "ContentText"), equalTo(Boolean.TRUE));

		// THEN
		verify(controller).showConfirmationDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"));
	}

	@Test
	void testShowWarningDialog() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);

		// WHEN
		actionFX.showWarningDialog("Title", "HeaderText", "ContentText");

		// THEN
		verify(controller).showWarningDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"));
	}

	@Test
	void testShowInformationDialog() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);

		// WHEN
		actionFX.showInformationDialog("Title", "HeaderText", "ContentText");

		// THEN
		verify(controller).showInformationDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"));
	}

	@Test
	void testShowErrorDialog() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);

		// WHEN
		actionFX.showErrorDialog("Title", "HeaderText", "ContentText");

		// THEN
		verify(controller).showErrorDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"));
	}

	@Test
	void testShowDirectoryChooser() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFolder = Files.createTempDirectory("junit").toFile();
		final File initialFolder = Files.createTempDirectory("junit").toFile();

		when(controller.showDirectoryChooserDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFolder);

		// WHEN
		assertThat(actionFX.showDirectoryChooserDialog("Title", initialFolder, owner), equalTo(selectedFolder));

		// THEN
		verify(controller).showDirectoryChooserDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowFileOpenDialog_initialFileName_extensionFilter() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFile = Files.createTempFile("junit", "-tmp").toFile();
		final File initialFolder = Files.createTempFile("junit", "-tmp").toFile();
		final ExtensionFilter filter = new ExtensionFilter("Text Files", "*.txt");

		when(controller.showFileOpenDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.anyString(), ArgumentMatchers.any(ExtensionFilter.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFile);

		// WHEN
		assertThat(actionFX.showFileOpenDialog("Title", initialFolder, "initial.txt", filter, owner),
				equalTo(selectedFile));

		// THEN
		verify(controller).showFileOpenDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq("initial.txt"), ArgumentMatchers.eq(filter), ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowFileOpenDialog() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFile = Files.createTempFile("junit", "-tmp").toFile();
		final File initialFolder = Files.createTempFile("junit", "-tmp").toFile();

		when(controller.showFileOpenDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFile);

		// WHEN
		assertThat(actionFX.showFileOpenDialog("Title", initialFolder, owner), equalTo(selectedFile));

		// THEN
		verify(controller).showFileOpenDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowFileSaveDialog() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFile = Files.createTempFile("junit", "-tmp").toFile();
		final File initialFolder = Files.createTempFile("junit", "-tmp").toFile();

		when(controller.showFileSaveDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFile);

		// WHEN
		assertThat(actionFX.showFileSaveDialog("Title", initialFolder, owner), equalTo(selectedFile));

		// THEN
		verify(controller).showFileSaveDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowFileSaveDialog_initialFileName_extensionFilter() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFile = Files.createTempFile("junit", "-tmp").toFile();
		final File initialFolder = Files.createTempFile("junit", "-tmp").toFile();
		final ExtensionFilter filter = new ExtensionFilter("Text Files", "*.txt");

		when(controller.showFileSaveDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.anyString(), ArgumentMatchers.any(ExtensionFilter.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFile);

		// WHEN
		assertThat(actionFX.showFileSaveDialog("Title", initialFolder, "initial.txt", filter, owner),
				equalTo(selectedFile));

		// THEN
		verify(controller).showFileSaveDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq("initial.txt"), ArgumentMatchers.eq(filter), ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowTextInputDialog() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);

		when(controller.showTextInputDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn("Text");

		// WHEN
		assertThat(actionFX.showTextInputDialog("Title", "HeaderText", "ContentText"), equalTo("Text"));

		// THEN
		verify(controller).showTextInputDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"));
	}

	@Test
	void testShowTextInputDialog_withDefaultText() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEAN))).thenReturn(controller);

		when(controller.showTextInputDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn("Text");

		// WHEN
		assertThat(actionFX.showTextInputDialog("Title", "HeaderText", "ContentText", "DefaultText"), equalTo("Text"));

		// THEN
		verify(controller).showTextInputDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"), ArgumentMatchers.eq("DefaultText"));
	}

	public static class AppClassWithoutAFXApplicationAnnotation {

	}

	public static class Controller {
		public View _view;
	}

}
