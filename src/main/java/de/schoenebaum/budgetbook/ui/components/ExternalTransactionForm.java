package de.schoenebaum.budgetbook.ui.components;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;

import de.schoenebaum.budgetbook.services.AccountService;
import de.schoenebaum.budgetbook.ui.model.UIExternalTransaction;
import de.schoenebaum.budgetbook.utils.Validators;

public class ExternalTransactionForm extends FormLayoutWithValue<UIExternalTransaction> {

	private static final long serialVersionUID = -3031207734894918709L;

	private TextField id = new TextField("ID");
	private TextField externalId = new TextField("External ID");
	private DatePicker date = new DatePicker("Date");
	private TextField relatedPartyName = new TextField("Related Party Name");
	private TextField relatedPartyId = new TextField("Related Party ID");
	private TextField subject = new TextField("Subject");
	private BigDecimalField amount = new BigDecimalField("Amount");
	private AccountSelect account;

	public ExternalTransactionForm(AccountService accountService) {
		super(new Binder<>(UIExternalTransaction.class));
		// TODO: autocomplete
		id.setReadOnly(true);
		externalId.setReadOnly(true);
		relatedPartyName.setValueChangeMode(ValueChangeMode.EAGER);
		relatedPartyId.setValueChangeMode(ValueChangeMode.EAGER);
		subject.setValueChangeMode(ValueChangeMode.EAGER);
		amount.setValueChangeMode(ValueChangeMode.EAGER);
		this.account = new AccountSelect(accountService);

		binder.forMemberField(relatedPartyName)
			.asRequired("Must not be empty");
		binder.forMemberField(date)
			.asRequired("Must not be empty");
		binder.forMemberField(subject)
			.asRequired("Must not be empty");
		binder.forMemberField(amount)
			.asRequired("Must not be empty")
			.withValidator(Validators.BIG_DECIMAL_NOT_ZERO);

		this.add(id);
		this.add(externalId);
		this.add(date);
		this.add(account);
		this.add(relatedPartyName);
		this.add(relatedPartyId);
		this.add(subject);
		this.add(amount);

		this.init();
	}

	@Override
	public void focusFirstElement() {
		relatedPartyName.focus();
	}
}