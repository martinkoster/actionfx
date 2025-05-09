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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Files;
import java.util.Locale;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX.ActionFXBuilder;
import com.github.actionfx.core.annotation.ValidationMode;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.container.DefaultActionFXBeanContainer;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.events.SimplePriorityAwareEventBus;
import com.github.actionfx.core.extension.beans.BeanExtension;
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
		// reset instance to 'null' in order to force the creation of a
		// new ActionFX instance for each test
		ActionFX.instance = null;
	}

	@Test
	void testBuilder_minimal_withConfigurationClass_sampleApp() {
		// WHEN
		assertThat(ActionFX.isConfigured()).isFalse();
		assertThat(ActionFX.isInitialized()).isFalse();
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();

		// THEN
		assertThat(ActionFX.isConfigured()).isTrue();
		assertThat(ActionFX.isInitialized()).isFalse();
		assertThat(actionFX.getEnhancementStrategy()).isEqualTo(EnhancementStrategy.SUBCLASSING);
		assertThat(actionFX.getEnhancer()).isInstanceOf(ActionFXByteBuddyEnhancer.class);
		assertThat(actionFX.getMainViewId()).isEqualTo("mainView");
		assertThat(actionFX.getScanPackage()).isEqualTo(SampleApp.class.getPackage().getName());
		assertThat(actionFX.getBeanContainer()).isInstanceOf(DefaultActionFXBeanContainer.class);
		assertThat(actionFX.getObservableLocale().getValue()).isEqualTo(Locale.getDefault());
		assertThat(actionFX.getValidationGlobalMode()).isEqualTo(ValidationMode.MANUAL);
		assertThat(actionFX.isValidationApplyResultDecoration()).isFalse();
		assertThat(actionFX.isValidationApplyRequiredDecoration()).isFalse();
		assertThat(actionFX.getValidationStartTimeoutMs()).isEqualTo(500);
		assertThat(actionFX).isEqualTo(ActionFX.getInstance());
	}

	@Test
	void testBuilder_minimal_withConfigurationClass_invalidConfigurationClass() {
		// GIVEN
		final ActionFXBuilder builder = ActionFX.builder();

		// WHEN and THEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> builder.configurationClass(AppClassWithoutAFXApplicationAnnotation.class));
		assertThat(ex.getMessage()).contains("or its super-classes are not annotated with @AFXApplication");
	}

	@Test
	void testBuilder_configurative() {
		// GIVEN
		final ActionFXEnhancer enhancer = Mockito.mock(ActionFXEnhancer.class);

		// WHEN
		final ActionFX actionFX = ActionFX.builder().scanPackage(SampleApp.class.getPackage().getName())
				.mainViewId("mainView").actionFXEnhancer(enhancer).locale(Locale.US)
				.enhancementStrategy(EnhancementStrategy.SUBCLASSING).validationGlobalMode(ValidationMode.MANUAL)
				.validationApplyResultDecoration(false).validationApplyRequiredDecoration(false)
				.validationStartTimeoutMs(500).build();

		// THEN
		assertThat(actionFX.getEnhancementStrategy()).isEqualTo(EnhancementStrategy.SUBCLASSING);
		assertThat(actionFX.getEnhancer()).isEqualTo(enhancer);
		assertThat(actionFX.getMainViewId()).isEqualTo("mainView");
		assertThat(actionFX.getScanPackage()).isEqualTo(SampleApp.class.getPackage().getName());
		assertThat(actionFX.getBeanContainer()).isInstanceOf(DefaultActionFXBeanContainer.class);
		assertThat(actionFX.getObservableLocale().getValue()).isEqualTo(Locale.US);
		assertThat(actionFX.getValidationGlobalMode()).isEqualTo(ValidationMode.MANUAL);
		assertThat(actionFX.isValidationApplyResultDecoration()).isFalse();
		assertThat(actionFX.isValidationApplyRequiredDecoration()).isFalse();
		assertThat(actionFX.getValidationStartTimeoutMs()).isEqualTo(500);
		assertThat(actionFX).isEqualTo(ActionFX.getInstance());
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
		assertThat(actionFX).isNotNull();
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
		assertThat(actionFX).isNotNull();
		assertThat(Thread.getDefaultUncaughtExceptionHandler()).isSameAs(handler);

	}

	@SuppressWarnings("unchecked")
	@Test
	void testBuilder_customController_and_customBeanExtensions_instancesArePassed() {
		// GIVEN
		final Consumer<Object> customControllerExtension = Mockito.mock(Consumer.class);
		final BeanExtension customBeanExtension = Mockito.mock(BeanExtension.class);

		// WHEN
		final ActionFX actionFX = ActionFX.builder().controllerExtension(customControllerExtension)
				.beanExtension(customBeanExtension).build();

		// THEN
		assertThat(actionFX.actionFXExtensionsBean).isNotNull();
		assertThat(actionFX.actionFXExtensionsBean.getCustomControllerExtensions()).containsExactly(customControllerExtension);
		assertThat(actionFX.actionFXExtensionsBean.getCustomBeanExtensions()).containsExactly(customBeanExtension);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testBuilder_customController_and_customBeanExtensions_classesArePassed() {
		// WHEN
		final ActionFX actionFX = ActionFX.builder().controllerExtension(CustomControllerExtension.class)
				.beanExtension(CustomBeanExtension.class).build();

		// THEN
		assertThat(actionFX.actionFXExtensionsBean).isNotNull();
		assertThat(actionFX.actionFXExtensionsBean.getCustomControllerExtensions())
				.anyMatch(ActionFXTest.CustomControllerExtension.class::isInstance);
		assertThat(actionFX.actionFXExtensionsBean.getCustomBeanExtensions())
				.anyMatch(ActionFXTest.CustomBeanExtension.class::isInstance);
	}

	@Test
	void testBuilder_beanContainerClass_byClass() {
		// WHEN
		final ActionFX actionFX = ActionFX.builder().beanContainerClass(CustomBeanContainer.class).build();

		// THEN
		assertThat(actionFX.getBeanContainer()).isInstanceOf(CustomBeanContainer.class);
	}

	@Test
	void testBuilder_beanContainer_byInstance() {
		// WHEN
		final CustomBeanContainer container = new CustomBeanContainer();
		final ActionFX actionFX = ActionFX.builder().beanContainer(container).build();

		// THEN
		assertThat(actionFX.getBeanContainer()).isSameAs(container);
	}

	@Test
	void testBuilder_enableBeanContainerAutodetection() {
		// WHEN
		final ActionFX actionFX = ActionFX.builder().enableBeanContainerAutodetection(true).build();

		// THEN
		assertThat(actionFX.getBeanContainer()).isInstanceOf(DefaultActionFXBeanContainer.class);
	}

	@Test
	void testBuilder_disableBeanContainerAutodetection() {
		// WHEN
		final ActionFX actionFX = ActionFX.builder().enableBeanContainerAutodetection(false).build();

		// THEN
		assertThat(actionFX.getBeanContainer()).isInstanceOf(DefaultActionFXBeanContainer.class);
	}

	@Test
	void testScanForActionFXComponents_usingDefaultBeanContainer() {
		// GIVEN
		assertThat(ActionFX.isConfigured()).isFalse();
		assertThat(ActionFX.isInitialized()).isFalse();
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();

		// WHEN
		actionFX.scanForActionFXComponents();

		// THEN
		assertThat(ActionFX.isConfigured()).isTrue();
		assertThat(ActionFX.isInitialized()).isTrue();
		final View view = actionFX.getView("mainView");
		final MainController mainControllerById = actionFX.getBean("mainController");
		final MainController mainControllerByClassName = actionFX.getBean(MainController.class);

		assertThat(view).isNotNull();
		assertThat(mainControllerById).isNotNull();
		assertThat(mainControllerByClassName).isNotNull();
		assertThat(mainControllerById).isSameAs(mainControllerByClassName);
	}

	@Test
	void testScanForActionFXComponents_usingCustomBeanContainer() {
		// GIVEN
		final BeanContainerFacade customBeanContainer = Mockito.mock(BeanContainerFacade.class);
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class)
				.beanContainer(customBeanContainer).build();
		final ArgumentCaptor<String> rootPackageCaptor = ArgumentCaptor.forClass(String.class);

		// WHEN
		actionFX.scanForActionFXComponents();

		// THEN (custom container has be asked to populate container with the
		// rootPackage of SampleApp)
		verify(customBeanContainer).runComponentScan(rootPackageCaptor.capture());
		assertThat(rootPackageCaptor.getValue()).isEqualTo(SampleApp.class.getPackageName());
	}

	@Test
	void testScanForActionFXComponents_scanAlreadyPerformed_illegalState() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).build();

		// WHEN
		actionFX.scanForActionFXComponents();

		// THEN (another call to scanComponents results in an exception)
		assertThrows(IllegalStateException.class, actionFX::scanForActionFXComponents);
	}

	@Test
	void testScanForActionFXComponents_getView_viewNotFound() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();

		// WHEN
		actionFX.scanForActionFXComponents();
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> actionFX.getView("fantasyView"));

		// THEN
		assertThat(ex.getMessage()).contains("There is no view with ID='fantasyView'");
	}

	@Test
	void testScanForActionFXComponents_assureActionFXBeansAreAdded() {
		// GIVEN
		final BeanContainerFacade customBeanContainer = Mockito.mock(BeanContainerFacade.class);
		final ActionFX actionFX = ActionFX.builder().locale(Locale.US).beanContainer(customBeanContainer).build();

		// WHEN
		actionFX.scanForActionFXComponents();

		// THEN
		verify(customBeanContainer, times(1)).addActionFXBeans(eq(actionFX));
	}

	@Test
	void testAddController() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().build();
		actionFX.scanForActionFXComponents();

		// WHEN
		actionFX.addController(TestController.class);

		// THEN
		assertThat(actionFX.getBean(TestController.class)).isNotNull();
	}

	@Test
	void testScanForActionFXComponents_getView_viewIsNotInstanceOfView() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();

		// WHEN
		actionFX.scanForActionFXComponents();
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> actionFX.getView("mainController"));

		// THEN
		assertThat(ex.getMessage()).contains("Bean with ID='mainController' is not of type");
	}

	@Test
	void testGetConversionService() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final ConversionService service = actionFX.getConversionService();

		// THEN
		assertThat(service).isNotNull();
	}

	@Test
	void testGetEventBus() {
		// GIVEN
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class).locale(Locale.US).build();
		actionFX.scanForActionFXComponents();

		// WHEN
		final PriorityAwareEventBus eventBus = actionFX.getEventBus();

		// THEN
		assertThat(eventBus).isNotNull().isInstanceOf(SimplePriorityAwareEventBus.class);
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
		assertThat(view).isSameAs(mockView);
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
	void testShowView_withViewId() {
		// GIVEN
		final BeanContainerFacade customBeanContainer = Mockito.mock(BeanContainerFacade.class);
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class)
				.beanContainer(customBeanContainer).build();
		actionFX.scanForActionFXComponents();
		final View mockView = Mockito.mock(View.class);
		when(customBeanContainer.getBean(ArgumentMatchers.eq("viewId"))).thenReturn(mockView);

		// WHEN
		actionFX.showView("viewId");

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
	@TestInFxThread
	void testShowView_withStageSupplied_withViewId() {
		// GIVEN
		final BeanContainerFacade customBeanContainer = Mockito.mock(BeanContainerFacade.class);
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class)
				.beanContainer(customBeanContainer).build();
		actionFX.scanForActionFXComponents();
		final View mockView = Mockito.mock(View.class);
		when(customBeanContainer.getBean(ArgumentMatchers.eq("viewId"))).thenReturn(mockView);

		final Stage stage = new Stage();

		// WHEN
		actionFX.showView("viewId", stage);

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
	void testShowViewAndWait_withViewId() {
		// GIVEN
		final BeanContainerFacade customBeanContainer = Mockito.mock(BeanContainerFacade.class);
		final ActionFX actionFX = ActionFX.builder().configurationClass(SampleApp.class)
				.beanContainer(customBeanContainer).build();
		actionFX.scanForActionFXComponents();
		final View mockView = Mockito.mock(View.class);
		when(customBeanContainer.getBean(ArgumentMatchers.eq("viewId"))).thenReturn(mockView);

		// WHEN
		actionFX.showViewAndWait("viewId");

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
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);
		when(controller.showConfirmationDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn(Boolean.TRUE);

		// WHEN
		assertThat(actionFX.showConfirmationDialog("Title", "HeaderText", "ContentText")).isEqualTo(Boolean.TRUE);

		// THEN
		verify(controller).showConfirmationDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"));
	}

	@Test
	void testShowWarningDialog() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);

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
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);

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
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);

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
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFolder = Files.createTempDirectory("junit").toFile();
		final File initialFolder = Files.createTempDirectory("junit").toFile();

		when(controller.showDirectoryChooserDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFolder);

		// WHEN
		assertThat(actionFX.showDirectoryChooserDialog("Title", initialFolder, owner)).isEqualTo(selectedFolder);

		// THEN
		verify(controller).showDirectoryChooserDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowFileOpenDialog_initialFileName_extensionFilter() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFile = Files.createTempFile("junit", "-tmp").toFile();
		final File initialFolder = Files.createTempFile("junit", "-tmp").toFile();
		final ExtensionFilter filter = new ExtensionFilter("Text Files", "*.txt");

		when(controller.showFileOpenDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.anyString(), ArgumentMatchers.any(ExtensionFilter.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFile);

		// WHEN
		assertThat(actionFX.showFileOpenDialog("Title", initialFolder, "initial.txt", filter, owner)).isEqualTo(selectedFile);

		// THEN
		verify(controller).showFileOpenDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq("initial.txt"), ArgumentMatchers.eq(filter), ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowFileOpenDialog() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFile = Files.createTempFile("junit", "-tmp").toFile();
		final File initialFolder = Files.createTempFile("junit", "-tmp").toFile();

		when(controller.showFileOpenDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFile);

		// WHEN
		assertThat(actionFX.showFileOpenDialog("Title", initialFolder, owner)).isEqualTo(selectedFile);

		// THEN
		verify(controller).showFileOpenDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowFileSaveDialog() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFile = Files.createTempFile("junit", "-tmp").toFile();
		final File initialFolder = Files.createTempFile("junit", "-tmp").toFile();

		when(controller.showFileSaveDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFile);

		// WHEN
		assertThat(actionFX.showFileSaveDialog("Title", initialFolder, owner)).isEqualTo(selectedFile);

		// THEN
		verify(controller).showFileSaveDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowFileSaveDialog_initialFileName_extensionFilter() throws IOException {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);
		final Window owner = Mockito.mock(Window.class);
		final File selectedFile = Files.createTempFile("junit", "-tmp").toFile();
		final File initialFolder = Files.createTempFile("junit", "-tmp").toFile();
		final ExtensionFilter filter = new ExtensionFilter("Text Files", "*.txt");

		when(controller.showFileSaveDialog(ArgumentMatchers.anyString(), ArgumentMatchers.any(File.class),
				ArgumentMatchers.anyString(), ArgumentMatchers.any(ExtensionFilter.class),
				ArgumentMatchers.any(Window.class))).thenReturn(selectedFile);

		// WHEN
		assertThat(actionFX.showFileSaveDialog("Title", initialFolder, "initial.txt", filter, owner)).isEqualTo(selectedFile);

		// THEN
		verify(controller).showFileSaveDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq(initialFolder),
				ArgumentMatchers.eq("initial.txt"), ArgumentMatchers.eq(filter), ArgumentMatchers.eq(owner));
	}

	@Test
	void testShowTextInputDialog() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);

		when(controller.showTextInputDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString())).thenReturn("Text");

		// WHEN
		assertThat(actionFX.showTextInputDialog("Title", "HeaderText", "ContentText")).isEqualTo("Text");

		// THEN
		verify(controller).showTextInputDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"));
	}

	@Test
	void testShowTextInputDialog_withDefaultText() {
		// GIVEN
		final ActionFX actionFX = Mockito.spy(ActionFX.builder().configurationClass(SampleApp.class).build());
		final DialogController controller = Mockito.mock(DialogController.class);
		when(actionFX.getBean(ArgumentMatchers.eq(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME)))
				.thenReturn(controller);

		when(controller.showTextInputDialog(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn("Text");

		// WHEN
		assertThat(actionFX.showTextInputDialog("Title", "HeaderText", "ContentText", "DefaultText")).isEqualTo("Text");

		// THEN
		verify(controller).showTextInputDialog(ArgumentMatchers.eq("Title"), ArgumentMatchers.eq("HeaderText"),
				ArgumentMatchers.eq("ContentText"), ArgumentMatchers.eq("DefaultText"));
	}

	public static class AppClassWithoutAFXApplicationAnnotation {

	}

	public static class Controller {
		public View _view;
	}

	public static class CustomControllerExtension implements Consumer<Object> {

		@Override
		public void accept(final Object t) {
		}
	}

	public static class CustomBeanExtension implements BeanExtension {

		@Override
		public void extendBean(final Class<?> beanClass, final String beanId, final boolean singleton,
				final boolean lazyInit) {
		}
	}

	public static class CustomBeanContainer extends DefaultActionFXBeanContainer {

	}
}
