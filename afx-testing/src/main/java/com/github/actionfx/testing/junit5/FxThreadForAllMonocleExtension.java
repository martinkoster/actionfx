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
package com.github.actionfx.testing.junit5;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testfx.framework.junit5.ApplicationExtension;

/**
 * Extension to TestFX's {@link ApplicationExtension} that starts one JavaFX
 * thread for <b>ALL</b> test methods. This makes the JavaFX thread also
 * available outside of the test methods itself, which might me required by the
 * test fixture. For example, when testing JavaFX applications that also use a
 * Spring container, Spring might want to use the JavaFX application to
 * instantiate beans inside the JavaFX thread.
 * 
 *
 * @author MartinKoster
 */
public class FxThreadForAllMonocleExtension extends AbstractHeadlessMonocleExtension implements AfterAllCallback {

	@Override
	public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
		super.postProcessTestInstance(testInstance, context);
		startJavaFxApplication();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		stopJavaFxApplication();
	}

}
