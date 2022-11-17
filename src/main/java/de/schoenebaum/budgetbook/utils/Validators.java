package de.schoenebaum.budgetbook.utils;

import java.math.BigDecimal;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

public class Validators {

	public static final Validator<BigDecimal> BIG_DECIMAL_NOT_ZERO = new Validator<BigDecimal>() {

		private static final long serialVersionUID = 4893490014770886278L;

		@Override
		public ValidationResult apply(BigDecimal value, ValueContext context) {
			if (value == null) {
				return ValidationResult.error("Must not be empty");
			}
			if (BigDecimal.ZERO.equals(value)) {
				return ValidationResult.error("Must not be zero");
			}
			return ValidationResult.ok();
		}
	};

	public static final Validator<BigDecimal> BIG_DECIMAL_GREATER_THAN_ZERO = new Validator<BigDecimal>() {

		private static final long serialVersionUID = -2401195854133380315L;

		@Override
		public ValidationResult apply(BigDecimal value, ValueContext context) {
			if (value == null) {
				return ValidationResult.error("Must not be empty");
			}
			if (value.compareTo(BigDecimal.ZERO) < 1) {
				return ValidationResult.error("Must be greater than zero");
			}
			return ValidationResult.ok();
		}
	};

}
