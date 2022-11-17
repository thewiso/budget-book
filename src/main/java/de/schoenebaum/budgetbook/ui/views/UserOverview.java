package de.schoenebaum.budgetbook.ui.views;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.schoenebaum.budgetbook.security.SecurityEnabledCondition;
import de.schoenebaum.budgetbook.services.UserService;
import de.schoenebaum.budgetbook.ui.components.ChangePasswordDialog;

@PermitAll
@Route(value = "user", layout = MainLayout.class)
@Conditional(SecurityEnabledCondition.class)
@PageTitle("User | Budget Book")
public class UserOverview extends VerticalLayout {

	private static final long serialVersionUID = 1452820930689839041L;

	@Autowired
	public UserOverview(MainLayout mainLayout, UserService userService) {
		ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(userService);

		Button changePasswordButton = new Button("Change password");
		changePasswordButton.addClickListener(e -> changePasswordDialog.open());

		this.add(changePasswordButton, changePasswordDialog);

		mainLayout.focusTabItem(getClass());
	}

}
