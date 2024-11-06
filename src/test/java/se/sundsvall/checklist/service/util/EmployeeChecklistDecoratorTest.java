package se.sundsvall.checklist.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;

class EmployeeChecklistDecoratorTest {

	@Test
	void decorateChecklistWithCustomTasks() {
		// Arrange
		final var phaseId = UUID.randomUUID().toString();
		final var commonTaskId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var checklist = EmployeeChecklist.builder()
			.withPhases(List.of(EmployeeChecklistPhase.builder()
				.withId(phaseId)
				.withTasks(new ArrayList<>(List.of(
					EmployeeChecklistTask.builder()
						.withId(commonTaskId)
						.withSortOrder(2)
						.build())))
				.build()))
			.build();

		final var customTasks = List.of(CustomTaskEntity.builder()
			.withId(customTaskId)
			.withSortOrder(1)
			.withPhase(PhaseEntity.builder().withId(phaseId).build())
			.build());

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithCustomTasks(checklist, customTasks);

		// Assert
		assertThat(result.getPhases().getFirst().getTasks()).hasSize(2).extracting(EmployeeChecklistTask::getSortOrder).containsExactly(1, 2);
	}

	@Test
	void decorateChecklistWithCustomTasksWhenNoMatchingPhase() {
		// Arrange
		final var phaseId = UUID.randomUUID().toString();
		final var commonTaskId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();
		final var checklist = EmployeeChecklist.builder()
			.withPhases(List.of(EmployeeChecklistPhase.builder()
				.withId(UUID.randomUUID().toString())
				.withTasks(new ArrayList<>(List.of(
					EmployeeChecklistTask.builder()
						.withId(commonTaskId)
						.withSortOrder(2)
						.build())))
				.build()))
			.build();

		final var customTasks = List.of(CustomTaskEntity.builder()
			.withId(customTaskId)
			.withSortOrder(1)
			.withPhase(PhaseEntity.builder().withId(phaseId).build())
			.build());

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithCustomTasks(checklist, customTasks);

		// Assert
		assertThat(result.getPhases().get(0).getTasks()).hasSize(1).extracting(EmployeeChecklistTask::getSortOrder).containsExactly(2);
	}

	@Test
	void decorateChecklistWithFulfilment() {
		// Arrange
		final var commonTaskId = UUID.randomUUID().toString();
		final var commonFulfilmentStatus = FulfilmentStatus.FALSE;
		final var commonResponseText = "commonResponseText";
		final var commonUpdated = OffsetDateTime.now();
		final var commonLastSavedBy = "commonLastSavedBy";
		final var customTaskId = UUID.randomUUID().toString();
		final var customFulfilmentStatus = FulfilmentStatus.TRUE;
		final var customResponseText = "customResponseText";
		final var customUpdated = OffsetDateTime.now().minusDays(1);
		final var customLastSavedBy = "commonLastSavedBy";
		final var entity = EmployeeChecklistEntity.builder()
			.withFulfilments(List.of(FulfilmentEntity.builder()
				.withTask(TaskEntity.builder()
					.withId(commonTaskId)
					.build())
				.withCompleted(commonFulfilmentStatus)
				.withResponseText(commonResponseText)
				.withUpdated(commonUpdated)
				.withLastSavedBy(commonLastSavedBy)
				.build()))
			.withCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
				.withCustomTask(CustomTaskEntity.builder()
					.withId(customTaskId)
					.build())
				.withCompleted(customFulfilmentStatus)
				.withResponseText(customResponseText)
				.withUpdated(customUpdated)
				.withLastSavedBy(customLastSavedBy)
				.build()))
			.build();

		final var checklist = EmployeeChecklist.builder()
			.withPhases(List.of(EmployeeChecklistPhase.builder()
				.withTasks(List.of(
					EmployeeChecklistTask.builder()
						.withId(commonTaskId)
						.build(),
					EmployeeChecklistTask.builder()
						.withId(customTaskId)
						.withCustomTask(true)
						.build()))
				.build()))
			.build();

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithFulfilment(checklist, Optional.of(entity));

		// Assert
		assertThat(result.getPhases().get(0).getTasks())
			.extracting(
				EmployeeChecklistTask::getId,
				EmployeeChecklistTask::getFulfilmentStatus,
				EmployeeChecklistTask::getResponseText,
				EmployeeChecklistTask::getUpdated,
				EmployeeChecklistTask::getUpdatedBy)
			.containsExactlyInAnyOrder(
				tuple(
					commonTaskId,
					commonFulfilmentStatus,
					commonResponseText,
					commonUpdated,
					commonLastSavedBy),
				tuple(
					customTaskId,
					customFulfilmentStatus,
					customResponseText,
					customUpdated,
					customLastSavedBy));
	}

	@Test
	void decorateChecklistWithFulfilmentWhenFulfilmentsNotPresent() {
		// Arrange
		final var commonTaskId = UUID.randomUUID().toString();
		final var customTaskId = UUID.randomUUID().toString();

		final var checklist = EmployeeChecklist.builder()
			.withPhases(List.of(EmployeeChecklistPhase.builder()
				.withTasks(List.of(
					EmployeeChecklistTask.builder()
						.withId(commonTaskId)
						.build(),
					EmployeeChecklistTask.builder()
						.withId(customTaskId)
						.withCustomTask(true)
						.build()))
				.build()))
			.build();

		final var entity = EmployeeChecklistEntity.builder().build();

		// Act
		EmployeeChecklistDecorator.decorateWithFulfilment(checklist, Optional.of(entity));

		// Assert that no fulfilment data has been added to tasks
		assertThat(checklist.getPhases().stream().map(EmployeeChecklistPhase::getTasks).flatMap(List::stream).toList()).allSatisfy(t -> {
			assertThat(t).hasAllNullFieldsOrPropertiesExcept("id", "customTask");
		});
	}

	@Test
	void decorateChecklistWithFulfilmentWhenEntityNotPresent() {
		// Arrange
		final var checklist = EmployeeChecklist.builder().build();

		// Act
		EmployeeChecklistDecorator.decorateWithFulfilment(checklist, Optional.empty());

		// Assert
		assertThat(checklist).isEqualTo(EmployeeChecklist.builder().build());
	}

	@Test
	void decorateChecklistPhaseWithCustomTasks() {
		// Arrange
		final var phaseId = UUID.randomUUID().toString();

		final var phase = EmployeeChecklistPhase.builder()
			.withTasks(new ArrayList<>(List.of(
				EmployeeChecklistTask.builder().withSortOrder(2).build(),
				EmployeeChecklistTask.builder().withSortOrder(4).build())))
			.withId(phaseId)
			.build();

		final var customTasks = List.of(
			CustomTaskEntity.builder()
				.withPhase(PhaseEntity.builder()
					.withId(UUID.randomUUID().toString()) // Simulating customTask belonging to other phase
					.build())
				.withSortOrder(1)
				.build(),
			CustomTaskEntity.builder()
				.withPhase(PhaseEntity.builder()
					.withId(phaseId)
					.build())
				.withSortOrder(1)
				.build(),
			CustomTaskEntity.builder()
				.withPhase(PhaseEntity.builder()
					.withId(phaseId)
					.build())
				.withSortOrder(3)
				.build(),
			CustomTaskEntity.builder()
				.withPhase(PhaseEntity.builder()
					.withId(phaseId)
					.build())
				.withSortOrder(5)
				.build());

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithCustomTasks(phase, customTasks);

		// Assert
		assertThat(result.getTasks()).hasSize(5).extracting(EmployeeChecklistTask::getSortOrder).containsExactly(1, 2, 3, 4, 5);
	}

	@Test
	void decorateChecklistPhaseWhenCustomTasksDoesNotExist() {
		// Arrange
		final var phase = EmployeeChecklistPhase.builder()
			.withTasks(new ArrayList<>(List.of(
				EmployeeChecklistTask.builder().withSortOrder(2).build(),
				EmployeeChecklistTask.builder().withSortOrder(4).build())))
			.build();

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithCustomTasks(phase, null);

		// Assert
		assertThat(result.getTasks()).hasSize(2).extracting(EmployeeChecklistTask::getSortOrder).containsExactly(2, 4);
	}

	@Test
	void decorateChecklistPhaseWithFulfilment() {
		// Arrange
		final var commonTaskId = UUID.randomUUID().toString();
		final var commonFulfilmentStatus = FulfilmentStatus.FALSE;
		final var commonResponseText = "commonResponseText";
		final var commonUpdated = OffsetDateTime.now();
		final var customTaskId = UUID.randomUUID().toString();
		final var customFulfilmentStatus = FulfilmentStatus.TRUE;
		final var customResponseText = "customResponseText";
		final var customUpdated = OffsetDateTime.now().minusDays(1);
		final var checklist = EmployeeChecklistEntity.builder()
			.withFulfilments(List.of(FulfilmentEntity.builder()
				.withTask(TaskEntity.builder()
					.withId(commonTaskId)
					.build())
				.withCompleted(commonFulfilmentStatus)
				.withResponseText(commonResponseText)
				.withUpdated(commonUpdated)
				.build()))
			.withCustomFulfilments(List.of(CustomFulfilmentEntity.builder()
				.withCustomTask(CustomTaskEntity.builder()
					.withId(customTaskId)
					.build())
				.withCompleted(customFulfilmentStatus)
				.withResponseText(customResponseText)
				.withUpdated(customUpdated)
				.build()))
			.build();

		final var phase = EmployeeChecklistPhase.builder()
			.withTasks(List.of(
				EmployeeChecklistTask.builder()
					.withId(commonTaskId)
					.build(),
				EmployeeChecklistTask.builder()
					.withCustomTask(true)
					.withId(customTaskId)
					.build()))
			.build();

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithFulfilment(phase, checklist);

		// Assert
		assertThat(result).hasAllNullFieldsOrPropertiesExcept("tasks", "sortOrder");
		assertThat(result.getTasks()).hasSize(2);

		assertThat(result.getTasks())
			.extracting(
				EmployeeChecklistTask::getId,
				EmployeeChecklistTask::getFulfilmentStatus,
				EmployeeChecklistTask::getResponseText,
				EmployeeChecklistTask::getUpdated)
			.containsExactlyInAnyOrder(
				tuple(
					commonTaskId,
					commonFulfilmentStatus,
					commonResponseText,
					commonUpdated),
				tuple(
					customTaskId,
					customFulfilmentStatus,
					customResponseText,
					customUpdated));
	}

	@Test
	void decorateChecklistPhaseWhenFulfilmentsDoesNotExist() {
		// Arrange
		final var checklist = EmployeeChecklistEntity.builder().build();

		final var phase = EmployeeChecklistPhase.builder()
			.withTasks(List.of(EmployeeChecklistTask.builder().build()))
			.build();

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithFulfilment(phase, checklist);

		// Assert
		assertThat(result).hasAllNullFieldsOrPropertiesExcept("tasks", "sortOrder");
		assertThat(result.getTasks())
			.hasSize(1)
			.allSatisfy(ot -> assertThat(ot).hasAllNullFieldsOrPropertiesExcept("customTask"));
	}

	@Test
	void decorateChecklistTaskWithFulfilment() {
		// Arrange
		final var fulfilmentStatus = FulfilmentStatus.FALSE;
		final var responseText = "responseText";
		final var updated = OffsetDateTime.now();
		final var task = EmployeeChecklistTask.builder().build();
		final var fulfilment = FulfilmentEntity.builder()
			.withCompleted(fulfilmentStatus)
			.withResponseText(responseText)
			.withUpdated(updated)
			.build();

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithFulfilment(task, fulfilment);

		// Assert
		assertThat(result).hasAllNullFieldsOrPropertiesExcept("fulfilmentStatus", "responseText", "updated", "customTask");
		assertThat(result.getFulfilmentStatus()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getUpdated()).isEqualTo(updated);
	}

	@Test
	void decorateChecklistTaskWithCustomFulfilment() {
		// Arrange
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";
		final var updated = OffsetDateTime.now();
		final var task = EmployeeChecklistTask.builder().build();
		final var fulfilment = CustomFulfilmentEntity.builder()
			.withCompleted(fulfilmentStatus)
			.withResponseText(responseText)
			.withUpdated(updated)
			.build();

		// Act
		final var result = EmployeeChecklistDecorator.decorateWithFulfilment(task, fulfilment);

		// Assert
		assertThat(result).hasAllNullFieldsOrPropertiesExcept("fulfilmentStatus", "responseText", "updated", "customTask");
		assertThat(result.getFulfilmentStatus()).isEqualTo(fulfilmentStatus);
		assertThat(result.getResponseText()).isEqualTo(responseText);
		assertThat(result.getUpdated()).isEqualTo(updated);
	}
}
