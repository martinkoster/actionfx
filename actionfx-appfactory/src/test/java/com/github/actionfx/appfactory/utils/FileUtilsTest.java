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
package com.github.actionfx.appfactory.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.github.actionfx.appfactory.text.TestUtils;

/**
 * JUnit test case for {@link FileUtils}.
 *
 * @author koster
 *
 */
class FileUtilsTest {

	@Test
	void testCreateDirectories() throws IOException {
		// GIVEN
		final Path tmpPath = Files.createTempDirectory("temp");
		final String folder = tmpPath.toAbsolutePath().toString() + "/hello/world";

		// WHEN
		FileUtils.createDirectories(folder);

		// THEN
		TestUtils.assertFileExists(folder);
		Files.delete(Path.of(folder));
		Files.delete(Path.of(tmpPath.toAbsolutePath().toString() + "/hello"));
		Files.delete(tmpPath);

	}

}
