package se.sundsvall.checklist.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;

import java.util.ArrayList;
import java.util.List;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.ChecklistCreateRequest;
import se.sundsvall.checklist.api.model.ChecklistUpdateRequest;
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.api.model.PhaseCreateRequest;
import se.sundsvall.checklist.api.model.PhaseUpdateRequest;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.api.model.TaskCreateRequest;
import se.sundsvall.checklist.api.model.TaskUpdateRequest;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;

public final class ChecklistMapper {
	private ChecklistMapper() {}

	// -----------------------------
	// Entity mappings
	// -----------------------------

	public static ChecklistEntity toChecklistEntity(final ChecklistCreateRequest checklistCreateRequest, final String municipalityId) {
		return ofNullable(checklistCreateRequest)
			.map(request -> ChecklistEntity.builder()
				.withName(request.getName())
				.withDisplayName(request.getDisplayName())
				.withVersion(1)
				.withLifeCycle(CREATED)
				.withMunicipalityId(municipalityId)
				.withLastSavedBy(checklistCreateRequest.getCreatedBy())
				.build())
			.orElse(null);
	}

	public static ChecklistEntity updateChecklistEntity(final ChecklistEntity entity, final ChecklistUpdateRequest request) {
		ofNullable(request.getDisplayName()).ifPresent(entity::setDisplayName);
		entity.setLastSavedBy(request.getUpdatedBy());
		return entity;
	}

	public static PhaseEntity toPhaseEntity(final PhaseCreateRequest phaseCreateRequest, final String municipalityId) {
		return ofNullable(phaseCreateRequest)
			.map(request -> PhaseEntity.builder()
				.withName(request.getName())
				.withBodyText(request.getBodyText())
				.withMunicipalityId(municipalityId)
				.withSortOrder(request.getSortOrder())
				.withPermission(request.getPermission())
				.withTimeToComplete(request.getTimeToComplete())
				.withLastSavedBy(phaseCreateRequest.getCreatedBy())
				.build())
			.orElse(null);
	}

	public static PhaseEntity updatePhaseEntity(final PhaseEntity entity, final PhaseUpdateRequest request) {
		ofNullable(request.getName()).ifPresent(entity::setName);
		ofNullable(request.getBodyText()).ifPresent(entity::setBodyText);
		ofNullable(request.getTimeToComplete()).ifPresent(entity::setTimeToComplete);
		ofNullable(request.getPermission()).ifPresent(entity::setPermission);
		ofNullable(request.getSortOrder()).ifPresent(entity::setSortOrder);
		entity.setLastSavedBy(request.getUpdatedBy());
		return entity;
	}

	public static TaskEntity toTaskEntity(final TaskCreateRequest taskCreateRequest, PhaseEntity phase) {
		return ofNullable(taskCreateRequest)
			.map(request -> TaskEntity.builder()
				.withHeading(request.getHeading())
				.withText(request.getText())
				.withPhase(phase)
				.withPermission(request.getPermission())
				.withRoleType(request.getRoleType())
				.withSortOrder(request.getSortOrder())
				.withQuestionType(request.getQuestionType())
				.withLastSavedBy(taskCreateRequest.getCreatedBy())
				.build())
			.orElse(null);
	}

	public static TaskEntity updateTaskEntity(final TaskEntity entity, final TaskUpdateRequest request) {
		ofNullable(request.getHeading()).ifPresent(entity::setHeading);
		ofNullable(request.getText()).ifPresent(entity::setText);
		ofNullable(request.getRoleType()).ifPresent(entity::setRoleType);
		ofNullable(request.getPermission()).ifPresent(entity::setPermission);
		ofNullable(request.getSortOrder()).ifPresent(entity::setSortOrder);
		ofNullable(request.getQuestionType()).ifPresent(entity::setQuestionType);
		entity.setLastSavedBy(request.getUpdatedBy());
		return entity;
	}

	// -----------------------------
	// API mappings
	// -----------------------------

	public static Checklist toChecklist(final ChecklistEntity checklistEntity) {
		return ofNullable(checklistEntity)
			.map(entity -> Checklist.builder()
				.withId(entity.getId())
				.withName(entity.getName())
				.withDisplayName(entity.getDisplayName())
				.withVersion(entity.getVersion())
				.withLifeCycle(entity.getLifeCycle())
				.withCreated(entity.getCreated())
				.withUpdated(entity.getUpdated())
				.withLastSavedBy(entity.getLastSavedBy())
				.build())
			.orElse(null);
	}

	public static List<Phase> toPhases(final List<PhaseEntity> entities) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.map(ChecklistMapper::toPhase)
			.sorted(comparing(Phase::getSortOrder))
			.collect(toCollection(ArrayList::new));
	}

	public static Phase toPhase(final PhaseEntity phaseEntity) {
		return ofNullable(phaseEntity)
			.map(entity -> Phase.builder()
				.withId(entity.getId())
				.withName(entity.getName())
				.withBodyText(entity.getBodyText())
				.withSortOrder(entity.getSortOrder())
				.withPermission(entity.getPermission())
				.withTimeToComplete(entity.getTimeToComplete())
				.withCreated(entity.getCreated())
				.withUpdated(entity.getUpdated())
				.withLastSavedBy(entity.getLastSavedBy())
				.build())
			.orElse(null);
	}

	public static List<Task> toTasks(final List<TaskEntity> entities) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.map(ChecklistMapper::toTask)
			.sorted(comparing(Task::getSortOrder))
			.collect(toCollection(ArrayList::new));
	}

	public static Task toTask(final TaskEntity taskEntity) {
		return ofNullable(taskEntity)
			.map(entity -> Task.builder()
				.withId(entity.getId())
				.withText(entity.getText())
				.withHeading(entity.getHeading())
				.withRoleType(entity.getRoleType())
				.withQuestionType(entity.getQuestionType())
				.withPermission(entity.getPermission())
				.withSortOrder(entity.getSortOrder())
				.withCreated(entity.getCreated())
				.withUpdated(entity.getUpdated())
				.withLastSavedBy(entity.getLastSavedBy())
				.build())
			.orElse(null);
	}
}
