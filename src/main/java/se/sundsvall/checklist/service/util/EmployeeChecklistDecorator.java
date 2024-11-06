package se.sundsvall.checklist.service.util;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toEmployeeChecklistTask;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;

public final class EmployeeChecklistDecorator {
	private EmployeeChecklistDecorator() {}

	public static EmployeeChecklist decorateWithCustomTasks(EmployeeChecklist employeeChecklist, List<CustomTaskEntity> customTasks) {
		ofNullable(customTasks).orElse(emptyList()).stream()
			.forEach(customTaskEntity -> addToPhase(customTaskEntity, employeeChecklist));

		return employeeChecklist;
	}

	private static void addToPhase(CustomTaskEntity entity, EmployeeChecklist employeeChecklist) {
		employeeChecklist.getPhases().stream()
			.filter(phase -> Objects.equals(phase.getId(), entity.getPhase().getId()))
			.findAny()
			.ifPresent(phase -> {
				phase.getTasks().add(toEmployeeChecklistTask(entity));
				phase.getTasks().sort(comparing(EmployeeChecklistTask::getSortOrder));
			});
	}

	public static EmployeeChecklist decorateWithFulfilment(EmployeeChecklist employeeChecklist, Optional<EmployeeChecklistEntity> employeeChecklistEntity) {
		employeeChecklistEntity.ifPresent(entity -> {
			ofNullable(entity.getFulfilments()).orElse(emptyList()).stream()
				.forEach(fulfilment -> addFulfilmentToTasksInPhases(employeeChecklist.getPhases(), fulfilment));

			ofNullable(entity.getCustomFulfilments()).orElse(emptyList()).stream()
				.forEach(fulfilment -> addFulfilmentToTasksInPhases(employeeChecklist.getPhases(), fulfilment));
		});

		return employeeChecklist;
	}

	private static void addFulfilmentToTasksInPhases(List<EmployeeChecklistPhase> phases, FulfilmentEntity fulfilment) {
		ofNullable(phases).orElse(emptyList()).stream()
			.map(EmployeeChecklistPhase::getTasks)
			.flatMap(List::stream)
			.filter(task -> Objects.equals(task.getId(), fulfilment.getTask().getId()))
			.findAny()
			.ifPresent(task -> decorateWithFulfilment(task, fulfilment));
	}

	private static void addFulfilmentToTasksInPhases(List<EmployeeChecklistPhase> phases, CustomFulfilmentEntity fulfilment) {
		ofNullable(phases).orElse(emptyList()).stream()
			.map(EmployeeChecklistPhase::getTasks)
			.flatMap(List::stream)
			.filter(task -> Objects.equals(task.getId(), fulfilment.getCustomTask().getId()))
			.findAny()
			.ifPresent(task -> decorateWithFulfilment(task, fulfilment));
	}

	public static EmployeeChecklistPhase decorateWithCustomTasks(EmployeeChecklistPhase phase, List<CustomTaskEntity> customTasks) {
		ofNullable(customTasks).orElse(emptyList()).stream()
			.filter(cte -> Objects.equals(phase.getId(), cte.getPhase().getId()))
			.forEach(customTaskEntity -> addToPhase(customTaskEntity, phase));

		return phase;
	}

	private static void addToPhase(CustomTaskEntity entity, EmployeeChecklistPhase phase) {
		phase.getTasks().add(toEmployeeChecklistTask(entity));
		phase.getTasks().sort(comparing(EmployeeChecklistTask::getSortOrder));
	}

	public static EmployeeChecklistPhase decorateWithFulfilment(EmployeeChecklistPhase phase, EmployeeChecklistEntity employeeChecklistEntity) {
		ofNullable(employeeChecklistEntity.getFulfilments()).orElse(emptyList()).stream()
			.forEach(fulfilment -> addFulfilmentToTasks(phase.getTasks(), fulfilment));

		ofNullable(employeeChecklistEntity.getCustomFulfilments()).orElse(emptyList()).stream()
			.forEach(fulfilment -> addFulfilmentToTasks(phase.getTasks(), fulfilment));

		return phase;
	}

	private static void addFulfilmentToTasks(List<EmployeeChecklistTask> tasks, FulfilmentEntity fulfilment) {
		ofNullable(tasks).orElse(emptyList()).stream()
			.filter(task -> !task.isCustomTask())
			.filter(task -> Objects.equals(task.getId(), fulfilment.getTask().getId()))
			.findAny()
			.ifPresent(task -> decorateWithFulfilment(task, fulfilment));
	}

	private static void addFulfilmentToTasks(List<EmployeeChecklistTask> tasks, CustomFulfilmentEntity fulfilment) {
		ofNullable(tasks).orElse(emptyList()).stream()
			.filter(EmployeeChecklistTask::isCustomTask)
			.filter(task -> Objects.equals(task.getId(), fulfilment.getCustomTask().getId()))
			.findAny()
			.ifPresent(task -> decorateWithFulfilment(task, fulfilment));
	}

	public static EmployeeChecklistTask decorateWithFulfilment(EmployeeChecklistTask task, FulfilmentEntity fulfilment) {

		task.setFulfilmentStatus(fulfilment.getCompleted());
		task.setResponseText(fulfilment.getResponseText());
		task.setUpdated(fulfilment.getUpdated());
		task.setUpdatedBy(fulfilment.getLastSavedBy());

		return task;
	}

	public static EmployeeChecklistTask decorateWithFulfilment(EmployeeChecklistTask task, CustomFulfilmentEntity fulfilment) {

		task.setFulfilmentStatus(fulfilment.getCompleted());
		task.setResponseText(fulfilment.getResponseText());
		task.setUpdated(fulfilment.getUpdated());
		task.setUpdatedBy(fulfilment.getLastSavedBy());

		return task;
	}
}
