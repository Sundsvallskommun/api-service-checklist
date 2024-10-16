package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.ACTIVE;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.DEPRECATED;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.RETIRED;

import org.junit.jupiter.api.Test;

class LifeCycleTest {

	@Test
	void enums() {
		assertThat(LifeCycle.values()).containsExactlyInAnyOrder(ACTIVE, CREATED, DEPRECATED, RETIRED);
	}

	@Test
	void enumValues() {
		assertThat(ACTIVE).hasToString("ACTIVE");
		assertThat(CREATED).hasToString("CREATED");
		assertThat(DEPRECATED).hasToString("DEPRECATED");
		assertThat(RETIRED).hasToString("RETIRED");
	}
}
