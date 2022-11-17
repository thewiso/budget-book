package de.schoenebaum.budgetbook.db.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode
@ToString
public abstract class Transaction {

	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid", updatable = false)
	protected UUID id;

	@Column(nullable = false)
	protected LocalDate date;

	@Column(nullable = false)
	protected String subject;

	@Column(nullable = false)
	protected BigDecimal amount;

}
