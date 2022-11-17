package de.schoenebaum.budgetbook.ui.components;

import java.util.Objects;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;

import de.schoenebaum.budgetbook.db.entities.ExternalTransaction;
import de.schoenebaum.budgetbook.db.entities.InternalTransaction;
import de.schoenebaum.budgetbook.services.AccountService;
import de.schoenebaum.budgetbook.ui.components.FormLayoutWithValue.FormLayoutWithValueChangeEvent;
import de.schoenebaum.budgetbook.ui.model.UITransaction;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TransactionForm extends Div
		implements HasValue<FormLayoutWithValueChangeEvent<UITransaction>, UITransaction> {

	private static final long serialVersionUID = 3157228975866057893L;

	public enum FormType {
		InternalTransactionForm, ExternalTransactionForm
	}

	private FormLayoutWithValue activeForm;

	private final InternalTransactionForm internalTransactionForm;
	private final ExternalTransactionForm externalTransactionForm;

	public TransactionForm(AccountService accountService) {
		internalTransactionForm = new InternalTransactionForm(accountService);
		externalTransactionForm = new ExternalTransactionForm(accountService);

		internalTransactionForm.setVisible(false);
		externalTransactionForm.setVisible(false);

		this.add(internalTransactionForm);
		this.add(externalTransactionForm);
		this.setSizeFull();

		activateForm(FormType.ExternalTransactionForm);
	}

	@Override
	public void setValue(UITransaction value) {
		if (value != null) {
			if (value instanceof InternalTransaction) {
				activateForm(FormType.InternalTransactionForm);
			} else if (value instanceof ExternalTransaction) {
				activateForm(FormType.ExternalTransactionForm);
			} else {
				throw new IllegalArgumentException();
			}
		}
		activeForm.setValue(value);
	}

	@Override
	public UITransaction getValue() {
		return (UITransaction) activeForm.getValue();
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		activeForm.setReadOnly(readOnly);

	}

	@Override
	public boolean isReadOnly() {
		return activeForm.isReadOnly();
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		activeForm.setRequiredIndicatorVisible(requiredIndicatorVisible);

	}

	@Override
	public boolean isRequiredIndicatorVisible() {
		return activeForm.isRequiredIndicatorVisible();
	}

	public void activateForm(FormType formType) {
		Objects.requireNonNull(formType);

		if (activeForm != null) {
			activeForm.setVisible(false);
		}

		switch (formType) {
		case ExternalTransactionForm:
			activeForm = externalTransactionForm;
			break;
		case InternalTransactionForm:
			activeForm = internalTransactionForm;
			break;
		default:
			throw new IllegalArgumentException();
		}

		activeForm.setVisible(true);
	}

	@Override
	public Registration addValueChangeListener(
			ValueChangeListener<? super FormLayoutWithValueChangeEvent<UITransaction>> listener) {
		throw new UnsupportedOperationException();
	}

	public InternalTransactionForm getInternalTransactionForm() {
		return internalTransactionForm;
	}

	public ExternalTransactionForm getExternalTransactionForm() {
		return externalTransactionForm;
	}

	public void focusFirstElement() {
		activeForm.focusFirstElement();
	}
}
