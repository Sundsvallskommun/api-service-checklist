package se.sundsvall.checklist.api.validation.impl;

import static java.util.Objects.isNull;

import java.util.Set;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.checklist.api.validation.ValidChannels;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;

public class ValidChannelsConstraintValidator implements ConstraintValidator<ValidChannels, Set<CommunicationChannel>> {

	private boolean nullable;

	@Override
	public void initialize(final ValidChannels constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(final Set<CommunicationChannel> value, final ConstraintValidatorContext context) {
		if (isNull(value) && nullable) {
			return true;
		}

		if (CollectionUtils.isEmpty(value)) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("must not be empty").addConstraintViolation();
			return false;
		}

		if (value.contains(CommunicationChannel.NO_COMMUNICATION) && value.size() > 1) { // NOSONAR
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Can not have NO_COMMUNICATION in combination with other values").addConstraintViolation();
			return false;
		}

		return true;
	}
}
