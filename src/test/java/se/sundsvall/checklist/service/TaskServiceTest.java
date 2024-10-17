package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createTaskCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createTaskUpdateRequest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

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

	private ChecklistEntity checklistEntity;
	private PhaseEntity phaseEntity;
	private TaskEntity taskEntity;
	private TaskCreateRequest createRequest;
	private TaskUpdateRequest updateRequest;

	@Mock
	private ChecklistRepository mockChecklistRepository;

	@Mock
	private PhaseRepository mockPhaseRepository;

	@Mock
	private TaskRepository mockTaskRepository;

	@InjectMocks
	private TaskService taskService;

	@BeforeEach
	void setup() {
		this.checklistEntity = createChecklistEntity();
		this.phaseEntity = checklistEntity.getPhases().getFirst();
		this.taskEntity = phaseEntity.getTasks().getFirst();
		this.createRequest = createTaskCreateRequest();
		this.updateRequest = createTaskUpdateRequest();
	}

	@Test
	void getAllTasksInPhaseTest() {
		var serviceSpy = spy(taskService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(any());
		doReturn(phaseEntity).when(serviceSpy).getPhaseInChecklist(any(), any());

		var result = serviceSpy.getAllTasksInPhase("string", "string");

		assertThat(result).hasSize(phaseEntity.getTasks().size());
		verify(serviceSpy).getChecklistById(any());
		verify(serviceSpy).getPhaseInChecklist(any(), any());
		verify(serviceSpy).getAllTasksInPhase(any(), any());
		verifyNoMoreInteractions(serviceSpy);
		verifyNoInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getTaskInPhaseByIdTest() {
		var serviceSpy = spy(taskService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(any());
		doReturn(phaseEntity).when(serviceSpy).getPhaseInChecklist(any(), any());
		doReturn(taskEntity).when(serviceSpy).getTaskInPhase(any(), any());

		var result = serviceSpy.getTaskInPhaseById("string", "string", "string");

		assertThat(result).isNotNull().satisfies(task -> {
			assertThat(task.getId()).isEqualTo(taskEntity.getId());
			assertThat(task.getHeading()).isEqualTo(taskEntity.getHeading());
			assertThat(task.getText()).isEqualTo(taskEntity.getText());
		});
		verify(serviceSpy).getChecklistById(any());
		verify(serviceSpy).getPhaseInChecklist(any(), any());
		verify(serviceSpy).getTaskInPhase(any(), any());
		verify(serviceSpy).getTaskInPhaseById(any(), any(), any());
		verifyNoMoreInteractions(serviceSpy);
		verifyNoInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void createTaskTest() {
		var serviceSpy = spy(taskService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(any());
		doReturn(phaseEntity).when(serviceSpy).getPhaseInChecklist(any(), any());
		when(mockTaskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = serviceSpy.createTask("string", "string", createRequest);

		assertThat(result).isNotNull().satisfies(task -> {
			assertThat(task.getHeading()).isEqualTo(createRequest.getHeading());
			assertThat(task.getText()).isEqualTo(createRequest.getText());
			assertThat(task.getRoleType()).isEqualTo(createRequest.getRoleType());
			assertThat(task.getQuestionType()).isEqualTo(createRequest.getQuestionType());
		});
		verify(serviceSpy).getChecklistById(any());
		verify(serviceSpy).getPhaseInChecklist(any(), any());
		verify(serviceSpy).createTask(any(), any(), any());
		verify(mockTaskRepository).save(any());
		verify(mockPhaseRepository).save(any());
		verifyNoMoreInteractions(serviceSpy, mockTaskRepository, mockPhaseRepository);
		verifyNoInteractions(mockChecklistRepository);
	}

	@Test
	void updateTaskTest() {
		var serviceSpy = spy(taskService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(any());
		doReturn(phaseEntity).when(serviceSpy).getPhaseInChecklist(any(), any());
		doReturn(taskEntity).when(serviceSpy).getTaskInPhase(any(), any());
		when(mockTaskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = serviceSpy.updateTask("string", "string", "string", updateRequest);

		assertThat(result).isNotNull().satisfies(task -> {
			assertThat(task.getHeading()).isEqualTo(updateRequest.getHeading());
			assertThat(task.getText()).isEqualTo(updateRequest.getText());
			assertThat(task.getRoleType()).isEqualTo(updateRequest.getRoleType());
			assertThat(task.getQuestionType()).isEqualTo(updateRequest.getQuestionType());
		});
		verify(serviceSpy).getChecklistById(any());
		verify(serviceSpy).getPhaseInChecklist(any(), any());
		verify(serviceSpy).getTaskInPhase(any(), any());
		verify(serviceSpy).updateTask(any(), any(), any(), any());
		verify(mockTaskRepository).save(any());
		verifyNoMoreInteractions(serviceSpy, mockTaskRepository);
		verifyNoInteractions(mockChecklistRepository, mockPhaseRepository);
	}

	@Test
	void deleteTaskTest() {
		var serviceSpy = spy(taskService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(any());
		doReturn(phaseEntity).when(serviceSpy).getPhaseInChecklist(any(), any());
		doReturn(taskEntity).when(serviceSpy).getTaskInPhase(any(), any());

		serviceSpy.deleteTask("string", "string", "string");

		verify(serviceSpy).getChecklistById(any());
		verify(serviceSpy).getPhaseInChecklist(any(), any());
		verify(serviceSpy).getTaskInPhase(any(), any());
		verify(serviceSpy).deleteTask(any(), any(), any());
		verify(mockTaskRepository).delete(any());
		verify(mockPhaseRepository).save(any());
		verifyNoMoreInteractions(serviceSpy, mockTaskRepository, mockPhaseRepository);
		verifyNoInteractions(mockChecklistRepository);
	}

	@Test
	void getChecklistByIdTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.of(checklistEntity));

		var result = taskService.getChecklistById(any());

		assertThat(result).isNotNull().satisfies(list -> {
			assertThat(list.getName()).isEqualTo(checklistEntity.getName());
			assertThat(list.getPhases()).hasSize(checklistEntity.getPhases().size());
			assertThat(list.getVersion()).isEqualTo(checklistEntity.getVersion());
			assertThat(list.getRoleType()).isEqualTo(checklistEntity.getRoleType());
			assertThat(list.getLifeCycle()).isEqualTo(checklistEntity.getLifeCycle());
		});
		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
		verifyNoInteractions(mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getChecklistByIdNotFoundTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.getChecklistById(any()))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Checklist not found");
		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
		verifyNoInteractions(mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getPhaseInChecklistTest() {
		var result = taskService.getPhaseInChecklist(checklistEntity, phaseEntity.getId());

		assertThat(result).isNotNull().satisfies(phase -> {
			assertThat(phase.getId()).isEqualTo(phaseEntity.getId());
			assertThat(phase.getName()).isEqualTo(phaseEntity.getName());
			assertThat(phase.getTasks()).hasSize(phaseEntity.getTasks().size());
		});
		verifyNoInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getPhaseInChecklistNotFoundTest() {
		assertThatThrownBy(() -> taskService.getPhaseInChecklist(checklistEntity, "notFound"))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Phase not found");
		verifyNoInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getTaskInPhaseTest() {
		var result = taskService.getTaskInPhase(phaseEntity, taskEntity.getId());

		assertThat(result).isNotNull().satisfies(task -> {
			assertThat(task.getId()).isEqualTo(taskEntity.getId());
			assertThat(task.getHeading()).isEqualTo(taskEntity.getHeading());
			assertThat(task.getQuestionType()).isEqualTo(taskEntity.getQuestionType());
			assertThat(task.getRoleType()).isEqualTo(taskEntity.getRoleType());
			assertThat(task.getText()).isEqualTo(taskEntity.getText());
		});
		verifyNoInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

	@Test
	void getTaskInPhaseNotFoundTest() {
		assertThatThrownBy(() -> taskService.getTaskInPhase(phaseEntity, "notFound"))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Task not found");
		verifyNoInteractions(mockChecklistRepository, mockPhaseRepository, mockTaskRepository);
	}

}
