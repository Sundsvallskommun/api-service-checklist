package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.NEW_EMPLOYEE;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER_FOR_NEW_EMPLOYEE;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.NEW_MANAGER;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER_FOR_NEW_MANAGER;

import org.junit.jupiter.api.Test;

class RoleTypeTest {

	@Test
	void enums() {
		assertThat(RoleType.values()).containsExactlyInAnyOrder(NEW_EMPLOYEE, MANAGER_FOR_NEW_EMPLOYEE, NEW_MANAGER, MANAGER_FOR_NEW_MANAGER);
	}
}
