package se.sundsvall.checklist.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklistParameters;
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
import se.sundsvall.checklist.service.model.Employee;
import se.sundsvall.checklist.service.model.Employment;
import se.sundsvall.checklist.service.model.Manager;

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

	@Mock
	private Page<EmployeeChecklistEntity> pagedEmployeeChecklistMock;

	@InjectMocks
	private EmployeeChecklistIntegration integration;

	@Captor
	private ArgumentCaptor<EmployeeEntity> employeeEntityCaptor;

	@Captor
	private ArgumentCaptor<EmployeeChecklistEntity> employeeChecklistEntityCaptor;

	@Captor
	private ArgumentCaptor<MentorEntity> mentorEntityCaptor;

	@AfterEach
	void assertNoMoreInteractions() {
		verifyNoMoreInteractions(employeeRepositoryMock, managerRepositoryMock, employeeChecklistsRepositoryMock, organizationRepositoryMock, delegateRepositoryMock, customTaskRepositoryMock, phaseRepositoryMock, pagedEmployeeChecklistMock);
	}

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
		final var managerId = UUID.randomUUID().toString();
		final var jobTitle = "jobTitle";
		final var entity = EmployeeEntity.builder()
			.withManager(ManagerEntity.builder()
				.withPersonId(UUID.randomUUID().toString()) // Simulate old manager uuid
				.build())
			.build();

		final var employment = Employment.builder()
			.withTitle(jobTitle)
			.withIsMainEmployment(true)
			.withManager(Manager.builder()
				.withPersonId(managerId)
				.build())
			.build();
		final var employee = Employee.builder()
			.withMainEmployment(employment)
			.build();

		when(managerRepositoryMock.findById(managerId)).thenReturn(Optional.of(ManagerEntity.builder().withPersonId(managerId).build()));

		// Act
		integration.updateEmployeeInformation(entity, employee);

		// Verify and assert
		verify(managerRepositoryMock).findById(managerId);
		verify(employeeRepositoryMock).save(employeeEntityCaptor.capture());
		assertThat(employeeEntityCaptor.getValue().getManager().getPersonId()).isEqualTo(managerId);
		assertThat(employeeEntityCaptor.getValue().getTitle()).isEqualTo(jobTitle);
	}

	@Test
	void updateEmployeeInformationWhenManagerNotExists() {
		// Arrange
		final var oldManagerId = UUID.randomUUID().toString();
		final var newManagerId = UUID.randomUUID().toString();
		final var entity = EmployeeEntity.builder()
			.withManager(ManagerEntity.builder()
				.withPersonId(oldManagerId)
				.build())
			.build();
		final var employment = Employment.builder()
			.withIsMainEmployment(true)
			.withManager(Manager.builder()
				.withPersonId(newManagerId)
				.build())
			.build();
		final var employee = Employee.builder()
			.withMainEmployment(employment)
			.build();

		// Act
		integration.updateEmployeeInformation(entity, employee);

		// Verify and assert
		verify(managerRepositoryMock).findById(newManagerId);
		verify(employeeRepositoryMock).save(employeeEntityCaptor.capture());
		assertThat(employeeEntityCaptor.getValue().getManager().getPersonId()).isEqualTo(newManagerId);
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

	@ParameterizedTest
	@EnumSource(value = FulfilmentStatus.class, mode = Mode.INCLUDE, names = "TRUE")
	void updateAllTasksInPhaseWhenNoPresentFulfilmentExistsLeedingToChecklistCompleted(FulfilmentStatus fulfilmentStatus) {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();

		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(fulfilmentStatus)
			.build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(taskId)
					.withPhase(PhaseEntity.builder()
						.withId(phaseId)
						.build())
					.build()))
				.build()))
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withId(customTaskId)
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
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(result.getFulfilments()).hasSize(1).allSatisfy(f -> {
			assertThat(f).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "task", "completed");
			assertThat(f.getCompleted()).isEqualTo(fulfilmentStatus);
		});
		assertThat(result.getCustomFulfilments()).hasSize(1).allSatisfy(f -> {
			assertThat(f).hasAllNullFieldsOrPropertiesExcept("employeeChecklist", "customTask", "completed");
			assertThat(f.getCompleted()).isEqualTo(fulfilmentStatus);
		});

		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isTrue();

	}

	@ParameterizedTest
	@EnumSource(value = FulfilmentStatus.class, mode = Mode.EXCLUDE, names = "TRUE")
	void updateAllTasksInPhaseWhenPresentFulfilmentExistsandChecklistNotCompleted(FulfilmentStatus fulfilmentStatus) {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var updatedBy = "updatedBy";
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(fulfilmentStatus)
			.withUpdatedBy(updatedBy)
			.build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(taskId)
					.withPhase(PhaseEntity.builder()
						.withId(phaseId)
						.build())
					.build()))
				.build()))
			.withCustomTasks(List.of(
				CustomTaskEntity.builder()
					.withId(customTaskId)
					.withPhase(PhaseEntity.builder()
						.withId(phaseId)
						.build())
					.build(),
				CustomTaskEntity.builder()
					.withId(UUID.randomUUID().toString())
					.withPhase(PhaseEntity.builder()
						.withId(UUID.randomUUID().toString())
						.build())
					.build()))
			.build();

		entity.setFulfilments(List.of(FulfilmentEntity.builder()
			.withEmployeeChecklist(entity)
			.withCompleted(FulfilmentStatus.EMPTY)
			.withTask(entity.getChecklists().getFirst().getTasks().getFirst())
			.build()));
		entity.setCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
			.withEmployeeChecklist(entity)
			.withCompleted(FulfilmentStatus.EMPTY)
			.withCustomTask(entity.getCustomTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));
		when(employeeChecklistsRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		final var result = integration.updateAllFulfilmentForAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

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

		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isFalse();
	}

	@Test
	void updateAllTasksInPhaseToEmptyWhenChecklistCompletedSinceEariler() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var fulfilmentStatus = FulfilmentStatus.EMPTY;
		final var customTaskId = UUID.randomUUID().toString();
		final var updatedBy = "updatedBy";
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(fulfilmentStatus)
			.withUpdatedBy(updatedBy)
			.build();
		final var entity = EmployeeChecklistEntity.builder()
			.withCompleted(true)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(taskId)
					.withPhase(PhaseEntity.builder()
						.withId(phaseId)
						.build())
					.build()))
				.build()))
			.withCustomTasks(List.of(
				CustomTaskEntity.builder()
					.withId(customTaskId)
					.withPhase(PhaseEntity.builder()
						.withId(phaseId)
						.build())
					.build(),
				CustomTaskEntity.builder()
					.withId(UUID.randomUUID().toString())
					.withPhase(PhaseEntity.builder()
						.withId(UUID.randomUUID().toString())
						.build())
					.build()))
			.build();

		entity.setFulfilments(List.of(FulfilmentEntity.builder()
			.withEmployeeChecklist(entity)
			.withCompleted(FulfilmentStatus.TRUE)
			.withTask(entity.getChecklists().getFirst().getTasks().getFirst())
			.build()));
		entity.setCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
			.withEmployeeChecklist(entity)
			.withCompleted(FulfilmentStatus.FALSE)
			.withCustomTask(entity.getCustomTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));
		when(employeeChecklistsRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		final var result = integration.updateAllFulfilmentForAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

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

		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isTrue();
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

	@ParameterizedTest
	@EnumSource(value = FulfilmentStatus.class, mode = Mode.INCLUDE, names = "TRUE")
	void updateCommonTaskFulfilmentWhenNoPresentFulfilmentExistsLeedingToChecklistCompleted(FulfilmentStatus fulfilmentStatus) {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
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
						.withId(UUID.randomUUID().toString())
						.build())
					.build()))
				.build()))
			.build();

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCommonTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(result).isEqualTo(entity.getFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);
		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = FulfilmentStatus.class, mode = Mode.EXCLUDE, names = "TRUE")
	void updateCommonTaskFulfilmentWhenPresentFulfilmentExistsandChecklistNotCompleted(FulfilmentStatus fulfilmentStatus) {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(
					TaskEntity.builder()
						.withId(taskId)
						.withPhase(PhaseEntity.builder()
							.build())
						.build(),
					TaskEntity.builder()
						.withId(UUID.randomUUID().toString())
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
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(result).isEqualTo(entity.getFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);
		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isFalse();
	}

	@Test
	void updateCommonTaskFulfilmentToEmptyWhenChecklistCompletedSinceEariler() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(FulfilmentStatus.EMPTY)
			.withResponseText(responseText)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withCompleted(true)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(
					TaskEntity.builder()
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
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(result).isEqualTo(entity.getFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(FulfilmentStatus.EMPTY);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);
		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isTrue();
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

	@ParameterizedTest
	@EnumSource(value = FulfilmentStatus.class, mode = Mode.INCLUDE, names = "TRUE")
	void updateCustomTaskFulfilmentWhenNoPresentFulfilmentExistsLeedingToChecklistCompleted(FulfilmentStatus fulfilmentStatus) {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
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
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(result).isEqualTo(entity.getCustomFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);
		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = FulfilmentStatus.class, mode = Mode.EXCLUDE, names = "TRUE")
	void updateCustomTaskFulfilmentWhenPresentFulfilmentExistsAndChecklistNotCompleted(FulfilmentStatus fulfilmentStatus) {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(
				CustomTaskEntity.builder()
					.withId(taskId)
					.build(),
				CustomTaskEntity.builder()
					.withId(UUID.randomUUID().toString())
					.build()))
			.build();

		entity.setCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
			.withCustomTask(entity.getCustomTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCustomTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(result).isEqualTo(entity.getCustomFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);
		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isFalse();
	}

	@Test
	void updateCustomTaskFulfilmentToEmptyWhenChecklistCompletedSinceEariler() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var responseText = "responseText";
		final var updatedBy = "updatedBy";

		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(FulfilmentStatus.EMPTY)
			.withResponseText(responseText)
			.withUpdatedBy(updatedBy)
			.build();

		final var entity = EmployeeChecklistEntity.builder()
			.withCompleted(true)
			.withCustomTasks(List.of(
				CustomTaskEntity.builder()
					.withId(taskId)
					.build(),
				CustomTaskEntity.builder()
					.withId(UUID.randomUUID().toString())
					.build()))
			.build();

		entity.setCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
			.withCustomTask(entity.getCustomTasks().getFirst())
			.build()));

		when(employeeChecklistsRepositoryMock.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)).thenReturn(Optional.of(entity));

		// Act
		final var result = integration.updateCustomTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId);
		verify(employeeChecklistsRepositoryMock).save(employeeChecklistEntityCaptor.capture());

		assertThat(result).isEqualTo(entity.getCustomFulfilments().getFirst());
		assertThat(result.getCompleted()).isEqualTo(FulfilmentStatus.EMPTY);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getLastSavedBy()).isEqualTo(updatedBy);
		assertThat(employeeChecklistEntityCaptor.getValue().isCompleted()).isTrue();
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
		final var uuid = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withLoginname(username)
			.withPersonId(uuid)
			.build();

		when(employeeRepositoryMock.existsById(uuid)).thenReturn(true);

		// Act
		final var result = integration.initiateEmployee(municipalityId, employee, null);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(uuid);
		assertThat(result).isEqualTo("Employee with loginname username already has an employee checklist.");
	}

	@Test
	void initiateEmployeeWithNoMainEmployment() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var uuid = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withLoginname(username)
			.withPersonId(uuid)
			.build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, null));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(uuid);
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No main employment found for employee with loginname username.");
	}

	@Test
	void initiateEmployeeWhenOrganizationIsNotPresentInDatabase() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var username = "username";
		final var employeeUuid = UUID.randomUUID().toString();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withLoginname(username)
			.withPersonId(employeeUuid)
			.withMainEmployment(Employment.builder()
				.withOrgId(orgId)
				.withOrgName(orgName)
				.withCompanyId(companyId)
				.withIsMainEmployment(true)
				.withManager(Manager.builder()
					.withPersonId(managerUuid)
					.build())
				.build())
			.build();
		final var orgTree = OrganizationTree.map("1|" + companyId + "|Root¤2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);

		when(employeeRepositoryMock.save(any())).thenReturn(OrganizationMapper.toEmployeeEntity(employee));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid);
		verify(managerRepositoryMock).findById(managerUuid);
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
		final var employeeUuid = UUID.randomUUID().toString();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID().toString();
		final var organizationUuid = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withLoginname(username)
			.withPersonId(employeeUuid)
			.withMainEmployment(Employment.builder()
				.withIsManager(true)
				.withOrgId(orgId)
				.withOrgName(orgName)
				.withCompanyId(companyId)
				.withIsMainEmployment(true)
				.withManager(Manager.builder()
					.withPersonId(managerUuid)
					.build())
				.build())
			.build();
		final var orgTree = OrganizationTree.map("1|" + companyId + "|Root¤2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);
		final var organization = OrganizationEntity.builder()
			.withId(organizationUuid)
			.withOrganizationNumber(companyId)
			.build();

		when(employeeRepositoryMock.save(any())).thenReturn(OrganizationMapper.toEmployeeEntity(employee));
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(companyId, municipalityId)).thenReturn(Optional.of(organization));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid);
		verify(managerRepositoryMock).findById(managerUuid);
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
		final var employeeUuid = UUID.randomUUID().toString();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID().toString();
		final var organizationUuid = UUID.randomUUID().toString();
		final var checklistUuid = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withLoginname(username)
			.withPersonId(employeeUuid)
			.withMainEmployment(Employment.builder()
				.withOrgId(orgId)
				.withOrgName(orgName)
				.withCompanyId(companyId)
				.withIsMainEmployment(true)
				.withManager(Manager.builder()
					.withPersonId(managerUuid)
					.build())
				.build())
			.build();
		final var orgTree = OrganizationTree.map("1|" + companyId + "|Root¤2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);
		final var organization = OrganizationEntity.builder()
			.withId(organizationUuid)
			.withOrganizationNumber(companyId)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withId(checklistUuid)
				.withLifeCycle(LifeCycle.DEPRECATED)
				.build()))
			.build();

		when(employeeRepositoryMock.save(any())).thenReturn(OrganizationMapper.toEmployeeEntity(employee));
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(companyId, municipalityId)).thenReturn(Optional.of(organization));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> integration.initiateEmployee(municipalityId, employee, orgTree));

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid);
		verify(managerRepositoryMock).findById(managerUuid);
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
		final var employeeUuid = UUID.randomUUID().toString();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID().toString();
		final var organizationUuid = UUID.randomUUID().toString();
		final var checklistUuid = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withLoginname(username)
			.withPersonId(employeeUuid)
			.withMainEmployment(Employment.builder()
				.withOrgId(orgId)
				.withOrgName(orgName)
				.withCompanyId(companyId)
				.withIsMainEmployment(true)
				.withManager(Manager.builder()
					.withPersonId(managerUuid)
					.build())
				.build())
			.build();
		final var orgTree = OrganizationTree.map("1|" + companyId + "|Root¤2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);
		final var checklistEntity = ChecklistEntity.builder()
			.withId(checklistUuid)
			.withLifeCycle(LifeCycle.ACTIVE)
			.build();
		final var companyEntity = OrganizationEntity.builder()
			.withId(organizationUuid)
			.withOrganizationNumber(companyId)
			.withChecklists(List.of(checklistEntity))
			.build();
		final var employeeEntity = OrganizationMapper.toEmployeeEntity(employee);

		when(employeeRepositoryMock.save(any())).thenReturn(employeeEntity);
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(companyId, municipalityId)).thenReturn(Optional.of(companyEntity));

		// Act
		final var result = integration.initiateEmployee(municipalityId, employee, orgTree);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid);
		verify(managerRepositoryMock).findById(managerUuid);
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
		final var employeeUuid = UUID.randomUUID().toString();
		final var companyId = 2;
		final var orgId = 2124;
		final var orgName = "orgName";
		final var managerUuid = UUID.randomUUID().toString();
		final var companyUuid = UUID.randomUUID().toString();
		final var departmentUuid = UUID.randomUUID().toString();
		final var checklistUuid = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withLoginname(username)
			.withPersonId(employeeUuid)
			.withMainEmployment(Employment.builder()
				.withOrgId(orgId)
				.withOrgName(orgName)
				.withCompanyId(companyId)
				.withIsMainEmployment(true)
				.withManager(Manager.builder()
					.withPersonId(managerUuid)
					.build())
				.build())
			.build();
		final var orgTree = OrganizationTree.map("1|" + companyId + "|Root¤2|21|Level-2¤3|212|Level-3¤4|" + orgId + "|" + orgName);
		final var companyChecklistEntity = ChecklistEntity.builder()
			.withId(checklistUuid)
			.withLifeCycle(LifeCycle.ACTIVE)
			.build();
		final var departmentChecklistEntity = ChecklistEntity.builder()
			.withId(checklistUuid)
			.withLifeCycle(LifeCycle.ACTIVE)
			.build();
		final var companyEntity = OrganizationEntity.builder()
			.withId(companyUuid)
			.withOrganizationNumber(companyId)
			.withChecklists(List.of(companyChecklistEntity))
			.build();
		final var departmentEntity = OrganizationEntity.builder()
			.withId(departmentUuid)
			.withOrganizationNumber(orgId)
			.withOrganizationName(orgName)
			.withChecklists(List.of(departmentChecklistEntity))
			.build();
		final var managerEntity = ManagerEntity.builder()
			.withPersonId(managerUuid)
			.build();

		final var employeeEntity = OrganizationMapper.toEmployeeEntity(employee);

		when(employeeRepositoryMock.save(any())).thenReturn(employeeEntity);
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(companyId, municipalityId)).thenReturn(Optional.of(companyEntity));
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(orgId, municipalityId)).thenReturn(Optional.of(departmentEntity));
		when(managerRepositoryMock.findById(managerUuid)).thenReturn(Optional.of(managerEntity));

		// Act
		final var result = integration.initiateEmployee(municipalityId, employee, orgTree);

		// Verify and assert
		verify(employeeRepositoryMock).existsById(employeeUuid);
		verify(managerRepositoryMock).findById(managerUuid);
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

	@Test
	void fetchAllOngoingEmployeeChecklists() {
		// Arrange
		final var page = PageRequest.of(2, 3);
		final var result = List.of(EmployeeChecklistEntity.builder().build());
		final var parameters = new OngoingEmployeeChecklistParameters()
			.withEmployeeName("employeeName")
			.withMunicipalityId("municipalityId");

		when(pagedEmployeeChecklistMock.getContent()).thenReturn(result);
		when(employeeChecklistsRepositoryMock.findAllByOngoingEmployeeChecklistParameters(any(), any())).thenReturn(pagedEmployeeChecklistMock);

		// Act
		final var response = integration.fetchAllOngoingEmployeeChecklists(parameters, page);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findAllByOngoingEmployeeChecklistParameters(parameters, page);
		assertThat(response.getContent()).isEqualTo(result);
	}

	@Test
	void findOngoingChecklists() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var result = List.of(EmployeeChecklistEntity.builder().build());

		when(employeeChecklistsRepositoryMock.findAllByChecklistsMunicipalityIdAndCompletedFalse(municipalityId)).thenReturn(result);

		// Act
		final var response = integration.findOngoingChecklists(municipalityId);

		// Verify and assert
		verify(employeeChecklistsRepositoryMock).findAllByChecklistsMunicipalityIdAndCompletedFalse(municipalityId);
		assertThat(response).isEqualTo(result);
	}
}
