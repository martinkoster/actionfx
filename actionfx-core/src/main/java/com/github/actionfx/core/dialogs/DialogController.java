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

import java.io.File;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Controller for working with simple dialogs like confirmation-, information-, warning-, error-, file handling- and
 * text input dialogs.
 *
 * @author koster
 *
 */
public class DialogController {

    /**
     * Displays a modal confirmation dialogue with the specified {@code title} and {@code message}.
     *
     * @param title
     *            the title of the dialog
     * @param headerText
     *            the header text to be displayed in the dialog
     * @param contentText
     *            the content text to be displayed in the dialog
     * @return {@code true}, when the OK button has been pressed, {@code false} otherwise.
     */
    public boolean showConfirmationDialog(final String title, final String headerText, final String contentText) {
        final Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.initModality(Modality.APPLICATION_MODAL);
        final Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Displays a modal warning dialogue with the specified {@code title} and {@code message}.
     *
     * @param title
     *            the title of the dialog
     * @param headerText
     *            the header text to be displayed in the dialog
     * @param contentText
     *            the content text to be displayed in the dialog
     */
    public void showWarningDialog(final String title, final String headerText, final String contentText) {
        final Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Displays a modal information dialogue with the specified {@code title} and {@code message}.
     *
     * @param title
     *            the title of the dialog
     * @param headerText
     *            the header text to be displayed in the dialog
     * @param contentText
     *            the content text to be displayed in the dialog
     */
    public void showInformationDialog(final String title, final String headerText, final String contentText) {
        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Displays a modal error dialogue with the specified {@code title} and {@code message}.
     *
     * @param title
     *            the title of the dialog
     * @param headerText
     *            the header text to be displayed in the dialog
     * @param contentText
     *            the content text to be displayed in the dialog
     */
    public void showErrorDialog(final String title, final String headerText, final String contentText) {
        final Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Displays a directory chooser and returns the selected {@link File} descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param owner
     *            the window owner
     * @return the selected directory, or {@code null}, if no directory has been selected
     */
    public File showDirectoryChooserDialog(final String title, final File defaultDirectory, final Window owner) {
        final DirectoryChooserWrapper chooser = createDirectoryChooserWrapper();
        chooser.setTitle(title);
        if (defaultDirectory != null) {
            chooser.setInitialDirectory(defaultDirectory);
        }
        return chooser.showDialog(owner);
    }

    /**
     * Displays a file chooser and returns the selected <tt>File</tt> descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param initialFileName
     *            the initially suggested file name
     * @param fileExtensionFilter
     *            an optional file extension filter
     * @param owner
     *            the window owner
     * @return the selected file, or {@code null} if no file has been selected
     */
    public File showFileOpenDialog(final String title, final File defaultDirectory, final String initialFileName,
            final ExtensionFilter fileExtensionFilter, final Window owner) {
        final FileChooserWrapper chooser = createFileChooserWrapper();
        chooser.setTitle(title);
        if (initialFileName != null) {
            chooser.setInitialFileName(initialFileName);
        }
        if (fileExtensionFilter != null) {
            chooser.addSelectedExtensionFilter(fileExtensionFilter);
        }
        if (defaultDirectory != null) {
            chooser.setInitialDirectory(defaultDirectory);
        }
        return chooser.showOpenDialog(owner);
    }

    /**
     * Displays a file chooser and returns the selected {@link File} descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param owner
     *            the window owner
     * @return the selected file, or {@code null}, if no file has been selected
     */
    public File showFileOpenDialog(final String title, final File defaultDirectory, final Window owner) {
        return showFileOpenDialog(title, defaultDirectory, null, null, owner);
    }

    /**
     * Displays a file chooser and returns the selected {@link File} descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param owner
     *            the window owner
     * @return the selected file, or {@code null}, if no file has been selected
     */
    public File showFileSaveDialog(final String title, final File defaultDirectory, final Window owner) {
        return showFileSaveDialog(title, defaultDirectory, null, null, owner);
    }

    /**
     * Displays a file chooser and returns the selected {@link File} descriptor.
     *
     * @param title
     *            the dialog title
     * @param defaultDirectory
     *            the directory to show, when the dialog is opened
     * @param initialFileName
     *            the initially suggested file name
     * @param fileExtensionFilter
     *            an optional file extension filter
     * @param owner
     *            the window owner
     * @return the selected file, or {@code null}, if no file has been selected
     */
    public File showFileSaveDialog(final String title, final File defaultDirectory, final String initialFileName,
            final ExtensionFilter fileExtensionFilter, final Window owner) {
        final FileChooserWrapper chooser = createFileChooserWrapper();
        chooser.setTitle(title);
        if (initialFileName != null) {
            chooser.setInitialFileName(initialFileName);
        }
        if (fileExtensionFilter != null) {
            chooser.addSelectedExtensionFilter(fileExtensionFilter);
        }
        if (defaultDirectory != null) {
            chooser.setInitialDirectory(defaultDirectory);
        }
        return chooser.showSaveDialog(owner);
    }

    /**
     * Displays a modal <tt>TextInputDialog</tt> that lets the user enter a single string value.
     *
     * @param title
     *            the title of the input dialog
     * @param headerText
     *            a header text to be displayed inside the dialogue
     * @param contentText
     *            a content text displayed in front of the input text field
     * @return the entered string value, or <tt>null</tt>, if no value has been entered.
     */
    public String showTextInputDialog(final String title, final String headerText, final String contentText) {
        return showTextInputDialog(title, headerText, contentText, null);
    }

    /**
     * Displays a modal <tt>TextInputDialog</tt> that lets the user enter a single string value. A default text is
     * already pre-set.
     *
     * @param title
     *            the title of the input dialog
     * @param headerText
     *            a header text to be displayed inside the dialogue
     * @param contentText
     *            a content text displayed in front of the input text field
     * @param defaultValue
     *            the pre-set default text
     * @return the entered string value, or <tt>null</tt>, if no value has been entered.
     */
    public String showTextInputDialog(final String title, final String headerText, final String contentText,
            final String defaultValue) {
        final TextInputDialog dialog = new TextInputDialog(defaultValue == null ? "" : defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        final Optional<String> result = dialog.showAndWait();
        return result.isPresent() ? result.get() : null;
    }

    /**
     * Creates a new {@link FileChooser}. Externalized in separate method in order to allow unit testing.
     *
     * @return a new file chooser instance
     */
    FileChooserWrapper createFileChooserWrapper() {
        return new FileChooserWrapper(new FileChooser());
    }

    /**
     * Creates a new {@link DirectoryChooser}. Externalized in separate method in order to allow unit testing.
     *
     * @return a new file chooser instance
     */
    DirectoryChooserWrapper createDirectoryChooserWrapper() {
        return new DirectoryChooserWrapper(new DirectoryChooser());
    }

    /**
     * Wrapper for file chooser dialogs in order to facilitate a unit testing without PowerMock. This is required,
     * because "showViewAndWait" and other methods are "final" and can not be intercepted by a testing framework.
     *
     * @author MartinKoster
     *
     */
    static class FileChooserWrapper {

        private final FileChooser wrappable;

        FileChooserWrapper(final FileChooser wrappable) {
            this.wrappable = wrappable;
        }

        File showOpenDialog(final Window ownerWindow) {
            return wrappable.showOpenDialog(ownerWindow);
        }

        File showSaveDialog(final Window ownerWindow) {
            return wrappable.showSaveDialog(ownerWindow);
        }

        FileChooser getWrappable() {
            return wrappable;
        }

        void setTitle(final String value) {
            wrappable.setTitle(value);
        }

        void setInitialDirectory(final File value) {
            wrappable.setInitialDirectory(value);
        }

        void setInitialFileName(final String value) {
            wrappable.setInitialFileName(value);
        }

        void addSelectedExtensionFilter(final ExtensionFilter filter) {
            wrappable.getExtensionFilters().add(filter);
        }

    }

    /**
     * Wrapper for directory chooser dialogs in order to facilitate a unit testing without PowerMock. This is required,
     * because "showViewAndWait" and other methods are "final" and can not be intercepted by a testing framework.
     *
     * @author MartinKoster
     *
     */
    static class DirectoryChooserWrapper {

        private final DirectoryChooser wrappable;

        DirectoryChooserWrapper(final DirectoryChooser wrappable) {
            this.wrappable = wrappable;
        }

        File showDialog(final Window ownerWindow) {
            return wrappable.showDialog(ownerWindow);
        }

        DirectoryChooser getWrappable() {
            return wrappable;
        }

        void setTitle(final String value) {
            wrappable.setTitle(value);
        }

        void setInitialDirectory(final File value) {
            wrappable.setInitialDirectory(value);
        }
    }
}
