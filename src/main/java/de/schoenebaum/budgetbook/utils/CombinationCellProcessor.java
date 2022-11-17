package de.schoenebaum.budgetbook.utils;

import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.util.CsvContext;

public class CombinationCellProcessor<T> extends CellProcessorAdaptor {

	protected final int[] alternativeColumnIndices;
	protected final Predicate<T> isEmptyValue;
	protected final BinaryOperator<T> mergeFunction;
	protected final Class<T> typeClass;

	public CombinationCellProcessor(Class<T> typeClass, Predicate<T> isEmptyValue, BinaryOperator<T> mergeFunction,
			int... alternativeColumnIndices) {
		super();
		Objects.requireNonNull(typeClass);
		Objects.requireNonNull(isEmptyValue);
		Objects.requireNonNull(mergeFunction);
		this.typeClass = typeClass;
		this.isEmptyValue = isEmptyValue;
		this.mergeFunction = mergeFunction;
		this.alternativeColumnIndices = alternativeColumnIndices;
	}

	public CombinationCellProcessor(Class<T> typeClass, Predicate<T> isEmptyValue, BinaryOperator<T> mergeFunction,
			final CellProcessor next, int... alternativeColumnIndices) {
		super(next);
		Objects.requireNonNull(typeClass);
		Objects.requireNonNull(isEmptyValue);
		Objects.requireNonNull(mergeFunction);
		this.typeClass = typeClass;
		this.isEmptyValue = isEmptyValue;
		this.mergeFunction = mergeFunction;
		this.alternativeColumnIndices = alternativeColumnIndices;
	}

	public CombinationCellProcessor(Class<T> typeClass, Predicate<T> isEmptyValue, BinaryOperator<T> mergeFunction,
			List<Integer> alternativeColumnIndices) {
		super();
		Objects.requireNonNull(typeClass);
		Objects.requireNonNull(isEmptyValue);
		Objects.requireNonNull(mergeFunction);
		this.typeClass = typeClass;
		this.isEmptyValue = isEmptyValue;
		this.mergeFunction = mergeFunction;
		this.alternativeColumnIndices = alternativeColumnIndices.stream()
			.mapToInt(i -> i)
			.toArray();
	}

	public CombinationCellProcessor(Class<T> typeClass, Predicate<T> isEmptyValue, BinaryOperator<T> mergeFunction,
			final CellProcessor next, List<Integer> alternativeColumnIndices) {
		super(next);
		Objects.requireNonNull(typeClass);
		Objects.requireNonNull(isEmptyValue);
		Objects.requireNonNull(mergeFunction);
		this.typeClass = typeClass;
		this.isEmptyValue = isEmptyValue;
		this.mergeFunction = mergeFunction;
		this.alternativeColumnIndices = alternativeColumnIndices.stream()
			.mapToInt(i -> i)
			.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T execute(Object value, CsvContext context) {
		if (value != null && !(this.typeClass.isAssignableFrom(value.getClass()))) {
			throw new SuperCsvCellProcessorException(typeClass, value, context, this);
		}

		T result = null;
		int nextAlternativeIndex = 0;
		boolean remainingAlternativeExists = true;
		while (remainingAlternativeExists) {
			if (!isEmptyValue.test((T) value)) {
				if (result == null) {
					result = (T) value;
				} else {
					result = mergeFunction.apply(result, (T) value);
				}
			}

			if (nextAlternativeIndex < alternativeColumnIndices.length) {
				value = context.getRowSource()
					.get(alternativeColumnIndices[nextAlternativeIndex++]);
			} else {
				remainingAlternativeExists = false;
			}
		}

		if (result != null) {
			return next.execute(result, context);
		} else {
			throw new SuperCsvException("Initial value and alternatives for row were empty");
		}
	}

}
