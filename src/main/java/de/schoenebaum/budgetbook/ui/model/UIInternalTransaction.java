package de.schoenebaum.budgetbook.ui.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Supplier;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.entities.InternalTransaction;
import lombok.experimental.Delegate;

public class UIInternalTransaction extends InternalTransaction implements UITransaction, GridTransaction {

	@Delegate
	protected final InternalTransaction entity;
	private final Supplier<Account> relatedAccountSupplier;
	private final boolean negateAmount;

	public UIInternalTransaction(InternalTransaction transaction, Account referenceAccount) {
		Objects.requireNonNull(referenceAccount);
		Objects.requireNonNull(transaction);

		this.entity = transaction;

		if (referenceAccount.equals(getSourceAccount())) {
			negateAmount = true;
			relatedAccountSupplier = this::getTargetAccount;
		} else if (referenceAccount.equals(getTargetAccount())) {
			negateAmount = false;
			relatedAccountSupplier = this::getSourceAccount;
		} else {
			throw new IllegalArgumentException(
					"Given account is neither sourceAccount nor targetAccount of given transaction");
		}
	}

	@Override
	public String getRelatedPartyName() {
		Account relatedAccount = relatedAccountSupplier.get();
		return relatedAccount != null ? relatedAccount.getName() : null;
	}

	@Override
	public BigDecimal getSubjectiveAmount() {
		BigDecimal amount = getAmount();
		if (amount == null || !negateAmount) {
			return amount;
		} else {
			return amount.negate();
		}
	}

	@Override
	public InternalTransaction getTransaction() {
		return entity;
	}

	@Override
	public GridTransaction getGridTransaction() {
		return this;
	}
}
