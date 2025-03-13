package se.sundsvall.checklist.service.mapper;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mockStatic;
import static se.sundsvall.checklist.TestObjectFactory.createCustomTaskEntity;
import static se.sundsvall.checklist.TestObjectFactory.createEmployeeChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseEntity;
import static se.sundsvall.checklist.TestObjectFactory.createTaskEntity;
import static se.sundsvall.checklist.integration.db.model.enums.EmploymentPosition.EMPLOYEE;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toEmployeeChecklist;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toEmployeeChecklistPhase;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toEmployeeChecklistTask;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.toStakeholder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.zalando.problem.Status;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.InitiationInformation;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.InitiationInfoEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.EmploymentPosition;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.service.model.Manager;
import se.sundsvall.dept44.requestid.RequestId;

class EmployeeChecklistMapperTest {

	@Test
	void toInitiationInfoEntity() {
		final var detail = EmployeeChecklistResponse.Detail.builder()
			.withStatus(Status.I_AM_A_TEAPOT)
			.withInformation("Stout and firm")
			.build();
		final var municipalityId = "municipalityId";
		mockStatic(RequestId.class).when(RequestId::get).thenReturn("logId");

		final var entity = EmployeeChecklistMapper.toInitiationInfoEntity(municipalityId, detail);

		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(entity.getLogId()).isEqualTo("logId");
		assertThat(entity.getInformation()).isEqualTo("Stout and firm");
		assertThat(entity.getStatus()).isEqualTo("418 I'm a teapot");
	}

	@ParameterizedTest
	@EnumSource(EmploymentPosition.class)
	void toEmployeeChecklistEntity(EmploymentPosition employmentPosition) {
		// Arrange
		final var startDate = LocalDate.of(2023, 12, 24);
		final var employeeEntity = EmployeeEntity.builder()
			.withEmploymentPosition(employmentPosition)
			.withStartDate(startDate)
			.build();
		final var checklistEntity = ChecklistEntity.builder()
			.build();

		// Act
		final var entity = EmployeeChecklistMapper.toEmployeeChecklistEntity(employeeEntity, List.of(checklistEntity));

		// Assert
		assertThat(entity.getChecklists()).hasSize(1).containsExactly(checklistEntity);
		assertThat(entity.getCorrespondence()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getCustomFulfilments()).isNullOrEmpty();
		assertThat(entity.getCustomTasks()).isNullOrEmpty();
		assertThat(entity.getEmployee()).isEqualTo(employeeEntity);
		assertThat(entity.getEndDate()).isEqualTo(startDate.plusMonths(employmentPosition == EMPLOYEE ? 6 : 24));
		assertThat(entity.getExpirationDate()).isEqualTo(startDate.plusMonths(employmentPosition == EMPLOYEE ? 9 : 27));
		assertThat(entity.getFulfilments()).isNullOrEmpty();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getStartDate()).isEqualTo(startDate);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.isCompleted()).isFalse();
		assertThat(entity.isLocked()).isFalse();
	}

	@ParameterizedTest
	@EnumSource(EmploymentPosition.class)
	void toEmployeeChecklistEntityWhenNoStartDateInformation(EmploymentPosition employmentPosition) {
		// Arrange
		final var employeeEntity = EmployeeEntity.builder()
			.withEmploymentPosition(employmentPosition)
			.build();
		final var checklistEntity = ChecklistEntity.builder()
			.build();

		// Act
		final var entity = EmployeeChecklistMapper.toEmployeeChecklistEntity(employeeEntity, List.of(checklistEntity));

		// Assert
		assertThat(entity.getChecklists()).hasSize(1).containsExactly(checklistEntity);
		assertThat(entity.getCorrespondence()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getCustomFulfilments()).isNullOrEmpty();
		assertThat(entity.getCustomTasks()).isNullOrEmpty();
		assertThat(entity.getEmployee()).isEqualTo(employeeEntity);
		assertThat(entity.getEndDate()).isEqualTo(LocalDate.now().plusMonths(employmentPosition == EMPLOYEE ? 6 : 24));
		assertThat(entity.getExpirationDate()).isEqualTo(LocalDate.now().plusMonths(employmentPosition == EMPLOYEE ? 9 : 27));
		assertThat(entity.getFulfilments()).isNullOrEmpty();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getStartDate()).isEqualTo(LocalDate.now());
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.isCompleted()).isFalse();
		assertThat(entity.isLocked()).isFalse();
	}

	@Test
	void toManagerEntity() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var personId = UUID.randomUUID().toString();
		final var username = "username";

		final var manager = Manager.builder()
			.withEmailAddress(emailAddress)
			.withGivenname(firstName)
			.withLastname(lastName)
			.withPersonId(personId)
			.withLoginname(username)
			.build();

		// Act
		final var entity = OrganizationMapper.toManagerEntity(manager);

		// Assert
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getEmail()).isEqualTo(emailAddress);
		assertThat(entity.getEmployees()).isNullOrEmpty();
		assertThat(entity.getFirstName()).isEqualTo(firstName);
		assertThat(entity.getLastName()).isEqualTo(lastName);
		assertThat(entity.getPersonId()).isEqualTo(personId);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getUsername()).isEqualTo(username);
	}

	@Test
	void toEmployeeChecklistTest() {
		final var entity = createEmployeeChecklistEntity(true, true);

		final var result = toEmployeeChecklist(entity);

		assertThat(result).isNotNull().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(entity.getId());
			assertThat(r.getCreated()).isEqualTo(entity.getCreated());
			assertThat(r.getUpdated()).isEqualTo(entity.getUpdated());
			assertThat(r.getEmployee()).isEqualTo(toStakeholder(entity.getEmployee()));
			assertThat(r.getManager()).isEqualTo(toStakeholder(entity.getEmployee().getManager()));
			assertThat(r.getStartDate()).isEqualTo(entity.getStartDate());
			assertThat(r.getEndDate()).isEqualTo(entity.getEndDate());
			assertThat(r.getExpirationDate()).isEqualTo(entity.getExpirationDate());
			assertThat(r.isLocked()).isEqualTo(entity.isLocked());
			assertThat(r.getCompleted()).isEqualTo(entity.isCompleted());
			assertThat(r.getMentor()).isNotNull().satisfies(mentor -> {
				assertThat(mentor.getUserId()).isEqualTo(entity.getMentor().getUserId());
				assertThat(mentor.getName()).isEqualTo(entity.getMentor().getName());
			});
		});
	}

	@Test
	void toEmployeeChecklistPhaseTest() {
		final var phaseEntity = createPhaseEntity();
		final var taskEntities = List.of(createTaskEntity(phaseEntity), createTaskEntity(phaseEntity));
		final var result = toEmployeeChecklistPhase(phaseEntity, taskEntities);

		assertThat(result).isNotNull().satisfies(phase -> {
			assertThat(phase.getId()).isEqualTo(phaseEntity.getId());
			assertThat(phase.getName()).isEqualTo(phaseEntity.getName());
			assertThat(phase.getBodyText()).isEqualTo(phaseEntity.getBodyText());
			assertThat(phase.getTimeToComplete()).isEqualTo(phaseEntity.getTimeToComplete());
			assertThat(phase.getSortOrder()).isEqualTo(phaseEntity.getSortOrder());
			assertThat(phase.getTasks()).hasSize(2).satisfies(tasks -> {
				assertThat(tasks.stream().map(EmployeeChecklistTask::getId).toList())
					.containsAll(taskEntities.stream().map(TaskEntity::getId).toList());
			});
		});
	}

	@Test
	void toEmployeeChecklistTaskTest() {
		final var entity = createTaskEntity();

		final var result = toEmployeeChecklistTask(entity);

		assertThat(result).isNotNull().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(entity.getId());
			assertThat(r.getHeading()).isEqualTo(entity.getHeading());
			assertThat(r.getText()).isEqualTo(entity.getText());
			assertThat(r.getRoleType()).isEqualTo(entity.getRoleType());
			assertThat(r.getQuestionType()).isEqualTo(entity.getQuestionType());
			assertThat(r.isCustomTask()).isFalse();
		});
	}

	@Test
	void customTaskToEmployeeChecklistTaskTest() {
		final var entity = createCustomTaskEntity();

		final var result = toEmployeeChecklistTask(entity);

		assertThat(result).isNotNull().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(entity.getId());
			assertThat(r.getHeading()).isEqualTo(entity.getHeading());
			assertThat(r.getText()).isEqualTo(entity.getText());
			assertThat(r.getRoleType()).isEqualTo(entity.getRoleType());
			assertThat(r.getQuestionType()).isEqualTo(entity.getQuestionType());
			assertThat(r.isCustomTask()).isTrue();
		});
	}

	@Test
	void toEmployeeChecklistPhasesSortTest() {
		final var phase1 = PhaseEntity.builder().withSortOrder(1).build();
		final var phase2 = PhaseEntity.builder().withSortOrder(2).build();

		assertThat(EmployeeChecklistMapper.toEmployeeChecklistPhases(List.of(
			TaskEntity.builder().withSortOrder(2)
				.withPhase(phase2)
				.build(),
			TaskEntity.builder().withSortOrder(1)
				.withPhase(phase2)
				.build(),
			TaskEntity.builder().withSortOrder(2)
				.withPhase(phase1)
				.build(),
			TaskEntity.builder().withSortOrder(1)
				.withPhase(phase1)
				.build())))
			.hasSize(2)
			.satisfiesExactly(phase -> {
				assertThat(phase.getSortOrder()).isEqualTo(1);
				assertThat(phase.getTasks()).extracting(EmployeeChecklistTask::getSortOrder).containsExactly(1, 2);
			}, phase -> {
				assertThat(phase.getSortOrder()).isEqualTo(2);
				assertThat(phase.getTasks()).extracting(EmployeeChecklistTask::getSortOrder).containsExactly(1, 2);
			});
	}

	@Test
	void toEmployeeChecklistPhasesEmptyTest() {
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistPhases(List.of())).isEmpty();
	}

	@Test
	void toEmployeeChecklistTasksSortTest() {
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistTasks(List.of(
			TaskEntity.builder().withSortOrder(2).build(),
			TaskEntity.builder().withSortOrder(1).build()))).hasSize(2)
			.extracting(EmployeeChecklistTask::getSortOrder).containsExactly(1, 2);
	}

	@Test
	void toEmployeeChecklistTasksEmptyTest() {
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistTasks(List.of())).isEmpty();
	}

	@Test
	void toEmployeeChecklistTaskCustomTaskNullTest() {
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistTask((CustomTaskEntity) null)).isNull();
	}

	@Test
	void toEmployeeChecklistTaskTaskNullTest() {
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistTask((TaskEntity) null)).isNull();
	}

	@Test
	void toEmployeeChecklistEntityFromNull() {
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistEntity(null, null)).isNull();
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistEntity(EmployeeEntity.builder().build(), null)).isNull();
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistEntity(null, List.of(ChecklistEntity.builder().build()))).isNull();
	}

	@Test
	void toFulfilmentEntity() {
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();
		final var taskEntity = TaskEntity.builder().build();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var entity = EmployeeChecklistMapper.toFulfilmentEntity(employeeChecklistEntity, taskEntity, fulfilmentStatus, responseText, updatedBy);

		assertThat(entity.getEmployeeChecklist()).isEqualTo(employeeChecklistEntity);
		assertThat(entity.getTask()).isEqualTo(taskEntity);
		assertThat(entity.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getResponseText()).isEqualTo(responseText);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getLastSavedBy()).isEqualTo(updatedBy);
	}

	@Test
	void toFulfilmentEntityWithoutResponseText() {
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();
		final var taskEntity = TaskEntity.builder().build();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var updatedBy = "updatedBy";

		final var entity = EmployeeChecklistMapper.toFulfilmentEntity(employeeChecklistEntity, taskEntity, fulfilmentStatus, null, updatedBy);

		assertThat(entity.getEmployeeChecklist()).isEqualTo(employeeChecklistEntity);
		assertThat(entity.getTask()).isEqualTo(taskEntity);
		assertThat(entity.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getResponseText()).isNull();
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getLastSavedBy()).isEqualTo(updatedBy);
	}

	@Test
	void toFulfilmentEntityWithEmptyValues() {
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();
		final var taskEntity = TaskEntity.builder().build();

		final var entity = EmployeeChecklistMapper.toFulfilmentEntity(employeeChecklistEntity, taskEntity, null, null, null);

		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "task");
		assertThat(entity.getEmployeeChecklist()).isEqualTo(employeeChecklistEntity);
		assertThat(entity.getTask()).isEqualTo(taskEntity);
	}

	@Test
	void toFulfilmentEntityFromNulls() {
		assertThat(EmployeeChecklistMapper.toFulfilmentEntity(null, TaskEntity.builder().build(), null, null, null)).isNull();
		assertThat(EmployeeChecklistMapper.toFulfilmentEntity(EmployeeChecklistEntity.builder().build(), null, null, null, null)).isNull();
	}

	@Test
	void toCustomFulfilmentEntity() {
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();
		final var taskEntity = CustomTaskEntity.builder().build();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var entity = EmployeeChecklistMapper.toCustomFulfilmentEntity(employeeChecklistEntity, taskEntity, fulfilmentStatus, responseText, updatedBy);

		assertThat(entity.getEmployeeChecklist()).isEqualTo(employeeChecklistEntity);
		assertThat(entity.getCustomTask()).isEqualTo(taskEntity);
		assertThat(entity.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getResponseText()).isEqualTo(responseText);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getLastSavedBy()).isEqualTo(updatedBy);
	}

	@Test
	void toCustomFulfilmentEntityWithoutResponseText() {
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();
		final var taskEntity = CustomTaskEntity.builder().build();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var updatedBy = "updatedBy";

		final var entity = EmployeeChecklistMapper.toCustomFulfilmentEntity(employeeChecklistEntity, taskEntity, fulfilmentStatus, null, updatedBy);

		assertThat(entity.getEmployeeChecklist()).isEqualTo(employeeChecklistEntity);
		assertThat(entity.getCustomTask()).isEqualTo(taskEntity);
		assertThat(entity.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getResponseText()).isNull();
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getLastSavedBy()).isEqualTo(updatedBy);
	}

	@Test
	void toCustomFulfilmentEntityWithEmptyValues() {
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();
		final var taskEntity = CustomTaskEntity.builder().build();

		final var entity = EmployeeChecklistMapper.toCustomFulfilmentEntity(employeeChecklistEntity, taskEntity, null, null, null);

		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "customTask");
		assertThat(entity.getEmployeeChecklist()).isEqualTo(employeeChecklistEntity);
		assertThat(entity.getCustomTask()).isEqualTo(taskEntity);
	}

	@Test
	void toCustomFulfilmentEntityFromNulls() {
		assertThat(EmployeeChecklistMapper.toCustomFulfilmentEntity(null, CustomTaskEntity.builder().build(), null, null, null)).isNull();
		assertThat(EmployeeChecklistMapper.toCustomFulfilmentEntity(EmployeeChecklistEntity.builder().build(), null, null, null, null)).isNull();
	}

	@Test
	void toCustomTaskEntity() {
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();
		final var phaseEntity = PhaseEntity.builder().build();
		final var heading = "heading";
		final var headingReference = "headingReference";
		final var questionType = QuestionType.COMPLETED_OR_NOT_RELEVANT;
		final var roleType = RoleType.MANAGER_FOR_NEW_MANAGER;
		final var sortOrder = 654;
		final var text = "text";
		final var createdBy = "someUSer";

		final var customTaskRequest = CustomTaskCreateRequest.builder()
			.withHeading(heading)
			.withHeadingReference(headingReference)
			.withQuestionType(questionType)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withText(text)
			.withCreatedBy(createdBy)
			.build();

		final var entity = EmployeeChecklistMapper.toCustomTaskEntity(employeeChecklistEntity, phaseEntity, customTaskRequest);

		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getHeading()).isEqualTo(heading);
		assertThat(entity.getHeadingReference()).isEqualTo(headingReference);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getEmployeeChecklist()).isEqualTo(employeeChecklistEntity);
		assertThat(entity.getPhase()).isEqualTo(phaseEntity);
		assertThat(entity.getQuestionType()).isEqualTo(questionType);
		assertThat(entity.getRoleType()).isEqualTo(roleType);
		assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
		assertThat(entity.getText()).isEqualTo(text);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getLastSavedBy()).isEqualTo(createdBy);
	}

	@Test
	void toCustomTaskEntityFromNullRequest() {
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();
		final var phaseEntity = PhaseEntity.builder().build();

		assertThat(EmployeeChecklistMapper.toCustomTaskEntity(employeeChecklistEntity, phaseEntity, null)).isNull();
	}

	@Test
	void updateCustomTaskEntity() {
		final var heading = "heading";
		final var headingReference = "headingReference";
		final var questionType = QuestionType.COMPLETED_OR_NOT_RELEVANT;
		final var roleType = RoleType.NEW_MANAGER;
		final var sortOrder = 654;
		final var text = "text";
		final var updatedBy = "someUser";

		final var request = CustomTaskUpdateRequest.builder()
			.withHeading(heading)
			.withHeadingReference(headingReference)
			.withQuestionType(questionType)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withText(text)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = CustomTaskEntity.builder().build();
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("sortOrder").hasFieldOrPropertyWithValue("sortOrder", 0);

		EmployeeChecklistMapper.updateCustomTaskEntity(entity, request);

		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getHeading()).isEqualTo(heading);
		assertThat(entity.getHeadingReference()).isEqualTo(headingReference);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getEmployeeChecklist()).isNull();
		assertThat(entity.getPhase()).isNull();
		assertThat(entity.getQuestionType()).isEqualTo(questionType);
		assertThat(entity.getRoleType()).isEqualTo(roleType);
		assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
		assertThat(entity.getText()).isEqualTo(text);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getLastSavedBy()).isEqualTo(updatedBy);
	}

	@Test
	void updateCustomTaskEntityFromNullValues() {
		final var entity = CustomTaskEntity.builder().build();
		final var request = CustomTaskUpdateRequest.builder().build();

		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("sortOrder").hasFieldOrPropertyWithValue("sortOrder", 0);
		EmployeeChecklistMapper.updateCustomTaskEntity(entity, request);
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("sortOrder").hasFieldOrPropertyWithValue("sortOrder", 0);
	}

	@Test
	void toCustomTask() {
		final var entity = createCustomTaskEntity();

		final var result = EmployeeChecklistMapper.toCustomTask(entity);

		assertThat(result).isNotNull().satisfies(r -> {
			assertThat(r.getCreated()).isEqualTo(entity.getCreated());
			assertThat(r.getHeading()).isEqualTo(entity.getHeading());
			assertThat(r.getHeadingReference()).isEqualTo(entity.getHeadingReference());
			assertThat(r.getId()).isEqualTo(entity.getId());
			assertThat(r.getQuestionType()).isEqualTo(entity.getQuestionType());
			assertThat(r.getRoleType()).isEqualTo(entity.getRoleType());
			assertThat(r.getSortOrder()).isEqualTo(entity.getSortOrder());
			assertThat(r.getText()).isEqualTo(entity.getText());
			assertThat(r.getUpdated()).isEqualTo(entity.getUpdated());
			assertThat(r.getLastSavedBy()).isEqualTo(entity.getLastSavedBy());
		});
	}

	@Test
	void toCustomTaskFromNull() {
		assertThat(EmployeeChecklistMapper.toCustomTask(null)).isNull();
	}

	@Test
	void toDetail() {
		final var bean = EmployeeChecklistMapper.toDetail(Status.I_AM_A_TEAPOT, "Stout and firm");

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getStatus()).isEqualTo(Status.I_AM_A_TEAPOT);
		assertThat(bean.getInformation()).isEqualTo("Stout and firm");
	}

	@Test
	void toDetailFromNullValues() {
		assertThat(EmployeeChecklistMapper.toDetail(null, null)).hasAllNullFieldsOrProperties();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void toInitiationInformationFromEmptyList(List<InitiationInfoEntity> entries) {
		final var bean = EmployeeChecklistMapper.toInitiationInformation(entries);

		assertThat(bean).hasAllNullFieldsOrPropertiesExcept("summary");
		assertThat(bean.getSummary()).isEqualTo("The last scheduled execution did not find any employees to initialize checklists for");
	}

	@Test
	void toInitiationInformationFromListWithOnlySuccess() {
		final var logId = UUID.randomUUID().toString();
		final var success = InitiationInfoEntity.builder().withCreated(OffsetDateTime.now()).withLogId(logId).withStatus("200").withInformation("Happy life").build();

		final var bean = EmployeeChecklistMapper.toInitiationInformation(List.of(success));

		assertThat(bean.getSummary()).isEqualTo("No problems occurred when initializing checklists for 1 employees");
		assertThat(bean.getExecuted()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(bean.getLogId()).isEqualTo(logId);
		assertThat(bean.getDetails()).hasSize(1)
			.extracting(
				InitiationInformation.Detail::getInformation,
				InitiationInformation.Detail::getStatus)
			.containsExactly(tuple(
				"Happy life",
				200));
	}

	@Test
	void toInitiationInformationFromListWithSuccessAndFailure() {
		final var logId = UUID.randomUUID().toString();
		final var success = InitiationInfoEntity.builder().withCreated(OffsetDateTime.now()).withLogId(logId).withStatus("200").withInformation("Happy life").build();
		final var failure = InitiationInfoEntity.builder().withCreated(OffsetDateTime.now()).withLogId(logId).withStatus("404").withInformation("Not wanted").build();

		final var bean = EmployeeChecklistMapper.toInitiationInformation(List.of(success, failure));

		assertThat(bean.getSummary()).isEqualTo("1 potential problems occurred when initializing checklists for 2 employees");
		assertThat(bean.getExecuted()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(bean.getLogId()).isEqualTo(logId);
		assertThat(bean.getDetails()).hasSize(2)
			.extracting(
				InitiationInformation.Detail::getInformation,
				InitiationInformation.Detail::getStatus)
			.containsExactlyInAnyOrder(
				tuple(
					"Happy life",
					200),
				tuple(
					"Not wanted",
					404));
	}

	@Test
	void toInitiationInformationFromListWithUnknownFailureStatus() {
		final var logId = UUID.randomUUID().toString();
		final var failure = InitiationInfoEntity.builder().withCreated(OffsetDateTime.now()).withLogId(logId).withInformation("Mysterious error").build();

		final var bean = EmployeeChecklistMapper.toInitiationInformation(List.of(failure));

		assertThat(bean.getSummary()).isEqualTo("1 potential problems occurred when initializing checklists for 1 employees");
		assertThat(bean.getExecuted()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(bean.getLogId()).isEqualTo(logId);
		assertThat(bean.getDetails()).hasSize(1)
			.extracting(
				InitiationInformation.Detail::getInformation,
				InitiationInformation.Detail::getStatus)
			.containsExactlyInAnyOrder(tuple(
				"Mysterious error",
				422));
	}
}
