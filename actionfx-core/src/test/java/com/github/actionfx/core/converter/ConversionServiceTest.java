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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.github.actionfx.core.ActionFX;

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
    void testConstruct_localeIsTakenFromSystem() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN and THEN
        assertThat(service.getLocaleProperty(), equalTo(Locale.getDefault()));
    }

    @Test
    void testConstruct_localeIsTakenFromActionFX() {
        // GIVEN
        final ActionFX actionFX = ActionFX.builder().locale(Locale.UK).build();
        final ConversionService service = new ConversionService();

        // WHEN and THEN
        assertThat(service.getLocaleProperty(), equalTo(Locale.UK));
        actionFX.reset(); // destroy ActionFX instance again, so other test methods are not impacted
    }

    @Test
    void testConvert_typeFile() throws IOException {
        // GIVEN
        final ConversionService service = new ConversionService();
        final File file = Files.createTempFile("junit", "-tmp").toFile();

        // WHEN
        assertThat(service.convert(file, String.class), equalTo(file.getAbsolutePath()));
        assertThat(service.convert(file, URI.class), equalTo(file.toURI()));
        assertThat(service.convert(file, Path.class), equalTo(file.toPath()));
    }

    @Test
    void testConvert_typePath() throws IOException {
        // GIVEN
        final ConversionService service = new ConversionService();
        final File file = Files.createTempFile("junit", "-tmp").toFile();

        // WHEN
        assertThat(service.convert(file.toPath(), String.class), equalTo(file.getAbsolutePath()));
        assertThat(service.convert(file.toPath(), URI.class), equalTo(file.toURI()));
        assertThat(service.convert(file.toPath(), File.class), equalTo(file));
    }

    @Test
    void testConvert_typeURI() throws IOException {
        // GIVEN
        final ConversionService service = new ConversionService();
        final File file = Files.createTempFile("junit", "-tmp").toFile();

        // WHEN
        assertThat(service.convert(file.toURI(), String.class), equalTo(file.toURI().toString()));
        assertThat(service.convert(file.toURI(), Path.class), equalTo(file.toPath()));
        assertThat(service.convert(file.toURI(), File.class), equalTo(file));
    }

    @Test
    void testConvert_typeString() throws IOException {
        // GIVEN
        final ConversionService service = new ConversionService();
        final File file = Files.createTempFile("junit", "-tmp").toFile();

        // WHEN
        assertThat(service.convert(file.toURI().toString(), URI.class), equalTo(file.toURI()));
        assertThat(service.convert(file.getAbsolutePath(), Path.class), equalTo(file.toPath()));
        assertThat(service.convert(file.getAbsolutePath(), File.class), equalTo(file));
    }

    @Test
    void testConvert_fromStringToNumber() {
        // GIVEN
        final ConversionService service = new ConversionService();
        final String number = "124";

        // WHEN and THEN
        assertThat(service.convert(number, Integer.class), equalTo(Integer.valueOf(number)));
        assertThat(service.convert(number, Float.class), equalTo(Float.valueOf(number)));
        assertThat(service.convert(number, Double.class), equalTo(Double.valueOf(number)));
        assertThat(service.convert(number, Short.class), equalTo(Short.valueOf(number)));
        assertThat(service.convert(number, Byte.class), equalTo(Byte.valueOf(number)));
    }

    @Test
    void testConvert_fromNumberToString() {
        // GIVEN
        final ConversionService service = new ConversionService();
        final String number = "124";

        // WHEN and THEN
        assertThat(service.convert(Integer.valueOf(number), String.class), equalTo("124"));
        assertThat(service.convert(Float.valueOf(number), String.class), equalTo("124"));
        assertThat(service.convert(Double.valueOf(number), String.class), equalTo("124"));
        assertThat(service.convert(Short.valueOf(number), String.class), equalTo("124"));
        assertThat(service.convert(Byte.valueOf(number), String.class), equalTo("124"));
    }

    @Test
    void testConvert_conversionNotPossible() throws IOException {
        // GIVEN
        final ConversionService service = new ConversionService();
        final File file = Files.createTempFile("junit", "-tmp").toFile();

        // WHEN
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.convert(file, Integer.class));
        assertThat(ex.getMessage(),
                equalTo("Unable to convert type 'class java.io.File' to type 'class java.lang.Integer'!"));
    }

    @Test
    void testConvert_targetTypeIsCompatible() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN
        final Object object = service.convert("Hello World", Object.class);

        // THEN
        assertThat(object, equalTo("Hello World"));
    }

    @Test
    void testConvert_sourceIsNull_targetIsNotAPrimitive() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN
        final Object object = service.convert(null, Integer.class);

        // THEN
        assertThat(object, nullValue());
    }

    @Test
    void testConvert_sourceIsNull_targetIsAPrimitive() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.convert(null, int.class));

        // THEN
        assertThat(ex.getMessage(), equalTo("Can not convert 'null' to target type 'int' as it is a primitive!"));
    }

    @Test
    void testConvert_withFormatPattern() throws ParseException {
        // GIVEN
        final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.GERMANY));
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        // WHEN and THEN
        assertThat(service.convert(42000.0, String.class, "#,###.00"), equalTo("42.000,00"));
        assertThat(service.convert(42000.0f, String.class, "#,###.00"), equalTo("42.000,00"));
        assertThat(service.convert("05.09.2021 11:46:00", Date.class, "dd.MM.yyyy HH:mm:ss"),
                equalTo(sdf.parse("05.09.2021 11:46:00")));
        assertThat(service.convert(sdf.parse("05.09.2021 11:46:00"), String.class, "dd.MM.yyyy HH:mm:ss"),
                equalTo("05.09.2021 11:46:00"));
    }

    @Test
    void testConvert_withJavaTime() {
        // GIVEN
        final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.GERMANY));
        final ZonedDateTime time = ZonedDateTime.parse("2022-01-01T10:15:30+01:00[Europe/Berlin]");

        // WHEN and THEN
        assertThat(service.convert(time, DayOfWeek.class), equalTo(DayOfWeek.SATURDAY));
        assertThat(service.convert(time, Month.class), equalTo(Month.JANUARY));
        assertThat(service.convert(time, MonthDay.class), equalTo(MonthDay.of(1, 1)));
        assertThat(service.convert(time, Year.class), equalTo(Year.of(2022)));
        assertThat(service.convert(time, YearMonth.class), equalTo(YearMonth.of(2022, 1)));
        assertThat(service.convert(time, String.class, "dd.MM.yyyy HH:mm"), equalTo("01.01.2022 10:15"));
        assertThat(service.convert("2022-01-01T10:15:30+01:00", Instant.class, "yyyy-MM-dd'T'HH:mm:ssxxx"),
                equalTo(time.toInstant()));
    }

    @Test
    void testConvert_withDate() {
        // GIVEN
        final ConversionService service = new ConversionService(new SimpleObjectProperty<>(Locale.GERMANY));
        final ZonedDateTime javaTime = ZonedDateTime.parse("2022-01-01T10:15:30+01:00[Europe/Berlin]");
        final Date date = Date.from(javaTime.toInstant());

        // WHEN and THEN
        assertThat(service.convert(javaTime, Date.class), equalTo(date));
        assertThat(service.convert(date, Instant.class), equalTo(date.toInstant()));
        assertThat(service.convert(date, ZonedDateTime.class),
                equalTo(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())));
    }

    @Test
    void testCanConvert() {
        // GIVEN
        final ConversionService service = new ConversionService();

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
        final ConversionService service = new ConversionService();

        // WHEN and THEN
        assertThat(service.canConvert(File.class, Integer.class), equalTo(false));
        assertThat(service.canConvert(Double.class, Path.class), equalTo(false));
    }

    @Test
    void testCanConvert_withValue_conversionIsPossible() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN
        assertThat(service.canConvert((String) null, String.class, null), equalTo(true));
        assertThat(service.canConvert("Hello there", String.class, null), equalTo(true));
        assertThat(service.canConvert("31.12.2022", LocalDate.class, "dd.MM.yyyy"), equalTo(true));
        assertThat(service.canConvert("3.0", Double.class, "#.#"), equalTo(true));
    }

    @Test
    void testCanConvert_withValue_conversionIsNotPossible() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN
        assertThat(service.canConvert("1.1.22", LocalDate.class, "dd.MM.yyyy"), equalTo(false));
        assertThat(service.canConvert("Hello World", List.class, null), equalTo(false));
    }

    @Test
    void testCreateStringConverter() throws IOException {
        // GIVEN
        final ConversionService service = new ConversionService();
        final File file = Files.createTempFile("junit", "-tmp").toFile();

        // WHEN
        final StringConverter<File> converter = service.createStringConverter(File.class);

        // THEN
        assertThat(converter.fromString(file.getAbsolutePath()), equalTo(file));
        assertThat(converter.toString(file), equalTo(file.getAbsolutePath()));
    }

    @Test
    void testCreateBidirectionalConverter_numberToNumber() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN
        final BidirectionalConverter<Integer, Double> converter = service.createBidirectionalConverter(Integer.class,
                Double.class);

        // THEN
        assertThat(converter, notNullValue());
        assertThat(converter.to(Integer.valueOf(42)), equalTo(Double.valueOf(42.0)));
        assertThat(converter.from(Double.valueOf(42.0)), equalTo(Integer.valueOf(42)));
    }

    @Test
    void testCreateConverter_checkClassHierarchy() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN and THEN
        assertThat(service.createConverter(String.class, Double.class, null),
                instanceOf(StringToDoubleConverter.class));
        assertThat(service.createConverter(String.class, Number.class, null),
                instanceOf(StringToNumberConverter.class));
        assertThat(service.createConverter(Double.class, String.class, null),
                instanceOf(DoubleToStringConverter.class));
        assertThat(service.createConverter(Number.class, String.class, null),
                instanceOf(ObjectToStringConverter.class));
    }

    @Test
    void testConvert_fromStringToBoolean() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN and THEN
        assertThat(service.convert("true", boolean.class), equalTo(true));
        assertThat(service.convert("true", Boolean.class), equalTo(Boolean.TRUE));
        assertThat(service.convert("yes", Boolean.class), equalTo(Boolean.TRUE));
        assertThat(service.convert("ja", Boolean.class), equalTo(Boolean.TRUE));
        assertThat(service.convert("oui", Boolean.class), equalTo(Boolean.TRUE));
        assertThat(service.convert("si", Boolean.class), equalTo(Boolean.TRUE));

        assertThat(service.convert("false", boolean.class), equalTo(false));
        assertThat(service.convert("false", Boolean.class), equalTo(Boolean.FALSE));
        assertThat(service.convert("no", Boolean.class), equalTo(Boolean.FALSE));
        assertThat(service.convert("nein", Boolean.class), equalTo(Boolean.FALSE));
        assertThat(service.convert("non", Boolean.class), equalTo(Boolean.FALSE));
    }

    @Test
    void testConvert_fromStringToEnum() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN and THEN
        assertThat(service.convert("VALUEA", TestEnum.class), equalTo(TestEnum.VALUEA));
        assertThat(service.convert("VALUEB", TestEnum.class), equalTo(TestEnum.VALUEB));
    }

    @Test
    void testConvert_fromStringToCharset() {
        // GIVEN
        final ConversionService service = new ConversionService();

        // WHEN and THEN
        assertThat(service.convert("UTF-8", Charset.class), equalTo(StandardCharsets.UTF_8));
        assertThat(service.convert("utf8", Charset.class), equalTo(StandardCharsets.UTF_8));
    }

    public static enum TestEnum {
        VALUEA,
        VALUEB,
    }
}
