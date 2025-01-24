package se.sundsvall.checklist.service.util;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus.EMPTY;
import static se.sundsvall.checklist.service.util.TaskType.COMMON;
import static se.sundsvall.checklist.service.util.TaskType.CUSTOM;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.zalando.problem.Problem;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;

public final class ServiceUtils {
	private static final String NO_MATCHING_EMPLOYEE_CHECKLIST_TASK_FOUND = "Task with id %s was not found in employee checklist with id %s.";
	private static final String NO_MAIN_EMPLOYMENT_FOUND = "No main employment found for employee with loginname %s.";

	private ServiceUtils() {}

	public static TaskType calculateTaskType(EmployeeChecklistEntity employeeChecklist, String taskId) {
		return isCommonTask(taskId, employeeChecklist)
			.or(() -> isCustomTask(taskId, employeeChecklist))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_MATCHING_EMPLOYEE_CHECKLIST_TASK_FOUND.formatted(taskId, employeeChecklist.getId())));
	}

	private static Optional<TaskType> isCommonTask(String taskId, final EmployeeChecklistEntity employeeChecklist) {
		final var found = ofNullable(employeeChecklist.getChecklists()).orElse(emptyList()).stream()
			.map(ChecklistEntity::getTasks)
			.flatMap(List::stream)
			.anyMatch(task -> Objects.equals(task.getId(), taskId));

		return found ? Optional.of(COMMON) : Optional.empty();
	}

	private static Optional<TaskType> isCustomTask(String taskId, final EmployeeChecklistEntity employeeChecklist) {
		final var found = ofNullable(employeeChecklist.getCustomTasks()).orElse(emptyList()).stream()
			.anyMatch(task -> Objects.equals(task.getId(), taskId));

		return found ? Optional.of(CUSTOM) : Optional.empty();
	}

	public static Employment getMainEmployment(Employee employee) {
		return ofNullable(employee.getEmployments()).orElse(emptyList()).stream()
			.filter(employment -> Objects.nonNull(employment.getIsMainEmployment()))
			.filter(Employment::getIsMainEmployment)
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_MAIN_EMPLOYMENT_FOUND.formatted(employee.getLoginname())));
	}

	/**
	 * Method checks if all tasks in the employee checklist are marked as completed
	 *
	 * @param  employeeChecklist the employee onboarding to check
	 * @return                   true if all tasks are completed, false otherwise
	 */
	public static boolean allTasksAreCompleted(EmployeeChecklistEntity employeeChecklist) {
		// Collect task ids from all tasks and custom tasks in the employee checklist
		final var taskIds = ofNullable(employeeChecklist.getChecklists()).orElse(emptyList()).stream()
			.map(ChecklistEntity::getTasks)
			.flatMap(List::stream)
			.map(TaskEntity::getId)
			.collect(Collectors.toCollection(ArrayList::new));

		taskIds.addAll(ofNullable(employeeChecklist.getCustomTasks()).orElse(emptyList()).stream()
			.map(CustomTaskEntity::getId)
			.toList());

		// Collect task ids for all task and custom tasks marked as completed by the employee or manager
		final var completedTaskIds = ofNullable(employeeChecklist.getFulfilments()).orElse(emptyList()).stream()
			.filter(fulfilment -> !Objects.equals(fulfilment.getCompleted(), EMPTY)) // // tasks shall have a status with either TRUE or FALSE as value to be considered as completed
			.map(FulfilmentEntity::getTask)
			.map(TaskEntity::getId)
			.collect(Collectors.toCollection(ArrayList::new));

		completedTaskIds.addAll(ofNullable(employeeChecklist.getCustomFulfilments()).orElse(emptyList()).stream()
			.filter(fulfilment -> !Objects.equals(fulfilment.getCompleted(), EMPTY)) // // tasks shall have a status with either TRUE or FALSE as value to be considered as completed
			.map(CustomFulfilmentEntity::getCustomTask)
			.map(CustomTaskEntity::getId)
			.toList());

		return completedTaskIds.containsAll(taskIds);
	}

	public static Optional<EmployeeChecklistEntity> fetchEntity(List<EmployeeChecklistEntity> entities, String id) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.filter(entity -> Objects.equals(id, entity.getId()))
			.findAny();
	}
}
