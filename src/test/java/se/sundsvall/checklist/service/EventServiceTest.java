package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.service.mapper.EventlogMapper.toEvent;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.EventType;
import generated.se.sundsvall.eventlog.PageEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.eventlog.EventlogIntegration;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

	@Mock
	private EventlogIntegration eventlogIntegrationMock;

	@InjectMocks
	private EventService eventService;

	@Captor
	private ArgumentCaptor<Event> eventArgumentCaptor;

	@Test
	void createChecklistEvent() {
		var eventType = EventType.CREATE;
		var message = "Checklist created";
		var checklistEntity = new ChecklistEntity();
		var user = "user";
		var event = toEvent(eventType, message, checklistEntity, user);

		eventService.createChecklistEvent(eventType, message, checklistEntity, user);

		verify(eventlogIntegrationMock).createEvent(eventArgumentCaptor.capture());
		var capturedEvent = eventArgumentCaptor.getValue();
		assertThat(capturedEvent).usingRecursiveComparison().ignoringFields("created").isEqualTo(event);
		verifyNoMoreInteractions(eventlogIntegrationMock);
	}

	@Test
	void getChecklistEvents() {
		var municipalityId = "municipalityId";
		var logKey = "logKey";
		var pageable = PageRequest.of(0, 10);
		var pageEvent = new PageEvent();
		when(eventlogIntegrationMock.getEvents(municipalityId, logKey, pageable)).thenReturn(pageEvent);

		var result = eventService.getChecklistEvents(municipalityId, logKey, pageable);

		assertThat(result).isEqualTo(pageEvent);
		verify(eventlogIntegrationMock).getEvents(municipalityId, logKey, pageable);
		verifyNoMoreInteractions(eventlogIntegrationMock);
	}

}
