package de.schoenebaum.budgetbook.ui.views;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.PermitAll;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.services.AccountService;
import de.schoenebaum.budgetbook.services.ImportCSVService;
import de.schoenebaum.budgetbook.services.TransactionService;
import de.schoenebaum.budgetbook.ui.components.EditAccountDialog;
import de.schoenebaum.budgetbook.ui.components.ImportDataDialog;

@PermitAll
@Route(value = "", layout = MainLayout.class)
@PageTitle("Account Overview | Budget Book")
public class AccountOverview extends VerticalLayout {

	private static final long serialVersionUID = -4301893990049790824L;
	private final AccountService accountService;
	private final TransactionService transactionService;

	private Grid<Account> basicAccountsGrid;
	private GridListDataView<Account> basicAccountsGridDataView;
	private final List<Account> basicAccounts;
	private Grid<Account> budgetAccountsGrid;
	private GridListDataView<Account> budgetAccountsGridDataView;
	private final List<Account> budgetAccounts;
	private TextField budgetAccountSearchField;
	private Button addBudgetAccountButton;
	private EditAccountDialog editAccountDialog;

	@Autowired
	public AccountOverview(MainLayout mainLayout, AccountService accountService, TransactionService transactionService,
			ImportCSVService importCSVService) {
		this.accountService = accountService;
		this.transactionService = transactionService;

		// IMPORT DATA DIALOG
		ImportDataDialog importDataDialog = new ImportDataDialog(importCSVService);
		importDataDialog.addChangePersistedListener(this::loadBasicAccounts);
		Button openImportDataDialogButton = new Button("Import", VaadinIcon.UPLOAD.create());
		openImportDataDialogButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		openImportDataDialogButton.addClickListener(e -> importDataDialog.open());

		// INCOME / EXPENSE
		basicAccounts = new LinkedList<>();
		ComponentRenderer<RouterLink, Account> accountLinkRenderer = new ComponentRenderer<>(this::createAccountLink);
		ComponentRenderer<Span, Account> balanceRenderer = new ComponentRenderer<>(this::createBalanceSpan);

		basicAccountsGrid = new Grid<>(Account.class, false);
		basicAccountsGrid.addColumn(accountLinkRenderer)
			.setHeader("Name");
		basicAccountsGrid.addColumn(balanceRenderer)
			.setHeader("Balance");
		basicAccountsGrid.setAllRowsVisible(true);
		basicAccountsGridDataView = basicAccountsGrid.setItems(basicAccounts);
		basicAccountsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		basicAccountsGrid.setSelectionMode(SelectionMode.NONE);

		loadBasicAccounts();
		
		// BUDGET ACCOUNTS
		budgetAccountSearchField = new TextField();
		budgetAccountSearchField.setPlaceholder("Search");
		budgetAccountSearchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		budgetAccountSearchField.setValueChangeMode(ValueChangeMode.EAGER);
		budgetAccountSearchField.addValueChangeListener(e -> budgetAccountsGridDataView.refreshAll());

		addBudgetAccountButton = new Button("Add", VaadinIcon.PLUS.create());
		addBudgetAccountButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		addBudgetAccountButton.addClickListener(event -> editAccountDialog.createNewAccount());

		HorizontalLayout topGridBar = new HorizontalLayout();
		topGridBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
		topGridBar.add(addBudgetAccountButton, budgetAccountSearchField);
		topGridBar.setWidthFull();
		topGridBar.setAlignItems(Alignment.END);

		budgetAccounts = new LinkedList<>();

		budgetAccountsGrid = new Grid<>(Account.class, false);
		Column<Account> nameColumn = budgetAccountsGrid.addColumn(accountLinkRenderer)
			.setHeader("Name")
			.setSortable(true)
			.setComparator(Account::getName);
		budgetAccountsGrid.addColumn(balanceRenderer)
			.setHeader("Balance");
		budgetAccountsGrid.addComponentColumn(this::createChangeAccountButton);
		budgetAccountsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		budgetAccountsGrid.setSelectionMode(SelectionMode.NONE);
		budgetAccountsGridDataView = budgetAccountsGrid.setItems(budgetAccounts);
		budgetAccountsGridDataView.addFilter(this::filterBudgetAccounts);
		budgetAccountsGrid.getDataCommunicator()
			.getKeyMapper()
			.setIdentifierGetter(Account::getId);

		GridSortOrder<Account> order = new GridSortOrder<>(nameColumn, SortDirection.ASCENDING);
		budgetAccountsGrid.sort(Arrays.asList(order));

		loadBudgetAccounts();

		// EDIT ACCOUNT DIALOG
		editAccountDialog = new EditAccountDialog(accountService);
		editAccountDialog.addChangePersistedListener(this::loadBudgetAccounts);

		this.add(openImportDataDialogButton, new H2("Income/Expense"), basicAccountsGrid, new H2("Budgets"), topGridBar,
				budgetAccountsGrid, editAccountDialog, importDataDialog);
		mainLayout.focusTabItem(getClass());
	}

	protected Span createBalanceSpan(Account account) {
		BigDecimal balance = transactionService.getSum(account);

		Span retVal = new Span(balance.toPlainString());

		if (balance.compareTo(BigDecimal.ZERO) < 0) {
			retVal.getElement()
				.getThemeList()
				.add("badge error");
		} else {
			retVal.getElement()
				.getThemeList()
				.add("badge success");
		}

		return retVal;
	}

	protected RouterLink createAccountLink(Account account) {
		return new RouterLink(account.getName(), AccountDetails.class, account.getId()
			.toString());
	}

	protected boolean filterBudgetAccounts(Account account) {
		String searchTerm = budgetAccountSearchField.getValue()
			.trim();

		if (searchTerm.isEmpty()) {
			return true;
		}
		return StringUtils.containsIgnoreCase(account.getName(), searchTerm);
	}

	protected void loadBudgetAccounts() {
		budgetAccounts.clear();
		budgetAccounts.addAll(accountService.findBudgetAccounts());
		budgetAccountsGridDataView.refreshAll();
	}

	protected void loadBasicAccounts() {
		basicAccounts.clear();
		Account openIncome = accountService.findOpenIncomeAccount();
		Account openExpense = accountService.findOpenExpenseAccount();
		basicAccounts.add(openIncome);
		basicAccounts.add(openExpense);
		basicAccountsGridDataView.refreshAll();
	}

	protected Button createChangeAccountButton(Account account) {
		Button retVal = new Button("Edit", VaadinIcon.EDIT.create());
		retVal.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
		retVal.addClickListener(event -> {
			editAccountDialog.editAccount(account);
		});
		return retVal;
	}

}
