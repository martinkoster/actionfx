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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.annotation.AFXEnableNode;
import com.github.actionfx.core.annotation.AFXFormBinding;
import com.github.actionfx.core.annotation.AFXFormMapping;
import com.github.actionfx.core.test.ViewCreator;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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
class FormBindingControllerExtensionTest {

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
		assertThat(controller.customerFirstNameControl.getText(), equalTo("John"));
		assertThat(controller.customerLastNameControl.getText(), equalTo("Doe"));
		assertThat(controller.customerCountryControl.getValue(), equalTo("USA"));
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems(),
				contains("Item 1", "Item 2"));
		assertThat(controller.customerTermsAndConditionsControl.isSelected(), equalTo(true));

		// check binding from control side by entering values into controls
		controller.customerFirstNameControl.setText("Joe");
		controller.customerLastNameControl.setText("Dalton");
		controller.customerCountryControl.getSelectionModel().select("France");
		controller.customerSelectedProductsControl.getSelectionModel().select("Item 3");
		controller.customerTermsAndConditionsControl.setSelected(false);

		assertThat(model.getFirstName(), equalTo("Joe"));
		assertThat(model.getLastName(), equalTo("Dalton"));
		assertThat(model.getCountry(), equalTo("France"));
		assertThat(model.getSelectedProducts(), contains("Item 1", "Item 2", "Item 3"));
		assertThat(model.isTermsAndConditions(), equalTo(false));

		// check binding from model side by changing values in the model
		model.setFirstName("Jane");
		model.setLastName("Doe");
		model.setCountry("Italy");
		model.getSelectedProducts().clear();
		model.setTermsAndConditions(true);

		assertThat(controller.customerFirstNameControl.getText(), equalTo("Jane"));
		assertThat(controller.customerLastNameControl.getText(), equalTo("Doe"));
		assertThat(controller.customerCountryControl.getValue(), equalTo("Italy"));
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems(), hasSize(0));
		assertThat(controller.customerTermsAndConditionsControl.isSelected(), equalTo(true));
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
		assertThat(controller.customerFirstNameControl.getText(), equalTo("John"));
		assertThat(controller.customerLastNameControl.getText(), equalTo("Doe"));
		assertThat(controller.customerCountryControl.getValue(), equalTo("USA"));
		assertThat(controller.customerSelectedProductsControl.getItems(),
				contains("Item 1", "Item 2", "Item 3", "Item 4", "Item 5"));
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems(),
				contains("Item 1", "Item 2"));
		assertThat(controller.customerTermsAndConditionsControl.isSelected(), equalTo(true));
		assertThat(controller.localDateTimeTextField.getText(), equalTo("05.09.2021 13:05"));

		// check binding from control side by entering values into controls
		controller.customerFirstNameControl.setText("Joe");
		controller.customerLastNameControl.setText("Dalton");
		controller.customerCountryControl.getSelectionModel().select("France");
		controller.customerSelectedProductsControl.getSelectionModel().select("Item 3");
		controller.customerTermsAndConditionsControl.setSelected(false);
		controller.localDateTimeTextField.setText("05.10.2022 15:10");

		assertThat(model.getFirstName(), equalTo("Joe"));
		assertThat(model.getLastName(), equalTo("Dalton"));
		assertThat(model.getCountry(), equalTo("France"));
		assertThat(model.getSelectedProducts(), contains("Item 1", "Item 2", "Item 3"));
		assertThat(model.isTermsAndConditions(), equalTo(false));
		assertThat(model.getLocalDateTime(), equalTo(LocalDateTime.of(2022, 10, 5, 15, 10)));

		// check binding from model side by changing values in the model
		model.setFirstName("Jane");
		model.setLastName("Doe");
		model.setCountry("Italy");
		model.getSelectedProducts().clear();
		model.setTermsAndConditions(true);
		model.setLocalDateTime(LocalDateTime.of(2023, 11, 10, 16, 9));

		assertThat(controller.customerFirstNameControl.getText(), equalTo("Jane"));
		assertThat(controller.customerLastNameControl.getText(), equalTo("Doe"));
		assertThat(controller.customerCountryControl.getValue(), equalTo("Italy"));
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems(), hasSize(0));
		assertThat(controller.customerTermsAndConditionsControl.isSelected(), equalTo(true));
		assertThat(controller.localDateTimeTextField.getText(), equalTo("10.11.2023 16:09"));
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
		assertThat(controller.customerFirstNameControl.getText(), equalTo("John"));
		assertThat(controller.customerLastNameControl.getText(), equalTo("Doe"));
		assertThat(controller.customerCountryControl.getValue(), equalTo("USA"));
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems(),
				contains("Item 1", "Item 2"));
		assertThat(controller.customerTermsAndConditionsControl.isSelected(), equalTo(true));

		// change to a different model
		controller.modelWithNameBasedBinding.set(model2);
		assertThat(controller.customerFirstNameControl.getText(), equalTo("Joe"));
		assertThat(controller.customerLastNameControl.getText(), equalTo("Dalton"));
		assertThat(controller.customerCountryControl.getValue(), equalTo("France"));
		assertThat(controller.customerSelectedProductsControl.getSelectionModel().getSelectedItems(),
				contains("Item 3", "Item 4", "Item 5"));
		assertThat(controller.customerTermsAndConditionsControl.isSelected(), equalTo(false));
	}

	@Test
	void testAccept_annotatedFieldHasNullValue() {
		// GIVEN
		final CustomerControllerWithFieldValueNull controller = new CustomerControllerWithFieldValueNull();
		final FormBindingControllerExtension extension = new FormBindingControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString("is annotated by @AFXFormBinding, but value is null!"));
	}

	@Test
	void testAccept_annotatedFieldIsNotOfTypeObjectProperty() {
		// GIVEN
		final CustomerControllerWithInvalidFieldType controller = new CustomerControllerWithInvalidFieldType();
		final FormBindingControllerExtension extension = new FormBindingControllerExtension();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> extension.accept(controller));

		// THEN
		assertThat(ex.getMessage(), containsString(
				"is annotated by @AFXFormBinding, is not of expected type javafx.beans.property.ObjectProperty!"));
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
	 * The model used to bind to the customer form. Please note that attributes can
	 * be also Java primitives, not necessarily JavaFX properties. However,
	 * bi-directional binding will only work with JavaFX properties.
	 *
	 * @author koster
	 *
	 */
	public class CustomerModel {

		private final StringProperty firstName = new SimpleStringProperty();

		private final StringProperty lastName = new SimpleStringProperty();

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
