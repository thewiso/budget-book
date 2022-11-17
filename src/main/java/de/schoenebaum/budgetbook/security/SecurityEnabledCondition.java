package de.schoenebaum.budgetbook.security;

import java.util.List;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SecurityEnabledCondition implements Condition {

	@Override

	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return isSecurityEnabled(context.getEnvironment());
	}

	@SuppressWarnings("unchecked")
	public static boolean isSecurityEnabled(Environment environment) {
		List<String> excludedAutoConfigurations = environment.getProperty("spring.autoconfigure.exclude", List.class);
		return excludedAutoConfigurations == null
				|| !excludedAutoConfigurations.contains(SecurityAutoConfiguration.class.getName());
	}
}
