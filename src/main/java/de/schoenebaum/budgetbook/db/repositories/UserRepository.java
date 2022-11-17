package de.schoenebaum.budgetbook.db.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.schoenebaum.budgetbook.db.entities.User;

public interface UserRepository extends JpaRepository<User, UUID>{

	@Query("SELECT u FROM User u")
	User getUser();
	
}
