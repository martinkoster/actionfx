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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Converts a {@link java.time} to another {@link java.time} instance.
 * <p>
 * The following JDK types are supported:
 * <ul>
 * <li>{@link java.time.Instant}</li>
 * <li>{@link java.time.OffsetDateTime}</li>
 * <li>{@link java.time.OffsetTime}</li>
 * <li>{@link java.time.ZonedDateTime}</li>
 * <li>{@link java.time.LocalDateTime}</li>
 * <li>{@link java.time.LocalTime}</li>
 * <li>{@link java.time.LocalDate}</li>
 * <li>{@link java.time.Year}</li>
 * <li>{@link java.time.YearMonth}</li>
 * <li>{@link java.time.DayOfWeek}</li>
 * <li>{@link java.time.Month}</li>
 * </ul>
 * <p>
 * In case a {@link Temporal} <b>without</b> time zone information is converted
 * into a {@link Temporal} <b>with</b> time zone, the systems default time zone
 * is taken.
 *
 * @author koster
 *
 * @param <S> the source type
 * @param <T> the target type
 */
public class JavaTimeToJavaTimeConverter<S extends TemporalAccessor, T extends TemporalAccessor>
		implements Converter<S, T> {

	private static final Map<Class<?>, UnaryOperator<TemporalAccessor>> TIME_TO_TIME_FUNCTIONS = new HashMap<>();

	static {
		initializeTimeConversionFunctions();
	}

	private final Class<T> targetNumberClass;

	public JavaTimeToJavaTimeConverter(final Class<T> targetNumberClass) {
		this.targetNumberClass = targetNumberClass;
	}

	/**
	 * Convenient factory method for creating an instance of
	 * {@link JavaTimeToJavaTimeConverter}.
	 *
	 * @param targetClass the target class
	 * @return the created converter
	 */
	public static <A extends TemporalAccessor, B extends TemporalAccessor> JavaTimeToJavaTimeConverter<A, B> to(
			final Class<B> targetClass) {
		return new JavaTimeToJavaTimeConverter<>(targetClass);
	}

	@Override
	public T convert(final S source) {
		return convertTimeToTargetClass(source, targetNumberClass);
	}

	/**
	 * Convert the given Java time into an instance of the given target class. Can
	 * be used in a static way.
	 *
	 * @param time        the Java time to convert
	 * @param targetClass the target class to convert to
	 * @return the converted Java time
	 * @throws IllegalArgumentException if the target class is not supported (i.e.
	 *                                  not a standard Java time subclass as
	 *                                  included in the JDK)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends TemporalAccessor> T convertTimeToTargetClass(final TemporalAccessor time,
			final Class<T> targetClass) throws IllegalArgumentException {
		final UnaryOperator<TemporalAccessor> conversionFunction = TIME_TO_TIME_FUNCTIONS.get(targetClass);
		if (conversionFunction != null) {
			return (T) conversionFunction.apply(time);
		} else {
			throw new IllegalArgumentException(
					"Can not convert Java time [" + time + "] of type [" + time.getClass().getCanonicalName()
							+ "] to unsupported target class [" + targetClass.getCanonicalName() + "]");
		}
	}

	/**
	 * Converts the given Java time to an {@link Instant}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static Instant toInstant(final TemporalAccessor time) {
		if (Instant.class.isAssignableFrom(time.getClass())) {
			return (Instant) time;
		} else {
			final ZonedDateTime lt = toZonedDateTime(time);
			return lt.toInstant();
		}
	}

	/**
	 * Converts the given Java time to an {@link LocalDateTime}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static LocalDateTime toLocalDateTime(final TemporalAccessor time) {
		if (LocalDateTime.class.isAssignableFrom(time.getClass())) {
			return (LocalDateTime) time;
		} else {
			final ZonedDateTime lt = toZonedDateTime(time);
			return lt.toLocalDateTime();
		}
	}

	/**
	 * Converts the given Java time to an {@link LocalDate}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static LocalDate toLocalDate(final TemporalAccessor time) {
		if (LocalDate.class.isAssignableFrom(time.getClass())) {
			return (LocalDate) time;
		} else {
			final ZonedDateTime lt = toZonedDateTime(time);
			return lt.toLocalDate();
		}
	}

	/**
	 * Converts the given Java time to an {@link LocalTime}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static LocalTime toLocalTime(final TemporalAccessor time) {
		if (LocalTime.class.isAssignableFrom(time.getClass())) {
			return (LocalTime) time;
		} else {
			final ZonedDateTime lt = toZonedDateTime(time);
			return lt.toLocalTime();
		}
	}

	/**
	 * Converts the given Java time to an {@link Year}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static Year toYear(final TemporalAccessor time) {
		if (Year.class.isAssignableFrom(time.getClass())) {
			return (Year) time;
		} else {
			final ZonedDateTime zdt = toZonedDateTime(time);
			return Year.from(zdt);
		}
	}

	/**
	 * Converts the given Java time to an {@link YearMonth}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static YearMonth toYearMonth(final TemporalAccessor time) {
		if (YearMonth.class.isAssignableFrom(time.getClass())) {
			return (YearMonth) time;
		} else {
			final ZonedDateTime zdt = toZonedDateTime(time);
			return YearMonth.from(zdt);
		}
	}

	/**
	 * Converts the given Java time to an {@link ZonedDateTime}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static ZonedDateTime toZonedDateTime(final TemporalAccessor time) {
		if (ZonedDateTime.class.isAssignableFrom(time.getClass())) {
			return (ZonedDateTime) time;
		} else if (Instant.class.isAssignableFrom(time.getClass())) {
			final Instant instant = (Instant) time;
			return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		} else if (LocalDateTime.class.isAssignableFrom(time.getClass())) {
			final LocalDateTime ldt = (LocalDateTime) time;
			return ZonedDateTime.of(ldt, ZoneId.systemDefault());
		} else if (LocalDate.class.isAssignableFrom(time.getClass())) {
			final LocalDate ld = (LocalDate) time;
			return ZonedDateTime.of(ld, LocalTime.MIDNIGHT, ZoneId.systemDefault());
		} else if (LocalTime.class.isAssignableFrom(time.getClass())) {
			final LocalTime lt = (LocalTime) time;
			return ZonedDateTime.of(LocalDate.now(), lt, ZoneId.systemDefault());
		} else if (Year.class.isAssignableFrom(time.getClass())) {
			final Year y = (Year) time;
			return ZonedDateTime.of(y.atDay(1).atTime(0, 0), ZoneId.systemDefault());
		} else if (YearMonth.class.isAssignableFrom(time.getClass())) {
			final YearMonth ym = (YearMonth) time;
			return ZonedDateTime.of(ym.atDay(1), LocalTime.MIDNIGHT, ZoneId.systemDefault());
		} else if (Month.class.isAssignableFrom(time.getClass())) {
			final Month month = (Month) time;
			return ZonedDateTime.of(LocalDateTime.of(Year.now(ZoneId.systemDefault()).getValue(), month, 1, 0, 0),
					ZoneId.systemDefault());
		} else if (DayOfWeek.class.isAssignableFrom(time.getClass())) {
			final DayOfWeek dow = (DayOfWeek) time;
			return ZonedDateTime.now().withDayOfYear(1).with(TemporalAdjusters.firstInMonth(dow))
					.with(LocalTime.MIDNIGHT);
		} else {
			return ZonedDateTime.from(time);
		}
	}

	/**
	 * Converts the given Java time to an {@link OffsetDateTime}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static OffsetDateTime toOffsetDateTime(final TemporalAccessor time) {
		if (OffsetDateTime.class.isAssignableFrom(time.getClass())) {
			return (OffsetDateTime) time;
		} else {
			final ZonedDateTime zdt = toZonedDateTime(time);
			return zdt.toOffsetDateTime();
		}
	}

	/**
	 * Converts the given Java time to an {@link OffsetTime}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static OffsetTime toOffsetTime(final TemporalAccessor time) {
		if (OffsetTime.class.isAssignableFrom(time.getClass())) {
			return (OffsetTime) time;
		} else {
			final OffsetDateTime odt = toOffsetDateTime(time);
			return odt.toOffsetTime();
		}
	}

	/**
	 * Converts the given Java time to an {@link Month}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static Month toMonth(final TemporalAccessor time) {
		if (Month.class.isAssignableFrom(time.getClass())) {
			return (Month) time;
		} else {
			final ZonedDateTime zdt = toZonedDateTime(time);
			return Month.from(zdt);
		}
	}

	/**
	 * Converts the given Java time to an {@link DayOfWeek}.
	 *
	 * @param time the time to convert
	 * @return the converted time
	 */
	private static DayOfWeek toDayOfWeek(final TemporalAccessor time) {
		if (DayOfWeek.class.isAssignableFrom(time.getClass())) {
			return (DayOfWeek) time;
		} else {
			final ZonedDateTime zdt = toZonedDateTime(time);
			return DayOfWeek.from(zdt);
		}
	}

	/**
	 * Initializes the internal time-to-time conversion functions.
	 */
	private static void initializeTimeConversionFunctions() {
		TIME_TO_TIME_FUNCTIONS.put(Instant.class, JavaTimeToJavaTimeConverter::toInstant);
		TIME_TO_TIME_FUNCTIONS.put(LocalDateTime.class, JavaTimeToJavaTimeConverter::toLocalDateTime);
		TIME_TO_TIME_FUNCTIONS.put(LocalDate.class, JavaTimeToJavaTimeConverter::toLocalDate);
		TIME_TO_TIME_FUNCTIONS.put(LocalTime.class, JavaTimeToJavaTimeConverter::toLocalTime);
		TIME_TO_TIME_FUNCTIONS.put(Year.class, JavaTimeToJavaTimeConverter::toYear);
		TIME_TO_TIME_FUNCTIONS.put(YearMonth.class, JavaTimeToJavaTimeConverter::toYearMonth);
		TIME_TO_TIME_FUNCTIONS.put(ZonedDateTime.class, JavaTimeToJavaTimeConverter::toZonedDateTime);
		TIME_TO_TIME_FUNCTIONS.put(OffsetDateTime.class, JavaTimeToJavaTimeConverter::toOffsetDateTime);
		TIME_TO_TIME_FUNCTIONS.put(OffsetTime.class, JavaTimeToJavaTimeConverter::toOffsetTime);
		TIME_TO_TIME_FUNCTIONS.put(Month.class, JavaTimeToJavaTimeConverter::toMonth);
		TIME_TO_TIME_FUNCTIONS.put(DayOfWeek.class, JavaTimeToJavaTimeConverter::toDayOfWeek);
	}
}
