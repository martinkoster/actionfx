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
package com.github.actionfx.core.method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.actionfx.core.ActionFXMock;
import com.github.actionfx.core.annotation.AFXControlValue;
import com.github.actionfx.core.container.BeanContainerFacade;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.ParentView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * JUnit test case for {@link ControlValueAnnotatedParameterResolver}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class ControlValueAnnotatedParameterResolverTest {
	private ActionFXMock actionFX;

	@BeforeEach
	void onSetup() {
		actionFX = new ActionFXMock();
		actionFX.addBean(BeanContainerFacade.CONVERSION_SERVICE_BEANNAME, new ConversionService());
	}

	@Test
	void testResolved_textField_valueTakenFromUserValue() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithString");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final String value = resolver.resolve(instance, method, parameter, annotation, String.class);

		// THEN
		assertThat(value).isEqualTo("Hello World");
	}

	@Test
	void testResolved_textField_valueTakenFromSingleValue() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithSourcePropertySingleValue");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final String value = resolver.resolve(instance, method, parameter, annotation, String.class);

		// THEN
		assertThat(value).isEqualTo("Hello World");
	}

	@Test
	void testResolved_textField_withFormatPattern() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithFormatPattern");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final Double value = resolver.resolve(instance, method, parameter, annotation, Double.class);

		// THEN
		assertThat(value).isEqualTo(Double.valueOf(42.0));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testResolved_listView_valueTakenFromUserValue() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithListArgument");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final List<String> value = resolver.resolve(instance, method, parameter, annotation, List.class);

		// THEN
		assertThat(value).containsExactly("Item 2", "Item 3");
	}

	@SuppressWarnings("unchecked")
	@Test
	void testResolved_listView_valueTakenFromItemsObservableList() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithSourcePropertyItems");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final List<String> value = resolver.resolve(instance, method, parameter, annotation, List.class);

		// THEN
		assertThat(value).containsExactly("Item 1", "Item 2", "Item 3");
	}

	@Test
	void testResolved_unknownControl() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithUnknownControl");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> resolver.resolve(instance, method, parameter, annotation, List.class));

		// THEN
		assertThat(ex.getMessage()).contains("There is no node with ID='someUnknownControl' inside the view associated with controller");
	}

	@Test
	void testResolved_nodeIsNotOfTypeControl() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithTypeIsNotControl");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> resolver.resolve(instance, method, parameter, annotation, List.class));

		// THEN
		assertThat(ex.getMessage()).contains("Node with ID='vbox' inside the view hosted by controller");
		assertThat(ex.getMessage()).contains("is not a javafx.scene.control.Control!");
	}

	@Test
	void testResolved_incompatibleType() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithIncompatibleType");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> resolver.resolve(instance, method, parameter, annotation, List.class));

		// THEN
		assertThat(ex.getMessage()).contains("Value retrieved for control with ID='textField'");
		assertThat(ex.getMessage()).contains(" is not compatible with the method argument of type 'interface java.util.List'!");
	}

	@Test
	void testResolved_viewIsNull() {
		// GIVEN
		final ClassWithMethods instance = new ClassWithMethods();
		instance._view = null; // set view explicitely to null
		final Method method = ReflectionUtils.findMethod(ClassWithMethods.class, "methodWithUnknownControl");
		final Parameter parameter = method.getParameters()[0];
		final AFXControlValue annotation = parameter.getAnnotation(AFXControlValue.class);
		final ControlValueAnnotatedParameterResolver resolver = new ControlValueAnnotatedParameterResolver();

		// WHEN
		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> resolver.resolve(instance, method, parameter, annotation, List.class));

		// THEN
		assertThat(ex.getMessage()).contains("There is no view associated with controller");
	}

	public class ClassWithMethods {

		public View _view;

		public ClassWithMethods() {
			_view = new ParentView("viewId", new StaticView(), this);
		}

		public void methodWithString(@AFXControlValue("textField") final String textValue) {
		}

		public void methodWithSourcePropertySingleValue(
				@AFXControlValue(value = "textField", sourceProperty = ControlProperties.SINGLE_VALUE_PROPERTY) final String textValue) {
		}

		public void methodWithListArgument(@AFXControlValue("listView") final List<String> selectedEntries) {
		}

		public void methodWithSourcePropertyItems(
				@AFXControlValue(value = "listView", sourceProperty = ControlProperties.ITEMS_OBSERVABLE_LIST) final List<String> entries) {
		}

		public void methodWithUnknownControl(@AFXControlValue("someUnknownControl") final String textValue) {
		}

		public void methodWithTypeIsNotControl(@AFXControlValue("vbox") final String textValue) {
		}

		public void methodWithIncompatibleType(@AFXControlValue("textField") final List<String> selectedEntries) {
		}

		public void methodWithFormatPattern(
				@AFXControlValue(value = "numberTextField", formatPattern = "#,###.##") final Double value) {
		}

	}

	/**
	 * A static view class with some elements having an ID.
	 *
	 * @author koster
	 *
	 */
	public class StaticView extends HBox {

		public StaticView() {
			final TextField textField = new TextField();
			textField.setId("textField");
			textField.setText("Hello World");
			final TextField numberTextField = new TextField();
			numberTextField.setId("numberTextField");
			numberTextField.setText("42.00");
			final ListView<String> listView = new ListView<>();
			listView.setId("listView");
			listView.getItems().addAll("Item 1", "Item 2", "Item 3");
			listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			listView.getSelectionModel().select(1);
			listView.getSelectionModel().select(2);
			final VBox vbox = new VBox();
			vbox.setId("vbox");
			getChildren().addAll(textField, numberTextField, listView, vbox);
		}
	}

}
