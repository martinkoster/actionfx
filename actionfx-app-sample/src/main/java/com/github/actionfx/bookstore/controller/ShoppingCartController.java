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
package com.github.actionfx.bookstore.controller;

import java.util.List;

import javax.inject.Inject;

import com.github.actionfx.bookstore.model.Book;
import com.github.actionfx.bookstore.model.OrderSummary;
import com.github.actionfx.common.converter.DoubleCurrencyStringConverter;
import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXCellValueConfig;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXEnableNode;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXRequiresUserConfirmation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

@AFXController(viewId = "shoppingCartView", fxml = "/fxml/ShoppingCartView.fxml")
public class ShoppingCartController {

	// activate button, when the table has items
	@AFXEnableNode(whenAllControlsHaveValues = "bookTableView")
	@FXML
	private Button removeAllButton;

	// activate button, when user selected values
	@AFXEnableNode(whenAllContolsHaveUserValues = "bookTableView")
	@FXML
	private Button removeSelectedButton;

	@AFXEnableNode(whenAllControlsHaveValues = "bookTableView")
	@FXML
	private Button checkoutButton;

	@AFXEnableMultiSelection
	@AFXCellValueConfig(colId = "titleColumn", propertyValue = "title")
	@AFXCellValueConfig(colId = "categoryColumn", propertyValue = "category")
	@AFXCellValueConfig(colId = "priceColumn", propertyValue = "price", stringConverter = DoubleCurrencyStringConverter.class)
	@FXML
	private TableView<Book> bookTableView;

	@Inject
	private ActionFX actionFX;

	@AFXOnAction(nodeId = "removeAllButton")
	@AFXRequiresUserConfirmation(title = "Confirmation", header = "Empty Shopping Cart", content = "Are you sure you want to empty the shopping cart?")
	public void emptyShoppingCart() {
		bookTableView.getItems().clear();
	}

	@AFXOnAction(nodeId = "removeSelectedButton")
	@AFXRequiresUserConfirmation(title = "Confirmation", header = "Remove selected books", content = "Are you sure to remove the selected books from the shopping cart?")
	public void removeSelectedBooks(@AFXControlValue("bookTableView") final List<Book> selectedBooks) {
		bookTableView.getItems().removeAll(selectedBooks);
	}

	@AFXOnAction(nodeId = "checkoutButton")
	public void checkout() {
		final OrderSummary model = new OrderSummary();
		model.getOrder().getOrderedBooks().addAll(bookTableView.getItems());

		// publish order summary to start the checkout process
		actionFX.publishEvent(model);
	}

	public void addToShoppingCart(final List<Book> books) {
		bookTableView.getItems().addAll(books);
	}
}
