package de.schoenebaum.budgetbook.db.entities;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Table(name = "USER_")
public class User {
	
	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid", updatable = false)
	protected UUID id;

	private String hash;
	
}
