package de.schoenebaum.budgetbook.ui.views;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.schoenebaum.budgetbook.services.TransactionService;
import de.schoenebaum.budgetbook.ui.components.chart.Chart;
import de.schoenebaum.budgetbook.ui.components.chart.configuration.ChartConfiguration;
import de.schoenebaum.budgetbook.ui.components.chart.configuration.ChartData;
import de.schoenebaum.budgetbook.ui.components.chart.configuration.ChartTwoDimensionalDataPoint;
import de.schoenebaum.budgetbook.ui.components.chart.configuration.ChartDataset;
import de.schoenebaum.budgetbook.ui.components.chart.configuration.ChartOptions;
import de.schoenebaum.budgetbook.ui.components.chart.configuration.ChartType;

@PermitAll
@Route(value = "analytics", layout = MainLayout.class)
@PageTitle("Analytics | Budget Book")
public class AnalyticsOverview extends VerticalLayout {

	private static final long serialVersionUID = 1452820930689839041L;
	private final TransactionService transactionService;

	private Chart balanceDiagram;
	private DatePicker balanceStartDatePicker;
	private DatePicker balanceEndDatePicker;

	private Chart expenseDiagram;

	@Autowired
	public AnalyticsOverview(MainLayout mainLayout, TransactionService transactionService) {
		this.transactionService = transactionService;

		// BALANCE DIAGRAM
		balanceStartDatePicker = new DatePicker("From", LocalDate.now()
			.minusMonths(1));
		balanceStartDatePicker.addValueChangeListener(e -> {
			balanceEndDatePicker.setMin(e.getValue());
			this.updateBalanceDiagram();
		});
		balanceStartDatePicker.setRequired(true);
		balanceEndDatePicker = new DatePicker("To", LocalDate.now());
		balanceEndDatePicker.addValueChangeListener(e -> {
			balanceStartDatePicker.setMax(e.getValue());
			this.updateBalanceDiagram();
		});
		balanceEndDatePicker.setMax(LocalDate.now());
		balanceEndDatePicker.setRequired(true);

		HorizontalLayout balanceDiagramDatePickerContainer = new HorizontalLayout(balanceStartDatePicker,
				balanceEndDatePicker);

		balanceDiagram = new Chart();
//		balanceDiagram.setHeight("20%");

		this.add(new H2("Account Balances"), balanceDiagramDatePickerContainer, balanceDiagram);
		updateBalanceDiagram();

		// EXPENSE DIAGRAMM
		expenseDiagram = new Chart();
//		expenseDiagram.setHeight("400px");
		this.add(new H2("Highest Expenses"), expenseDiagram);
		updateExpenseDiagram();

		mainLayout.focusTabItem(getClass());
	}

	protected void updateBalanceDiagram() {
		LocalDate startDate = balanceStartDatePicker.getValue();
		LocalDate endDate = balanceEndDatePicker.getValue();
		if (startDate == null || endDate == null) {
			return;
		}

		Map<String, Map<LocalDate, BigDecimal>> balances = transactionService.getDailyBalances(startDate, endDate);
		int colorIndex = 0;
		List<ChartDataset> datasets = new LinkedList<>();
		for (Map.Entry<String, Map<LocalDate, BigDecimal>> accountEntry : balances.entrySet()) {
			ChartDataset.ChartDatasetBuilder builder = ChartDataset.builder();
			builder.label(accountEntry.getKey())
				.borderColorEntry(Chart.COLORS[colorIndex])
				.backgroundColorEntry(Chart.COLORS_50_PERCENT_ALPHA[colorIndex]);

			for (Map.Entry<LocalDate, BigDecimal> entry : accountEntry.getValue()
				.entrySet()) {
				builder.dataPoint(ChartTwoDimensionalDataPoint.builder()
					.x(entry.getKey())
					.y(entry.getValue())
					.build());
			}
			datasets.add(builder.build());

			colorIndex++;
		}

		balanceDiagram.setConfiguration(ChartConfiguration.builder()
			.type(ChartType.Line)
			.data(ChartData.builder()
				.datasets(datasets)
				.build())
			.options(ChartOptions.builder()
				.maintainAspectRatio(false)
				.build())
			.build());
	}

	protected void updateExpenseDiagram() {
		Map<String, BigDecimal> expensesByAccountNames = transactionService.getExpensesByAccountNames();
		List<Map.Entry<String, BigDecimal>> sortedExpenses = expensesByAccountNames.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
			.toList();

		int colorIndex = 0;
		ChartDataset.ChartDatasetBuilder datasetBuilder = ChartDataset.builder();
		ChartData.ChartDataBuilder dataBuilder = ChartData.builder();

		for (Map.Entry<String, BigDecimal> entry : sortedExpenses) {
			dataBuilder.label(entry.getKey());
			datasetBuilder.backgroundColorEntry(Chart.COLORS[colorIndex])
				.dataPoint(entry.getValue());

			colorIndex++;
		}

		expenseDiagram.setConfiguration(ChartConfiguration.builder()
			.type(ChartType.Pie)
			.data(dataBuilder.dataset(datasetBuilder.build())
				.build())
			.options(ChartOptions.builder()
				.maintainAspectRatio(false)
				.build())
			.build());
	}
}
