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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	@Test
	void testCopyClasspathFile() throws IOException {
		// GIVEN
		final Path tmpPath = Files.createTempDirectory("temp");

		// WHEN
		FileUtils.copyClasspathFile("/files/hello.txt", tmpPath.toAbsolutePath().toString());

		// THEN
		TestUtils.assertFileExists(tmpPath.toAbsolutePath().toString() + "/hello.txt");
		Files.delete(Path.of(tmpPath.toAbsolutePath().toString() + "/hello.txt"));
		Files.delete(tmpPath);
	}

	@Test
	void testCopyClasspathFile_fileDoesNotExist() throws IOException {
		// GIVEN
		final Path tmpPath = Files.createTempDirectory("temp");
		final String absoluteFolderPath = tmpPath.toAbsolutePath().toString();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> FileUtils.copyClasspathFile("/fantasyfile", absoluteFolderPath));

		// THEN
		assertThat(ex.getMessage(), containsString("Unable to create input stream from location"));
		Files.delete(tmpPath);
	}

	@Test
	void testReadFromFile() throws IOException {
		// GIVEN
		final Path tmpPath = Files.createTempDirectory("temp");
		FileUtils.copyClasspathFile("/files/hello.txt", tmpPath.toAbsolutePath().toString());

		// WHEN
		final String content = FileUtils.readFromFile(tmpPath.toAbsolutePath().toString() + "/hello.txt",
				StandardCharsets.UTF_8);

		// THEN
		assertThat(content, equalTo("Hello World!"));
		Files.delete(Path.of(tmpPath.toAbsolutePath().toString() + "/hello.txt"));
		Files.delete(tmpPath);
	}

	@Test
	void testReadFromFile_fileDoesNotExist() {
		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> FileUtils.readFromFile("/fantasyfile", StandardCharsets.UTF_8));

		// THEN
		assertThat(ex.getMessage(), containsString("Unable to read file content from location"));
	}

	@Test
	void testReadFromClasspath() {
		// WHEN
		final String content = FileUtils.readFromClasspath("/files/hello.txt", StandardCharsets.UTF_8);

		// THEN
		assertThat(content, equalTo("Hello World!"));
	}

	@Test
	void testReadFromClasspath_fileDoesNotExist() {
		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> FileUtils.readFromClasspath("/fantasyfile", StandardCharsets.UTF_8));

		// THEN
		assertThat(ex.getMessage(), containsString("Unable to create input stream from location"));
	}

	@Test
	void testReadFromInputStream() {
		// GIVEN
		final InputStream inputStream = FileUtilsTest.class.getResourceAsStream("/files/hello.txt");

		// WHEN
		final String content = FileUtils.readFromInputStream(inputStream, StandardCharsets.UTF_8);

		// THEN
		assertThat(content, equalTo("Hello World!"));

	}

	@Test
	void testUnzip_fromClasspath() throws IOException {
		// GIVEN
		final Path tmpPath = Files.createTempDirectory("temp");

		// WHEN
		FileUtils.unzip("/zip/testproject.zip", tmpPath.toFile());

		// THEN
		TestUtils.assertFileExists(Paths.get(tmpPath.toString(), "folder1").toString());
		TestUtils.assertFileExists(Paths.get(tmpPath.toString(), "folder2").toString());
		TestUtils.assertFileExists(
				Paths.get(Paths.get(tmpPath.toString(), "folder1").toString(), "text.txt").toString());
		TestUtils.assertFileExists(Paths.get(tmpPath.toString(), "README.txt").toString());
	}

	@Test
	void testUnzip_fromInputStream() throws IOException {
		// GIVEN
		final Path tmpPath = Files.createTempDirectory("temp");
		final InputStream inputStream = FileUtilsTest.class.getResourceAsStream("/zip/testproject.zip");

		// WHEN
		FileUtils.unzip(inputStream, tmpPath.toFile());

		// THEN
		TestUtils.assertFileExists(Paths.get(tmpPath.toString(), "folder1").toString());
		TestUtils.assertFileExists(Paths.get(tmpPath.toString(), "folder2").toString());
		TestUtils.assertFileExists(
				Paths.get(Paths.get(tmpPath.toString(), "folder1").toString(), "text.txt").toString());
		TestUtils.assertFileExists(Paths.get(tmpPath.toString(), "README.txt").toString());
	}
}
