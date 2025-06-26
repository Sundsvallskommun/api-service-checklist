package se.sundsvall.checklist.integration.eventlog;

import static org.zalando.problem.Status.BAD_GATEWAY;
import static se.sundsvall.checklist.service.util.StringUtils.sanitizeAndCompress;

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
		final var sanitizedLogKey = sanitizeAndCompress(event.getLogKey());
		try {
			LOG.info("Creating event for checklist: {}", sanitizedLogKey);
			eventlogClient.createEvent(event.getMunicipalityId(), event.getLogKey(), event);
			LOG.info("Successfully created event for checklist: {}", sanitizedLogKey);
		} catch (final Exception e) {
			// This exception should not be thrown to the client.
			LOG.error("Could not create event for checklist: {}", sanitizedLogKey, e);
		}
	}

	public PageEvent getEvents(final String municipalityId, final String logKey, final Pageable pageable) {
		final var sanitizedMunicipalityId = sanitizeAndCompress(municipalityId);
		final var sanitizedLogKey = sanitizeAndCompress(logKey);

		try {
			LOG.info("Fetching events for checklist: {}", sanitizedLogKey);
			final var events = eventlogClient.getEvents(sanitizedMunicipalityId, sanitizedLogKey, pageable);
			LOG.info("Successfully fetched events for checklist: {}", sanitizedLogKey);
			return events;
		} catch (final Exception e) {
			LOG.error("Could not fetch events for checklist: {}", sanitizedLogKey, e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not fetch events for checklist: %s".formatted(sanitizedLogKey));
		}
	}

}
