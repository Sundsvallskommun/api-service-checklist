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

	private static final String CHECKLIST_NOT_FOUND = "Checklist not found within municipality %s";
	private static final String PHASE_NOT_FOUND = "Phase not found within municipality %s";
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

	public List<Task> getTasks(final String municipalityId, final String checklistId, final String phaseId) {
		final var checklist = getChecklist(municipalityId, checklistId);
		verifyPhaseIsPresent(municipalityId, phaseId); // This is here to verify that sent in phase id is present in database

		return toTasks(checklist.getTasks().stream()
			.filter(task -> task.getPhase().getId().equals(phaseId))
			.toList());
	}

	public Task getTask(final String municipalityId, final String checklistId, final String phaseId, final String taskId) {
		final var checklist = getChecklist(municipalityId, checklistId);
		verifyPhaseIsPresent(municipalityId, phaseId); // This is here to verify that sent in phase id is present in database
		final var task = getTaskInPhase(checklist, phaseId, taskId);

		return toTask(task);
	}

	@Transactional
	public Task createTask(final String municipalityId, final String checklistId, final String phaseId, final TaskCreateRequest request) {
		final var checklist = getChecklist(municipalityId, checklistId);
		final var phase = getPhase(municipalityId, phaseId);
		final var task = taskRepository.save(toTaskEntity(request, phase));
		checklist.getTasks().add(task);
		checklistRepository.save(checklist);

		return toTask(task);
	}

	public Task updateTask(final String municipalityId, final String checklistId, final String phaseId, final String taskId, final TaskUpdateRequest request) {
		final var checklist = getChecklist(municipalityId, checklistId);
		verifyPhaseIsPresent(municipalityId, phaseId); // This is here to verify that sent in phase id is present in database
		final var task = getTaskInPhase(checklist, phaseId, taskId);

		return toTask(taskRepository.save(updateTaskEntity(task, request)));
	}

	@Transactional
	public void deleteTask(final String municipalityId, final String checklistId, final String phaseId, final String taskId) {
		final var checklist = getChecklist(municipalityId, checklistId);
		final var phase = getPhase(municipalityId, phaseId); // This is here to verify that sent in phase id is present in database
		final var task = getTaskInPhase(checklist, phase.getId(), taskId);
		taskRepository.delete(task);
	}

	private ChecklistEntity getChecklist(final String municipalityId, final String id) {
		return checklistRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));
	}

	private PhaseEntity getPhase(final String municipalityId, final String id) {
		return phaseRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, PHASE_NOT_FOUND.formatted(municipalityId)));
	}

	private void verifyPhaseIsPresent(final String municipalityId, final String id) {
		if (!phaseRepository.existsByIdAndMunicipalityId(id, municipalityId)) {
			throw Problem.valueOf(NOT_FOUND, PHASE_NOT_FOUND.formatted(municipalityId));
		}
	}

	private TaskEntity getTaskInPhase(final ChecklistEntity checklist, final String phaseId, final String taskId) {
		return checklist.getTasks().stream()
			.filter(task -> task.getPhase().getId().equals(phaseId))
			.filter(task -> task.getId().equals(taskId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, TASK_NOT_FOUND));
	}
}
