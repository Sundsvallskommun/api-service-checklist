package se.sundsvall.checklist.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.ObjectUtils.anyNull;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.EMPLOYEE;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.toStakeholder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import se.sundsvall.checklist.api.model.CustomTask;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistInformation;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;

public final class EmployeeChecklistMapper {

	private EmployeeChecklistMapper() {}

	// -----------------------------
	// Entity mappings
	// -----------------------------

	public static FulfilmentEntity toFulfilmentEntity(EmployeeChecklistEntity employeeChecklistEntity, TaskEntity taskEntity, FulfilmentStatus status) {
		return toFulfilmentEntity(employeeChecklistEntity, taskEntity, status, null);
	}

	public static FulfilmentEntity toFulfilmentEntity(EmployeeChecklistEntity employeeChecklistEntity, TaskEntity taskEntity, FulfilmentStatus status, String responseText) {
		if (anyNull(employeeChecklistEntity, taskEntity)) {
			return null;
		}

		return FulfilmentEntity.builder()
			.withCompleted(status)
			.withEmployeeChecklist(employeeChecklistEntity)
			.withResponseText(responseText)
			.withTask(taskEntity)
			.build();
	}

	public static CustomFulfilmentEntity toCustomFulfilmentEntity(EmployeeChecklistEntity employeeChecklistEntity, CustomTaskEntity customTaskEntity, FulfilmentStatus status) {
		return toCustomFulfilmentEntity(employeeChecklistEntity, customTaskEntity, status, null);
	}

	public static CustomFulfilmentEntity toCustomFulfilmentEntity(EmployeeChecklistEntity employeeChecklistEntity, CustomTaskEntity customTaskEntity, FulfilmentStatus status, String responseText) {
		if (anyNull(employeeChecklistEntity, customTaskEntity)) {
			return null;
		}

		return CustomFulfilmentEntity.builder()
			.withCompleted(status)
			.withCustomTask(customTaskEntity)
			.withEmployeeChecklist(employeeChecklistEntity)
			.withResponseText(responseText)
			.build();
	}

	public static EmployeeChecklistEntity toEmployeeChecklistEntity(final EmployeeEntity employeeEntity, final ChecklistEntity checklistEntity) {
		if (anyNull(employeeEntity, checklistEntity)) {
			return null;
		}

		final var startDate = ofNullable(employeeEntity.getStartDate()).orElse(LocalDate.now());

		return EmployeeChecklistEntity.builder()
			.withChecklist(checklistEntity)
			.withEmployee(employeeEntity)
			.withEndDate(startDate.plus(employeeEntity.getRoleType().getTimeToComplete()))
			.withExpirationDate(startDate.plus(employeeEntity.getRoleType().getTimeToExpiration()))
			.withStartDate(startDate)
			.build();
	}

	public static CustomTaskEntity toCustomTaskEntity(EmployeeChecklistEntity employeeChecklistEntity, PhaseEntity phaseEntity, CustomTaskCreateRequest request) {
		return ofNullable(request)
			.map(r -> CustomTaskEntity.builder()
				.withHeading(r.getHeading())
				.withEmployeeChecklist(employeeChecklistEntity)
				.withPhase(phaseEntity)
				.withQuestionType(r.getQuestionType())
				.withRoleType(EMPLOYEE) // Hardcoded as custom tasks only can exist for the employee, never for the manager
				.withSortOrder(r.getSortOrder())
				.withText(r.getText())
				.build())
			.orElse(null);
	}

	public static CustomTaskEntity updateCustomTaskEntity(CustomTaskEntity entity, CustomTaskUpdateRequest request) {
		ofNullable(request.getHeading()).ifPresent(entity::setHeading);
		ofNullable(request.getQuestionType()).ifPresent(entity::setQuestionType);
		ofNullable(request.getSortOrder()).ifPresent(entity::setSortOrder);
		ofNullable(request.getText()).ifPresent(entity::setText);

		return entity;
	}

	// -----------------------------
	// API mappings
	// -----------------------------

	public static EmployeeChecklistInformation toEmployeeChecklistDTO(final EmployeeChecklistEntity employeeChecklistEntity) {
		return ofNullable(employeeChecklistEntity)
			.map(entity -> EmployeeChecklistInformation.builder()
				.withId(entity.getId())
				.withEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName())
				.withEmployeeUsername(entity.getEmployee().getUsername())
				.withOrganizationName(entity.getEmployee().getCompany().getOrganizationName())
				.withManagerName(entity.getEmployee().getManager().getFirstName() + " " + entity.getEmployee().getManager().getLastName())
				.withEmployeeStartDate(entity.getEmployee().getStartDate())
				.withDelegatedTo(employeeChecklistEntity.getDelegates().stream().map(d -> d.getFirstName() + " " + d.getLastName()).toList())
				.build())
			.orElse(null);
	}

	public static EmployeeChecklist toEmployeeChecklist(final EmployeeChecklistEntity employeeChecklistEntity) {
		return ofNullable(employeeChecklistEntity)
			.map(entity -> EmployeeChecklist.builder()
				.withId(entity.getId())
				.withManager(toStakeholder(entity.getEmployee().getManager()))
				.withEmployee(toStakeholder(entity.getEmployee()))
				.withPhases(toEmployeeChecklistPhases(entity.getChecklist().getPhases()))
				.withCreated(entity.getCreated())
				.withUpdated(entity.getUpdated())
				.withStartDate(entity.getStartDate())
				.withEndDate(entity.getEndDate())
				.withExpirationDate(entity.getExpirationDate())
				.withLocked(entity.isLocked())
				.build())
			.orElse(null);
	}

	public static List<EmployeeChecklistPhase> toEmployeeChecklistPhases(List<PhaseEntity> entities) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.map(EmployeeChecklistMapper::toEmployeeChecklistPhase)
			.sorted(comparing(EmployeeChecklistPhase::getSortOrder))
			.collect(toCollection(ArrayList::new));
	}

	public static EmployeeChecklistPhase toEmployeeChecklistPhase(final PhaseEntity phaseEntity) {
		return ofNullable(phaseEntity)
			.map(entity -> EmployeeChecklistPhase.builder()
				.withId(entity.getId())
				.withName(entity.getName())
				.withBodyText(entity.getBodyText())
				.withRoleType(entity.getRoleType())
				.withSortOrder(entity.getSortOrder())
				.withTimeToComplete(entity.getTimeToComplete())
				.withTasks(toEmployeeChecklistTasks(entity.getTasks()))
				.build())
			.orElse(null);
	}

	public static List<EmployeeChecklistTask> toEmployeeChecklistTasks(List<TaskEntity> entities) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.map(EmployeeChecklistMapper::toEmployeeChecklistTask)
			.sorted(comparing(EmployeeChecklistTask::getSortOrder))
			.collect(toCollection(ArrayList::new));
	}

	public static EmployeeChecklistTask toEmployeeChecklistTask(final CustomTaskEntity customTaskEntity) {
		return ofNullable(customTaskEntity)
			.map(entity -> EmployeeChecklistTask.builder()
				.withId(entity.getId())
				.withHeading(entity.getHeading())
				.withText(entity.getText())
				.withSortOrder(entity.getSortOrder())
				.withRoleType(entity.getRoleType())
				.withQuestionType(entity.getQuestionType())
				.withCustomTask(true)
				.build())
			.orElse(null);
	}

	public static EmployeeChecklistTask toEmployeeChecklistTask(final TaskEntity taskEntity) {
		return ofNullable(taskEntity)
			.map(entity -> EmployeeChecklistTask.builder()
				.withId(entity.getId())
				.withHeading(entity.getHeading())
				.withText(entity.getText())
				.withSortOrder(entity.getSortOrder())
				.withRoleType(entity.getRoleType())
				.withQuestionType(entity.getQuestionType())
				.build())
			.orElse(null);
	}

	public static CustomTask toCustomTask(final CustomTaskEntity customTaskEntity) {
		return ofNullable(customTaskEntity)
			.map(entity -> CustomTask.builder()
				.withId(entity.getId())
				.withHeading(entity.getHeading())
				.withText(entity.getText())
				.withSortOrder(entity.getSortOrder())
				.withRoleType(entity.getRoleType())
				.withQuestionType(entity.getQuestionType())
				.withCreated(customTaskEntity.getCreated())
				.withUpdated(entity.getUpdated())
				.build())
			.orElse(null);
	}
}
