package se.sundsvall.checklist.api.validation.impl;

import static java.util.Objects.isNull;

import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.checklist.api.validation.ValidPeriod;

/**
 * Validator for {@link ValidPeriod} annotation.
 * Tries to parse the value to a {@link Period}.
 * If it fails, the value is not valid.
 */
public class ValidPeriodConstraintValidator implements ConstraintValidator<ValidPeriod, String> {

	private boolean nullable;

	@Override
	public void initialize(final ValidPeriod constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (isNull(value) && nullable) {
			return true;
		}
		return isValidFormat(value);
	}

	public boolean isValidFormat(final String value) {
		try {
			Period.parse(value);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}
}
