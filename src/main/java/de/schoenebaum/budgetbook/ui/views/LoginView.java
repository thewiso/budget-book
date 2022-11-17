package de.schoenebaum.budgetbook.ui.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import de.schoenebaum.budgetbook.ui.components.PasswordLoginForm;

@Route("login")
@PageTitle("Login | Budget Book")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

	private static final long serialVersionUID = -977656416321240027L;

	@Autowired
	public LoginView() {		
		Div header = new Div();
		header.addClassName("login-header");
		header.add(new H1("Budget Book"));
		header.add(new Paragraph("Analyzing and budgeting income and outcome"));

		Div container = new Div(header, new PasswordLoginForm());
		container.addClassName("login-container");

		add(container);
		setHeightFull();
		setWidthFull();
		setAlignItems(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.CENTER);
		addClassName("login-background");
	}
}
