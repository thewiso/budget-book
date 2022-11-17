package de.schoenebaum.budgetbook.services;

import org.springframework.context.annotation.Conditional;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;

import de.schoenebaum.budgetbook.security.SecurityEnabledCondition;

@Service
@Conditional(SecurityEnabledCondition.class)
public class SecurityService {

	private static final String ROOT_URL = "/";

	public void logout() {
		UI.getCurrent()
			.getPage()
			.setLocation(ROOT_URL);
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

		try {
			logoutHandler.logout(VaadinServletRequest.getCurrent()
				.getHttpServletRequest(), null, null);
		} catch (IllegalStateException e) {
			// Bug in Vaadin framework, ignore...
		}
	}

}
