package se.sundsvall.checklist.integration.db.model.enums;

import java.time.Period;
import java.time.temporal.TemporalAmount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Enum with possible employment positions
 * Using a string in ISO-8601 duration format to represent the time to complete the checklist.
 * This is used to determine the checklist due date.
 */
@Getter
@Schema(enumAsRef = true)
public enum EmploymentPosition {
	EMPLOYEE(Period.ofMonths(6), Period.ofMonths(9)),
	MANAGER(Period.ofMonths(24), Period.ofMonths(27));

	private final TemporalAmount timeToComplete;
	private final TemporalAmount timeToExpiration;

	EmploymentPosition(final TemporalAmount timeToComplete, final TemporalAmount timeToExpiration) {
		this.timeToComplete = timeToComplete;
		this.timeToExpiration = timeToExpiration;
	}
}
