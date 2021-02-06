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
package com.github.actionfx.controlsfx;

import java.util.Optional;

import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.BreadCrumbBar.BreadCrumbButton;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.GridView;
import org.controlsfx.control.HiddenSidesPane;
import org.controlsfx.control.HyperlinkLabel;
import org.controlsfx.control.InfoOverlay;
import org.controlsfx.control.ListActionView;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.PlusMinusSlider;
import org.controlsfx.control.PrefixSelectionChoiceBox;
import org.controlsfx.control.PrefixSelectionComboBox;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.control.Rating;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.SegmentedBar;
import org.controlsfx.control.SegmentedBar.Segment;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.SnapshotView;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.WorldMapView;
import org.controlsfx.control.WorldMapView.Country;
import org.controlsfx.control.WorldMapView.Location;
import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;

import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;

/**
 * Helper-class for providing {@link ControlWrapper} instances.
 *
 * @author koster
 *
 */
public class ControlsFXControlProvider {

	private ControlsFXControlProvider() {
		// class can not be instatiated
	}

	public static ControlWrapper breadCrumbButton() {
		final BreadCrumbButton c = new BreadCrumbButton("hello world");
		return new ControlWrapper(c);
	}

	public static ControlWrapper breadCrumbBar() {
		final TreeItem<String> root = new TreeItem<>("root");
		final TreeItem<String> child = new TreeItem<>("hello");
		root.getChildren().add(child);
		final BreadCrumbBar<String> c = new BreadCrumbBar<>(child);
		return new ControlWrapper(c);
	}

	public static ControlWrapper checkListView(final String... selectedEntries) {
		final CheckListView<String> c = new CheckListView<>();
		c.getItems().add("Item 1");
		c.getItems().add("Item 2");
		c.getItems().add("Item 3");
		if (selectedEntries.length > 0) {
			for (final String entry : selectedEntries) {
				c.getCheckModel().check(entry);
			}
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper checkComboBox(final String... selectedEntries) {
		final CheckComboBox<String> c = new CheckComboBox<>();
		c.getItems().add("Item 1");
		c.getItems().add("Item 2");
		c.getItems().add("Item 3");
		if (selectedEntries.length > 0) {
			for (final String entry : selectedEntries) {
				c.getCheckModel().check(entry);
			}
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper checkTreeView(final boolean selectSecond) {
		final CheckTreeView<String> c = new CheckTreeView<>();
		final CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>("root");
		final CheckBoxTreeItem<String> child1 = new CheckBoxTreeItem<>("child1");
		final CheckBoxTreeItem<String> child2 = new CheckBoxTreeItem<>("child2");
		root.getChildren().add(child1);
		root.getChildren().add(child2);
		c.setRoot(root);
		c.getCheckModel().clearChecks();
		if (selectSecond) {
			c.getCheckModel().check(child1);
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper gridView() {
		final GridView<String> c = new GridView<>();
		c.getItems().add("Item 1");
		c.getItems().add("Item 2");
		c.getItems().add("Item 3");
		return new ControlWrapper(c);
	}

	public static ControlWrapper hiddenSidesPane() {
		final HiddenSidesPane c = new HiddenSidesPane();
		return new ControlWrapper(c);
	}

	public static ControlWrapper hyperlinkLabel() {
		final HyperlinkLabel c = new HyperlinkLabel();
		c.setText("hello world");
		return new ControlWrapper(c);
	}

	public static ControlWrapper infoOverlay() {
		final InfoOverlay c = new InfoOverlay();
		c.setText("hello world");
		return new ControlWrapper(c);
	}

	public static ControlWrapper listActionView() {
		final ListActionView<String> c = new ListActionView<>();
		c.getItems().add("Item 1");
		c.getItems().add("Item 2");
		c.getItems().add("Item 3");
		return new ControlWrapper(c);
	}

	public static ControlWrapper listSelectionView(final String... selectedItems) {
		// in CI environment, the first instantiation of this class fails
		// with an "UnsatisfiedLinkError to "javafx_font_pango" - however the second
		// succeeds...
		try {
			new ListSelectionView<>();
		} catch (final Throwable t) {
			// do nothing
		}
		final ListSelectionView<String> c = new ListSelectionView<>();
		c.getSourceItems().add("Item 1");
		c.getSourceItems().add("Item 2");
		c.getSourceItems().add("Item 3");
		for (final String selected : selectedItems) {
			c.getTargetItems().add(selected);
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper maskerPane() {
		final MaskerPane c = new MaskerPane();
		c.setProgress(0.75);
		return new ControlWrapper(c);
	}

	public static ControlWrapper masterDetailPane() {
		final MasterDetailPane c = new MasterDetailPane();
		return new ControlWrapper(c);
	}

	public static ControlWrapper notificationPane() {
		final NotificationPane c = new NotificationPane();
		c.setText("hello world");
		return new ControlWrapper(c);
	}

	public static ControlWrapper plusMinusSlider() {
		final PlusMinusSlider c = new PlusMinusSlider();
		return new ControlWrapper(c);
	}

	public static ControlWrapper prefixSelectionChoiceBox(final boolean selectSecond) {
		final PrefixSelectionChoiceBox<String> c = new PrefixSelectionChoiceBox<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper prefixSelectionComboBox(final boolean selectSecond) {
		final PrefixSelectionComboBox<String> c = new PrefixSelectionComboBox<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper propertySheet() {
		final PropertySheet c = new PropertySheet();
		c.getItems().add(new Item() {

			@Override
			public void setValue(final Object value) {
			}

			@Override
			public Object getValue() {
				return null;
			}

			@Override
			public Class<?> getType() {
				return null;
			}

			@Override
			public Optional<ObservableValue<? extends Object>> getObservableValue() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public String getCategory() {
				return null;
			}
		});
		return new ControlWrapper(c);
	}

	public static ControlWrapper rangeSlider() {
		final RangeSlider c = new RangeSlider(0.0, 100.0, 20.0, 80.0);
		return new ControlWrapper(c);
	}

	public static ControlWrapper rating() {
		final Rating c = new Rating(5);
		c.setRating(3.0);
		return new ControlWrapper(c);
	}

	public static ControlWrapper searchableComboBox(final boolean selectSecond) {
		final SearchableComboBox<String> c = new SearchableComboBox<>();
		c.getItems().add("Choice 1");
		c.getItems().add("Choice 2");
		c.getItems().add("Choice 3");
		if (selectSecond) {
			c.getSelectionModel().select(1);
		}
		return new ControlWrapper(c);
	}

	public static ControlWrapper segmentedBar() {
		final SegmentedBar<Segment> c = new SegmentedBar<>();
		c.getSegments().add(new Segment(0, "hello"));
		c.getSegments().add(new Segment(1, "world"));
		return new ControlWrapper(c);
	}

	public static ControlWrapper segmentedButton() {
		final SegmentedButton c = new SegmentedButton();
		c.getButtons().add(new ToggleButton("hello"));
		c.getButtons().add(new ToggleButton("world"));
		return new ControlWrapper(c);
	}

	public static ControlWrapper snapshotView() {
		final SnapshotView c = new SnapshotView();
		return new ControlWrapper(c);
	}

	public static ControlWrapper statusBar() {
		final StatusBar c = new StatusBar();
		c.setProgress(0.75);
		return new ControlWrapper(c);
	}

	public static ControlWrapper taskProgressView() {
		final TaskProgressView<Task<?>> c = new TaskProgressView<>();
		c.getTasks().add(new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				return null;
			}
		});
		return new ControlWrapper(c);
	}

	public static ControlWrapper customPasswordField() {
		final CustomPasswordField c = new CustomPasswordField();
		c.setText("Password");
		return new ControlWrapper(c);
	}

	public static ControlWrapper customTextField() {
		final CustomTextField c = new CustomTextField();
		c.setText("Hello World");
		return new ControlWrapper(c);
	}

	public static ControlWrapper toggleSwitch(final boolean selected) {
		final ToggleSwitch c = new ToggleSwitch();
		c.setSelected(selected);
		return new ControlWrapper(c);
	}

	public static ControlWrapper worldMapView() {
		final WorldMapView c = new WorldMapView();
		c.setSelectedCountries(FXCollections.observableArrayList(Country.DE));
		c.setSelectedLocations(FXCollections.observableArrayList(new Location("test", 42.0, 21.0)));
		return new ControlWrapper(c);
	}

}
