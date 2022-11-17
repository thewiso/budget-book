package de.schoenebaum.budgetbook.ui.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface GridTransaction extends UITransaction {

	public LocalDate getDate();

	public BigDecimal getSubjectiveAmount();

	public String getSubject();

	public String getRelatedPartyName();

}
