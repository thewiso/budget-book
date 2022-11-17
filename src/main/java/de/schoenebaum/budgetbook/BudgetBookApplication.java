package de.schoenebaum.budgetbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication
@Theme("app-theme")
@EnableScheduling
public class BudgetBookApplication implements AppShellConfigurator {

	private static final long serialVersionUID = -8850789336835286259L;

	public static void main(String[] args) {
		SpringApplication.run(BudgetBookApplication.class, args);
	}

}
