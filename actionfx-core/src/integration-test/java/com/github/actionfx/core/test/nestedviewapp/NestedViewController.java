package com.github.actionfx.core.test.nestedviewapp;

import java.util.Locale;

import javax.inject.Inject;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.view.BorderPanePosition;

import javafx.beans.value.ObservableValue;

@AFXNestedView(refViewId = "nestedTabPaneView", attachToNodeWithId = "mainBorderPane", attachToBorderPanePosition = BorderPanePosition.CENTER)
@AFXController(viewId = "mainId", fxml = "/testfxml/ViewWithNestedView.fxml", resourcesBasename = "i18n.I18N")
public class NestedViewController {

	@Inject
	private Locale locale;

	@Inject
	private ObservableValue<Locale> observableLocale;

	@Inject
	private ActionFX actionFX;

	@Inject
	private DialogController dialogController;

	public Locale getLocale() {
		return locale;
	}

	public ObservableValue<Locale> getObservableLocale() {
		return observableLocale;
	}

	public ActionFX getActionFX() {
		return actionFX;
	}

	public DialogController getDialogController() {
		return dialogController;
	}

}