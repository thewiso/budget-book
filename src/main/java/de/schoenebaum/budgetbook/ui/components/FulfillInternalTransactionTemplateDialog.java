package de.schoenebaum.budgetbook.ui.components;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;

import de.schoenebaum.budgetbook.db.entities.InternalTransactionTemplate;
import de.schoenebaum.budgetbook.services.InternalTransactionTemplateService;
import de.schoenebaum.budgetbook.services.TransactionService;

public class FulfillInternalTransactionTemplateDialog extends Dialog {

	private static final long serialVersionUID = -5639129587804009537L;
	private static final Logger LOG = LoggerFactory.getLogger(FulfillInternalTransactionTemplateDialog.class);

	private final InternalTransactionTemplateService internalTransactionTemplateService;
	private final TransactionService transactionService;
	private final List<InternalTransactionTemplate> internalTransactionTemplates;

	private TextField templateSearchField;

	private Grid<InternalTransactionTemplate> templatesGrid;
	private GridListDataView<InternalTransactionTemplate> templatesGridDataView;
	private GridMultiSelectionModel<InternalTransactionTemplate> templatesGridSelectionModel;

	public FulfillInternalTransactionTemplateDialog(
			InternalTransactionTemplateService internalTransactionTemplateService,
			TransactionService transactionService) {
		this.internalTransactionTemplateService = internalTransactionTemplateService;
		this.transactionService = transactionService;

		internalTransactionTemplates = new LinkedList<>();

		// TOP GRID BAR
		templateSearchField = new TextField();
		templateSearchField.setPlaceholder("Search");
		templateSearchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		templateSearchField.setValueChangeMode(ValueChangeMode.EAGER);
		templateSearchField.addValueChangeListener(e -> templatesGridDataView.refreshAll());

		HorizontalLayout topGridBar = new HorizontalLayout();
		topGridBar.setJustifyContentMode(JustifyContentMode.END);
		topGridBar.add(templateSearchField);
		topGridBar.setWidthFull();
		topGridBar.setAlignItems(Alignment.END);

		// GRID
		templatesGrid = new Grid<>(InternalTransactionTemplate.class, false);
		Column<InternalTransactionTemplate> sourceAccountColumn = templatesGrid.addColumn(t -> t.getSourceAccount()
			.getName())
			.setHeader("Source")
			.setSortable(true);
		templatesGrid.addColumn(t -> t.getTargetAccount()
			.getName())
			.setHeader("Target")
			.setSortable(true);
		templatesGrid.addColumn(InternalTransactionTemplate::getSubject)
			.setHeader("Subject")
			.setSortable(true);
		templatesGrid.addColumn(InternalTransactionTemplate::getAmount)
			.setHeader("Amount")
			.setSortable(true);
		templatesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		templatesGridSelectionModel = (GridMultiSelectionModel<InternalTransactionTemplate>) templatesGrid
			.setSelectionMode(SelectionMode.MULTI);
		templatesGridDataView = templatesGrid.setItems(internalTransactionTemplates);
		templatesGridDataView.addFilter(this::filterTemplates);
		templatesGrid.getDataCommunicator()
			.getKeyMapper()
			.setIdentifierGetter(InternalTransactionTemplate::getId);

		GridSortOrder<InternalTransactionTemplate> order = new GridSortOrder<>(sourceAccountColumn,
				SortDirection.ASCENDING);
		templatesGrid.sort(Arrays.asList(order));

		Button fulfillButton = new Button("Fulfill templates", this::onFulfillButtonClick);
		fulfillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
		getFooter().add(fulfillButton);

		Button cancelButton = new Button("Cancel", (e) -> this.close());
		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		getFooter().add(cancelButton);

		this.setSizeFull();
		this.add(new H2("Internal Transaction Templates"), topGridBar, templatesGrid);
	}

	@Override
	public void open() {
		loadInternalTransactionTemplates();
		templatesGridSelectionModel.selectAll();
		super.open();
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

	protected void onFulfillButtonClick(ClickEvent<?> event) {
		try {
			transactionService.fulfillTemplates(templatesGridSelectionModel.getSelectedItems());
			this.close();
		} catch (Exception e) {
			LOG.error("Could not fulfill InternalTransactionTemplates", e);
			Notification notification = new Notification("Could not persist changes. Please try again.");
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.open();
		}

	}

}
