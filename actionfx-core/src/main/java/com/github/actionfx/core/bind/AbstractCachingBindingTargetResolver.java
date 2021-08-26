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

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
	private final Map<CacheKey, List<BindingTarget>> bindingTargetCache = Collections
			.synchronizedMap(new IdentityHashMap<>());

	@Override
	public List<BindingTarget> resolve(final Object bean, final View view) {
		return bindingTargetCache.computeIfAbsent(CacheKey.from(bean.getClass(), view),
				cacheKey -> resolveInternal(bean, view));
	}

	/**
	 * Resolve the actual binding targets by checking all controls in the given
	 * {@code view}, whether these are candidates for a binding.
	 *
	 * @param bean the root bean instance holding values to bind to controls
	 * @param view the view holing controls
	 * @return the resolved {@link BindingTarget}s
	 */
	protected List<BindingTarget> resolveInternal(final Object bean, final View view) {
		final List<BindingTarget> result = view.getViewNodesAsStream()
				.filter(nodeWrapper -> Control.class.isAssignableFrom(nodeWrapper.getWrappedType()))
				.map(node -> resolveInternal((Control) node.getWrapped(), bean, view)).flatMap(List::stream)
				.collect(Collectors.toList());
		Collections.sort(result, new BindingTargetComparator());
		return result;
	}

	/**
	 * Method to be overridden to resolve the actual binding targets. Please note
	 * that one control can result in multiple binding targets (different properties
	 * of one control can be bound). Thus, this method returns a list of binding
	 * targets.
	 *
	 * @param control the control that is potential binding target
	 *
	 * @param bean    the root bean instance holding values to bind to controls
	 * @param view    the view holing controls
	 * @return the resolved {@link BindingTarget}s, or an empty list, if resolution
	 *         is not possible.
	 */
	protected abstract List<BindingTarget> resolveInternal(Control control, final Object bean, final View view);

	/**
	 * Cache key class.
	 *
	 * @author koster
	 *
	 */
	private static class CacheKey {

		private final Class<?> beanClass;

		private final View view;

		private CacheKey(final Class<?> beanClass, final View view) {
			this.beanClass = beanClass;
			this.view = view;
		}

		static CacheKey from(final Class<?> beanClass, final View view) {
			return new CacheKey(beanClass, view);
		}

		@Override
		public int hashCode() {
			return Objects.hash(beanClass, view);
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
			return Objects.equals(beanClass, other.beanClass) && Objects.equals(view, other.view);
		}

	}

	/**
	 * Comparator implementation for {@link BindingTarget} in order to guarantee
	 * that target properties of type "items" and "value" are bound before
	 * properties of type "user value".
	 *
	 * @author koster
	 *
	 */
	private static class BindingTargetComparator implements Comparator<BindingTarget> {

		@Override
		public int compare(final BindingTarget o1, final BindingTarget o2) {
			return Integer.compare(o1.getTargetProperty().getOrder(), o2.getTargetProperty().getOrder());
		}

	}

}
