/*
w * Copyright (c) 2022 Martin Koster
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
package com.github.actionfx.validation.controller;

import java.util.Arrays;
import java.util.List;

import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXValidateBoolean;
import com.github.actionfx.core.annotation.AFXValidateCustom;
import com.github.actionfx.core.annotation.AFXValidateMinMax;
import com.github.actionfx.core.annotation.AFXValidateRegExp;
import com.github.actionfx.core.annotation.AFXValidateRequired;
import com.github.actionfx.core.annotation.AFXValidateSize;
import com.github.actionfx.core.annotation.AFXValidateTemporal;
import com.github.actionfx.core.annotation.ValidationHelper;
import com.github.actionfx.core.validation.ValidationResult;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * ActionFX controller for showing validations on different controls.
 *
 * @author koster
 *
 */
@AFXController(viewId = "validationView", fxml = "/fxml/ValidationView.fxml", title = "ValidationView", width = 660, height = 450)
public class ValidationController {

    @AFXValidateBoolean(message = "Please confirms", expected = true)
    @FXML
    protected CheckBox checkbox;

    @AFXValidateRegExp(message = "Please enter a valid mail address", regExp = ValidationHelper.EMAIL_ADDRESS_REG_EXP, validationStartTimeoutMs = 300, required = true)
    @FXML
    protected TextField emailTextField;

    @AFXEnableMultiSelection
    @AFXValidateSize(message = "Please select at 2 elements", min = 2)
    @FXML
    protected ListView<String> entryListView;

    @AFXValidateTemporal(message = "Please enter a date in the future with pattern dd.MM.yyyy", future = true, formatPattern = "dd.MM.yyyy", validationStartTimeoutMs = 300)
    @FXML
    protected TextField futureTextField;

    @AFXValidateCustom(validationMethod = "customValidationMethod", validationStartTimeoutMs = 300)
    @FXML
    protected TextField helloWorldTextField;

    @AFXValidateSize(message = "Please enter a name of length 2 and 20", min = 2, max = 20, validationStartTimeoutMs = 300)
    @FXML
    protected TextField nameTextField;

    @AFXValidateMinMax(message = "Please enter a numerical value between 10 and 100", min = 10, max = 100, formatPattern = "#,###", validationStartTimeoutMs = 300)
    @FXML
    protected TextField numericalValueTextField;

    @AFXValidateTemporal(message = "Please select a date in the past", past = true)
    @FXML
    protected DatePicker pastDatePicker;

    @AFXValidateRequired(message = "This is a mandatory field.", validationStartTimeoutMs = 300)
    @FXML
    protected TextField reqiredTextField;

    @FXML
    protected Button validateButton;

    @AFXLoadControlData(controlId = "entryListView")
    public List<String> listViewData() {
        return Arrays.asList("Value 1", "Value 2", "Value 3", "Value 4");
    }

    public ValidationResult customValidationMethod(final String text) {
        return null;
    }

    @AFXOnAction(nodeId = "validateButton", async = false)
    public void validateButtonAction(final ActionEvent event) {
        // TODO: implement action method
    }

}
