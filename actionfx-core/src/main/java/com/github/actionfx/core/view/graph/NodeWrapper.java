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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.BorderPanePosition;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/**
 * Wrapper around a {@link Node} (or other objects inside a scene graph, e.g.
 * {@link Tab} that does not inherit from {@link Node}) to provide a unique
 * access to certain properties, e.g. to the children list (just for nodes of
 * type {@link Parent}).
 *
 * @author koster
 *
 */
public class NodeWrapper {

	private static final Map<Class<?>, Field> CHILDREN_FIELD_CACHE = Collections.synchronizedMap(new HashMap<>());

	private static final Map<Class<?>, Field> ID_FIELD_CACHE = Collections.synchronizedMap(new HashMap<>());

	private static final Map<Class<?>, Boolean> SUPPORTS_MULTIPLE_CHILDREN = Collections
			.synchronizedMap(new HashMap<>());

	private static final Map<Class<?>, Boolean> SUPPORTS_SINGLE_CHILD = Collections.synchronizedMap(new HashMap<>());

	// the field name of the "onAction" property inside controls that do support it
	private static final String ON_ACTION_FIELD_NAME = "onAction";

	private final Object wrapped;

	/**
	 * Constructor accepting the node to wrap.
	 *
	 * @param node the node to wrap
	 */
	public NodeWrapper(final Object node) {
		wrapped = node;
	}

	/**
	 * Convenient factory method for creating a {@link NodeWrapper} instance.
	 *
	 * @param node the node to wrap
	 * @return the node wrapper instance
	 */
	public static NodeWrapper of(final Object node) {
		return new NodeWrapper(node);
	}

	/**
	 * Gets the wrapped instance.
	 *
	 * @return the wrapped instance
	 */
	@SuppressWarnings("unchecked")
	public <T> T getWrapped() {
		return (T) wrapped;
	}

	/**
	 * Gets the node type of the wrapped instance.
	 *
	 * @return the node type
	 */
	public Class<?> getWrappedType() {
		return wrapped.getClass();
	}

	/**
	 * Gets the ID of the wrapped node. In case the node does not inherit from
	 * {@link Node}, we try to access a field with name "id" of the wrapped type.
	 *
	 * @return the ID of the wrapped node.
	 */
	@SuppressWarnings("unchecked")
	public String getId() {
		if (isOfType(Node.class)) {
			return ((Node) wrapped).getId();
		} else {
			// we look up the "id" field, e.g. for "Tab"s
			final Field idField = lookupIdField(getWrappedType());
			if (idField == null) {
				// no ID field? so we don't have any ID here...
				return null;
			}
			final Property<String> idProperty = (Property<String>) getPropertyFieldValue(idField, wrapped);
			return idProperty != null ? idProperty.getValue() : null;
		}
	}

	/**
	 * Attaches the given {@link node} to the wrapped node, using the supplied
	 * {@link NodeAttacher} strategy.
	 *
	 * @param node     the node to attach
	 * @param attacher the attacher strategy
	 */
	public void attachNode(final Object node, final NodeAttacher attacher) {
		attacher.attach(node, this);
	}

	/**
	 * Checks, whether the wrapped object is of type {@link Parent} and can have
	 * children.
	 *
	 * @return {@code true}, if and only if the wrapped node is of type
	 *         {@link Parent}.
	 */
	public boolean isParent() {
		return isOfType(Parent.class);
	}

	/**
	 * Checks, whether the wrapped object is a {@link GridPane} and where its
	 * children are addressable by column and row.
	 *
	 * @return {@code true}, if the wrapped node is a {@link GridPane},
	 *         {@code false} otherwise.
	 */
	public boolean isGridPane() {
		return isOfType(GridPane.class);
	}

	/**
	 * Checks, whether the wrapped object is a {@link BorderPane} and where its
	 * children are addressable by the position inside the border layout (TOP, LEFT,
	 * CENTER, RIGHT, BOTTOM).
	 *
	 * @return {@code true}, if the wrapped node is a {@link BorderPane},
	 *         {@code false} otherwise.
	 */
	public boolean isBorderPane() {
		return isOfType(BorderPane.class);
	}

	/**
	 * Checks, whether the wrapped object is a {@link AnchorPane} and where its
	 * child can be positioned to fully fill the anchor pane.
	 *
	 * @return {@code true}, if the wrapped node is a {@link BorderPane},
	 *         {@code false} otherwise.
	 */
	public boolean isAnchorPane() {
		return isOfType(AnchorPane.class);
	}

	/**
	 * Checks, whether the wrapped object is a {@link TabPane}.
	 *
	 * @return {@code true}, if the wrapped node is a {@link TabPane}, {@code false}
	 *         otherwise.
	 */
	public boolean isTabPane() {
		return isOfType(TabPane.class);
	}

	/**
	 * Checks, whether the wrapped object is a {@link Tab}.
	 *
	 * @return {@code true}, if the wrapped node is a {@link Tab}, {@code false}
	 *         otherwise.
	 */
	public boolean isTab() {
		return isOfType(Tab.class);
	}

	/**
	 * Checks, whether the wrapped object is a {@link Control}.
	 *
	 * @return {@code true}, if the wrapped node is a {@link Control}, {@code false}
	 *         otherwise.
	 */
	public boolean isControl() {
		return isOfType(Control.class);
	}

	/**
	 * Checks, whether the wrapped type is of the given {@code clazz}.
	 *
	 * @param clazz the type to check
	 * @return {@code true}, if the class is of the given type, {@code false}
	 *         otherwise.
	 */
	public boolean isOfType(final Class<?> clazz) {
		return clazz.isAssignableFrom(getWrappedType());
	}

	/**
	 * Returns the children list of this node. In case the node does not have a
	 * children list, an {@link IllegalStateException} is thrown.
	 *
	 * @return the children list (never {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public <T> ObservableList<T> getChildren() {
		if (!supportsMultipleChildren()) {
			throw new IllegalStateException(
					"Class '" + wrapped.getClass().getCanonicalName() + "' does not support multiple children!");
		}
		final Field childrenField = lookupChildrenField(getWrappedType());
		final Object childrenValue = getFieldValue(childrenField, wrapped);
		if (childrenValue == null) {
			throw new IllegalStateException("Retrieving children from type '" + wrapped.getClass().getCanonicalName()
					+ "' failed, value was 'null'!");
		}
		return (ObservableList<T>) childrenValue;
	}

	/**
	 * Returns the single property containing the only child of this node. In case
	 * the node does not have a single child property, an
	 * {@link IllegalStateException} is thrown.
	 *
	 * @return the property containing the child
	 */
	@SuppressWarnings("unchecked")
	public <T> Property<T> getSingleChildProperty() {
		if (!supportsSingleChild()) {
			throw new IllegalStateException(
					"Class '" + wrapped.getClass().getCanonicalName() + "' does not support a single child!!");
		}
		final Field childField = lookupChildrenField(getWrappedType());
		final Object childValue = getPropertyFieldValue(childField, wrapped);
		if (childValue == null) {
			throw new IllegalStateException("Retrieving single child property from type '"
					+ wrapped.getClass().getCanonicalName() + "' failed, value was 'null'!");
		}
		return (Property<T>) childValue;
	}

	/**
	 * Checks, whether the wrapped {@link Node} supports multiple children. This is
	 * NOT the case e.g. for a {@link javafx.scene.control.ScrollPane} that only
	 * accepts a single node in attribute {@code content} as child.
	 *
	 * @return {@code true}, if the node supports multiple children, {@code false},
	 *         if the node supports only a single child.
	 */
	@SuppressWarnings("unchecked")
	public boolean supportsMultipleChildren() {
		final Boolean cached = SUPPORTS_MULTIPLE_CHILDREN.get(getWrappedType());
		if (cached != null) {
			return cached.booleanValue();
		}
		final Field childField = lookupChildrenField(getWrappedType());
		if (childField == null) {
			SUPPORTS_MULTIPLE_CHILDREN.put(getWrappedType(), Boolean.FALSE);
			return false;
		}
		// check, if field type is a list already...
		if (List.class.isAssignableFrom(childField.getType())) {
			SUPPORTS_MULTIPLE_CHILDREN.put(getWrappedType(), Boolean.TRUE);
			return true;
		}
		// check, if wrapped node holds a property with an observable list inside (e.g.
		// ListView)
		final Object fieldValue = getPropertyFieldValue(childField, wrapped);
		if (fieldValue != null && Property.class.isAssignableFrom(fieldValue.getClass())) {
			final Property<Object> property = (Property<Object>) fieldValue;
			final Object value = property.getValue();
			if (value != null && List.class.isAssignableFrom(value.getClass())) {
				SUPPORTS_MULTIPLE_CHILDREN.put(getWrappedType(), Boolean.TRUE);
				return true;
			}
		}
		SUPPORTS_MULTIPLE_CHILDREN.put(getWrappedType(), Boolean.FALSE);
		return false;
	}

	/**
	 * Checks, whether the wrapped {@link Node} supports a single child only. This
	 * is the case e.g. for a {@link javafx.scene.control.ScrollPane} that only
	 * accepts a single node in attribute {@code content} as child.
	 *
	 * @return {@code true}, if the node supports a single child, {@code false}, if
	 *         the node supports only multiple children.
	 */
	public boolean supportsSingleChild() {
		final Boolean cached = SUPPORTS_SINGLE_CHILD.get(getWrappedType());
		if (cached != null) {
			return cached.booleanValue();
		}
		final Field childField = lookupChildrenField(getWrappedType());
		if (childField == null) {
			SUPPORTS_SINGLE_CHILD.put(getWrappedType(), Boolean.FALSE);
			return false;
		}
		final boolean supportsSingleChild = Property.class.isAssignableFrom(childField.getType());
		SUPPORTS_SINGLE_CHILD.put(getWrappedType(), Boolean.valueOf(supportsSingleChild));
		return supportsSingleChild;
	}

	/**
	 * Checks, whether the wrapped {@link Node} is a leaf node that does not accept
	 * any children.
	 *
	 * @return {@code true}, if the wrapped node is a leaf node, {@code false}
	 *         otherwise.
	 */
	public boolean isLeafNode() {
		return !Parent.class.isAssignableFrom(getWrappedType())
				&& AnnotationUtils.findAnnotation(getWrappedType(), DefaultProperty.class) == null;
	}

	/**
	 * Returns the "on action" property of a control. In case this property is not
	 * supported, {@code null} is returned.
	 *
	 * @return the "on action" property, or {@code null}, in case the property is
	 *         not supported by the wrapped control
	 */
	@SuppressWarnings("unchecked")
	public ObjectProperty<EventHandler<ActionEvent>> getOnActionProperty() {
		final Field field = ReflectionUtils.findField(getWrappedType(), ON_ACTION_FIELD_NAME);
		if (field == null) {
			return null;
		}
		final Object value = ReflectionUtils.getFieldValueByPropertyGetter(field, getWrapped());
		if (value == null) {
			return null;
		}
		if (!ObjectProperty.class.isAssignableFrom(value.getClass())) {
			throw new IllegalStateException("OnAction property in control of type '"
					+ getWrappedType().getCanonicalName() + "' has type '" + value.getClass().getCanonicalName()
					+ "', expected was type '" + ObjectProperty.class.getCanonicalName() + "'!");
		}
		return (ObjectProperty<EventHandler<ActionEvent>>) value;
	}

	/**
	 * Gets the {@link Window} where the wrapped node is currently displayed in. In
	 * case the node has not yet been displayed, {@code null} is returned.
	 *
	 * @return the {@link Window} in that this view is displayed, or {@code null},
	 *         in case the node has not yet been displayed.
	 */
	public Window getWindow() {
		final Scene scene = getScene();
		if (scene == null) {
			return null;
		}
		return scene.getWindow();
	}

	/**
	 * Gets the {@link Scene} that the wrapped node is part of. In case the node has
	 * not yet been added to a scene, {@code null} is returned.
	 *
	 * @return the {@link Scene} that the wrapped node is part of, or {@code null},
	 *         in case the node has not been added to a scene.
	 */
	public Scene getScene() {
		if (isOfType(Node.class)) {
			final Node node = (Node) wrapped;
			return node.getScene();
		} else if (isOfType(Tab.class)) {
			final Tab tab = (Tab) wrapped;
			if (tab.getTabPane() == null) {
				return null;
			}
			return tab.getTabPane().getScene();
		}
		throw new IllegalStateException(
				"Can not access scene from node of type '" + getWrappedType().getCanonicalName() + "'!");
	}

	/**
	 * Looks up a node by its {@code id} in the scene graph hierarchy. In case there
	 * is no node with the given {@code id}, {@code null} is returned. Unlike the
	 * {@link Node#lookup(String)} method, {@code id} must not be prefixed by
	 * {@code #}.
	 *
	 * @param id the ID of the node to search for
	 * @return the found node, or {@code null}, if the node with {@code id} can not
	 *         be found.
	 */
	public NodeWrapper lookup(final String id) {
		final ObjectProperty<NodeWrapper> foundNode = new SimpleObjectProperty<>(null);
		final NodeVisitor visitor = (parent, child) -> {
			if (id.equals(child.getId())) {
				foundNode.set(child);
				return false;
			}
			return true;
		};
		applyNodeVisitorByBFS(visitor);
		return foundNode.get();
	}

	/**
	 * Applies the given {@link NodeVisitor} to this node and all children nodes of
	 * the graph by using a <i>Depth-First Search</i> approach.
	 *
	 * @param nodeVisitor the node visitor to apply
	 */
	public void applyNodeVisitorByDFS(final NodeVisitor nodeVisitor) {
		traverseRecursivelyByDFS(null, this, nodeVisitor);
	}

	/**
	 * Applies the given {@link NodeVisitor} to this node and all children nodes of
	 * the graph by using a <i>Breadth-First Search</i> approach.
	 *
	 * @param nodeVisitor the node visitor to apply
	 */
	public void applyNodeVisitorByBFS(final NodeVisitor nodeVisitor) {
		traverseByBFS(this, nodeVisitor);
	}

	/**
	 * Traverse the scene graph recursively by using a <i>Depth-First Search</i>
	 * approach, starting with {@link parent} and applies the given
	 * {@link NodeVisitor}.
	 *
	 * @param parent      the parent of {@code node} (or {@code null} in case
	 *                    {@code node} is the root node)
	 * @param node        the node to start scene graph traversal with
	 * @param nodeVisitor the node visitor to apply during scene graph traversal
	 * @return {@code true}, if the complete scene graph has been traversed,
	 *         {@code false}, in case the scene graph traversal has been cancelled
	 *         by the supplied {@link NodeVisitor}.
	 */
	public boolean traverseRecursivelyByDFS(final NodeWrapper parent, final NodeWrapper node,
			final NodeVisitor nodeVisitor) {
		if (!nodeVisitor.visit(parent, node)) {
			// traversal is aborted, when node visitor returns false
			return false;
		}
		if (node.isLeafNode()) {
			// if node is a leaf node, we continue the traversal in other branches
			return true;
		}
		// Dive down into the scene graph hierarchy
		if (node.supportsMultipleChildren()) {
			for (final Object childOfChild : node.getChildren()) {
				if (!traverseRecursivelyByDFS(node, NodeWrapper.of(childOfChild), nodeVisitor)) {
					return false;
				}
			}
		} else if (node.supportsSingleChild()) {
			final Property<Node> property = node.getSingleChildProperty();
			if (property != null && property.getValue() != null
					&& !traverseRecursivelyByDFS(node, NodeWrapper.of(property.getValue()), nodeVisitor)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Traverse the scene graph recursively by using a <i>Breadth-First Search</i>
	 * approach, starting with {@link parent} and applies the given
	 * {@link NodeVisitor}.
	 *
	 * @param root        the root node to start breadth-first search traversal for
	 * @param nodeVisitor the node visitor to apply during scene graph traversal
	 * @return {@code true}, if the complete scene graph has been traversed,
	 *         {@code false}, in case the scene graph traversal has been cancelled
	 *         by the supplied {@link NodeVisitor}.
	 */
	public static boolean traverseByBFS(final NodeWrapper root, final NodeVisitor nodeVisitor) {
		final Queue<NodeWrapper> queue = new LinkedList<>();
		// maps the child -> parent
		final Map<NodeWrapper, NodeWrapper> childToParentMap = new IdentityHashMap<>();
		queue.offer(root);
		while (!queue.isEmpty()) {
			final NodeWrapper node = queue.poll();
			if (!nodeVisitor.visit(childToParentMap.get(node), node)) {
				// if node visitor returns false, we are done with the node visit
				return false;
			}
			if (node.isLeafNode()) {
				// if node is a leaf, then we just continue with the next iteration
				continue;
			}
			if (node.supportsMultipleChildren()) {
				queue.addAll(node.getChildren().stream().map(NodeWrapper::of).map(wrapper -> {
					childToParentMap.put(node, wrapper);
					return wrapper;
				}).collect(Collectors.toList()));
			} else if (node.supportsSingleChild()) {
				final Property<Object> property = node.getSingleChildProperty();
				if (property != null && property.getValue() != null) {
					final NodeWrapper child = NodeWrapper.of(property.getValue());
					childToParentMap.put(child, node);
					queue.offer(child);
				}
			}
		}
		return true;
	}

	/**
	 * Returns a stream to all nodes, starting from this {@link NodeWrapper}. The
	 * order of nodes in the stream corresponds to a depth-first search approach.
	 *
	 * @return the nodes as a stream
	 */
	public Stream<NodeWrapper> getNodesAsStream() {
		return Stream.concat(Stream.of(this), getChildrenNodesAsStream().flatMap(NodeWrapper::getNodesAsStream));
	}

	/**
	 * Returns a stream to all children nodes, excluding this {@link NodeWrapper}.
	 * The order of nodes in the stream corresponds to a depth-first search
	 * approach.
	 *
	 * @return the children nodes as a stream
	 */
	private Stream<NodeWrapper> getChildrenNodesAsStream() {
		if (supportsMultipleChildren()) {
			return getChildren().stream().map(NodeWrapper::of);
		} else if (supportsSingleChild()) {
			final Object child = getSingleChildProperty().getValue();
			return child == null ? Stream.empty() : Stream.of(getSingleChildProperty().getValue()).map(NodeWrapper::of);
		} else {
			return Stream.empty();
		}
	}

	/**
	 * Generates a {@link DefaultAttacher} for attaching a node to this wrapper. A
	 * default attacher simply adds a node to the children list (in case of multiple
	 * children) or to the child property (in case of a single child).
	 * <p>
	 * In case you need a more sophisticated attaching strategy, you should use a
	 * different {@link NodeAttacher} for attaching a node to your
	 * {@link NodeWrapper} instance.
	 *
	 * @return the generated node attacher
	 */
	public static NodeAttacher defaultAttacher() {
		return new DefaultAttacher();
	}

	/**
	 * Generates a {@link ListAttacher} for attaching a node to this wrapper.
	 *
	 * @param index the index that the node shall be attached to
	 * @return the generated node attacher
	 */
	public static NodeAttacher listAttacher(final int index) {
		return new ListAttacher(index);
	}

	/**
	 * Generates a {@link ListLastAttacher} for attaching a node to this wrapper.
	 *
	 * @return the generated node attacher
	 */
	public static NodeAttacher listLastAttacher() {
		return new ListLastAttacher();
	}

	/**
	 * Generates a {@link ListFirstAttacher} for attaching a node to this wrapper.
	 *
	 * @return the generated node attacher
	 */
	public static NodeAttacher listFirstAttacher() {
		return new ListFirstAttacher();
	}

	/**
	 * Generates a {@link ColRowAttacher} for attaching a node to this wrapper.
	 *
	 * @return the generated node attacher
	 */
	public static NodeAttacher colRowAttacher(final int column, final int row) {
		return new ColRowAttacher(column, row);
	}

	/**
	 * Generates a {@link BorderPaneAttacher} for attaching a node to this wrapper.
	 *
	 * @param position the position inside the {@link BorderPane} that the node
	 *                 shall be attached to
	 * @return the generated node attacher
	 */
	public static NodeAttacher borderPaneAttacher(final BorderPanePosition position) {
		return new BorderPaneAttacher(position);
	}

	/**
	 * Determines the best node attacher for the configured {@link AFXNestedView}
	 * annotation.
	 *
	 * @param wrapper    the node wrapper
	 * @param nestedView the {@link AFXNestedView}
	 * @return the {@link NodeAttacher} for the supplied {@link AFXNestedView}
	 *         annotation
	 */
	public static NodeAttacher nodeAttacherFor(final NodeWrapper wrapper, final AFXNestedView nestedView) {
		if (wrapper.isBorderPane() && nestedView.attachToBorderPanePosition() != BorderPanePosition.NONE) {
			return borderPaneAttacher(nestedView.attachToBorderPanePosition());
		}
		if (wrapper.isAnchorPane() && nestedView.attachToAnchorLeft() != -1 && nestedView.attachToAnchorTop() != -1
				&& nestedView.attachToAnchorRight() != -1 && nestedView.attachToAnchorBottom() != -1) {
			return anchorPaneAttacher(nestedView.attachToAnchorLeft(), nestedView.attachToAnchorTop(),
					nestedView.attachToAnchorRight(), nestedView.attachToAnchorBottom());
		}
		if (wrapper.isGridPane() && nestedView.attachToColumn() != -1 && nestedView.attachToRow() != -1) {
			return colRowAttacher(nestedView.attachToColumn(), nestedView.attachToRow());
		}
		if (nestedView.attachToIndex() != -1) {
			return listAttacher(nestedView.attachToIndex());
		}
		return defaultAttacher();
	}

	/**
	 * Generates a {@link AnchorPaneAttacher} for attaching a node to this wrapper.
	 *
	 * @param leftAnchor   the left anchor to set
	 * @param topAnchor    the top anchor to set
	 * @param rightAnchor  the right anchor to set
	 * @param bottomAnchor the bottom anchor to set
	 * @return the generated node attacher
	 */
	public static NodeAttacher anchorPaneAttacher(final double leftAnchor, final double topAnchor,
			final double rightAnchor, final double bottomAnchor) {
		return new AnchorPaneAttacher(leftAnchor, topAnchor, rightAnchor, bottomAnchor);
	}

	/**
	 * Generates a {@link AnchorPaneAttacher} for attaching a node to this wrapper.
	 * The node attached will be configured to fully fill the anchor pane, i.e. the
	 * node itself will be stretched to the size of the anchor pane.
	 *
	 * @return the generated node attacher
	 */
	public static NodeAttacher anchorPaneFillingAttacher() {
		return new AnchorPaneAttacher(0.0, 0.0, 0.0, 0.0);
	}

	/**
	 * Looks up the field that contains the children in the given {@code nodeClass}.
	 * The annotation {@link DefaultProperty} is considered for looking up the
	 * field.
	 *
	 * @param nodeClass the node class to check for the children field
	 * @return the field containing the node's children
	 * @throws IllegalStateException in case {@code nodeClass} has no children
	 *                               property
	 */
	private Field lookupChildrenField(final Class<?> nodeClass) {
		Field propertyField = CHILDREN_FIELD_CACHE.get(nodeClass);
		if (propertyField == null) {
			// check, if the node class holds a DefaultProperty
			final DefaultProperty defaultProperty = AnnotationUtils.findAnnotation(nodeClass, DefaultProperty.class);
			final String propertyName = defaultProperty != null ? defaultProperty.value() : "children";
			propertyField = ReflectionUtils.findField(nodeClass, propertyName);
			if (propertyField == null) {
				// no child field
				return null;
			}
			CHILDREN_FIELD_CACHE.put(nodeClass, propertyField);
		}
		return propertyField;
	}

	/**
	 * Looks up the ID field of the given node class.
	 *
	 * @param nodeClass the node class to check for the ID field
	 * @return the field containing the node's ID
	 * @throws IllegalStateException in case {@code nodeClass} has no ID property
	 */
	private Field lookupIdField(final Class<?> nodeClass) {
		Field idField = ID_FIELD_CACHE.get(nodeClass);
		if (idField == null) {
			// check, if the class holds an "id" field
			idField = ReflectionUtils.findField(nodeClass, "id");
			if (idField == null) {
				// no ID field
				return null;
			}
			ID_FIELD_CACHE.put(nodeClass, idField);
		}
		return idField;
	}

	/**
	 * Gets the value from the provided {@code field}.
	 * <p>
	 * Please note, that the {@code getter} method is accessed first, because most
	 * JavaFX nodes perform a lazy-initialization of the field value in the
	 * corresponding {@code getter} method.
	 * <p>
	 * In case the access via the {@code getter} is not possible, the field is
	 * directly accessed.
	 *
	 * @param field    the field
	 * @param instance the instance that holds the field value
	 * @return the retrieved value
	 */
	private Object getFieldValue(final Field field, final Object instance) {
		try {
			return ReflectionUtils.getFieldValueByGetter(field, instance);
		} catch (final Exception e) {
			// getter does not exist, we do a direct field access as fall back
		}
		return ReflectionUtils.getFieldValue(field, instance);
	}

	/**
	 * Gets the value from the provided {@code field}.
	 * <p>
	 * Please note, that the {@code property-getter} method ({@code fieldProperty()}
	 * for field with name {@code field}) is accessed first, because most JavaFX
	 * nodes perform a lazy-initialization of the field value in the corresponding
	 * {@code getter} method.
	 * <p>
	 * In case the access via the {@code property-getter} is not possible, the field
	 * is directly accessed.
	 *
	 * @param field    the field
	 * @param instance the instance that holds the field value
	 * @return the retrieved value
	 */
	private Object getPropertyFieldValue(final Field field, final Object instance) {
		try {
			return ReflectionUtils.getFieldValueByPropertyGetter(field, instance);
		} catch (final Exception e) {
			// getter does not exist, we do a direct field access as fall back
		}
		return ReflectionUtils.getFieldValue(field, instance);
	}

	/**
	 * Interface for attaching a node to the node wrapper's children at a specified
	 * position.
	 *
	 * @author koster
	 *
	 */
	@FunctionalInterface
	public interface NodeAttacher {

		/**
		 * Overriding methods must be able to attach the supplied {@link node} to the
		 * node wrapper.
		 *
		 * @param node   the node to attach (not necessarily inheriting from
		 *               {@link Node} (e.g. it is also possible to attach {@link Tab}s
		 *               not inheriting from {@link Node} to a {@link TabPane})
		 * @param target the target node that the {@code node} shall be attached to
		 */
		void attach(Object node, NodeWrapper target);
	}

	/**
	 * Attaches a node to a list at a specified position.
	 *
	 * @author koster
	 *
	 */
	public static class ListAttacher implements NodeAttacher {

		private final int index;

		public ListAttacher(final int index) {
			this.index = index;
		}

		@Override
		public void attach(final Object node, final NodeWrapper target) {
			target.getChildren().add(index, node);
		}
	}

	/**
	 * Attaches a node as last element to a list.
	 *
	 * @author koster
	 *
	 */
	public static class ListLastAttacher implements NodeAttacher {

		@Override
		public void attach(final Object node, final NodeWrapper target) {
			target.getChildren().add(node);
		}
	}

	/**
	 * Attaches a node as first element to a list.
	 *
	 * @author koster
	 *
	 */
	public static class ListFirstAttacher implements NodeAttacher {

		@Override
		public void attach(final Object node, final NodeWrapper target) {
			target.getChildren().add(0, node);
		}
	}

	/**
	 * Specialized attacher for {@link GridPane} nodes, where the node can be
	 * attached at a specified column and row.
	 *
	 * @author koster
	 *
	 */
	public static class ColRowAttacher implements NodeAttacher {

		private final int column;

		private final int row;

		public ColRowAttacher(final int column, final int row) {
			this.column = column;
			this.row = row;
		}

		@Override
		public void attach(final Object node, final NodeWrapper target) {
			if (!target.isGridPane()) {
				throwIllegalStateExceptionForUnexpectedType(target, GridPane.class);
			}
			// check, if there is a node already at the specified position
			final GridPane gridPane = (GridPane) target.getWrapped();
			final Node child = getNodeByColumnRowIndex(column, row, gridPane);
			if (child != null) {
				gridPane.getChildren().remove(child);
			}
			final Node n = (Node) node;
			GridPane.setConstraints(n, column, row);
			gridPane.getChildren().add(n);
		}

		/**
		 * Gets the children node at {@code column} and {@code row}.
		 *
		 * @param column   the column index
		 * @param row      the row index
		 * @param gridPane the grid pane
		 * @return the child node, or {@code null}, if there is no child at the given
		 *         column and row
		 */
		private static Node getNodeByColumnRowIndex(final int column, final int row, final GridPane gridPane) {
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
	}

	/**
	 * Simple attacher that supports single child wrapper like {@link ScrollPane}
	 * (and its {@code content} property) as well as node derived from
	 * {@link Parent}. In the latter case, the node is simply append at the end of
	 * the list.
	 *
	 * @author koster
	 *
	 */
	public static class DefaultAttacher implements NodeAttacher {

		@Override
		public void attach(final Object node, final NodeWrapper target) {
			if (target.supportsSingleChild()) {
				final Property<Object> childProperty = target.getSingleChildProperty();
				childProperty.setValue(node);
			} else if (target.supportsMultipleChildren()) {
				target.getChildren().add(node);
			} else {
				throw new IllegalStateException(
						"No strategy defined to attach node of type '" + node.getClass().getCanonicalName()
								+ " to node of type '" + target.getWrapped().getClass().getCanonicalName() + "'");
			}
		}
	}

	/**
	 * Attacher that positions a node in a {@link BorderPane}.
	 *
	 * @author koster
	 *
	 */
	public static class BorderPaneAttacher implements NodeAttacher {

		private final BorderPanePosition position;

		public BorderPaneAttacher(final BorderPanePosition position) {
			this.position = position;
		}

		@Override
		public void attach(final Object node, final NodeWrapper target) {
			if (!target.isBorderPane()) {
				throwIllegalStateExceptionForUnexpectedType(target, BorderPane.class);
			}
			final BorderPane borderPane = (BorderPane) target.getWrapped();
			final Node n = (Node) node;
			switch (position) {
			case TOP:
				borderPane.setTop(n);
				break;
			case LEFT:
				borderPane.setLeft(n);
				break;
			case RIGHT:
				borderPane.setRight(n);
				break;
			case BOTTOM:
				borderPane.setBottom(n);
				break;
			case CENTER:
			default:
				borderPane.setCenter(n);
			}
		}
	}

	/**
	 * Attacher that positions a node inside an {@link AnchorPane}.
	 *
	 * @author koster
	 *
	 */
	public static class AnchorPaneAttacher implements NodeAttacher {

		private final double leftAnchor;

		private final double topAnchor;

		private final double rightAnchor;

		private final double bottomAnchor;

		public AnchorPaneAttacher(final double leftAnchor, final double topAnchor, final double rightAnchor,
				final double bottomAnchor) {
			this.leftAnchor = leftAnchor;
			this.topAnchor = topAnchor;
			this.rightAnchor = rightAnchor;
			this.bottomAnchor = bottomAnchor;
		}

		@Override
		public void attach(final Object node, final NodeWrapper target) {
			if (!target.isAnchorPane()) {
				throw new IllegalStateException("Node of type '" + target.getWrappedType().getCanonicalName()
						+ "' is not of type + '" + AnchorPane.class.getCanonicalName() + "'");
			}
			final Node n = (Node) node;
			target.getChildren().add(n);
			AnchorPane.setTopAnchor(n, topAnchor);
			AnchorPane.setBottomAnchor(n, bottomAnchor);
			AnchorPane.setLeftAnchor(n, leftAnchor);
			AnchorPane.setRightAnchor(n, rightAnchor);
		}
	}

	private static void throwIllegalStateExceptionForUnexpectedType(final NodeWrapper target,
			final Class<?> expectedType) {
		throw new IllegalStateException("Node of type '" + target.getWrappedType().getCanonicalName()
				+ "' is not of type + '" + expectedType.getCanonicalName() + "'");
	}

}
