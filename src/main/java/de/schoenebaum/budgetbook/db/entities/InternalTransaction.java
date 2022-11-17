package de.schoenebaum.budgetbook.db.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InternalTransaction extends Transaction {

	@ManyToOne
	@JoinColumn(nullable = false)
	protected Account sourceAccount;

	@ManyToOne
	@JoinColumn(nullable = false)
	protected Account targetAccount;

}
