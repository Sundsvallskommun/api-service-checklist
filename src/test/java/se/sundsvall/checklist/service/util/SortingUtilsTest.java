package se.sundsvall.checklist.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.PHASE;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.TASK;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
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

class SortingUtilsTest {

	private static final String PHASE_ID1 = UUID.randomUUID().toString();
	private static final String PHASE_ID2 = UUID.randomUUID().toString();
	private static final String TASK_ID1 = UUID.randomUUID().toString();
	private static final String TASK_ID2 = UUID.randomUUID().toString();
	private static final String TASK_ID3 = UUID.randomUUID().toString();

	@Test
	void applyCustomSortorderToEmployeeChecklist() {
		final var employeeChecklist = EmployeeChecklist.builder()
			.withPhases(List.of(
				EmployeeChecklistPhase.builder()
					.withId(PHASE_ID1)
					.withSortOrder(1)
					.withTasks(List.of(
						EmployeeChecklistTask.builder()
							.withId(TASK_ID1)
							.withSortOrder(2)
							.build()))
					.build()))
			.build();

		SortingUtils.applyCustomSortorder(employeeChecklist, createCustomorder());

		assertThat(employeeChecklist.getPhases().getFirst().getSortOrder()).isEqualTo(101);
		assertThat(employeeChecklist.getPhases().getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(202);
	}

	@Test
	void sortEmployeeChecklistPhasesWhenDifferentOrder() {
		final var phases = List.of(
			EmployeeChecklistPhase.builder()
				.withId(PHASE_ID1)
				.withSortOrder(2)
				.withTasks(List.of(
					EmployeeChecklistTask.builder()
						.withId(TASK_ID1)
						.withSortOrder(2)
						.build(),
					EmployeeChecklistTask.builder()
						.withId(TASK_ID2)
						.withSortOrder(1)
						.build()))
				.build(),
			EmployeeChecklistPhase.builder()
				.withId(PHASE_ID2)
				.withSortOrder(1)
				.build());

		assertThat(SortingUtils.sortEmployeeChecklistPhases(phases)).satisfiesExactly(
			phase -> {
				assertThat(phase.getId()).isEqualTo(PHASE_ID2);
				assertThat(phase.getSortOrder()).isEqualTo(1);
			},
			phase -> {
				assertThat(phase.getId()).isEqualTo(PHASE_ID1);
				assertThat(phase.getSortOrder()).isEqualTo(2);
				assertThat(phase.getTasks().getFirst().getId()).isEqualTo(TASK_ID2);
				assertThat(phase.getTasks().getFirst().getSortOrder()).isEqualTo(1);
				assertThat(phase.getTasks().getLast().getId()).isEqualTo(TASK_ID1);
				assertThat(phase.getTasks().getLast().getSortOrder()).isEqualTo(2);
			});
	}

	@Test
	void sortEmployeeChecklistPhasesWhenSameOrder() {
		final var phases = List.of(
			EmployeeChecklistPhase.builder()
				.withId(PHASE_ID1)
				.withSortOrder(1)
				.withName("Phase 1")
				.withTasks(List.of(
					EmployeeChecklistTask.builder()
						.withId(TASK_ID1)
						.withSortOrder(1)
						.withHeading("Task 1")
						.build(),
					EmployeeChecklistTask.builder()
						.withId(TASK_ID2)
						.withSortOrder(1)
						.withHeading("Task 2")
						.build()))
				.build(),
			EmployeeChecklistPhase.builder()
				.withId(PHASE_ID2)
				.withSortOrder(1)
				.withName("Phase 2")
				.build());

		assertThat(SortingUtils.sortEmployeeChecklistPhases(phases)).satisfiesExactly(
			phase -> {
				assertThat(phase.getId()).isEqualTo(PHASE_ID1);
				assertThat(phase.getSortOrder()).isEqualTo(1);
				assertThat(phase.getTasks().getFirst().getId()).isEqualTo(TASK_ID1);
				assertThat(phase.getTasks().getFirst().getSortOrder()).isEqualTo(1);
				assertThat(phase.getTasks().getLast().getId()).isEqualTo(TASK_ID2);
				assertThat(phase.getTasks().getLast().getSortOrder()).isEqualTo(1);
			},
			phase -> {
				assertThat(phase.getId()).isEqualTo(PHASE_ID2);
				assertThat(phase.getSortOrder()).isEqualTo(1);
			});
	}

	@Test
	void sortEmployeeChecklistTasksWhenDifferentOrder() {
		final var tasks = List.of(
			EmployeeChecklistTask.builder()
				.withId(TASK_ID1)
				.withSortOrder(2)
				.build(),
			EmployeeChecklistTask.builder()
				.withId(TASK_ID2)
				.withSortOrder(1)
				.build());

		assertThat(SortingUtils.sortEmployeeChecklistTasks(tasks)).satisfiesExactly(
			task -> {
				assertThat(task.getId()).isEqualTo(TASK_ID2);
				assertThat(task.getSortOrder()).isEqualTo(1);
			},
			task -> {
				assertThat(task.getId()).isEqualTo(TASK_ID1);
				assertThat(task.getSortOrder()).isEqualTo(2);
			});
	}

	@Test
	void sortEmployeeChecklistTasksWhenSameOrder() {
		final var tasks = List.of(
			EmployeeChecklistTask.builder()
				.withId(TASK_ID1)
				.withSortOrder(1)
				.withHeading("Task 1")
				.build(),
			EmployeeChecklistTask.builder()
				.withId(TASK_ID2)
				.withSortOrder(1)
				.withHeading("Task 2")
				.build());

		assertThat(SortingUtils.sortEmployeeChecklistTasks(tasks)).satisfiesExactly(
			task -> {
				assertThat(task.getId()).isEqualTo(TASK_ID1);
				assertThat(task.getSortOrder()).isEqualTo(1);
			},
			task -> {
				assertThat(task.getId()).isEqualTo(TASK_ID2);
				assertThat(task.getSortOrder()).isEqualTo(1);
			});
	}

	@Test
	void applyCustomSortorderToChecklist() {
		final var checklist = Checklist.builder()
			.withPhases(
				List.of(Phase.builder()
					.withId(PHASE_ID1)
					.withSortOrder(1)
					.withTasks(
						List.of(Task.builder()
							.withId(TASK_ID1)
							.withSortOrder(2)
							.build()))
					.build()))
			.build();

		SortingUtils.applyCustomSortorder(checklist, createCustomorder());

		assertThat(checklist.getPhases().getFirst().getSortOrder()).isEqualTo(101);
		assertThat(checklist.getPhases().getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(202);
	}

	@Test
	void applyCustomSortorderToListOfTasks() {
		final var tasks = List.of(
			Task.builder()
				.withId(TASK_ID1)
				.withSortOrder(1)
				.build(), Task.builder()
					.withId(TASK_ID2)
					.withSortOrder(2)
					.build());

		SortingUtils.applyCustomSortorder(tasks, createCustomorderForTasks());

		assertThat(tasks).satisfiesExactlyInAnyOrder(task -> {
			assertThat(task.getId()).isEqualTo(TASK_ID2);
			assertThat(task.getSortOrder()).isEqualTo(10);
		}, task -> {
			assertThat(task.getId()).isEqualTo(TASK_ID1);
			assertThat(task.getSortOrder()).isEqualTo(20);
		});
	}

	@Test
	void sortChecklistPhasesWhenDifferentOrder() {
		final var phases = List.of(
			Phase.builder()
				.withId(PHASE_ID1)
				.withSortOrder(2)
				.withTasks(List.of(
					Task.builder()
						.withId(TASK_ID1)
						.withSortOrder(2)
						.build(),
					Task.builder()
						.withId(TASK_ID2)
						.withSortOrder(1)
						.build()))
				.build(),
			Phase.builder()
				.withId(PHASE_ID2)
				.withSortOrder(1)
				.build());

		assertThat(SortingUtils.sortPhases(phases)).satisfiesExactly(
			phase -> {
				assertThat(phase.getId()).isEqualTo(PHASE_ID2);
				assertThat(phase.getSortOrder()).isEqualTo(1);
			},
			phase -> {
				assertThat(phase.getId()).isEqualTo(PHASE_ID1);
				assertThat(phase.getSortOrder()).isEqualTo(2);
				assertThat(phase.getTasks().getFirst().getId()).isEqualTo(TASK_ID2);
				assertThat(phase.getTasks().getFirst().getSortOrder()).isEqualTo(1);
				assertThat(phase.getTasks().getLast().getId()).isEqualTo(TASK_ID1);
				assertThat(phase.getTasks().getLast().getSortOrder()).isEqualTo(2);
			});
	}

	@Test
	void sortChecklistPhasesWhenSameOrder() {
		final var phases = List.of(
			Phase.builder()
				.withId(PHASE_ID1)
				.withSortOrder(1)
				.withName("Phase 1")
				.withTasks(List.of(
					Task.builder()
						.withId(TASK_ID1)
						.withSortOrder(1)
						.withHeading("Task 1")
						.build(),
					Task.builder()
						.withId(TASK_ID2)
						.withSortOrder(1)
						.withHeading("Task 2")
						.build()))
				.build(),
			Phase.builder()
				.withId(PHASE_ID2)
				.withSortOrder(1)
				.withName("Phase 2")
				.build());

		assertThat(SortingUtils.sortPhases(phases)).satisfiesExactly(
			phase -> {
				assertThat(phase.getId()).isEqualTo(PHASE_ID1);
				assertThat(phase.getSortOrder()).isEqualTo(1);
				assertThat(phase.getTasks().getFirst().getId()).isEqualTo(TASK_ID1);
				assertThat(phase.getTasks().getFirst().getSortOrder()).isEqualTo(1);
				assertThat(phase.getTasks().getLast().getId()).isEqualTo(TASK_ID2);
				assertThat(phase.getTasks().getLast().getSortOrder()).isEqualTo(1);
			},
			phase -> {
				assertThat(phase.getId()).isEqualTo(PHASE_ID2);
				assertThat(phase.getSortOrder()).isEqualTo(1);
			});
	}

	@Test
	void sortChecklistTasksWhenDifferentOrder() {
		final var tasks = List.of(
			Task.builder()
				.withId(TASK_ID1)
				.withSortOrder(2)
				.build(),
			Task.builder()
				.withId(TASK_ID2)
				.withSortOrder(1)
				.build());

		assertThat(SortingUtils.sortTasks(tasks)).satisfiesExactly(
			task -> {
				assertThat(task.getId()).isEqualTo(TASK_ID2);
				assertThat(task.getSortOrder()).isEqualTo(1);
			},
			task -> {
				assertThat(task.getId()).isEqualTo(TASK_ID1);
				assertThat(task.getSortOrder()).isEqualTo(2);
			});
	}

	@Test
	void sortChecklistTasksWhenSameOrder() {
		final var tasks = List.of(
			Task.builder()
				.withId(TASK_ID1)
				.withSortOrder(1)
				.withHeading("Task 1")
				.build(),
			Task.builder()
				.withId(TASK_ID2)
				.withSortOrder(1)
				.withHeading("Task 2")
				.build());

		assertThat(SortingUtils.sortTasks(tasks)).satisfiesExactly(
			task -> {
				assertThat(task.getId()).isEqualTo(TASK_ID1);
				assertThat(task.getSortOrder()).isEqualTo(1);
			},
			task -> {
				assertThat(task.getId()).isEqualTo(TASK_ID2);
				assertThat(task.getSortOrder()).isEqualTo(1);
			});

	}

	@Test
	void getChecklistItemIds() {
		final var checklistEntity = ChecklistEntity.builder()
			.withTasks(List.of(TaskEntity.builder()
				.withId(TASK_ID1)
				.withPhase(PhaseEntity.builder()
					.withId(PHASE_ID1)
					.build())
				.build(),
				TaskEntity.builder()
					.withId(TASK_ID2)
					.withPhase(PhaseEntity.builder()
						.withId(PHASE_ID2)
						.build())
					.build(),
				TaskEntity.builder()
					.withId(TASK_ID3)
					.withPhase(PhaseEntity.builder()
						.withId(PHASE_ID1)
						.build())
					.build()))
			.build();

		final var result = SortingUtils.getChecklistItemIds(checklistEntity);

		assertThat(result).containsExactlyInAnyOrder(PHASE_ID1, PHASE_ID2, TASK_ID1, TASK_ID2, TASK_ID3);

	}

	private static List<SortorderEntity> createCustomorder() {
		return List.of(
			SortorderEntity.builder()
				.withComponentId(PHASE_ID1)
				.withComponentType(PHASE)
				.withPosition(101)
				.build(),
			SortorderEntity.builder()
				.withComponentId(TASK_ID1)
				.withComponentType(TASK)
				.withPosition(202)
				.build());
	}

	private static List<SortorderEntity> createCustomorderForTasks() {
		return List.of(
			SortorderEntity.builder()
				.withComponentId(TASK_ID1)
				.withComponentType(TASK)
				.withPosition(20)
				.build(),
			SortorderEntity.builder()
				.withComponentId(PHASE_ID1)
				.withComponentType(PHASE)
				.withPosition(15)
				.build(),
			SortorderEntity.builder()
				.withComponentId(TASK_ID2)
				.withComponentType(TASK)
				.withPosition(10)
				.build());
	}
}
