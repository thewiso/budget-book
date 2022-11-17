package de.schoenebaum.budgetbook.ui.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.schoenebaum.budgetbook.ui.components.FormLayoutWithValue.FormLayoutWithValueChangeEvent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public abstract class FormLayoutWithValue<T> extends FormLayout
		implements HasValue<FormLayoutWithValueChangeEvent<T>, T> {

	private static final long serialVersionUID = -3031207734894918709L;

	private List<ValueChangeListener<? super FormLayoutWithValueChangeEvent<T>>> valueChangeEventListeners = new LinkedList<>();

	protected T currentValue;
	protected final Binder<T> binder;

	@AllArgsConstructor
	@EqualsAndHashCode
	@Accessors(chain = true)
	public static class FormLayoutWithValueChangeEvent<T> implements ValueChangeEvent<T> {

		private static final long serialVersionUID = 8809328319722392690L;

		@Getter
		@NonNull
		private T value;

		@Getter
		private boolean valueChanged;

		@Getter
		private boolean valueValid;

		private HasValue<?, T> source;

		@Getter
		private boolean fromClient;

		@Override
		public HasValue<?, T> getHasValue() {
			return source;
		}

		@Override
		public T getOldValue() {
			throw new UnsupportedOperationException();
		}

	}

	public FormLayoutWithValue(Binder<T> binder) {
		this.binder = binder;
	}

	protected void init() {
		binder.bindInstanceFields(this);
		binder.addValueChangeListener(this::fireValueChange);
	}

	protected void fireValueChange(ValueChangeEvent<?> event) {
		if (currentValue != null) {
			var newEvent = new FormLayoutWithValueChangeEvent<T>(currentValue,
					!Objects.equals(event.getValue(), event.getOldValue()), binder.isValid(), this,
					event.isFromClient());
			valueChangeEventListeners.forEach(action -> action.valueChanged(newEvent));
		}
	}

	@Override
	public void setValue(T value) {
		this.currentValue = value;
		binder.setBean(currentValue);

		if (currentValue != null) {
			binder.validate();
		}
	}

	@Override
	public T getValue() {
		return currentValue;
	}

	@Override
	public Registration addValueChangeListener(
			ValueChangeListener<? super FormLayoutWithValueChangeEvent<T>> listener) {
		return Registration.addAndRemove(valueChangeEventListeners, listener);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		binder.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequiredIndicatorVisible() {
		throw new UnsupportedOperationException();
	}

	public abstract void focusFirstElement();

}