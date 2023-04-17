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
package com.github.actionfx.core.view;

import static com.github.actionfx.core.test.utils.TestUtils.assertControlHasUserValue;
import static com.github.actionfx.core.test.utils.TestUtils.enterValue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXShowView;
import com.github.actionfx.core.annotation.ValidationMode;
import com.github.actionfx.core.bind.BindingTargetResolver;
import com.github.actionfx.core.bind.MappingBasedBindingTargetResolver;
import com.github.actionfx.core.container.instantiation.MultilingualViewController;
import com.github.actionfx.core.decoration.Decoration;
import com.github.actionfx.core.decoration.DecorationUtils;
import com.github.actionfx.core.decoration.GraphicDecoration;
import com.github.actionfx.core.decoration.StyleClassDecoration;
import com.github.actionfx.core.validation.ValidationMessage;
import com.github.actionfx.core.validation.ValidationOptions;
import com.github.actionfx.core.validation.ValidationResult;
import com.github.actionfx.core.validation.ValidationStatus;
import com.github.actionfx.core.validation.Validator;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.core.view.graph.NodeWrapper;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Popup;
import javafx.stage.Stage;

@ExtendWith(FxThreadForAllMonocleExtension.class)
class FxmlViewTest {

	@AfterEach
	void onTearDown() {
		resetActionFX();
	}

	@Test
	void testFxmlView() {
		// WHEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());

		// THEN
		assertThat(view.getId(), equalTo("testId"));
		assertThat(view.getController(), instanceOf(TestController.class));
		assertThat(view.getRootNode(), notNullValue());
		assertThat(view.getRootNode(), instanceOf(GridPane.class));
	}

	@Test
	void testFxmlView_internationalized() {
		// GIVEN
		final Locale locale = new Locale("de", "DE");
		final ResourceBundle bundle = ResourceBundle.getBundle("i18n.TestResources", locale);

		// WHEN
		final FxmlView view = new FxmlView("multilingualView", "/testfxml/MultilingualView.fxml",
				new MultilingualViewController(), bundle);

		// THEN
		assertThat(view.getId(), equalTo("multilingualView"));
		assertThat(view.getController(), instanceOf(MultilingualViewController.class));
		final MultilingualViewController controller = (MultilingualViewController) view.getController();
		assertThat(controller.getLabel().getText(), equalTo("Hallo Welt"));
	}

	@Test
	@TestInFxThread
	void testShow() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());

		// WHEN
		view.show();

		// THEN
		assertThat(view.getWindow(), notNullValue());
		assertThat(view.getWindow(), instanceOf(Stage.class));
	}

	@Test
	@TestInFxThread
	void testShow_withStage() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final Stage stage = new Stage();

		// WHEN
		view.show(stage);

		// THEN
		assertThat(view.getWindow(), notNullValue());
		assertThat(view.getWindow(), sameInstance(stage));
		assertThat(stage.getScene(), notNullValue());
		assertThat(stage.getScene().getRoot(), sameInstance(view.getRootNode()));
	}

	@Test
	@TestInFxThread
	void testShow_withPopup() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final Stage owner = new Stage();
		final Popup popup = new Popup();

		// WHEN
		view.show(popup, owner);

		// THEN
		assertThat(view.getWindow(), notNullValue());
		assertThat(view.getWindow(), sameInstance(popup));
		assertThat(popup.getContent(), hasSize(1));
		assertThat(popup.getContent().get(0), sameInstance(view.getRootNode()));
	}

	@Test
	@TestInFxThread
	void testHide() {
		// GIVEN
		final Stage stage = Mockito.spy(Stage.class);
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		view.show(stage);

		// WHEN
		view.hide();

		// THEN
		verify(stage, times(1)).hide();
	}

	@Test
	void testAttachViewToParent() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final AnchorPane parent = new AnchorPane();

		// WHEN
		view.attachViewToParent(parent, NodeWrapper.anchorPaneFillingAttacher());

		// THEN
		assertThat(parent.getChildren(), hasSize(1));
		assertThat(parent.getChildren().get(0), sameInstance(view.getRootNode()));
	}

	@Test
	void testDetachView_parentSupportsMultipleChildren() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final AnchorPane parent = new AnchorPane();
		parent.getChildren().add(view.getRootNode());

		// WHEN
		view.detachView();

		// THEN
		assertThat(parent.getChildren(), hasSize(0));
	}

	@Test
	void testReattachView() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());
		final AnchorPane parent = new AnchorPane();
		view.attachViewToParent(parent, NodeWrapper.anchorPaneFillingAttacher());
		view.detachView();
		assertThat(parent.getChildren(), hasSize(0));

		// WHEN
		view.reattachView();

		// THEN
		assertThat(parent.getChildren(), hasSize(1));
		assertThat(parent.getChildren().get(0), sameInstance(view.getRootNode()));
	}

	@Test
	void testReattachView_viewHasNotBeenAttachedBefore() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleView.fxml", new TestController());

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> view.reattachView());

		// THEN
		assertThat(ex.getMessage(), containsString("Can not re-attach view"));
	}

	@Test
	void testLookupNode_nodeIsCached() {
		// GIVEN
		final FxmlView view = new FxmlView("testId", "/testfxml/SampleViewWithNodeId.fxml", new TestController());
		assertThat(view.lookupCache.entrySet(), hasSize(0));

		// WHEN
		final NodeWrapper textFieldWrapper = view.lookupNode("textField");

		// THEN
		assertThat(textFieldWrapper, notNullValue());
		assertThat(textFieldWrapper.getWrapped(), instanceOf(TextField.class));
		// check that the node is now cached!
		assertThat(view.lookupCache.get("textField"), equalTo(textFieldWrapper));
	}

	@Test
	void testBind() {
		// GIVEN
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final CustomerModel model = createCustomerModel();
		final BindingTargetResolver resolver = createBindingTargetResolver();

		// WHEN
		view.bind(model, resolver);

		// THEN
		assertControlHasUserValue(view, "firstNameTextField", "Please enter first name");
		assertControlHasUserValue(view, "lastNameTextField", "Please enter last name");
		assertControlHasUserValue(view, "countryChoiceBox", "Germany");
		assertControlHasUserValue(view, "streetTextField", "Please enter street");
		assertControlHasUserValue(view, "postalCodeTextField", "Please enter postal code");
		assertControlHasUserValue(view, "cityTextField", "Please enter city");
		assertControlHasUserValue(view, "shoppingCartListView", Arrays.asList("Item 1", "Item 2", "Item 3"));

		// check binding by entering values in the controls
		enterValue(view, "firstNameTextField", "John");
		enterValue(view, "lastNameTextField", "Doe");
		enterValue(view, "countryChoiceBox", "USA");
		enterValue(view, "streetTextField", "1600 Pennsylvania Avenue");
		enterValue(view, "postalCodeTextField", "20500");
		enterValue(view, "cityTextField", "Washington");
		enterValue(view, "shoppingCartListView", Arrays.asList("Item 4", "Item 5"));

		// check that model has the entered values reflected
		assertThat(model.getFirstName(), equalTo("John"));
		assertThat(model.getLastName(), equalTo("Doe"));
		assertThat(model.getCountry(), equalTo("USA"));
		assertThat(model.getStreet(), equalTo("1600 Pennsylvania Avenue"));
		assertThat(model.getPostalCode(), equalTo("20500"));
		assertThat(model.getCity(), equalTo("Washington"));
		assertThat(model.getSelectedProducts(), contains("Item 4", "Item 5"));
	}

	@Test
	void testUnbind() {
		// GIVEN
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final CustomerModel model = createCustomerModel();
		final BindingTargetResolver resolver = createBindingTargetResolver();
		view.bind(model, resolver);

		// WHEN
		view.unbind(model);

		// THEN
		assertControlHasUserValue(view, "firstNameTextField", "Please enter first name");
		assertControlHasUserValue(view, "lastNameTextField", "Please enter last name");
		assertControlHasUserValue(view, "countryChoiceBox", "Germany");
		assertControlHasUserValue(view, "streetTextField", "Please enter street");
		assertControlHasUserValue(view, "postalCodeTextField", "Please enter postal code");
		assertControlHasUserValue(view, "cityTextField", "Please enter city");
		assertControlHasUserValue(view, "shoppingCartListView", Arrays.asList("Item 1", "Item 2", "Item 3"));

		// check binding by entering values in the controls
		enterValue(view, "firstNameTextField", "John");
		enterValue(view, "lastNameTextField", "Doe");
		enterValue(view, "countryChoiceBox", "USA");
		enterValue(view, "streetTextField", "1600 Pennsylvania Avenue");
		enterValue(view, "postalCodeTextField", "20500");
		enterValue(view, "cityTextField", "Washington");
		enterValue(view, "shoppingCartListView", Arrays.asList("Item 4", "Item 5"));

		// check that model has the old values, as model is no longer bound
		assertThat(model.getFirstName(), equalTo("Please enter first name"));
		assertThat(model.getLastName(), equalTo("Please enter last name"));
		assertThat(model.getCountry(), equalTo("Germany"));
		assertThat(model.getStreet(), equalTo("Please enter street"));
		assertThat(model.getPostalCode(), equalTo("Please enter postal code"));
		assertThat(model.getCity(), equalTo("Please enter city"));
		assertThat(model.getSelectedProducts(), contains("Item 1", "Item 2", "Item 3"));
	}

	@Test
	void testUnbindAll() {
		// GIVEN
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final CustomerModel model = createCustomerModel();
		final BindingTargetResolver resolver = createBindingTargetResolver();
		view.bind(model, resolver);

		// WHEN
		view.unbindAll();

		// THEN
		assertControlHasUserValue(view, "firstNameTextField", "Please enter first name");
		assertControlHasUserValue(view, "lastNameTextField", "Please enter last name");
		assertControlHasUserValue(view, "countryChoiceBox", "Germany");
		assertControlHasUserValue(view, "streetTextField", "Please enter street");
		assertControlHasUserValue(view, "postalCodeTextField", "Please enter postal code");
		assertControlHasUserValue(view, "cityTextField", "Please enter city");
		assertControlHasUserValue(view, "shoppingCartListView", Arrays.asList("Item 1", "Item 2", "Item 3"));

		// check binding by entering values in the controls
		enterValue(view, "firstNameTextField", "John");
		enterValue(view, "lastNameTextField", "Doe");
		enterValue(view, "countryChoiceBox", "USA");
		enterValue(view, "streetTextField", "1600 Pennsylvania Avenue");
		enterValue(view, "postalCodeTextField", "20500");
		enterValue(view, "cityTextField", "Washington");
		enterValue(view, "shoppingCartListView", Arrays.asList("Item 4", "Item 5"));

		// check that model has the old values, as model is no longer bound
		assertThat(model.getFirstName(), equalTo("Please enter first name"));
		assertThat(model.getLastName(), equalTo("Please enter last name"));
		assertThat(model.getCountry(), equalTo("Germany"));
		assertThat(model.getStreet(), equalTo("Please enter street"));
		assertThat(model.getPostalCode(), equalTo("Please enter postal code"));
		assertThat(model.getCity(), equalTo("Please enter city"));
		assertThat(model.getSelectedProducts(), contains("Item 1", "Item 2", "Item 3"));
	}

	@Test
	@TestInFxThread
	void testRegisterValidator_onChange_noDelayedValidation_noRequiredDecoration() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final TextField textField = view.lookupNode("lastNameTextField").getWrapped();
		final Validator validator = validatorProducingValidationResultsForControl(textField,
				ControlProperties.USER_VALUE_OBSERVABLE,
				ValidationResult.builder().addErrorMessage("Error message", textField, true));

		// WHEN
		view.registerValidator("lastNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options().validationMode(ValidationMode.ONCHANGE));
		textField.setText("Hello there");

		// THEN
		assertThat(view.getValidationResult().getStatus(), equalTo(ValidationStatus.ERROR));
		assertThatValidationResultHoldsMessagesWithText(view.getValidationResult(), "Error message");
		assertThat(ControlWrapper.of(textField).isRequired(), equalTo(false));
	}

	@Test
	@TestInFxThread
	void testRegisterValidator_onChange_noDelayedValidation_requiredDecorationIsApplied() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final TextField textField = view.lookupNode("lastNameTextField").getWrapped();
		final Validator validator = validatorProducingValidationResultsForControl(textField,
				ControlProperties.USER_VALUE_OBSERVABLE, ValidationResult.builder());

		// WHEN
		view.registerValidator("lastNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options().required(true).validationMode(ValidationMode.ONCHANGE));
		textField.setText("Hello there");

		// THEN
		assertThat(view.getValidationResult().getStatus(), equalTo(ValidationStatus.OK));
		assertThat(ControlWrapper.of(textField).isRequired(), equalTo(true));
		assertThatNodeHasDecorationsOfType(textField, GraphicDecoration.class);
	}

	@Test
	@TestInFxThread
	void testRegisterValidator_onChange_noDelayedValidation_requiredDecorationAndErrorDecorationIsApplied() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final TextField textField = view.lookupNode("lastNameTextField").getWrapped();
		final Validator validator = validatorProducingValidationResultsForControl(textField,
				ControlProperties.USER_VALUE_OBSERVABLE,
				ValidationResult.builder().addErrorMessage("Error message", textField, true));

		// WHEN
		view.registerValidator("lastNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options().required(true).validationMode(ValidationMode.ONCHANGE));
		textField.setText("Hello there");

		// THEN
		assertThatValidationStatusIsERROR(view.getValidationResult());
		assertThatValidationResultHoldsMessagesWithText(view.getValidationResult(), "Error message");
		assertThat(ControlWrapper.of(textField).isRequired(), equalTo(true));
		assertThatNodeHasDecorationsOfType(textField, GraphicDecoration.class, GraphicDecoration.class,
				StyleClassDecoration.class);
	}

	@Test
	@TestInFxThread
	void testRegisterValidator_onChange_noDelayedValidation_errorValidationDecorationIsApplied() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final TextField textField = view.lookupNode("lastNameTextField").getWrapped();
		final Validator validator = validatorProducingValidationResultsForControl(textField,
				ControlProperties.USER_VALUE_OBSERVABLE, ValidationResult.builder().addErrorMessage("Error message", // ERROR
																														// (onChange)
						textField, true));

		// WHEN register
		view.registerValidator("lastNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options().validationMode(ValidationMode.ONCHANGE));
		// THEN everything is OK
		assertThatValidationStatusIsOK(view.getValidationResult());

		// AND WHEN a change in the control occurs
		textField.setText("New Text");

		// THEN an error is registered
		assertThatValidationStatusIsERROR(view.getValidationResult());
		assertThatValidationResultHoldsMessagesWithText(view.getValidationResult(), "Error message");
		assertThatNodeHasDecorationsOfType(textField, GraphicDecoration.class, StyleClassDecoration.class);
	}

	@Test
	@TestInFxThread
	void testRegisterValidator_onChange_noDelayedValidation_errorValidationDecorationIsDisabled() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final TextField textField = view.lookupNode("lastNameTextField").getWrapped();
		final Validator validator = validatorProducingValidationResultsForControl(textField,
				ControlProperties.USER_VALUE_OBSERVABLE, ValidationResult.builder().addErrorMessage("Error message", // ERROR
																														// (onChange)
						textField, true));

		// WHEN register
		view.registerValidator("lastNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options().validationMode(ValidationMode.ONCHANGE)
						.applyValidationResultDecorations(false));
		// THEN everything is OK
		assertThatValidationStatusIsOK(view.getValidationResult());

		// AND WHEN a change in the control occurs
		textField.setText("New Text");

		// THEN an error is registered, but node has no decoration
		assertThatValidationStatusIsERROR(view.getValidationResult());
		assertThatValidationResultHoldsMessagesWithText(view.getValidationResult(), "Error message");
		assertThatNodeHasNoDecorations(textField);
	}

	@Test
	void testRegisterValidator_onChange_delayedValidation_errorValidationDecorationIsApplied() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final TextField textField = view.lookupNode("lastNameTextField").getWrapped();
		final Validator validator = validatorProducingValidationResultsForControl(textField,
				ControlProperties.USER_VALUE_OBSERVABLE, ValidationResult.builder().addErrorMessage("Error message", // ERROR
																														// (onChange)
						textField, true));

		// WHEN register
		view.registerValidator("lastNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options().validationMode(ValidationMode.ONCHANGE).validationStartTimeoutMs(100));
		// THEN everything is OK
		assertThatValidationStatusIsOK(view.getValidationResult());

		// AND WHEN a change in the control occurs
		textField.setText("New Text");
		WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);

		// THEN an error is registered
		assertThatValidationStatusIsERROR(view.getValidationResult());
		assertThatValidationResultHoldsMessagesWithText(view.getValidationResult(), "Error message");
		assertThatNodeHasDecorationsOfType(textField, GraphicDecoration.class, StyleClassDecoration.class);
	}

	@Test
	@TestInFxThread
	void testRegisterValidator_onChange_noDelayedValidation_onObservableList_onUserValue() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final ListView<String> shoppingCartListView = controller.shoppingCartListView;

		final Validator validator = validatorProducingValidationResultsForControl(shoppingCartListView,
				ControlProperties.USER_VALUE_OBSERVABLE, ValidationResult.builder().addErrorMessage("Error message", // ERROR
																														// (onChange)
						shoppingCartListView, true));

		// WHEN register
		view.registerValidator("shoppingCartListView", ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options().validationMode(ValidationMode.ONCHANGE));
		// THEN everything is OK
		assertThatValidationStatusIsOK(view.getValidationResult());

		// AND WHEN a change in the control occurs
		shoppingCartListView.getSelectionModel().select("Item 1");

		// THEN an error is registered
		assertThatValidationStatusIsERROR(view.getValidationResult());
		assertThatValidationResultHoldsMessagesWithText(view.getValidationResult(), "Error message");
	}

	@Test
	@TestInFxThread
	void testRegisterValidator_withGlobalValidationModeOnChange_noDelayedValidation() {
		// GIVEN
		givenActionFXSpecifiesValidationMode(ValidationMode.ONCHANGE);
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();
		final TextField textField = view.lookupNode("lastNameTextField").getWrapped();
		final Validator validator = validatorProducingValidationResultsForControl(textField,
				ControlProperties.USER_VALUE_OBSERVABLE, ValidationResult.builder().addErrorMessage("Error message", // ERROR
																														// (onChange)
						textField, true));

		// WHEN register
		view.registerValidator("lastNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options()); // no validation mode specified here, take the global validation mode

		// THEN everything is OK
		assertThatValidationStatusIsOK(view.getValidationResult());

		// AND WHEN a change in the control occurs
		textField.setText("New Text");

		// THEN an error is registered
		assertThatValidationStatusIsERROR(view.getValidationResult());
		assertThatValidationResultHoldsMessagesWithText(view.getValidationResult(), "Error message");
	}

	@Test
	@TestInFxThread
	void testValidate_withValidationDecorationsApplied() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();

		final Control lastNameTextField = view.lookupNode("lastNameTextField").getWrapped();
		registerValidatorWithControl(view, lastNameTextField,
				ValidationResult.builder().addErrorMessage("Error message 1", // ERROR (onValidate)
						lastNameTextField, true));

		final Control firstNameTextField = view.lookupNode("firstNameTextField").getWrapped();
		registerValidatorWithControl(view, firstNameTextField,
				ValidationResult.builder().addErrorMessage("Error message 2", // ERROR (onValidate)
						firstNameTextField, true));
		assertThatValidationStatusIsOK(view.getValidationResult());

		// WHEN an explicite validation is requested
		final ValidationResult validationResult = view.validate();

		// THEN two errors are registered
		assertThatValidationStatusIsERROR(validationResult);
		assertThatValidationResultHoldsMessagesWithText(validationResult, "Error message 1", "Error message 2");
		assertThatNodeHasDecorationsOfType(validationResult.getMessages().get(0).getTarget(),
				StyleClassDecoration.class, GraphicDecoration.class);
		assertThatNodeHasDecorationsOfType(validationResult.getMessages().get(1).getTarget(),
				StyleClassDecoration.class, GraphicDecoration.class);
	}

	@Test
	@TestInFxThread
	void testValidate_withValidationDecorationsDisabled() {
		// GIVEN
		givenActionFXIsSetupWithDefaults();
		final CustomerController controller = new CustomerController();
		final FxmlView view = new FxmlView("testId", "/testfxml/CustomerForm.fxml", controller);
		controller.init();

		final Control lastNameTextField = view.lookupNode("lastNameTextField").getWrapped();
		registerValidatorWithControl(view, lastNameTextField,
				ValidationResult.builder().addErrorMessage("Error message 1", // ERROR (onValidate)
						lastNameTextField, true));

		final Control firstNameTextField = view.lookupNode("firstNameTextField").getWrapped();
		registerValidatorWithControl(view, firstNameTextField,
				ValidationResult.builder().addErrorMessage("Error message 2", // ERROR (onValidate)
						firstNameTextField, true));
		assertThatValidationStatusIsOK(view.getValidationResult());

		// WHEN an explicite validation is requested
		final ValidationResult validationResult = view.validate(false);

		// THEN two errors are registered
		assertThatValidationStatusIsERROR(validationResult);
		assertThatValidationResultHoldsMessagesWithText(validationResult, "Error message 1", "Error message 2");
		assertThatNodeHasNoDecorations(validationResult.getMessages().get(0).getTarget());
		assertThatNodeHasNoDecorations(validationResult.getMessages().get(1).getTarget());
	}

	private void registerValidatorWithControl(final FxmlView view, final Control control, final ValidationResult result,
			final ValidationResult... results) {
		final Validator validator = validatorProducingValidationResultsForControl(control,
				ControlProperties.USER_VALUE_OBSERVABLE, result, results);
		view.registerValidator(control, ControlProperties.USER_VALUE_OBSERVABLE, validator,
				ValidationOptions.options());
	}

	private Validator validatorProducingValidationResultsForControl(final Control control,
			final ControlProperties controlProperty, final ValidationResult result, final ValidationResult... results) {

		final Validator validator = Mockito.mock(Validator.class);
		when(validator.validate(control, controlProperty)).thenReturn(result, results);
		return validator;
	}

	private void assertThatValidationResultHoldsMessagesWithText(final ValidationResult validationResult,
			final String... messages) {
		assertThat(validationResult, notNullValue());
		assertThat(validationResult.getMessages(), hasSize(messages.length));
		assertThat(validationResult.getMessages().stream().map(ValidationMessage::getText).collect(Collectors.toList()),
				containsInAnyOrder(messages));
	}

	private void assertThatValidationStatusIsOK(final ValidationResult validationResult) {
		if (validationResult != null) {
			assertThat(validationResult.getStatus(), equalTo(ValidationStatus.OK));
		}
	}

	private void assertThatValidationStatusIsERROR(final ValidationResult validationResult) {
		assertThat(validationResult, notNullValue());
		assertThat(validationResult.getStatus(), equalTo(ValidationStatus.ERROR));
	}

	private void assertThatNodeHasDecorationsOfType(final Node node, final Class<?>... decorationTypes) {
		final List<Decoration> nodeDecorations = DecorationUtils.getDecorations(node);
		assertThat(nodeDecorations, hasSize(decorationTypes.length));
		assertThat(nodeDecorations.stream().map(Decoration::getClass).collect(Collectors.toList()),
				hasItems(decorationTypes));
	}

	private void assertThatNodeHasNoDecorations(final Node node) {
		final List<Decoration> nodeDecorations = DecorationUtils.getDecorations(node);
		assertThat(nodeDecorations, hasSize(0));
	}

	private void givenActionFXSpecifiesValidationMode(final ValidationMode validationMode) {
		ActionFX.builder().validationGlobalMode(validationMode).build();
	}

	private void givenActionFXIsSetupWithDefaults() {
		ActionFX.builder().build();
	}

	private void resetActionFX() {
		if (ActionFX.isConfigured()) {
			ActionFX.getInstance().reset();
		}
	}

	private CustomerModel createCustomerModel() {
		final CustomerModel model = new CustomerModel();
		model.setFirstName("Please enter first name");
		model.setLastName("Please enter last name");
		model.setCountry("Germany");
		model.setStreet("Please enter street");
		model.setPostalCode("Please enter postal code");
		model.setCity("Please enter city");
		model.getSelectedProducts().addAll(Arrays.asList("Item 1", "Item 2", "Item 3"));
		return model;
	}

	private BindingTargetResolver createBindingTargetResolver() {
		final MappingBasedBindingTargetResolver resolver = new MappingBasedBindingTargetResolver(false);
		resolver.registerMapping("firstNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, "firstName");
		resolver.registerMapping("lastNameTextField", ControlProperties.USER_VALUE_OBSERVABLE, "lastName");
		resolver.registerMapping("countryChoiceBox", ControlProperties.USER_VALUE_OBSERVABLE, "country");
		resolver.registerMapping("streetTextField", ControlProperties.USER_VALUE_OBSERVABLE, "street");
		resolver.registerMapping("postalCodeTextField", ControlProperties.USER_VALUE_OBSERVABLE, "postalCode");
		resolver.registerMapping("cityTextField", ControlProperties.USER_VALUE_OBSERVABLE, "city");
		resolver.registerMapping("shoppingCartListView", ControlProperties.USER_VALUE_OBSERVABLE, "selectedProducts");
		return resolver;
	}

	/**
	 * Test controller that holds the {@link AFXController} and {@link AFXShowView}
	 * annotations.
	 *
	 * @author koster
	 *
	 */
	@AFXController(viewId = "testId", fxml = "/testfxml/SampleView.fxml", icon = "icon.png", singleton = true, maximized = true, modal = false, title = "Hello World", width = 100, height = 50, posX = 10, posY = 20, stylesheets = {
			"cssClass1", "cssClass2" })
	public class TestController {

		@AFXShowView()
		public void actionMethod() {
			System.out.println("Original Method");
		}
	}

	/**
	 * Test controller for the customer form.
	 *
	 * @author koster
	 *
	 */
	public class CustomerController {

		@FXML
		private ChoiceBox<String> countryChoiceBox;

		@FXML
		private ListView<String> shoppingCartListView;

		void init() {
			countryChoiceBox.getItems().addAll("Germany", "France", "Spain", "Italy", "Portugal", "UK", "USA");
			shoppingCartListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			shoppingCartListView.getItems().addAll(Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5"));
		}
	}

	/**
	 * The model used to bind to the customer form.
	 *
	 * @author koster
	 *
	 */
	public class CustomerModel {

		private String firstName;

		private String lastName;

		private String country;

		private String street;

		private String postalCode;

		private String city;

		// observable list that can be bidirectionally bound
		private final List<String> selectedProducts = FXCollections.observableArrayList();

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(final String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(final String lastName) {
			this.lastName = lastName;
		}

		public String getPostalCode() {
			return postalCode;
		}

		public void setPostalCode(final String postalCode) {
			this.postalCode = postalCode;
		}

		public String getCity() {
			return city;
		}

		public void setCity(final String city) {
			this.city = city;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(final String country) {
			this.country = country;
		}

		public List<String> getSelectedProducts() {
			return selectedProducts;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(final String street) {
			this.street = street;
		}

	}

}
