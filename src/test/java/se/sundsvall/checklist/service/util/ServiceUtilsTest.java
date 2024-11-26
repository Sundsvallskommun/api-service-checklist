package se.sundsvall.checklist.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;

class ServiceUtilsTest {

	@Test
	void calculateTaskTypeForCommonTask() {
		// Arrange
		final var taskId = UUID.randomUUID().toString();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder()
				.withTasks(List.of(TaskEntity.builder()
					.withId(taskId)
					.build()))
				.build()))
			.build();

		// Act and assert
		assertThat(ServiceUtils.calculateTaskType(employeeChecklist, taskId)).isEqualTo(TaskType.COMMON);
	}

	@Test
	void calculateTaskTypeForCustomTask() {
		// Arrange
		final var taskId = UUID.randomUUID().toString();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withCustomTasks(List.of(CustomTaskEntity.builder()
				.withId(taskId)
				.build()))
			.build();

		// Act and assert
		assertThat(ServiceUtils.calculateTaskType(employeeChecklist, taskId)).isEqualTo(TaskType.CUSTOM);
	}

	@Test
	void calculateTaskTypeForMissingTask() {
		// Arrange
		final var taskId = UUID.randomUUID().toString();
		final var employeeChecklist = EmployeeChecklistEntity.builder().withId(UUID.randomUUID().toString()).build();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> ServiceUtils.calculateTaskType(employeeChecklist, taskId));

		// Assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Task with id %s was not found in employee checklist with id %s.".formatted(taskId, employeeChecklist.getId()));
	}

	@Test
	void getMainEmployment() {
		// Arrange
		final var mainEmployment = new Employment().isMainEmployment(true);
		final var sideEmployment = new Employment();
		final var employee = new Employee().employments(List.of(sideEmployment, mainEmployment));

		// Act and assert
		assertThat(ServiceUtils.getMainEmployment(employee)).isEqualTo(mainEmployment);
	}

	@Test
	void getMainEmploymentWhenMainEmploymentSignalIsNull() {
		// Arrange
		final var sideEmployment = new Employment();
		final var username = "username";
		final var employee = new Employee().loginname(username).employments(List.of(sideEmployment));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> ServiceUtils.getMainEmployment(employee));

		// Assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No main employment found for employee with loginname %s.".formatted(username));
	}

	@Test
	void getMainEmploymentWhenEmploymentsAreNull() {
		// Arrange
		final var username = "username";
		final var employee = new Employee().loginname(username);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> ServiceUtils.getMainEmployment(employee));

		// Assert
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No main employment found for employee with loginname %s.".formatted(username));
	}

	@Test
	void calculateCompletedWhenAllFulfilmentsAreTrueOrFalse() {
		final var employeeChecklist = EmployeeChecklist.builder()
			.withPhases(List.of(EmployeeChecklistPhase.builder()
				.withTasks(List.of(
					EmployeeChecklistTask.builder()
						.withFulfilmentStatus(FulfilmentStatus.TRUE)
						.build(),
					EmployeeChecklistTask.builder()
						.withFulfilmentStatus(FulfilmentStatus.FALSE)
						.build()))
				.build()))
			.build();

		ServiceUtils.calculateCompleted(employeeChecklist);

		assertThat(employeeChecklist.getCompleted()).isTrue();
	}

	@Test
	void calculateCompletedWhenSomeFulfilmentsHaveEmptyValue() {
		final var employeeChecklist = EmployeeChecklist.builder()
			.withPhases(List.of(EmployeeChecklistPhase.builder()
				.withTasks(List.of(
					EmployeeChecklistTask.builder()
						.withFulfilmentStatus(FulfilmentStatus.TRUE)
						.build(),
					EmployeeChecklistTask.builder()
						.withFulfilmentStatus(FulfilmentStatus.FALSE)
						.build(),
					EmployeeChecklistTask.builder()
						.withFulfilmentStatus(FulfilmentStatus.EMPTY)
						.build()))
				.build()))
			.build();

		ServiceUtils.calculateCompleted(employeeChecklist);

		assertThat(employeeChecklist.getCompleted()).isFalse();
	}

	@Test
	void calculateCompletedWhenSomeTasksHasNoFulfilmentsData() {
		final var employeeChecklist = EmployeeChecklist.builder()
			.withPhases(List.of(EmployeeChecklistPhase.builder()
				.withTasks(List.of(
					EmployeeChecklistTask.builder()
						.withFulfilmentStatus(FulfilmentStatus.TRUE)
						.build(),
					EmployeeChecklistTask.builder()
						.withFulfilmentStatus(FulfilmentStatus.FALSE)
						.build(),
					EmployeeChecklistTask.builder()
						.build()))
				.build()))
			.build();

		ServiceUtils.calculateCompleted(employeeChecklist);

		assertThat(employeeChecklist.getCompleted()).isFalse();

	}

	@Test
	void fetchExistingEntity() {
		final var wantedEntityId = UUID.randomUUID().toString();
		final var entity = EmployeeChecklistEntity.builder().withId(wantedEntityId).build();

		assertThat(ServiceUtils.fetchEntity(List.of(
			EmployeeChecklistEntity.builder().withId(UUID.randomUUID().toString()).build(),
			entity), wantedEntityId)).isPresent().get().isEqualTo(entity);
	}

	@Test
	void fetchNonExistingEntity() {
		final var wantedEntityId = UUID.randomUUID().toString();
		final var entity = EmployeeChecklistEntity.builder().withId(UUID.randomUUID().toString()).build();

		assertThat(ServiceUtils.fetchEntity(List.of(entity), wantedEntityId)).isEmpty();
	}

	@Test
	void fetchEntityFromNull() {
		assertThat(ServiceUtils.fetchEntity(null, "id")).isEmpty();
	}
}
