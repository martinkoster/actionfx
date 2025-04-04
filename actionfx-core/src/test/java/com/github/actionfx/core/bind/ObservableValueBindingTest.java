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
package com.github.actionfx.core.bind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.beans.BeanPropertyReference;
import com.github.actionfx.core.beans.BeanWrapper;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ChoiceBox;

/**
 * JUnit test case for {@link ObservableValueBinding}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ObservableValueBindingTest {

	@Test
	void testBind_bidirectional() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<String> source = wrapper.getBeanPropertyReference("stringValue");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<?, String> binding = new ObservableValueBinding<>(source, target);

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("Hello World");
		target.set("Hello back");
		assertThat(source.getValue()).isEqualTo("Hello back");
		source.setValue("nice talking to you");
		assertThat(target.get()).isEqualTo("nice talking to you");
	}

	@Test
	void testBind_bidirectional_withStringToNumberConversion() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<Integer> source = wrapper.getBeanPropertyReference("integerValue");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<Integer, String> binding = new ObservableValueBinding<>(source, target);

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("0");
		target.set("42");
		assertThat(source.getValue()).isEqualTo(42);
		source.setValue(21);
		assertThat(target.get()).isEqualTo("21");
	}

	@Test
	void testBind_bidirectional_withPrimitiveIntConversion() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<Integer> source = wrapper.getBeanPropertyReference("objectIntegerValue");
		final IntegerProperty target = new SimpleIntegerProperty(42);
		final ObservableValueBinding<Integer, Number> binding = new ObservableValueBinding<>(source, target);

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(target.get()).isEqualTo(5);
		target.set(42);
		assertThat(source.getValue()).isEqualTo(Integer.valueOf(42));
	}

	@Test
	void testBind_unidirectional_withStringToJavaTimeConversion() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<LocalDateTime> source = wrapper.getBeanPropertyReference("localDateTime");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<LocalDateTime, String> binding = new ObservableValueBinding<>(source, target,
				"dd.MM.yyyy HH:mm");

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("05.09.2021 13:05");
		target.set("05.10.2021 15:10");
		assertThat(source.getValue()).isEqualTo(LocalDateTime.of(2021, 10, 5, 15, 10));
	}

	@Test
	void testBind_unidirectional_withStringToJavaTimeConversion_invalidInput_restoreOldValue() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<LocalDateTime> source = wrapper.getBeanPropertyReference("localDateTime");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<LocalDateTime, String> binding = new ObservableValueBinding<>(source, target,
				"dd.MM.yyyy HH:mm");

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("05.09.2021 13:05");
		target.set("invalid"); // invalid date input
		assertThat(source.getValue()).isEqualTo(LocalDateTime.of(2021, 9, 5, 13, 5)); // old value still present
	}

	@Test
	void testBind_bidirectional_withSelectionModel() {
		// GIVEN
		final Model model = new Model();
		model.setStringValue("Item 2");
		final BeanWrapper wrapper = new BeanWrapper(model);
		final BeanPropertyReference<String> source = wrapper.getBeanPropertyReference("stringValue");

		final ChoiceBox<String> choiceBox = new ChoiceBox<>();
		choiceBox.getItems().addAll("Item 1", "Item 2", "Item 3");
		final ObservableValueBinding<String, String> binding = new ObservableValueBinding<>(source,
				choiceBox.getSelectionModel().selectedItemProperty(), choiceBox.getSelectionModel());

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(choiceBox.getValue()).isEqualTo("Item 2");
		choiceBox.getSelectionModel().select("Item 3");
		assertThat(source.getValue()).isEqualTo("Item 3");
		model.setStringValue("Item 1");
		assertThat(choiceBox.getValue()).isEqualTo("Item 1");
	}

	@Test
	void testBind_unidirectional() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<String> source = wrapper.getBeanPropertyReference("readOnly");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<String, String> binding = new ObservableValueBinding<>(source, target);

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("Hello World");
	}

	@Test
	void testBind_valueBased() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<String> source = wrapper.getBeanPropertyReference("plainString");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<String, String> binding = new ObservableValueBinding<>(source, target);

		// WHEN
		binding.bind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("Hello World");
		target.set("Hello back");
		assertThat(source.getValue()).isEqualTo("Hello back");
	}

	@Test
	void testUnbind_bidirectional() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<String> source = wrapper.getBeanPropertyReference("stringValue");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<?, String> binding = new ObservableValueBinding<>(source, target);
		binding.bind();

		// WHEN
		binding.unbind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.BIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("Hello World");
		target.set("Hello back");
		assertThat(source.getValue()).isEqualTo("Hello World"); // no value change, property is not bound anymore
	}

	@Test
	void testUnbind_unidirectional() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<String> source = wrapper.getBeanPropertyReference("readOnly");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<?, String> binding = new ObservableValueBinding<>(source, target);
		binding.bind();

		// WHEN
		binding.unbind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("Hello World");
	}

	@Test
	void testUnbind_valueBased() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new Model());
		final BeanPropertyReference<String> source = wrapper.getBeanPropertyReference("plainString");
		final StringProperty target = new SimpleStringProperty("");
		final ObservableValueBinding<?, String> binding = new ObservableValueBinding<>(source, target);
		binding.bind();

		// WHEN
		binding.unbind();

		// THEN
		assertThat(binding.getBindingType()).isEqualTo(BindingType.UNIDIRECTIONAL);
		assertThat(target.get()).isEqualTo("Hello World");
		target.set("Hello back");
		assertThat(source.getValue()).isEqualTo("Hello World"); // no value change, property is not bound anymore
	}

	@Test
	void testNullReturningPropertyGetter() {
		// GIVEN
		final BeanWrapper wrapper = new BeanWrapper(new ModelWithNullReturningPropertyGetter());
		final BeanPropertyReference<String> source = wrapper.getBeanPropertyReference("value");
		final StringProperty target = new SimpleStringProperty("");

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> new ObservableValueBinding<>(source, target));

		// THEN
		assertThat(ex.getMessage()).contains("has a property-getter, but property-getter did a null value!");
	}

	private class ReadOnlyPropertyImpl extends ReadOnlyStringProperty {

		private final String value;

		public ReadOnlyPropertyImpl(final String value) {
			this.value = value;
		}

		@Override
		public Object getBean() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void addListener(final ChangeListener<? super String> listener) {
		}

		@Override
		public void removeListener(final ChangeListener<? super String> listener) {
		}

		@Override
		public void addListener(final InvalidationListener listener) {
		}

		@Override
		public void removeListener(final InvalidationListener listener) {
		}

		@Override
		public String get() {
			return value;
		}
	}

	public class Model {

		private final IntegerProperty integerValue = new SimpleIntegerProperty(0);

		private final StringProperty stringValue = new SimpleStringProperty("Hello World");

		private final ReadOnlyPropertyImpl readOnly = new ReadOnlyPropertyImpl("Hello World");

		private String plainString = "Hello World";

		private final ObjectProperty<Integer> objectIntegerValue = new SimpleObjectProperty<>(Integer.valueOf(5));

		private LocalDateTime localDateTime = LocalDateTime.of(2021, 9, 5, 13, 5);

		public final StringProperty stringValueProperty() {
			return stringValue;
		}

		public final String getStringValue() {
			return stringValueProperty().get();
		}

		public final void setStringValue(final String stringValue) {
			stringValueProperty().set(stringValue);
		}

		public String getPlainString() {
			return plainString;
		}

		public void setPlainString(final String plainString) {
			this.plainString = plainString;
		}

		public final ReadOnlyPropertyImpl readOnlyProperty() {
			return readOnly;
		}

		public final String getReadOnly() {
			return readOnlyProperty().get();
		}

		public final IntegerProperty integerValueProperty() {
			return integerValue;
		}

		public final int getIntegerValue() {
			return integerValueProperty().get();
		}

		public final void setIntegerValue(final int integerValue) {
			integerValueProperty().set(integerValue);
		}

		public final ObjectProperty<Integer> objectIntegerValueProperty() {
			return objectIntegerValue;
		}

		public final Integer getObjectIntegerValue() {
			return objectIntegerValueProperty().get();
		}

		public final void setObjectIntegerValue(final Integer objectIntegerValue) {
			objectIntegerValueProperty().set(objectIntegerValue);
		}

		public LocalDateTime getLocalDateTime() {
			return localDateTime;
		}

		public void setLocalDateTime(final LocalDateTime localDateTime) {
			this.localDateTime = localDateTime;
		}

	}

	public class ModelWithNullReturningPropertyGetter {
		public StringProperty valueProperty() {
			return null;
		}
	}

}
