/*
 * Copyright (c) 2022 Martin Koster
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
package com.github.actionfx.core.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.ValidationMode;
import com.github.actionfx.core.decoration.CompoundValidationDecoration;
import com.github.actionfx.core.decoration.GraphicValidationDecoration;
import com.github.actionfx.core.decoration.StyleClassValidationDecoration;
import com.github.actionfx.core.decoration.ValidationDecoration;
import com.github.actionfx.core.listener.TimedChangeListener;
import com.github.actionfx.core.listener.TimedListChangeListener;
import com.github.actionfx.core.utils.AFXUtils;
import com.github.actionfx.core.validation.ValidationMessage;
import com.github.actionfx.core.validation.ValidationOptions;
import com.github.actionfx.core.validation.ValidationResult;
import com.github.actionfx.core.validation.Validator;
import com.github.actionfx.core.view.graph.ControlProperties;
import com.github.actionfx.core.view.graph.ControlWrapper;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.scene.control.Control;

/**
 * Abstract base class for an ActionFX view implementations.
 *
 * @author koster
 *
 */
public abstract class AbstractValidatingView extends AbstractView {

    protected ObjectProperty<ValidationDecoration> validationDecoratorProperty = new SimpleObjectProperty<>( // NOSONAR
            this, "validationDecorator",
            new CompoundValidationDecoration(new GraphicValidationDecoration(), new StyleClassValidationDecoration())) {
        @Override
        protected void invalidated() {
            // when the decorator changes, rerun the decoration to update the visuals
            // immediately.
            redecorate();
        }
    };

    // the validation result of the entire view including all of its controls
    protected ReadOnlyObjectWrapper<ValidationResult> validationResultProperty = new ReadOnlyObjectWrapper<>();

    // the current result of validations per control
    protected final ObservableMap<Control, ValidationResult> validationResults = FXCollections
            .synchronizedObservableMap(FXCollections.observableMap(new WeakHashMap<>()));

    protected BooleanProperty invalidProperty = new SimpleBooleanProperty();

    protected final ObservableMap<Control, List<ValidationTask>> validatedControlsMap = FXCollections
            .observableMap(new ConcurrentHashMap<>());

    protected AbstractValidatingView() {
        validationResultProperty.set(new ValidationResult(Collections.emptyList()));
        validationResultProperty().addListener((o, oldValue, validationResult) -> {
            invalidProperty.set(!validationResult.getErrors().isEmpty());
            redecorate();
        });

        // notify validation result observers
        validationResults.addListener((
                final MapChangeListener.Change<? extends Control, ? extends ValidationResult> change) -> validationResultProperty
                        .set(ValidationResult.from(validationResults.values())));
    }

    /**
     * @return The Validation decorator property
     */
    public ObjectProperty<ValidationDecoration> validationDecoratorProperty() {
        return validationDecoratorProperty;
    }

    /**
     * Returns current validation decorator
     *
     * @return current validation decorator or null if none
     */
    public ValidationDecoration getValidationDecorator() {
        return validationDecoratorProperty.get();
    }

    /**
     * Sets new validation decorator
     *
     * @param decorator
     *            new validation decorator. Null value is valid - no decoration will occur
     */
    public void setValidationDecorator(final ValidationDecoration decorator) {
        validationDecoratorProperty.set(decorator);
    }

    /**
     * Retrieves current validation result
     *
     * @return validation result
     */
    public ValidationResult getValidationResult() {
        return validationResultProperty.get();
    }

    /**
     * Can be used to track validation result changes
     *
     * @return The Validation result property.
     */
    @Override
    public ObservableValue<ValidationResult> validationResultProperty() {
        return validationResultProperty.getReadOnlyProperty();
    }

    @Override
    public ValidationResult validate(final boolean applyValidationDecoration) {
        // Validation is guaranteed to be executed within the JavaFX application thread.
        final Task<ValidationResult> validationTask = new Task<>() {
            @Override
            protected ValidationResult call() throws Exception {
                for (final List<ValidationTask> tasks : validatedControlsMap.values()) {
                    for (final ValidationTask task : tasks) {
                        task.accept(applyValidationDecoration);
                    }
                }
                return getValidationResult();
            }
        };
        try {
            return AFXUtils.runInFxThreadAndWait(validationTask);
        } catch (InterruptedException | ExecutionException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Unable to perform validation from thread " + Thread.currentThread().toString());
        }
    }

    @Override
    public void registerValidator(final Control control, final ControlProperties controlProperty,
            final Validator validator, final ValidationOptions options) {

        final ControlWrapper wrapper = ControlWrapper.of(control);
        applyRequiredDecoration(control, options, wrapper);

        final ValidationTask validationTask = createValidationTask(control, controlProperty, validator);
        registerValidationTask(control, validationTask);

        if (options.getValidationMode() == ValidationMode.ONCHANGE) {
            final Observable observable = wrapper.getObservable(controlProperty);
            installChangeListener(observable, validationTask, options.getValidationStartTimeoutMs(),
                    options.isApplyValidationResultDecorations());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void installChangeListener(final Observable observable, final ValidationTask validationTask,
            final int validationStartTimeoutMs, final boolean applyValidationDecoration) {
        if (ObservableValue.class.isAssignableFrom(observable.getClass())) {
            final ObservableValue observableValue = (ObservableValue) observable;
            observableValue.addListener(new TimedChangeListener(
                    (obs, oldValue, newValue) -> validationTask.accept(applyValidationDecoration),
                    validationStartTimeoutMs));
        } else if (ObservableList.class.isAssignableFrom(observable.getClass())) {
            final ObservableList observableList = (ObservableList) observable;
            observableList.addListener(new TimedListChangeListener(
                    (ListChangeListener) change -> validationTask.accept(applyValidationDecoration),
                    validationStartTimeoutMs));
        }
    }

    private void registerValidationTask(final Control control, final ValidationTask validationTask) {
        final List<ValidationTask> validationTasks = validatedControlsMap.computeIfAbsent(control,
                ctrl -> new ArrayList<>());
        validationTasks.add(validationTask);
    }

    private ValidationTask createValidationTask(final Control control, final ControlProperties controlProperty,
            final Validator validator) {
        return new ValidationTask(applyValidationResultDecoration -> AFXUtils.runInFxThread(() -> {
            final ValidationResult validationResult = validator.validate(control, controlProperty);
            if (validationResult != null) {
                validationResult.overrideApplyValidationResultDecoration(applyValidationResultDecoration);
                validationResults.put(control, validationResult);
            }
        }));
    }

    private void applyRequiredDecoration(final Control control, final ValidationOptions options,
            final ControlWrapper wrapper) {
        final RequiredFlagMapChangeListener listener = new RequiredFlagMapChangeListener();
        // make sure this listener is only registered once in that control
        control.getProperties().removeListener(listener);
        control.getProperties().addListener(listener);
        wrapper.setRequired(options.isRequired());
    }

    @Override
    public void registerValidator(final String nodeId, final ControlProperties controlProperty,
            final Validator validator, final ValidationOptions options) {
        final Control control = lookupNode(nodeId, Control.class).getWrapped();
        registerValidator(control, controlProperty, validator, options);
    }

    /**
     * Redecorates all known components Only decorations related to validation are affected
     */
    protected void redecorate() {
        final Optional<ValidationDecoration> optionalDecorator = Optional.ofNullable(getValidationDecorator());
        for (final Control target : validatedControlsMap.keySet()) {
            optionalDecorator.ifPresent(decorator -> {
                decorator.removeDecorations(target);
                if (ActionFX.getInstance().isValidationApplyRequiredDecoration()) {
                    decorator.applyRequiredDecoration(target);
                }
                final Optional<ValidationMessage> highestMessage = getHighestMessage(target);
                if (highestMessage.isPresent() && highestMessage.get().isApplyValidationResultDecoration()) {
                    decorator.applyValidationDecoration(highestMessage.get());
                }
            });
        }
    }

    /**
     * Returns optional highest severity message for a control
     *
     * @param target
     *            control
     * @return Optional highest severity message for a control
     */
    public Optional<ValidationMessage> getHighestMessage(final Control target) {
        return Optional.ofNullable(validationResults.get(target))
                .flatMap(result -> result.getMessages().stream().max(ValidationMessage.COMPARATOR));
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * A validation on a single control property. Class is only used for view-internal purposes.
     *
     * @author koster
     *
     */
    private static class ValidationTask implements Consumer<Boolean> {

        private final Consumer<Boolean> runnable;

        public ValidationTask(final Consumer<Boolean> runnable) {
            this.runnable = runnable;
        }

        @Override
        public void accept(final Boolean applyValidationResultDecoration) {
            runnable.accept(applyValidationResultDecoration);
        }
    }

    /**
     * Map listener that triggers a "redecorate" operations, when the "required" flag inside the control's properties is
     * changed.
     *
     * @author MartinKoster
     *
     */
    protected class RequiredFlagMapChangeListener implements MapChangeListener<Object, Object> {

        @Override
        public void onChanged(final Change<? extends Object, ? extends Object> change) {
            if (ControlWrapper.USER_PROPERTIES_REQUIRED_KEY.equals(change.getKey())) {
                redecorate();
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + RequiredFlagMapChangeListener.class.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            // 1 listener of this type allowed
            return getClass() == obj.getClass();
        }

    }
}
