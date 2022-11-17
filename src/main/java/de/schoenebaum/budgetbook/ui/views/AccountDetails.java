package de.schoenebaum.budgetbook.ui.views;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.PermitAll;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.FooterRow.FooterCell;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveEvent.ContinueNavigationAction;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.entities.ExternalTransaction;
import de.schoenebaum.budgetbook.db.entities.InternalTransaction;
import de.schoenebaum.budgetbook.db.entities.Transaction;
import de.schoenebaum.budgetbook.services.AccountService;
import de.schoenebaum.budgetbook.services.TransactionService;
import de.schoenebaum.budgetbook.ui.components.AccountSelect;
import de.schoenebaum.budgetbook.ui.components.FormLayoutWithValue.FormLayoutWithValueChangeEvent;
import de.schoenebaum.budgetbook.ui.components.SimpleConfirmDialog;
import de.schoenebaum.budgetbook.ui.components.TransactionForm;
import de.schoenebaum.budgetbook.ui.model.GridTransaction;
import de.schoenebaum.budgetbook.ui.model.UIExternalTransaction;
import de.schoenebaum.budgetbook.ui.model.UIInternalTransaction;
import de.schoenebaum.budgetbook.ui.model.UITransaction;

@PermitAll
@Route(value = "accounts", layout = MainLayout.class)
public class AccountDetails extends VerticalLayout implements HasUrlParameter<String>, BeforeLeaveObserver, HasDynamicTitle {

	private static final long serialVersionUID = -3104605716568210030L;
	private static final Logger LOG = LoggerFactory.getLogger(AccountDetails.class);

	private final AccountService accountService;
	private final TransactionService transactionService;

	private Account selectedAccount = null;
	private final List<GridTransaction> transactions = new LinkedList<>();

	private H2 title;
	private AccountSelect accountSelect;
	private TextField transactionsSearchField;
	private HorizontalLayout topGridBar;
	private Div editListControls;
	private Button editListButton;
	private MenuBar editListActionMenuBar;
	private MenuItem addTransactionMenuItem;
	private MenuItem addInternalTransactionMenuItem;
	private MenuItem addExternalTransactionMenuItem;
	private MenuItem deleteTransactionMenuItem;
	private MenuItem saveListChangesMenuItem;
	private MenuItem discardListChangesMenuItem;

	private Grid<GridTransaction> transactionsGrid;
	private GridListDataView<GridTransaction> transactionsGridDataView;
	private Column<GridTransaction> dateColumn;
	private FooterCell sumCell;

	private boolean editModeActivated = false;
	private TransactionForm transactionForm;

	private UITransaction currentSelectedTransaction;
	private HashMap<UUID, Transaction> changedTransactions = new HashMap<>();
	private HashMap<UUID, Transaction> deletedTransactions = new HashMap<>();
	private HashMap<UUID, Transaction> addedTransactions = new HashMap<>();

	@Autowired
	public AccountDetails(MainLayout mainLayout, AccountService accountService,
			TransactionService externalTransactionService) {
		this.accountService = accountService;
		this.transactionService = externalTransactionService;

		title = new H2();
		
		// SELECT
		accountSelect = new AccountSelect(accountService);
		accountSelect.setEmptySelectionAllowed(true);
		accountSelect.setValue(accountSelect.getEmptyValue());
		accountSelect.addValueChangeListener(this::onSelectValueChanged);

		// SEARCH FIELD
		transactionsSearchField = new TextField();
		transactionsSearchField.setPlaceholder("Search");
		transactionsSearchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		transactionsSearchField.setValueChangeMode(ValueChangeMode.EAGER);
		transactionsSearchField.addValueChangeListener(e -> transactionsGridDataView.refreshAll());


		// EDIT LIST ACTIONS
		editListButton = new Button("Edit", new Icon(VaadinIcon.PENCIL));
		editListButton.addClickListener(this::onEditListButtonClicked);

		editListActionMenuBar = new MenuBar();
		
		addTransactionMenuItem = editListActionMenuBar.addItem(new Icon(VaadinIcon.PLUS));
		addTransactionMenuItem.add("Add");
		addTransactionMenuItem.addThemeNames("success");

		addInternalTransactionMenuItem = addTransactionMenuItem.getSubMenu()
			.addItem("Add Internal Transaction");
		addInternalTransactionMenuItem.addClickListener(this::onAddInternalTransactionButtonClicked);
		addExternalTransactionMenuItem = addTransactionMenuItem.getSubMenu()
			.addItem("Add External Transaction");
		addExternalTransactionMenuItem.addClickListener(this::onAddExternalTransactionButtonClicked);

		deleteTransactionMenuItem = editListActionMenuBar.addItem(new Icon(VaadinIcon.MINUS));
		styleDefaultDeleteTransactionButton();
		deleteTransactionMenuItem.addThemeNames("error", "last");
		deleteTransactionMenuItem.addClickListener(this::onDeleteTransactionButtonClicked);

		saveListChangesMenuItem = editListActionMenuBar.addItem(new Icon(VaadinIcon.CHECK));
		saveListChangesMenuItem.add("Save");
		saveListChangesMenuItem.addThemeNames("primary", "first");
		saveListChangesMenuItem.addClickListener(this::onSaveListChangeButtonClicked);

		discardListChangesMenuItem = editListActionMenuBar.addItem(new Icon(VaadinIcon.CLOSE));
		discardListChangesMenuItem.add("Cancel");
		discardListChangesMenuItem.addThemeNames("primary", "error");
		discardListChangesMenuItem.addClickListener(this::onDiscardListChangeButtonClicked);

		editListControls = new Div();
		editListControls.add(editListButton);
		editListControls.addClassName("with-top-padding-l");
		

		topGridBar = new HorizontalLayout();
		topGridBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
		topGridBar.add(editListControls, transactionsSearchField);
		topGridBar.setWidthFull();
		topGridBar.setAlignItems(Alignment.END);

		// GRID
		transactionsGrid = new Grid<>(GridTransaction.class, false);
		transactionsGrid.addComponentColumn(this::getIconByTransaction)
			.setHeader("Type")
			.setSortable(false)
			.setAutoWidth(true);
		dateColumn = transactionsGrid.addColumn(GridTransaction::getDate)
			.setHeader("Date")
			.setSortable(true)
			.setAutoWidth(true);
		Column<GridTransaction> amountColumn = transactionsGrid.addColumn(GridTransaction::getSubjectiveAmount)
			.setHeader("Amount")
			.setSortable(true)
			.setAutoWidth(true);
		transactionsGrid.addColumn(GridTransaction::getSubject)
			.setHeader("Subject")
			.setSortable(true)
			.setAutoWidth(true);
		transactionsGrid.addColumn(GridTransaction::getRelatedPartyName)
			.setHeader("Related Party Name")
			.setSortable(true)
			.setAutoWidth(true);

		sumCell = transactionsGrid.prependFooterRow().getCell(amountColumn);
		
		GridSortOrder<GridTransaction> order = new GridSortOrder<>(dateColumn, SortDirection.DESCENDING);
		transactionsGrid.sort(Arrays.asList(order));

		transactionsGrid.setClassNameGenerator(this::getGridRowClassName);
		transactionsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		transactionsGrid.setSelectionMode(SelectionMode.SINGLE);
		transactionsGrid.addSelectionListener(this::onTransactionSelected);

		transactionsGridDataView = transactionsGrid.setItems(transactions);
		// the following line had to be added because the grid does not handle the
		// changing hashCode values of one grid entry while editing
		transactionsGrid.getDataCommunicator()
			.getKeyMapper()
			.setIdentifierGetter(t -> t.getTransaction()
				.getId());
		transactionsGridDataView.addFilter(this::filterTransactions);

		// TRANSACTION FORM
		transactionForm = new TransactionForm(accountService);
		transactionForm.setVisible(false);
		transactionForm.getInternalTransactionForm()
			.addValueChangeListener(this::onTransactionFormValueChange);
		transactionForm.getExternalTransactionForm()
			.addValueChangeListener(this::onTransactionFormValueChange);

		this.add(title, accountSelect, topGridBar, transactionsGrid, transactionForm);
		mainLayout.focusTabItem(getClass());
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		transactions.clear();
		boolean enableElements;
		if (parameter == null) {
			accountSelect.setValue(accountSelect.getEmptyValue());
			enableElements = false;
		} else {
			UUID accountId = UUID.fromString(parameter);
			selectedAccount = accountService.getById(accountId);
			title.setText(selectedAccount.getName());
			accountSelect.setValue(selectedAccount);
			refreshTransactions(accountId);
			enableElements = true;
		}
		
		topGridBar.setEnabled(enableElements);
		transactionsGrid.setEnabled(enableElements);
	}

	@Override
	public void beforeLeave(BeforeLeaveEvent event) {
		if (editModeActivated) {
			ContinueNavigationAction action = event.postpone();

			SimpleConfirmDialog.createSimpleConfirmDialog("Discard changes", "Do you really want to discard changes?",
					confirmEvent -> {
						action.proceed();
					})
				.open();
		}

	}

	protected void refreshTransactions(UUID id) {
		selectedAccount = accountService.getById(id);
		transactions.clear();
		transactions.addAll(transactionService.getTransactions(selectedAccount));
		transactionsGridDataView.refreshAll();
		sumCell.setComponent(createSumComponent(transactionService.getSum(selectedAccount)));
	}

	protected void onSelectValueChanged(ValueChangeEvent<Account> event) {
		String accountId;
		if (event.getValue() != null) {
			accountId = event.getValue()
				.getId()
				.toString();
		} else {
			accountId = null;
		}

		getUI().ifPresent(ui -> ui.navigate(getClass(), accountId));

	}

	protected boolean filterTransactions(GridTransaction transaction) {
		String searchTerm = transactionsSearchField.getValue()
			.trim();

		if (searchTerm.isEmpty()) {
			return true;
		}
		
		String[] searchTerms = searchTerm.split("\\s");
		
		return StringUtils.containsAnyIgnoreCase(transaction.getSubject(), searchTerms) 
				|| StringUtils.containsAnyIgnoreCase(transaction.getRelatedPartyName(), searchTerms) ;
	}

	protected void onEditListButtonClicked(ClickEvent<Button> event) {
		activateEditMode(true);
		selectTransaction(transactionsGrid.getSelectionModel()
			.getFirstSelectedItem());
	}

	protected void onAddInternalTransactionButtonClicked(ClickEvent<?> event) {
		InternalTransaction newTransaction = new InternalTransaction();
		newTransaction.setId(UUID.randomUUID());
		newTransaction.setDate(LocalDate.now());
		newTransaction.setSourceAccount(selectedAccount);

		UIInternalTransaction newUITransaction = new UIInternalTransaction(newTransaction, selectedAccount);

		addedTransactions.put(newTransaction.getId(), newUITransaction);
		transactions.add(0, newUITransaction);
		transactionsGridDataView.refreshAll();
		transactionsGrid.select(newUITransaction);
		changeVisibilityByFormValidity(false);
	}

	protected void onAddExternalTransactionButtonClicked(ClickEvent<?> event) {
		UIExternalTransaction newTransaction = new UIExternalTransaction();
		newTransaction.setId(UUID.randomUUID());
		newTransaction.setDate(LocalDate.now());
		newTransaction.setAccount(selectedAccount);
		newTransaction.setExternalId(UUID.randomUUID()
			.toString());

		addedTransactions.put(newTransaction.getId(), newTransaction);
		transactions.add(0, newTransaction);
		transactionsGrid.select(newTransaction);
		changeVisibilityByFormValidity(false);
	}

	@SuppressWarnings("unlikely-arg-type")
	protected void onDeleteTransactionButtonClicked(ClickEvent<?> event) {
		UUID currentSelectedTransactionId = currentSelectedTransaction.getTransaction()
			.getId();
		if (addedTransactions.containsKey(currentSelectedTransactionId)) {
			addedTransactions.remove(currentSelectedTransaction);
			transactions.remove(currentSelectedTransaction);
			transactionsGridDataView.refreshAll();
			transactionsGrid.deselectAll();
		} else {
			if (deletedTransactions.containsKey(currentSelectedTransactionId)) {
				deletedTransactions.remove(currentSelectedTransactionId);
			} else {
				deletedTransactions.put(currentSelectedTransactionId, currentSelectedTransaction.getTransaction());
			}

			selectTransaction(Optional.of(currentSelectedTransaction.getGridTransaction()));
			transactionsGridDataView.refreshItem(currentSelectedTransaction.getGridTransaction());
		}
	}

	protected void onDiscardListChangeButtonClicked(ClickEvent<?> event) {
		SimpleConfirmDialog
			.createSimpleConfirmDialog("Discard changes", "Do you really want to discard changes?", confirmEvent -> {
				activateEditMode(false);
				refreshTransactions(selectedAccount.getId());
			})
			.open();
	}

	protected void onSaveListChangeButtonClicked(ClickEvent<?> event) {
		SimpleConfirmDialog
			.createSimpleConfirmDialog("Save changes", "Do you really want to save changes?", confirmEvent -> {
				boolean error = false;

				Iterator<Entry<UUID, Transaction>> changedIter = changedTransactions.entrySet().iterator();
				while(changedIter.hasNext()) {
					var changedEntry = changedIter.next();
					try {
						transactionService.save(changedEntry.getValue());
						changedIter.remove();
					} catch (Exception e) {
						error = true;
						LOG.error("Could not save changes for transaction {}", changedEntry.getValue(), e);
					}
				}

				Iterator<Entry<UUID, Transaction>> deletedIter = deletedTransactions.entrySet().iterator();
				while(deletedIter.hasNext()) {
					var deleteEntry = deletedIter.next();
					try {
						transactionService.delete(deleteEntry.getValue());
						deletedIter.remove();
					} catch (Exception e) {
						error = true;
						LOG.error("Could not delete transaction {}", deleteEntry.getValue(), e);
					}
				}

				Iterator<Entry<UUID, Transaction>> addedIter = addedTransactions.entrySet().iterator();
				while(addedIter.hasNext()) {
					var addedEntry = addedIter.next();
					try {
						transactionService.save(addedEntry.getValue());
						addedIter.remove();
					} catch (Exception e) {
						error = true;
						LOG.error("Could not add transaction {}", addedEntry.getValue(), e);
					}
				}

				if (!error) {
					activateEditMode(false);
					refreshTransactions(selectedAccount.getId());
				} else {
					Notification notification = Notification.show("Could not save all changes");
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				}

			})
			.open();
	}

	protected void onTransactionSelected(SelectionEvent<Grid<GridTransaction>, GridTransaction> event) {
		selectTransaction(event.getFirstSelectedItem());
	}

	protected void selectTransaction(Optional<GridTransaction> selectedTransaction) {
		if (editModeActivated) {
			boolean deletedTransaction = selectedTransaction
				.map(transaction -> deletedTransactions.containsKey(transaction.getTransaction()
					.getId()))
				.orElse(false);

			currentSelectedTransaction = selectedTransaction.orElse(null);
			transactionForm.setValue(currentSelectedTransaction);
			transactionForm.setEnabled(selectedTransaction.isPresent() && !deletedTransaction);
			deleteTransactionMenuItem.setEnabled(selectedTransaction.isPresent());

			if (deletedTransaction) {
				deleteTransactionMenuItem.removeAll();
				deleteTransactionMenuItem.add(LumoIcon.UNDO.create());
				deleteTransactionMenuItem.add("Undo delete");
			} else {
				styleDefaultDeleteTransactionButton();
			}

			transactionForm.focusFirstElement();
		}
	}

	protected String getGridRowClassName(GridTransaction transaction) {
		if (editModeActivated) {
			UUID id = transaction.getTransaction()
				.getId();
			if (addedTransactions.containsKey(id)) {
				return "added-grid-item";
			} else if (deletedTransactions.containsKey(id)) {
				return "deleted-grid-item";
			} else if (changedTransactions.containsKey(id)) {
				return "changed-grid-item";
			}
		}
		return null;
	}

	protected void onTransactionFormValueChange(FormLayoutWithValueChangeEvent<? extends UITransaction> event) {
		UUID eventTransactionId = event.getValue()
			.getTransaction()
			.getId();
		if (event.isValueChanged() && !addedTransactions.containsKey(eventTransactionId)
				&& !deletedTransactions.containsKey(eventTransactionId)) {
			changedTransactions.put(eventTransactionId, event.getValue()
				.getTransaction());
		}

		changeVisibilityByFormValidity(event.isValueValid());
		transactionsGridDataView.refreshItem(event.getValue()
			.getGridTransaction());
		transactionForm.scrollIntoView();
	}

	protected void styleDefaultDeleteTransactionButton() {
		deleteTransactionMenuItem.removeAll();
		deleteTransactionMenuItem.add(VaadinIcon.MINUS.create());
		deleteTransactionMenuItem.add("Delete");
	}

	protected void activateEditMode(boolean activate) {
		changedTransactions.clear();
		deletedTransactions.clear();
		addedTransactions.clear();

		editModeActivated = activate;
		accountSelect.setEnabled(!activate);
		transactionForm.setVisible(activate);

		if (activate) {
			editListControls.removeAll();
			editListControls.add(editListActionMenuBar);
			transactionForm.scrollIntoView();		
			sumCell.getComponent().setVisible(false);
		} else {
			editListControls.removeAll();
			editListControls.add(editListButton);
			transactionsGrid.deselectAll();
			changeVisibilityByFormValidity(true);
			sumCell.getComponent().setVisible(true);
		}
	}

	protected void changeVisibilityByFormValidity(boolean isValid) {
		transactionsGrid.setEnabled(isValid);
		transactionsSearchField.setEnabled(isValid);
		addTransactionMenuItem.setEnabled(isValid);
		deleteTransactionMenuItem.setEnabled(isValid);
		saveListChangesMenuItem.setEnabled(isValid);
	}

	protected Icon getIconByTransaction(GridTransaction gridTransaction) {
		if (gridTransaction.getTransaction() instanceof InternalTransaction) {
			return VaadinIcon.COMPRESS_SQUARE.create();
		} else if (gridTransaction.getTransaction() instanceof ExternalTransaction) {
			return VaadinIcon.EXPAND_SQUARE.create();
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	protected Component createSumComponent(BigDecimal sum) {
		Span component = new Span(sum.toPlainString());
		if(sum.compareTo(BigDecimal.ZERO) >= 0) {
			component.addClassName("text-green-bold");
		}else {
			component.addClassName("text-red-bold");
		}
			
		return component;
	}

	@Override
	public String getPageTitle() {
		if(accountSelect.getValue() == null) {
			return "Account | Budget Book";
		}else {
			return accountSelect.getValue().getName() + " | Budget Book";
		}
		
	}

}
