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
package com.github.actionfx.appfactory.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.actionfx.appfactory.utils.FileUtils;

/**
 * Utils for unit testing.
 *
 * @author koster
 *
 */
public final class TestUtils {

	private TestUtils() {
		// class can not be instantiated
	}

	public static void copyClasspathFile(final String classpathLocation, final Path folder) {
		try (InputStream inputStream = TestUtils.class.getResourceAsStream(classpathLocation)) {
			Files.copy(inputStream,
					Path.of(folder.toAbsolutePath().toString(), Path.of(classpathLocation).getFileName().toString()));
		} catch (final IOException e) {
			throw new IllegalStateException("Can not copy file '" + classpathLocation + "' to folder '" + folder + "'!",
					e);
		}
	}

	public static void assertFileExists(final String absoluteFilePath) {
		final Path path = Path.of(absoluteFilePath);
		assertThat(Files.exists(path)).isTrue();
	}

	public static void assertFileContentIdentical(final String actualFileAbsolutePath,
			final String expectedFileClasspath) {
		assertFileExists(actualFileAbsolutePath);
		assertThat(FileUtils.readFromFile(actualFileAbsolutePath, StandardCharsets.UTF_8)).isEqualTo(FileUtils.readFromClasspath(expectedFileClasspath, StandardCharsets.UTF_8));
	}
}
