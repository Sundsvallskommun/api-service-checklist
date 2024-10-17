package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toPhase;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toPhaseEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toPhases;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updatePhaseEntity;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.api.model.PhaseCreateRequest;
import se.sundsvall.checklist.api.model.PhaseUpdateRequest;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;

@Service
public class PhaseService {

	private static final String CHECKLIST_NOT_FOUND = "Checklist not found";
	private static final String PHASE_NOT_FOUND = "Phase not found";

	private final PhaseRepository phaseRepository;
	private final ChecklistRepository checklistRepository;

	public PhaseService(final PhaseRepository phaseRepository, final ChecklistRepository checklistRepository) {
		this.phaseRepository = phaseRepository;
		this.checklistRepository = checklistRepository;
	}

	public List<Phase> getChecklistPhases(final String checklistId) {
		var checklist = getChecklistById(checklistId);
		return toPhases(checklist.getPhases());
	}

	public Phase getChecklistPhase(final String checklistId, final String phaseId) {
		var checklist = getChecklistById(checklistId);
		var phase = getPhaseInChecklist(checklist, phaseId);
		return toPhase(phase);
	}

	@Transactional
	public Phase createChecklistPhase(final String checklistId, final PhaseCreateRequest request) {
		var checklist = getChecklistById(checklistId);
		var phaseEntity = toPhaseEntity(request);
		phaseRepository.save(phaseEntity);
		checklist.getPhases().add(phaseEntity);
		checklistRepository.save(checklist);
		return toPhase(phaseEntity);
	}

	public Phase updateChecklistPhase(final String checklistId, final String phaseId, final PhaseUpdateRequest request) {
		var checklist = getChecklistById(checklistId);
		var phase = getPhaseInChecklist(checklist, phaseId);
		return toPhase(phaseRepository.save(updatePhaseEntity(phase, request)));
	}

	@Transactional
	public void deleteChecklistPhase(final String checklistId, final String phaseId) {
		var checklist = getChecklistById(checklistId);
		var phase = getPhaseInChecklist(checklist, phaseId);
		checklist.getPhases().remove(phase);
		phaseRepository.delete(phase);
		checklistRepository.save(checklist);
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
}
