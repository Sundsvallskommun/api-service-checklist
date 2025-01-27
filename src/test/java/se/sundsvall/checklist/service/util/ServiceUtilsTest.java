package se.sundsvall.checklist.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
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
	void allTasksAreCompletedWhenAllFulfilmentsAreTrue() {
		final var task1 = TaskEntity.builder().withId("task_1").build();
		final var task2 = TaskEntity.builder().withId("task_2").build();
		final var customTask1 = CustomTaskEntity.builder().withId("task_3").build();
		final var customTask2 = CustomTaskEntity.builder().withId("task_4").build();

		final var fulfilment_task1 = FulfilmentEntity.builder().withTask(task1).withCompleted(FulfilmentStatus.TRUE).build();
		final var fulfilment_task2 = FulfilmentEntity.builder().withTask(task2).withCompleted(FulfilmentStatus.TRUE).build();
		final var fulfilment_custom_task1 = CustomFulfilmentEntity.builder().withCustomTask(customTask1).withCompleted(FulfilmentStatus.TRUE).build();
		final var fulfilment_custom_task2 = CustomFulfilmentEntity.builder().withCustomTask(customTask2).withCompleted(FulfilmentStatus.TRUE).build();

		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder().withTasks(List.of(task1, task2)).build()))
			.withFulfilments(List.of(fulfilment_task1, fulfilment_task2))
			.withCustomTasks(List.of(customTask1, customTask2))
			.withCustomFulfilments(List.of(fulfilment_custom_task1, fulfilment_custom_task2))
			.build();

		assertThat(ServiceUtils.allTasksAreCompleted(employeeChecklistEntity)).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = FulfilmentStatus.class, mode = Mode.EXCLUDE, names = "TRUE")
	void allTasksAreNotCompletedWhenFulfilmentsHaveFalseOrEmptyValue(FulfilmentStatus fulfilmentStatus) {
		final var task1 = TaskEntity.builder().withId("task_1").build();
		final var task2 = TaskEntity.builder().withId("task_2").build();
		final var customTask1 = CustomTaskEntity.builder().withId("task_3").build();

		final var fulfilment_task1 = FulfilmentEntity.builder().withTask(task1).withCompleted(fulfilmentStatus).build();
		final var fulfilment_task2 = FulfilmentEntity.builder().withTask(task2).withCompleted(FulfilmentStatus.TRUE).build();
		final var fulfilment_custom_task1 = CustomFulfilmentEntity.builder().withCustomTask(customTask1).withCompleted(FulfilmentStatus.TRUE).build();

		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder().withTasks(List.of(task1, task2)).build()))
			.withFulfilments(List.of(fulfilment_task1, fulfilment_task2))
			.withCustomTasks(List.of(customTask1))
			.withCustomFulfilments(List.of(fulfilment_custom_task1))
			.build();

		assertThat(ServiceUtils.allTasksAreCompleted(employeeChecklistEntity)).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = FulfilmentStatus.class, mode = Mode.EXCLUDE, names = "TRUE")
	void allTasksAreNotCompletedWhenCustomFulfilmentsHaveFalseOrEmptyValue(FulfilmentStatus fulfilmentStatus) {
		final var task1 = TaskEntity.builder().withId("task_1").build();
		final var customTask1 = CustomTaskEntity.builder().withId("task_3").build();
		final var customTask2 = CustomTaskEntity.builder().withId("task_4").build();

		final var fulfilment_task1 = FulfilmentEntity.builder().withTask(task1).withCompleted(FulfilmentStatus.TRUE).build();
		final var fulfilment_custom_task1 = CustomFulfilmentEntity.builder().withCustomTask(customTask1).withCompleted(FulfilmentStatus.TRUE).build();
		final var fulfilment_custom_task2 = CustomFulfilmentEntity.builder().withCustomTask(customTask2).withCompleted(fulfilmentStatus).build();

		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder().withTasks(List.of(task1)).build()))
			.withFulfilments(List.of(fulfilment_task1))
			.withCustomTasks(List.of(customTask1, customTask2))
			.withCustomFulfilments(List.of(fulfilment_custom_task1, fulfilment_custom_task2))
			.build();

		assertThat(ServiceUtils.allTasksAreCompleted(employeeChecklistEntity)).isFalse();
	}

	@Test
	void allTasksAreNotCompletedWhenTasksHasNoFulfilmentsData() {
		final var task1 = TaskEntity.builder().withId("task_1").build();
		final var task2 = TaskEntity.builder().withId("task_2").build();
		final var customTask1 = CustomTaskEntity.builder().withId("task_3").build();

		final var fulfilment_task1 = FulfilmentEntity.builder().withTask(task1).withCompleted(FulfilmentStatus.TRUE).build();
		final var fulfilment_custom_task1 = CustomFulfilmentEntity.builder().withCustomTask(customTask1).withCompleted(FulfilmentStatus.TRUE).build();

		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder().withTasks(List.of(task1, task2)).build()))
			.withFulfilments(List.of(fulfilment_task1))
			.withCustomTasks(List.of(customTask1))
			.withCustomFulfilments(List.of(fulfilment_custom_task1))
			.build();

		assertThat(ServiceUtils.allTasksAreCompleted(employeeChecklistEntity)).isFalse();
	}

	@Test
	void allTasksAreNotCompletedWhenCustomTasksHasNoFulfilmentsData() {
		final var task1 = TaskEntity.builder().withId("task_1").build();
		final var customTask1 = CustomTaskEntity.builder().withId("task_3").build();
		final var customTask2 = CustomTaskEntity.builder().withId("task_4").build();

		final var fulfilment_task1 = FulfilmentEntity.builder().withTask(task1).withCompleted(FulfilmentStatus.TRUE).build();
		final var fulfilment_custom_task1 = CustomFulfilmentEntity.builder().withCustomTask(customTask1).withCompleted(FulfilmentStatus.TRUE).build();

		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withChecklists(List.of(ChecklistEntity.builder().withTasks(List.of(task1)).build()))
			.withFulfilments(List.of(fulfilment_task1))
			.withCustomTasks(List.of(customTask1, customTask2))
			.withCustomFulfilments(List.of(fulfilment_custom_task1))
			.build();

		assertThat(ServiceUtils.allTasksAreCompleted(employeeChecklistEntity)).isFalse();
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
