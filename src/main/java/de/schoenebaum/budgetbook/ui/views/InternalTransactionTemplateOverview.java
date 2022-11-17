package de.schoenebaum.budgetbook.ui.views;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.security.PermitAll;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorCancelEvent;
import com.vaadin.flow.component.grid.editor.EditorSaveEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.entities.InternalTransactionTemplate;
import de.schoenebaum.budgetbook.services.AccountService;
import de.schoenebaum.budgetbook.services.InternalTransactionTemplateService;
import de.schoenebaum.budgetbook.services.TransactionService;
import de.schoenebaum.budgetbook.ui.components.AccountSelect;
import de.schoenebaum.budgetbook.ui.components.FulfillInternalTransactionTemplateDialog;
import de.schoenebaum.budgetbook.ui.components.SimpleConfirmDialog;
import de.schoenebaum.budgetbook.ui.components.ValidationMessage;
import de.schoenebaum.budgetbook.utils.Validators;
import org.springframework.beans.factory.annotation.Autowired;

@PermitAll
@Route(value = "internal-transaction-templates", layout = MainLayout.class)
@PageTitle("Internal Transaction Templates | Budget Book")
public class InternalTransactionTemplateOverview extends VerticalLayout {

	private static final long serialVersionUID = -3369649454230352141L;
	private static final Logger LOG = LoggerFactory.getLogger(InternalTransactionTemplateOverview.class);

	private final InternalTransactionTemplateService internalTransactionTemplateService;
	private final List<InternalTransactionTemplate> internalTransactionTemplates;

	private TextField templateSearchField;

	private Grid<InternalTransactionTemplate> templatesGrid;
	private GridListDataView<InternalTransactionTemplate> templatesGridDataView;

	private Editor<InternalTransactionTemplate> templateEditor;
	private boolean newTemplateInEditing = false;
	private ValidationMessage sourceAccountMessage;
	private ValidationMessage targetAccountMessage;
	private ValidationMessage subjectMessage;
	private ValidationMessage amountMessage;
	private AccountSelect sourceAccountSelect;
	private AccountSelect targetAccountSelect;

	@Autowired
	public InternalTransactionTemplateOverview(MainLayout mainLayout,
			InternalTransactionTemplateService internalTransactionTemplateService, AccountService accountService,
			TransactionService transactionService) {
		this.internalTransactionTemplateService = internalTransactionTemplateService;
		internalTransactionTemplates = new LinkedList<>();

		// FULFILL TEMPLATES
		FulfillInternalTransactionTemplateDialog fulfillDialog = new FulfillInternalTransactionTemplateDialog(
				internalTransactionTemplateService, transactionService);

		Button fulfillTemplatesButton = new Button("Fulfill templates", VaadinIcon.ARROW_FORWARD.create());
		fulfillTemplatesButton.addClickListener(e -> fulfillDialog.open());

		// TOP GRID BAR
		templateSearchField = new TextField();
		templateSearchField.setPlaceholder("Search");
		templateSearchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		templateSearchField.setValueChangeMode(ValueChangeMode.EAGER);
		templateSearchField.addValueChangeListener(e -> templatesGridDataView.refreshAll());

		Button addTemplateButton = new Button("Add", VaadinIcon.PLUS.create());
		addTemplateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		addTemplateButton.addClickListener(event -> {
			if (templateEditor.isOpen()) {
				templateEditor.cancel();
			}

			newTemplateInEditing = true;
			InternalTransactionTemplate newTemplate = new InternalTransactionTemplate();
			newTemplate.setId(UUID.randomUUID());
			internalTransactionTemplates.add(0, newTemplate);
			templatesGridDataView.refreshAll();
			templateEditor.editItem(newTemplate);
		});

		HorizontalLayout topGridBar = new HorizontalLayout();
		topGridBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
		topGridBar.add(addTemplateButton, templateSearchField);
		topGridBar.setWidthFull();
		topGridBar.setAlignItems(Alignment.END);
		topGridBar.addClassName("with-top-padding-l");

		// GRID
		templatesGrid = new Grid<>(InternalTransactionTemplate.class, false);
		Column<InternalTransactionTemplate> sourceAccountColumn = templatesGrid
			.addColumn(t -> getNullableAccountName(t.getSourceAccount()))
			.setHeader("Source")
			.setSortable(true);
		Column<InternalTransactionTemplate> targetAccountColumn = templatesGrid
			.addColumn(t -> getNullableAccountName(t.getTargetAccount()))
			.setHeader("Target")
			.setSortable(true);
		Column<InternalTransactionTemplate> subjectColumn = templatesGrid
			.addColumn(InternalTransactionTemplate::getSubject)
			.setHeader("Subject")
			.setSortable(true);
		Column<InternalTransactionTemplate> amountColumn = templatesGrid
			.addColumn(InternalTransactionTemplate::getAmount)
			.setHeader("Amount")
			.setSortable(true);
		Column<InternalTransactionTemplate> editColumn = templatesGrid
			.addComponentColumn(this::createChangeTemplateButton);
		Column<InternalTransactionTemplate> deleteColumn = templatesGrid
			.addComponentColumn(this::createDeleteTemplateButton);
		templatesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		templatesGrid.setSelectionMode(SelectionMode.NONE);
		templatesGridDataView = templatesGrid.setItems(internalTransactionTemplates);
		templatesGridDataView.addFilter(this::filterTemplates);
		templatesGrid.getDataCommunicator()
			.getKeyMapper()
			.setIdentifierGetter(InternalTransactionTemplate::getId);
		templateEditor = templatesGrid.getEditor();

		GridSortOrder<InternalTransactionTemplate> order = new GridSortOrder<>(sourceAccountColumn,
				SortDirection.ASCENDING);
		templatesGrid.sort(Arrays.asList(order));

		loadInternalTransactionTemplates();

		// GRID EDITOR
		Binder<InternalTransactionTemplate> templateBinder = new Binder<>(InternalTransactionTemplate.class);
		templateEditor.setBinder(templateBinder);
		templateEditor.setBuffered(true);

		sourceAccountMessage = new ValidationMessage();
		targetAccountMessage = new ValidationMessage();
		subjectMessage = new ValidationMessage();
		amountMessage = new ValidationMessage();
		VerticalLayout validationLayout = new VerticalLayout();
		validationLayout.add(sourceAccountMessage, targetAccountMessage, subjectMessage, amountMessage);

		sourceAccountSelect = new AccountSelect(accountService);
		sourceAccountSelect.setEmptySelectionAllowed(true);
		sourceAccountSelect.setWidthFull();
		sourceAccountSelect.setLabel("");
		templateBinder.forField(sourceAccountSelect)
			.asRequired("Source account must not be empty")
			.withStatusLabel(sourceAccountMessage)
			.bind(InternalTransactionTemplate::getSourceAccount, InternalTransactionTemplate::setSourceAccount);
		sourceAccountColumn.setEditorComponent(sourceAccountSelect);

		targetAccountSelect = new AccountSelect(accountService);
		targetAccountSelect.setEmptySelectionAllowed(true);
		targetAccountSelect.setWidthFull();
		targetAccountSelect.setLabel("");
		templateBinder.forField(targetAccountSelect)
			.asRequired("Target account must not be empty")
			.withStatusLabel(targetAccountMessage)
			.withValidator(this::validateInternalTransactionTemplateAccount)
			.bind(InternalTransactionTemplate::getTargetAccount, InternalTransactionTemplate::setTargetAccount);
		targetAccountColumn.setEditorComponent(targetAccountSelect);

		TextField subjectField = new TextField();
		subjectField.setWidthFull();
		templateBinder.forField(subjectField)
			.asRequired("Subject must not be empty")
			.withStatusLabel(subjectMessage)
			.bind(InternalTransactionTemplate::getSubject, InternalTransactionTemplate::setSubject);
		subjectColumn.setEditorComponent(subjectField);

		BigDecimalField amountField = new BigDecimalField();
		amountField.setWidthFull();
		templateBinder.forField(amountField)
			.asRequired("Amount must not be empty")
			.withValidator(Validators.BIG_DECIMAL_GREATER_THAN_ZERO)
			.withStatusLabel(amountMessage)
			.bind(InternalTransactionTemplate::getAmount, InternalTransactionTemplate::setAmount);
		amountColumn.setEditorComponent(amountField);

		Button saveButton = new Button("Save", e -> templateEditor.save());
		Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> templateEditor.cancel());
		cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
		HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
		actions.setPadding(false);
		editColumn.setEditorComponent(actions);
		deleteColumn.setEditorComponent(new Div());

		templateEditor.addCancelListener(this::onEditorCancel);
		templateEditor.addSaveListener(this::onEditorSafe);

		this.add(new H2("Internal Transaction Templates"), fulfillTemplatesButton, topGridBar, templatesGrid,
				validationLayout, fulfillDialog);
		mainLayout.focusTabItem(getClass());
	}

	protected boolean filterTemplates(InternalTransactionTemplate template) {
		String searchTerm = templateSearchField.getValue()
			.trim();

		if (searchTerm.isEmpty()) {
			return true;
		}
		return StringUtils.containsIgnoreCase(template.getSubject(), searchTerm);
	}

	protected void loadInternalTransactionTemplates() {
		internalTransactionTemplates.clear();
		internalTransactionTemplates.addAll(internalTransactionTemplateService.findAll());
		templatesGridDataView.refreshAll();
	}

	protected Button createChangeTemplateButton(InternalTransactionTemplate template) {
		Button retVal = new Button("Edit", VaadinIcon.EDIT.create());
		retVal.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
		retVal.addClickListener(event -> {
			if (templateEditor.isOpen()) {
				templateEditor.cancel();
			}
			templateEditor.editItem(template);
		});
		return retVal;
	}

	protected Button createDeleteTemplateButton(InternalTransactionTemplate template) {
		Button retVal = new Button("Delete", VaadinIcon.TRASH.create());
		retVal.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
		retVal.addClickListener(event -> {
			SimpleConfirmDialog.createSimpleConfirmDialog("Delete Internal Transaction Template",
					"Do you really want to delete the template?", confirmEvent -> {
						try {
							internalTransactionTemplateService.delete(template);
							internalTransactionTemplates.remove(template);
							templatesGridDataView.refreshAll();
						} catch (Exception e) {
							LOG.error("Could not delete InternalTransactionTemplate {}", template, e);
							Notification notification = Notification.show("Could not save all changes");
							notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
						}
					})
				.open();
		});
		return retVal;
	}

	protected void onEditorSafe(EditorSaveEvent<InternalTransactionTemplate> event) {
		newTemplateInEditing = false;
		try {
			internalTransactionTemplateService.save(event.getItem());
		} catch (Exception e) {
			LOG.error("Could not persist InternalTransactionTemplate {}", event.getItem(), e);
			Notification notification = new Notification("Could not persist changes. Please try again.");
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.open();
		}
	}

	protected void onEditorCancel(EditorCancelEvent<InternalTransactionTemplate> event) {
		sourceAccountMessage.setText("");
		targetAccountMessage.setText("");
		subjectMessage.setText("");
		amountMessage.setText("");

		if (event.getItem() != null && newTemplateInEditing && internalTransactionTemplates.contains(event.getItem())) {
			internalTransactionTemplates.remove(event.getItem());
			templatesGridDataView.refreshAll();
			newTemplateInEditing = false;
		}
	}

	protected String getNullableAccountName(Account account) {
		if (account == null) {
			return null;
		}
		return account.getName();
	}

	protected ValidationResult validateInternalTransactionTemplateAccount(Account value, ValueContext context) {
		// a bit hacky, but "the cross field validation with BeanValidator is not yet
		// supported" https://github.com/vaadin/framework/issues/8385

		Account sourceAccount = sourceAccountSelect.getValue();
		Account targetAccount = targetAccountSelect.getValue();

		if (sourceAccount != null && targetAccount != null && sourceAccount.equals(targetAccount)) {
			return ValidationResult.error("Source and target account must not be the same");
		}
		return ValidationResult.ok();
	}

}
