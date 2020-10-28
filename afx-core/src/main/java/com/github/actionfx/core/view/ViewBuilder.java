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
import java.util.List;

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
	public ViewBuilder(Class<T> viewType) {
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
	public ViewBuilder(T view) {
		this.view = view;
	}

	public ViewBuilder<T> id(String id) {
		view.id = id;
		return this;
	}

	public ViewBuilder<T> width(int width) {
		view.width = width;
		return this;
	}

	public ViewBuilder<T> height(int height) {
		view.height = height;
		return this;
	}

	public ViewBuilder<T> windowTitle(String title) {
		view.windowTitle = title;
		return this;
	}

	public ViewBuilder<T> modalDialogue(boolean modalDialogue) {
		view.modalDialogue = modalDialogue;
		return this;
	}

	public ViewBuilder<T> maximized(boolean maximized) {
		view.maximized = maximized;
		return this;
	}

	public ViewBuilder<T> posX(int posX) {
		view.posX = posX;
		return this;
	}

	public ViewBuilder<T> posY(int posY) {
		view.posY = posY;
		return this;
	}

	public ViewBuilder<T> icon(String icon) {
		view.icon = icon;
		return this;
	}

	public ViewBuilder<T> stylesheets(List<String> stylesheets) {
		view.stylesheets.addAll(stylesheets);
		return this;
	}

	public T getView() {
		return view;
	}

}
