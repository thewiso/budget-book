package de.schoenebaum.budgetbook.db.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.schoenebaum.budgetbook.db.entities.InternalTransactionTemplate;

public interface InternalTransactionTemplateRepository extends JpaRepository<InternalTransactionTemplate, UUID> {

}
