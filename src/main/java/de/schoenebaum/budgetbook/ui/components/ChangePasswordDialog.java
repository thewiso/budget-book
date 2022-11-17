package de.schoenebaum.budgetbook.ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;

import de.schoenebaum.budgetbook.services.UserService;

public class ChangePasswordDialog extends Dialog {

	private static final long serialVersionUID = -5634028439936315241L;
	private static final Logger LOG = LoggerFactory.getLogger(ChangePasswordDialog.class);

	private final UserService userService;

	private PasswordField currentPassword;
	private PasswordField newPassword;
	private PasswordField newPasswordRepititon;
	private ErrorHint errorHint;

	public ChangePasswordDialog(UserService userService) {
		this.userService = userService;
		
		currentPassword = new PasswordField("Current password");
		currentPassword.setRequired(true);
		currentPassword.setWidthFull();
		newPassword = new PasswordField("New password");
		newPassword.setRequired(true);
		newPassword.setWidthFull();
		newPasswordRepititon = new PasswordField("Repeat new password");
		newPasswordRepititon.setRequired(true);
		newPasswordRepititon.setWidthFull();
		
		errorHint = new ErrorHint();
		
		VerticalLayout fieldLayout = new VerticalLayout(currentPassword, newPassword, newPasswordRepititon,errorHint);
		add(fieldLayout);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> this.close());
		
		Button confirmButton = new Button("Ok");
		confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		confirmButton.addClickListener(this::onConfirmButtonClicked);
		
		getFooter().add(confirmButton, cancelButton);
		
		resetDialog();
	}

	@Override
	public void open() {
		resetDialog();
		super.open();
	}


	protected void onConfirmButtonClicked(ClickEvent<?> event) {
		errorHint.setVisible(false);
		
		if(currentPassword.isEmpty() || newPassword.isEmpty() || newPasswordRepititon.isEmpty()) {
			errorHint.setMessage("All fields have to be filled");
			errorHint.setVisible(true);
		}else if(!newPassword.getValue().equals(newPasswordRepititon.getValue())) {
			errorHint.setMessage("New password and it's repition have to be identical");
			errorHint.setVisible(true);
		}else {
			try {			
				userService.setNewPassword(currentPassword.getValue(), newPassword.getValue());
				this.close();
			} catch (Exception e) {
				LOG.error("Error while setting new password", e);
				errorHint.setMessage(e.getMessage());
				errorHint.setVisible(true);
			}
		}
	}

	protected void resetDialog() {
		currentPassword.clear();
		newPassword.clear();
		newPasswordRepititon.clear();
		errorHint.setVisible(false);
	}
}
