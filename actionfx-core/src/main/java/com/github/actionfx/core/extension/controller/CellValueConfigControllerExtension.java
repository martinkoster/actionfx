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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.github.actionfx.core.annotation.AFXCellValueConfig;
import com.github.actionfx.core.utils.ReflectionUtils;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * Extends controllers for functionality for {@link AFXCellValueConfig}
 * annotation. It configures cell values and cell factories e.g. for table
 * columns inside table views or tree table views.
 * <p>
 * The following JavaFX controls are supported by this extensions:
 * <p>
 * <ul>
 * <li>{@link TableView} and contained {@link TableColumn}</li>
 * <li>{@link TreeView} and contained {@link TreeTableColumn}</li>
 * <li>{@link TreeView}</li>
 * <li>{@link ListView}</li>
 * </ul>
 *
 *
 * @author koster
 *
 */
public class CellValueConfigControllerExtension extends AbstractAnnotatedFieldControllerExtension<AFXCellValueConfig> {

	private final List<ColumnConfigurer> columnConfigurer = new ArrayList<>();

	public CellValueConfigControllerExtension() {
		super(AFXCellValueConfig.class);
		columnConfigurer.add(new TableColumnConfigurer());
		columnConfigurer.add(new TreeTableColumnConfigurer());
		columnConfigurer.add(new TreeViewConfigurer());
		columnConfigurer.add(new ListViewConfigurer());
	}

	@Override
	protected void extend(final Object controller, final Field annotatedElement, final AFXCellValueConfig annotation) {
		final Object fieldValue = ReflectionUtils.getFieldValue(annotatedElement, controller);
		if (fieldValue == null) {
			throw new IllegalStateException("Field '" + annotatedElement.getName() + "' in controller '"
					+ controller.getClass().getCanonicalName()
					+ "' is annotated by @AFXCellValueConfig, but value is null!");
		}
		for (final ColumnConfigurer configurer : columnConfigurer) {
			if (configurer.isApplicableFor(fieldValue.getClass())) {
				configurer.configure(fieldValue, annotation);
				return;
			}
		}

	}

	/**
	 * Base class for a single column configurer.
	 *
	 * @author koster
	 */
	private static interface ColumnConfigurer {

		/**
		 * Checks, whether this configurer is applicable for the given {@code clazz}. If
		 * {@code true} is returned, the method {@link #configure(Object)} can be
		 * invoked.
		 *
		 * @param clazz the class to check
		 * @return {@code true}, if this configurer is applicable for the given
		 *         {@code clazz}, {@code false} otherwise.
		 */
		boolean isApplicableFor(Class<?> clazz);

		/**
		 * Configures the given {@code instance} using the configuration definition in
		 * annotation {@link AFXCellValueConfig}.
		 *
		 * @param instance   the instance to configure
		 * @param annotation the annotation applied to the instance
		 */
		void configure(Object instance, AFXCellValueConfig annotation);

		/**
		 * Returns the default implementation of type {@link IndexedCell} to use for
		 * this configurer.
		 *
		 * @return the cell implementation used as default for this configurer
		 */
		@SuppressWarnings("rawtypes")
		Class<? extends IndexedCell> defaultCellImplementation();

		/**
		 * Configures the cell factory in the given {@code cellFactoryProperty} held be
		 * the supplied {@code instance}, using the configuration in the supplied
		 * {@link AFXCellValueConfig} annotation.
		 *
		 * @param annotation          the annotation holding the desired cell value
		 *                            configuration
		 * @param columnInstance      the column instance to configure
		 * @param cellFactoryProperty the property for setting the configure cell
		 *                            factory
		 */
		@SuppressWarnings("rawtypes")
		public default void configureCellFactory(final AFXCellValueConfig annotation, final Object columnInstance,
				final ObjectProperty<Callback> cellFactoryProperty) {
			if (annotation.cellType() != IndexedCell.class || annotation.stringConverter() != StringConverter.class
					|| annotation.editable()) {
				final Class<? extends IndexedCell> cellType = determineCellType(annotation);
				final Callback cellFactory = createCellFactory(cellType, annotation.stringConverter());
				cellFactoryProperty.set(cellFactory);
			}
		}

		/**
		 * Creates a cell factory that uses the given {@code cellType} as cell and the
		 * supplied {@link StringConverter}.
		 *
		 * @param cellType             the cell type to use
		 * @param stringConverterClass the string converter type to use
		 * @return the cell factory as a callback
		 */
		@SuppressWarnings("rawtypes")
		public default Callback createCellFactory(final Class<? extends IndexedCell> cellType,
				final Class<? extends StringConverter> stringConverterClass) {
			return list -> {
				final IndexedCell cell = ReflectionUtils.instantiateClass(cellType);
				final Field converterField = ReflectionUtils.findField(cellType, "converter");
				if (converterField != null) {
					ReflectionUtils.setFieldValueBySetter(converterField, cell,
							createStringConverter(stringConverterClass));
				}
				return cell;
			};
		}

		/**
		 * Instantiates a string converter based on the supplied
		 * {@code stringConverterClass}.
		 *
		 * @param stringConverterClass the string converter class to instantiate.
		 * @return the instantiated string converter class. Uses
		 *         {@link DefaultStringConverter}, if {@code stringConverterClass} is
		 *         equal to {@code StringConverter}.
		 */
		@SuppressWarnings("rawtypes")
		public default StringConverter createStringConverter(
				final Class<? extends StringConverter> stringConverterClass) {
			return stringConverterClass != StringConverter.class
					? ReflectionUtils.instantiateClass(stringConverterClass)
					: new DefaultStringConverter();
		}

		/**
		 * Determines the cell implementation to use. Whenever the annotation attribute
		 * {@link AFXCellValueConfig##cellType()} contains a value, this type is used.
		 * If there is no value specified under {@link AFXCellValueConfig#cellType()},
		 * the default type for a particular {@link ColumnConfigurer} is returned.
		 *
		 * @param annotation the annotation
		 * @return the determined cell type
		 */
		@SuppressWarnings({ "rawtypes" })
		public default Class<? extends IndexedCell> determineCellType(final AFXCellValueConfig annotation) {
			if (annotation.cellType() != IndexedCell.class) {
				return annotation.cellType();
			}
			return defaultCellImplementation();
		}
	}

	/**
	 * Column configurer that handles both {@link TableView} and {@link TableColumn}
	 * instances.
	 *
	 * @author koster
	 *
	 */
	private static class TableColumnConfigurer implements ColumnConfigurer {

		@Override
		public boolean isApplicableFor(final Class<?> clazz) {
			return TableView.class.isAssignableFrom(clazz) || TableColumn.class.isAssignableFrom(clazz);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void configure(final Object instance, final AFXCellValueConfig annotation) {
			final TableColumn column = TableView.class.isAssignableFrom(instance.getClass())
					? findTableColumn((TableView) instance, annotation)
					: (TableColumn) instance;
			column.setCellValueFactory(new PropertyValueFactory(annotation.propertyValue()));
			configureCellFactory(annotation, column, column.cellFactoryProperty());
			if (annotation.editable()) {
				enableEditing(column, annotation.propertyValue());
			}
		}

		/**
		 * Enables edit functionality for the given {@code column}.
		 *
		 * @param column       the column to enable editing for
		 * @param propertyName the name of the property that receives the value
		 */
		private <S, T> void enableEditing(final TableColumn<S, T> column, final String propertyName) {
			final TableView<S> tableView = column.getTableView();
			tableView.setEditable(true);
			column.setEditable(true);
			column.setOnEditCommit(editEvent -> {
				final S backingInstance = editEvent.getRowValue();
				final Field field = ReflectionUtils.findField(backingInstance.getClass(), propertyName);
				ReflectionUtils.setFieldValueBySetter(field, backingInstance, editEvent.getNewValue());
			});
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private TableColumn findTableColumn(final TableView tableView, final AFXCellValueConfig annotation) {
			if ("".equals(annotation.colId()) && annotation.colIdx() == -1) {
				throw new IllegalStateException(
						"Annotation @AFXTableColum is applied to a javafx.scene.control.TableView, but annotation does not define, which column shall be configured!");
			}
			if (!"".equals(annotation.colId())) {
				final List<TableColumn> columns = tableView.getColumns();
				for (final TableColumn column : columns) {
					if (annotation.colId().equals(column.getId())) {
						return column;
					}
				}
				throw new IllegalStateException(
						"Annotation @AFXTableColum is applied to a javafx.scene.control.TableView, but requested column ID is not present!!");
			} else {
				if (tableView.getColumns().size() <= annotation.colIdx()) {
					throw new IllegalStateException(
							"Annotation @AFXTableColum is applied to a javafx.scene.control.TableView, but requested column index exceeds the number of available columns!");
				}
				return (TableColumn) tableView.getColumns().get(annotation.colIdx());
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Class<? extends IndexedCell> defaultCellImplementation() {
			return TextFieldTableCell.class;
		}

	}

	/**
	 * Column configurer that handles both {@link TreeTableView} and
	 * {@link TreeTableColumn} instances.
	 *
	 * @author koster
	 *
	 */
	private static class TreeTableColumnConfigurer implements ColumnConfigurer {

		@Override
		public boolean isApplicableFor(final Class<?> clazz) {
			return TreeTableView.class.isAssignableFrom(clazz) || TreeTableColumn.class.isAssignableFrom(clazz);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void configure(final Object instance, final AFXCellValueConfig annotation) {
			final TreeTableColumn column = TreeTableView.class.isAssignableFrom(instance.getClass())
					? findTreeTableColumn((TreeTableView) instance, annotation)
					: (TreeTableColumn) instance;
			column.setCellValueFactory(new TreeItemPropertyValueFactory(annotation.propertyValue()));
			configureCellFactory(annotation, column, column.cellFactoryProperty());
			if (annotation.editable()) {
				enableEditing(column, annotation.propertyValue());
			}
		}

		/**
		 * Enables edit functionality for the given {@code column}.
		 *
		 * @param column       the column to enable editing for
		 * @param propertyName the name of the property that receives the value
		 */
		private <S, T> void enableEditing(final TreeTableColumn<S, T> column, final String propertyName) {
			final TreeTableView<S> treeTableView = column.getTreeTableView();
			treeTableView.setEditable(true);
			column.setEditable(true);
			column.setOnEditCommit(editEvent -> {
				final S backingInstance = editEvent.getRowValue().getValue();
				final Field field = ReflectionUtils.findField(backingInstance.getClass(), propertyName);
				ReflectionUtils.setFieldValueBySetter(field, backingInstance, editEvent.getNewValue());
			});
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private TreeTableColumn findTreeTableColumn(final TreeTableView treeTableView,
				final AFXCellValueConfig annotation) {
			if ("".equals(annotation.colId()) && annotation.colIdx() == -1) {
				throw new IllegalStateException(
						"Annotation @AFXTableColum is applied to a javafx.scene.control.TreeTableView, but annotation does not define, which column shall be configured!");
			}
			if (!"".equals(annotation.colId())) {
				final List<TreeTableColumn> columns = treeTableView.getColumns();
				for (final TreeTableColumn column : columns) {
					if (annotation.colId().equals(column.getId())) {
						return column;
					}
				}
				throw new IllegalStateException(
						"Annotation @AFXTableColum is applied to a javafx.scene.control.TreeTableView, but requested column ID is not present!!");
			} else {
				if (treeTableView.getColumns().size() <= annotation.colIdx()) {
					throw new IllegalStateException(
							"Annotation @AFXTableColum is applied to a javafx.scene.control.TreeTableView, but requested column index exceeds the number of available columns!");
				}
				return (TreeTableColumn) treeTableView.getColumns().get(annotation.colIdx());
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Class<? extends IndexedCell> defaultCellImplementation() {
			return TextFieldTreeTableCell.class;
		}

	}

	/**
	 * Column configurer that handles {@link TreeView} instances.
	 *
	 * @author koster
	 *
	 */
	private static class TreeViewConfigurer implements ColumnConfigurer {

		@Override
		public boolean isApplicableFor(final Class<?> clazz) {
			return TreeView.class.isAssignableFrom(clazz);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void configure(final Object instance, final AFXCellValueConfig annotation) {
			final TreeView treeView = (TreeView) instance;
			configureCellFactory(annotation, instance, treeView.cellFactoryProperty());
			if (annotation.editable()) {
				enableEditing(treeView);
			}
		}

		/**
		 * Enables edit functionality for the given {@code column}.
		 *
		 * @param treeView the tree view to enable editing for
		 */
		private <T> void enableEditing(final TreeView<T> treeView) {
			treeView.setEditable(true);
			treeView.setOnEditCommit(editEvent -> editEvent.getTreeItem().setValue(editEvent.getNewValue()));
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Class<? extends IndexedCell> defaultCellImplementation() {
			return TextFieldTreeCell.class;
		}
	}

	/**
	 * Column configurer that handles {@link TreeView} instances.
	 *
	 * @author koster
	 *
	 */

	private static class ListViewConfigurer implements ColumnConfigurer {

		@Override
		public boolean isApplicableFor(final Class<?> clazz) {
			return ListView.class.isAssignableFrom(clazz);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void configure(final Object instance, final AFXCellValueConfig annotation) {
			final ListView listView = (ListView) instance;
			configureCellFactory(annotation, instance, listView.cellFactoryProperty());
			if (annotation.editable()) {
				enableEditing(listView);
			}
		}

		/**
		 * Enables edit functionality for the given {@code column}.
		 *
		 * @param listView the list view to enable editing for
		 */
		private <T> void enableEditing(final ListView<T> listView) {
			listView.setEditable(true);
			listView.setOnEditCommit(
					editEvent -> listView.getItems().set(editEvent.getIndex(), editEvent.getNewValue()));
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Class<? extends IndexedCell> defaultCellImplementation() {
			return TextFieldListCell.class;
		}
	}

}
