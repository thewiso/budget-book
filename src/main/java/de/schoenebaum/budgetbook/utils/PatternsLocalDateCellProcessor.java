package de.schoenebaum.budgetbook.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Objects;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

public class PatternsLocalDateCellProcessor extends CellProcessorAdaptor {

	protected final DateTimeFormatter[] formatters;
	protected final Collection<String> pattern;

	public PatternsLocalDateCellProcessor(Collection<String> pattern) {
		Objects.requireNonNull(pattern);
		this.pattern = pattern;
		this.formatters = pattern.stream()
			.map(DateTimeFormatter::ofPattern)
			.toArray(DateTimeFormatter[]::new);
	}

	public PatternsLocalDateCellProcessor(CellProcessor next, Collection<String> pattern) {
		super(next);
		this.pattern = pattern;
		Objects.requireNonNull(pattern);
		this.formatters = pattern.stream()
			.map(DateTimeFormatter::ofPattern)
			.toArray(DateTimeFormatter[]::new);
	}

	@Override
	public <T> T execute(Object value, CsvContext context) {
		validateInputNotNull(value, context);
		if( !(value instanceof String) ) {
			throw new SuperCsvCellProcessorException(String.class, value, context, this);
		}
		
		for(DateTimeFormatter formatter: formatters) {
			try {
				return next.execute(LocalDate.parse((String) value, formatter), context);
			}catch (DateTimeParseException e) {
				//ignore and try next...
			}
		}
		
		throw new DateTimeParseException(
				"Could not format String '" + value + "' to LocalDate with formatters: " + String.join(",", pattern),
				(CharSequence) value, 0);
	}

}
