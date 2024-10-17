package se.sundsvall.checklist.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.service.util.TaskType.COMMON;
import static se.sundsvall.checklist.service.util.TaskType.CUSTOM;

import org.junit.jupiter.api.Test;

class TaskTypeTest {

	@Test
	void enums() {
		assertThat(TaskType.values()).containsExactlyInAnyOrder(COMMON, CUSTOM);
	}

	@Test
	void enumValues() {
		assertThat(COMMON).hasToString("COMMON");
		assertThat(CUSTOM).hasToString("CUSTOM");
	}
}
