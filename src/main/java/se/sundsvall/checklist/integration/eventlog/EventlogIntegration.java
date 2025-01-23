package se.sundsvall.checklist.integration.eventlog;

import static org.zalando.problem.Status.BAD_GATEWAY;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.PageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

/**
 * Wrapper class for {@link EventlogClient}.
 */
@Component
public class EventlogIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(EventlogIntegration.class);

	private final EventlogClient eventlogClient;

	public EventlogIntegration(final EventlogClient eventlogClient) {
		this.eventlogClient = eventlogClient;
	}

	public void createEvent(final Event event) {
		try {
			LOG.info("Creating event for checklist: {}", event.getLogKey());
			eventlogClient.createEvent(event.getMunicipalityId(), event.getLogKey(), event);
			LOG.info("Successfully created event for checklist: {}", event.getLogKey());
		} catch (Exception e) {
			// This exception should not be thrown to the client.
			LOG.error("Could not create event for checklist: {}", event.getLogKey(), e);
		}
	}

	public PageEvent getEvents(final String municipalityId, final String logKey, final Pageable pageable) {
		try {
			LOG.info("Fetching events for checklist: {}", logKey);
			var events = eventlogClient.getEvents(municipalityId, logKey, pageable);
			LOG.info("Successfully fetched events for checklist: {}", logKey);
			return events;
		} catch (Exception e) {
			LOG.error("Could not fetch events for checklist: {}", logKey, e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not fetch events for checklist: " + logKey);
		}
	}

}
