package de.schoenebaum.budgetbook;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.ui.model.UIExternalTransaction;

public class ExampleTests {

	@Test
	public void lombokHashCodesEquals() {
		UIExternalTransaction transaction = new UIExternalTransaction();
		transaction.setAccount(new Account());
		transaction.setAmount(BigDecimal.ONE);
		transaction.setDate(LocalDate.now());

		int hashCode1 = transaction.hashCode();
		int hashCode2 = transaction.hashCode();
		Assertions.assertEquals(hashCode1, hashCode2);
	}
}
