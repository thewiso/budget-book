package de.schoenebaum.budgetbook.ui.components;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;

import de.schoenebaum.budgetbook.services.AccountService;
import de.schoenebaum.budgetbook.ui.model.UIInternalTransaction;
import de.schoenebaum.budgetbook.utils.Validators;

public class InternalTransactionForm extends FormLayoutWithValue<UIInternalTransaction> {

	private static final long serialVersionUID = 3856628451799991335L;

	private TextField id = new TextField("ID");
	private DatePicker date = new DatePicker("Date");
	private TextField subject = new TextField("Subject");
	private BigDecimalField amount = new BigDecimalField("Amount");
	private AccountSelect sourceAccount;
	private AccountSelect targetAccount;

	public InternalTransactionForm(AccountService accountService) {
		super(new Binder<>(UIInternalTransaction.class));
		// TODO: autocomplete
		id.setReadOnly(true);
		subject.setValueChangeMode(ValueChangeMode.EAGER);
		amount.setValueChangeMode(ValueChangeMode.EAGER);
		sourceAccount = new AccountSelect(accountService);
		sourceAccount.setReadOnly(true);
		targetAccount = new AccountSelect(accountService);
		targetAccount.setEmptySelectionAllowed(true);

		binder.forMemberField(date)
			.asRequired("Must not be empty");
		binder.forMemberField(subject)
			.asRequired("Must not be empty");
		binder.forMemberField(amount)
			.asRequired("Must not be empty")
			.withValidator(Validators.BIG_DECIMAL_GREATER_THAN_ZERO);
		binder.forMemberField(targetAccount)
			.asRequired("Must not be empty");

		this.add(id);
		this.add(date);
		this.add(sourceAccount);
		this.add(targetAccount);
		this.add(subject);
		this.add(amount);

		this.init();
	}

	@Override
	public void setValue(UIInternalTransaction value) {
		super.setValue(value);
		if (value != null) {
			targetAccount
				.setItemEnabledProvider(account -> account == null || !account.equals(value.getSourceAccount()));
		}
	}

	@Override
	public void focusFirstElement() {
		subject.focus();
	}

}
