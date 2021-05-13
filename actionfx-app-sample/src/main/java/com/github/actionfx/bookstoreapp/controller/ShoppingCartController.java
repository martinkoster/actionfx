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
package com.github.actionfx.bookstoreapp.controller;

import java.util.List;

import com.github.actionfx.bookstoreapp.model.Book;
import com.github.actionfx.common.converter.DoubleCurrencyStringConverter;
import com.github.actionfx.core.annotation.AFXCellValueConfig;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXRequiresUserConfirmation;
import com.github.actionfx.core.annotation.AFXShowView;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

@AFXController(viewId = "shoppingCartView", fxml = "/fxml/ShoppingCartView.fxml")
public class ShoppingCartController {

	@FXML
	private Button emptyButton;

	@FXML
	private Button checkoutButton;

	@AFXCellValueConfig(colId = "titleColumn", propertyValue = "title")
	@AFXCellValueConfig(colId = "categoryColumn", propertyValue = "category")
	@AFXCellValueConfig(colId = "priceColumn", propertyValue = "price", stringConverter = DoubleCurrencyStringConverter.class)
	@FXML
	private TableView<Book> bookTableView;

	@AFXOnAction(controlId = "emptyButton")
	@AFXRequiresUserConfirmation(title="Confirmation", header="Empty Shopping Cart", content = "Are you sure you want to empty the shopping cart?")
	public void emptyShoppingCart() {
		bookTableView.getItems().clear();
	}

	@AFXOnAction(controlId = "checkoutButton")
	@AFXShowView(viewId = "checkoutView", showInNewWindow = true)
	public void checkout() {
		// no to-do here as of the moment. Displaying of the view is achieved by
		// annotation "AFXShowView"
	}

	public void addToShoppingCart(final List<Book> books) {
		bookTableView.getItems().addAll(books);
	}
}
