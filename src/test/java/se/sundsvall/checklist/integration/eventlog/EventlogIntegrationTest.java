package se.sundsvall.checklist.integration.eventlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.PageEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class EventlogIntegrationTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String LOG_KEY = "logKey";

	@Mock
	private EventlogClient eventlogClientMock;

	@InjectMocks
	private EventlogIntegration eventlogIntegration;

	/**
	 * Test scenario where the eventlogClient returns a successful response.
	 */
	@Test
	void createEvent_1() {
		var event = new Event();
		event.setMunicipalityId(MUNICIPALITY_ID);
		event.setLogKey(LOG_KEY);
		when(eventlogClientMock.createEvent(MUNICIPALITY_ID, LOG_KEY, event)).thenReturn(ResponseEntity.ok().build());

		eventlogIntegration.createEvent(event);

		verify(eventlogClientMock).createEvent(MUNICIPALITY_ID, LOG_KEY, event);
	}

	/**
	 * Test scenario where the eventlogClient throws an exception, but the exception is not handled.
	 */
	@Test
	void createEvent_2() {
		var event = new Event();
		event.setMunicipalityId(MUNICIPALITY_ID);
		event.setLogKey(LOG_KEY);
		when(eventlogClientMock.createEvent(MUNICIPALITY_ID, LOG_KEY, event)).thenThrow(new RuntimeException());

		eventlogIntegration.createEvent(event);

		verify(eventlogClientMock).createEvent(MUNICIPALITY_ID, LOG_KEY, event);
	}

	/**
	 * Test scenario where the eventlogClient returns a pageEvent.
	 */
	@Test
	void getEvents_1() {
		var municipalityId = MUNICIPALITY_ID;
		var logKey = LOG_KEY;
		var pageable = PageRequest.of(0, 10);
		var pageEvent = new PageEvent();
		when(eventlogClientMock.getEvents(municipalityId, logKey, pageable)).thenReturn(pageEvent);

		var result = eventlogIntegration.getEvents(municipalityId, logKey, pageable);

		assertThat(result).isEqualTo(pageEvent);
		verify(eventlogClientMock).getEvents(municipalityId, logKey, pageable);
	}

	/**
	 * Test scenario where the eventlogClient throws an exception.
	 */
	@Test
	void getEvents_2() {
		var municipalityId = MUNICIPALITY_ID;
		var logKey = LOG_KEY;
		var pageable = PageRequest.of(0, 10);
		when(eventlogClientMock.getEvents(municipalityId, logKey, pageable)).thenThrow(new RuntimeException());

		assertThatThrownBy(() -> eventlogIntegration.getEvents(municipalityId, logKey, pageable))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Could not fetch events for checklist: " + logKey);

		verify(eventlogClientMock).getEvents(municipalityId, logKey, pageable);
	}
}
