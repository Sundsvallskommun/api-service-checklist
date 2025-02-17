package se.sundsvall.checklist.api.validation.impl;

import static java.util.Objects.isNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import se.sundsvall.checklist.api.validation.RoleTypeSubset;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

public class RoleTypeSubsetConstraintValidator implements ConstraintValidator<RoleTypeSubset, RoleType> {

	private List<RoleType> subset;
	private boolean nullable;

	@Override
	public void initialize(RoleTypeSubset constraintAnnotation) {
		this.subset = Objects.isNull(constraintAnnotation.oneOf()) ? Collections.emptyList() : List.of(constraintAnnotation.oneOf());
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(RoleType value, ConstraintValidatorContext context) {
		if (isNull(value) && nullable) {
			return true;
		}

		return value != null && subset.contains(value);
	}

}
