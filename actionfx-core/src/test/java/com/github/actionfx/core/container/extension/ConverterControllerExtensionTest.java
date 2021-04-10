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
package com.github.actionfx.core.container.extension;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.annotation.AFXConverter;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * JUnit test case for {@link ConverterControllerExtension}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ConverterControllerExtensionTest {

	@Test
	void testAccept() {
		// GIVEN
		final ControllerWithComboBox controller = new ControllerWithComboBox();
		final ConverterControllerExtension extension = new ConverterControllerExtension();

		// WHEN
		extension.accept(controller);

		// THEN
		assertThat(controller.comboBox.getConverter(), instanceOf(MovieStringConverter.class));

	}

	public static class ControllerWithComboBox {

		public View _view;

		@AFXConverter(MovieStringConverter.class)
		private final ComboBox<Movie> comboBox = new ComboBox<>();
	}

	public static class MovieStringConverter extends StringConverter<Movie> {

		@Override
		public String toString(final Movie object) {
			return object.getName();
		}

		@Override
		public Movie fromString(final String string) {
			return new Movie(string);
		}
	}

	public static class Movie {

		private String name;

		public Movie(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}
	}

}
