package se.sundsvall.checklist.api.validation.impl;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.checklist.api.validation.ValidJson;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;

public class ValidJsonConstraintValidator implements ConstraintValidator<ValidJson, String> {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public boolean isValid(final String request, final ConstraintValidatorContext context) {
		try {
			if (nonNull(request) && startsWithAny(request, "[", "{") && endsWithAny(request, "]", "}")) {
				// Deserialize json string into checklist entity (and sub ordinates)
				final var structure = MAPPER.readValue(request, ChecklistEntity.class);

				// Validate that required attributes are present
				return Objects.nonNull(structure.getRoleType()) &&
					StringUtils.isNotBlank(structure.getName()) &&
					StringUtils.isNotBlank(structure.getDisplayName());
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
