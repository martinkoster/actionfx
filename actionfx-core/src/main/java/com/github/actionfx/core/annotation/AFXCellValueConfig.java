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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.StringConverter;

/**
 * Annotation that can be applied at field level to configure tables/table columns and what data shall be displayed
 * there and how.
 * <p>
 * The annotation can be applied on:
 * <p>
 * <ul>
 * <li>{@link javafx.scene.control.TableView} or {@link javafx.scene.control.TreeTableView} for configuring the
 * contained columns without the need to have the columns itself injected via @FXML</li>
 * <li>{@link javafx.scene.control.TableColumn} or {@link javafx.scene.control.TreeTableColumn} for configuring the
 * annotated column directly</li>
 * <li>{@link javafx.scene.control.TreeView}</li>
 * <li>{@link javafx.scene.control.ListView}</li>
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
     * {@link javafx.scene.control.TableColumn} or {@link javafx.scene.control.TreeTableColumn} and no value in
     * {@link #colIdx()} is given.
     * <p>
     * The field is meaningless, when the annotation is applied on {@link javafx.scene.control.TableColumn} or
     * {@link javafx.scene.control.TreeTableColumn} field directly.
     *
     * @return the name of the column ("id")
     */
    public String colId() default "";

    /**
     * The index of the column to configure (0-based). This field is mandatory, if applied on a
     * {@link javafx.scene.control.TableColumn} or {@link javafx.scene.control.TreeTableColumn} and no value in
     * {@link #colId()} is given.
     * <p>
     * The field is meaningless, when the annotation is applied on {@link javafx.scene.control.TableColumn} or
     * {@link javafx.scene.control.TreeTableColumn} field directly.
     *
     * @return the name of the column ("id")
     */
    public int colIdx() default -1;

    /**
     * The property value that is used to configure a {@link PropertyValueFactory} or
     * {@link TreeItemPropertyValueFactory} as a cell-value factory for the column to configure. This field is
     * mandatory, when annotating fields of type {@link javafx.scene.control.TableView},
     * {@link javafx.scene.control.TableColumn}, {@link javafx.scene.control.TreeTableView} or
     * {@link javafx.scene.control.TreeTableColumn}.
     * <p>
     * For fields of type {@link javafx.scene.control.TreeView} and {@link javafx.scene.control.ListView} it must remain
     * empty, because the components do not allow accessing single Java bean properties for displaying. For these
     * components, using a {@link #stringConverter()} must be used.
     *
     * @return the name of the property to use for a {@link PropertyValueFactory}
     */
    public String propertyValue() default "";

    /**
     * An optional string converter class that shall be used to convert the backed domain objects value under
     * {@link #propertyValue()} into a displayable string in the table column and convert it back from a string to the
     * property's data type.
     *
     * @return the string converter class to use
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends StringConverter> stringConverter() default StringConverter.class;

    /**
     * An optional cell type to set for the given cell to configure.
     *
     * @return the cell type to use (e.g. {@link javafx.scene.control.cell.CheckBoxTableCell})
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends IndexedCell> cellType() default IndexedCell.class;

    /**
     * Flag that indicates whether the user shall be able to edit cell data in the table column directly or not. The
     * default is {@code false} (no editing).
     *
     * @return {@code true}, if the cell data can be edited by the user, {@code false}, if the data is not editable.
     *         Default is {@code false}.
     */
    public boolean editable() default false;

    /**
     * Annotation to make {@link AFXCellValueConfig} repeatable (e.g. on {@link javafx.scene.control.TableView} and
     * {@link javafx.scene.control.TreeTableView} directly). This annotation is not intended for direct use in
     * application code.
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
