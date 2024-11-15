package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.EmploymentPosition.EMPLOYEE;
import static se.sundsvall.checklist.integration.db.model.enums.EmploymentPosition.MANAGER;

import java.time.Period;

import org.junit.jupiter.api.Test;

class EmploymentTypeTest {

	@Test
	void enums() {
		assertThat(EmploymentPosition.values()).containsExactlyInAnyOrder(EMPLOYEE, MANAGER);
	}

	@Test
	void enumValues() {
		assertThat(EMPLOYEE).hasToString("EMPLOYEE")
			.extracting(EmploymentPosition::getTimeToComplete, EmploymentPosition::getTimeToExpiration)
			.containsExactlyInAnyOrder(Period.ofMonths(6), Period.ofMonths(9));

		assertThat(MANAGER).hasToString("MANAGER")
			.extracting(EmploymentPosition::getTimeToComplete, EmploymentPosition::getTimeToExpiration)
			.containsExactlyInAnyOrder(Period.ofMonths(24), Period.ofMonths(27));
	}
}
