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
package com.github.actionfx.core.view.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.view.BorderPanePosition;
import com.github.actionfx.core.view.graph.ControlWrapperTest.ControlWithNonObjectPropertyAction;
import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JUnit test case for {@link NodeWrapper}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class NodeWrapperTest {

	@Test
	void testIsParent() {
		assertThat(wrapperWithAnchorPane().isParent()).isTrue();
		assertThat(wrapperWithCanvas().isParent()).isFalse();
	}

	@Test
	void testIsLeafNode() {
		assertThat(wrapperWithAnchorPane().isLeafNode()).isFalse();
		assertThat(wrapperWithTabPane().isLeafNode()).isFalse();
		assertThat(wrapperWithTab().isLeafNode()).isFalse();
		assertThat(wrapperWithCanvas().isLeafNode()).isTrue();
	}

	@Test
	void testIsControl() {
		assertThat(wrapperWithScrollPane().isControl()).isTrue();
		assertThat(wrapperWithAnchorPane().isControl()).isFalse();
	}

	@Test
	void testGetChildren() {
		// GIVEN
		final Label[] labels = new Label[] { label("text1"), label("text2") };

		// WHEN and THEN
		assertThat(wrapperWithAnchorPane(labels).getChildren()).containsExactly((Object[]) labels);
		assertThat(wrapperWithVBox(labels).getChildren()).containsExactly((Object[]) labels);
		assertThat(wrapperWithSplitPane(labels).getChildren()).containsExactly((Object[]) labels);
	}

	@Test
	void testGetChildren_accordion() {
		// GIVEN
		final TitledPane titledPane = new TitledPane();

		// WHEN and THEN
		assertThat(wrapperWithAccordion(titledPane).getChildren()).containsExactly(titledPane);
	}

	@Test
	void testGetChildren_nodeListIsNull() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithNodeWithNullNodeList();

		// WHEN and THEN
		assertThrows(IllegalStateException.class, wrapper::getChildren);
	}

	@Test
	void testGetChildren_nodeDoesNotSupportMultipleChildren() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithScrollPane();

		// WHEN and THEN
		assertThrows(IllegalStateException.class, wrapper::getChildren);
	}

	@Test
	void testGetChildren_nodeIsNotOfTypeParent() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithCanvas();

		// WHEN and THEN
		assertThrows(IllegalStateException.class, wrapper::getChildren);
	}

	@Test
	void testSingleChildProperty() {
		// GIVEN
		final Label label = label("text");

		// WHEN
		final Property<Node> property = wrapperWithScrollPane(label).getSingleChildProperty();

		// THEN
		assertThat(property.getValue()).isSameAs(label);
	}

	@Test
	void testSingleChildProperty_propertyIsNull() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithNodeWithNullChild();

		// WHEN and THEN
		assertThrows(IllegalStateException.class, wrapper::getSingleChildProperty);
	}

	@Test
	void testSupportsMultipleChildren() {
		assertThat(wrapperWithAnchorPane().supportsMultipleChildren()).isTrue();
		assertThat(wrapperWithVBox().supportsMultipleChildren()).isTrue();
		assertThat(wrapperWithSplitPane().supportsMultipleChildren()).isTrue();
		assertThat(wrapperWithScrollPane().supportsMultipleChildren()).isFalse(); // single child only
	}

	@Test
	void testSupportsSingleChild() {
		assertThat(wrapperWithAnchorPane().supportsSingleChild()).isFalse();
		assertThat(wrapperWithVBox().supportsSingleChild()).isFalse();
		assertThat(wrapperWithSplitPane().supportsSingleChild()).isFalse();
		assertThat(wrapperWithScrollPane().supportsSingleChild()).isTrue(); // single child only
	}

	@Test
	void testGetChildrenReadOnly_nodeSupportsMultipleChildren() {
		// GIVEN
		final Label[] labels = new Label[] { label("text1"), label("text2") };
		final NodeWrapper wrapper = wrapperWithAnchorPane(labels);

		// WHEN
		final List<Node> result = wrapper.getChildrenReadOnly();

		// THEN
		assertThat(result).hasSize(2).containsExactly(labels);
	}

	@Test
	void testGetChildrenReadOnly_nodeSupportsSingleChild() {
		// GIVEN
		final Label label = label("text1");
		final NodeWrapper wrapper = wrapperWithScrollPane(label);

		// WHEN
		final List<Node> result = wrapper.getChildrenReadOnly();

		// THEN
		assertThat(result).hasSize(1).containsExactly(label);
	}

	@Test
	void testGetChildrenReadOnly_nodeSupportsNoChildren() {
		assertThat(wrapperWithCanvas().getChildrenReadOnly()).isEmpty();
	}

	@Test
	void testAttachNode_defaultAttacher_singleChild() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithScrollPane(label("will_be_replaced"));
		final Label label = label("attached");

		// WHEN
		wrapper.attachNode(label, NodeWrapper.defaultAttacher());

		// THEN
		assertThat(wrapper.getSingleChildProperty().getValue()).isSameAs(label);
	}

	@Test
	void testAttachNode_defaultAttacher_singleChild_noChildrenPresent_contentPropertyIsLazilyInitialized() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithScrollPane();
		final Label label = label("attached");

		// WHEN
		wrapper.attachNode(label, NodeWrapper.defaultAttacher());

		// THEN
		assertThat(wrapper.getSingleChildProperty().getValue()).isSameAs(label);
	}

	@Test
	void testAttachNode_defaultAttacher_multipleChildren() {
		// GIVEN
		final Label firstChild = label("first");
		final NodeWrapper wrapper = wrapperWithVBox(firstChild);
		final Label attached = label("attached");

		// WHEN
		wrapper.attachNode(attached, NodeWrapper.defaultAttacher());

		// THEN
		assertThat(wrapper.getChildren()).containsExactly(firstChild, attached);
	}

	@Test
	void testAttachNode_defaultAttacher_tabPane() {
		// GIVEN
		final Tab tab = new Tab();
		final NodeWrapper wrapper = wrapperWithTabPane();

		// WHEN
		wrapper.attachNode(tab, NodeWrapper.defaultAttacher());

		// THEN
		assertThat(wrapper.getChildren()).contains(tab);
	}

	@Test
	void testAttachNode_listAttacher_multipleChildren() {
		// GIVEN
		final Label firstChild = label("first");
		final Label secondChild = label("second");
		final NodeWrapper wrapper = wrapperWithVBox(firstChild, secondChild);
		final Label attached = label("attached");

		// WHEN (position it between first and second child)
		wrapper.attachNode(attached, NodeWrapper.listAttacher(1));

		// THEN (it is position in the middle)
		assertThat(wrapper.getChildren()).containsExactly(firstChild, attached, secondChild);
	}

	@Test
	void testAttachNode_listFirstAttacher_multipleChildren() {
		// GIVEN
		final Label firstChild = label("first");
		final NodeWrapper wrapper = wrapperWithVBox(firstChild);
		final Label attached = label("attached");

		// WHEN
		wrapper.attachNode(attached, NodeWrapper.listFirstAttacher());

		// THEN (it is positioned as first child)
		assertThat(wrapper.getChildren()).containsExactly(attached, firstChild);
	}

	@Test
	void testAttachNode_listLastAttacher_multipleChildren() {
		// GIVEN
		final Label firstChild = label("first");
		final NodeWrapper wrapper = wrapperWithVBox(firstChild);
		final Label attached = label("attached");

		// WHEN
		wrapper.attachNode(attached, NodeWrapper.listLastAttacher());

		// THEN (it is positioned as last child)
		assertThat(wrapper.getChildren()).containsExactly(firstChild, attached);
	}

	@Test
	void testAttachNode_colRowAttacher() {
		// GIVEN (a 3x3 grid with 9 children)
		final Label firstChild = label("one");
		final Label secondChild = label("two");
		final Label thirdChild = label("three");
		final Label fourthChild = label("four");
		final Label fifthChild = label("five");
		final Label sixthChild = label("six");
		final Label seventhChild = label("seven");
		final Label eightChild = label("eight");
		final Label ninthChild = label("nine");
		final NodeWrapper wrapper = wrapperWithGridPane(3, 3, firstChild, secondChild, thirdChild, fourthChild,
				fifthChild, sixthChild, seventhChild, eightChild, ninthChild);
		final Label attached = label("attached");

		// WHEN (attach node in the middle and replace 5th child)
		wrapper.attachNode(attached, NodeWrapper.colRowAttacher(1, 1));

		// THEN (it is positioned where the 5th child before)
		final GridPane gridPane = (GridPane) wrapper.getWrapped();
		assertThat(getNodeByRowColumnIndex(0, 0, gridPane)).isSameAs(firstChild);
		assertThat(getNodeByRowColumnIndex(0, 1, gridPane)).isSameAs(secondChild);
		assertThat(getNodeByRowColumnIndex(0, 2, gridPane)).isSameAs(thirdChild);
		assertThat(getNodeByRowColumnIndex(1, 0, gridPane)).isSameAs(fourthChild);
		assertThat(getNodeByRowColumnIndex(1, 1, gridPane)).isSameAs(attached); // attached replaces fifthChild
		assertThat(getNodeByRowColumnIndex(1, 2, gridPane)).isSameAs(sixthChild);
		assertThat(getNodeByRowColumnIndex(2, 0, gridPane)).isSameAs(seventhChild);
		assertThat(getNodeByRowColumnIndex(2, 1, gridPane)).isSameAs(eightChild);
		assertThat(getNodeByRowColumnIndex(2, 2, gridPane)).isSameAs(ninthChild);
	}

	@Test
	void testAttachNode_borderPaneAttacher() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithBorderPane();
		final Label top = label("top");
		final Label left = label("left");
		final Label right = label("right");
		final Label bottom = label("bottom");
		final Label center = label("center");

		// WHEN
		wrapper.attachNode(top, NodeWrapper.borderPaneAttacher(BorderPanePosition.TOP));
		wrapper.attachNode(left, NodeWrapper.borderPaneAttacher(BorderPanePosition.LEFT));
		wrapper.attachNode(right, NodeWrapper.borderPaneAttacher(BorderPanePosition.RIGHT));
		wrapper.attachNode(bottom, NodeWrapper.borderPaneAttacher(BorderPanePosition.BOTTOM));
		wrapper.attachNode(center, NodeWrapper.borderPaneAttacher(BorderPanePosition.CENTER));

		// THEN
		final BorderPane borderPane = (BorderPane) wrapper.getWrapped();
		assertThat(borderPane.getTop()).isSameAs(top);
		assertThat(borderPane.getLeft()).isSameAs(left);
		assertThat(borderPane.getRight()).isSameAs(right);
		assertThat(borderPane.getBottom()).isSameAs(bottom);
		assertThat(borderPane.getCenter()).isSameAs(center);
	}

	@Test
	void testAttachNode_anchorPaneAttacher() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithAnchorPane();
		final Label label = label("Text");

		// WHEN
		wrapper.attachNode(label, NodeWrapper.anchorPaneAttacher(10.0, 20.0, 30.0, 40.0));

		// THEN
		assertThat(wrapper.getChildren()).hasSize(1);
		assertThat(wrapper.getChildren().get(0)).isSameAs(label);
		assertThat(AnchorPane.getLeftAnchor((Node) wrapper.getChildren().get(0))).isEqualTo(10.0);
		assertThat(AnchorPane.getTopAnchor((Node) wrapper.getChildren().get(0))).isEqualTo(20.0);
		assertThat(AnchorPane.getRightAnchor((Node) wrapper.getChildren().get(0))).isEqualTo(30.0);
		assertThat(AnchorPane.getBottomAnchor((Node) wrapper.getChildren().get(0))).isEqualTo(40.0);
	}

	@Test
	void testAttachNode_anchorPaneFillingAttacher() {
		// GIVEN
		final NodeWrapper wrapper = wrapperWithAnchorPane();
		final Label label = label("Text");

		// WHEN
		wrapper.attachNode(label, NodeWrapper.anchorPaneFillingAttacher());

		// THEN
		assertThat(wrapper.getChildren()).hasSize(1);
		assertThat(wrapper.getChildren().get(0)).isSameAs(label);
		assertThat(AnchorPane.getLeftAnchor((Node) wrapper.getChildren().get(0))).isEqualTo(0.0);
		assertThat(AnchorPane.getTopAnchor((Node) wrapper.getChildren().get(0))).isEqualTo(0.0);
		assertThat(AnchorPane.getRightAnchor((Node) wrapper.getChildren().get(0))).isEqualTo(0.0);
		assertThat(AnchorPane.getBottomAnchor((Node) wrapper.getChildren().get(0))).isEqualTo(0.0);
	}

	@Test
	void testGetId_derivedFromNode() {
		// GIVEN
		final HBox hbox = new HBox();
		hbox.setId("hbox");

		// WHEN and THEN
		assertThat(NodeWrapper.of(hbox).getId()).isEqualTo("hbox");
	}

	@Test
	void testGetId_NotDerivedFromNode() {
		// GIVEN
		final Tab tab = new Tab();
		tab.setId("tab");

		// WHEN and THEN
		assertThat(NodeWrapper.of(tab).getId()).isEqualTo("tab");
	}

	@Test
	void testApplyNodeVisitorByDFS() {
		// GIVEN
		final List<String> ids = new ArrayList<>();
		final NodeVisitor visitor = (parent, child) -> {
			if (child.getId() != null) {
				ids.add(child.getId());
			}
			return true;
		}; // collect IDs in order of appearance
		final NodeWrapper nodeWrapper = wrapperWithHierarchy();

		// WHEN
		nodeWrapper.applyNodeVisitorByDFS(visitor);

		// THEN (check expected visited order of depth-first search)
		assertThat(ids).containsExactly("borderPane", "accordion", "titledPane", "scrollPane", "listView", "tabPane", "tabOne", "tabTwo", "canvas", "vbox", "anchorPane", "gridPane");
	}

	@Test
	void testApplyNodeVisitorByDFS_cancelAtCertainNode() {
		// GIVEN
		final List<String> ids = new ArrayList<>();
		final NodeVisitor visitor = (parent, child) -> {
			if (child.getId() != null) {
				ids.add(child.getId());
			}
			// stop at "tabTwo"
			return !"tabTwo".equals(child.getId());
		};
		final NodeWrapper nodeWrapper = wrapperWithHierarchy();

		// WHEN
		nodeWrapper.applyNodeVisitorByDFS(visitor);

		// THEN (check expected visited order of depth-first search, stopped at
		// "tabTwo")
		assertThat(ids).containsExactly("borderPane", "accordion", "titledPane", "scrollPane", "listView", "tabPane", "tabOne", "tabTwo");
	}

	@Test
	void testApplyNodeVisitorByBFS() {
		// GIVEN
		final List<String> ids = new ArrayList<>();
		final NodeVisitor visitor = (parent, child) -> {
			if (child.getId() != null) {
				ids.add(child.getId());
			}
			return true;
		}; // collect IDs in order of appearance
		final NodeWrapper nodeWrapper = wrapperWithHierarchy();

		// WHEN
		nodeWrapper.applyNodeVisitorByBFS(visitor);

		// THEN (check expected visited order of breadth-first search)
		assertThat(ids).containsExactly("borderPane", "accordion", "scrollPane", "tabPane", "vbox", "titledPane", "listView", "tabOne", "tabTwo", "anchorPane", "gridPane", "canvas");
	}

	@Test
	void testApplyNodeVisitorByBFS_cancelAtCertainNode() {
		// GIVEN
		final List<String> ids = new ArrayList<>();
		final NodeVisitor visitor = (parent, child) -> {
			ids.add(child.getId());
			// stop at "tabTwo"
			return !"tabTwo".equals(child.getId());
		};
		final NodeWrapper nodeWrapper = wrapperWithHierarchy();

		// WHEN
		nodeWrapper.applyNodeVisitorByBFS(visitor);

		// THEN (check expected visited order of breadth-first search, stopped at
		// "tabTwo")
		assertThat(ids).containsExactly("borderPane", "accordion", "scrollPane", "tabPane", "vbox", "titledPane", "listView", "tabOne", "tabTwo");
	}

	@Test
	void testGetNodesAsStream() {
		// GIVEN
		final NodeWrapper nodeWrapper = wrapperWithHierarchy();

		// WHEN
		final List<String> ids = nodeWrapper.getNodesAsStream().map(NodeWrapper::getId).filter(Objects::nonNull)
				.toList();

		// THEN (check expected visited order of depth-first search)
		assertThat(ids).containsExactly("borderPane", "accordion", "titledPane", "scrollPane", "listView", "tabPane", "tabOne", "tabTwo", "canvas", "vbox", "anchorPane", "gridPane");
	}

	@Test
	void testLookup_nodesExist() {
		assertThat((Object) wrapperWithHierarchy().lookup("borderPane").getWrapped()).isInstanceOf(BorderPane.class);
		assertThat((Object) wrapperWithHierarchy().lookup("accordion").getWrapped()).isInstanceOf(Accordion.class);
		assertThat((Object) wrapperWithHierarchy().lookup("scrollPane").getWrapped()).isInstanceOf(ScrollPane.class);
		assertThat((Object) wrapperWithHierarchy().lookup("tabPane").getWrapped()).isInstanceOf(TabPane.class);
		assertThat((Object) wrapperWithHierarchy().lookup("vbox").getWrapped()).isInstanceOf(VBox.class);
		assertThat((Object) wrapperWithHierarchy().lookup("listView").getWrapped()).isInstanceOf(ListView.class);
		assertThat((Object) wrapperWithHierarchy().lookup("tabOne").getWrapped()).isInstanceOf(Tab.class);
		assertThat((Object) wrapperWithHierarchy().lookup("tabTwo").getWrapped()).isInstanceOf(Tab.class);
		assertThat((Object) wrapperWithHierarchy().lookup("anchorPane").getWrapped()).isInstanceOf(AnchorPane.class);
		assertThat((Object) wrapperWithHierarchy().lookup("gridPane").getWrapped()).isInstanceOf(GridPane.class);
		assertThat((Object) wrapperWithHierarchy().lookup("canvas").getWrapped()).isInstanceOf(Canvas.class);
	}

	@Test
	@TestInFxThread
	void testGetWindow() {
		// GIVEN
		final Stage stage = new Stage();
		final VBox root = new VBox();
		final Label label = new Label();
		root.getChildren().add(label);
		final Scene scene = new Scene(root);
		stage.setScene(scene);

		// WHEN
		final Window window = NodeWrapper.of(label).getWindow();

		// THEN
		assertThat(window).isNotNull().isSameAs(stage);
	}

	@Test
	void testGetScene() {
		// GIVEN
		final VBox root = new VBox();
		final Label label = new Label();
		root.getChildren().add(label);
		final Scene scene = new Scene(root);

		// WHEN
		final Scene nodeScene = NodeWrapper.of(label).getScene();

		// THEN
		assertThat(nodeScene).isNotNull().isSameAs(scene);
	}

	@Test
	void testLookup_nodeDoesNotExist() {
		assertThat(wrapperWithHierarchy().lookup("fantasyNode")).isNull();
	}

	@Test
	void testGetOnActionProperty() {
		// GIVEN
		final NodeWrapper wrapper = ControlWrapperProvider.button();

		// WHEN
		final ObjectProperty<EventHandler<ActionEvent>> onActionProperty = wrapper.getOnActionProperty();

		// THEN
		assertThat(onActionProperty).isNotNull();
	}

	@Test
	void testGetOnActionProperty_propertyNotOfExpectedTypeObjectProperty() {
		// GIVEN
		final NodeWrapper wrapper = ControlWrapper.of(new ControlWithNonObjectPropertyAction());

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class, wrapper::getOnActionProperty);

		// THEN
		assertThat(ex.getMessage()).contains("OnAction property in control of type 'com.github.actionfx.core.view.graph.ControlWrapperTest.ControlWithNonObjectPropertyAction' has type 'javafx.beans.property.SimpleStringProperty', expected was type 'javafx.beans.property.ObjectProperty'!");
	}

	@Test
	void testGetOnActionPropertyField() {
		// WHEN
		final Field onActionProperty = NodeWrapper.getOnActionPropertyField(Button.class);

		// THEN
		assertThat(onActionProperty).isNotNull();
	}

	@Test
	void testGetOnActionPropertyField_fieldUnavailable() {
		// WHEN
		final Field onActionProperty = NodeWrapper.getOnActionPropertyField(BorderPane.class);

		// THEN
		assertThat(onActionProperty).isNull();
	}

	@Test
	void testGetDecorationChildren_isControl() {
		// GIVEN
		final NodeWrapper nodeWrapper = ControlWrapperProvider.button();

		// WHEN and THEN
		assertThat(nodeWrapper.getDecorationChildren()).isEmpty();
	}

	@Test
	void testGetDecorationChildren_isNotControl_supportsMultipleChildren() {
		// GIVEN
		final NodeWrapper nodeWrapper = NodeWrapper.of(new AnchorPane());

		// WHEN and THEN
		assertThat(nodeWrapper.getDecorationChildren()).isEmpty();
	}

	@Test
	void testGetDecorationChildren_isNotControl_doesNotSupportsMultipleChildren() {
		// GIVEN
		final NodeWrapper nodeWrapper = NodeWrapper.of(new Tab());

		// WHEN and THEN
		assertThat(nodeWrapper.getDecorationChildren()).isEmpty();
	}

	@Test
	void testDecorateWithDecorationNode_isControl() {
		// GIVEN
		final NodeWrapper nodeWrapper = ControlWrapperProvider.button();
		final Button button = nodeWrapper.getWrapped();
		final Node decorationNode = new ImageView();

		// WHEN
		nodeWrapper.decorateWithDecorationNode(decorationNode);
		button.setSkin(new ButtonSkin(button));

		// THEN
		assertThat(nodeWrapper.getDecorationChildren()).contains(decorationNode);
	}

	@Test
	void testDecorateWithDecorationNode_isNotControl_doesNotSupportMultipleChildren() {
		// GIVEN
		final NodeWrapper nodeWrapper = NodeWrapper.of(new Tab());
		final Node decorationNode = new ImageView();

		// WHEN
		nodeWrapper.decorateWithDecorationNode(decorationNode);

		// THEN
		assertThat(nodeWrapper.getDecorationChildren()).isEmpty(); // not supported...

	}

	@Test
	void testDecorateWithDecorationNode_isNotControl_supportsMultipleChildren() {
		// GIVEN
		final NodeWrapper nodeWrapper = NodeWrapper.of(new AnchorPane());
		final Node decorationNode = new ImageView();

		// WHEN
		nodeWrapper.decorateWithDecorationNode(decorationNode);

		// THEN
		assertThat(nodeWrapper.getDecorationChildren()).contains(decorationNode);
	}

	private static NodeWrapper wrapperWithAccordion(final TitledPane titledPane) {
		return new NodeWrapper(new Accordion(titledPane));
	}

	private static NodeWrapper wrapperWithAnchorPane(final Node... children) {
		return new NodeWrapper(new AnchorPane(children));
	}

	private static NodeWrapper wrapperWithVBox(final Node... children) {
		return new NodeWrapper(new VBox(children));
	}

	private static NodeWrapper wrapperWithSplitPane(final Node... children) {
		return new NodeWrapper(new SplitPane(children));
	}

	private static NodeWrapper wrapperWithScrollPane() {
		return new NodeWrapper(new ScrollPane());
	}

	private static NodeWrapper wrapperWithCanvas() {
		return new NodeWrapper(new Canvas());
	}

	private static NodeWrapper wrapperWithScrollPane(final Node child) {
		return new NodeWrapper(new ScrollPane(child));
	}

	private static NodeWrapper wrapperWithBorderPane() {
		return new NodeWrapper(new BorderPane());
	}

	private static NodeWrapper wrapperWithAnchorPane() {
		return new NodeWrapper(new AnchorPane());
	}

	private static NodeWrapper wrapperWithNodeWithNullNodeList() {
		return new NodeWrapper(new NodeWithNullNodeList());
	}

	private static NodeWrapper wrapperWithNodeWithNullChild() {
		return new NodeWrapper(new NodeWithNullChild());
	}

	private static NodeWrapper wrapperWithTabPane() {
		return new NodeWrapper(new TabPane());
	}

	private static NodeWrapper wrapperWithTab() {
		return new NodeWrapper(new Tab());
	}

	/**
	 * Wraps a more complex hierarchy of different node types.
	 * <p>
	 *
	 * <pre>
	 * BorderPane
	 *    |
	 *    |--- ScrollPane (LEFT)
	 *    |         |
	 *    |         |---- ListView
	 *    |
	 *    |--- TabPane (CENTER)
	 *    |         |
	 *    |         |---- TabOne
	 *    |         |---- TabTwo
	 *    |                  |
	 *    |                  |--- Canvas
	 *    |
	 *    |--- VBox (RIGHT)
	 *           |
	 *           |---- AnchorPane
	 *           |---- GridPane
	 * </pre>
	 *
	 * @return a wrapper with a complex hierarchy
	 */
	private static NodeWrapper wrapperWithHierarchy() {
		final BorderPane borderPane = new BorderPane();
		borderPane.setId("borderPane");

		final Accordion accordion = new Accordion();
		accordion.setId("accordion");

		final TitledPane titledPane = new TitledPane();
		titledPane.setId("titledPane");
		accordion.getPanes().add(titledPane);

		final ScrollPane scrollPane = new ScrollPane();
		scrollPane.setId("scrollPane");

		final ListView<String> listView = new ListView<>();
		listView.setId("listView");

		final TabPane tabPane = new TabPane();
		tabPane.setId("tabPane");

		final Tab tabOne = new Tab();
		tabOne.setId("tabOne");

		final Tab tabTwo = new Tab();
		tabTwo.setId("tabTwo");

		final Canvas canvas = new Canvas();
		canvas.setId("canvas");

		final VBox vbox = new VBox();
		vbox.setId("vbox");

		final AnchorPane anchorPane = new AnchorPane();
		anchorPane.setId("anchorPane");

		final GridPane gridPane = new GridPane();
		gridPane.setId("gridPane");

		borderPane.setTop(accordion);
		borderPane.setLeft(scrollPane);
		borderPane.setCenter(tabPane);
		borderPane.setRight(vbox);

		scrollPane.setContent(listView);

		tabPane.getTabs().add(tabOne);
		tabPane.getTabs().add(tabTwo);

		tabTwo.setContent(canvas);

		vbox.getChildren().add(anchorPane);
		vbox.getChildren().add(gridPane);

		return NodeWrapper.of(borderPane);
	}

	private static NodeWrapper wrapperWithGridPane(final int numCols, final int numRows, final Node... children) {
		final GridPane gridPane = new GridPane();
		for (int i = 0; i < numCols; i++) {
			final ColumnConstraints colConst = new ColumnConstraints();
			colConst.setPercentWidth(100.0 / numCols);
			gridPane.getColumnConstraints().add(colConst);
		}
		for (int i = 0; i < numRows; i++) {
			final RowConstraints rowConst = new RowConstraints();
			rowConst.setPercentHeight(100.0 / numRows);
			gridPane.getRowConstraints().add(rowConst);
		}
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				final int colIdx = i % numCols;
				final int rowIdx = i / numCols;
				gridPane.add(children[i], colIdx, rowIdx);
			}
		}
		return new NodeWrapper(gridPane);
	}

	private static Node getNodeByRowColumnIndex(final int row, final int column, final GridPane gridPane) {
		Node result = null;
		final ObservableList<Node> childrens = gridPane.getChildren();

		for (final Node node : childrens) {
			if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
				result = node;
				break;
			}
		}

		return result;
	}

	private static Label label(final String text) {
		return new Label(text);
	}

	@DefaultProperty("nodes")
	public static class NodeWithNullNodeList extends Parent {

		private final ObservableList<Node> nodes = null;

		public ObservableList<Node> getNodes() {
			// return null nodes
			return nodes;
		}
	}

	@DefaultProperty("node")
	public static class NodeWithNullChild extends Parent {

		private final Property<Node> node = null;

		public Property<Node> nodeProperty() {
			// return null node
			return node;
		}
	}

}
