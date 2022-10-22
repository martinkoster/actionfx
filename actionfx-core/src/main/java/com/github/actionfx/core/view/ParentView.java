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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ResourceBundle;

import com.github.actionfx.core.utils.AnnotationUtils;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.graph.NodeWrapper;

import javafx.fxml.FXML;
import javafx.scene.Parent;

/**
 * {@link View} implementation that is based on a class derived from {@link Parent}. This alternative to
 * {@link FxmlView} also to code scene graph views.
 *
 * @author koster
 *
 */
public class ParentView extends AbstractView {

    private final Object controller;

    public ParentView(final String id, final Parent parent, final Object controller) {
        this(id, parent, controller, null);
    }

    public ParentView(final String id, final Class<? extends Parent> parentClass, final Object controller) {
        this(id, parentClass, controller, null);
    }

    public ParentView(final String id, final Class<? extends Parent> parentClass, final Object controller,
            final ResourceBundle resourceBundle) {
        this(id, instantiateParentClass(parentClass, resourceBundle), controller, resourceBundle);
    }

    public ParentView(final String id, final Parent rootNode, final Object controller,
            final ResourceBundle resourceBundle) {
        super();
        this.id = id;
        this.rootNode = rootNode;
        this.controller = controller;
        this.resourceBundle = resourceBundle;
        injectViewComponents(controller);
    }

    @Override
    public Object getController() {
        return controller;
    }

    /**
     * Instantiates the given {@code parentClass} by looking up a constructor accepting a {@link ResourceBundle}
     * argument. If no such constructor is provided, the default no-argument constructor is invoked.
     *
     * @param parentClass
     *            the parent class to instantiate
     * @param resourceBundle
     *            the resource bundle to use as constructor argument
     * @return the instantiated class
     */
    static Parent instantiateParentClass(final Class<? extends Parent> parentClass,
            final ResourceBundle resourceBundle) {
        try {
            final Constructor<? extends Parent> constructorWithResourceBundle = parentClass
                    .getConstructor(ResourceBundle.class);
            return constructorWithResourceBundle.newInstance(resourceBundle);
        } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException
                | InstantiationException e) {
            // no constructor found accepting RessourceBundle - so we try the default
            // constructor next
        }
        // no constructor with ResourceBundle as argument? Then we use the default
        // no-argument constructor
        return ReflectionUtils.instantiateClass(parentClass);
    }

    /**
     * As we want to use a MVC pattern for coded scene graphs, we inject relevant view components into the given
     * controller. For this, we also scan for the {@link FXML} annotation in the controller class.
     *
     * @param controller
     *            the controller to inject view components into
     */
    protected void injectViewComponents(final Object controller) {
        final List<Field> annotatedFields = AnnotationUtils.findAllAnnotatedFields(controller.getClass(), FXML.class,
                true);
        for (final Field field : annotatedFields) {
            final NodeWrapper nodeWrapper = lookupNode(field.getName());
            ReflectionUtils.setFieldValue(field, controller, nodeWrapper.getWrapped());
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

}
