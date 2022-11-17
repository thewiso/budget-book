package de.schoenebaum.budgetbook.db.entities;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class InternalTransactionTemplate {

	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid", updatable = false)
	protected UUID id;

	@ManyToOne
	@JoinColumn(nullable = false)
	protected Account sourceAccount;

	@ManyToOne
	@JoinColumn(nullable = false)
	protected Account targetAccount;

	@Column(nullable = false)
	protected String subject;

	@Column(nullable = false)
	protected BigDecimal amount;

}
