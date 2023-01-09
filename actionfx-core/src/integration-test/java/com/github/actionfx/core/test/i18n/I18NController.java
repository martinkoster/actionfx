package com.github.actionfx.core.test.i18n;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXValidateRegExp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

@AFXController(viewId = "i18NView", fxml = "/testfxml/I18NView.fxml", resourcesBasename = "i18n.I18N")
public class I18NController {

	@AFXValidateRegExp(regExp = "[a-zA-Z]", messageKey = "validation.onlycharactersallowed", validationStartTimeoutMs = 0)
	@FXML
	public Label textLabel;

}