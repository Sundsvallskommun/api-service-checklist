package se.sundsvall.checklist.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
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
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.MentorEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeRepository;
import se.sundsvall.checklist.integration.db.repository.ManagerRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;
import se.sundsvall.checklist.service.OrganizationTree;
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

	@Mock
	private PhaseRepository phaseRepositoryMock;

	@InjectMocks
	private EmployeeChecklistIntegration integration;

	@Captor
	private ArgumentCaptor<EmployeeEntity> employeeEntityCaptor;

	@Captor
	private ArgumentCaptor<EmployeeChecklistEntity> employeeChecklistEntityCaptor;

	@Captor
	private ArgumentCaptor<MentorEntity> mentorEntityCaptor;

	@Test
	void fetchOptionalEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findByChecklistsMunicipalityIdAndEmployeeUsername(municipalityId, username)).thenReturn(entity);

		final var result = integration.fetchOptionalEmployeeChecklist(municipalityId, username);

		assertThat(result).isEqualTo(Optional.of(entity));
		verify(employeeChecklistsRepositoryMock).findByChecklistsMunicipalityIdAndEmployeeUsername(municipalityId, username);
	}

	@Test
	void fetchNonExistingOptionalEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";

		// Act, assert and verify
		assertThat(integration.fetchOptionalEmployeeChecklist(municipalityId, username)).isEmpty();
		verify(employeeChecklistsRepositoryMock).findByChecklistsMunicipalityIdAndEmployeeUsername(municipalityId, username);
	}

	@Test
	void fetchEmployeeChecklistsForManager() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var userId = "userId";
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findAllByChecklistsMunicipalityIdAndEmployeeManagerUsername(municipalityId, userId)).thenReturn(List.of(entity));

		// Act
		final var result = integration.fetchEmployeeChecklistsForManager(municipalityId, userId);

		// Verify and assert
		assertThat(result).isNotEmpty().containsExactly(entity);
		verify(employeeChecklistsRepositoryMock).findAllByChecklistsMunicipalityIdAndEmployeeManagerUsername(municipalityId, userId);
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
		final var municipalityId = "municipalityId";
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

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateAllFulfilmentForAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void updateAllTasksInPhaseWhenNoPresentFulfilmentExists() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.EMPTY;
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(fulfilmentStatus)
			.build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withPhase(PhaseEntity.builder()
						.withId(phaseId)
						.build())
					.build()))
				.build()))
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withPhase(PhaseEntity.builder()
					.withId(phaseId)
					.build())
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));
		when(employeeChecklistsRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		final var result = integration.updateAllFulfilmentForAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
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
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.EMPTY;
		final var updatedBy = "updatedBy";
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(fulfilmentStatus)
			.withUpdatedBy(updatedBy)
			.build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withPhase(PhaseEntity.builder()
						.withId(phaseId)
						.build())
					.build()))
				.build()))
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withPhase(PhaseEntity.builder()
					.withId(phaseId)
					.build())
				.build()))
			.build();

		entity.setFulfilments(List.of(FulfilmentEntity.builder()
			.withEmployeeChecklist(entity)
			.withTask(entity.getChecklists().getFirst().getTasks().getFirst())
			.build()));
		entity.setCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
			.withEmployeeChecklist(entity)
			.withCustomTask(entity.getCustomTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));
		when(employeeChecklistsRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		final var result = integration.updateAllFulfilmentForAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(entity);

		assertThat(result.getFulfilments()).hasSize(1).allSatisfy(f -> {
			assertThat(f).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "task", "completed", "lastSavedBy");
			assertThat(f.getCompleted()).isEqualTo(fulfilmentStatus);
			assertThat(f.getLastSavedBy()).isEqualTo(updatedBy);
		});
		assertThat(result.getCustomFulfilments()).hasSize(1).allSatisfy(f -> {
			assertThat(f).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "customTask", "completed", "lastSavedBy");
			assertThat(f.getCompleted()).isEqualTo(fulfilmentStatus);
			assertThat(f.getLastSavedBy()).isEqualTo(updatedBy);
		});
	}

	@Test
	void updateAllTasksInPhaseWhenFulfilmentStatusNotSetInRequest() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistPhaseUpdateRequest.builder().build();
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateAllFulfilmentForAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		assertThat(result).isEqualTo(entity);
	}

	@Test
	void updateAllTasksInPhaseForNonExistingEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistPhaseUpdateRequest.builder().build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateAllFulfilmentForAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found within municipality %s.".formatted(employeeChecklistId, municipalityId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void fetchEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.fetchEmployeeChecklist(municipalityId, employeeChecklistId);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		assertThat(result).isEqualTo(entity);
	}

	@Test
	void fetchNonExistingEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.fetchEmployeeChecklist(municipalityId, employeeChecklistId));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found within municipality %s.".formatted(employeeChecklistId, municipalityId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void updateCommonTaskFulfilmentWhenNoPresentFulfilmentsExists() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(taskId)
					.withPhase(PhaseEntity.builder()
						.build())
					.build()))
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCommonTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);

		// Verify and assert
		assertThat(result).isEqualTo(entity.getFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(entity);
	}

	@Test
	void updateCommonTaskFulfilmentWhenPresentFulfilmentsExists() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(taskId)
					.withPhase(PhaseEntity.builder()
						.build())
					.build()))
				.build()))
			.build();

		entity.setFulfilments(List.of(FulfilmentEntity.builder()
			.withTask(entity.getChecklists().getFirst().getTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCommonTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);

		// Verify and assert
		assertThat(result).isEqualTo(entity.getFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(entity);
	}

	@Test
	void updateCommonTaskFulfilmentForNonExistingTask() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(UUID.randomUUID().toString())
					.withPhase(PhaseEntity.builder()
						.build())
					.build()))
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateCommonTaskFulfilment(municipalityId, employeeChecklistId, taskId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No fulfilment information found for task with id %s in employee checklist with id %s.".formatted(taskId, employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void updateCommonTaskFulfilmentOnNonExistingEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateCommonTaskFulfilment(municipalityId, employeeChecklistId, taskId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found within municipality %s.".formatted(employeeChecklistId, municipalityId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void updateCustomTaskFulfilmentWhenNoPresentFulfilmentsExists() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withId(taskId)
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCustomTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);

		// Verify and assert
		assertThat(result).isEqualTo(entity.getCustomFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(entity);
	}

	@Test
	void updateCustomTaskFulfilmentWhenPresentFulfilmentsExists() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withId(taskId)
				.build()))
			.build();

		entity.setCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
			.withCustomTask(entity.getCustomTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCustomTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);

		// Verify and assert
		assertThat(result).isEqualTo(entity.getCustomFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(entity);
	}

	@Test
	void updateCustomTaskFulfilmentForNonExistingTask() {
		// Arrange
		final var municipalityId = "municipalityId";
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

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateCustomTaskFulfilment(municipalityId, employeeChecklistId, taskId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No fulfilment information found for task with id %s in employee checklist with id %s.".formatted(taskId, employeeChecklistId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void updateCustomTaskFulfilmentOnNonExistingEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.updateCustomTaskFulfilment(municipalityId, employeeChecklistId, taskId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found within municipality %s.".formatted(employeeChecklistId, municipalityId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void deleteEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var managerEntity = ManagerEntity.builder()
			.build();
		final var employeeEntity = EmployeeEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withManager(managerEntity)
			.build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employeeEntity)
			.build();
		managerEntity.getEmployees().add(employeeEntity);

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(employeeChecklistEntity));

		// Act
		integration.deleteEmployeeChecklist(municipalityId, employeeChecklistId);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(delegateRepositoryMock).deleteByEmployeeChecklist(employeeChecklistEntity);
		verify(employeeChecklistsRepositoryMock).delete(employeeChecklistEntity);
		verify(employeeRepositoryMock).delete(employeeEntity);
		assertThat(managerEntity.getEmployees()).isEmpty();
	}

	@Test
	void setMentor() {
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var entity = EmployeeChecklistEntity.builder().build();
		final var mentor = Mentor.builder()
			.withUserId("someUserId")
			.withName("someName")
			.build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		integration.setMentor(municipalityId, employeeChecklistId, mentor);

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(employeeChecklistEntityCaptor.getValue()).satisfies(savedEmployeeChecklistEntity -> assertThat(savedEmployeeChecklistEntity.getMentor()).satisfies(mentorEntity -> {
			assertThat(mentorEntity.getUserId()).isEqualTo(mentor.getUserId());
			assertThat(mentorEntity.getName()).isEqualTo(mentor.getName());
		}));
	}

	@Test
	void deleteMentor() {
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var entity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		integration.deleteMentor(municipalityId, employeeChecklistId);

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(employeeChecklistEntityCaptor.getValue()).satisfies(savedEmployeeChecklistEntity -> assertThat(savedEmployeeChecklistEntity.getMentor()).isNull());
	}

	@Test
	void deleteNonExistingEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.deleteEmployeeChecklist(municipalityId, employeeChecklistId));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found within municipality %s.".formatted(employeeChecklistId, municipalityId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void createCustomTask() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().withSortOrder(1).build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));
		when(phaseRepositoryMock.findByIdAndMunicipalityId(phaseId, municipalityId)).thenReturn(Optional.of(PhaseEntity.builder().withId(phaseId).build()));

		// Act
		final var result = integration.createCustomTask(municipalityId, employeeChecklistId, phaseId, request);

		// Verify and assert
		assertThat(result).isNotNull().isInstanceOf(CustomTaskEntity.class);

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(phaseRepositoryMock).findByIdAndMunicipalityId(phaseId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(entity);
		verify(customTaskRepositoryMock).save(result);
	}

	@Test
	void createCustomTaskForNonExistingEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().withSortOrder(1).build();

		final var e = assertThrows(ThrowableProblem.class, () -> integration.createCustomTask(municipalityId, employeeChecklistId, phaseId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s was not found within municipality %s.".formatted(employeeChecklistId, municipalityId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
	}

	@Test
	void createCustomTaskForNonExistingPhase() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().withSortOrder(1).build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		final var e = assertThrows(ThrowableProblem.class, () -> integration.createCustomTask(municipalityId, employeeChecklistId, phaseId, request));

		// Verify and assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Phase with id %s was not found within municipality %s.".formatted(phaseId, municipalityId));

		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(phaseRepositoryMock).findByIdAndMunicipalityId(phaseId, municipalityId);
	}

	@Test
	void initiateEmployeeWithExistingEmployeeChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var uuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(username)
			.personId(uuid);

		when(employeeRepositoryMock.existsById(uuid.toString())).thenReturn(true);

		// Act
		final var result = integration.initiateEmployee(municipalityId, employee, null);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(uuid.toString());
		assertThat(result).isEqualTo("Employee with loginname username already has an employee checklist.");
	}

	@Test
	void initiateEmployeeWithNoMainEmployment() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var uuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(username)
			.personId(uuid)
			.addEmploymentsItem(
				new Employment()
					.isMainEmployment(false)
					.formOfEmploymentId("1"));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, null));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(uuid.toString());
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No main employment found for employee with loginname username.");
	}

	@Test
	void initiateEmployeeNotValidForChecklist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var uuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(username)
			.personId(uuid)
			.addEmploymentsItem(
				new Employment()
					.isMainEmployment(true)
					.formOfEmploymentId("999"));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, null));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(uuid.toString());
		assertThat(e.getStatus()).isEqualTo(Status.NOT_ACCEPTABLE);
		assertThat(e.getMessage()).isEqualTo("Not Acceptable: Employee with loginname username does not have an employment type that validates for creating an employee checklist.");
	}

	@Test
	void initiateEmployeeWhenOrganizationIsNotPresentInDatabase() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(username)
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
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(2, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(21, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(212, municipalityId);
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(2124, municipalityId);

		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No checklist was found for any id in the organization tree for employee username. Search has been performed for id 2, 21, 212 and 2124.");
	}

	@Test
	void initiateEmployeeWhenOrganizationChecklistIsNotPresentInDatabase() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var organizationUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(username)
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
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(companyId, municipalityId)).thenReturn(Optional.of(organization));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(2, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(21, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(212, municipalityId);
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(2124, municipalityId);

		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No checklist was found for any id in the organization tree for employee username. Search has been performed for id 2, 21, 212 and 2124.");
	}

	@Test
	void initiateEmployeeWhenOrganizationActiveChecklistIsNotPresentInDatabase() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var organizationUuid = UUID.randomUUID();
		final var checklistUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(username)
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
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(companyId, municipalityId)).thenReturn(Optional.of(organization));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(2, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(21, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(212, municipalityId);
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(2124, municipalityId);

		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No checklist was found for any id in the organization tree for employee username. Search has been performed for id 2, 21, 212 and 2124.");
	}

	@Test
	@DisplayName("Initiation of employee belonging to a new department and a new manager, which will add department and manager entities in the database")
	void initiateEmployeeWithNewStructure() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var organizationUuid = UUID.randomUUID();
		final var checklistUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(username)
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
			.build();
		final var companyEntity = OrganizationEntity.builder()
			.withId(organizationUuid.toString())
			.withOrganizationNumber(companyId)
			.withChecklists(List.of(checklistEntity))
			.build();
		final var employeeEntity = OrganizationMapper.toEmployeeEntity(employee);

		when(employeeRepositoryMock.save(any())).thenReturn(employeeEntity);
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(companyId, municipalityId)).thenReturn(Optional.of(companyEntity));

		// Act
		final var result = integration.initiateEmployee(municipalityId, employee, orgTree);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(employeeRepositoryMock).save(employeeEntityCaptor.capture());
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(orgId, municipalityId);
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(companyId, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(212, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(21, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(employeeEntityCaptor.getValue().getDepartment()).isNotNull();
		assertThat(employeeEntityCaptor.getValue().getDepartment().getId()).isNull();
		assertThat(employeeEntityCaptor.getValue().getCompany()).isEqualTo(companyEntity);
		assertThat(employeeEntityCaptor.getValue().getEmployeeChecklist()).isNull();
		assertThat(employeeEntityCaptor.getValue().getManager()).isNotNull();
		assertThat(employeeChecklistEntityCaptor.getValue().getEmployee()).isEqualTo(employeeEntity);
		assertThat(employeeChecklistEntityCaptor.getValue().getChecklists()).hasSize(1).containsExactly(checklistEntity);
		assertThat(employeeChecklistEntityCaptor.getValue().getCustomFulfilments()).isNullOrEmpty();
		assertThat(employeeChecklistEntityCaptor.getValue().getCustomTasks()).isNullOrEmpty();
		assertThat(employeeChecklistEntityCaptor.getValue().getFulfilments()).isNullOrEmpty();
		assertThat(result).isEqualTo("Employee with loginname username processed successfully.");
	}

	@Test
	@DisplayName("Initiation of employee belonging to an existing department and manager, which will not add department and manager entities in the database and will use nearest checklist (department) to employee")
	void initiateEmployeeWithExistingStructure() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var employeeUuid = UUID.randomUUID();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID();
		final var companyUuid = UUID.randomUUID();
		final var departmentUuid = UUID.randomUUID();
		final var checklistUuid = UUID.randomUUID();
		final var employee = new Employee()
			.loginname(username)
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
			.build();
		final var departmentChecklistEntity = ChecklistEntity.builder()
			.withId(checklistUuid.toString())
			.withLifeCycle(LifeCycle.ACTIVE)
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
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(companyId, municipalityId)).thenReturn(Optional.of(companyEntity));
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(orgId, municipalityId)).thenReturn(Optional.of(departmentEntity));
		when(managerRepositoryMock.findById(managerUuid.toString())).thenReturn(Optional.of(managerEntity));

		// Act
		final var result = integration.initiateEmployee(municipalityId, employee, orgTree);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid.toString());
		verify(managerRepositoryMock).findById(managerUuid.toString());
		verify(employeeRepositoryMock).save(employeeEntityCaptor.capture());
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(orgId, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(212, municipalityId);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(21, municipalityId);
		verify(organizationRepositoryMock, times(2)).findByOrganizationNumberAndMunicipalityId(companyId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(employeeEntityCaptor.getValue().getDepartment()).isEqualTo(departmentEntity);
		assertThat(employeeEntityCaptor.getValue().getCompany()).isEqualTo(companyEntity);
		assertThat(employeeEntityCaptor.getValue().getManager()).isEqualTo(managerEntity);
		assertThat(employeeEntityCaptor.getValue().getEmployeeChecklist()).isNull();
		assertThat(employeeChecklistEntityCaptor.getValue().getEmployee()).isEqualTo(employeeEntity);
		assertThat(employeeChecklistEntityCaptor.getValue().getChecklists()).hasSize(2).containsExactlyInAnyOrder(companyChecklistEntity, departmentChecklistEntity);
		assertThat(employeeChecklistEntityCaptor.getValue().getCustomFulfilments()).isNullOrEmpty();
		assertThat(employeeChecklistEntityCaptor.getValue().getCustomTasks()).isNullOrEmpty();
		assertThat(employeeChecklistEntityCaptor.getValue().getFulfilments()).isNullOrEmpty();
		assertThat(result).isEqualTo("Employee with loginname username processed successfully.");
	}

	@AfterEach
	void assertNoMoreInteractions() {
		verifyNoMoreInteractions(employeeRepositoryMock, managerRepositoryMock, employeeChecklistsRepositoryMock, organizationRepositoryMock, delegateRepositoryMock, phaseRepositoryMock);
	}
}
