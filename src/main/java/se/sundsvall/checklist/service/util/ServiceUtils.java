package se.sundsvall.checklist.service.util;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus.EMPTY;
import static se.sundsvall.checklist.service.util.TaskType.COMMON;
import static se.sundsvall.checklist.service.util.TaskType.CUSTOM;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.zalando.problem.Problem;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;

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
		final var found = ofNullable(employeeChecklist.getChecklist()).orElse(ChecklistEntity.builder().build())
			.getPhases().stream()
			.map(PhaseEntity::getTasks)
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

	public static EmployeeChecklist calculateCompleted(EmployeeChecklist employeeChecklist) {
		employeeChecklist.setCompleted(
			employeeChecklist.getPhases().stream()
				.map(EmployeeChecklistPhase::getTasks)
				.flatMap(List::stream)
				.map(EmployeeChecklistTask::getFulfilmentStatus)
				.allMatch(s -> Objects.nonNull(s) && !Objects.equals(s, EMPTY))); // All tasks shall have a status with either TRUE or FALSE as value

		return employeeChecklist;
	}

	public static Optional<EmployeeChecklistEntity> fetchEntity(List<EmployeeChecklistEntity> entities, String id) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.filter(entity -> Objects.equals(id, entity.getId()))
			.findAny();
	}
}
