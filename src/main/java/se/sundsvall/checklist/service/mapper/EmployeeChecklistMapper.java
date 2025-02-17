package se.sundsvall.checklist.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.ObjectUtils.anyNull;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.toStakeholder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;
import se.sundsvall.checklist.api.model.CustomTask;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.InitiationInformation;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklist;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.InitiationInfoEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.dept44.requestid.RequestId;

public final class EmployeeChecklistMapper {

	private EmployeeChecklistMapper() {}

	// -----------------------------
	// Entity mappings
	// -----------------------------

	public static InitiationInfoEntity toInitiationInfoEntity(String municipalityId, EmployeeChecklistResponse.Detail detail) {
		return ofNullable(detail)
			.map(id -> InitiationInfoEntity.builder()
				.withMunicipalityId(municipalityId)
				.withLogId(RequestId.get())
				.withInformation(detail.getInformation())
				.withStatus(detail.getStatus().toString())
				.build())
			.orElse(null);
	}

	public static FulfilmentEntity toFulfilmentEntity(EmployeeChecklistEntity employeeChecklistEntity, TaskEntity taskEntity, FulfilmentStatus status, String responseText, String lastSavedBy) {
		if (anyNull(employeeChecklistEntity, taskEntity)) {
			return null;
		}

		return FulfilmentEntity.builder()
			.withCompleted(status)
			.withEmployeeChecklist(employeeChecklistEntity)
			.withLastSavedBy(lastSavedBy)
			.withResponseText(responseText)
			.withTask(taskEntity)
			.build();
	}

	public static CustomFulfilmentEntity toCustomFulfilmentEntity(EmployeeChecklistEntity employeeChecklistEntity, CustomTaskEntity customTaskEntity, FulfilmentStatus status, String responseText, String lastSavedBy) {
		if (anyNull(employeeChecklistEntity, customTaskEntity)) {
			return null;
		}

		return CustomFulfilmentEntity.builder()
			.withCompleted(status)
			.withCustomTask(customTaskEntity)
			.withEmployeeChecklist(employeeChecklistEntity)
			.withLastSavedBy(lastSavedBy)
			.withResponseText(responseText)
			.build();
	}

	public static EmployeeChecklistEntity toEmployeeChecklistEntity(final EmployeeEntity employeeEntity, final List<ChecklistEntity> checklistEntities) {
		if (anyNull(employeeEntity, checklistEntities)) {
			return null;
		}

		final var startDate = ofNullable(employeeEntity.getStartDate()).orElse(LocalDate.now());

		return EmployeeChecklistEntity.builder()
			.withChecklists(checklistEntities)
			.withEmployee(employeeEntity)
			.withEndDate(startDate.plus(employeeEntity.getEmploymentPosition().getTimeToComplete()))
			.withExpirationDate(startDate.plus(employeeEntity.getEmploymentPosition().getTimeToExpiration()))
			.withStartDate(startDate)
			.build();
	}

	public static CustomTaskEntity toCustomTaskEntity(EmployeeChecklistEntity employeeChecklistEntity, PhaseEntity phaseEntity, CustomTaskCreateRequest request) {
		return ofNullable(request)
			.map(r -> CustomTaskEntity.builder()
				.withHeading(r.getHeading())
				.withHeadingReference(r.getHeadingReference())
				.withEmployeeChecklist(employeeChecklistEntity)
				.withPhase(phaseEntity)
				.withQuestionType(r.getQuestionType())
				.withRoleType(r.getRoleType())
				.withSortOrder(r.getSortOrder())
				.withText(r.getText())
				.withLastSavedBy(request.getCreatedBy())
				.build())
			.orElse(null);
	}

	public static CustomTaskEntity updateCustomTaskEntity(CustomTaskEntity entity, CustomTaskUpdateRequest request) {
		ofNullable(request.getHeading()).ifPresent(entity::setHeading);
		ofNullable(request.getHeadingReference()).ifPresent(entity::setHeadingReference);
		ofNullable(request.getQuestionType()).ifPresent(entity::setQuestionType);
		ofNullable(request.getRoleType()).ifPresent(entity::setRoleType);
		ofNullable(request.getSortOrder()).ifPresent(entity::setSortOrder);
		ofNullable(request.getText()).ifPresent(entity::setText);
		entity.setLastSavedBy(request.getUpdatedBy());

		return entity;
	}

	// -----------------------------
	// API mappings
	// -----------------------------

	public static OngoingEmployeeChecklist mapToOngoingEmployeeChecklist(final EmployeeChecklistEntity employeeChecklist) {
		return ofNullable(employeeChecklist).map(checklist -> OngoingEmployeeChecklist.builder()
			.withEmployeeName(checklist.getEmployee().getFirstName() + " " + checklist.getEmployee().getLastName())
			.withEmployeeUsername(checklist.getEmployee().getUsername())
			.withDepartmentName(checklist.getEmployee().getDepartment().getOrganizationName())
			.withManagerName(checklist.getEmployee().getManager().getFirstName() + " " + checklist.getEmployee().getManager().getLastName())
			.withDelegatedTo(mapToListOfNames(checklist.getDelegates()))
			.withEmploymentDate(checklist.getEmployee().getStartDate())
			.withPurgeDate(checklist.getEndDate())
			.build())
			.orElse(null);
	}

	public static List<String> mapToListOfNames(final List<DelegateEntity> delegateEntities) {
		return ofNullable(delegateEntities).orElse(emptyList()).stream()
			.map(delegate -> delegate.getFirstName() + " " + delegate.getLastName())
			.toList();
	}

	public static EmployeeChecklist toEmployeeChecklist(final EmployeeChecklistEntity employeeChecklistEntity) {
		return ofNullable(employeeChecklistEntity)
			.map(entity -> EmployeeChecklist.builder()
				.withId(entity.getId())
				.withManager(toStakeholder(entity.getEmployee().getManager()))
				.withEmployee(toStakeholder(entity.getEmployee()))
				.withPhases(toEmployeeChecklistPhases(
					entity.getChecklists().stream()
						.map(ChecklistEntity::getTasks)
						.flatMap(List::stream)
						.toList()))
				.withCreated(entity.getCreated())
				.withUpdated(entity.getUpdated())
				.withStartDate(entity.getStartDate())
				.withEndDate(entity.getEndDate())
				.withExpirationDate(entity.getExpirationDate())
				.withCompleted(entity.isCompleted())
				.withLocked(entity.isLocked())
				.withMentor(ofNullable(employeeChecklistEntity.getMentor())
					.map(mentorEntity -> Mentor.builder()
						.withUserId(mentorEntity.getUserId())
						.withName(mentorEntity.getName())
						.build())
					.orElse(null))
				.build())
			.orElse(null);
	}

	public static List<EmployeeChecklistPhase> toEmployeeChecklistPhases(List<TaskEntity> entities) {
		final Map<PhaseEntity, List<TaskEntity>> groupedTasks = ofNullable(entities).orElse(emptyList()).stream()
			.collect(Collectors.groupingBy(TaskEntity::getPhase));

		return groupedTasks.entrySet().stream()
			.map(entry -> toEmployeeChecklistPhase(entry.getKey(), entry.getValue()))
			.sorted(comparing(EmployeeChecklistPhase::getSortOrder))
			.collect(toCollection(ArrayList::new));
	}

	public static EmployeeChecklistPhase toEmployeeChecklistPhase(final PhaseEntity phaseEntity, List<TaskEntity> taskEntities) {
		return ofNullable(phaseEntity)
			.map(entity -> EmployeeChecklistPhase.builder()
				.withId(entity.getId())
				.withName(entity.getName())
				.withBodyText(entity.getBodyText())
				.withSortOrder(entity.getSortOrder())
				.withTasks(toEmployeeChecklistTasks(taskEntities))
				.withTimeToComplete(entity.getTimeToComplete())
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
				.withHeadingReference(entity.getHeadingReference())
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
				.withHeadingReference(entity.getHeadingReference())
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
				.withHeadingReference(entity.getHeadingReference())
				.withText(entity.getText())
				.withSortOrder(entity.getSortOrder())
				.withRoleType(entity.getRoleType())
				.withQuestionType(entity.getQuestionType())
				.withCreated(customTaskEntity.getCreated())
				.withUpdated(entity.getUpdated())
				.withLastSavedBy(entity.getLastSavedBy())
				.build())
			.orElse(null);
	}

	public static Detail toDetail(StatusType status, String message) {
		return Detail.builder()
			.withInformation(message)
			.withStatus(status).build();
	}

	public static InitiationInformation toInitiationInformation(final List<InitiationInfoEntity> infos) {
		final var entries = CollectionUtils.isEmpty(infos) ? 0 : infos.size();
		final var errors = countErrors(infos);

		return InitiationInformation.builder()
			.withSummary(createHeader(entries, errors))
			.withLogId(ofNullable(infos).filter(ObjectUtils::isNotEmpty).map(List::getFirst).map(InitiationInfoEntity::getLogId).orElse(null))
			.withExecuted(ofNullable(infos).filter(ObjectUtils::isNotEmpty).map(List::getFirst).map(InitiationInfoEntity::getCreated).orElse(null))
			.withDetails(ofNullable(infos).filter(ObjectUtils::isNotEmpty).map(EmployeeChecklistMapper::toDetails).orElse(null))
			.build();
	}

	private static String createHeader(int entries, long errors) {
		if (entries == 0) {
			return "The last scheduled execution did not find any employees to initialize checklists for";
		}
		return errors > 0 ? "%s potential problems occurred when initializing checklists for %s employees".formatted(errors, entries) : "No problems occurred when initializing checklists for %s employees".formatted(entries);
	}

	private static List<InitiationInformation.Detail> toDetails(final List<InitiationInfoEntity> infos) {
		return infos.stream()
			.map(EmployeeChecklistMapper::toDetail)
			.sorted(Comparator.comparing(InitiationInformation.Detail::getStatus).reversed())
			.toList();
	}

	private static InitiationInformation.Detail toDetail(InitiationInfoEntity i) {
		return InitiationInformation.Detail.builder()
			.withStatus(nonNull(i.getStatus()) ? Integer.parseInt(i.getStatus()) : Status.UNPROCESSABLE_ENTITY.getStatusCode())
			.withInformation(i.getInformation())
			.build();
	}

	/**
	 * Method for calculating reported errors (where null status is also considered as an error)
	 *
	 * @param  infos list to evaluate errors in
	 * @return       total amount of errors (where status null is considered to be an error)
	 */
	private static long countErrors(final List<InitiationInfoEntity> infos) {
		final var errors = ofNullable(infos).orElse(emptyList()).stream()
			.map(InitiationInfoEntity::getStatus)
			.filter(Objects::nonNull)
			.map(Integer::parseInt)
			.map(HttpStatus::valueOf)
			.filter(HttpStatus::isError)
			.count();

		return errors + ofNullable(infos).orElse(emptyList()).stream()
			.map(InitiationInfoEntity::getStatus)
			.filter(Objects::isNull)
			.count();
	}
}
