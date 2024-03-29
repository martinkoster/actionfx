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
package com.github.actionfx.core.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;

/**
 * Base class for extensions that apply a logic to fields and methods that are marked with annotations.
 *
 * @param <A>
 *            the annotation
 * @param <E>
 *            the type of annotated element, e.g. a field or method.
 *
 * @author koster
 *
 */
public abstract class AbstractAnnotationBasedExtension<A extends Annotation, E extends AnnotatedElement> {

    protected final Class<A> annotationType;

    protected AbstractAnnotationBasedExtension(final Class<A> annotationType) {
        this.annotationType = annotationType;
    }

    /**
     * Overriding methods need to return a map, where the key is the annotated element (e.g. field, method) and the
     * value is a list of (potentially repeatable) annotations. In case the annotation is not repeatable, than e list
     * with size 1 is returned, containing the single annotation.
     *
     * @param clazz
     *            the class to lookup annotations for
     * @return the map of annotated elements and their corresponding annotations (potentially repeatable)
     */
    protected abstract Map<E, List<A>> lookupAnnotatedElements(Class<?> clazz);

}
