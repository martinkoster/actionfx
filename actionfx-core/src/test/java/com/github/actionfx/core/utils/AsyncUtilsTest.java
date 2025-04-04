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
package com.github.actionfx.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.util.WaitForAsyncUtils;

import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JUnit test case for {@link AsyncUtils}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class AsyncUtilsTest {

	@Test
	void testExecuteAsynchronously() {
		// GIVEN
		final StringProperty result = new SimpleStringProperty("");
		final Supplier<String> supplier = () -> {
			WaitForAsyncUtils.sleep(300, TimeUnit.MILLISECONDS);
			return "Hello World";
		};
		final Consumer<String> consumer = result::set;

		// WHEN
		AsyncUtils.executeAsynchronously(supplier, consumer);

		// THEN
		// the result is not yet available
		assertThat(result.get()).isEqualTo("");

		// lets wait until supplier is done
		WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
		assertThat(result.get()).isEqualTo("Hello World");
	}

}
