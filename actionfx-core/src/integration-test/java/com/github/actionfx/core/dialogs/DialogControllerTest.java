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
package com.github.actionfx.core.dialogs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.matcher.control.TextInputControlMatchers;

import com.github.actionfx.core.dialogs.DialogController.DirectoryChooserWrapper;
import com.github.actionfx.core.dialogs.DialogController.FileChooserWrapper;
import com.github.actionfx.testing.junit5.FxThreadForEachMonocleExtension;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JUnit integration test case for {@link DialogController}.
 * <p>
 * This test case uses TestFX for triggering button clicks on an opened stage.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForEachMonocleExtension.class)
class DialogControllerTest {

	private final ObjectProperty<Object> returnValue = new SimpleObjectProperty<>();

	private Window owner;

	private FxRobot fxRobot;

	private DialogController controller;

	@BeforeEach
	void onSetup() {
		owner = Mockito.mock(Window.class);
		fxRobot = new FxRobot();
		controller = Mockito.spy(DialogController.class);
		// default we're in US locale: keep (re)setting this for each test
		// important for button labeling like "OK" and "Cancel"
		Locale.setDefault(Locale.US);
		returnValue.set(null);
	}

	@Test
	void testShowConfirmationDialog_userClicksOk() throws Exception {
		// GIVEN
		setupDialog(() -> controller.showConfirmationDialog("Title", "HeaderText", "ContentText"));

		// WHEN
		fxRobot.clickOn("#openDialog");

		// THEN
		final javafx.stage.Stage actualAlertDialog = getTopModalStage();
		assertThat(actualAlertDialog, notNullValue());

		final DialogPane dialogPane = (DialogPane) actualAlertDialog.getScene().getRoot();
		assertThat(actualAlertDialog.getTitle(), equalTo("Title"));
		assertThat(dialogPane.getHeaderText(), equalTo("HeaderText"));
		assertThat(dialogPane.getContentText(), equalTo("ContentText"));

		// AND WHEN
		fxRobot.clickOn("OK");

		// AND THEN
		assertThat(returnValue.get(), equalTo(Boolean.TRUE));
	}

	@Test
	void testShowConfirmationDialog_userClicksCancel() throws Exception {
		// GIVEN
		setupDialog(() -> controller.showConfirmationDialog("Title", "HeaderText", "ContentText"));

		// WHEN
		fxRobot.clickOn("#openDialog");

		// THEN
		final javafx.stage.Stage actualAlertDialog = getTopModalStage();
		assertThat(actualAlertDialog, notNullValue());

		final DialogPane dialogPane = (DialogPane) actualAlertDialog.getScene().getRoot();
		assertThat(actualAlertDialog.getTitle(), equalTo("Title"));
		assertThat(dialogPane.getHeaderText(), equalTo("HeaderText"));
		assertThat(dialogPane.getContentText(), equalTo("ContentText"));

		// AND WHEN
		fxRobot.clickOn("Cancel");

		// AND THEN
		assertThat(returnValue.get(), equalTo(Boolean.FALSE));
	}

	@Test
	void testShowWarningDialog() throws Exception {
		// GIVEN
		setupDialog(() -> {
			controller.showWarningDialog("Title", "HeaderText", "ContentText");
			return true;
		});

		// WHEN
		fxRobot.clickOn("#openDialog");

		// THEN
		final javafx.stage.Stage actualAlertDialog = getTopModalStage();
		assertThat(actualAlertDialog, notNullValue());

		final DialogPane dialogPane = (DialogPane) actualAlertDialog.getScene().getRoot();
		assertThat(actualAlertDialog.getTitle(), equalTo("Title"));
		assertThat(dialogPane.getHeaderText(), equalTo("HeaderText"));
		assertThat(dialogPane.getContentText(), equalTo("ContentText"));

		// close dialog again
		fxRobot.clickOn("OK");
	}

	@Test
	void testShowErrorDialog() throws Exception {
		// GIVEN
		setupDialog(() -> {
			controller.showErrorDialog("Title", "HeaderText", "ContentText");
			return true;
		});

		// WHEN
		fxRobot.clickOn("#openDialog");

		// THEN
		final javafx.stage.Stage actualAlertDialog = getTopModalStage();
		assertThat(actualAlertDialog, notNullValue());

		final DialogPane dialogPane = (DialogPane) actualAlertDialog.getScene().getRoot();
		assertThat(actualAlertDialog.getTitle(), equalTo("Title"));
		assertThat(dialogPane.getHeaderText(), equalTo("HeaderText"));
		assertThat(dialogPane.getContentText(), equalTo("ContentText"));

		// close dialog again
		fxRobot.clickOn("OK");
	}

	@Test
	void testShowInformationDialog() throws Exception {
		// GIVEN
		setupDialog(() -> {
			controller.showInformationDialog("Title", "HeaderText", "ContentText");
			return true;
		});

		// WHEN
		fxRobot.clickOn("#openDialog");

		// THEN
		final javafx.stage.Stage actualAlertDialog = getTopModalStage();
		assertThat(actualAlertDialog, notNullValue());

		final DialogPane dialogPane = (DialogPane) actualAlertDialog.getScene().getRoot();
		assertThat(actualAlertDialog.getTitle(), equalTo("Title"));
		assertThat(dialogPane.getHeaderText(), equalTo("HeaderText"));
		assertThat(dialogPane.getContentText(), equalTo("ContentText"));

		// close dialog again
		fxRobot.clickOn("OK");
	}

	@Test
	void testShowDirectoryChooserDialog_noDefaultDirectory() throws Exception {
		// GIVEN
		final DirectoryChooserWrapper wrapper = Mockito.mock(DirectoryChooserWrapper.class);
		setupDirectoryChooser(controller, wrapper);

		// WHEN
		controller.showDirectoryChooserDialog("Title", null, owner);

		// THEN
		verify(wrapper).setTitle(eq("Title"));
		verify(wrapper, never()).setInitialDirectory(ArgumentMatchers.nullable(File.class));
		verify(wrapper).showDialog(eq(owner));
	}

	@Test
	void testShowDirectoryChooserDialog_defaultDirectory() throws Exception {
		// GIVEN
		final DirectoryChooserWrapper wrapper = Mockito.mock(DirectoryChooserWrapper.class);
		setupDirectoryChooser(controller, wrapper);
		final File folder = Files.createTempDirectory("junit").toFile();

		// WHEN
		controller.showDirectoryChooserDialog("Title", folder, owner);

		// THEN
		verify(wrapper).setTitle(eq("Title"));
		verify(wrapper).setInitialDirectory(eq(folder));
		verify(wrapper).showDialog(eq(owner));
	}

	@Test
	void testShowFileOpenDialog_noDefaultDirectory() throws Exception {
		// GIVEN
		final FileChooserWrapper wrapper = Mockito.mock(FileChooserWrapper.class);
		setupFileChooser(controller, wrapper);

		// WHEN
		controller.showFileOpenDialog("Title", null, owner);

		// THEN
		verify(wrapper).setTitle(eq("Title"));
		verify(wrapper, never()).setInitialDirectory(ArgumentMatchers.any());
		verify(wrapper, never()).setInitialFileName(ArgumentMatchers.nullable(String.class));
		verify(wrapper).showOpenDialog(eq(owner));
	}

	@Test
	void testShowFileOpenDialog_defaultDirectory_initialFileName_extensionFilter() throws Exception {
		// GIVEN
		final FileChooserWrapper wrapper = Mockito.mock(FileChooserWrapper.class);
		setupFileChooser(controller, wrapper);
		final File folder = Files.createTempDirectory("junit").toFile();
		final ExtensionFilter filter = new ExtensionFilter("Text Files", "*.txt");

		// WHEN
		controller.showFileOpenDialog("Title", folder, "initial.txt", filter, owner);

		// THEN
		verify(wrapper).setTitle(eq("Title"));
		verify(wrapper).setInitialDirectory(eq(folder));
		verify(wrapper).setInitialFileName(eq("initial.txt"));
		verify(wrapper).setSelectedExtensionFilter(eq(filter));
		verify(wrapper).showOpenDialog(eq(owner));
	}

	@Test
	void testShowFileSaveDialog_noDefaultDirectory() throws Exception {
		// GIVEN
		final FileChooserWrapper wrapper = Mockito.mock(FileChooserWrapper.class);
		setupFileChooser(controller, wrapper);

		// WHEN
		controller.showFileSaveDialog("Title", null, owner);

		// THEN
		verify(wrapper).setTitle(eq("Title"));
		verify(wrapper, never()).setInitialDirectory(ArgumentMatchers.any());
		verify(wrapper, never()).setInitialFileName(ArgumentMatchers.nullable(String.class));
		verify(wrapper).showSaveDialog(eq(owner));
	}

	@Test
	void testShowFileSaveDialog_defaultDirectory_initialFileName_extensionFilter() throws Exception {
		// GIVEN
		final FileChooserWrapper wrapper = Mockito.mock(FileChooserWrapper.class);
		setupFileChooser(controller, wrapper);
		final File folder = Files.createTempDirectory("junit").toFile();
		final ExtensionFilter filter = new ExtensionFilter("Text Files", "*.txt");

		// WHEN
		controller.showFileSaveDialog("Title", folder, "initial.txt", filter, owner);

		// THEN
		verify(wrapper).setTitle(eq("Title"));
		verify(wrapper).setInitialDirectory(eq(folder));
		verify(wrapper).setInitialFileName(eq("initial.txt"));
		verify(wrapper).setSelectedExtensionFilter(eq(filter));
		verify(wrapper).showSaveDialog(eq(owner));
	}

	@Test
	void testShowTextInputDialog() throws Exception {
		// GIVEN
		setupDialog(() -> controller.showTextInputDialog("Title", "HeaderText", "ContentText"));

		// WHEN
		fxRobot.clickOn("#openDialog");

		// THEN
		final javafx.stage.Stage actualAlertDialog = getTopModalStage();
		assertThat(actualAlertDialog, notNullValue());

		final DialogPane dialogPane = (DialogPane) actualAlertDialog.getScene().getRoot();
		assertThat(actualAlertDialog.getTitle(), equalTo("Title"));
		assertThat(dialogPane.getHeaderText(), equalTo("HeaderText"));
		assertThat(dialogPane.getContentText(), equalTo("ContentText"));
		FxAssert.verifyThat(".text-field", TextInputControlMatchers.hasText(""));

		// close dialog again
		fxRobot.clickOn("OK");
	}

	@Test
	void testShowTextInputDialog_withDefaultValue() throws Exception {
		// GIVEN
		setupDialog(() -> controller.showTextInputDialog("Title", "HeaderText", "ContentText", "Default Value"));

		// WHEN
		fxRobot.clickOn("#openDialog");

		// THEN
		final javafx.stage.Stage actualAlertDialog = getTopModalStage();
		assertThat(actualAlertDialog, notNullValue());

		final DialogPane dialogPane = (DialogPane) actualAlertDialog.getScene().getRoot();
		assertThat(actualAlertDialog.getTitle(), equalTo("Title"));
		assertThat(dialogPane.getHeaderText(), equalTo("HeaderText"));
		assertThat(dialogPane.getContentText(), equalTo("ContentText"));
		FxAssert.verifyThat(".text-field", TextInputControlMatchers.hasText("Default Value"));

		// close dialog again
		fxRobot.clickOn("OK");
		assertThat(returnValue.get(), equalTo("Default Value"));
	}

	@Test
	void testShowTextInputDialog_withDefaultValue_userCancels() throws Exception {
		// GIVEN
		setupDialog(() -> controller.showTextInputDialog("Title", "HeaderText", "ContentText", "Default Value"));

		// WHEN
		fxRobot.clickOn("#openDialog");

		// THEN
		final javafx.stage.Stage actualAlertDialog = getTopModalStage();
		assertThat(actualAlertDialog, notNullValue());

		final DialogPane dialogPane = (DialogPane) actualAlertDialog.getScene().getRoot();
		assertThat(actualAlertDialog.getTitle(), equalTo("Title"));
		assertThat(dialogPane.getHeaderText(), equalTo("HeaderText"));
		assertThat(dialogPane.getContentText(), equalTo("ContentText"));
		FxAssert.verifyThat(".text-field", TextInputControlMatchers.hasText("Default Value"));

		// close dialog again
		fxRobot.clickOn("Cancel");
		assertThat(returnValue.get(), nullValue());
	}

	/**
	 * Defines an expected behaviour for a dialog.
	 */
	private <T> void setupDialog(final Supplier<T> dialogCallback) throws Exception {
		final Button openDialogButton = new Button("Open Dialog");
		openDialogButton.setId("openDialog");
		openDialogButton.setOnAction(event -> {
			returnValue.set(dialogCallback.get());
		});
		final AnchorPane root = new AnchorPane(openDialogButton);
		root.setPrefSize(500, 500);
		FxToolkit.setupSceneRoot(() -> root);
		FxToolkit.setupStage(Stage::show);
	}

	/**
	 * Sets up a mock for {@link FileChooser} as it can not be handled via TestFX.
	 */
	private void setupFileChooser(final DialogController controller, final FileChooserWrapper wrapper) {
		when(controller.createFileChooserWrapper()).thenReturn(wrapper);
	}

	/**
	 * Sets up a mock for {@link FileChooser} as it can not be handled via TestFX.
	 */
	private void setupDirectoryChooser(final DialogController controller, final DirectoryChooserWrapper wrapper) {
		when(controller.createDirectoryChooserWrapper()).thenReturn(wrapper);
	}

	private javafx.stage.Stage getTopModalStage() {
		// Get a list of windows but ordered from top[0] to bottom[n] ones.
		// It is needed to get the first found modal window.
		final List<Window> allWindows = new ArrayList<>(fxRobot.robotContext().getWindowFinder().listWindows());
		Collections.reverse(allWindows);

		return (javafx.stage.Stage) allWindows.stream().filter(window -> window instanceof javafx.stage.Stage)
				.filter(window -> ((javafx.stage.Stage) window).getModality() == Modality.APPLICATION_MODAL).findFirst()
				.orElse(null);
	}
}
