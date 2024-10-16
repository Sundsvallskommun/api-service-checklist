package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.EMPLOYEE;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER;

import java.time.Period;

import org.junit.jupiter.api.Test;

class RoleTypeTest {

	@Test
	void enums() {
		assertThat(RoleType.values()).containsExactlyInAnyOrder(EMPLOYEE, MANAGER);
	}

	@Test
	void enumValues() {
		assertThat(EMPLOYEE).hasToString("EMPLOYEE")
			.extracting(RoleType::getTimeToComplete, RoleType::getTimeToExpiration)
			.containsExactlyInAnyOrder(Period.ofMonths(6), Period.ofMonths(9));

		assertThat(MANAGER).hasToString("MANAGER")
			.extracting(RoleType::getTimeToComplete, RoleType::getTimeToExpiration)
			.containsExactlyInAnyOrder(Period.ofMonths(24), Period.ofMonths(27));
	}
}
