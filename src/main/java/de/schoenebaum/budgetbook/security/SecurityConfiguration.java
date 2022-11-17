package de.schoenebaum.budgetbook.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import de.schoenebaum.budgetbook.ui.views.LoginView;

@EnableWebSecurity
@Configuration
@Conditional(SecurityEnabledCondition.class)
public class SecurityConfiguration extends VaadinWebSecurity {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests();
		super.configure(http);
		setLoginView(http, LoginView.class);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}


}