package com.github.actionfx.controlsfx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.WorldMapView.Country;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.view.graph.ControlWrapper;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForEachMonocleExtension;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * JUnit test case for {@link ControlWrapper}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForEachMonocleExtension.class)
@TestInFxThread
class ControlsFXControlWrapperTest {

	@Test
	void testBreadCrumbButton() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.breadCrumbButton();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "hello world");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "hello world");

	}

	@SuppressWarnings("unchecked")
	@Test
	void testBreadCrumbBar() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.breadCrumbBar();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertThat(((TreeItem<String>) wrapper.getValue()).getValue()).isEqualTo("hello");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertThat(((TreeItem<String>) wrapper.getUserValue()).getValue()).isEqualTo("hello");
	}

	@Test
	void testCheckComboBox_noSelection() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.checkComboBox();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Collections.emptyList());
	}

	@Test
	void testCheckComboBox_secondAndThirdEntryIsSelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.checkComboBox("Item 2", "Item 3");

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, "Item 3");
		assertSelectedValues(wrapper, "Item 2", "Item 3");
		assertUserValue(wrapper, Arrays.asList("Item 2", "Item 3"));
	}

	@Test
	void testCheckListView_noSelection() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.checkListView();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Collections.emptyList());
	}

	@Test
	void testCheckListView_secondAndThirdEntryIsSelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.checkListView("Item 2", "Item 3");

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, "Item 3");
		assertSelectedValues(wrapper, "Item 2", "Item 3");
		assertUserValue(wrapper, Arrays.asList("Item 2", "Item 3"));
	}

	@Test
	void testCheckTreeView_singleSelection_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.checkTreeView(false);
		final CheckTreeView<String> c = wrapper.getWrapped();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Collections.emptyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCheckTreeView_singleSelection_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.checkTreeView(true);
		final CheckTreeView<String> c = wrapper.getWrapped();
		final TreeItem<String> root = c.getRoot();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, c.getRoot());
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, root.getChildren().get(0));
		assertSelectedValues(wrapper, root.getChildren().get(0));
		assertUserValue(wrapper, Arrays.asList(root.getChildren().get(0)));
	}

	@Test
	void testGridView() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.gridView();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Arrays.asList("Item 1", "Item 2", "Item 3"));
	}

	@Test
	void testHiddenSidesPane() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.hiddenSidesPane();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValueIsNotSupported(wrapper);
	}

	@Test
	void testHyperlinkLabel() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.hyperlinkLabel();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "hello world");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "hello world");
	}

	@Test
	void testInfoOverlay() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.infoOverlay();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "hello world");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "hello world");
	}

	@Test
	void testListActionView() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.listActionView();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Arrays.asList("Item 1", "Item 2", "Item 3"));
	}

	@Test
	void testListSelectionView_noSelection() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.listSelectionView();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Collections.emptyList());
	}

	@Test
	void testListSelectionView_secondAndThirdEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.listSelectionView("Item 2", "Item 3");

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, true);
		assertValue(wrapper, null);
		assertValues(wrapper, "Item 1", "Item 2", "Item 3");
		assertSelectedValue(wrapper, "Item 3");
		assertSelectedValues(wrapper, "Item 2", "Item 3");
		assertUserValue(wrapper, Arrays.asList("Item 2", "Item 3"));
	}

	@Test
	void testMaskerPane() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.maskerPane();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 0.75);
	}

	@Test
	void testMasterDetailPane() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.masterDetailPane();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValueIsNotSupported(wrapper);
	}

	@Test
	void testNotificationPane() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.notificationPane();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "hello world");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "hello world");
	}

	@Test
	void testPlusMinusSlider() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.plusMinusSlider();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.0);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 0.0);
	}

	@Test
	void testPrefixSelectionChoiceBox_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.prefixSelectionChoiceBox(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@Test
	void testPrefixSelectionChoiceBox_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.prefixSelectionChoiceBox(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Choice 2");
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 2");
		assertSelectedValues(wrapper, "Choice 2");
		assertUserValue(wrapper, "Choice 2");

	}

	@Test
	void testPrefixSelectionComboBox_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.prefixSelectionComboBox(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@Test
	void testPrefixSelectionComboBox_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.prefixSelectionComboBox(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Choice 2");
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 2");
		assertSelectedValues(wrapper, "Choice 2");
		assertUserValue(wrapper, "Choice 2");
	}

	@Test
	void testPropertySheet() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.propertySheet();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertThat(wrapper.getItems()).hasSize(1);
		assertThat(wrapper.getItems().get(0)).isInstanceOf(Item.class);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertThat(wrapper.getUserValue()).isInstanceOf(ObservableList.class);
		assertThat((ObservableList<?>) wrapper.getItems()).hasSize(1);
		assertThat(((ObservableList<?>) wrapper.getItems()).get(0)).isInstanceOf(Item.class);
	}

	@Test
	void testRangeSlider() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.rangeSlider();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, 20.0, 80.0);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Arrays.asList(20.0, 80.0));
	}

	@Test
	void testRating() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.rating();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 3.0);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 3.0);

	}

	@Test
	void testSearchableComboBox_noEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.searchableComboBox(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, null);
	}

	@Test
	void testSearchableComboBox_secondEntrySelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.searchableComboBox(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, true);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Choice 2");
		assertValues(wrapper, "Choice 1", "Choice 2", "Choice 3");
		assertSelectedValue(wrapper, "Choice 2");
		assertSelectedValues(wrapper, "Choice 2");
		assertUserValue(wrapper, "Choice 2");
	}

	@Test
	void testSegmentedBar() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.segmentedBar();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertThat(wrapper.getItems()).hasSize(2);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertThat(wrapper.getUserValue()).isInstanceOf(ObservableList.class);
	}

	@Test
	void testSegmentedButton() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.segmentedButton();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertThat(wrapper.getItems()).hasSize(2);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertThat(wrapper.getUserValue()).isInstanceOf(ObservableList.class);
	}

	@Test
	void testSnapshotView() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.snapshotView();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValueIsNotSupported(wrapper);
	}

	@Test
	void testStatusBar() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.statusBar();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, 0.75);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, 0.75);
	}

	@Test
	void testTaskProgressView() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.taskProgressView();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertThat(wrapper.getItems()).isInstanceOf(ObservableList.class);
		assertThat((ObservableList<?>) wrapper.getItems()).hasSize(1);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertThat(wrapper.getUserValue()).isInstanceOf(ObservableList.class);
	}

	@Test
	void testCustomPasswordField() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.customPasswordField();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Password");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Password");
	}

	@Test
	void testCustomTextField() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.customTextField();

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, "Hello World");
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, "Hello World");
	}

	@Test
	void testToggleSwitch_notSelected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.toggleSwitch(false);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, false);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, false);
	}

	@Test
	void testToggleSwitch_selected() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.toggleSwitch(true);

		// WHEN and THEN
		assertSupportsValue(wrapper, true);
		assertSupportsValues(wrapper, false);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, true);
		assertValuesAreEmpty(wrapper);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, true);
	}

	@Test
	void testWorldMapView() {
		// GIVEN
		final ControlWrapper wrapper = ControlsFXControlProvider.worldMapView();

		// WHEN and THEN
		assertSupportsValue(wrapper, false);
		assertSupportsValues(wrapper, true);
		assertSupportsSelection(wrapper, false);
		assertSupportsMultiSelection(wrapper, false);
		assertValue(wrapper, null);
		assertValues(wrapper, Country.DE);
		assertSelectedValue(wrapper, null);
		assertSelectedValuesAreEmpty(wrapper);
		assertUserValue(wrapper, Arrays.asList(Country.DE));
	}

	@SuppressWarnings("unchecked")
	private static <V> void assertValue(final ControlWrapper wrapper, final V expectedValue) {
		assertThat((V) wrapper.getValue()).isEqualTo(expectedValue);
	}

	@SuppressWarnings("unchecked")
	private static <V> void assertValues(final ControlWrapper wrapper, final V... expectedValues) {
		assertThat(wrapper.getItems()).containsExactly(expectedValues);
	}

	private static <V> void assertValuesAreEmpty(final ControlWrapper wrapper) {
        assertThat(wrapper.getItems()).isEmpty();
	}

	private static void assertSupportsSelection(final ControlWrapper wrapper, final boolean expected) {
		assertThat(wrapper.supportsSelection()).isEqualTo(expected);
	}

	private static void assertSupportsMultiSelection(final ControlWrapper wrapper, final boolean expected) {
		assertThat(wrapper.supportsMultiSelection()).isEqualTo(expected);
	}

	private static void assertSupportsValue(final ControlWrapper wrapper, final boolean expected) {
		assertThat(wrapper.supportsValue()).isEqualTo(expected);
	}

	private static void assertSupportsValues(final ControlWrapper wrapper, final boolean expected) {
		assertThat(wrapper.supportsItems()).isEqualTo(expected);
	}

	@SuppressWarnings("unchecked")
	private static <V> void assertSelectedValue(final ControlWrapper wrapper, final V expectedValue) {
		assertThat((V) wrapper.getSelectedValue()).isEqualTo(expectedValue);
	}

	@SuppressWarnings("unchecked")
	private static <V> void assertSelectedValues(final ControlWrapper wrapper, final V... expectedValues) {
		assertThat(wrapper.getSelectedValues()).isEqualTo(Arrays.asList(expectedValues));
	}

	private static <V> void assertSelectedValuesAreEmpty(final ControlWrapper wrapper) {
        assertThat(wrapper.getSelectedValues()).isEmpty();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void assertUserValue(final ControlWrapper wrapper, final Object expectedValue) {
		final Object actualValue = wrapper.getUserValue();
		if (actualValue == null && expectedValue == null) {
			return;
		}
		if (expectedValue != null && List.class.isAssignableFrom(expectedValue.getClass())) {
			final List expectedValueList = (List) expectedValue;
			if (expectedValueList.isEmpty()) {
				assertThat(((List) actualValue)).isEmpty();
			} else {
				assertThat((List<Object>) actualValue).containsExactly(expectedValueList.toArray());
			}
		} else {
			assertThat(actualValue).isEqualTo(expectedValue);
		}
	}

	private static void assertUserValueIsNotSupported(final ControlWrapper wrapper) {
		assertThat(wrapper.getUserValue()).isNull();
	}
}
