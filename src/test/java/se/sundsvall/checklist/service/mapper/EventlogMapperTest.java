package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.TestObjectFactory.createEvent;
import static se.sundsvall.checklist.TestObjectFactory.createMetadata;
import static se.sundsvall.checklist.service.mapper.EventlogMapper.OWNER;
import static se.sundsvall.checklist.service.mapper.EventlogMapper.UNKNOWN;
import static se.sundsvall.checklist.service.mapper.EventlogMapper.USER_ID;

import generated.se.sundsvall.eventlog.EventType;
import java.util.List;
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
				assertThat(metadata.getKey()).isEqualTo(USER_ID);
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
				assertThat(metadata.getKey()).isEqualTo(USER_ID);
				assertThat(metadata.getValue()).isEqualTo(UNKNOWN);
			});
		});
	}

	/**
	 * Test scenario where we map a list of event objects.
	 */
	@Test
	void toEvents() {
		var event = createEvent();
		var events = List.of(event, event);

		var result = EventlogMapper.toEvents(events);

		assertThat(result).hasSize(2).allSatisfy(res -> {
			assertThat(res.getEventType()).isEqualTo(event.getType().toString());
			assertThat(res.getCreated()).isEqualTo(event.getCreated());
			assertThat(res.getHistoryReference()).isEqualTo(event.getHistoryReference());
			assertThat(res.getLogKey()).isEqualTo(event.getLogKey());
			assertThat(res.getMessage()).isEqualTo(event.getMessage());
			assertThat(res.getMunicipalityId()).isEqualTo(event.getMunicipalityId());
			assertThat(res.getOwner()).isEqualTo(event.getOwner());
			assertThat(res.getSourceType()).isEqualTo(event.getSourceType());
			assertThat(res.getMetadata()).hasSize(1);
		});
	}

	/**
	 * Test scenario where we map a singular event object.
	 */
	@Test
	void toEvent() {
		var event = createEvent();

		var result = EventlogMapper.toEvent(event);

		assertThat(result).satisfies(res -> {
			assertThat(res.getEventType()).isEqualTo(event.getType().toString());
			assertThat(res.getCreated()).isEqualTo(event.getCreated());
			assertThat(res.getHistoryReference()).isEqualTo(event.getHistoryReference());
			assertThat(res.getLogKey()).isEqualTo(event.getLogKey());
			assertThat(res.getMessage()).isEqualTo(event.getMessage());
			assertThat(res.getMunicipalityId()).isEqualTo(event.getMunicipalityId());
			assertThat(res.getOwner()).isEqualTo(event.getOwner());
			assertThat(res.getSourceType()).isEqualTo(event.getSourceType());
			assertThat(res.getMetadata()).hasSize(1);
		});
	}

	/**
	 * Test scenario where we map a list of metadata objects.
	 */
	@Test
	void toMetadata_1() {
		var metadata = createMetadata();
		var metadataList = List.of(metadata, metadata);

		var result = EventlogMapper.toMetadata(metadataList);

		assertThat(result).hasSize(2).allSatisfy(res -> {
			assertThat(res.getKey()).isEqualTo(metadata.getKey());
			assertThat(res.getValue()).isEqualTo(metadata.getValue());
		});
	}

	/**
	 * Test scenario where we map a singular metadata object.
	 */
	@Test
	void toMetadata_2() {
		var metadata = createMetadata();

		var result = EventlogMapper.toMetadata(metadata);

		assertThat(result).satisfies(res -> {
			assertThat(res.getKey()).isEqualTo(metadata.getKey());
			assertThat(res.getValue()).isEqualTo(metadata.getValue());
		});
	}

}
