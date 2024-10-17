package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toTask;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toTaskEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toTasks;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updateTaskEntity;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
public class TaskService {

	private static final String CHECKLIST_NOT_FOUND = "Checklist not found";
	private static final String PHASE_NOT_FOUND = "Phase not found";
	private static final String TASK_NOT_FOUND = "Task not found";

	private final TaskRepository taskRepository;
	private final ChecklistRepository checklistRepository;
	private final PhaseRepository phaseRepository;

	public TaskService(final TaskRepository taskRepository,
		final ChecklistRepository checklistRepository,
		final PhaseRepository phaseRepository) {
		this.taskRepository = taskRepository;
		this.checklistRepository = checklistRepository;
		this.phaseRepository = phaseRepository;
	}

	public List<Task> getAllTasksInPhase(final String checklistId, final String phaseId) {
		var checklist = getChecklistById(checklistId);
		var phase = getPhaseInChecklist(checklist, phaseId);
		return toTasks(phase.getTasks());
	}

	public Task getTaskInPhaseById(final String checklistId, final String phaseId, final String taskId) {
		var checklist = getChecklistById(checklistId);
		var phase = getPhaseInChecklist(checklist, phaseId);
		var task = getTaskInPhase(phase, taskId);
		return toTask(task);
	}

	@Transactional
	public Task createTask(final String checklistId, final String phaseId, final TaskCreateRequest request) {
		var checklist = getChecklistById(checklistId);
		var phase = getPhaseInChecklist(checklist, phaseId);
		var task = taskRepository.save(toTaskEntity(request));
		phase.getTasks().add(task);
		phaseRepository.save(phase);
		return toTask(task);
	}

	public Task updateTask(final String checklistId, final String phaseId, final String taskId, final TaskUpdateRequest request) {
		var checklist = getChecklistById(checklistId);
		var phase = getPhaseInChecklist(checklist, phaseId);
		var task = getTaskInPhase(phase, taskId);
		return toTask(taskRepository.save(updateTaskEntity(task, request)));
	}

	@Transactional
	public void deleteTask(final String checklistId, final String phaseId, final String taskId) {
		var checklist = getChecklistById(checklistId);
		var phase = getPhaseInChecklist(checklist, phaseId);
		var task = getTaskInPhase(phase, taskId);
		phase.getTasks().remove(task);
		taskRepository.delete(task);
		phaseRepository.save(phase);
	}

	ChecklistEntity getChecklistById(final String id) {
		return checklistRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND));
	}

	PhaseEntity getPhaseInChecklist(final ChecklistEntity checklist, final String phaseId) {
		return checklist.getPhases().stream()
			.filter(phase -> phase.getId().equals(phaseId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, PHASE_NOT_FOUND));
	}

	TaskEntity getTaskInPhase(final PhaseEntity phase, final String taskId) {
		return phase.getTasks().stream()
			.filter(task -> task.getId().equals(taskId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, TASK_NOT_FOUND));
	}

}
