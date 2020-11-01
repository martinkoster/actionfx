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
package com.github.actionfx.core.instrumentation;

/**
 * Interface for byte code enhancer. Enhancements is possible either via
 * instrumentation agents or sub-classing. As the actual implementation is
 * highly framework specific, we abstract the technology with this interface.
 * 
 * @author koster
 *
 */
public interface ActionFXEnhancer {

	/**
	 * Installs the byte code instrumentation for ActionFX. This method must be
	 * called as early as possible in the application execution.s
	 */
	void installAgent();

	/**
	 * Enhances the given {@code originalClass} by sub-classing and adding the
	 * required behavior for ActionFX.
	 * 
	 * @param originalClass the original class to enhance
	 * @return the sub-class with enhanced behavior
	 */
	Class<?> enhanceClass(Class<?> originalClass);

	/**
	 * Defines available enhancement strategies for ActionFX.
	 * 
	 * @author koster
	 *
	 */
	public enum EnhancementStrategy {

		/**
		 * Using this strategy, a byte-code instrumentation agent is installed at
		 * runtime
		 */
		RUNTIME_INSTRUMENTATION_AGENT,

		/**
		 * Using this strategy, subclasses are created
		 */
		SUBCLASSING
	}
}
