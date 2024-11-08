package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.TestObjectFactory.createCustomTaskEntity;
import static se.sundsvall.checklist.TestObjectFactory.createEmployeeChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseEntity;
import static se.sundsvall.checklist.TestObjectFactory.createTaskEntity;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.EMPLOYEE;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toEmployeeChecklist;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toEmployeeChecklistPhase;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toEmployeeChecklistTask;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.toStakeholder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import generated.se.sundsvall.employee.Manager;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

class EmployeeChecklistMapperTest {

	@ParameterizedTest
	@EnumSource(RoleType.class)
	void toEmployeeChecklistEntity(RoleType roleType) {
		// Arrange
		final var startDate = LocalDate.of(2023, 12, 24);
		final var employeeEntity = EmployeeEntity.builder()
			.withRoleType(roleType)
			.withStartDate(startDate)
			.build();
		final var checklistEntity = ChecklistEntity.builder()
			.build();

		// Act
		final var entity = EmployeeChecklistMapper.toEmployeeChecklistEntity(employeeEntity, checklistEntity);

		// Assert
		assertThat(entity.getChecklist()).isEqualTo(checklistEntity);
		assertThat(entity.getCorrespondence()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getCustomFulfilments()).isNullOrEmpty();
		assertThat(entity.getCustomTasks()).isNullOrEmpty();
		assertThat(entity.getEmployee()).isEqualTo(employeeEntity);
		assertThat(entity.getEndDate()).isEqualTo(startDate.plusMonths(roleType == EMPLOYEE ? 6 : 24));
		assertThat(entity.getExpirationDate()).isEqualTo(startDate.plusMonths(roleType == EMPLOYEE ? 9 : 27));
		assertThat(entity.getFulfilments()).isNullOrEmpty();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getStartDate()).isEqualTo(startDate);
		assertThat(entity.getUpdated()).isNull();
	}

	@ParameterizedTest
	@EnumSource(RoleType.class)
	void toEmployeeChecklistEntityWhenNoStartDateInformation(RoleType roleType) {
		// Arrange
		final var employeeEntity = EmployeeEntity.builder()
			.withRoleType(roleType)
			.build();
		final var checklistEntity = ChecklistEntity.builder()
			.build();

		// Act
		final var entity = EmployeeChecklistMapper.toEmployeeChecklistEntity(employeeEntity, checklistEntity);

		// Assert
		assertThat(entity.getChecklist()).isEqualTo(checklistEntity);
		assertThat(entity.getCorrespondence()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getCustomFulfilments()).isNullOrEmpty();
		assertThat(entity.getCustomTasks()).isNullOrEmpty();
		assertThat(entity.getEmployee()).isEqualTo(employeeEntity);
		assertThat(entity.getEndDate()).isEqualTo(LocalDate.now().plusMonths(roleType == EMPLOYEE ? 6 : 24));
		assertThat(entity.getExpirationDate()).isEqualTo(LocalDate.now().plusMonths(roleType == EMPLOYEE ? 9 : 27));
		assertThat(entity.getFulfilments()).isNullOrEmpty();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getStartDate()).isEqualTo(LocalDate.now());
		assertThat(entity.getUpdated()).isNull();
	}

	@Test
	void toManagerEntity() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var personId = UUID.randomUUID();
		final var username = "username";

		final var manager = new Manager()
			.emailAddress(emailAddress)
			.givenname(firstName)
			.lastname(lastName)
			.personId(personId)
			.loginname(username);

		// Act
		final var entity = OrganizationMapper.toManagerEntity(manager);

		// Assert
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getEmail()).isEqualTo(emailAddress);
		assertThat(entity.getEmployees()).isNullOrEmpty();
		assertThat(entity.getFirstName()).isEqualTo(firstName);
		assertThat(entity.getLastName()).isEqualTo(lastName);
		assertThat(entity.getPersonId()).isEqualTo(personId.toString());
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getUsername()).isEqualTo(username);
	}

	@Test
	void toEmployeeChecklistTest() {
		final var entity = createEmployeeChecklistEntity(true);

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
			assertThat(r.getMentor()).isNotNull().satisfies(mentor -> {
				assertThat(mentor.getUserId()).isEqualTo(entity.getMentor().getUserId());
				assertThat(mentor.getName()).isEqualTo(entity.getMentor().getName());
			});
		});
	}

	@Test
	void toEmployeeChecklistPhaseTest() {
		final var entity = createPhaseEntity();

		final var result = toEmployeeChecklistPhase(entity);

		assertThat(result).isNotNull().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(entity.getId());
			assertThat(r.getName()).isEqualTo(entity.getName());
			assertThat(r.getBodyText()).isEqualTo(entity.getBodyText());
			assertThat(r.getTimeToComplete()).isEqualTo(entity.getTimeToComplete());
			assertThat(r.getRoleType()).isEqualTo(entity.getRoleType());
			assertThat(r.getSortOrder()).isEqualTo(entity.getSortOrder());
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
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistPhases(List.of(
			PhaseEntity.builder().withSortOrder(2).build(),
			PhaseEntity.builder().withSortOrder(1).build())))
			.hasSize(2)
			.extracting(EmployeeChecklistPhase::getSortOrder).containsExactly(1, 2);
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
		assertThat(EmployeeChecklistMapper.toEmployeeChecklistEntity(null, ChecklistEntity.builder().build())).isNull();
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
		final var questionType = QuestionType.COMPLETED_OR_NOT_RELEVANT;
		final var sortOrder = 654;
		final var text = "text";
		final var createdBy = "someUSer";

		final var customTaskRequest = CustomTaskCreateRequest.builder()
			.withHeading(heading)
			.withQuestionType(questionType)
			.withSortOrder(sortOrder)
			.withText(text)
			.withCreatedBy(createdBy)
			.build();

		final var entity = EmployeeChecklistMapper.toCustomTaskEntity(employeeChecklistEntity, phaseEntity, customTaskRequest);

		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getHeading()).isEqualTo(heading);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getEmployeeChecklist()).isEqualTo(employeeChecklistEntity);
		assertThat(entity.getPhase()).isEqualTo(phaseEntity);
		assertThat(entity.getQuestionType()).isEqualTo(questionType);
		assertThat(entity.getRoleType()).isEqualTo(EMPLOYEE);
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
		final var questionType = QuestionType.COMPLETED_OR_NOT_RELEVANT;
		final var sortOrder = 654;
		final var text = "text";
		final var updatedBy = "someUser";

		final var request = CustomTaskUpdateRequest.builder()
			.withHeading(heading)
			.withQuestionType(questionType)
			.withSortOrder(sortOrder)
			.withText(text)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = CustomTaskEntity.builder().build();
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("sortOrder").hasFieldOrPropertyWithValue("sortOrder", 0);

		EmployeeChecklistMapper.updateCustomTaskEntity(entity, request);

		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getHeading()).isEqualTo(heading);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getEmployeeChecklist()).isNull();
		assertThat(entity.getPhase()).isNull();
		assertThat(entity.getQuestionType()).isEqualTo(questionType);
		assertThat(entity.getRoleType()).isNull();
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
}
