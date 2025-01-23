package se.sundsvall.checklist.service.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.EventType;
import generated.se.sundsvall.eventlog.Metadata;
import java.util.Optional;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;

public final class EventlogMapper {

	static final String OWNER = "Checklist";
	static final String USER = "user";
	static final String UNKNOWN = "Okänd";

	private EventlogMapper() {}

	public static Event toEvent(final EventType eventType, final String message, final ChecklistEntity checklistEntity, final String source) {
		return new Event()
			.created(now(systemDefault()))
			.municipalityId(checklistEntity.getMunicipalityId())
			.logKey(checklistEntity.getId())
			.type(eventType)
			.message(message)
			.owner(OWNER)
			.sourceType(Optional.of(checklistEntity).map(entity -> entity.getClass().getSimpleName()).orElse(null))
			.addMetadataItem(new Metadata(USER, Optional.ofNullable(source).orElse(UNKNOWN)));
	}
}
