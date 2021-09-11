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

import java.util.function.Function;

/**
 * {@link BidirectionalConverter} that is able to use 2 {@link Converter} to
 * perform to-/from conversion from and to the desired data type.
 *
 * @param <S> the source type to convert from
 * @param <T> the target type to convert to
 * @author koster
 *
 */
public class GenericBidirectionalConverter<S, T> implements BidirectionalConverter<S, T> {

	private final Function<S, T> toConverter;

	private final Function<T, S> fromConverter;

	/**
	 * Constructor accepting a to- and a from- converter.
	 *
	 * @param toConverter   the to- converter
	 * @param fromConverter the from- converter
	 */
	public GenericBidirectionalConverter(final Function<S, T> toConverter, final Function<T, S> fromConverter) {
		this.toConverter = toConverter;
		this.fromConverter = fromConverter;
	}

	@Override
	public T to(final S source) {
		return toConverter.apply(source);
	}

	@Override
	public S from(final T target) {
		return fromConverter.apply(target);
	}

}
