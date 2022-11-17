package de.schoenebaum.budgetbook.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties("de.schoenebaum.budgetbook.security")
@Getter
@Setter
public class SecurityProperties {

	private String initPasword = "password";
	
	private final boolean securityEnabled;
	
	@Autowired
	public SecurityProperties(Environment environment) {
		this.securityEnabled = SecurityEnabledCondition.isSecurityEnabled(environment);
	}	
	
}
