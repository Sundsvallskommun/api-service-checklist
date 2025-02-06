package se.sundsvall.checklist.service;

import static se.sundsvall.checklist.service.mapper.EventlogMapper.toEvent;

import generated.se.sundsvall.eventlog.EventType;
import generated.se.sundsvall.eventlog.PageEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.eventlog.EventlogIntegration;

@Service
public class EventService {

	public static final String TASK_ADDED = "Aktivitet '%s' har lagts till i fas '%s'";
	public static final String TASK_CHANGED = "Aktivitet '%s' i fas '%s' har ändrats";
	public static final String TASK_REMOVED = "Aktivitet '%s' har tagits bort från fas '%s'";
	public static final String CHECKLIST_ACTIVATED = "Mall '%s' har aktiverats";
	public static final String CHECKLIST_CREATED = "Mall '%s' har skapats";
	public static final String CHECKLIST_NEW_VERSION_CREATED = "En ny version av mall '%s' har skapats";
	public static final String CHECKLIST_UPDATED = "Mall '%s' har uppdaterats";
	public static final String CHECKLIST_DELETED = "Mall '%s' har raderats";

	private final EventlogIntegration eventlogIntegration;

	public EventService(EventlogIntegration eventlogIntegration) {
		this.eventlogIntegration = eventlogIntegration;
	}

	public void createChecklistEvent(final EventType eventType, final String message, final ChecklistEntity checklistEntity, final String userId) {
		final var event = toEvent(eventType, message, checklistEntity, userId);
		eventlogIntegration.createEvent(event);
	}

	public PageEvent getChecklistEvents(final String municipalityId, final String checklistId, final Pageable pageable) {
		return eventlogIntegration.getEvents(municipalityId, checklistId, pageable);
	}

}
