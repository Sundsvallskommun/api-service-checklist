package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.ERROR;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.NOT_SENT;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.SENT;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.WILL_NOT_SEND;

import org.junit.jupiter.api.Test;

class CorrespondenceStatusTest {

	@Test
	void enums() {
		assertThat(CorrespondenceStatus.values()).containsExactlyInAnyOrder(ERROR, NOT_SENT, SENT, WILL_NOT_SEND);
	}

	@Test
	void enumValues() {
		assertThat(ERROR).hasToString("ERROR");
		assertThat(NOT_SENT).hasToString("NOT_SENT");
		assertThat(SENT).hasToString("SENT");
		assertThat(WILL_NOT_SEND).hasToString("WILL_NOT_SEND");
	}
}
