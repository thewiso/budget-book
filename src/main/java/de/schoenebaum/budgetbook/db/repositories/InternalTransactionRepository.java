package de.schoenebaum.budgetbook.db.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.entities.InternalTransaction;

public interface InternalTransactionRepository extends JpaRepository<InternalTransaction, UUID> {

	@Query("SELECT SUM(t.amount) FROM InternalTransaction t WHERE t.sourceAccount = ?1")
	Optional<BigDecimal> getSumBySourceAccount(Account account);

	@Query("SELECT SUM(t.amount) FROM InternalTransaction t WHERE t.targetAccount = ?1")
	Optional<BigDecimal> getSumByTargetAccount(Account account);

	@Query("SELECT t FROM InternalTransaction t WHERE t.sourceAccount = ?1 OR t.targetAccount = ?1")
	List<InternalTransaction> findByAccount(Account account);
}
