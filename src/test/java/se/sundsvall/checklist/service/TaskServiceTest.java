package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createTaskCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createTaskUpdateRequest;
import static se.sundsvall.checklist.service.EventService.TASK_ADDED;
import static se.sundsvall.checklist.service.EventService.TASK_CHANGED;

import generated.se.sundsvall.eventlog.EventType;
import java.util.ArrayList;
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
import org.zalando.problem.Problem;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.api.model.TaskCreateRequest;
import se.sundsvall.checklist.api.model.TaskUpdateRequest;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;
import se.sundsvall.checklist.integration.db.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String USER_ID = "Chuck Norris";

	private static final TaskCreateRequest createRequest = createTaskCreateRequest();
	private static final TaskUpdateRequest updateRequest = createTaskUpdateRequest();

	private ChecklistEntity checklistEntity;
	private PhaseEntity phaseEntity;
	private TaskEntity taskEntity;

	@Mock
	private EmployeeChecklistRepository mockEmployeeChecklistRepository;

	@Mock
	private ChecklistRepository mockChecklistRepository;

	@Mock
	private PhaseRepository mockPhaseRepository;

	@Mock
	private TaskRepository mockTaskRepository;

	@Mock
	private SortorderService mockSortorderService;

	@Mock
	private EventService eventServiceMock;

	@InjectMocks
	private TaskService taskService;

	@Captor
	private ArgumentCaptor<ChecklistEntity> checklistEntityCaptor;

	@Captor
	private ArgumentCaptor<TaskEntity> taskEntityCaptor;

	@BeforeEach
	void setup() {
		checklistEntity = createChecklistEntity();
		phaseEntity = checklistEntity.getTasks().getFirst().getPhase();
		taskEntity = checklistEntity.getTasks().getFirst();
	}

	@Test
	void getTasksInPhase() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(true);
		when(mockSortorderService.applySortingToTasks(eq(MUNICIPALITY_ID), eq(checklistEntity.getOrganization().getOrganizationNumber()), anyList())).thenAnswer(inv -> inv.getArgument(2));

		final var result = taskService.getTasks(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId());

		assertThat(result).isNotEmpty().hasSize(2);
		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockSortorderService).applySortingToTasks(eq(MUNICIPALITY_ID), eq(checklistEntity.getOrganization().getOrganizationNumber()), anyList());
	}

	@Test
	void getTasksInPhaseChecklistNotFound() {
		assertThatThrownBy(() -> taskService.getTasks(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
	}

	@Test
	void getTasksInPhasePhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> taskService.getTasks(MUNICIPALITY_ID, checklistEntity.getId(), randomId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
	}

	@Test
	void getTaskInPhase() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(true);
		when(mockSortorderService.applySortingToTask(eq(MUNICIPALITY_ID), eq(checklistEntity.getOrganization().getOrganizationNumber()), any(Task.class))).thenAnswer(inv -> inv.getArgument(2));

		final var result = taskService.getTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId());

		assertThat(result).isNotNull().satisfies(task -> {
			assertThat(task.getId()).isEqualTo(taskEntity.getId());
			assertThat(task.getHeading()).isEqualTo(taskEntity.getHeading());
			assertThat(task.getHeadingReference()).isEqualTo(taskEntity.getHeadingReference());
			assertThat(task.getText()).isEqualTo(taskEntity.getText());
		});

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockSortorderService).applySortingToTask(eq(MUNICIPALITY_ID), eq(checklistEntity.getOrganization().getOrganizationNumber()), any(Task.class));
	}

	@Test
	void getTaskInPhaseChecklistNotFound() {
		assertThatThrownBy(() -> taskService.getTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
	}

	@Test
	void getTaskInPhasePhaseNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(true);

		assertThatThrownBy(() -> taskService.getTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), UUID.randomUUID().toString()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Task not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
	}

	@Test
	void createTask() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));
		when(mockTaskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		doNothing().when(eventServiceMock).createChecklistEvent(EventType.CREATE, TASK_ADDED.formatted(createRequest.getHeading(), phaseEntity.getName()), checklistEntity, createRequest.getCreatedBy());

		final var result = taskService.createTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), createRequest);

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockTaskRepository).save(taskEntityCaptor.capture());
		verify(mockChecklistRepository).save(checklistEntityCaptor.capture());
		verify(eventServiceMock).createChecklistEvent(EventType.CREATE, TASK_ADDED.formatted(createRequest.getHeading(), phaseEntity.getName()), checklistEntity, createRequest.getCreatedBy());

		assertThat(result).isNotNull().isInstanceOf(Task.class);
		assertThat(taskEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getHeading()).isEqualTo(createRequest.getHeading());
			assertThat(entity.getHeadingReference()).isEqualTo(createRequest.getHeadingReference());
			assertThat(entity.getText()).isEqualTo(createRequest.getText());
			assertThat(entity.getRoleType()).isEqualTo(createRequest.getRoleType());
			assertThat(entity.getQuestionType()).isEqualTo(createRequest.getQuestionType());
			assertThat(entity.getPhase()).isEqualTo(phaseEntity);
		});
		assertThat(checklistEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getTasks()).contains(taskEntityCaptor.getValue());
		});
	}

	@Test
	void createTaskChecklistNotFound() {
		assertThatThrownBy(() -> taskService.createTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), createRequest))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(eventServiceMock, never()).createChecklistEvent(any(), any(), any(), any());
	}

	@Test
	void createTaskPhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> taskService.createTask(MUNICIPALITY_ID, checklistEntity.getId(), randomId, createRequest))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).findByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
		verify(eventServiceMock, never()).createChecklistEvent(any(), any(), any(), any());
	}

	@Test
	void updateTask() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(true);
		when(mockTaskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(mockSortorderService.applySortingToTask(eq(MUNICIPALITY_ID), eq(checklistEntity.getOrganization().getOrganizationNumber()), any(Task.class))).thenAnswer(inv -> inv.getArgument(2));
		doNothing().when(eventServiceMock).createChecklistEvent(EventType.UPDATE, TASK_CHANGED.formatted(updateRequest.getHeading(), phaseEntity.getName()), checklistEntity, updateRequest.getUpdatedBy());

		final var result = taskService.updateTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId(), updateRequest);

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockTaskRepository).save(taskEntityCaptor.capture());
		verify(mockSortorderService).applySortingToTask(eq(MUNICIPALITY_ID), eq(checklistEntity.getOrganization().getOrganizationNumber()), any(Task.class));
		verify(eventServiceMock).createChecklistEvent(EventType.UPDATE, TASK_CHANGED.formatted(taskEntity.getHeading(), taskEntity.getPhase().getName()), checklistEntity, updateRequest.getUpdatedBy());

		assertThat(result).isNotNull().isInstanceOf(Task.class);
		assertThat(taskEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getHeading()).isEqualTo(updateRequest.getHeading());
			assertThat(entity.getHeadingReference()).isEqualTo(updateRequest.getHeadingReference());
			assertThat(entity.getText()).isEqualTo(updateRequest.getText());
			assertThat(entity.getRoleType()).isEqualTo(updateRequest.getRoleType());
			assertThat(entity.getQuestionType()).isEqualTo(updateRequest.getQuestionType());
		});
	}

	@Test
	void updateTaskChecklistNotFound() {
		assertThatThrownBy(() -> taskService.updateTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId(), updateRequest))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(eventServiceMock, never()).createChecklistEvent(any(), any(), any(), any());
	}

	@Test
	void updateTaskPhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> taskService.updateTask(MUNICIPALITY_ID, checklistEntity.getId(), randomId, taskEntity.getId(), updateRequest))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
	}

	@Test
	void updateTaskTaskNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(true);

		assertThatThrownBy(() -> taskService.updateTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), UUID.randomUUID().toString(), updateRequest))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Task not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(eventServiceMock, never()).createChecklistEvent(any(), any(), any(), any());
	}

	@Test
	void deleteTask() {
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(checklistEntity))
			.withFulfilments(new ArrayList<>(List.of(FulfilmentEntity.builder()
				.withTask(taskEntity)
				.build())))
			.build();

		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(true);
		doNothing().when(eventServiceMock).createChecklistEvent(EventType.DELETE, EventService.TASK_REMOVED.formatted(taskEntity.getHeading(), phaseEntity.getName()), checklistEntity, USER_ID);
		when(mockEmployeeChecklistRepository.findAllByChecklistsTasksId(taskEntity.getId())).thenReturn(List.of(employeeChecklist));

		taskService.deleteTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId(), USER_ID);

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockEmployeeChecklistRepository).findAllByChecklistsTasksId(taskEntity.getId());
		verify(mockSortorderService).deleteSortorderItem(taskEntity.getId());
		verify(mockTaskRepository).delete(taskEntityCaptor.capture());
		verify(eventServiceMock).createChecklistEvent(EventType.DELETE, EventService.TASK_REMOVED.formatted(taskEntity.getHeading(), phaseEntity.getName()), checklistEntity, USER_ID);

		assertThat(employeeChecklist.getFulfilments()).isNullOrEmpty();
		assertThat(taskEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getId()).isEqualTo(taskEntity.getId());
		});
	}

	@Test
	void deleteTaskChecklistNotFound() {
		assertThatThrownBy(() -> taskService.deleteTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId(), USER_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(eventServiceMock, never()).createChecklistEvent(any(), any(), any(), any());
	}

	@Test
	void deleteTaskPhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> taskService.deleteTask(MUNICIPALITY_ID, checklistEntity.getId(), randomId, taskEntity.getId(), USER_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
		verify(eventServiceMock, never()).createChecklistEvent(any(), any(), any(), any());
	}

	@Test
	void deleteTaskTaskNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(true);

		assertThatThrownBy(() -> taskService.deleteTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), UUID.randomUUID().toString(), USER_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Task not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).existsByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(eventServiceMock, never()).createChecklistEvent(any(), any(), any(), any());
	}

	@AfterEach
	void verifyNoMoreInteraction() {
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository, mockSortorderService, mockEmployeeChecklistRepository);
	}

}
