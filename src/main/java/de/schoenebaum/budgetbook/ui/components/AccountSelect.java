package de.schoenebaum.budgetbook.ui.components;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.select.Select;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.services.AccountService;

public class AccountSelect extends Select<Account> {

	private static final long serialVersionUID = 1142045458151377098L;

	public AccountSelect(AccountService accountService) {
		Account openIncome = accountService.findOpenIncomeAccount();
		Account openExpense = accountService.findOpenExpenseAccount();
		List<Account> budgetAccounts = accountService.findBudgetAccounts();
		List<Account> allAccounts = new LinkedList<>();
		allAccounts.add(openIncome);
		allAccounts.add(openExpense);
		allAccounts.addAll(budgetAccounts);

		setLabel("Account");
		setItemLabelGenerator(this::getNullableAccountText);
		setItems(allAccounts);
		addComponentAtIndex(2, new Hr());
	}

	protected String getNullableAccountText(Account account) {
		if (account != null) {
			return account.getName();
		}
		return "";
	}

}
