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
package com.github.actionfx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.actionfx.core.annotation.AFXNestedView.AFXNestedViews;
import com.github.actionfx.core.view.BorderPanePosition;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

/**
 * Repeatable annotation to define a nested view. Nested views can be used to
 * composite the overall scene graph view.
 * <p>
 * You can either use this annotation on a class that is additionally annotated
 * by {@link AFXController} annotation, or you can apply it to a field that is
 * also annotated by {@code @FXML}. On class-level, this annotation is
 * repeatable and can define one or many views to nest.
 *
 * @author MartinKoster
 *
 */
@Repeatable(AFXNestedViews.class)
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AFXNestedView {

	/**
	 * The referenced view ID that shall nested in the view.
	 *
	 * @return the view ID.
	 */
	public String refViewId();

	/**
	 * The parent node ID where the nested view shall be attached to. Please note
	 * that this ID is a real node ID in the JavaFX scenegraph.
	 * <p>
	 * This field is mandatory, if this annotation is used on class-level together
	 * with a {@link AFXController} annotation.
	 * <p>
	 * If used on a field annotated by {@code @FXML}, this value is irrelevant.
	 *
	 * @return the ID
	 */
	public String attachToNodeWithId() default "";

	/**
	 * Optional index referring to the target node's children list, where the view
	 * shall be attached to.
	 *
	 * @return the index in the target node's children list
	 */
	public int attachToIndex() default -1;

	/**
	 * Optional column index in case the target node is a {@link GridPane}. Must be
	 * used together with {@link #attachInRow}.
	 *
	 * @return the column index in the target {@link GridPane}, where the view shall
	 *         be attached to
	 */
	public int attachToColumn() default -1;

	/**
	 * Optional row index in case the target node is a {@link GridPane}. Must be
	 * used together with {@link #attachInColum}.
	 *
	 * @return the row index in the target {@link GridPane}, where the view shall be
	 *         attached to
	 */
	public int attachToRow() default -1;

	/**
	 * Optional border pane position in case the target node is a
	 * {@link BorderPane}.
	 *
	 * @return the {@link BorderPanePosition}, where the view shall be attached to
	 */
	public BorderPanePosition attachToBorderPanePosition() default BorderPanePosition.NONE;

	/**
	 * Optional anchor left in case the target node is an {@link AnchorPane}. Must
	 * be used together with {@link #attachToAnchorTop()},
	 * {@link #attachToAnchorRight()} and {@link #attachToAnchorBottom()}.
	 *
	 * @return the left anchor
	 */
	public double attachToAnchorLeft() default -1;

	/**
	 * Optional anchor top in case the target node is an {@link AnchorPane}. Must be
	 * used together with {@link #attachToAnchorLeft()},
	 * {@link #attachToAnchorRight()} and {@link #attachToAnchorBottom()}.
	 *
	 * @return the top anchor
	 */
	public double attachToAnchorTop() default -1;

	/**
	 * Optional anchor right in case the target node is an {@link AnchorPane}. Must
	 * be used together with {@link #attachToAnchorTop()},
	 * {@link #attachToAnchorLeft()} and {@link #attachToAnchorBottom()}.
	 *
	 * @return the right anchor
	 */
	public double attachToAnchorRight() default -1;

	/**
	 * Optional anchor bottom in case the target node is an {@link AnchorPane}. Must
	 * be used together with {@link #attachToAnchorTop()},
	 * {@link #attachToAnchorRight()} and {@link #attachToAnchorLeft()}.
	 *
	 * @return the bottom anchor
	 */
	public double attachToAnchorBottom() default -1;

	/**
	 * Annotation to make {@link AFXNestedView} repeatable. This annotation is not
	 * intended for direct use in application code.
	 *
	 * @author MartinKoster
	 *
	 */
	@Target({ ElementType.TYPE, ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface AFXNestedViews {

		AFXNestedView[] value();
	}

}
