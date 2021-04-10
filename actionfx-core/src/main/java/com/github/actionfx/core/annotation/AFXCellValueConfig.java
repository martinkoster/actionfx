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
package com.github.actionfx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.actionfx.core.annotation.AFXCellValueConfig.AFXTableColumns;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.StringConverter;

/**
 * Annotation that can be applied at field level to configure tables/table
 * columns and what data shall be displayed there and how.
 * <p>
 * The annotation can be applied on:
 * <p>
 * <ul>
 * <li>{@link TableView} or {@link TreeTableView} for configuring the contained
 * columns without the need to have the columns itself injected via @FXML</li>
 * <li>{@link TableColumn} or {@link TreeTableColumn} for configuring the
 * annotated column directly</li>
 * <li>{@link TreeView}</li>
 * <li>{@link ListView}</li>
 * </ul>
 *
 * @author koster
 *
 */
@Repeatable(AFXTableColumns.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXCellValueConfig {

	/**
	 * The name of the column to configure. This field is mandatory, if applied on a
	 * {@link TableColumn} or {@link TreeTableColumn} and no value in
	 * {@link #colIdx()} is given.
	 * <p>
	 * The field is meaningless, when the annotation is applied on
	 * {@link TableColumn} or {@link TreeTableColumn} field directly.
	 *
	 * @return the name of the column ("id")
	 */
	public String colId() default "";

	/**
	 * The index of the column to configure (0-based). This field is mandatory, if
	 * applied on a {@link TableColumn} or {@link TreeTableColumn} and no value in
	 * {@link #colId()} is given.
	 * <p>
	 * The field is meaningless, when the annotation is applied on
	 * {@link TableColumn} or {@link TreeTableColumn} field directly.
	 *
	 * @return the name of the column ("id")
	 */
	public int colIdx() default -1;

	/**
	 * The property value that is used to configure a {@link PropertyValueFactory}
	 * or {@link TreeItemPropertyValueFactory} as a cell-value factory for the
	 * column to configure. This field is mandatory, when annotating fields of type
	 * {@link TableView}, {@link TableColumn}, {@link TreeTableView} or
	 * {@link TreeTableColumn}.
	 * <p>
	 * For fields of type {@link TreeView} and {@link ListView} it must remain
	 * empty, because the components do not allow setting a single property from a
	 * Java bean. For these components, using a {@link #stringConverter()} makes
	 * sense.
	 *
	 * @return the name of the property to use for a {@link PropertyValueFactory}
	 */
	public String propertyValue() default "";

	/**
	 * An optional string converter class that shall be used to convert the backed
	 * domain objects value under {@link propertyValue} into a displayable string in
	 * the table column and convert it back from a string to the property's data
	 * type.
	 *
	 * @return the string converter class to use
	 */
	@SuppressWarnings("rawtypes")
	public Class<? extends StringConverter> stringConverter() default StringConverter.class;

	/**
	 * An optional cell type to set for the given cell to configure.
	 *
	 * @return the cell type to use (e.g.
	 *         {@link javafx.scene.control.cell.CheckBoxTableCell})
	 */
	@SuppressWarnings("rawtypes")
	public Class<? extends IndexedCell> cellType() default IndexedCell.class;

	/**
	 * Annotation to make {@link AFXCellValueConfig} repeatable (e.g. on
	 * {@link TableView} and {@link TreeTableView} directly). This annotation is not
	 * intended for direct use in application code.
	 *
	 * @author MartinKoster
	 *
	 */
	@Target({ ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface AFXTableColumns {

		AFXCellValueConfig[] value();
	}
}
