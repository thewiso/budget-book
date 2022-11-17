package de.schoenebaum.budgetbook.db.entities;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Account {

	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid", updatable = false)
	protected UUID id;

	@Column(unique = true, nullable = false)
	protected String name;

}
