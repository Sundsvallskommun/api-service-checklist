package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.integration.employee.EmployeeFilterBuilder.buildUuidEmployeeFilter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import generated.se.sundsvall.employee.PortalPersonData;
import se.sundsvall.checklist.api.model.CustomTask;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPaginatedResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.integration.db.EmployeeChecklistIntegration;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.employee.EmployeeIntegration;

@ExtendWith(MockitoExtension.class)
class EmployeeChecklistServiceTest {

	@Mock
	private EmployeeChecklistIntegration employeeChecklistIntegrationMock;

	@Mock
	private EmployeeIntegration employeeIntegrationMock;

	@Mock
	private CustomTaskRepository customTaskRepositoryMock;

	@InjectMocks
	private EmployeeChecklistService service;

	@BeforeEach
	void initializeFields() {
		ReflectionTestUtils.setField(service, "employeeInformationUpdateInterval", Duration.ofDays(1));
	}

	@AfterEach
	void assertNoMoreInteractions() {
		verifyNoMoreInteractions(employeeChecklistIntegrationMock, customTaskRepositoryMock, employeeIntegrationMock);
	}

	@Test
	void findEmployeeChecklistsBySearchString() {
		final var result = new EmployeeChecklistPaginatedResponse();
		when(employeeChecklistIntegrationMock.fetchPaginatedEmployeeChecklistsByString(any(), any())).thenReturn(result);

		final var response = service.findEmployeeChecklistsBySearchString(any(), any());

		assertThat(response).isNotNull().isEqualTo(result);
		verify(employeeChecklistIntegrationMock).fetchPaginatedEmployeeChecklistsByString(any(), any());
		verifyNoMoreInteractions(employeeChecklistIntegrationMock);
	}

	@Test
	void fetchOptionalEmployeeChecklist() {
		// Arrange
		final var userId = "userId";
		final var employeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var manager = ManagerEntity.builder().build();
		final var employee = EmployeeEntity.builder()
			.withManager(manager)
			.withUpdated(OffsetDateTime.now())
			.build();
		final var managerTask = TaskEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.MANAGER).build();
		final var employeeTask = TaskEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.EMPLOYEE).build();
		final var managerPhase = PhaseEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.MANAGER).withTasks(List.of(managerTask, employeeTask)).build();
		final var employeePhase = PhaseEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.EMPLOYEE).withTasks(List.of(managerTask, employeeTask)).build();
		final var checklist = ChecklistEntity.builder().withPhases(List.of(managerPhase, employeePhase)).build();
		final var employeChecklistEntity = EmployeeChecklistEntity.builder()
			.withId(employeChecklistId)
			.withEmployee(employee)
			.withChecklist(checklist)
			.build();
		final var customEmployeeTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.withRoleType(RoleType.EMPLOYEE)
			.withPhase(employeePhase)
			.build();
		final var customManagerTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.withRoleType(RoleType.MANAGER)
			.withPhase(managerPhase)
			.build();

		when(employeeChecklistIntegrationMock.fetchOptionalEmployeeChecklist(userId)).thenReturn(Optional.of(employeChecklistEntity));
		when(customTaskRepositoryMock.findAllByEmployeeChecklistId(employeChecklistId)).thenReturn(List.of(customEmployeeTask, customManagerTask));

		// Act
		final var employeeChecklist = service.fetchChecklistForEmployee(userId);

		// Assert and verify
		assertThat(employeeChecklist).isPresent();
		assertThat(employeeChecklist.get().getPhases()).hasSize(1).allSatisfy(ph -> {
			assertThat(ph.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
			assertThat(ph.getTasks()).hasSize(2).allSatisfy(t -> {
				assertThat(t.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
			});
		});
		verify(employeeChecklistIntegrationMock).fetchOptionalEmployeeChecklist(userId);
		verify(customTaskRepositoryMock).findAllByEmployeeChecklistId(employeChecklistId);
		verify(employeeChecklistIntegrationMock).fetchDelegateEmails(employeChecklistId);
	}

	@Test
	void fetchEmployeeChecklistForEmployeeWithOldInformation() {
		// Arrange
		final var employeeId = UUID.randomUUID().toString();
		final var userId = "userId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var employeeEntity = EmployeeEntity.builder()
			.withId(employeeId)
			.withUpdated(OffsetDateTime.now().minusDays(1).minusNanos(1))
			.build();
		final var checklist = ChecklistEntity.builder().build();
		final var employeChecklistEntity = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withEmployee(employeeEntity)
			.withChecklist(checklist)
			.build();
		final var customTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.build();
		final var employee = new Employee();

		when(employeeChecklistIntegrationMock.fetchOptionalEmployeeChecklist(userId)).thenReturn(Optional.of(employeChecklistEntity));
		when(customTaskRepositoryMock.findAllByEmployeeChecklistId(employeeChecklistId)).thenReturn(List.of(customTask));
		when(employeeIntegrationMock.getEmployeeInformation(buildUuidEmployeeFilter(employeeId))).thenReturn(List.of(employee));

		// Act
		final var employeeChecklist = service.fetchChecklistForEmployee(userId);

		// Assert and verify
		assertThat(employeeChecklist).isPresent();
		verify(employeeChecklistIntegrationMock).fetchOptionalEmployeeChecklist(userId);
		verify(customTaskRepositoryMock).findAllByEmployeeChecklistId(employeeChecklistId);
		verify(employeeChecklistIntegrationMock).fetchDelegateEmails(employeeChecklistId);
		verify(employeeIntegrationMock).getEmployeeInformation(buildUuidEmployeeFilter(employeeId));
		verify(employeeChecklistIntegrationMock).updateEmployeeInformation(employeeEntity, employee);
	}

	@Test
	void fetchNonExistentOptionalEmployeeChecklist() {
		// Arrange
		final var userId = "userId";

		// Act
		final var employeeChecklist = service.fetchChecklistForEmployee(userId);

		// Assert and verify
		assertThat(employeeChecklist).isEmpty();
		verify(employeeChecklistIntegrationMock).fetchOptionalEmployeeChecklist(userId);
	}

	@Test
	void fetchEmployeeChecklistsForManager() {
		// Arrange
		final var userId = "userId";
		final var employeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var manager = ManagerEntity.builder()
			.withUserName(userId)
			.build();
		final var employee = EmployeeEntity.builder()
			.withManager(manager)
			.withUpdated(OffsetDateTime.now())
			.build();
		final var checklist = ChecklistEntity.builder().build();
		final var employeChecklistEntity = EmployeeChecklistEntity.builder()
			.withId(employeChecklistId)
			.withEmployee(employee)
			.withChecklist(checklist)
			.build();
		final var customTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklistsForManager(userId)).thenReturn(List.of(employeChecklistEntity));
		when(customTaskRepositoryMock.findAllByEmployeeChecklistId(employeChecklistId)).thenReturn(List.of(customTask));

		// Act
		final var employeChecklists = service.fetchChecklistsForManager(userId);

		// Assert and verify
		assertThat(employeChecklists).hasSize(1);
		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklistsForManager(userId);
		verify(customTaskRepositoryMock).findAllByEmployeeChecklistId(employeChecklistId);
		verify(employeeChecklistIntegrationMock).fetchDelegateEmails(employeChecklistId);
	}

	@Test
	void fetchemployeChecklistsForManagerWithManagerInformationToUpdate() {
		// Arrange
		final var userId = "userId";

		final var employeeId = UUID.randomUUID().toString();
		final var oldManagerId = "oldManagerId";
		final var newManagerId = "newManagerId";
		final var employeChecklistId = UUID.randomUUID().toString();
		final var managerEntity = ManagerEntity.builder()
			.withUserName(oldManagerId)
			.build();
		final var employeeEntity = EmployeeEntity.builder()
			.withId(employeeId)
			.withManager(managerEntity)
			.withUpdated(OffsetDateTime.now().minusDays(1).minusNanos(1))
			.build();
		final var checklist = ChecklistEntity.builder().build();
		final var employeChecklistEntity = EmployeeChecklistEntity.builder()
			.withId(employeChecklistId)
			.withEmployee(employeeEntity)
			.withChecklist(checklist)
			.build();
		final var employment = new Employment()
			.isMainEmployment(true)
			.manager(new Manager().loginname(newManagerId));
		final var employee = new Employee()
			.employments(List.of(employment));

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklistsForManager(userId)).thenReturn(List.of(employeChecklistEntity));
		when(employeeIntegrationMock.getEmployeeInformation(buildUuidEmployeeFilter(employeeId))).thenReturn(List.of(employee));

		// Act
		final var employeChecklists = service.fetchChecklistsForManager(userId);

		// Assert and verify
		assertThat(employeChecklists).isEmpty(); // Due to that employee is updated and no longer has incoming userId as manager
		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklistsForManager(userId);
		verify(employeeChecklistIntegrationMock).updateEmployeeInformation(employeeEntity, employee);
		verify(employeeIntegrationMock).getEmployeeInformation(buildUuidEmployeeFilter(employeeId));
	}

	@Test
	void deleteEmployeChecklist() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();

		// Act
		service.deleteEmployeChecklist(employeChecklistId);

		// Assert and verify
		verify(employeeChecklistIntegrationMock).deleteEmployeeChecklist(employeChecklistId);
	}

	@Test
	void createCustomTask() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().build();
		final var employeChecklistEntity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(employeChecklistId)).thenReturn(employeChecklistEntity);
		when(employeeChecklistIntegrationMock.createCustomTask(employeChecklistId, phaseId, request)).thenReturn(CustomTaskEntity.builder().build());

		// Act
		final var result = service.createCustomTask(employeChecklistId, phaseId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(CustomTask.class);

		verify(employeeChecklistIntegrationMock).createCustomTask(employeChecklistId, phaseId, request);
	}

	@Test
	void createCustomTaskOnLockedemployeChecklist() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeChecklistId)
			.withLocked(true)
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(employeChecklistId)).thenReturn(employeeChecklist);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.createCustomTask(employeChecklistId, phaseId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeChecklistId));

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(employeChecklistId);
	}

	@Test
	void readCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var customTaskEntity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder().withId(employeeChecklistId).build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(customTaskEntity));

		// Act
		final var result = service.readCustomTask(employeeChecklistId, customTaskId);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(CustomTask.class);

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void readNonExistentCustomTask() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.readCustomTask(employeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeChecklistId, customTaskId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void readCustomTaskBelongingToOtherEmployeeChecklist() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var customTaskEntity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder().withId(UUID.randomUUID().toString()).build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(customTaskEntity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.readCustomTask(employeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeChecklistId, customTaskId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void updateCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var heading = "heading";
		final var questionType = QuestionType.COMPLETED_OR_NOT_RELEVANT_WITH_TEXT;
		final var sortOrder = 987;
		final var text = "text";
		final var entity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder().withId(employeeChecklistId).build())
			.build();
		final var request = CustomTaskUpdateRequest.builder()
			.withHeading(heading)
			.withQuestionType(questionType)
			.withSortOrder(sortOrder)
			.withText(text)
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var result = service.updateCustomTask(employeeChecklistId, customTaskId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(CustomTask.class);

		verify(customTaskRepositoryMock).findById(customTaskId);
		verify(customTaskRepositoryMock).save(entity);
	}

	@Test
	void updateNonExistentCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var request = CustomTaskUpdateRequest.builder().build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateCustomTask(employeeChecklistId, customTaskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeeChecklistId, customTaskId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void updateCustomTaskBelongingToOtherEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var entity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder().withId(UUID.randomUUID().toString()).build())
			.build();
		final var request = CustomTaskUpdateRequest.builder().build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateCustomTask(employeeChecklistId, customTaskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeeChecklistId, customTaskId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void updateCustomTaskOnLockedEmployeeChecklist() {
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var entity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder()
				.withLocked(true)
				.withId(employeeChecklistId)
				.build())
			.build();
		final var request = CustomTaskUpdateRequest.builder().build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateCustomTask(employeeChecklistId, customTaskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeeChecklistId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void deleteCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var entity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder().withId(employeeChecklistId)
				.withCustomFulfilments(new ArrayList<>(List.of(CustomFulfilmentEntity.builder()
					.withCustomTask(CustomTaskEntity.builder()
						.withId(customTaskId)
						.build())
					.build())))
				.build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		service.deleteCustomTask(employeeChecklistId, customTaskId);

		// Assert and verify
		assertThat(entity.getEmployeeChecklist().getCustomFulfilments()).isEmpty();
		verify(customTaskRepositoryMock).findById(customTaskId);
		verify(customTaskRepositoryMock).delete(entity);
	}

	@Test
	void deleteNonExistentCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.deleteCustomTask(employeeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeeChecklistId, customTaskId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void deleteCustomTaskBelongingToOtherEmployeeChecklist() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var entity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder().withId(UUID.randomUUID().toString()).build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.deleteCustomTask(employeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeChecklistId, customTaskId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void deleteCustomTaskOnLockedEmployeeChecklist() {
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var entity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder()
				.withLocked(true)
				.withId(employeeChecklistId)
				.build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.deleteCustomTask(employeeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeeChecklistId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void updateAllTasksInPhase() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistPhaseUpdateRequest.builder().build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withId(phaseId)
					.withTasks(List.of(TaskEntity.builder().build())).build()))
				.build())
			.build();

		when(employeeChecklistIntegrationMock.updateAllTasksInPhase(employeChecklistId, phaseId, request)).thenReturn(entity);

		// Act
		final var result = service.updateAllTasksInPhase(employeChecklistId, phaseId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(EmployeeChecklistPhase.class);
		assertThat(result.getId()).isEqualTo(phaseId);

		verify(employeeChecklistIntegrationMock).updateAllTasksInPhase(employeChecklistId, phaseId, request);
		verify(customTaskRepositoryMock).findAllByEmployeeChecklistId(employeChecklistId);
	}

	@Test
	void updateAllTasksInPhaseWithException() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistPhaseUpdateRequest.builder().build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withId(UUID.randomUUID().toString())
					.withTasks(List.of(TaskEntity.builder().build())).build()))
				.build())
			.build();

		when(employeeChecklistIntegrationMock.updateAllTasksInPhase(employeChecklistId, phaseId, request)).thenReturn(entity);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateAllTasksInPhase(employeChecklistId, phaseId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(e.getMessage()).isEqualTo("Internal Server Error: Could not read phase with id %s from employee checklist with id %s.".formatted(phaseId, employeChecklistId));

		verify(employeeChecklistIntegrationMock).updateAllTasksInPhase(employeChecklistId, phaseId, request);
	}

	@Test
	void updateCommonTaskFulfilment() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withTasks(List.of(TaskEntity.builder()
						.withId(taskId)
						.build()))
					.build()))
				.build())
			.build();
		final var fulfilment = FulfilmentEntity.builder().build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(employeChecklistId)).thenReturn(employeeChecklist);
		when(employeeChecklistIntegrationMock.updateCommonTaskFulfilment(employeChecklistId, taskId, request)).thenReturn(fulfilment);

		// Act
		final var result = service.updateTaskFulfilment(employeChecklistId, taskId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(EmployeeChecklistTask.class);

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(employeChecklistId);
		verify(employeeChecklistIntegrationMock).updateCommonTaskFulfilment(employeChecklistId, taskId, request);
	}

	@Test
	void updateNonExistingCommonTaskFulfilment() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeChecklistId)
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withTasks(List.of(TaskEntity.builder()
						.withId(UUID.randomUUID().toString())
						.build()))
					.build()))
				.build())
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(employeChecklistId)).thenReturn(employeeChecklist);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateTaskFulfilment(employeChecklistId, taskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Task with id %s was not found in employee checklist with id %s.".formatted(taskId, employeChecklistId));

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(employeChecklistId);
	}

	@Test
	void updateCustomTaskFulfilment() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(CustomTaskEntity.builder().withId(taskId).build()))
			.build();
		final var fulfilment = CustomFulfilmentEntity.builder().build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(employeChecklistId)).thenReturn(employeeChecklist);
		when(employeeChecklistIntegrationMock.updateCustomTaskFulfilment(employeChecklistId, taskId, request)).thenReturn(fulfilment);

		// Act
		final var result = service.updateTaskFulfilment(employeChecklistId, taskId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(EmployeeChecklistTask.class);

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(employeChecklistId);
		verify(employeeChecklistIntegrationMock).updateCustomTaskFulfilment(employeChecklistId, taskId, request);
	}

	@Test
	void updateNonExistingCustomTaskFulfilment() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeChecklistId)
			.withCustomTasks(List.of(CustomTaskEntity.builder().withId(UUID.randomUUID().toString())
				.build()))
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(employeChecklistId)).thenReturn(employeeChecklist);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateTaskFulfilment(employeChecklistId, taskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Task with id %s was not found in employee checklist with id %s.".formatted(taskId, employeChecklistId));

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(employeChecklistId);
	}

	@Test
	void updateTaskFulfilmentOnLockedEmployeeChecklist() {
		// Arrange
		final var employeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeChecklistId)
			.withLocked(true)
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(employeChecklistId)).thenReturn(employeeChecklist);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateTaskFulfilment(employeChecklistId, taskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeChecklistId));

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(employeChecklistId);
	}

	@Test
	void initiateEmployeeChecklists() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var orgTree = "2|12|OrgLevel 2¤3|122|OrgLevel 3¤4|" + orgId + "|OrgLevel 4";
		final var information = "All is good in the neighborhood";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		final var portalPersonData = new PortalPersonData()
			.companyId(companyId)
			.orgTree(orgTree);

		when(employeeIntegrationMock.getNewEmployees(any())).thenReturn(List.of(employee));
		when(employeeIntegrationMock.getEmployeeByEmail(emailAddress)).thenReturn(Optional.of(portalPersonData));
		when(employeeChecklistIntegrationMock.initiateEmployee(any(), any())).thenReturn(information);

		// Act
		final var response = service.initiateEmployeeChecklists();

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("Successful import of 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.OK, information));

		verify(employeeIntegrationMock).getNewEmployees("{}");
		verify(employeeIntegrationMock).getEmployeeByEmail(emailAddress);
		verify(employeeChecklistIntegrationMock).initiateEmployee(eq(employee), any());
	}

	@Test
	void initiateEmployeeChecklistssNoStructuralDataFound() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);

		when(employeeIntegrationMock.getNewEmployees(any())).thenReturn(List.of(employee));

		// Act
		final var response = service.initiateEmployeeChecklists();

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("1 potential problems occurred when importing 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.NOT_FOUND, "Not Found: Employee with username loginName is missing information regarding organizational structure."));

		verify(employeeIntegrationMock).getNewEmployees("{}");
		verify(employeeIntegrationMock).getEmployeeByEmail(emailAddress);
		verify(employeeChecklistIntegrationMock, never()).initiateEmployee(any(), any());
	}

	@Test
	void initiateEmployeeChecklistsThrowsException() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var orgTree = "2|12|OrgLevel 2¤3|122|OrgLevel 3¤4|" + orgId + "|OrgLevel 4";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		final var portalPersonData = new PortalPersonData()
			.companyId(companyId)
			.orgTree(orgTree);

		when(employeeIntegrationMock.getNewEmployees(any())).thenReturn(List.of(employee));
		when(employeeIntegrationMock.getEmployeeByEmail(emailAddress)).thenReturn(Optional.of(portalPersonData));
		when(employeeChecklistIntegrationMock.initiateEmployee(any(), any())).thenThrow(new NullPointerException("There is  a null value in the neighborhood"));

		// Act
		final var response = service.initiateEmployeeChecklists();

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("1 potential problems occurred when importing 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.INTERNAL_SERVER_ERROR, "Internal Server Error: There is  a null value in the neighborhood"));

		verify(employeeIntegrationMock).getNewEmployees("{}");
		verify(employeeIntegrationMock).getEmployeeByEmail(emailAddress);
		verify(employeeChecklistIntegrationMock).initiateEmployee(eq(employee), any());
	}

	@Test
	void initiateEmployeeChecklists_noNewEmployeesFound() {
		// Act
		final var response = service.initiateEmployeeChecklists();

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("No employees found");
		assertThat(response.getDetails()).isNullOrEmpty();

		verify(employeeIntegrationMock).getNewEmployees("{}");
	}

	@Test
	void initiateSpecificEmployeeChecklist() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var orgTree = "2|12|OrgLevel 2¤3|122|OrgLevel 3¤4|" + orgId + "|OrgLevel 4";
		final var information = "All is good in the neighborhood";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		final var portalPersonData = new PortalPersonData()
			.companyId(companyId)
			.orgTree(orgTree);

		when(employeeIntegrationMock.getEmployeeInformation(any())).thenReturn(List.of(employee));
		when(employeeIntegrationMock.getEmployeeByEmail(emailAddress)).thenReturn(Optional.of(portalPersonData));
		when(employeeChecklistIntegrationMock.initiateEmployee(any(), any())).thenReturn(information);

		// Act
		final var response = service.initiateSpecificEmployeeChecklist(employeeUuid.toString());

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("Successful import of 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.OK, information));

		verify(employeeIntegrationMock).getEmployeeInformation("{\"ShowOnlyNewEmployees\":false,\"PersonId\":\"" + employeeUuid.toString() + "\",\"EventInfo\":[\"Mover\",\"Corporate\",\"Company\",\"Rehire,Corporate\"]}");
		verify(employeeIntegrationMock).getEmployeeByEmail(emailAddress);
		verify(employeeChecklistIntegrationMock).initiateEmployee(eq(employee), any());
	}

	@Test
	void initiateSpecificEmployeeChecklist_noNewEmployeeFound() {
		// Arrange
		final var employeeUuid = UUID.randomUUID();

		// Act
		final var response = service.initiateSpecificEmployeeChecklist(employeeUuid.toString());

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("No employees found");
		assertThat(response.getDetails()).isNullOrEmpty();

		verify(employeeIntegrationMock).getEmployeeInformation("{\"ShowOnlyNewEmployees\":false,\"PersonId\":\"" + employeeUuid.toString() + "\",\"EventInfo\":[\"Mover\",\"Corporate\",\"Company\",\"Rehire,Corporate\"]}");
	}

	private static Employee createEmployee(final String emailAddress, final UUID employeeUuid, final UUID managerUuid, final int companyId, final int orgId, final String loginName) {
		return new Employee()
			.emailAddress(emailAddress)
			.personId(employeeUuid)
			.employments(List.of(new Employment()
				.formOfEmploymentId("1")
				.isMainEmployment(true)
				.companyId(companyId)
				.orgId(orgId)
				.manager(new Manager()
					.personId(managerUuid))))
			.loginname(loginName);
	}
}
