package de.schoenebaum.budgetbook.db.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.schoenebaum.budgetbook.db.entities.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {

	List<Account> findByNameNotIn(String[] names);

	Account findByName(String name);
}
