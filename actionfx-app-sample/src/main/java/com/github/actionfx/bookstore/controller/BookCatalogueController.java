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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.controlsfx.control.CheckListView;

import com.github.actionfx.bookstore.model.Book;
import com.github.actionfx.common.converter.DoubleCurrencyStringConverter;
import com.github.actionfx.core.annotation.AFXCellValueConfig;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.annotation.AFXController;
import com.github.actionfx.core.annotation.AFXEnableMultiSelection;
import com.github.actionfx.core.annotation.AFXEnableNode;
import com.github.actionfx.core.annotation.AFXLoadControlData;
import com.github.actionfx.core.annotation.AFXOnAction;
import com.github.actionfx.core.annotation.AFXOnControlValueChange;
import com.github.actionfx.core.annotation.AFXUseFilteredList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

@AFXController(viewId = "bookCatalogueView", fxml = "/fxml/BookCatalogueView.fxml")
public class BookCatalogueController {

    @FXML
    private CheckListView<String> categoriesCheckListView;

    // enable button, when user selects a book
    @AFXEnableNode(whenAllContolsHaveUserValues = "bookTableView")
    @FXML
    private Button addToShoppingCartButton;

    @AFXUseFilteredList(filterPredicateProperty = "catalogueFilterPredicateProperty")
    @AFXEnableMultiSelection
    @AFXCellValueConfig(colId = "titleColumn", propertyValue = "title")
    @AFXCellValueConfig(colId = "categoryColumn", propertyValue = "category")
    @AFXCellValueConfig(colId = "priceColumn", propertyValue = "price", stringConverter = DoubleCurrencyStringConverter.class)
    @FXML
    private TableView<Book> bookTableView;

    // both, ActionFX' BeanContainer and Spring bean container know how to interpret
    // this annotation
    @Inject
    private ShoppingCartController shoppingCartController;

    // holds the predicate that filters our book table
    private final ObjectProperty<Predicate<Book>> catalogueFilterPredicateProperty = new SimpleObjectProperty<>(
            b -> true);

    @PostConstruct
    public void initialize() {
        categoriesCheckListView.getCheckModel().checkAll();
    }

    @AFXLoadControlData(controlId = "categoriesCheckListView")
    public List<String> loadCategories() {
        return Arrays.asList("Thriller", "Science Fiction", "Fantasy", "Drama");
    }

    @AFXLoadControlData(controlId = "bookTableView", async = true)
    public List<Book> loadBooks() {
        final List<Book> books = new ArrayList<>();
        books.add(new Book("Sci-Fi Stories 3", "Science Fiction", 8.99));
        books.add(new Book("Thrilling 2", "Thriller", 9.99));
        books.add(new Book("Fantastic World", "Fantasy", 7.99));
        books.add(new Book("Drama Queen", "Drama", 2.99));
        return books;
    }

    /**
     * Immediately fired after the user changes the selection in the "categoryCheckListView".
     *
     * @param selectedCategories
     *            the selected categories
     * @param filterText
     *            the entered filter text (from the control "filterTextField")
     */
    @AFXOnControlValueChange(controlId = "categoriesCheckListView")
    public void onCategoryChange(final List<String> selectedCategories,
            @AFXControlValue("filterTextField") final String filterText) {
        applyPredicate(filterText, selectedCategories);
    }

    /**
     * Triggered 400ms after the user typed something in the filter text field.
     *
     * @param filterText
     *            the entered filter text
     * @param selectedCategories
     *            the selected categories (from control "categoriesCheckListView")
     */
    @AFXOnControlValueChange(controlId = "filterTextField", timeoutMs = 400)
    public void onFilterChange(final String filterText,
            @AFXControlValue("categoriesCheckListView") final List<String> selectedCategories) {
        applyPredicate(filterText, selectedCategories);
    }

    /**
     * Fired when the user clicks on button "Add to Shopping Cart". The selected books are retrieved from control
     * "bookTableView".
     *
     * @param selectedBooks
     *            the selected books from control "bookTableView"
     */
    @AFXOnAction(nodeId = "addToShoppingCartButton")
    public void addToShoppingCart(@AFXControlValue("bookTableView") final List<Book> selectedBooks) {
        shoppingCartController.addToShoppingCart(selectedBooks);
    }

    /**
     * Constructs and applies a predicate for filtering books.
     *
     * @param filterText
     *            the filter text to be applied on the title
     * @param selectedCategories
     *            the categories
     */
    public void applyPredicate(final String filterText, final List<String> selectedCategories) {
        final Predicate<Book> p = b -> selectedCategories.contains(b.getCategory());
        catalogueFilterPredicateProperty.set(p.and(b -> b.getTitle().toLowerCase().contains(filterText.toLowerCase())));
    }
}
