package de.schoenebaum.budgetbook.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.entities.User;
import de.schoenebaum.budgetbook.db.repositories.AccountRepository;
import de.schoenebaum.budgetbook.db.repositories.UserRepository;
import de.schoenebaum.budgetbook.security.SecurityProperties;

@Configuration
public class DatabasePopulation {

	@Bean
	public CommandLineRunner createAccounts(AccountRepository repository) {
		return (args) -> {
			String openIncome = "Open Income";
			if (repository.findByName(openIncome) == null) {
				Account openIncomeAccount = new Account();
				openIncomeAccount.setName(openIncome);
				repository.save(openIncomeAccount);
			}

			String openExpense = "Open Expense";
			if (repository.findByName(openExpense) == null) {
				Account openExpenseAccount = new Account();
				openExpenseAccount.setName(openExpense);
				repository.save(openExpenseAccount);
			}
		};
	}

	@Bean
	@ConditionalOnBean(value = PasswordEncoder.class)
	public CommandLineRunner createUser(UserRepository repository, PasswordEncoder encoder,
			SecurityProperties securityProperties) {
		return (args) -> {
			if (repository.getUser() == null) {
				User user = new User();
				user.setHash(encoder.encode(securityProperties.getInitPasword()));
				repository.save(user);
			}
		};
	}
}
