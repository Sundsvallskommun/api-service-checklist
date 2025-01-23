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

	public static final String TASK_ADDED = "Uppgift %s tillagd";
	public static final String TASK_CHANGED = "Uppgift %s ändrad";
	public static final String TASK_REMOVED = "Uppgift %s bortplockad";
	public static final String CHECKLIST_CREATED = "Checklista %s skapad";
	public static final String CHECKLIST_UPDATED = "Checklista %s uppdaterad";
	public static final String CHECKLIST_DELETED = "Checklista %s raderad";

	private final EventlogIntegration eventlogIntegration;

	public EventService(EventlogIntegration eventlogIntegration) {
		this.eventlogIntegration = eventlogIntegration;
	}

	public void createChecklistEvent(final EventType eventType, final String message, final ChecklistEntity checklistEntity, final String user) {
		var event = toEvent(eventType, message, checklistEntity, user);
		eventlogIntegration.createEvent(event);
	}

	public PageEvent getChecklistEvents(final String municipalityId, final String checklistId, final Pageable pageable) {
		return eventlogIntegration.getEvents(municipalityId, checklistId, pageable);
	}

}
