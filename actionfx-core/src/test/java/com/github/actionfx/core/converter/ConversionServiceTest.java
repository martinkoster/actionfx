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
package com.github.actionfx.core.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;

/**
 * JUnit test case for {@link ConversionService}.
 *
 * @author koster
 *
 */
class ConversionServiceTest {

	@Test
	void testConvert_typeFile() throws IOException {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));
		final File file = Files.createTempFile("junit", "-tmp").toFile();

		// WHEN
		assertThat(service.convert(file, String.class), equalTo(file.getAbsolutePath()));
		assertThat(service.convert(file, URI.class), equalTo(file.toURI()));
		assertThat(service.convert(file, Path.class), equalTo(file.toPath()));
	}

	@Test
	void testConvert_typePath() throws IOException {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));
		final File file = Files.createTempFile("junit", "-tmp").toFile();

		// WHEN
		assertThat(service.convert(file.toPath(), String.class), equalTo(file.getAbsolutePath()));
		assertThat(service.convert(file.toPath(), URI.class), equalTo(file.toURI()));
		assertThat(service.convert(file.toPath(), File.class), equalTo(file));
	}

	@Test
	void testConvert_typeURI() throws IOException {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));
		final File file = Files.createTempFile("junit", "-tmp").toFile();

		// WHEN
		assertThat(service.convert(file.toURI(), String.class), equalTo(file.toURI().toString()));
		assertThat(service.convert(file.toURI(), Path.class), equalTo(file.toPath()));
		assertThat(service.convert(file.toURI(), File.class), equalTo(file));
	}

	@Test
	void testConvert_typeString() throws IOException {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));
		final File file = Files.createTempFile("junit", "-tmp").toFile();

		// WHEN
		assertThat(service.convert(file.toURI().toString(), URI.class), equalTo(file.toURI()));
		assertThat(service.convert(file.getAbsolutePath(), Path.class), equalTo(file.toPath()));
		assertThat(service.convert(file.getAbsolutePath(), File.class), equalTo(file));
	}

	@Test
	void testConvert_fromStringToNumber() {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));
		final String number = "124.5";

		// WHEN and THEN
		assertThat(service.convert(number, Integer.class), equalTo(Integer.valueOf(number)));
		assertThat(service.convert(number, Float.class), equalTo(Float.valueOf(number)));
		assertThat(service.convert(number, Double.class), equalTo(Double.valueOf(number)));
		assertThat(service.convert(number, Short.class), equalTo(Short.valueOf(number)));
		assertThat(service.convert(number, Byte.class), equalTo(Byte.valueOf(number)));
	}

	@Test
	void testConvert_fromNumberToNumber() {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));

		// WHEN and THEN

	}

	@Test
	void testConvert_conversionNotPossible() throws IOException {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));
		final File file = Files.createTempFile("junit", "-tmp").toFile();

		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> service.convert(file, Integer.class));
		assertThat(ex.getMessage(), equalTo("Unable to convert type 'java.io.File' to type 'java.lang.Integer'!"));
	}

	@Test
	void testConvert_targetTypeIsCompatible() {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));

		// WHEN
		final Object object = service.convert("Hello World", Object.class);

		// THEN
		assertThat(object, equalTo("Hello World"));
	}

	@Test
	void testConvert_sourceIsNull_targetIsNotAPrimitive() {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));

		// WHEN
		final Object object = service.convert(null, Integer.class);

		// THEN
		assertThat(object, nullValue());
	}

	@Test
	void testConvert_sourceIsNull_targetIsAPrimitive() {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));

		// WHEN
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> service.convert(null, int.class));

		// THEN
		assertThat(ex.getMessage(), equalTo("Can not convert 'null' to target type 'int' as it is a primitive!"));
	}

	@Test
	void testCanConvert() {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));

		// WHEN and THEN
		assertThat(service.canConvert(File.class, String.class), equalTo(true));
		assertThat(service.canConvert(Path.class, String.class), equalTo(true));
		assertThat(service.canConvert(URI.class, String.class), equalTo(true));
		assertThat(service.canConvert(String.class, File.class), equalTo(true));
		assertThat(service.canConvert(Path.class, File.class), equalTo(true));
		assertThat(service.canConvert(URI.class, File.class), equalTo(true));
		assertThat(service.canConvert(File.class, Path.class), equalTo(true));
		assertThat(service.canConvert(String.class, Path.class), equalTo(true));
		assertThat(service.canConvert(URI.class, Path.class), equalTo(true));
		assertThat(service.canConvert(File.class, URI.class), equalTo(true));
		assertThat(service.canConvert(Path.class, URI.class), equalTo(true));
		assertThat(service.canConvert(String.class, URI.class), equalTo(true));

		assertThat(service.canConvert(File.class, File.class), equalTo(true));
		assertThat(service.canConvert(File.class, Object.class), equalTo(true));
	}

	@Test
	void testCanConvert_typeIsIncompatible() {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));

		// WHEN and THEN
		assertThat(service.canConvert(File.class, Integer.class), equalTo(false));
		assertThat(service.canConvert(Double.class, Path.class), equalTo(false));
	}

	@Test
	void testCreateStringConverter() throws IOException {
		// GIVEN
		final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.US));
		final File file = Files.createTempFile("junit", "-tmp").toFile();

		// WHEN
		final StringConverter<File> converter = service.createStringConverter(File.class);

		// THEN
		assertThat(converter.fromString(file.getAbsolutePath()), equalTo(file));
		assertThat(converter.toString(file), equalTo(file.getAbsolutePath()));
	}
}
