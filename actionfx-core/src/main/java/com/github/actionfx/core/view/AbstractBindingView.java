/*
 * Copyright (c) 2022 Martin Koster
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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.actionfx.core.bind.BindingModelProxy;
import com.github.actionfx.core.bind.BindingTarget;
import com.github.actionfx.core.bind.BindingTargetResolver;

/**
 * Abstract base class for an ActionFX view implementations that support a binding of a model class to JavaFX controls.
 *
 * @author koster
 *
 */
public abstract class AbstractBindingView extends AbstractValidatingView {

    // map of bound model instances and their used binding model proxies
    protected final Map<Object, BindingModelProxy> boundModelInstancesMap = Collections
            .synchronizedMap(new IdentityHashMap<>());

    @Override
    public void bind(final Object model, final BindingTargetResolver resolver) {
        final List<BindingTarget> bindingTargets = resolver.resolve(model, this);
        if (bindingTargets.isEmpty()) {
            // nothing to bind
            return;
        }
        final BindingModelProxy bindingModelProxy = new BindingModelProxy(model, bindingTargets);
        bindingModelProxy.bind();
        boundModelInstancesMap.put(model, bindingModelProxy);
    }

    @Override
    public void unbind(final Object model) {
        final BindingModelProxy bindingModelProxy = boundModelInstancesMap.get(model);
        if (bindingModelProxy == null) {
            // model is not bound
            return;
        }
        bindingModelProxy.unbind();
        boundModelInstancesMap.remove(model);
    }

    @Override
    public void unbindAll() {
        while (!boundModelInstancesMap.isEmpty()) {
            final var model = boundModelInstancesMap.keySet().iterator().next();
            unbind(model);
        }
    }
}
