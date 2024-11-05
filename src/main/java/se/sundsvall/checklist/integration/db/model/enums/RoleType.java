package se.sundsvall.checklist.integration.db.model.enums;

import java.time.Period;
import java.time.temporal.TemporalAmount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Determines if a checklisttemplate is for a new employee or a new manager
 * Using a string in ISO-8601 duration format to represent the time to complete the checklist.
 * This is used to determine the checklist due date.
 */
@Getter
@Schema(enumAsRef = true)
public enum RoleType {
	EMPLOYEE(Period.ofMonths(6), Period.ofMonths(9)),
	MANAGER(Period.ofMonths(24), Period.ofMonths(27));

	private final TemporalAmount timeToComplete;
	private final TemporalAmount timeToExpiration;

	RoleType(final TemporalAmount timeToComplete, final TemporalAmount timeToExpiration) {
		this.timeToComplete = timeToComplete;
		this.timeToExpiration = timeToExpiration;
	}
}
