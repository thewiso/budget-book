package de.schoenebaum.budgetbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.schoenebaum.budgetbook.db.entities.User;
import de.schoenebaum.budgetbook.db.repositories.UserRepository;
import de.schoenebaum.budgetbook.security.AdminDetails;
import de.schoenebaum.budgetbook.security.SecurityEnabledCondition;

@Service
@Conditional(SecurityEnabledCondition.class)
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return new AdminDetails(userRepository.getUser());
	}

	public void setNewPassword(String currentPassword, String newPassword) throws Exception {
		User adminUser = userRepository.getUser();
		
		if(passwordEncoder.matches(currentPassword, adminUser.getHash())) {
			try {
				adminUser.setHash(passwordEncoder.encode(newPassword));
				userRepository.save(adminUser);
			}catch(Exception e) {
				throw new Exception("Error occured while persisting new password", e);
			}
		}else {
			throw new Exception("Current password is not correct");
		}
	}
}
