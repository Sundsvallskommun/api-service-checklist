package se.sundsvall.checklist.service.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.EventType;
import generated.se.sundsvall.eventlog.Metadata;
import generated.se.sundsvall.eventlog.PageEvent;
import java.util.List;
import java.util.Optional;
import se.sundsvall.checklist.api.model.Events;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;

public final class EventlogMapper {

	static final String OWNER = "Checklist";
	static final String USER_ID = "UserId";
	static final String UNKNOWN = "Okänd";

	private EventlogMapper() {}

	public static Event toEvent(final EventType eventType, final String message, final ChecklistEntity checklistEntity, final String userId) {
		return new Event()
			.created(now(systemDefault()))
			.municipalityId(checklistEntity.getMunicipalityId())
			.logKey(checklistEntity.getId())
			.type(eventType)
			.message(message)
			.owner(OWNER)
			.sourceType(Optional.of(checklistEntity).map(entity -> entity.getClass().getSimpleName()).orElse(null))
			.addMetadataItem(new Metadata(USER_ID, Optional.ofNullable(userId).orElse(UNKNOWN)));
	}

	public static Events toEvents(final PageEvent pageEvent) {
		var events = new Events();
		events.setEventList(toEvents(pageEvent.getContent()));

		events.setPage(pageEvent.getPageable().getPageNumber());
		events.setCount(pageEvent.getNumberOfElements());
		events.setLimit(pageEvent.getPageable().getPageSize());
		events.setTotalPages(pageEvent.getTotalPages());
		events.setTotalRecords(pageEvent.getTotalElements());
		return events;
	}

	public static List<se.sundsvall.checklist.api.model.Event> toEvents(final List<Event> events) {
		return Optional.ofNullable(events).map(e -> e.stream()
			.map(EventlogMapper::toEvent)
			.toList())
			.orElse(null);
	}

	public static se.sundsvall.checklist.api.model.Event toEvent(final Event event) {
		return Optional.ofNullable(event).map(e -> se.sundsvall.checklist.api.model.Event.builder()
			.withCreated(e.getCreated())
			.withHistoryReference(e.getHistoryReference())
			.withLogKey(e.getLogKey())
			.withMessage(e.getMessage())
			.withMetadata(toMetadata(e.getMetadata()))
			.withMunicipalityId(e.getMunicipalityId())
			.withOwner(e.getOwner())
			.withSourceType(e.getSourceType())
			.withEventType(e.getType().toString())
			.build())
			.orElse(null);
	}

	public static List<se.sundsvall.checklist.api.model.Metadata> toMetadata(final List<Metadata> metadata) {
		return Optional.ofNullable(metadata).map(m -> m.stream()
			.map(EventlogMapper::toMetadata)
			.toList())
			.orElse(null);
	}

	public static se.sundsvall.checklist.api.model.Metadata toMetadata(final Metadata metadata) {
		return Optional.ofNullable(metadata).map(m -> se.sundsvall.checklist.api.model.Metadata.builder()
			.withKey(m.getKey())
			.withValue(m.getValue())
			.build())
			.orElse(null);
	}
}
