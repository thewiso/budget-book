package de.schoenebaum.budgetbook.db.entities;

import javax.persistence.Column;
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
public class ExternalTransaction extends Transaction {

	@Column(unique = true, nullable = false)
	protected String externalId;

	@Column(nullable = false)
	protected String relatedPartyName;

	protected String relatedPartyId;

	@ManyToOne
	@JoinColumn(nullable = false)
	protected Account account;

}
