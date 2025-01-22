package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.service.mapper.EventlogMapper.OWNER;
import static se.sundsvall.checklist.service.mapper.EventlogMapper.UNKNOWN;
import static se.sundsvall.checklist.service.mapper.EventlogMapper.USER;

import generated.se.sundsvall.eventlog.EventType;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;

class EventlogMapperTest {

	/**
	 * Test scenario where user is provided.
	 */
	@Test
	void toEvent_1() {
		var eventType = EventType.CREATE;
		var message = "Checklist created";
		var checklistEntity = new ChecklistEntity();
		var user = "username";

		var result = EventlogMapper.toEvent(eventType, message, checklistEntity, user);

		assertThat(result).satisfies(event -> {
			assertThat(event.getCreated()).isNotNull();
			assertThat(event.getHistoryReference()).isNull();
			assertThat(event.getMessage()).isEqualTo(message);
			assertThat(event.getOwner()).isEqualTo(OWNER);
			assertThat(event.getSourceType()).isEqualTo(checklistEntity.getClass().getSimpleName());
			assertThat(event.getType()).isEqualTo(eventType);
			assertThat(event.getMunicipalityId()).isEqualTo(checklistEntity.getMunicipalityId());
			assertThat(event.getLogKey()).isEqualTo(checklistEntity.getId());
			assertThat(event.getMetadata()).hasSize(1);
			assertThat(event.getMetadata().getFirst()).satisfies(metadata -> {
				assertThat(metadata.getKey()).isEqualTo(USER);
				assertThat(metadata.getValue()).isEqualTo("username");
			});
		});
	}

	/**
	 * Test scenario where user is not provided.
	 */
	@Test
	void toEvent_2() {
		var eventType = EventType.CREATE;
		var message = "Checklist created";
		var checklistEntity = new ChecklistEntity();

		var result = EventlogMapper.toEvent(eventType, message, checklistEntity, null);

		assertThat(result).satisfies(event -> {
			assertThat(event.getCreated()).isNotNull();
			assertThat(event.getHistoryReference()).isNull();
			assertThat(event.getMessage()).isEqualTo(message);
			assertThat(event.getOwner()).isEqualTo(OWNER);
			assertThat(event.getSourceType()).isEqualTo(checklistEntity.getClass().getSimpleName());
			assertThat(event.getType()).isEqualTo(eventType);
			assertThat(event.getMunicipalityId()).isEqualTo(checklistEntity.getMunicipalityId());
			assertThat(event.getLogKey()).isEqualTo(checklistEntity.getId());
			assertThat(event.getMetadata()).hasSize(1);
			assertThat(event.getMetadata().getFirst()).satisfies(metadata -> {
				assertThat(metadata.getKey()).isEqualTo(USER);
				assertThat(metadata.getValue()).isEqualTo(UNKNOWN);
			});
		});
	}
}
