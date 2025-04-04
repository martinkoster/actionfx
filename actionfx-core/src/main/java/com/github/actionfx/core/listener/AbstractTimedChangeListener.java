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

import java.util.Timer;
import java.util.TimerTask;

import com.github.actionfx.core.utils.AFXUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Abstract base class for change listener implementation that do potentially
 * support a delay in the invocation of a wrapped change listener.
 *
 * @author koster
 */
public abstract class AbstractTimedChangeListener {

	// property that allows to disable the listener
	protected final SimpleBooleanProperty listenerEnabled = new SimpleBooleanProperty();

	// uses a timer to call your resize method
	protected final Timer timer = new Timer(true);

	// task to execute after defined delay
	protected TimerTask task;

	// delay that has to pass in order to consider an
	// operation done
	protected long delayTime = 200;

	protected AbstractTimedChangeListener(final long delayTime, final BooleanProperty fireListenerProperty) {
		this.delayTime = delayTime;
		if (fireListenerProperty != null) {
			listenerEnabled.bind(fireListenerProperty);
		} else {
			listenerEnabled.set(true);
		}
	}

	protected void invokeListener(final Runnable listenerInvoker) {
		if (!listenerEnabled.get()) {
			return;
		}
		if (task != null) { // there was already a task scheduled from the
							// previous operation ...
			task.cancel(); // cancel it, we have a new event to consider
		}
		if (delayTime <= 0) {
			// run now
			AFXUtils.runInFxThread(listenerInvoker);
		} else {
			task = new TimerTask() // create new task
			{
				@Override
				public void run() {
					// ensure execution inside the JavaFX thread
					AFXUtils.runInFxThread(listenerInvoker);
				}
			};
			// schedule new task
			timer.schedule(task, delayTime);
		}
	}

}
