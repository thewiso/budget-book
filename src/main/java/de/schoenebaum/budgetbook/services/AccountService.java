package de.schoenebaum.budgetbook.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.repositories.AccountRepository;
import de.schoenebaum.budgetbook.db.repositories.ExternalTransactionRepository;

@Service
@Transactional(readOnly = true)
public class AccountService {

	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository,
			ExternalTransactionRepository externalTransactionRepository) {
		this.accountRepository = accountRepository;
	}

	public Account getById(UUID id) {
		Account account = accountRepository.getReferenceById(id);
		account.getName(); // getReferenceById seems to be implemented with lazy initialization, so this needed
		return account;
	}

	public List<Account> findBudgetAccounts() {
		return accountRepository.findByNameNotIn(new String[] { "Open Income", "Open Expense" });
	}

	public Account findOpenIncomeAccount() {
		return accountRepository.findByName("Open Income");
	}

	public Account findOpenExpenseAccount() {
		return accountRepository.findByName("Open Expense");
	}

	@Transactional
	public void save(Account account) {
		accountRepository.save(account);
	}

	@Transactional
	public void delete(Account account) {
		accountRepository.delete(account);
	}

	public Account findByName(String name) {
		return accountRepository.findByName(name);
	}

}