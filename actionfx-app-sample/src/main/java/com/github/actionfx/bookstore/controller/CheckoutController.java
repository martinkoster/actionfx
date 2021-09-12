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
package com.github.actionfx.bookstore.controller;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.github.actionfx.bookstore.model.Book;
import com.github.actionfx.bookstore.model.Customer;
import com.github.actionfx.bookstore.model.Order;
import com.github.actionfx.bookstore.model.OrderSummary;
import com.github.actionfx.common.converter.DoubleCurrencyStringConverter;
import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXCellValueConfig;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXEnableNode;
import com.github.actionfx.core.annotation.AFXFormBinding;
import com.github.actionfx.core.annotation.AFXFormMapping;
import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.view.graph.ControlProperties;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 * The checkout view, displayed in its own stage in a modal fashion.
 *
 * @author koster
 *
 */
@AFXController(viewId = "checkoutView", fxml = "/fxml/CheckoutView.fxml", icon = "/images/book.png", title = "Check Out", modal = true, width = 600, height = 500, posX = 600, posY = 300)
public class CheckoutController {

	@AFXFormBinding
	@AFXFormMapping(propertyName = "customer.firstName", controlId = "firstNameTextField")
	@AFXFormMapping(propertyName = "customer.lastName", controlId = "lastNameTextField")
	@AFXFormMapping(propertyName = "customer.country", controlId = "countryChoiceBox")
	@AFXFormMapping(propertyName = "customer.street", controlId = "streetTextField")
	@AFXFormMapping(propertyName = "customer.postalCode", controlId = "postalCodeTextField")
	@AFXFormMapping(propertyName = "customer.city", controlId = "cityTextField")
	@AFXFormMapping(propertyName = "order.orderedBooks", controlId = "bookTableView", targetProperty = ControlProperties.ITEMS_OBSERVABLE_LIST)
	@AFXFormMapping(propertyName = "order.orderedBooks", controlId = "bookTableView")
	private final ObjectProperty<OrderSummary> orderSummary = new SimpleObjectProperty<>(new OrderSummary());

	@AFXEnableMultiSelection
	@AFXCellValueConfig(colId = "titleColumn", propertyValue = "title")
	@AFXCellValueConfig(colId = "categoryColumn", propertyValue = "category")
	@AFXCellValueConfig(colId = "priceColumn", propertyValue = "price", stringConverter = DoubleCurrencyStringConverter.class)
	@FXML
	private TableView<Book> bookTableView;

	@AFXEnableNode(whenAllContolsHaveUserValues = { "firstNameTextField", "lastNameTextField", "countryChoiceBox",
			"streetTextField", "postalCodeTextField", "cityTextField" })
	@FXML
	private Button completeOrderButton;

	@Inject
	private ActionFX actionFX;

	@AFXLoadControlData(controlId = "countryChoiceBox")
	public List<String> availableCountries() {
		return Arrays.asList("Germany", "France", "Spain", "Italy", "Portugal", "UK", "USA");
	}

	public void startCheckout(final List<Book> books) {
		// create an order summary as model for our form-binding
		final OrderSummary model = new OrderSummary();
		model.getOrder().getOrderedBooks().addAll(books);
		orderSummary.set(model);
	}

	@AFXOnAction(nodeId = "completeOrderButton")
	public void completeCheckout() {
		actionFX.showInformationDialog("Order successfully placed", createOrderSummary(), "");
		actionFX.hideView(this);
	}

	private String createOrderSummary() {
		final StringBuilder builder = new StringBuilder();
		final OrderSummary model = orderSummary.get();
		builder.append("Shipped to:\n");
		final Customer customer = model.getCustomer();
		final Order order = model.getOrder();
		final DoubleCurrencyStringConverter converter = new DoubleCurrencyStringConverter();
		builder.append(customer.getFirstName() + " " + customer.getLastName() + "\n");
		builder.append(customer.getCountry() + "\n");
		builder.append(customer.getStreet() + "\n");
		builder.append(customer.getPostalCode() + " " + customer.getCity() + "\n");
		builder.append("\n");
		builder.append("Books ordered: " + order.getOrderedBooks().size() + ", Total price: "
				+ converter.toString(order.getOrderedBooks().stream().mapToDouble(Book::getPrice).sum()));
		return builder.toString();
	}

	@AFXOnAction(nodeId = "cancelOrderButton")
	public void cancelCheckout() {
		orderSummary.set(new OrderSummary());
		actionFX.hideView(this);
	}
}
