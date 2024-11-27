package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.PHASE;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.TASK;

import org.junit.jupiter.api.Test;

class ComponentTypeTest {

	@Test
	void enums() {
		assertThat(ComponentType.values()).containsExactlyInAnyOrder(PHASE, TASK);
	}

	@Test
	void enumValues() {
		assertThat(PHASE).hasToString("PHASE");
		assertThat(TASK).hasToString("TASK");
	}
}
