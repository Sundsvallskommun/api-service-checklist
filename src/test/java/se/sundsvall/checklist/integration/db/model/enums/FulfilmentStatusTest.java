package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus.EMPTY;
import static se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus.FALSE;
import static se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus.TRUE;

import org.junit.jupiter.api.Test;

class FulfilmentStatusTest {

	@Test
	void enums() {
		assertThat(FulfilmentStatus.values()).containsExactlyInAnyOrder(EMPTY, FALSE, TRUE);
	}

	@Test
	void enumValues() {
		assertThat(EMPTY).hasToString("EMPTY");
		assertThat(FALSE).hasToString("FALSE");
		assertThat(TRUE).hasToString("TRUE");
	}
}
