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
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link JavaTimeToJavaTimeConverter}.
 *
 * @author koster
 *
 */
class JavaTimeToJavaTimeConverterTest {

	@Test
	void testApply_fromZonedDateTime() {
		// GIVEN
		final ZonedDateTime now = ZonedDateTime.now();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now), equalTo(now.toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now), equalTo(now.toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now), equalTo(now.toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now), equalTo(now.toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now), equalTo(now.toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now),
				equalTo(now.toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now), equalTo(Year.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now), equalTo(YearMonth.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(Month.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now), equalTo(MonthDay.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now), equalTo(DayOfWeek.from(now)));
	}

	@Test
	void testApply_fromInstant() {
		// GIVEN
		final Instant now = Instant.now();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now),
				equalTo(now.atZone(ZoneId.systemDefault()).toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now),
				equalTo(now.atZone(ZoneId.systemDefault()).toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now),
				equalTo(now.atZone(ZoneId.systemDefault()).toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(now.atZone(ZoneId.systemDefault()).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now),
				equalTo(now.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now),
				equalTo(ZonedDateTime.ofInstant(now, ZoneId.systemDefault())));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now),
				equalTo(Year.from(now.atZone(ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now),
				equalTo(YearMonth.from(now.atZone(ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now),
				equalTo(Month.from(now.atZone(ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now),
				equalTo(MonthDay.from(now.atZone(ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now),
				equalTo(DayOfWeek.from(now.atZone(ZoneId.systemDefault()))));
	}

	@Test
	void testApply_fromOffsetDateTime() {
		// GIVEN
		final OffsetDateTime now = OffsetDateTime.now();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now), equalTo(now.toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now), equalTo(now.toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now), equalTo(now.toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now), equalTo(now.toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now), equalTo(now.toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now), equalTo(now.toZonedDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now), equalTo(Year.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now), equalTo(YearMonth.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(Month.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now), equalTo(MonthDay.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now), equalTo(DayOfWeek.from(now)));
	}

	@Test
	void testApply_fromLocalDateTime() {
		// GIVEN
		final LocalDateTime now = LocalDateTime.now();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now),
				equalTo(ZonedDateTime.of(now, ZoneId.systemDefault()).toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now), equalTo(now.toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now), equalTo(now.toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now, ZoneId.systemDefault()).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now),
				equalTo(ZonedDateTime.of(now, ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now, ZoneId.systemDefault())));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now), equalTo(Year.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now), equalTo(YearMonth.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(Month.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now),
				equalTo(MonthDay.from(ZonedDateTime.of(now, ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now), equalTo(DayOfWeek.from(now)));
	}

	@Test
	void testApply_fromLocalDate() {
		// GIVEN
		final LocalDate now = LocalDate.now();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now),
				equalTo(ZonedDateTime.of(now, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now),
				equalTo(ZonedDateTime.of(now, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now), equalTo(
				ZonedDateTime.of(now, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now, LocalTime.MIDNIGHT, ZoneId.systemDefault())));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now), equalTo(Year.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now), equalTo(YearMonth.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(Month.from(now)));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now),
				equalTo(MonthDay.from(ZonedDateTime.of(now, LocalTime.MIDNIGHT, ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now), equalTo(DayOfWeek.from(now)));
	}

	@Test
	void testApply_fromLocalTime() {
		// GIVEN
		final LocalTime now = LocalTime.now();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now),
				equalTo(ZonedDateTime.of(LocalDate.now(), now, ZoneId.systemDefault()).toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(LocalDate.now(), now, ZoneId.systemDefault()).toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now), equalTo(LocalDate.now()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(LocalDate.now(), now, ZoneId.systemDefault()).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now), equalTo(
				ZonedDateTime.of(LocalDate.now(), now, ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(LocalDate.now(), now, ZoneId.systemDefault())));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now), equalTo(Year.from(LocalDate.now())));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now),
				equalTo(YearMonth.from(LocalDate.now())));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(Month.from(LocalDate.now())));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now),
				equalTo(MonthDay.from(ZonedDateTime.of(LocalDate.now(), now, ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now),
				equalTo(DayOfWeek.from(LocalDate.now())));
	}

	@Test
	void testApply_fromYear() {
		// GIVEN
		final Year now = Year.now();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now), equalTo(
				ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault())));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now),
				equalTo(YearMonth.from(ZonedDateTime.of(now.atDay(1), LocalTime.MIDNIGHT, ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(Month.JANUARY));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now),
				equalTo(MonthDay.from(ZonedDateTime.now().withDayOfMonth(1).withMonth(1))));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now),
				equalTo(DayOfWeek.from(ZonedDateTime.now().withDayOfYear(1).with(now))));
	}

	@Test
	void testApply_fromYearMonth() {
		// GIVEN
		final YearMonth now = YearMonth.now();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now), equalTo(
				ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now),
				equalTo(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault())));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now),
				equalTo(Year.from(ZonedDateTime.of(now.atDay(1), LocalTime.MIDNIGHT, ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(now.getMonth()));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now),
				equalTo(MonthDay.from(ZonedDateTime.of(now.atDay(1).atTime(0, 0), ZoneId.systemDefault()))));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now),
				equalTo(DayOfWeek.from(ZonedDateTime.now().withDayOfMonth(1))));
	}

	@Test
	void testApply_fromMonth() {
		// GIVEN
		final Month now = ZonedDateTime.now().getMonth();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now),
				equalTo(ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT).toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now),
				equalTo(ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT).toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now),
				equalTo(ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT).toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now),
				equalTo(ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT).toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now), equalTo(
				ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now),
				equalTo(ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT)));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now),
				equalTo(Year.from(ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT))));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now),
				equalTo(YearMonth.of(Year.now().getValue(), now)));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now),
				equalTo(MonthDay.from(ZonedDateTime.now().withDayOfMonth(1).with(LocalTime.MIDNIGHT))));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now),
				equalTo(DayOfWeek.from(ZonedDateTime.now().withDayOfMonth(1))));
	}

	@Test
	void testApply_fromMonthDay() {
		// GIVEN
		final MonthDay now = MonthDay.from(ZonedDateTime.now());

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now),
				equalTo(ZonedDateTime.now().withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth())
						.with(LocalTime.MIDNIGHT).toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now),
				equalTo(ZonedDateTime.now().withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth())
						.with(LocalTime.MIDNIGHT).toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now),
				equalTo(ZonedDateTime.now().withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth())
						.with(LocalTime.MIDNIGHT).toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now),
				equalTo(ZonedDateTime.now().withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth())
						.with(LocalTime.MIDNIGHT).toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(ZonedDateTime.now().withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth())
						.with(LocalTime.MIDNIGHT).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now),
				equalTo(ZonedDateTime.now().withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth())
						.with(LocalTime.MIDNIGHT).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now), equalTo(ZonedDateTime.now()
				.withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth()).with(LocalTime.MIDNIGHT)));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now), equalTo(Year.from(ZonedDateTime.now()
				.withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth()).with(LocalTime.MIDNIGHT))));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now),
				equalTo(YearMonth.of(Year.now().getValue(), now.getMonth())));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(now.getMonth()));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now), equalTo(now));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now), equalTo(DayOfWeek.from(ZonedDateTime
				.now().withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth()).with(LocalTime.MIDNIGHT))));
	}

	@Test
	void testApply_fromDayOfWeek() {
		// GIVEN
		final DayOfWeek now = ZonedDateTime.now().getDayOfWeek();

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(Instant.class).convert(now), equalTo(ZonedDateTime.now()
				.withDayOfYear(1).with(TemporalAdjusters.firstInMonth(now)).with(LocalTime.MIDNIGHT).toInstant()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDateTime.class).convert(now),
				equalTo(ZonedDateTime.now().withDayOfYear(1).with(TemporalAdjusters.firstInMonth(now))
						.with(LocalTime.MIDNIGHT).toLocalDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalDate.class).convert(now), equalTo(ZonedDateTime.now()
				.withDayOfYear(1).with(TemporalAdjusters.firstInMonth(now)).with(LocalTime.MIDNIGHT).toLocalDate()));
		assertThat(JavaTimeToJavaTimeConverter.to(LocalTime.class).convert(now), equalTo(ZonedDateTime.now()
				.withDayOfYear(1).with(TemporalAdjusters.firstInMonth(now)).with(LocalTime.MIDNIGHT).toLocalTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetDateTime.class).convert(now),
				equalTo(ZonedDateTime.now().withDayOfYear(1).with(TemporalAdjusters.firstInMonth(now))
						.with(LocalTime.MIDNIGHT).toOffsetDateTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(OffsetTime.class).convert(now), equalTo(ZonedDateTime.now()
				.withDayOfYear(1).with(now).with(LocalTime.MIDNIGHT).toOffsetDateTime().toOffsetTime()));
		assertThat(JavaTimeToJavaTimeConverter.to(ZonedDateTime.class).convert(now), equalTo(ZonedDateTime.now()
				.withDayOfYear(1).with(TemporalAdjusters.firstInMonth(now)).with(LocalTime.MIDNIGHT)));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(now), equalTo(Year.now()));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(now), equalTo(YearMonth.from(ZonedDateTime
				.now().withDayOfYear(1).with(TemporalAdjusters.firstInMonth(now)).with(LocalTime.MIDNIGHT))));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(now), equalTo(Month.JANUARY));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(now), equalTo(MonthDay.from(ZonedDateTime
				.now().withDayOfYear(1).with(TemporalAdjusters.firstInMonth(now)).with(LocalTime.MIDNIGHT))));
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(now), equalTo(now));
	}

	@Test
	void testApply_crossChecks() {
		// GIVEN
		final ZonedDateTime time = ZonedDateTime.parse("2022-01-01T10:15:30+01:00[Europe/Berlin]");

		// WHEN and THEN
		assertThat(JavaTimeToJavaTimeConverter.to(DayOfWeek.class).convert(time), equalTo(DayOfWeek.SATURDAY));
		assertThat(JavaTimeToJavaTimeConverter.to(Month.class).convert(time), equalTo(Month.JANUARY));
		assertThat(JavaTimeToJavaTimeConverter.to(MonthDay.class).convert(time), equalTo(MonthDay.of(1, 1)));
		assertThat(JavaTimeToJavaTimeConverter.to(Year.class).convert(time), equalTo(Year.of(2022)));
		assertThat(JavaTimeToJavaTimeConverter.to(YearMonth.class).convert(time), equalTo(YearMonth.of(2022, 1)));
	}
}
