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
package com.github.actionfx.core.extension.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXEnableNode;
import com.github.actionfx.core.annotation.AFXFormBinding;
import com.github.actionfx.core.annotation.AFXFormMapping;
import com.github.actionfx.core.annotation.BooleanValue;
import com.github.actionfx.core.annotation.ValidationHelper;
import com.github.actionfx.core.annotation.ValidationMode;
import com.github.actionfx.core.test.ViewCreator;
import com.github.actionfx.core.validation.ValidationMessage;
import com.github.actionfx.core.validation.ValidationResult;
import com.github.actionfx.core.validation.ValidationStatus;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;

/**
 * JUnit test case for {@link FormBindingControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
@TestInFxThread
class FormBindingControllerExtensionTest {

    @BeforeAll
    static void onSetup() {
        ActionFX.builder().validationGlobalMode(ValidationMode.ONCHANGE).validationStartTimeoutMs(0).build()
                .scanForActionFXComponents();
    }

    @AfterAll
    static void tearDown() {
        ActionFX.getInstance().reset();
    }

    @Test
    void testAccept_withNameBasedMatching() {
        // GIVEN
        final CustomerController controller = new CustomerController();
        controller.customerSelectedProductsControl.getItems().addAll("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
        final FormBindingControllerExtension extension = new FormBindingControllerExtension();
        final CustomerModel model = createCustomerModel("John", "Doe", "USA", true, "Item 1", "Item 2");

        // WHEN
        extension.accept(controller);

        // THEN
        controller.modelWithNameBasedBinding.set(model);

		assertThat(controller.customerFirstNameControl.getText()).isEqualTo("John");
		assertThat(controller.customerLastNameControl.getText()).isEqualTo("Doe");
		assertThat(controller.customerCountryControl.getValue()).isEqualTo("USA");
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems()).containsExactly("Item 1", "Item 2");
        assertThat(controller.customerTermsAndConditionsControl.isSelected()).isTrue();

        // check binding from control side by entering values into controls
        controller.customerFirstNameControl.setText("Joe");
        controller.customerLastNameControl.setText("Dalton");
        controller.customerCountryControl.getSelectionModel().select("France");
        controller.customerSelectedProductsControl.getSelectionModel().select("Item 3");
        controller.customerTermsAndConditionsControl.setSelected(false);

		assertThat(model.getFirstName()).isEqualTo("Joe");
		assertThat(model.getLastName()).isEqualTo("Dalton");
		assertThat(model.getCountry()).isEqualTo("France");
		assertThat(model.getSelectedProducts()).containsExactly("Item 1", "Item 2", "Item 3");
        assertThat(model.isTermsAndConditions()).isFalse();

        // check binding from model side by changing values in the model
        model.setFirstName("Jane");
        model.setLastName("Doe");
        model.setCountry("Italy");
        model.getSelectedProducts().clear();
        model.setTermsAndConditions(true);

		assertThat(controller.customerFirstNameControl.getText()).isEqualTo("Jane");
		assertThat(controller.customerLastNameControl.getText()).isEqualTo("Doe");
		assertThat(controller.customerCountryControl.getValue()).isEqualTo("Italy");
        assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems()).isEmpty();
        assertThat(controller.customerTermsAndConditionsControl.isSelected()).isTrue();
    }

    @Test
    void testAccept_withMappingBasedMatching() {
        // GIVEN
        final CustomerController controller = new CustomerController();
        final FormBindingControllerExtension extension = new FormBindingControllerExtension();
        final CustomerModel model = createCustomerModel("John", "Doe", "USA", true, "Item 1", "Item 2");

        // WHEN
        extension.accept(controller);

        // THEN
        controller.modelWithMappingBasedBinding.set(model);
		assertThat(controller.customerFirstNameControl.getText()).isEqualTo("John");
		assertThat(controller.customerLastNameControl.getText()).isEqualTo("Doe");
		assertThat(controller.customerCountryControl.getValue()).isEqualTo("USA");
		assertThat(controller.customerSelectedProductsControl.getItems()).containsExactly("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems()).containsExactly("Item 1", "Item 2");
        assertThat(controller.customerTermsAndConditionsControl.isSelected()).isTrue();
		assertThat(controller.localDateTimeTextField.getText()).isEqualTo("05.09.2021 13:05");

        // check binding from control side by entering values into controls
        controller.customerFirstNameControl.setText("Joe");
        controller.customerLastNameControl.setText("Dalton");
        controller.customerCountryControl.getSelectionModel().select("France");
        controller.customerSelectedProductsControl.getSelectionModel().select("Item 3");
        controller.customerTermsAndConditionsControl.setSelected(false);
        controller.localDateTimeTextField.setText("05.10.2022 15:10");

		assertThat(model.getFirstName()).isEqualTo("Joe");
		assertThat(model.getLastName()).isEqualTo("Dalton");
		assertThat(model.getCountry()).isEqualTo("France");
		assertThat(model.getSelectedProducts()).containsExactly("Item 1", "Item 2", "Item 3");
        assertThat(model.isTermsAndConditions()).isFalse();
		assertThat(model.getLocalDateTime()).isEqualTo(LocalDateTime.of(2022, 10, 5, 15, 10));

        // check binding from model side by changing values in the model
        model.setFirstName("Jane");
        model.setLastName("Doe");
        model.setCountry("Italy");
        model.getSelectedProducts().clear();
        model.setTermsAndConditions(true);
        model.setLocalDateTime(LocalDateTime.of(2023, 11, 10, 16, 9));

		assertThat(controller.customerFirstNameControl.getText()).isEqualTo("Jane");
		assertThat(controller.customerLastNameControl.getText()).isEqualTo("Doe");
		assertThat(controller.customerCountryControl.getValue()).isEqualTo("Italy");
        assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems()).isEmpty();
        assertThat(controller.customerTermsAndConditionsControl.isSelected()).isTrue();
		assertThat(controller.localDateTimeTextField.getText()).isEqualTo("10.11.2023 16:09");
    }

    @Test
    void testAccept_withMappingBasedMatching_withValidation() {
        // GIVEN
        final CustomerControllerWithValidation controller = new CustomerControllerWithValidation();
        final FormBindingControllerExtension extension = new FormBindingControllerExtension();
        final CustomerModel model = createCustomerModel("", "", "Germany", false);

        // WHEN
        extension.accept(controller);

        // THEN
        controller.modelWithMappingBasedBinding.set(model);
        controller._view.validate();

        // THEN assert validation errors are shown
        assertControlHasValidationError(controller._view, controller.customerFirstNameControl,
                "First Name is mandatory");
        assertControlHasValidationError(controller._view, controller.customerLastNameControl, "Last Name is mandatory");
        assertControlHasValidationError(controller._view, controller.customerEmailControl,
                "Entered value is not a valid e-mail address");
        assertControlHasValidationError(controller._view, controller.customerCountryControl,
                "This service cannot be provided in country 'Germany'");
        assertControlHasValidationError(controller._view, controller.customerAgeControl,
                "Entered age is not between 18 and 200");
        assertControlHasValidationError(controller._view, controller.customerSelectedProductsControl,
                "Please select at least 1 product and at maximum 5 products");
        assertControlHasValidationError(controller._view, controller.customerTermsAndConditionsControl,
                "Please accept the terms and conditions");
        assertControlHasValidationError(controller._view, controller.localDateTimeTextField,
                "Please enter a date in the future");

        // update values so that these will pass validations
        controller.customerFirstNameControl.setText("Joe");
        controller.customerLastNameControl.setText("Dalton");
        controller.customerAgeControl.setText("18");
        controller.customerEmailControl.setText("joe.dalton@gmail.com");
        controller.customerCountryControl.getSelectionModel().select("France");
        controller.customerSelectedProductsControl.getSelectionModel().select("Item 3");
        controller.customerTermsAndConditionsControl.setSelected(true);
        controller.localDateTimeTextField.setText("05.10.2065 15:10");

        // THEN validation successful
        assertControlHasNoValidationError(controller._view, controller.customerFirstNameControl);
        assertControlHasNoValidationError(controller._view, controller.customerLastNameControl);
        assertControlHasNoValidationError(controller._view, controller.customerAgeControl);
        assertControlHasNoValidationError(controller._view, controller.customerEmailControl);
        assertControlHasNoValidationError(controller._view, controller.customerCountryControl);
        assertControlHasNoValidationError(controller._view, controller.customerSelectedProductsControl);
        assertControlHasNoValidationError(controller._view, controller.customerTermsAndConditionsControl);
        assertControlHasNoValidationError(controller._view, controller.localDateTimeTextField);

		// check values in bound instance
		assertThat(model.getFirstName()).isEqualTo("Joe");
		assertThat(model.getLastName()).isEqualTo("Dalton");
		assertThat(model.getEmail()).isEqualTo("joe.dalton@gmail.com");
		assertThat(model.getAge()).isEqualTo(18.0);
		assertThat(model.getCountry()).isEqualTo("France");
		assertThat(model.getSelectedProducts()).containsExactly("Item 3");
        assertThat(model.isTermsAndConditions()).isTrue();
		assertThat(model.getLocalDateTime()).isEqualTo(LocalDateTime.of(2065, 10, 5, 15, 10));
    }

    @Test
    void testAccept_modelIsChanged() {
        // GIVEN
        final CustomerController controller = new CustomerController();
        controller.customerSelectedProductsControl.getItems().addAll("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
        final FormBindingControllerExtension extension = new FormBindingControllerExtension();
        final CustomerModel model1 = createCustomerModel("John", "Doe", "USA", true, "Item 1", "Item 2");
        final CustomerModel model2 = createCustomerModel("Joe", "Dalton", "France", false, "Item 3", "Item 4",
                "Item 5");

        // WHEN
        extension.accept(controller);

        // THEN
        controller.modelWithNameBasedBinding.set(model1);
		assertThat(controller.customerFirstNameControl.getText()).isEqualTo("John");
		assertThat(controller.customerLastNameControl.getText()).isEqualTo("Doe");
		assertThat(controller.customerCountryControl.getValue()).isEqualTo("USA");
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems()).containsExactly("Item 1", "Item 2");
        assertThat(controller.customerTermsAndConditionsControl.isSelected()).isTrue();

        // change to a different model
        controller.modelWithNameBasedBinding.set(model2);
		assertThat(controller.customerFirstNameControl.getText()).isEqualTo("Joe");
		assertThat(controller.customerLastNameControl.getText()).isEqualTo("Dalton");
		assertThat(controller.customerCountryControl.getValue()).isEqualTo("France");
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems()).containsExactly("Item 3", "Item 4", "Item 5");
        assertThat(controller.customerTermsAndConditionsControl.isSelected()).isFalse();
    }

    @Test
    void testAccept_annotatedFieldHasNullValue() {
        // GIVEN
        final CustomerControllerWithFieldValueNull controller = new CustomerControllerWithFieldValueNull();
        final FormBindingControllerExtension extension = new FormBindingControllerExtension();

        // WHEN
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).contains("is annotated by @AFXFormBinding, but value is null!");
    }

    @Test
    void testAccept_annotatedFieldIsNotOfTypeObjectProperty() {
        // GIVEN
        final CustomerControllerWithInvalidFieldType controller = new CustomerControllerWithInvalidFieldType();
        final FormBindingControllerExtension extension = new FormBindingControllerExtension();

        // WHEN
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage()).contains("is annotated by @AFXFormBinding, is not of expected type javafx.beans.property.ObjectProperty!");
    }

    private CustomerModel createCustomerModel(final String firstName, final String lastName, final String country,
            final boolean termsAndConditions, final String... selectedProducts) {
        final CustomerModel model = new CustomerModel();
        model.setFirstName(firstName);
        model.setLastName(lastName);
        model.setCountry(country);
        model.setTermsAndConditions(termsAndConditions);
        model.getAllProducts().addAll("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
        model.getSelectedProducts().addAll(selectedProducts);
        return model;
    }

    private static void assertControlHasValidationError(final View view, final Control control,
            final String expectedValidationMessage) {
        final Optional<ValidationMessage> vmOpt = getValidationMessage(view, control);
		assertThat(vmOpt.get()).isNotNull();
		assertThat(vmOpt.get().getStatus()).isEqualTo(ValidationStatus.ERROR);
		assertThat(vmOpt.get().getText()).isEqualTo(expectedValidationMessage);
    }

    private static void assertControlHasNoValidationError(final View view, final Control control) {
        final Optional<ValidationMessage> vmOpt = getValidationMessage(view, control);
        assertThat(vmOpt).isNotPresent();
    }

    private static Optional<ValidationMessage> getValidationMessage(final View view, final Control control) {
        return view.validationResultProperty().getValue().getErrors().stream()
                .filter(vm -> vm.getTarget() == control).findFirst();
    }

    public class Controller {

        public View _view;

        @AFXEnableNode(whenAllContolsHaveUserValues = { "textField" })
        public Button button;

        public TextField textField;

        public Controller() {
            textField = new TextField();
            _view = ViewCreator.create().appendNode(textField, "textField");
        }
    }

    /**
     * Test controller for the customer form.
     *
     * @author koster
     *
     */
    public class CustomerController {

        public View _view;

        public TextField customerFirstNameControl;

        public TextField customerLastNameControl;

        public ChoiceBox<String> customerCountryControl;

        public ListView<String> customerSelectedProductsControl;

        public CheckBox customerTermsAndConditionsControl;

        public TextField localDateTimeTextField;

        // the name of the field and control is taken for matching binding targets
        @AFXFormBinding(disableNameBasedMapping = false, controlPrefix = "customer", controlSuffix = "Control")
        private final ObjectProperty<CustomerModel> modelWithNameBasedBinding = new SimpleObjectProperty<>();

        // the mappings are taken for matching binding targets - name based matchings
        // are explicitly disabled
        @AFXFormBinding(disableNameBasedMapping = true)
        @AFXFormMapping(controlId = "customerFirstNameControl", propertyName = "firstName")
        @AFXFormMapping(controlId = "customerLastNameControl", targetProperty = ControlProperties.SINGLE_VALUE_PROPERTY, propertyName = "lastName")
        @AFXFormMapping(controlId = "customerCountryControl", propertyName = "country")
        @AFXFormMapping(controlId = "customerSelectedProductsControl", targetProperty = ControlProperties.USER_VALUE_OBSERVABLE, propertyName = "selectedProducts")
        @AFXFormMapping(controlId = "customerSelectedProductsControl", targetProperty = ControlProperties.ITEMS_OBSERVABLE_LIST, propertyName = "allProducts")
        @AFXFormMapping(controlId = "customerTermsAndConditionsControl", propertyName = "termsAndConditions")
        @AFXFormMapping(controlId = "localDateTimeTextField", propertyName = "localDateTime", formatPattern = "dd.MM.yyyy HH:mm")
        private final ObjectProperty<CustomerModel> modelWithMappingBasedBinding = new SimpleObjectProperty<>();

        CustomerController() {
            customerFirstNameControl = new TextField();
            customerLastNameControl = new TextField();
            customerCountryControl = new ChoiceBox<>();
            customerSelectedProductsControl = new ListView<>();
            customerTermsAndConditionsControl = new CheckBox();
            localDateTimeTextField = new TextField();

            customerCountryControl.getItems().addAll("Germany", "France", "Spain", "Italy", "Portugal", "UK", "USA");
            customerSelectedProductsControl.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            _view = ViewCreator.create().appendNode(customerFirstNameControl, "customerFirstNameControl")
                    .appendNode(customerLastNameControl, "customerLastNameControl")
                    .appendNode(customerCountryControl, "customerCountryControl")
                    .appendNode(customerSelectedProductsControl, "customerSelectedProductsControl")
                    .appendNode(customerTermsAndConditionsControl, "customerTermsAndConditionsControl")
                    .appendNode(localDateTimeTextField, "localDateTimeTextField");
        }
    }

    public class CustomerControllerWithInvalidFieldType {

        public View _view;

        // field is not of type ObjectProperty
        @AFXFormBinding
        private final StringProperty model = new SimpleStringProperty();

        public CustomerControllerWithInvalidFieldType() {
            _view = ViewCreator.create();
        }
    }

    public class CustomerControllerWithFieldValueNull {

        public View _view;

        // field value is null
        @AFXFormBinding
        private final StringProperty model = null;

        public CustomerControllerWithFieldValueNull() {
            _view = ViewCreator.create();
        }
    }

    /**
     * Test controller for the customer form including validations.
     *
     * @author koster
     *
     */
    public class CustomerControllerWithValidation {

        public View _view;

        public TextField customerFirstNameControl;

        public TextField customerLastNameControl;

        public TextField customerEmailControl;

        public TextField customerAgeControl;

        public ChoiceBox<String> customerCountryControl;

        public ListView<String> customerSelectedProductsControl;

        public CheckBox customerTermsAndConditionsControl;

        public TextField localDateTimeTextField;

        // the mappings are taken for matching binding targets - name based matchings
        // are explicitly disabled
        @AFXFormBinding(disableNameBasedMapping = true)
        @AFXFormMapping(controlId = "customerFirstNameControl", propertyName = "firstName", required = true, validationMessage = "First Name is mandatory")
        @AFXFormMapping(controlId = "customerLastNameControl", targetProperty = ControlProperties.SINGLE_VALUE_PROPERTY, propertyName = "lastName", required = true, validationMessage = "Last Name is mandatory")
        @AFXFormMapping(controlId = "customerEmailControl", propertyName = "email", required = true, regExp = ValidationHelper.EMAIL_ADDRESS_REG_EXP, validationMessage = "Entered value is not a valid e-mail address")
        @AFXFormMapping(controlId = "customerAgeControl", propertyName = "age", required = true, minVal = 18.0, maxVal = 200.0, validationMessage = "Entered age is not between 18 and 200")
        @AFXFormMapping(controlId = "customerCountryControl", propertyName = "country", validationMethod = "validateCountryControl")
        @AFXFormMapping(controlId = "customerSelectedProductsControl", targetProperty = ControlProperties.USER_VALUE_OBSERVABLE, propertyName = "selectedProducts", required = true, minSize = 1, maxSize = 5, validationMessage = "Please select at least 1 product and at maximum 5 products")
        @AFXFormMapping(controlId = "customerSelectedProductsControl", targetProperty = ControlProperties.ITEMS_OBSERVABLE_LIST, propertyName = "allProducts")
        @AFXFormMapping(controlId = "customerTermsAndConditionsControl", propertyName = "termsAndConditions", required = true, expectedBoolean = BooleanValue.TRUE, validationMessage = "Please accept the terms and conditions")
        @AFXFormMapping(controlId = "localDateTimeTextField", propertyName = "localDateTime", formatPattern = "dd.MM.yyyy HH:mm", required = true, future = true, validationMessage = "Please enter a date in the future")
        private final ObjectProperty<CustomerModel> modelWithMappingBasedBinding = new SimpleObjectProperty<>();

        CustomerControllerWithValidation() {
            customerFirstNameControl = new TextField();
            customerLastNameControl = new TextField();
            customerEmailControl = new TextField();
            customerAgeControl = new TextField();
            customerCountryControl = new ChoiceBox<>();
            customerSelectedProductsControl = new ListView<>();
            customerTermsAndConditionsControl = new CheckBox();
            localDateTimeTextField = new TextField();

            customerCountryControl.getItems().addAll("Germany", "France", "Spain", "Italy", "Portugal", "UK", "USA");
            customerSelectedProductsControl.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            _view = ViewCreator.create().appendNode(customerFirstNameControl, "customerFirstNameControl")
                    .appendNode(customerLastNameControl, "customerLastNameControl")
                    .appendNode(customerEmailControl, "customerEmailControl")
                    .appendNode(customerAgeControl, "customerAgeControl")
                    .appendNode(customerCountryControl, "customerCountryControl")
                    .appendNode(customerSelectedProductsControl, "customerSelectedProductsControl")
                    .appendNode(customerTermsAndConditionsControl, "customerTermsAndConditionsControl")
                    .appendNode(localDateTimeTextField, "localDateTimeTextField");
        }

        public ValidationResult validateCountryControl(final String selectedCountry) {
            return ValidationResult.builder().addErrorMessageIf("This service cannot be provided in country 'Germany'",
                    customerCountryControl, "Germany".equals(selectedCountry));
        }
    }

    /**
     * The model used to bind to the customer form. Please note that attributes can be also Java primitives, not
     * necessarily JavaFX properties. However, bi-directional binding will only work with JavaFX properties.
     *
     * @author koster
     *
     */
    public class CustomerModel {

        private final StringProperty firstName = new SimpleStringProperty();

        private final StringProperty lastName = new SimpleStringProperty();

        private final StringProperty email = new SimpleStringProperty();

        private final DoubleProperty age = new SimpleDoubleProperty();

        private final StringProperty country = new SimpleStringProperty();

        private final ObservableList<String> allProducts = FXCollections.observableArrayList();

        private final ObservableList<String> selectedProducts = FXCollections.observableArrayList();

        private final BooleanProperty termsAndConditions = new SimpleBooleanProperty();

        private final ObjectProperty<LocalDateTime> localDateTime = new SimpleObjectProperty<>(
                LocalDateTime.of(2021, 9, 5, 13, 5));

        public final StringProperty firstNameProperty() {
            return firstName;
        }

        public final String getFirstName() {
            return firstNameProperty().get();
        }

        public final void setFirstName(final String firstName) {
            firstNameProperty().set(firstName);
        }

        public final StringProperty lastNameProperty() {
            return lastName;
        }

        public final String getLastName() {
            return lastNameProperty().get();
        }

        public final void setLastName(final String lastName) {
            lastNameProperty().set(lastName);
        }

        public final StringProperty emailProperty() {
            return email;
        }

        public final String getEmail() {
            return emailProperty().get();
        }

        public final void setEmail(final String email) {
            emailProperty().set(email);
        }

        public final DoubleProperty ageProperty() {
            return age;
        }

        public final double getAge() {
            return ageProperty().get();
        }

        public final void setAge(final double age) {
            ageProperty().set(age);
        }

        public final StringProperty countryProperty() {
            return country;
        }

        public final String getCountry() {
            return countryProperty().get();
        }

        public final void setCountry(final String country) {
            countryProperty().set(country);
        }

        public ObservableList<String> getSelectedProducts() {
            return selectedProducts;
        }

        public final BooleanProperty termsAndConditionsProperty() {
            return termsAndConditions;
        }

        public final boolean isTermsAndConditions() {
            return termsAndConditionsProperty().get();
        }

        public final void setTermsAndConditions(final boolean termsAndConditions) {
            termsAndConditionsProperty().set(termsAndConditions);
        }

        public ObservableList<String> getAllProducts() {
            return allProducts;
        }

        public final ObjectProperty<LocalDateTime> localDateTimeProperty() {
            return localDateTime;
        }

        public final LocalDateTime getLocalDateTime() {
            return localDateTimeProperty().get();
        }

        public final void setLocalDateTime(final LocalDateTime localDateTime) {
            localDateTimeProperty().set(localDateTime);
        }

    }
}
