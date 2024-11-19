package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createTaskCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createTaskUpdateRequest;

import java.util.Optional;
import java.util.UUID;

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
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;
import se.sundsvall.checklist.integration.db.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final TaskCreateRequest createRequest = createTaskCreateRequest();
	private static final TaskUpdateRequest updateRequest = createTaskUpdateRequest();

	private ChecklistEntity checklistEntity;
	private PhaseEntity phaseEntity;
	private TaskEntity taskEntity;

	@Mock
	private ChecklistRepository mockChecklistRepository;

	@Mock
	private PhaseRepository mockPhaseRepository;

	@Mock
	private TaskRepository mockTaskRepository;

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
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));

		final var result = taskService.getTasks(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId());

		assertThat(result).isNotEmpty().hasSize(2);
		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getTasksInPhaseChecklistNotFound() {
		assertThatThrownBy(() -> taskService.getTasks(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getTasksInPhasePhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> taskService.getTasks(MUNICIPALITY_ID, checklistEntity.getId(), randomId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).findByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getTaskInPhase() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));

		final var result = taskService.getTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId());

		assertThat(result).isNotNull().satisfies(task -> {
			assertThat(task.getId()).isEqualTo(taskEntity.getId());
			assertThat(task.getHeading()).isEqualTo(taskEntity.getHeading());
			assertThat(task.getText()).isEqualTo(taskEntity.getText());
		});

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getTaskInPhaseChecklistNotFound() {
		assertThatThrownBy(() -> taskService.getTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getTaskInPhasePhaseNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));

		assertThatThrownBy(() -> taskService.getTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), UUID.randomUUID().toString()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Task not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void createTask() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));
		when(mockTaskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = taskService.createTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), createRequest);

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockTaskRepository).save(taskEntityCaptor.capture());
		verify(mockChecklistRepository).save(checklistEntityCaptor.capture());
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);

		assertThat(result).isNotNull().isInstanceOf(Task.class);
		assertThat(taskEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getHeading()).isEqualTo(createRequest.getHeading());
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
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
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
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void updateTask() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));
		when(mockTaskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = taskService.updateTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId(), updateRequest);

		verify(mockTaskRepository).save(taskEntityCaptor.capture());
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
		assertThat(result).isNotNull().isInstanceOf(Task.class);
		assertThat(taskEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getHeading()).isEqualTo(updateRequest.getHeading());
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
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void updateTaskPhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> taskService.updateTask(MUNICIPALITY_ID, checklistEntity.getId(), randomId, taskEntity.getId(), updateRequest))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).findByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void updateTaskTaskNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));

		assertThatThrownBy(() -> taskService.updateTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), UUID.randomUUID().toString(), updateRequest))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Task not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void deleteTask() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));

		taskService.deleteTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId());

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockTaskRepository).delete(taskEntityCaptor.capture());
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);

		assertThat(taskEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getId()).isEqualTo(taskEntity.getId());
		});
	}

	@Test
	void deleteTaskChecklistNotFound() {
		assertThatThrownBy(() -> taskService.deleteTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), taskEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void deleteTaskPhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> taskService.deleteTask(MUNICIPALITY_ID, checklistEntity.getId(), randomId, taskEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).findByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void deleteTaskTaskNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));

		assertThatThrownBy(() -> taskService.deleteTask(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), UUID.randomUUID().toString()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Task not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockPhaseRepository).findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}
}
