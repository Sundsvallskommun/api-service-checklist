package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.NO_COMMUNICATION;

import org.junit.jupiter.api.Test;

class CommunicationChannelTest {

	@Test
	void enums() {
		assertThat(CommunicationChannel.values()).containsExactlyInAnyOrder(EMAIL, NO_COMMUNICATION);
	}

	@Test
	void enumValues() {
		assertThat(EMAIL).hasToString("EMAIL");
		assertThat(NO_COMMUNICATION).hasToString("NO_COMMUNICATION");
	}
}
