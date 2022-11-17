package de.schoenebaum.budgetbook.ui.model;

import de.schoenebaum.budgetbook.db.entities.Transaction;

public interface UITransaction {

	public Transaction getTransaction();

	public GridTransaction getGridTransaction();

}
