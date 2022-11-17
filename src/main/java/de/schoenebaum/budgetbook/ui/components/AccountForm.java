package de.schoenebaum.budgetbook.ui.components;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.value.ValueChangeMode;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.services.AccountService;

public class AccountForm extends FormLayoutWithValue<Account> {

	private static final long serialVersionUID = 8915570118287925407L;
	private final AccountService accountService;

	private TextField id = new TextField("ID");
	private TextField name = new TextField("Name");

	public AccountForm(AccountService accountService) {
		super(new Binder<>(Account.class));
		this.accountService = accountService;

		id.setReadOnly(true);
		name.setValueChangeMode(ValueChangeMode.EAGER);

		binder.forMemberField(name)
			.asRequired("Must not be empty")
			.withValidator(this::validateName);

		this.add(id);
		this.add(name);

		this.init();
	}

	protected ValidationResult validateName(String value, ValueContext context) {
		if (accountService.findByName(value) == null) {
			return ValidationResult.ok();
		} else {
			return ValidationResult.error("Another account with the same already exists");
		}
	}

	@Override
	public void focusFirstElement() {
		name.focus();
	}

}
