package de.schoenebaum.budgetbook.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import de.schoenebaum.budgetbook.db.entities.User;

public class AdminDetails implements UserDetails {

	private static final long serialVersionUID = -1122094795638497723L;
	
	private final User user;
	
	public AdminDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(() -> "ALL");
	}

	@Override
	public String getPassword() {
		return user.getHash();
	}

	@Override
	public String getUsername() {
		return "admin";
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
