package se.sundsvall.checklist.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.createEmployeeChecklistEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.specification.filterSpecification;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeRepository;
import se.sundsvall.checklist.integration.db.repository.ManagerRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.service.OrganizationTree;
import se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper;
import se.sundsvall.checklist.service.mapper.OrganizationMapper;

@ExtendWith(MockitoExtension.class)
class EmployeeChecklistIntegrationTest {

	@Mock
	private EmployeeRepository employeeRepositoryMock;

	@Mock
	private ManagerRepository managerRepositoryMock;

	@Mock
	private EmployeeChecklistRepository employeeChecklistsRepositoryMock;

	@Mock
	private OrganizationRepository organizationRepositoryMock;

	@Mock
	private DelegateRepository delegateRepositoryMock;

	@Mock
	private CustomTaskRepository customTaskRepositoryMock;

	@InjectMocks
	private EmployeeChecklistIntegration integration;

	@Captor
	private ArgumentCaptor<EmployeeEntity> employeeEntityCaptor;

	@Captor
	private ArgumentCaptor<EmployeeChecklistEntity> employeeChecklistEntityCaptor;

	@Test
	void fetchPaginatedEmployeeChecklistsByString() {
		final var entity = createEmployeeChecklistEntity();
		final var dto = EmployeeChecklistMapper.toEmployeeChecklistDTO(entity);
		final filterSpecification spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), entity.getId());
		final var pageable = PageRequest.of(0, 10);
		final Page<EmployeeChecklistEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

		when(employeeChecklistsRepositoryMock.findAll(spec, pageable)).thenReturn(page);

		final var result = integration.fetchPaginatedEmployeeChecklistsByString(spec, pageable);

		assertThat(result.getEmployeeChecklists().getFirst()).isEqualTo(dto);
		verify(employeeChecklistsRepositoryMock).findAll(spec, pageable);
		verifyNoMoreInteractions(employeeChecklistsRepositoryMock);
		verifyNoInteractions(employeeRepositoryMock, managerRepositoryMock, organizationRepositoryMock, delegateRepositoryMock);
	}

	@Test
	void fetchOptionalEmployeeChecklist() {
		// Arrange
		final var userId = "userId";
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findByEmployeeUserName(userId)).thenReturn(entity);

		final var result = integration.fetchOptionalEmployeeChecklist(userId);

		assertThat(result).isEqualTo(Optional.of(entity));
		verify(employeeChecklistsRepositoryMock).findByEmployeeUserName(userId);
	}

	@Test
	void fetchNonExistingOptionalEmployeeChecklist() {
		// Arrange
		final var userId = "userId";

		// Act, assert and verify
		assertThat(integration.fetchOptionalEmployeeChecklist(userId)).isEmpty();
		verify(employeeChecklistsRepositoryMock).findByEmployeeUserName(userId);
	}

	@Test
	void fetchEmployeeChecklistsForManager() {
		// Arrange
		final var userId = "userId";
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findAllByEmployeeManagerUserName(userId)).thenReturn(List.of(entity));

		// Act
		final var result = integration.fetchEmployeeChecklistsForManager(userId);

		// Verify and assert
		assertThat(result).isNotEmpty().containsExactly(entity);
		verify(employeeChecklistsRepositoryMock).findAllByEmployeeManagerUserName(userId);
	}

	@Test
	void updateEmployeeInformationWhenManagerExists() {
		// Arrange
		final var managerId = UUID.randomUUID();
		final var jobTitle = "jobTitle";
		final var entity = EmployeeEntity.builder()
			.withManager(ManagerEntity.builder()
				.withPersonId(UUID.randomUUID().toString()) // Simulate old manager uuid
				.build())
			.build();
		final var employment = new Employment().title(jobTitle).isMainEmployment(true).manager(new Manager().personId(managerId));
		final var employee = new Employee().employments(List.of(employment));

		when(managerRepositoryMock.findById(managerId.toString())).thenReturn(Optional.of(ManagerEntity.builder().withPersonId(managerId.toString()).build()));

		// Act
		integration.updateEmployeeInformation(entity, employee);

		// Verify and assert
		verify(managerRepositoryMock).findById(managerId.toString());
		verify(employeeRepositoryMock).save(employeeEntityCaptor.capture());
		assertThat(employeeEntityCaptor.getValue().getManager().getPersonId()).isEqualTo(managerId.toString());
		assertThat(employeeEntityCaptor.getValue().getTitle()).isEqualTo(jobTitle);
	}

	@Test
	void updateEmployeeInformationWhenManagerNotExists() {
		// Arrange
		final var oldManagerId = UUID.randomUUID().toString();
		final var newManagerId = UUID.randomUUID();
		final var entity = EmployeeEntity.builder()
			.withManager(ManagerEntity.builder()
				.withPersonId(oldManagerId.toString())
				.build())
			.build();
		final var employment = new Employment().isMainEmployment(true).manager(new Manager().personId(newManagerId));
		final var employee = new Employee().employments(List.of(employment));

		// Act
		integration.updateEmployeeInformation(entity, employee);

		// Verify and assert
		verify(managerRepositoryMock).findById(newManagerId.toString());
		verify(employeeRepositoryMock).save(employeeEntityCaptor.capture());
		assertThat(employeeEntityCaptor.getValue().getManager().getPersonId()).isEqualTo(newManagerId.toString());
	}

	@Test
	void fetchDelegateEmail() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var email = "email";
		final var entity = DelegateEntity.builder().withEmail(email).build();

		when(delegateRepositoryMock.findAllByEmployeeChecklistId(employeeChecklistId)).thenReturn(List.of(entity));

		// Act
		final var result = integration.fetchDelegateEmails(employeeChecklistId);

		// Verify and assert
		assertThat(result).hasSize(1).containsExactly(email);
		verify(delegateRepositoryMock).findAllByEmployeeChecklistId(employeeChecklistId);
	}

	@Test
	void fetchDelegateEmailWhenNotFound() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();

		when(delegateRepositoryMock.findAllByEmployeeChecklistId(employeeChecklistId)).thenReturn(Collections.emptyList());

		// Act
		final var result = integration.fetchDelegateEmails(employeeChecklistId);

		// Verify and assert
		assertThat(result).isNull();
		verify(delegateRepositoryMock).findAllByEmployeeChecklistId(employeeChecklistId);
	}

	@Test
	void updateAllTasksInPhaseOnLockedEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.EMPTY;
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(fulfilmentStatus)
			.build();
		final var entity = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withLocked(true)
			.build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateAllTasksInPhase(employeeChecklistId, phaseId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void updateAllTasksInPhaseWhenNoPresentFulfilmentExists() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.EMPTY;
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(fulfilmentStatus)
			.build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withId(phaseId)
					.withTasks(List.of(TaskEntity.builder()
						.build()))
					.build()))
				.build())
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withPhase(PhaseEntity.builder()
					.withId(phaseId)
					.build())
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));
		when(employeeChecklistsRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		final var result = integration.updateAllTasksInPhase(employeeChecklistId, phaseId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		verify(employeeChecklistsRepositoryMock).save(entity);

		assertThat(result.getFulfilments()).hasSize(1).allSatisfy(f -> {
			assertThat(f).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "task", "completed");
			assertThat(f.getCompleted()).isEqualTo(fulfilmentStatus);
		});
		assertThat(result.getCustomFulfilments()).hasSize(1).allSatisfy(f -> {
			assertThat(f).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "customTask", "completed");
			assertThat(f.getCompleted()).isEqualTo(fulfilmentStatus);
		});
	}

	@Test
	void updateAllTasksInPhaseWhenPresentFulfilmentExists() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.EMPTY;
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(fulfilmentStatus)
			.build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withId(phaseId)
					.withTasks(List.of(TaskEntity.builder()
						.build()))
					.build()))
				.build())
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withPhase(PhaseEntity.builder()
					.withId(phaseId)
					.build())
				.build()))
			.build();

		entity.setFulfilments(List.of(FulfilmentEntity.builder()
			.withEmployeeChecklist(entity)
			.withTask(entity.getChecklist().getPhases().getFirst().getTasks().getFirst())
			.build()));
		entity.setCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
			.withEmployeeChecklist(entity)
			.withCustomTask(entity.getCustomTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));
		when(employeeChecklistsRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		final var result = integration.updateAllTasksInPhase(employeeChecklistId, phaseId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		verify(employeeChecklistsRepositoryMock).save(entity);

		assertThat(result.getFulfilments()).hasSize(1).allSatisfy(f -> {
			assertThat(f).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "task", "completed");
			assertThat(f.getCompleted()).isEqualTo(fulfilmentStatus);
		});
		assertThat(result.getCustomFulfilments()).hasSize(1).allSatisfy(f -> {
			assertThat(f).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "customTask", "completed");
			assertThat(f.getCompleted()).isEqualTo(fulfilmentStatus);
		});
	}

	@Test
	void updateAllTasksInPhaseWhenFulfilmentStatusNotSetInRequest() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistPhaseUpdateRequest.builder().build();
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateAllTasksInPhase(employeeChecklistId, phaseId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		assertThat(result).isEqualTo(entity);
	}

	@Test
	void updateAllTasksInPhaseForNonExistingEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistPhaseUpdateRequest.builder().build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateAllTasksInPhase(employeeChecklistId, phaseId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found.".formatted(employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void fetchEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.fetchEmployeeChecklist(employeeChecklistId);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		assertThat(result).isEqualTo(entity);
	}

	@Test
	void fetchNonExistingEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.fetchEmployeeChecklist(employeeChecklistId));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found.".formatted(employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void updateCommonTaskFulfilmentWhenNoPresentFulfilmentsExists() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withTasks(List.of(TaskEntity.builder()
						.withId(taskId)
						.build()))
					.build()))
				.build())
			.build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCommonTaskFulfilment(employeeChecklistId, taskId, request);

		// Verify and assert
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		verify(employeeChecklistsRepositoryMock).save(entity);
	}

	@Test
	void updateCommonTaskFulfilmentWhenPresentFulfilmentsExists() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withTasks(List.of(TaskEntity.builder()
						.withId(taskId)
						.build()))
					.build()))
				.build())
			.build();

		entity.setFulfilments(List.of(FulfilmentEntity.builder()
			.withTask(entity.getChecklist().getPhases().getFirst().getTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCommonTaskFulfilment(employeeChecklistId, taskId, request);

		// Verify and assert
		assertThat(result).isEqualTo(entity.getFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		verify(employeeChecklistsRepositoryMock).save(entity);
	}

	@Test
	void updateCommonTaskFulfilmentForNonExistingTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withTasks(List.of(TaskEntity.builder()
						.withId(UUID.randomUUID().toString())
						.build()))
					.build()))
				.build())
			.build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateCommonTaskFulfilment(employeeChecklistId, taskId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No fulfilment information found for task with id %s in employee checklist with id %s.".formatted(taskId, employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void updateCommonTaskFulfilmentOnNonExistingEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateCommonTaskFulfilment(employeeChecklistId, taskId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found.".formatted(employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void updateCustomTaskFulfilmentWhenNoPresentFulfilmentsExists() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withId(taskId)
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCustomTaskFulfilment(employeeChecklistId, taskId, request);

		// Verify and assert
		assertThat(result).isEqualTo(entity.getCustomFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		verify(employeeChecklistsRepositoryMock).save(entity);
	}

	@Test
	void updateCustomTaskFulfilmentWhenPresentFulfilmentsExists() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withId(taskId)
				.build()))
			.build();

		entity.setCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
			.withCustomTask(entity.getCustomTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCustomTaskFulfilment(employeeChecklistId, taskId, request);

		// Verify and assert
		assertThat(result).isEqualTo(entity.getCustomFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		verify(employeeChecklistsRepositoryMock).save(entity);
	}

	@Test
	void updateCustomTaskFulfilmentForNonExistingTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withId(UUID.randomUUID().toString())
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateCustomTaskFulfilment(employeeChecklistId, taskId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No fulfilment information found for task with id %s in employee checklist with id %s.".formatted(taskId, employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void updateCustomTaskFulfilmentOnNonExistingEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateCustomTaskFulfilment(employeeChecklistId, taskId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found.".formatted(employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void deleteEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		integration.deleteEmployeeChecklist(employeeChecklistId);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		verify(delegateRepositoryMock).deleteByEmployeeChecklist(entity);
		verify(employeeChecklistsRepositoryMock).deleteById(employeeChecklistId);
	}

	@Test
	void deleteNonExistingEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.deleteEmployeeChecklist(employeeChecklistId));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found.".formatted(employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void createCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().withSortOrder(1).build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withId(phaseId)
					.build()))
				.build())
			.build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.createCustomTask(employeeChecklistId, phaseId, request);

		// Verify and assert
		assertThat(result).isNotNull().isInstanceOf(CustomTaskEntity.class);

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
		verify(employeeChecklistsRepositoryMock).save(entity);
		verify(customTaskRepositoryMock).save(result);
	}

	@Test
	void createCustomTaskForNonExistingEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().withSortOrder(1).build();

		final var e = assertThrows(ThrowableProblem.class, () -> integration.createCustomTask(employeeChecklistId, phaseId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found.".formatted(employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);
	}

	@Test
	void createCustomTaskForNonExistingEmployeeChecklistPhase() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().withSortOrder(1).build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklist(ChecklistEntity.builder()
				.withPhases(List.of(PhaseEntity.builder()
					.withId(UUID.randomUUID().toString())
					.build()))
				.build())
			.build();

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		when(employeeChecklistsRepositoryMock.findById(employeeChecklistId)).thenReturn(Optional.of(entity));

		final var e = assertThrows(ThrowableProblem.class, () -> integration.createCustomTask(employeeChecklistId, phaseId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Phase with id %s was not found in employee checklist with id %s.".formatted(phaseId, employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findById(employeeChecklistId);

	}

	@Test
	void initiateEmployeeWithExistingEmployeeChecklist() {
		// Arrange
		final var userName = "userName";
		final var uuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(userName)
			.personId(uuid);

		when(employeeRepositoryMock.existsById(uuid.toString())).thenReturn(true);

		// Act
		final var result = integration.initiateEmployee(employee, null);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(uuid.toString());
		assertThat(result).isEqualTo("Employee with loginname userName already has an employee checklist.");
	}

	@Test
	void initiateEmployeeWithNoMainEmployment() {
		// Arrange
		final var userName = "userName";
		final var uuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(userName)
			.personId(uuid)
			.addEmploymentsItem(
				new Employment()
					.isMainEmployment(false)
					.formOfEmploymentId("1"));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(employee, null));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(uuid.toString());
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No main employment found for employee with loginname userName.");
	}

	@Test
	void initiateEmployeeNotValidForChecklist() {
		// Arrange
		final var userName = "userName";
		final var uuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(userName)
			.personId(uuid)
			.addEmploymentsItem(
				new Employment()
					.isMainEmployment(true)
					.formOfEmploymentId("999"));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(employee, null));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(uuid.toString());
		assertThat(e.getStatus()).isEqualTo(Status.NOT_ACCEPTABLE);
		assertThat(e.getMessage()).isEqualTo("Not Acceptable: Employee with loginname userName does not have an employment type that validates for creating an employee checklist.");
	}

	@Test
	void initiateEmployeeWhenOrganizationIsNotPresentInDatabase() {
		// Arrange
		final var userName = "userName";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(userName)
			.personId(employeeUuid)
			.addEmploymentsItem(
				new Employment()
					.orgId(orgId)
					.orgName(orgName)
					.companyId(companyId)
					.isMainEmployment(true)
					.formOfEmploymentId("1")
					.manager(new Manager()
						.personId(managerUuid)));
		final var orgTree = OrganizationTree.map(companyId, "2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);

		when(employeeRepositoryMock.save(any())).thenReturn(OrganizationMapper.toEmployeeEntity(employee));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(2);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(21);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(212);
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(2124);

		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No EMPLOYEE checklist was found for any id in the organization tree for employee userName. Search has been performed for id 2, 21, 212 and 2124.");
	}

	@Test
	void initiateEmployeeWhenOrganizationChecklistIsNotPresentInDatabase() {
		// Arrange
		final var userName = "userName";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var organizationUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(userName)
			.personId(employeeUuid)
			.isManager(true)
			.addEmploymentsItem(
				new Employment()
					.orgId(orgId)
					.orgName(orgName)
					.companyId(companyId)
					.isMainEmployment(true)
					.formOfEmploymentId("1")
					.manager(new Manager()
						.personId(managerUuid)));
		final var orgTree = OrganizationTree.map(companyId, "2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);
		final var organization = OrganizationEntity.builder()
			.withId(organizationUuid.toString())
			.withOrganizationNumber(companyId)
			.build();

		when(employeeRepositoryMock.save(any())).thenReturn(OrganizationMapper.toEmployeeEntity(employee));
		when(organizationRepositoryMock.findOneByOrganizationNumber(companyId)).thenReturn(organization);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(2);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(21);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(212);
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(2124);

		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No MANAGER checklist was found for any id in the organization tree for employee userName. Search has been performed for id 2, 21, 212 and 2124.");
	}

	@Test
	void initiateEmployeeWhenOrganizationActiveChecklistIsNotPresentInDatabase() {
		// Arrange
		final var userName = "userName";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var organizationUuid = UUID.randomUUID();
		final var checklistUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(userName)
			.personId(employeeUuid)
			.addEmploymentsItem(
				new Employment()
					.orgId(orgId)
					.orgName(orgName)
					.companyId(companyId)
					.isMainEmployment(true)
					.formOfEmploymentId("1")
					.manager(new Manager()
						.personId(managerUuid)));
		final var orgTree = OrganizationTree.map(companyId, "2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);
		final var organization = OrganizationEntity.builder()
			.withId(organizationUuid.toString())
			.withOrganizationNumber(companyId)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withId(checklistUuid.toString())
				.withLifeCycle(LifeCycle.DEPRECATED)
				.build()))
			.build();

		when(employeeRepositoryMock.save(any())).thenReturn(OrganizationMapper.toEmployeeEntity(employee));
		when(organizationRepositoryMock.findOneByOrganizationNumber(companyId)).thenReturn(organization);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(2);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(21);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(212);
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(2124);

		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No EMPLOYEE checklist was found for any id in the organization tree for employee userName. Search has been performed for id 2, 21, 212 and 2124.");
	}

	@Test
	@DisplayName("Initiation of employee belonging to a new department and a new manager, which will add department and manager entities in the database")
	void initiateEmployeeWithNewStructure() {
		// Arrange
		final var userName = "userName";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var organizationUuid = UUID.randomUUID();
		final var checklistUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(userName)
			.personId(employeeUuid)
			.addEmploymentsItem(
				new Employment()
					.orgId(orgId)
					.orgName(orgName)
					.companyId(companyId)
					.isMainEmployment(true)
					.formOfEmploymentId("1")
					.manager(new Manager()
						.personId(managerUuid)));
		final var orgTree = OrganizationTree.map(companyId, "2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);
		final var checklistEntity = ChecklistEntity.builder()
			.withId(checklistUuid.toString())
			.withLifeCycle(LifeCycle.ACTIVE)
			.withRoleType(RoleType.EMPLOYEE)
			.build();
		final var companyEntity = OrganizationEntity.builder()
			.withId(organizationUuid.toString())
			.withOrganizationNumber(companyId)
			.withChecklists(List.of(checklistEntity))
			.build();
		final var employeeEntity = OrganizationMapper.toEmployeeEntity(employee);

		when(employeeRepositoryMock.save(any())).thenReturn(employeeEntity);
		when(organizationRepositoryMock.findOneByOrganizationNumber(companyId)).thenReturn(companyEntity);

		// Act
		final var result = integration.initiateEmployee(employee, orgTree);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(employeeRepositoryMock).save(employeeEntityCaptor.capture());
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(orgId);
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(companyId);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(212);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(21);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(employeeEntityCaptor.getValue().getDepartment()).isNotNull();
		assertThat(employeeEntityCaptor.getValue().getDepartment().getId()).isNull();
		assertThat(employeeEntityCaptor.getValue().getCompany()).isEqualTo(companyEntity);
		assertThat(employeeEntityCaptor.getValue().getEmployeeChecklist()).isNull();
		assertThat(employeeEntityCaptor.getValue().getManager()).isNotNull();
		assertThat(employeeChecklistEntityCaptor.getValue().getEmployee()).isEqualTo(employeeEntity);
		assertThat(employeeChecklistEntityCaptor.getValue().getChecklist()).isEqualTo(checklistEntity);
		assertThat(employeeChecklistEntityCaptor.getValue().getCustomFulfilments()).isNullOrEmpty();
		assertThat(employeeChecklistEntityCaptor.getValue().getCustomTasks()).isNullOrEmpty();
		assertThat(employeeChecklistEntityCaptor.getValue().getFulfilments()).isNullOrEmpty();
		assertThat(result).isEqualTo("Employee with loginname userName processed successfully.");
	}

	@Test
	@DisplayName("Initiation of employee belonging to an existing department and manager, which will not add department and manager entities in the database and will use company checklist")
	void initiateEmployeeWithExistingStructure() {
		// Arrange
		final var userName = "userName";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var companyUuid = UUID.randomUUID();
		final var departmentUuid = UUID.randomUUID();
		final var checklistUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(userName)
			.personId(employeeUuid)
			.addEmploymentsItem(
				new Employment()
					.orgId(orgId)
					.orgName(orgName)
					.companyId(companyId)
					.isMainEmployment(true)
					.formOfEmploymentId("1")
					.manager(new Manager()
						.personId(managerUuid)));
		final var orgTree = OrganizationTree.map(companyId, "2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);
		final var companyChecklistEntity = ChecklistEntity.builder()
			.withId(checklistUuid.toString())
			.withLifeCycle(LifeCycle.ACTIVE)
			.withRoleType(RoleType.EMPLOYEE)
			.build();
		final var departmentChecklistEntity = ChecklistEntity.builder()
			.withId(checklistUuid.toString())
			.withLifeCycle(LifeCycle.ACTIVE)
			.withRoleType(RoleType.MANAGER)
			.build();
		final var companyEntity = OrganizationEntity.builder()
			.withId(companyUuid.toString())
			.withOrganizationNumber(companyId)
			.withChecklists(List.of(companyChecklistEntity))
			.build();
		final var departmentEntity = OrganizationEntity.builder()
			.withId(departmentUuid.toString())
			.withOrganizationNumber(orgId)
			.withOrganizationName(orgName)
			.withChecklists(List.of(departmentChecklistEntity))
			.build();
		final var managerEntity = ManagerEntity.builder()
			.withPersonId(managerUuid.toString())
			.build();

		final var employeeEntity = OrganizationMapper.toEmployeeEntity(employee);

		when(employeeRepositoryMock.save(any())).thenReturn(employeeEntity);
		when(organizationRepositoryMock.findOneByOrganizationNumber(companyId)).thenReturn(companyEntity);
		when(organizationRepositoryMock.findOneByOrganizationNumber(orgId)).thenReturn(departmentEntity);
		when(managerRepositoryMock.findById(managerUuid.toString())).thenReturn(Optional.of(managerEntity));

		// Act
		final var result = integration.initiateEmployee(employee, orgTree);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(employeeRepositoryMock).save(employeeEntityCaptor.capture());
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(orgId);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(21);
		verify(organizationRepositoryMock).findOneByOrganizationNumber(212);
		verify(organizationRepositoryMock, times(2)).findOneByOrganizationNumber(companyId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(employeeEntityCaptor.getValue().getDepartment()).isEqualTo(departmentEntity);
		assertThat(employeeEntityCaptor.getValue().getCompany()).isEqualTo(companyEntity);
		assertThat(employeeEntityCaptor.getValue().getManager()).isEqualTo(managerEntity);
		assertThat(employeeEntityCaptor.getValue().getEmployeeChecklist()).isNull();
		assertThat(employeeChecklistEntityCaptor.getValue().getEmployee()).isEqualTo(employeeEntity);
		assertThat(employeeChecklistEntityCaptor.getValue().getChecklist()).isEqualTo(companyChecklistEntity);
		assertThat(employeeChecklistEntityCaptor.getValue().getCustomFulfilments()).isNullOrEmpty();
		assertThat(employeeChecklistEntityCaptor.getValue().getCustomTasks()).isNullOrEmpty();
		assertThat(employeeChecklistEntityCaptor.getValue().getFulfilments()).isNullOrEmpty();
		assertThat(result).isEqualTo("Employee with loginname userName processed successfully.");
	}

	@AfterEach
	void assertNoMoreInteractions() {
		verifyNoMoreInteractions(employeeRepositoryMock, managerRepositoryMock, employeeChecklistsRepositoryMock, organizationRepositoryMock, delegateRepositoryMock);
	}
}
