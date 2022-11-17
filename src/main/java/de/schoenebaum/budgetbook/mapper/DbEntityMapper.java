package de.schoenebaum.budgetbook.mapper;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import de.schoenebaum.budgetbook.db.entities.InternalTransaction;
import de.schoenebaum.budgetbook.db.entities.InternalTransactionTemplate;

@Service
public class DbEntityMapper {

	public InternalTransaction mapInternalTransactionTemplate(InternalTransactionTemplate template) {
		InternalTransaction transaction = new InternalTransaction();

		transaction.setSourceAccount(template.getSourceAccount());
		transaction.setTargetAccount(template.getTargetAccount());
		transaction.setSubject(template.getSubject());
		transaction.setAmount(template.getAmount());
		transaction.setDate(LocalDate.now());

		return transaction;
	}

}
