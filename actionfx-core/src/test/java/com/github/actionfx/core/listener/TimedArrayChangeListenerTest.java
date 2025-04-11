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
package com.github.actionfx.core.listener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.testing.annotation.TestInFxThread;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;

/**
 * JUnit test case for {@link TimedArrayChangeListener}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class TimedArrayChangeListenerTest {

    @Test
    void testChanged_withDefaultSetting_delayOf200ms() {
        // GIVEN
        final BooleanProperty listenerExecuted = new SimpleBooleanProperty(false);
        final ObservableIntegerArray array = FXCollections.observableIntegerArray();
        final TimedArrayChangeListener<ObservableIntegerArray> tcl = new TimedArrayChangeListener<>(
                (observablearray, size, from, to) -> listenerExecuted.set(true));

        // WHEN
        tcl.onChanged(array, false, 0, 1);

        // THEN
        // the test method is not inside the Fx-Thread, the change listener spawns a new
        // thread and executes the actual wrapped listener inside the JavaFX thread...we
        // will wait here until it is completed.
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertThat(listenerExecuted.get()).isTrue();
    }

    @Test
    void testChanged_withDefaultSetting_ensureListenerIsOnlyFiredOnceDuringDelayOf500ms() {
        // GIVEN
        final AtomicInteger listenerExecuted = new AtomicInteger(0);
        final ObservableIntegerArray array = FXCollections.observableIntegerArray();
        final TimedArrayChangeListener<ObservableIntegerArray> tcl = new TimedArrayChangeListener<>(
                (observablearray, size, from, to) -> listenerExecuted.incrementAndGet(), 500);

        // WHEN (call listener 3 times)
        tcl.onChanged(array, false, 0, 1);
        tcl.onChanged(array, false, 0, 1);
        tcl.onChanged(array, false, 0, 1);

        // THEN
        // the wrapped listener is only executed once, because it was fired 3 times
        // within the period of 500ms
        WaitForAsyncUtils.sleep(800, TimeUnit.MILLISECONDS);
		assertThat(listenerExecuted.get()).isEqualTo(1);
    }

    @Test
    @TestInFxThread
    void testChanged_withDefaultSetting_withFireListenerPropertyTrue() {
        // GIVEN
        final BooleanProperty fireListenerProperty = new SimpleBooleanProperty(true);
        final BooleanProperty listenerExecuted = new SimpleBooleanProperty(false);
        final ObservableIntegerArray array = FXCollections.observableIntegerArray();
        final TimedArrayChangeListener<ObservableIntegerArray> tcl = new TimedArrayChangeListener<>(
                (observablearray, size, from, to) -> listenerExecuted.set(true), 0, fireListenerProperty);

        // WHEN
        tcl.onChanged(array, false, 0, 1);

		// THEN
		// we are now in the Fx-thread and timeout is set to 0, so the wrapped listener
		// is directly executed.
        assertThat(listenerExecuted.get()).isTrue();
    }

    @Test
    @TestInFxThread
    void testChanged_withDefaultSetting_withFireListenerPropertyFalse() {
        // GIVEN
        final BooleanProperty fireListenerProperty = new SimpleBooleanProperty(false);
        final BooleanProperty listenerExecuted = new SimpleBooleanProperty(false);
        final ObservableIntegerArray array = FXCollections.observableIntegerArray();
        final TimedArrayChangeListener<ObservableIntegerArray> tcl = new TimedArrayChangeListener<>(
                (observablearray, size, from, to) -> listenerExecuted.set(true), 0, fireListenerProperty);

        // WHEN
        tcl.onChanged(array, false, 0, 1);

		// THEN
		// the fireListenerProperty is set to false, so the wrapped listener is not
		// executed
        assertThat(listenerExecuted.get()).isFalse();
    }

}
