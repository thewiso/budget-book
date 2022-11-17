package de.schoenebaum.budgetbook.db.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.entities.ExternalTransaction;

public interface ExternalTransactionRepository extends JpaRepository<ExternalTransaction, UUID> {

    @Query("SELECT SUM(t.amount) FROM ExternalTransaction t WHERE t.account = ?1")
    Optional<BigDecimal> getSumByAccount(Account account);

    List<ExternalTransaction> findByAccount(Account account);

}
