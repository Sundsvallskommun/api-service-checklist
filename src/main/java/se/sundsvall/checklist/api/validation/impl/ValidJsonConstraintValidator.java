package se.sundsvall.checklist.api.validation.impl;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.checklist.api.validation.ValidJson;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;

public class ValidJsonConstraintValidator implements ConstraintValidator<ValidJson, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidJsonConstraintValidator.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public boolean isValid(final String request, final ConstraintValidatorContext context) {
		try {
			if (nonNull(request) && startsWithAny(request.trim(), "[", "{") && endsWithAny(request.trim(), "]", "}")) {
				// Deserialize json string into checklist entity (and sub ordinates)
				final var structure = MAPPER.readValue(request, ChecklistEntity.class);

				// Validate that required attributes are present
				return isNotBlank(structure.getName()) &&
					isNotBlank(structure.getDisplayName());
			}

			return false;
		} catch (final Exception e) {
			LOGGER.error("Invalid json structure", e);
			return false;
		}
	}
}
