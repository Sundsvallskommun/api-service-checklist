package se.sundsvall.checklist.api.validation.impl;

import static java.util.Objects.isNull;

import java.util.Set;

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
		return isValid(value);
	}

	public boolean isValid(final Set<CommunicationChannel> value) {
		return !value.contains(CommunicationChannel.NO_COMMUNICATION) || value.size() == 1;
	}
}
