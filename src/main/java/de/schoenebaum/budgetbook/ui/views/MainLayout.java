package de.schoenebaum.budgetbook.ui.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import de.schoenebaum.budgetbook.db.repositories.AccountRepository;
import de.schoenebaum.budgetbook.db.repositories.ExternalTransactionRepository;
import de.schoenebaum.budgetbook.mapper.ImportXMLMapper;
import de.schoenebaum.budgetbook.security.SecurityProperties;
import de.schoenebaum.budgetbook.services.SecurityService;

@SpringComponent
@UIScope
public class MainLayout extends AppLayout implements RouterLayout {

	private static final Logger LOG = LoggerFactory.getLogger(MainLayout.class);
	private static final long serialVersionUID = -7720533774466642764L;

	private Tabs tabsComponent;
	private Map<Class<? extends Component>, Tab> navigationTarget2tab;

	public MainLayout(AccountRepository accountRepository, ExternalTransactionRepository externalTransactionRepository,
			ImportXMLMapper importMapper, SecurityProperties securityProperties, Optional<SecurityService> securityService) {
		DrawerToggle toggle = new DrawerToggle();

		H1 title = new H1("Budget Book");
		title.addClassName("navbar-title");

		navigationTarget2tab = new HashMap<>();
		tabsComponent = new Tabs();
		tabsComponent.setOrientation(Tabs.Orientation.VERTICAL);

		Tab accountOverviewTab = new Tab(VaadinIcon.HOME.create(), new RouterLink("Home", AccountOverview.class));
		navigationTarget2tab.put(AccountOverview.class, accountOverviewTab);
		tabsComponent.add(accountOverviewTab);

		Tab accountDetailsTab = new Tab(VaadinIcon.LIST.create(),
				new RouterLink("Account Details", AccountDetails.class));
		navigationTarget2tab.put(AccountDetails.class, accountDetailsTab);
		tabsComponent.add(accountDetailsTab);

		Tab internalTransactionTemplateTab = new Tab(VaadinIcon.EXCHANGE.create(),
				new RouterLink("Transaction Templates", InternalTransactionTemplateOverview.class));
		navigationTarget2tab.put(InternalTransactionTemplateOverview.class, internalTransactionTemplateTab);
		tabsComponent.add(internalTransactionTemplateTab);

		Tab analyticsOverviewTab = new Tab(VaadinIcon.BAR_CHART.create(),
				new RouterLink("Analytics", AnalyticsOverview.class));
		navigationTarget2tab.put(AnalyticsOverview.class, analyticsOverviewTab);
		tabsComponent.add(analyticsOverviewTab);
		
		if(securityProperties.isSecurityEnabled()) {
			Tab userOverviewTab = new Tab(VaadinIcon.USER.create(),
					new RouterLink("User", UserOverview.class));
			navigationTarget2tab.put(UserOverview.class, userOverviewTab);
			tabsComponent.add(userOverviewTab);
		}
		
		addToDrawer(tabsComponent);

		addToNavbar(toggle, VaadinIcon.BOOK_DOLLAR.create(), title);
		if (securityService.isPresent()) {
			Button logoutButton = new Button(VaadinIcon.SIGN_OUT.create());
			logoutButton.addClassName("navbar-logout-button");
			logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			logoutButton.addClickListener(e -> securityService.get()
				.logout());
			addToNavbar(logoutButton);
		}

	}

	public void focusTabItem(Class<?> navigationTarget) {
		Tab tab = navigationTarget2tab.get(navigationTarget);
		if (tab != null) {
			tabsComponent.setSelectedTab(tab);
		} else {
			LOG.error("Could not foucs tab with navigation targert {}", navigationTarget);
		}
	}

}
