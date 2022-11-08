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
package com.github.actionfx.core.listener;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Implementation of the {@link ChangeListener} interface that schedules a delayed action after the event occurs. In
 * case an additional event occurs before the action has been executed, the "old" task is cancelled and a new one is
 * scheduled for the new event.
 *
 * @author koster
 */
public class TimedChangeListener<T> extends AbstractTimedChangeListener
        implements ChangeListener<T> {

    private ChangeListener<T> changeListener;

    public TimedChangeListener(final ChangeListener<T> changeListener) {
        this(changeListener, 200, null);
    }

    public TimedChangeListener(final ChangeListener<T> changeListener, final long delayTime) {
        this(changeListener, delayTime, null);
    }

    public TimedChangeListener(final ChangeListener<T> changeListener, final BooleanProperty fireListenerProperty) {
        this(changeListener, 200, fireListenerProperty);
    }

    public TimedChangeListener(final ChangeListener<T> changeListener, final long delayTime,
            final BooleanProperty fireListenerProperty) {
        super(delayTime, fireListenerProperty);
        this.changeListener = changeListener;
    }

    @Override
    public void changed(final ObservableValue<? extends T> observable, final T oldValue, final T newValue) {
        invokeListener(() -> changeListener.changed(observable, oldValue, newValue));
    }
}
