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

import generated.se.sundsvall.employee.PortalPersonData;
import generated.se.sundsvall.mdviewer.Organization;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.api.model.CustomTask;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklistParameters;
import se.sundsvall.checklist.integration.db.EmployeeChecklistIntegration;
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
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.InitiationRepository;
import se.sundsvall.checklist.integration.employee.EmployeeIntegration;
import se.sundsvall.checklist.integration.mdviewer.MDViewerClient;
import se.sundsvall.checklist.service.OrganizationTree.OrganizationLine;
import se.sundsvall.checklist.service.model.Employee;
import se.sundsvall.checklist.service.model.Employment;
import se.sundsvall.checklist.service.model.Manager;

@ExtendWith(MockitoExtension.class)
class EmployeeChecklistServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private EmployeeChecklistIntegration employeeChecklistIntegrationMock;

	@Mock
	private EmployeeIntegration employeeIntegrationMock;

	@Mock
	private CustomTaskRepository customTaskRepositoryMock;

	@Mock
	private SortorderService sortorderServiceMock;

	@Mock
	private InitiationRepository initiationRepositoryMock;

	@Mock
	private MDViewerClient mdViewerClientMock;

	@InjectMocks
	private EmployeeChecklistService service;

	@Captor
	private ArgumentCaptor<OrganizationTree> organizationTreeCaptor;

	@BeforeEach
	void initializeFields() {
		ReflectionTestUtils.setField(service, "employeeInformationUpdateInterval", Duration.ofDays(1));
	}

	@AfterEach
	void assertNoMoreInteractions() {
		verifyNoMoreInteractions(employeeChecklistIntegrationMock, customTaskRepositoryMock, employeeIntegrationMock, sortorderServiceMock, initiationRepositoryMock, mdViewerClientMock);
	}

	@Test
	void fetchOptionalEmployeeChecklist() {
		// Arrange
		final var username = "username";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var manager = ManagerEntity.builder().build();
		final var employee = EmployeeEntity.builder()
			.withManager(manager)
			.withUpdated(OffsetDateTime.now())
			.build();
		final var phase = PhaseEntity.builder().withId(UUID.randomUUID().toString()).build();
		final var managerTask = TaskEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.MANAGER_FOR_NEW_EMPLOYEE).withPhase(phase).build();
		final var employeeTask = TaskEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.NEW_EMPLOYEE).withPhase(phase).build();
		final var checklist = ChecklistEntity.builder().withTasks(List.of(managerTask, employeeTask)).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withEmployee(employee)
			.withChecklists(List.of(checklist))
			.build();
		final var customEmployeeTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.withRoleType(RoleType.NEW_EMPLOYEE)
			.withPhase(phase)
			.build();
		final var customManagerTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.withRoleType(RoleType.MANAGER_FOR_NEW_EMPLOYEE)
			.withPhase(phase)
			.build();

		when(employeeChecklistIntegrationMock.fetchOptionalEmployeeChecklist(MUNICIPALITY_ID, username)).thenReturn(Optional.of(employeeChecklistEntity));
		when(customTaskRepositoryMock.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID)).thenReturn(List.of(customEmployeeTask, customManagerTask));
		when(sortorderServiceMock.applySorting(any(), any())).thenAnswer(arg -> arg.getArgument(1));

		// Act
		final var employeeChecklist = service.fetchChecklistForEmployee(MUNICIPALITY_ID, username);

		// Assert and verify
		assertThat(employeeChecklist).isPresent();
		assertThat(employeeChecklist.get().getPhases()).hasSize(1).allSatisfy(ph -> {
			assertThat(ph.getTasks()).hasSize(2).allSatisfy(t -> {
				assertThat(t.getRoleType()).isEqualTo(RoleType.NEW_EMPLOYEE);
				assertThat(t.getFulfilmentStatus()).isEqualTo(FulfilmentStatus.EMPTY);
			});
		});
		verify(employeeChecklistIntegrationMock).fetchOptionalEmployeeChecklist(MUNICIPALITY_ID, username);
		verify(customTaskRepositoryMock).findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID);
		verify(employeeChecklistIntegrationMock).fetchDelegateEmails(employeeChecklistId);
		verify(sortorderServiceMock).applySorting(any(), any());
	}

	@Test
	void fetchEmployeeChecklistForEmployeeWithOldInformation() {
		// Arrange
		final var employeeId = UUID.randomUUID().toString();
		final var username = "username";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var employeeEntity = EmployeeEntity.builder()
			.withId(employeeId)
			.withUpdated(OffsetDateTime.now().minusDays(1).minusNanos(1))
			.build();
		final var checklist = ChecklistEntity.builder().build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withEmployee(employeeEntity)
			.withChecklists(List.of(checklist))
			.build();
		final var customTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.build();
		final var employee = Employee.builder().build();

		when(employeeChecklistIntegrationMock.fetchOptionalEmployeeChecklist(MUNICIPALITY_ID, username)).thenReturn(Optional.of(employeeChecklistEntity));
		when(customTaskRepositoryMock.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID)).thenReturn(List.of(customTask));
		when(employeeIntegrationMock.getEmployeeInformation(MUNICIPALITY_ID, employeeId)).thenReturn(List.of(employee));
		when(sortorderServiceMock.applySorting(any(), any())).thenAnswer(args -> args.getArgument(1));

		// Act
		final var employeeChecklist = service.fetchChecklistForEmployee(MUNICIPALITY_ID, username);

		// Assert and verify
		assertThat(employeeChecklist).isPresent();
		verify(employeeChecklistIntegrationMock).fetchOptionalEmployeeChecklist(MUNICIPALITY_ID, username);
		verify(customTaskRepositoryMock).findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID);
		verify(employeeChecklistIntegrationMock).fetchDelegateEmails(employeeChecklistId);
		verify(employeeIntegrationMock).getEmployeeInformation(MUNICIPALITY_ID, employeeId);
		verify(employeeChecklistIntegrationMock).updateEmployeeInformation(employeeEntity, employee);
		verify(sortorderServiceMock).applySorting(any(), any());
	}

	@Test
	void fetchNonExistentOptionalEmployeeChecklist() {
		// Arrange
		final var username = "username";

		// Act
		final var employeeChecklist = service.fetchChecklistForEmployee(MUNICIPALITY_ID, username);

		// Assert and verify
		assertThat(employeeChecklist).isEmpty();
		verify(employeeChecklistIntegrationMock).fetchOptionalEmployeeChecklist(MUNICIPALITY_ID, username);
	}

	@Test
	void fetchEmployeeChecklistsForManager() {
		// Arrange
		final var username = "username";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var manager = ManagerEntity.builder()
			.withUsername(username)
			.build();
		final var employee = EmployeeEntity.builder()
			.withManager(manager)
			.withUpdated(OffsetDateTime.now())
			.build();

		final var phase1 = PhaseEntity.builder().withId(UUID.randomUUID().toString()).build();
		final var phase2 = PhaseEntity.builder().withId(UUID.randomUUID().toString()).build();
		final var managerTask1 = TaskEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.MANAGER_FOR_NEW_EMPLOYEE).withPhase(phase1).build();
		final var managerTask2 = TaskEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.MANAGER_FOR_NEW_EMPLOYEE).withPhase(phase2).build();
		final var employeeTask = TaskEntity.builder().withId(UUID.randomUUID().toString()).withRoleType(RoleType.NEW_EMPLOYEE).withPhase(phase2).build();

		final var checklist = ChecklistEntity.builder()
			.withTasks(List.of(managerTask1, managerTask2, employeeTask))
			.build();

		final var customEmployeeTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.withRoleType(RoleType.NEW_EMPLOYEE)
			.withPhase(phase1)
			.build();
		final var customManagerTask = CustomTaskEntity.builder()
			.withId(customTaskId)
			.withRoleType(RoleType.MANAGER_FOR_NEW_EMPLOYEE)
			.withPhase(phase2)
			.build();

		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withEmployee(employee)
			.withChecklists(List.of(checklist))
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklistsForManager(MUNICIPALITY_ID, username)).thenReturn(List.of(employeeChecklistEntity));
		when(customTaskRepositoryMock.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID)).thenReturn(List.of(customEmployeeTask, customManagerTask));
		when(sortorderServiceMock.applySorting(any(), any())).thenAnswer(arg -> arg.getArgument(1));

		// Act
		final var employeeChecklists = service.fetchChecklistsForManager(MUNICIPALITY_ID, username);

		// Assert and verify
		assertThat(employeeChecklists).hasSize(1);
		assertThat(employeeChecklists.getFirst().getPhases()).hasSize(2).satisfiesExactlyInAnyOrder(ph -> {
			assertThat(ph.getTasks()).hasSize(2).satisfiesExactlyInAnyOrder(t -> {
				assertThat(t.isCustomTask()).isTrue();
				assertThat(t.getRoleType()).isEqualTo(RoleType.NEW_EMPLOYEE);
				assertThat(t.getFulfilmentStatus()).isEqualTo(FulfilmentStatus.EMPTY);
			}, t -> {
				assertThat(t.isCustomTask()).isFalse();
				assertThat(t.getRoleType()).isEqualTo(RoleType.MANAGER_FOR_NEW_EMPLOYEE);
				assertThat(t.getFulfilmentStatus()).isEqualTo(FulfilmentStatus.EMPTY);
			});
		}, ph -> {
			assertThat(ph.getTasks()).hasSize(3).satisfiesExactlyInAnyOrder(t -> {
				assertThat(t.isCustomTask()).isTrue();
				assertThat(t.getRoleType()).isEqualTo(RoleType.MANAGER_FOR_NEW_EMPLOYEE);
				assertThat(t.getFulfilmentStatus()).isEqualTo(FulfilmentStatus.EMPTY);
			}, t -> {
				assertThat(t.isCustomTask()).isFalse();
				assertThat(t.getRoleType()).isEqualTo(RoleType.NEW_EMPLOYEE);
				assertThat(t.getFulfilmentStatus()).isEqualTo(FulfilmentStatus.EMPTY);
			}, t -> {
				assertThat(t.isCustomTask()).isFalse();
				assertThat(t.getRoleType()).isEqualTo(RoleType.MANAGER_FOR_NEW_EMPLOYEE);
				assertThat(t.getFulfilmentStatus()).isEqualTo(FulfilmentStatus.EMPTY);
			});
		});

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklistsForManager(MUNICIPALITY_ID, username);
		verify(customTaskRepositoryMock).findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID);
		verify(employeeChecklistIntegrationMock).fetchDelegateEmails(employeeChecklistId);
		verify(sortorderServiceMock).applySorting(any(), any());
	}

	@Test
	void fetchemployeeChecklistsForManagerWithManagerInformationToUpdate() {
		// Arrange
		final var username = "username";

		final var employeeId = UUID.randomUUID().toString();
		final var oldManagerId = "oldManagerId";
		final var newManagerId = "newManagerId";
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var employeeEntity = EmployeeEntity.builder()
			.withId(employeeId)
			.withManager(ManagerEntity.builder()
				.withUsername(oldManagerId)
				.build())
			.withUpdated(OffsetDateTime.now().minusDays(1).minusNanos(1))
			.build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withEmployee(employeeEntity)
			.withChecklists(List.of(ChecklistEntity.builder().build()))
			.build();
		final var employee = Employee.builder()
			.withMainEmployment(Employment.builder()
				.withIsMainEmployment(true)
				.withManager(Manager.builder()
					.withLoginname(newManagerId)
					.build())
				.build())
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklistsForManager(MUNICIPALITY_ID, username)).thenReturn(List.of(employeeChecklistEntity));
		when(employeeIntegrationMock.getEmployeeInformation(MUNICIPALITY_ID, employeeId)).thenReturn(List.of(employee));

		// Act
		final var employeeChecklists = service.fetchChecklistsForManager(MUNICIPALITY_ID, username);

		// Assert and verify
		assertThat(employeeChecklists).isEmpty(); // Due to that employee is updated and no longer has incoming userId as manager
		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklistsForManager(MUNICIPALITY_ID, username);
		verify(employeeChecklistIntegrationMock).updateEmployeeInformation(employeeEntity, employee);
		verify(employeeIntegrationMock).getEmployeeInformation(MUNICIPALITY_ID, employeeId);
	}

	@Test
	void deleteEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();

		// Act
		service.deleteEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);

		// Assert and verify
		verify(employeeChecklistIntegrationMock).deleteEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);
	}

	@Test
	void setMentor() {
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var mentor = Mentor.builder().build();

		// Act
		service.setMentor(MUNICIPALITY_ID, employeeChecklistId, mentor);

		// Assert and verify
		verify(employeeChecklistIntegrationMock).setMentor(MUNICIPALITY_ID, employeeChecklistId, mentor);
	}

	@Test
	void deleteMentor() {
		final var employeeChecklistId = UUID.randomUUID().toString();

		// Act
		service.deleteMentor(MUNICIPALITY_ID, employeeChecklistId);

		// Assert and verify
		verify(employeeChecklistIntegrationMock).deleteMentor(MUNICIPALITY_ID, employeeChecklistId);
	}

	@Test
	void createCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId)).thenReturn(employeeChecklistEntity);
		when(employeeChecklistIntegrationMock.createCustomTask(MUNICIPALITY_ID, employeeChecklistId, phaseId, request)).thenReturn(CustomTaskEntity.builder().build());

		// Act
		final var result = service.createCustomTask(MUNICIPALITY_ID, employeeChecklistId, phaseId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(CustomTask.class);

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);
		verify(employeeChecklistIntegrationMock).createCustomTask(MUNICIPALITY_ID, employeeChecklistId, phaseId, request);
	}

	@Test
	void createCustomTaskOnLockedemployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = CustomTaskCreateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withLocked(true)
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId)).thenReturn(employeeChecklist);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.createCustomTask(MUNICIPALITY_ID, employeeChecklistId, phaseId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeeChecklistId));

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);
	}

	@Test
	void readCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var customTaskEntity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder()
				.withId(employeeChecklistId)
				.withChecklists(List.of(ChecklistEntity.builder()
					.withMunicipalityId(MUNICIPALITY_ID)
					.build()))
				.build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(customTaskEntity));

		// Act
		final var result = service.readCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(CustomTask.class);

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void readNonExistentCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.readCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeeChecklistId, customTaskId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void readCustomTaskBelongingToOtherEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var customTaskEntity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder().withId(UUID.randomUUID().toString()).build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(customTaskEntity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.readCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeeChecklistId, customTaskId));

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
			.withEmployeeChecklist(
				EmployeeChecklistEntity.builder()
					.withId(employeeChecklistId)
					.withChecklists(List.of(ChecklistEntity.builder()
						.withMunicipalityId(MUNICIPALITY_ID)
						.build()))
					.build())
			.build();
		final var request = CustomTaskUpdateRequest.builder()
			.withHeading(heading)
			.withQuestionType(questionType)
			.withSortOrder(sortOrder)
			.withText(text)
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var result = service.updateCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId, request);

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
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId, request));

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
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId, request));

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
				.withChecklists(List.of(ChecklistEntity.builder()
					.withMunicipalityId(MUNICIPALITY_ID)
					.build()))
				.build())
			.build();
		final var request = CustomTaskUpdateRequest.builder().build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId, request));

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
				.withChecklists(List.of(ChecklistEntity.builder()
					.withMunicipalityId(MUNICIPALITY_ID)
					.build()))
				.withCustomFulfilments(new ArrayList<>(List.of(CustomFulfilmentEntity.builder()
					.withCustomTask(CustomTaskEntity.builder()
						.withId(customTaskId)
						.build())
					.build())))
				.build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		service.deleteCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId);

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
		final var e = assertThrows(ThrowableProblem.class, () -> service.deleteCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeeChecklistId, customTaskId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void deleteCustomTaskBelongingToOtherEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var entity = CustomTaskEntity.builder()
			.withEmployeeChecklist(EmployeeChecklistEntity.builder().withId(UUID.randomUUID().toString()).build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.deleteCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s does not contain any custom task with id %s.".formatted(employeeChecklistId, customTaskId));

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
				.withChecklists(List.of(ChecklistEntity.builder()
					.withMunicipalityId(MUNICIPALITY_ID)
					.build()))
				.build())
			.build();

		when(customTaskRepositoryMock.findById(customTaskId)).thenReturn(Optional.of(entity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.deleteCustomTask(MUNICIPALITY_ID, employeeChecklistId, customTaskId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeeChecklistId));

		verify(customTaskRepositoryMock).findById(customTaskId);
	}

	@Test
	void updateAllTasksInPhase() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistPhaseUpdateRequest.builder().build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withPhase(PhaseEntity.builder()
						.withId(phaseId)
						.build())
					.build()))
				.build()))
			.build();

		when(employeeChecklistIntegrationMock.updateAllFulfilmentForAllTasksInPhase(MUNICIPALITY_ID, employeeChecklistId, phaseId, request)).thenReturn(entity);

		// Act
		final var result = service.updateAllTasksInPhase(MUNICIPALITY_ID, employeeChecklistId, phaseId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(EmployeeChecklistPhase.class);
		assertThat(result.getId()).isEqualTo(phaseId);

		verify(employeeChecklistIntegrationMock).updateAllFulfilmentForAllTasksInPhase(MUNICIPALITY_ID, employeeChecklistId, phaseId, request);
		verify(customTaskRepositoryMock).findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID);
	}

	@Test
	void updateAllTasksInPhaseWithException() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistPhaseUpdateRequest.builder().build();
		final var entity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withPhase(PhaseEntity.builder()
						.withId(UUID.randomUUID().toString())
						.build())
					.build()))
				.build()))
			.build();

		when(employeeChecklistIntegrationMock.updateAllFulfilmentForAllTasksInPhase(MUNICIPALITY_ID, employeeChecklistId, phaseId, request)).thenReturn(entity);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateAllTasksInPhase(MUNICIPALITY_ID, employeeChecklistId, phaseId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(e.getMessage()).isEqualTo("Internal Server Error: Could not read phase with id %s from employee checklist with id %s.".formatted(phaseId, employeeChecklistId));

		verify(employeeChecklistIntegrationMock).updateAllFulfilmentForAllTasksInPhase(MUNICIPALITY_ID, employeeChecklistId, phaseId, request);
	}

	@Test
	void updateCommonTaskFulfilment() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(taskId)
					.withPhase(PhaseEntity.builder()
						.withId(UUID.randomUUID().toString())
						.build())
					.build()))
				.build()))
			.build();
		final var fulfilment = FulfilmentEntity.builder().build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId)).thenReturn(employeeChecklist);
		when(employeeChecklistIntegrationMock.updateCommonTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request)).thenReturn(fulfilment);

		// Act
		final var result = service.updateTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(EmployeeChecklistTask.class);

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);
		verify(employeeChecklistIntegrationMock).updateCommonTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request);
	}

	@Test
	void updateNonExistingCommonTaskFulfilment() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(UUID.randomUUID().toString())
					.withPhase(PhaseEntity.builder()
						.build())
					.build()))
				.build()))
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId)).thenReturn(employeeChecklist);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Task with id %s was not found in employee checklist with id %s.".formatted(taskId, employeeChecklistId));

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);
	}

	@Test
	void updateCustomTaskFulfilment() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(CustomTaskEntity.builder().withId(taskId).build()))
			.build();
		final var fulfilment = CustomFulfilmentEntity.builder().build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId)).thenReturn(employeeChecklist);
		when(employeeChecklistIntegrationMock.updateCustomTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request)).thenReturn(fulfilment);

		// Act
		final var result = service.updateTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request);

		// Assert and verify
		assertThat(result).isNotNull().isInstanceOf(EmployeeChecklistTask.class);

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);
		verify(employeeChecklistIntegrationMock).updateCustomTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request);
	}

	@Test
	void updateNonExistingCustomTaskFulfilment() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withCustomTasks(List.of(CustomTaskEntity.builder().withId(UUID.randomUUID().toString())
				.build()))
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId)).thenReturn(employeeChecklist);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Task with id %s was not found in employee checklist with id %s.".formatted(taskId, employeeChecklistId));

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);
	}

	@Test
	void updateTaskFulfilmentOnLockedEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var request = EmployeeChecklistTaskUpdateRequest.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withLocked(true)
			.build();

		when(employeeChecklistIntegrationMock.fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId)).thenReturn(employeeChecklist);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.updateTaskFulfilment(MUNICIPALITY_ID, employeeChecklistId, taskId, request));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeeChecklistId));

		verify(employeeChecklistIntegrationMock).fetchEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId);
	}

	@Test
	void initiateEmployeeChecklists() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var rootOrgId = 13;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var orgTree = "2|12|OrgLevel 2¤3|122|OrgLevel 3¤4|" + orgId + "|OrgLevel 4";
		final var information = "All is good in the neighborhood";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		final var portalPersonData = new PortalPersonData()
			.companyId(companyId)
			.orgTree(orgTree);

		when(employeeIntegrationMock.getNewEmployees(eq(MUNICIPALITY_ID), any())).thenReturn(List.of(employee));
		when(employeeIntegrationMock.getEmployeeByEmail(MUNICIPALITY_ID, emailAddress)).thenReturn(Optional.of(portalPersonData));
		when(employeeChecklistIntegrationMock.initiateEmployee(any(), any(), any())).thenReturn(information);
		when(mdViewerClientMock.getOrganizationsForCompany(companyId)).thenReturn(List.of(
			new Organization().orgId(rootOrgId).treeLevel(1).orgName("Sundsvalls kommun"),
			new Organization().orgId(12).treeLevel(2).orgName("OrgLevel 2").parentId(rootOrgId),
			new Organization().orgId(122).treeLevel(3).orgName("OrgLevel 3").parentId(12),
			new Organization().orgId(orgId).treeLevel(4).orgName("OrgLevel 4").parentId(122)));

		// Act
		final var response = service.initiateEmployeeChecklists(MUNICIPALITY_ID);

		// Assert and verify
		verify(employeeIntegrationMock).getNewEmployees(MUNICIPALITY_ID, LocalDate.now().minusDays(30));
		verify(employeeIntegrationMock).getEmployeeByEmail(MUNICIPALITY_ID, emailAddress);
		verify(mdViewerClientMock).getOrganizationsForCompany(companyId);
		verify(employeeChecklistIntegrationMock).initiateEmployee(eq(MUNICIPALITY_ID), eq(employee), organizationTreeCaptor.capture());

		assertOrgTreeParameters();
		assertThat(response.getSummary()).isEqualTo("Successful import of 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.OK, information));
	}

	@Test
	void initiateEmployeeChecklistsNoStructuralDataFound() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);

		when(employeeIntegrationMock.getNewEmployees(eq(MUNICIPALITY_ID), any())).thenReturn(List.of(employee));

		// Act
		final var response = service.initiateEmployeeChecklists(MUNICIPALITY_ID);

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("1 potential problems occurred when importing 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.NOT_FOUND, "Not Found: Employee with loginname loginName is missing information regarding organizational structure."));

		verify(employeeIntegrationMock).getNewEmployees(MUNICIPALITY_ID, LocalDate.now().minusDays(30));
		verify(employeeIntegrationMock).getEmployeeByEmail(MUNICIPALITY_ID, emailAddress);
		verify(mdViewerClientMock, never()).getOrganizationsForCompany(companyId);
		verify(employeeChecklistIntegrationMock, never()).initiateEmployee(eq(MUNICIPALITY_ID), any(), any());
	}

	@Test
	void initiateEmployeeChecklistsThrowsException() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var rootOrgId = 13;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var orgTree = "2|12|OrgLevel 2¤3|122|OrgLevel 3¤4|" + orgId + "|OrgLevel 4";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		final var portalPersonData = new PortalPersonData()
			.companyId(companyId)
			.orgTree(orgTree);

		when(employeeIntegrationMock.getNewEmployees(eq(MUNICIPALITY_ID), any())).thenReturn(List.of(employee));
		when(employeeIntegrationMock.getEmployeeByEmail(MUNICIPALITY_ID, emailAddress)).thenReturn(Optional.of(portalPersonData));
		when(mdViewerClientMock.getOrganizationsForCompany(companyId)).thenReturn(List.of(
			new Organization().orgId(rootOrgId).treeLevel(1).orgName("Sundsvalls kommun"),
			new Organization().orgId(12).treeLevel(2).orgName("OrgLevel 2").parentId(rootOrgId),
			new Organization().orgId(122).treeLevel(3).orgName("OrgLevel 3").parentId(12),
			new Organization().orgId(orgId).treeLevel(4).orgName("OrgLevel 4").parentId(122)));
		when(employeeChecklistIntegrationMock.initiateEmployee(any(), any(), any())).thenThrow(new NullPointerException("There is a null value in the neighborhood"));

		// Act
		final var response = service.initiateEmployeeChecklists(MUNICIPALITY_ID);

		// Assert and verify
		verify(employeeIntegrationMock).getNewEmployees(MUNICIPALITY_ID, LocalDate.now().minusDays(30));
		verify(employeeIntegrationMock).getEmployeeByEmail(MUNICIPALITY_ID, emailAddress);
		verify(mdViewerClientMock).getOrganizationsForCompany(companyId);
		verify(employeeChecklistIntegrationMock).initiateEmployee(eq(MUNICIPALITY_ID), eq(employee), organizationTreeCaptor.capture());

		assertOrgTreeParameters();
		assertThat(response.getSummary()).isEqualTo("1 potential problems occurred when importing 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.INTERNAL_SERVER_ERROR, "Internal Server Error: There is a null value in the neighborhood"));
	}

	@Test
	void initiateEmployeeChecklists_invalidFormOfEmployment() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		employee.getMainEmployment().setFormOfEmploymentId("invalid");
		employee.getMainEmployment().setEventType("joiner");

		when(employeeIntegrationMock.getNewEmployees(eq(MUNICIPALITY_ID), any())).thenReturn(List.of(employee));

		// Act
		final var response = service.initiateEmployeeChecklists(MUNICIPALITY_ID);

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("1 potential problems occurred when importing 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.NOT_ACCEPTABLE,
				"Not Acceptable: Creation of checklist not possible for employee with loginname loginName as the employee does not have a main employment with an employment form that validates for creating an employee checklist."));

		verify(employeeIntegrationMock).getNewEmployees(MUNICIPALITY_ID, LocalDate.now().minusDays(30));
	}

	@Test
	void initiateEmployeeChecklists_invalidEventType() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		employee.getMainEmployment().setFormOfEmploymentId("9");
		employee.getMainEmployment().setEventType("invalid");

		when(employeeIntegrationMock.getNewEmployees(eq(MUNICIPALITY_ID), any())).thenReturn(List.of(employee));

		// Act
		final var response = service.initiateEmployeeChecklists(MUNICIPALITY_ID);

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("1 potential problems occurred when importing 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.NOT_ACCEPTABLE,
				"Not Acceptable: Creation of checklist not possible for employee with loginname loginName as the employee does not have a main employment with an event type that validates for creating an employee checklist."));

		verify(employeeIntegrationMock).getNewEmployees(MUNICIPALITY_ID, LocalDate.now().minusDays(30));
	}

	@Test
	void initiateEmployeeChecklists_noNewEmployeesFound() {
		// Act
		final var response = service.initiateEmployeeChecklists(MUNICIPALITY_ID);

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("No employees found");
		assertThat(response.getDetails()).isNullOrEmpty();

		verify(employeeIntegrationMock).getNewEmployees(MUNICIPALITY_ID, LocalDate.now().minusDays(30));
	}

	@Test
	void initiateSpecificEmployeeChecklist() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var rootOrgId = 13;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var orgTree = "2|12|OrgLevel 2¤3|122|OrgLevel 3¤4|" + orgId + "|OrgLevel 4";
		final var information = "All is good in the neighborhood";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		final var portalPersonData = new PortalPersonData()
			.companyId(companyId)
			.orgTree(orgTree);

		when(employeeIntegrationMock.getEmployeeInformation(eq(MUNICIPALITY_ID), any())).thenReturn(List.of(employee));
		when(employeeIntegrationMock.getEmployeeByEmail(MUNICIPALITY_ID, emailAddress)).thenReturn(Optional.of(portalPersonData));
		when(employeeChecklistIntegrationMock.initiateEmployee(eq(MUNICIPALITY_ID), any(), any())).thenReturn(information);
		when(mdViewerClientMock.getOrganizationsForCompany(companyId)).thenReturn(List.of(
			new Organization().orgId(rootOrgId).treeLevel(1).orgName("Sundsvalls kommun"),
			new Organization().orgId(12).treeLevel(2).orgName("OrgLevel 2").parentId(rootOrgId),
			new Organization().orgId(122).treeLevel(3).orgName("OrgLevel 3").parentId(12),
			new Organization().orgId(orgId).treeLevel(4).orgName("OrgLevel 4").parentId(122)));

		// Act
		final var response = service.initiateSpecificEmployeeChecklist(MUNICIPALITY_ID, employeeUuid.toString());

		// Assert and verify
		verify(employeeIntegrationMock).getEmployeeInformation(MUNICIPALITY_ID, employeeUuid.toString());
		verify(employeeIntegrationMock).getEmployeeByEmail(MUNICIPALITY_ID, emailAddress);
		verify(mdViewerClientMock).getOrganizationsForCompany(companyId);
		verify(employeeChecklistIntegrationMock).initiateEmployee(eq(MUNICIPALITY_ID), eq(employee), organizationTreeCaptor.capture());

		assertOrgTreeParameters();
		assertThat(response.getSummary()).isEqualTo("Successful import of 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.OK, information));
	}

	@Test
	void initiateSpecificEmployeeChecklist_noNewEmployeeFound() {
		// Arrange
		final var employeeUuid = UUID.randomUUID();

		// Act
		final var response = service.initiateSpecificEmployeeChecklist(MUNICIPALITY_ID, employeeUuid.toString());

		// Assert and verify
		assertThat(response.getSummary()).isEqualTo("No employees found");
		assertThat(response.getDetails()).isNullOrEmpty();

		verify(employeeIntegrationMock).getEmployeeInformation(MUNICIPALITY_ID, employeeUuid.toString());
	}

	@Test
	// Should pass as form of employment and event type should not be verified when initiating a specific employee checklist
	void initiateSpecificEmployeeChecklist_invalidFormOfEmploymentAndEventType() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var managerUuid = UUID.randomUUID();
		final var companyId = 1;
		final var rootOrgId = 13;
		final var orgId = 1225;
		final var loginName = "loginName";
		final var employee = createEmployee(emailAddress, employeeUuid, managerUuid, companyId, orgId, loginName);
		final var orgTree = "2|12|OrgLevel 2¤3|122|OrgLevel 3¤4|" + orgId + "|OrgLevel 4";
		final var information = "All is good in the neighborhood";
		final var portalPersonData = new PortalPersonData()
			.companyId(companyId)
			.orgTree(orgTree);

		employee.getMainEmployment().setFormOfEmploymentId("invalid");
		employee.getMainEmployment().setEventType("invalid");

		when(employeeIntegrationMock.getEmployeeInformation(eq(MUNICIPALITY_ID), any())).thenReturn(List.of(employee));
		when(employeeIntegrationMock.getEmployeeByEmail(MUNICIPALITY_ID, emailAddress)).thenReturn(Optional.of(portalPersonData));
		when(employeeChecklistIntegrationMock.initiateEmployee(eq(MUNICIPALITY_ID), any(), any())).thenReturn(information);
		when(mdViewerClientMock.getOrganizationsForCompany(companyId)).thenReturn(List.of(
			new Organization().orgId(rootOrgId).treeLevel(1).orgName("Sundsvalls kommun"),
			new Organization().orgId(12).treeLevel(2).orgName("OrgLevel 2").parentId(rootOrgId),
			new Organization().orgId(122).treeLevel(3).orgName("OrgLevel 3").parentId(12),
			new Organization().orgId(orgId).treeLevel(4).orgName("OrgLevel 4").parentId(122)));

		// Act
		final var response = service.initiateSpecificEmployeeChecklist(MUNICIPALITY_ID, employeeUuid.toString());

		// Assert and verify
		verify(employeeIntegrationMock).getEmployeeInformation(MUNICIPALITY_ID, employeeUuid.toString());
		verify(employeeIntegrationMock).getEmployeeByEmail(MUNICIPALITY_ID, emailAddress);
		verify(mdViewerClientMock).getOrganizationsForCompany(companyId);
		verify(employeeChecklistIntegrationMock).initiateEmployee(eq(MUNICIPALITY_ID), eq(employee), organizationTreeCaptor.capture());

		assertOrgTreeParameters();
		assertThat(response.getSummary()).isEqualTo("Successful import of 1 employees");
		assertThat(response.getDetails()).extracting(Detail::getStatus, Detail::getInformation)
			.containsExactly(tuple(Status.OK, information));
	}

	@Test
	void getOngoingEmployeeChecklists() {
		final var parameters = new OngoingEmployeeChecklistParameters().withMunicipalityId("2281");
		final var department = OrganizationEntity.builder()
			.withOrganizationName("Drak huset")
			.build();

		final var manager = ManagerEntity.builder()
			.withFirstName("Johnny")
			.withLastName("Bravo")
			.build();

		final var employee = EmployeeEntity.builder()
			.withFirstName("Mini")
			.withLastName("LUS")
			.withUsername("mini01lus")
			.withDepartment(department)
			.withStartDate(LocalDate.now())
			.withManager(manager)
			.build();

		final var delegate = DelegateEntity.builder()
			.withFirstName("John")
			.withLastName("Doe")
			.build();

		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.withDelegates(List.of(delegate))
			.withEndDate(LocalDate.now().plusDays(5))
			.build();

		final Page<EmployeeChecklistEntity> pageResult = new PageImpl<>(List.of(employeeChecklist));
		when(employeeChecklistIntegrationMock.fetchAllOngoingEmployeeChecklists(any(), any())).thenReturn(pageResult);

		final var result = service.getOngoingEmployeeChecklists(parameters);

		assertThat(result).isNotNull();
		assertThat(result.getChecklists()).hasSize(1).allSatisfy(checklist -> {
			assertThat(checklist.getEmployeeName()).isEqualTo("Mini LUS");
			assertThat(checklist.getEmployeeUsername()).isEqualTo("mini01lus");
			assertThat(checklist.getManagerName()).isEqualTo("Johnny Bravo");
			assertThat(checklist.getDepartmentName()).isEqualTo("Drak huset");
			assertThat(checklist.getEmploymentDate()).isEqualTo(LocalDate.now());
			assertThat(checklist.getPurgeDate()).isEqualTo(LocalDate.now().plusDays(5));
			assertThat(checklist.getDelegatedTo()).containsExactly("John Doe");
		});

		assertThat(result.getMetadata()).satisfies(meta -> {
			assertThat(meta.getPage()).isEqualTo(pageResult.getNumber() + 1);
			assertThat(meta.getLimit()).isEqualTo(pageResult.getSize());
			assertThat(meta.getCount()).isEqualTo(pageResult.getTotalElements());
			assertThat(meta.getTotalPages()).isEqualTo(pageResult.getTotalPages());
			assertThat(meta.getTotalRecords()).isEqualTo(pageResult.getTotalElements());
		});

		verify(employeeChecklistIntegrationMock).fetchAllOngoingEmployeeChecklists(any(), any());
	}

	@Test
	void getInitiationInformation() {
		// Arrange
		when(initiationRepositoryMock.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(Collections.emptyList());

		// Act
		final var result = service.getInitiationInformation(MUNICIPALITY_ID);

		// Assert and verify
		assertThat(result).isNotNull();
		assertThat(result.getSummary()).isEqualTo("The last scheduled execution did not find any employees to initialize checklists for");

		verify(initiationRepositoryMock).findAllByMunicipalityId(MUNICIPALITY_ID);

	}

	private void assertOrgTreeParameters() {
		assertThat(organizationTreeCaptor.getValue().getTree()).hasSize(4).containsKeys(1, 2, 3, 4);
		assertThat(organizationTreeCaptor.getValue().getTree().values())
			.extracting(
				OrganizationLine::getLevel,
				OrganizationLine::getOrgId,
				OrganizationLine::getOrgName)
			.containsExactly(
				tuple(1, "13", "Sundsvalls kommun"),
				tuple(2, "12", "OrgLevel 2"),
				tuple(3, "122", "OrgLevel 3"),
				tuple(4, "1225", "OrgLevel 4"));
	}

	private static Employee createEmployee(final String emailAddress, final UUID employeeUuid, final UUID managerUuid, final int companyId, final int orgId, final String loginName) {
		return Employee.builder()
			.withEmailAddress(emailAddress)
			.withPersonId(employeeUuid.toString())
			.withLoginname(loginName)
			.withMainEmployment(Employment.builder()
				.withFormOfEmploymentId("1")
				.withEventType("Joiner")
				.withIsMainEmployment(true)
				.withCompanyId(companyId)
				.withOrgId(orgId)
				.withManager(Manager.builder()
					.withPersonId(managerUuid.toString())
					.build())
				.build())
			.build();
	}
}
