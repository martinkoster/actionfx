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
package com.github.actionfx.core.view;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXNestedView;
import com.github.actionfx.core.view.graph.NodeWrapper;

/**
 * Builder for setting up instances derived from {@link AbstractView}.
 *
 * @author koster
 *
 */
public class ViewBuilder<T extends AbstractView> {

	// the instance under construction.
	private T view;

	/**
	 * Constructor that accepts the class type to build. In case this constructor is
	 * used, the supplied view type must have a no-arg default constructor.
	 *
	 * @param viewType the view type
	 */
	public ViewBuilder(final Class<T> viewType) {
		try {
			this.view = viewType.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Class '" + viewType.getCanonicalName()
					+ "' can not be instantiated with a no-arg default constructor!", e);
		}
	}

	/**
	 * Constructor that accepts an already constructed instance.
	 *
	 * @param view the constructed view instance
	 */
	public ViewBuilder(final T view) {
		this.view = view;
	}

	public ViewBuilder<T> id(final String id) {
		view.id = id;
		return this;
	}

	public ViewBuilder<T> width(final int width) {
		view.width = width;
		return this;
	}

	public ViewBuilder<T> height(final int height) {
		view.height = height;
		return this;
	}

	public ViewBuilder<T> windowTitle(final String title) {
		view.windowTitle = title;
		return this;
	}

	public ViewBuilder<T> modalDialogue(final boolean modalDialogue) {
		view.modalDialogue = modalDialogue;
		return this;
	}

	public ViewBuilder<T> maximized(final boolean maximized) {
		view.maximized = maximized;
		return this;
	}

	public ViewBuilder<T> posX(final int posX) {
		view.posX = posX;
		return this;
	}

	public ViewBuilder<T> posY(final int posY) {
		view.posY = posY;
		return this;
	}

	public ViewBuilder<T> icon(final String icon) {
		view.icon = icon;
		return this;
	}

	public ViewBuilder<T> stylesheets(final String[] stylesheets) {
		if (stylesheets == null) {
			return this;
		}
		view.stylesheets.addAll(Arrays.asList(stylesheets));
		view.applyStylesheets();
		return this;
	}

	/**
	 * Embeds the given {@link AFXNestedView}s into the {@link View} under
	 * construction.
	 *
	 * @param nestedViews the nested views to embed into the {@link View} under
	 *                    construction.
	 */
	public ViewBuilder<T> nestedViews(final List<AFXNestedView> nestedViews) {
		embedNestedViews(view, nestedViews);
		return this;
	}

	public T getView() {
		return view;
	}

	/**
	 * Embeds the given {@link AFXNestedView}s into the supplied {@link View}.
	 *
	 * @param view        the view that acts as target for the nested views
	 * @param nestedViews the nested views to embed into the supplied {@code view}.
	 */
	public static void embedNestedViews(final View view, final List<AFXNestedView> nestedViews) {
		if (nestedViews.isEmpty()) {
			return;
		}
		final ActionFX actionFX = ActionFX.getInstance();
		for (final AFXNestedView nestedView : nestedViews) {
			// get view to attach
			final View viewToAttach = actionFX.getView(nestedView.refViewId());
			if (viewToAttach == null) {
				throw new IllegalStateException("Nested view with viewId='" + nestedView.refViewId()
						+ "' does not exist, can not embed it into this view '" + view.getId() + "'!");
			}
			final NodeWrapper wrappedRootNode = NodeWrapper.of(view.getRootNode());
			final NodeWrapper target = wrappedRootNode.lookup(nestedView.attachToNodeWithId());
			if (target == null) {
				throw new IllegalStateException("Node with id='" + nestedView.attachToNodeWithId()
						+ "' does not exist, can not attach a nested view into this view '" + view.getId() + "'!");
			}
			// and finally attach the nested view to our located parent
			target.attachNode(viewToAttach.getRootNode(), NodeWrapper.nodeAttacherFor(target, nestedView));
		}
	}

}
