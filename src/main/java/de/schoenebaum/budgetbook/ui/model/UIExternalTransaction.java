package de.schoenebaum.budgetbook.ui.model;

import java.math.BigDecimal;

import de.schoenebaum.budgetbook.db.entities.ExternalTransaction;
import lombok.experimental.Delegate;

public class UIExternalTransaction extends ExternalTransaction implements UITransaction, GridTransaction {

	@Delegate
	protected final ExternalTransaction entity;

	public UIExternalTransaction() {
		this(new ExternalTransaction());
	}

	public UIExternalTransaction(ExternalTransaction externalTransaction) {
		this.entity = externalTransaction;
	}

	@Override
	public BigDecimal getSubjectiveAmount() {
		return getAmount();
	}

	@Override
	public ExternalTransaction getTransaction() {
		return entity;
	}

	@Override
	public GridTransaction getGridTransaction() {
		return this;
	}

}
