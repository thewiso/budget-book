package de.schoenebaum.budgetbook.configuration;

import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

import de.schoenebaum.budgetbook.utils.PatternsLocalDateCellProcessor;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties("de.schoenebaum.budgetbook.import")
@Getter
@Setter
public class ImportProperties {

	public enum TransactionAttribute {
		date(true, p -> new PatternsLocalDateCellProcessor(p.dateFormatPatterns)),
		subject(true, true), amount(true, p -> {
			DecimalFormatSymbols s = new DecimalFormatSymbols();
			s.setDecimalSeparator(p.numberFormatDecimalSeparator);
			return new ParseBigDecimal(s);
		}), externalId, relatedPartyName(true, true), relatedPartyId;

		@Getter
		private final boolean mandatory;
		
		@Getter
		private final boolean combination;
		private final Function<ImportProperties, CellProcessor> cellProcessorFunktion;

		private TransactionAttribute() {
			this(false, p -> new Optional(), false);
		}

		private TransactionAttribute(boolean mandatory) {
			this(mandatory, p -> mandatory ? new NotNull() : new Optional(), false);
		}

		private TransactionAttribute(boolean mandatory, boolean combination) {
			this(mandatory, p -> mandatory ? new NotNull() : new Optional(), combination);
		}

		private TransactionAttribute(boolean mandatory,
				Function<ImportProperties, CellProcessor> cellProcessorFunktion) {
			this(mandatory, cellProcessorFunktion, false);
		}

		private TransactionAttribute(boolean mandatory, Function<ImportProperties, CellProcessor> cellProcessorFunktion,
				boolean combination) {
			this.mandatory = mandatory;
			this.cellProcessorFunktion = cellProcessorFunktion;
			this.combination = combination;
		}

	

		public CellProcessor getCellProcessor(ImportProperties importProperties) {
			return cellProcessorFunktion.apply(importProperties);
		}

	}

	private Map<TransactionAttribute, List<String>> transactionAttributeToCsvHeader;

	private List<String> dateFormatPatterns = Collections.singletonList("dd.MM.yyyy");
	private char numberFormatDecimalSeparator = ',';

}
