package se.sundsvall.checklist.service.util;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.PHASE;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.TASK;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;

public class SortingUtils {
	private SortingUtils() {}

	/**
	 * Helper method for applying custom sort order to a checklist
	 *
	 * @param checklist  the checklist to apply custom sorting to
	 * @param customSort the custom sort order to apply
	 */
	public static void applyCustomSortorder(final Checklist checklist, final List<SortorderEntity> customSort) {
		customSort.stream()
			.filter(entity -> PHASE == entity.getComponentType())
			.forEach(entity -> ofNullable(checklist.getPhases()).orElse(emptyList()).stream()
				.filter(phase -> phase.getId().equals(entity.getComponentId()))
				.findAny()
				.ifPresent(phase -> phase.setSortOrder(entity.getPosition())));

		customSort.stream()
			.filter(entity -> TASK == entity.getComponentType())
			.forEach(entity -> ofNullable(checklist.getPhases()).orElse(emptyList()).stream()
				.map(Phase::getTasks)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.filter(task -> task.getId().equals(entity.getComponentId()))
				.findAny()
				.ifPresent(task -> task.setSortOrder(entity.getPosition())));
	}

	/**
	 * Helper method for applying custom sort order to a list of tasks
	 *
	 * @param tasks      the list of tasks to apply custom sort order to
	 * @param customSort the custom sort order to apply
	 */
	public static void applyCustomSortorder(final List<Task> tasks, final List<SortorderEntity> customSort) {
		customSort.stream()
			.filter(entity -> TASK == entity.getComponentType())
			.forEach(entity -> ofNullable(tasks).orElse(emptyList()).stream()
				.filter(task -> task.getId().equals(entity.getComponentId()))
				.findAny()
				.ifPresent(task -> task.setSortOrder(entity.getPosition())));
	}

	/**
	 * Helper method for arranging phases and their respective tasks in the correct order for employee checklists
	 *
	 * @param  phases the list of phases to sort
	 * @return        the sorted list
	 */
	public static List<Phase> sortPhases(final List<Phase> phases) {
		ofNullable(phases).orElse(emptyList())
			.forEach(phase -> phase.setTasks(sortTasks(phase.getTasks())));

		return ofNullable(phases).orElse(emptyList()).stream()
			.sorted(SortingUtils::compare)
			.toList();
	}

	private static int compare(final Phase a, final Phase b) {
		final var order = a.getSortOrder() - b.getSortOrder();
		return order != 0 ? order : a.getName().compareTo(b.getName());
	}

	/**
	 * Helper method for arranging tasks in the correct order for employee checklists
	 *
	 * @param  tasks the list of tasks to sort
	 * @return       the sorted list
	 */
	public static List<Task> sortTasks(final List<Task> tasks) {
		return ofNullable(tasks).orElse(emptyList()).stream()
			.sorted(SortingUtils::compare)
			.toList();
	}

	private static int compare(final Task a, final Task b) {
		final var order = a.getSortOrder() - b.getSortOrder();
		return order != 0 ? order : a.getHeading().compareTo(b.getHeading());
	}

	/**
	 * Helper method for applying custom sort order to an employee checklist
	 *
	 * @param employeeChecklist the employee checklist to apply custom sorting to
	 * @param customSort        the custom sort order to apply
	 */
	public static void applyCustomSortorder(final EmployeeChecklist employeeChecklist, final List<SortorderEntity> customSort) {
		customSort.stream()
			.filter(entity -> PHASE == entity.getComponentType())
			.forEach(entity -> ofNullable(employeeChecklist.getPhases()).orElse(emptyList()).stream()
				.filter(phase -> phase.getId().equals(entity.getComponentId()))
				.findAny()
				.ifPresent(phase -> phase.setSortOrder(entity.getPosition())));

		customSort.stream()
			.filter(entity -> TASK == entity.getComponentType())
			.forEach(entity -> ofNullable(employeeChecklist.getPhases()).orElse(emptyList()).stream()
				.map(EmployeeChecklistPhase::getTasks)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.filter(task -> task.getId().equals(entity.getComponentId()))
				.findAny()
				.ifPresent(task -> task.setSortOrder(entity.getPosition())));
	}

	/**
	 * Helper method for arranging phases and their respective tasks in the correct order for employee checklists
	 *
	 * @param  phases the list of phases to sort
	 * @return        the sorted list
	 */
	public static List<EmployeeChecklistPhase> sortEmployeeChecklistPhases(final List<EmployeeChecklistPhase> phases) {
		ofNullable(phases).orElse(emptyList())
			.forEach(phase -> phase.setTasks(sortEmployeeChecklistTasks(phase.getTasks())));

		return ofNullable(phases).orElse(emptyList()).stream()
			.sorted(SortingUtils::compare)
			.toList();
	}

	private static int compare(final EmployeeChecklistPhase a, final EmployeeChecklistPhase b) {
		final var order = a.getSortOrder() - b.getSortOrder();
		return order != 0 ? order : a.getName().compareTo(b.getName());
	}

	/**
	 * Helper method for arranging tasks in the correct order for employee checklists
	 *
	 * @param  tasks the list of tasks to sort
	 * @return       the sorted list
	 */
	public static List<EmployeeChecklistTask> sortEmployeeChecklistTasks(final List<EmployeeChecklistTask> tasks) {
		return ofNullable(tasks).orElse(emptyList()).stream()
			.sorted(SortingUtils::compare)
			.toList();
	}

	private static int compare(final EmployeeChecklistTask a, final EmployeeChecklistTask b) {
		final var order = a.getSortOrder() - b.getSortOrder();
		return order != 0 ? order : a.getHeading().compareTo(b.getHeading());
	}

	/**
	 * Helper method to retrieve id:s for all checklist components that is present in the provided checklist entity
	 *
	 * @param  checklistEntity the entity to fetch component id:s from
	 * @return                 a list of component id:s that are contained within provided checklist entity
	 */
	public static List<String> getChecklistItemIds(final ChecklistEntity checklistEntity) {
		final var sortorderItems = ofNullable(checklistEntity.getTasks()).orElse(emptyList())
			.stream()
			.map(TaskEntity::getId)
			.collect(Collectors.toCollection(ArrayList::new));

		sortorderItems.addAll(ofNullable(checklistEntity.getTasks()).orElse(emptyList())
			.stream()
			.map(TaskEntity::getPhase)
			.map(PhaseEntity::getId)
			.distinct()
			.toList());

		return sortorderItems;
	}
}
