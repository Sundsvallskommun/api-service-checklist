package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toPhase;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toPhaseEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toPhases;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updatePhaseEntity;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.api.model.PhaseCreateRequest;
import se.sundsvall.checklist.api.model.PhaseUpdateRequest;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;
import se.sundsvall.checklist.integration.db.repository.TaskRepository;

@Service
public class PhaseService {

	private static final String PHASE_NOT_FOUND = "Phase not found within municipality %s";
	private static final String PHASE_CONTAINS_TASKS = "Phase can not be deleted as it has tasks connected to it";

	private final PhaseRepository phaseRepository;
	private final TaskRepository taskRepository;
	private final CustomTaskRepository customTaskRepository;

	public PhaseService(final PhaseRepository phaseRepository, final TaskRepository taskRepository, final CustomTaskRepository customTaskRepository) {
		this.phaseRepository = phaseRepository;
		this.taskRepository = taskRepository;
		this.customTaskRepository = customTaskRepository;
	}

	public List<Phase> getPhases(final String municipalityId) {
		return toPhases(phaseRepository.findAllByMunicipalityId(municipalityId));
	}

	public Phase getPhase(final String municipalityId, final String phaseId) {
		final var phase = getPhaseEntity(municipalityId, phaseId);
		return toPhase(phase);
	}

	public Phase createPhase(final String municipalityId, final PhaseCreateRequest request) {
		final var phaseEntity = toPhaseEntity(request, municipalityId);
		phaseRepository.save(phaseEntity);
		return toPhase(phaseEntity);
	}

	public Phase updatePhase(final String municipalityId, final String id, final PhaseUpdateRequest request) {
		final var phase = getPhaseEntity(municipalityId, id);
		return toPhase(phaseRepository.save(updatePhaseEntity(phase, request)));
	}

	public void deletePhase(final String municipalityId, final String id) {
		final var phase = getPhaseEntity(municipalityId, id);
		if (taskRepository.countByPhaseId(phase.getId()) > 0 || customTaskRepository.countByPhaseId(phase.getId()) > 0) {
			throw Problem.valueOf(CONFLICT, PHASE_CONTAINS_TASKS);
		}
		phaseRepository.delete(phase);
	}

	private PhaseEntity getPhaseEntity(final String municipalityId, final String id) {
		return phaseRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, PHASE_NOT_FOUND.formatted(municipalityId)));
	}
}
