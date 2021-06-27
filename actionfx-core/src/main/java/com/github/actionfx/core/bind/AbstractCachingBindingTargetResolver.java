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
package com.github.actionfx.core.bind;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.actionfx.core.view.View;

import javafx.scene.control.Control;

/**
 * Abstract base class of a {@link BindingTargetResolver} implementation that
 * supports an internal caching.
 *
 * @author koster
 *
 */
public abstract class AbstractCachingBindingTargetResolver implements BindingTargetResolver {

	// the actual cache - resolution is tried against the cache
	private final Map<CacheKey, Control> bindingTargetCache = Collections.synchronizedMap(new HashMap<>());

	@Override
	public Control resolve(final View view, final Field field) {
		return bindingTargetCache.computeIfAbsent(CacheKey.of(view, field),
				cacheKey -> resolveInternal(cacheKey.getView(), cacheKey.getField()));
	}

	/**
	 * Method to be overridden to resolve the actual binding target.
	 *
	 * @param view  the view holing controls
	 * @param field the field
	 * @return the binding target control
	 */
	protected abstract Control resolveInternal(final View view, Field field);

	/**
	 * Cache key implementation for storing views and fields in hash maps.
	 *
	 * @author koster
	 *
	 */
	private static class CacheKey {

		private final View view;

		private final Field field;

		public CacheKey(final View view, final Field field) {
			this.view = view;
			this.field = field;
		}

		public static CacheKey of(final View view, final Field field) {
			return new CacheKey(view, field);
		}

		public View getView() {
			return view;
		}

		public Field getField() {
			return field;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (field == null ? 0 : field.hashCode());
			result = prime * result + (view == null ? 0 : view.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final CacheKey other = (CacheKey) obj;
			if (field == null) {
				if (other.field != null) {
					return false;
				}
			} else if (!field.equals(other.field)) {
				return false;
			}
			if (view == null) {
				if (other.view != null) {
					return false;
				}
			} else if (!view.equals(other.view)) {
				return false;
			}
			return true;
		}

	}
}
