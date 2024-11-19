package se.sundsvall.checklist.integration.db;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.flywaydb.core.internal.util.CollectionsUtils.hasItems;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toChecklist;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toPhases;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;

@Component
public class ChecklistBuilder {

	private final PhaseRepository phaseRepository;

	ChecklistBuilder(PhaseRepository phaseRepository) {
		this.phaseRepository = phaseRepository;
	}

	public Checklist buildChecklist(final ChecklistEntity checklistEntity) {
		return Stream.ofNullable(toChecklist(checklistEntity))
			.map(checklist -> populateWithPhases(checklist, phaseRepository.findAllByMunicipalityId(checklistEntity.getMunicipalityId())))
			.map(checklist -> populateWithTasks(checklist, checklistEntity.getTasks()))
			.findAny()
			.orElse(null);
	}

	private Checklist populateWithPhases(final Checklist checklist, List<PhaseEntity> entities) {
		checklist.setPhases(toPhases(entities));
		return checklist;
	}

	private Checklist populateWithTasks(final Checklist checklist, List<TaskEntity> taskEntities) {
		ofNullable(taskEntities).orElse(emptyList())
			.stream()
			.forEach(taskEntity -> ofNullable(checklist.getPhases()).orElse(emptyList()).stream()
				.filter(phase -> phase.getId().equals(taskEntity.getPhase().getId()))
				.findFirst()
				.ifPresent(phase -> addTaskToPhase(phase, taskEntity)));

		ofNullable(checklist.getPhases()).orElse(emptyList()).stream()
			.filter(phase -> hasItems(phase.getTasks()))
			.forEach(phase -> phase.getTasks().sort((a1, a2) -> a1.getSortOrder() - a2.getSortOrder())); // Sort tasks when all items are collected

		return checklist;
	}

	private void addTaskToPhase(Phase phase, TaskEntity taskEntity) {
		if (Objects.isNull(phase.getTasks())) {
			phase.setTasks(new ArrayList<>());
		}
		phase.getTasks().add(toTask(taskEntity));
	}
}
