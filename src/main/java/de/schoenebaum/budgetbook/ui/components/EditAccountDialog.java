package de.schoenebaum.budgetbook.ui.components;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.shared.Registration;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.services.AccountService;
import de.schoenebaum.budgetbook.ui.components.FormLayoutWithValue.FormLayoutWithValueChangeEvent;

public class EditAccountDialog extends Dialog {

	private static final long serialVersionUID = -1783724019650060092L;
	private static final Logger LOG = LoggerFactory.getLogger(EditAccountDialog.class);

	protected List<Runnable> changePersistedListeners = new LinkedList<>();

	protected final AccountService accountService;

	protected AccountForm accountForm;
	protected Button confirmButton;

	public EditAccountDialog(AccountService accountService) {
		this.accountService = accountService;
		accountForm = new AccountForm(accountService);
		accountForm.addValueChangeListener(this::onAccountFormValueChange);

		confirmButton = new Button("Ok");
		confirmButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
		confirmButton.addClickListener(this::onConfirmButtonClicked);

		Button cancelButton = new Button("Cancel");
		cancelButton.addClickListener(event -> this.close());

		add(accountForm);
		getFooter().add(confirmButton, cancelButton);
	}

	public void createNewAccount() {
		setHeaderTitle("Create New Account");
		Account newAccount = new Account();
		newAccount.setId(UUID.randomUUID());
		accountForm.setValue(newAccount);
		enableByFormValidity(false);

		this.open();
		accountForm.focusFirstElement();
	}

	public void editAccount(Account account) {
		setHeaderTitle("Create New Account");
		accountForm.setValue(account);
		this.open();
		accountForm.focusFirstElement();
	}

	protected void enableByFormValidity(boolean valid) {
		confirmButton.setEnabled(valid);
	}

	protected void onAccountFormValueChange(FormLayoutWithValueChangeEvent<Account> event) {
		enableByFormValidity(event.isValueValid());
	}

	protected void onConfirmButtonClicked(ClickEvent<?> event) {
		try {
			accountService.save(accountForm.getValue());
			changePersistedListeners.forEach(Runnable::run);
			this.close();
		} catch (Exception e) {
			LOG.error("Could not persist account {}", accountForm.getValue(), e);
			Notification notification = new Notification("Could not persist changes. Please try again.");
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.open();
		}
	}

	public Registration addChangePersistedListener(Runnable listener) {
		return Registration.addAndRemove(changePersistedListeners, listener);
	}

}
