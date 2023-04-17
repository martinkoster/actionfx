/*
 * Copyright (c) 2020 Martin Koster
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
package com.github.actionfx.core.container;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.container.instantiation.BeanDefinitionPostProcessor;
import com.github.actionfx.core.container.instantiation.ControllerInstancePostProcessor;
import com.github.actionfx.core.container.instantiation.MultilingualViewController;
import com.github.actionfx.core.container.instantiation.SampleViewController;
import com.github.actionfx.core.converter.ConversionService;
import com.github.actionfx.core.dialogs.DialogController;
import com.github.actionfx.core.events.PriorityAwareEventBus;
import com.github.actionfx.core.extension.ActionFXExtensionsBean;
import com.github.actionfx.core.extension.beans.BeanExtension;
import com.github.actionfx.core.test.DerivedFromTestView;
import com.github.actionfx.core.test.TestController;
import com.github.actionfx.core.test.TestView;
import com.github.actionfx.core.test.app.LazilyInitializedController;
import com.github.actionfx.core.test.app.MainController;
import com.github.actionfx.core.test.app.ModelWithDefaultConstructor;
import com.github.actionfx.core.test.app.NonLazilyInitializedController;
import com.github.actionfx.core.test.app.SampleApp;
import com.github.actionfx.core.view.FxmlView;
import com.github.actionfx.core.view.View;
import com.github.actionfx.testing.junit5.FxThreadForAllMonocleExtension;

import javafx.beans.property.SimpleObjectProperty;

/**
 * JUnit test case for {@link DefaultActionFXBeanContainer}.
 *
 * @author koster
 *
 */
@ExtendWith(FxThreadForAllMonocleExtension.class)
class DefaultActionFXBeanContainerTest {

    @BeforeAll
    static void beforeAll() {
        ActionFX.builder().build();
    }

    @Test
    void testGetBean_singletonById() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.addBeanDefinition("beanId", TestView.class, true, true, TestView::new);

        // WHEN
        final TestView view1 = container.getBean("beanId");
        final TestView view2 = container.getBean("beanId");

        // THEN
        assertThat(view1, notNullValue());
        assertThat(view1.isInitializeInvoked(), equalTo(true));
        assertThat(view2, notNullValue());
        assertThat(view2.isInitializeInvoked(), equalTo(true));
        assertThat(view1, sameInstance(view2)); // consecutive calls to getBean yield the same instance
    }

    @Test
    void testGetBean_singletonByType() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.addBeanDefinition("beanId", TestView.class, true, true, TestView::new);

        // WHEN
        final TestView view1 = container.getBean(TestView.class);
        final TestView view2 = container.getBean(TestView.class);

        // THEN
        assertThat(view1, notNullValue());
        assertThat(view1.isInitializeInvoked(), equalTo(true));
        assertThat(view2, notNullValue());
        assertThat(view2.isInitializeInvoked(), equalTo(true));
        assertThat(view1, sameInstance(view2)); // consecutive calls to getBean yield the same instance
    }

    @Test
    void testGetBean_singletonByType_superTypeIsRequested() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.addBeanDefinition("beanId", DerivedFromTestView.class, true, true, DerivedFromTestView::new);

        // WHEN (request the super type 'TestView', although 'DerivedFromTestView' is
        // registered)
        final TestView view1 = container.getBean(TestView.class);
        final TestView view2 = container.getBean(TestView.class);

        // THEN
        assertThat(view1, notNullValue());
        assertThat(view1.isInitializeInvoked(), equalTo(true));
        assertThat(view2, notNullValue());
        assertThat(view2.isInitializeInvoked(), equalTo(true));
        assertThat(view1, sameInstance(view2)); // consecutive calls to getBean yield the same instance
        assertThat(view1, instanceOf(DerivedFromTestView.class));
    }

    @Test
    void testGetBean_byId_idDoesNotExist() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.addBeanDefinition("beanId", TestView.class, true, true, TestView::new);

        // WHEN and THEN
        assertThat(container.getBean("someNonExistingId"), nullValue());
    }

    @Test
    void testGetBean_byType_idDoesNotExist() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.addBeanDefinition("beanId", TestView.class, true, true, TestView::new);

        // WHEN and THEN
        assertThat(container.getBean(String.class), nullValue());
    }

    @Test
    void testGetBean_nonSingletonById() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.addBeanDefinition("beanId", TestView.class, false, true, TestView::new);

        // WHEN
        final TestView view1 = container.getBean("beanId");
        final TestView view2 = container.getBean("beanId");

        // THEN
        assertThat(view1, notNullValue());
        assertThat(view1.isInitializeInvoked(), equalTo(true));
        assertThat(view2, notNullValue());
        assertThat(view2.isInitializeInvoked(), equalTo(true));
        assertThat(view1, not(sameInstance(view2))); // consecutive calls to getBean yield the different instance
    }

    @Test
    void testGetBean_nonSingletonByType() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.addBeanDefinition("beanId", TestView.class, false, true, TestView::new);

        // WHEN
        final TestView view1 = container.getBean(TestView.class);
        final TestView view2 = container.getBean(TestView.class);

        // THEN
        assertThat(view1, notNullValue());
        assertThat(view1.isInitializeInvoked(), equalTo(true));
        assertThat(view2, notNullValue());
        assertThat(view2.isInitializeInvoked(), equalTo(true));
        assertThat(view1, not(sameInstance(view2))); // consecutive calls to getBean yield the different instances
    }

    @Test
    void testRunComponentScan() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();

        // WHEN
        container.runComponentScan(SampleApp.class.getPackageName());

        // THEN
        final View view = container.getBean("mainView");
        final MainController mainControllerById = container.getBean("mainController");
        final MainController mainControllerByClassName = container.getBean(MainController.class);

        assertThat(view, notNullValue());
        assertThat(mainControllerById, notNullValue());
        assertThat(mainControllerByClassName, notNullValue());
        assertThat(mainControllerById, sameInstance(mainControllerByClassName));
        assertThat(view, instanceOf(FxmlView.class));
        final FxmlView fxmlView = (FxmlView) view;
        assertThat(fxmlView.getController(), sameInstance(mainControllerById));

        // check that non-lazy controllers are instantiated
        assertThat(NonLazilyInitializedController.isConstructed(), equalTo(true));

        // check that lazy controllers are not instantiated
        assertThat(LazilyInitializedController.isConstructed(), equalTo(false));
    }

    @Test
    void testGetBean_withDependencyInjection() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.runComponentScan(SampleApp.class.getPackageName());

        // WHEN
        final MainController controller = container.getBean("mainController");
        final View view = container.getBean("mainView");
        final ModelWithDefaultConstructor model = container.getBean(ModelWithDefaultConstructor.class);

        // THEN (verify all annotated fields are resolved)
        assertThat(controller.getMainView(), notNullValue());
        assertThat(controller.getMainView(), sameInstance(view)); // view is a singleton!
        assertThat(controller.getModel(), notNullValue());
        assertThat(controller.getModel(), sameInstance(model)); // type is a singleton!

        // check, that @Inject-annotated field in abstract base class is resolved
        assertThat(controller.getBaseModel(), notNullValue());
        assertThat(controller.getBaseModel(), sameInstance(model)); // type is still a singleton!

    }

    @Test
    void testResolveResourceBundle() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();

        // WHEN
        final ResourceBundle bundle = container.resolveResourceBundle(MultilingualViewController.class, Locale.GERMANY);

        // THEN
        assertThat(bundle, notNullValue());
        assertThat(bundle.getString("label.text"), equalTo("Hallo Welt"));
    }

    @Test
    void testResolveResourceBundle_noResourcesBasenameSpecified() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();

        // WHEN (controller does not specify a "resourcesBasename")
        final ResourceBundle bundle = container.resolveResourceBundle(SampleViewController.class, Locale.GERMANY);

        // THEN
        assertThat(bundle, nullValue());
    }

    @Test
    void testResolveResourceBundle_suppliedNoAnnotatedController() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();

        // WHEN (supplied class holds no @AFXController annotation)
        final ResourceBundle bundle = container.resolveResourceBundle(String.class, Locale.GERMANY);

        // THEN
        assertThat(bundle, nullValue());
    }

    @Test
    void testAddControllerBeanDefinition() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();
        container.addControllerBeanDefinition(TestController.class);

        // WHEN
        final TestController controller = container.getBean("testController");

        // THEN
        assertThat(controller, notNullValue());
    }

    @Test
    void testAddControllerBeanDefinition_classIsNotAController() {
        // GIVEN
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer();

        // WHEN
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> container.addControllerBeanDefinition(NonController.class));

        // THEN
        assertThat(ex.getMessage(), containsString("is not annotated by @AFXController!"));
    }

    @Test
    void testPostProcessBeanDefinition() {
        // GIVEN
        final BeanExtension beanExtension = Mockito.mock(BeanExtension.class);
        final ActionFXExtensionsBean extensionsBean = new ActionFXExtensionsBean(Collections.emptyList(),
                Arrays.asList(beanExtension));
        final DefaultActionFXBeanContainer container = new DefaultActionFXBeanContainer(extensionsBean);

        // WHEN
        container.postProcessBeanDefinition(NonController.class, "nonController", true, false);

        // THEN
        verify(beanExtension, times(1)).extendBean(NonController.class, "nonController", true, false);
    }

    @Test
    void testAddActionFXBeans() {
        // GIVEN
        final BeanContainerFacade beanContainer = new DefaultActionFXBeanContainer();

        // WHEN
        beanContainer.addActionFXBeans(ActionFX.getInstance());

        // THEN
        assertThat(beanContainer.getBean(BeanContainerFacade.ACTIONFX_EXTENSION_BEANNAME),
                instanceOf(ActionFXExtensionsBean.class));
        assertThat(beanContainer.getBean(BeanContainerFacade.CONTROLLER_INSTANCE_POSTPROCESSOR_BEANNAME),
                instanceOf(ControllerInstancePostProcessor.class));
        assertThat(beanContainer.getBean(BeanContainerFacade.BEAN_DEFINITION_POSTPROCESSOR_BEANNAME),
                instanceOf(BeanDefinitionPostProcessor.class));
        assertThat(beanContainer.getBean(BeanContainerFacade.EVENT_BUS_BEANNAME),
                instanceOf(PriorityAwareEventBus.class));
        assertThat(beanContainer.getBean(BeanContainerFacade.LOCALE_PROPERTY_BEANNAME),
                instanceOf(SimpleObjectProperty.class));
        assertThat(beanContainer.getBean(BeanContainerFacade.LOCALE_BEANNAME), instanceOf(Locale.class));
        assertThat(beanContainer.getBean(BeanContainerFacade.ACTIONFX_BEANNAME), instanceOf(ActionFX.class));
        assertThat(beanContainer.getBean(BeanContainerFacade.DIALOG_CONTROLLER_BEANNAME),
                instanceOf(DialogController.class));
        assertThat(beanContainer.getBean(BeanContainerFacade.CONVERSION_SERVICE_BEANNAME),
                instanceOf(ConversionService.class));
    }

    public static class NonController {

    }
}
