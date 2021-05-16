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
package com.github.actionfx.texteditor.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

import javax.inject.Inject;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXFromFileOpenDialog;
import com.github.actionfx.core.annotation.AFXFromFileSaveDialog;
import com.github.actionfx.core.annotation.AFXFromTextInputDialog;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXRequiresUserConfirmation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * Controller for a simple text editor.
 * <p>
 * The demo shows how menus and dialogs like open-, save-, information- and
 * confirmation dialogs can be integrated with ActionFX.
 * <p>
 * Please note that the referenced menu items are not injected here via @FXML,
 * but these are of course preset in the view's scene graph.
 *
 * @author koster
 *
 */
@AFXController(viewId = "textEditorDemoView", fxml = "/fxml/TextEditor.fxml", maximized = true, title = "Text Editor")
public class TextEditorController {

	@FXML
	private TextArea editorTextArea;

	@Inject
	private ActionFX actionFX;

	@AFXOnAction(nodeId = "openFileMenuItem", async = true)
	public void openFile(@AFXFromFileOpenDialog(title = "Open Text File", extensionFilter = { "Text Files",
			"*.txt" }) final Path path) throws IOException {
		editorTextArea.clear();
		try (final Scanner scanner = new Scanner(path)) {
			scanner.useDelimiter(System.lineSeparator());
			while (scanner.hasNext()) {
				// since we activated "async=true", we are not inside the JavaFX thread.
				// So changing the state of UI components requires us using
				// Platform.runLater(..)
				final String line = scanner.next();
				Platform.runLater(() -> editorTextArea.appendText(line + System.lineSeparator()));
			}
		}
	}

	@AFXOnAction(nodeId = "saveFileMenuItem", async = true)
	public void saveFile(
			@AFXFromFileSaveDialog(title = "Save Text File", extensionFilter = { "Text Files",
					"*.txt" }) final File file,
			@AFXControlValue("editorTextArea") final String text) throws IOException {
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
			writer.write(text);
		}
		// show an information dialog that saving was successful
		Platform.runLater(() -> actionFX.showInformationDialog("Save successful",
				"File '" + file.getAbsolutePath() + "' has been successfully saved.", null));
	}

	@AFXOnAction(nodeId = "closeMenuItem")
	@AFXRequiresUserConfirmation(title = "Exit", header = "Exit Text Editor", content = "Are you sure you want to exit the Text Editor?")
	public void close() {
		Platform.exit();
	}

	@AFXOnAction(nodeId = "findMenuItem")
	public void find(
			@AFXFromTextInputDialog(title = "Find", header = "Search for text", content = "Please enter a text to search for") final String searchText,
			@AFXControlValue("editorTextArea") final String text) {
		final int beginIndex = text.indexOf(searchText);
		if (beginIndex > -1) {
			final int endIndex = beginIndex + searchText.length();
			editorTextArea.selectRange(beginIndex, endIndex);
		} else {
			actionFX.showInformationDialog("Find", "Text '" + searchText + "' has not been found!", null);
		}
	}

	@AFXOnAction(nodeId = "aboutMenuItem")
	public void about() {
		actionFX.showInformationDialog("About", "About Text Editor",
				"This is a simple Text Editor realized with ActionFX.");
	}
}
