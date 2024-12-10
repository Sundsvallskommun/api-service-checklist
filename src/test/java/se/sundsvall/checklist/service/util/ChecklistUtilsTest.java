package se.sundsvall.checklist.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@ExtendWith(MockitoExtension.class)
class ChecklistUtilsTest {

	@Mock
	private ObjectMapper objectMapperMock;

	@InjectMocks
	private ChecklistUtils checklistUtils;

	@Test
	void testClearFields() {
		final var phase1 = PhaseEntity.builder().build();
		final var phase2 = PhaseEntity.builder().build();
		final var entity = ChecklistEntity.builder()
			.withId("id-0")
			.withCreated(OffsetDateTime.now())
			.withUpdated(OffsetDateTime.now())
			.withLifeCycle(LifeCycle.ACTIVE)
			.withVersion(123)
			.withTasks(List.of(
				TaskEntity.builder()
					.withId("id-1")
					.withCreated(OffsetDateTime.now())
					.withUpdated(OffsetDateTime.now())
					.withPhase(phase1)
					.build(),
				TaskEntity.builder()
					.withId("id-2")
					.withCreated(OffsetDateTime.now())
					.withUpdated(OffsetDateTime.now())
					.withPhase(phase2)
					.build()))
			.build();

		final var result = ChecklistUtils.clearFields(entity, 321);

		assertThat(result).hasAllNullFieldsOrPropertiesExcept("tasks", "version", "lifeCycle");
		assertThat(result.getVersion()).isEqualTo(321);
		assertThat(result.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
		assertThat(result.getTasks()).hasSize(2)
			.allSatisfy(task -> {
				assertThat(task).hasAllNullFieldsOrPropertiesExcept("phase", "sortOrder");
				assertThat(task.getSortOrder()).isZero();
			})
			.satisfiesExactlyInAnyOrder(
				task -> assertThat(task.getPhase()).isEqualTo(phase1),
				task -> assertThat(task.getPhase()).isEqualTo(phase2));

	}

	@Test
	void testFindMatchingTaskIds() {
		final var phase = PhaseEntity.builder()
			.withId("phase")
			.build();

		final var clone = ChecklistEntity.builder().withTasks(List.of(
			TaskEntity.builder()
				.withHeading("Task 1")
				.withId("cloned-task-1-id")
				.withPhase(phase)
				.build(),
			TaskEntity.builder()
				.withHeading("Task 2")
				.withId("cloned-task-2-id")
				.withPhase(phase)
				.build())).build();

		final var origin = ChecklistEntity.builder().withTasks(List.of(
			TaskEntity.builder()
				.withHeading("Task 1")
				.withId("origin-task-1-id")
				.withPhase(phase)
				.build(),
			TaskEntity.builder()
				.withHeading("Task 2")
				.withId("origin-task-2-id")
				.withPhase(phase)
				.build())).build();

		final var translationMap = ChecklistUtils.findMatchingTaskIds(clone, origin);

		assertThat(translationMap).containsExactlyInAnyOrderEntriesOf(Map.of(
			"cloned-task-1-id", "origin-task-1-id",
			"cloned-task-2-id", "origin-task-2-id"));
	}

	@Test
	void testFindMatchingTaskIdsWhenNoMatches() {
		final var phase = PhaseEntity.builder()
			.withId("phase")
			.build();

		final var clone = ChecklistEntity.builder().withTasks(List.of(
			TaskEntity.builder()
				.withHeading("Task 1")
				.withId("cloned-task-1-id")
				.withPhase(phase)
				.build(),
			TaskEntity.builder()
				.withHeading("Task 2")
				.withId("cloned-task-2-id")
				.withPhase(phase)
				.build())).build();

		final var origin = ChecklistEntity.builder().withTasks(List.of(
			TaskEntity.builder()
				.withHeading("Task 3")
				.withId("origin-task-3-id")
				.withPhase(phase)
				.build(),
			TaskEntity.builder()
				.withHeading("Task 4")
				.withId("origin-task-4-id")
				.withPhase(phase)
				.build())).build();

		assertThat(ChecklistUtils.findMatchingTaskIds(clone, origin)).isEmpty();
	}

	@Test
	void testFindMatchingTaskIdsWithTasksOnCloneNull() {
		final var phase = PhaseEntity.builder()
			.withId("phase")
			.build();

		final var clone = ChecklistEntity.builder().build();

		final var origin = ChecklistEntity.builder().withTasks(List.of(
			TaskEntity.builder()
				.withHeading("Task 1")
				.withId("origin-task-1-id")
				.withPhase(phase)
				.build(),
			TaskEntity.builder()
				.withHeading("Task 2")
				.withId("origin-task-2-id")
				.withPhase(phase)
				.build())).build();

		assertThat(ChecklistUtils.findMatchingTaskIds(clone, origin)).isEmpty();
	}

	@Test
	void testFindMatchingTaskIdsWithTasksOnOriginNull() {
		final var phase = PhaseEntity.builder()
			.withId("phase")
			.build();

		final var clone = ChecklistEntity.builder().withTasks(List.of(
			TaskEntity.builder()
				.withHeading("Task 1")
				.withId("origin-task-1-id")
				.withPhase(phase)
				.build(),
			TaskEntity.builder()
				.withHeading("Task 2")
				.withId("origin-task-2-id")
				.withPhase(phase)
				.build())).build();

		final var origin = ChecklistEntity.builder().build();

		assertThat(ChecklistUtils.findMatchingTaskIds(clone, origin)).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("compareArgumentsProvider")
	void testCompare(TaskEntity taskA, TaskEntity taskB, boolean equals) {
		assertThat(ChecklistUtils.compare(taskA, taskB)).isEqualTo(equals);
	}

	static Stream<Arguments> compareArgumentsProvider() {
		final var phase = PhaseEntity.builder().build();

		return Stream.of(
			Arguments.of(null, null, true),
			Arguments.of(TaskEntity.builder().build(), TaskEntity.builder().build(), true),
			Arguments.of(TaskEntity.builder().build(), TaskEntity.builder().withPhase(PhaseEntity.builder().build()).build(), false),
			Arguments.of(TaskEntity.builder().withPhase(PhaseEntity.builder().build()).build(), TaskEntity.builder().build(), false),
			Arguments.of(TaskEntity.builder().withPhase(PhaseEntity.builder().build()).build(), TaskEntity.builder().withPhase(PhaseEntity.builder().build()).build(), true),
			Arguments.of(TaskEntity.builder().withPhase(PhaseEntity.builder().withId("A").build()).build(), TaskEntity.builder().withPhase(PhaseEntity.builder().withId("A").build()).build(), true),
			Arguments.of(TaskEntity.builder().withPhase(PhaseEntity.builder().withId("A").build()).build(), TaskEntity.builder().withPhase(PhaseEntity.builder().withId("a").build()).build(), false),
			Arguments.of(TaskEntity.builder().withHeading("A").build(), TaskEntity.builder().withHeading("A").build(), true),
			Arguments.of(TaskEntity.builder().withHeading("A").build(), TaskEntity.builder().withHeading("a").build(), false),
			Arguments.of(TaskEntity.builder().withText("A").build(), TaskEntity.builder().withText("A").build(), true),
			Arguments.of(TaskEntity.builder().withText("A").build(), TaskEntity.builder().withText("a").build(), false),
			Arguments.of(TaskEntity.builder().withQuestionType(QuestionType.YES_OR_NO).build(), TaskEntity.builder().withQuestionType(QuestionType.YES_OR_NO).build(), true),
			Arguments.of(TaskEntity.builder().withQuestionType(QuestionType.YES_OR_NO).build(), TaskEntity.builder().withQuestionType(QuestionType.YES_OR_NO_WITH_TEXT).build(), false),
			Arguments.of(TaskEntity.builder().withRoleType(RoleType.NEW_EMPLOYEE).build(), TaskEntity.builder().withRoleType(RoleType.NEW_EMPLOYEE).build(), true),
			Arguments.of(TaskEntity.builder().withRoleType(RoleType.NEW_EMPLOYEE).build(), TaskEntity.builder().withRoleType(RoleType.NEW_MANAGER).build(), false),
			Arguments.of(TaskEntity.builder().withSortOrder(112).build(), TaskEntity.builder().withSortOrder(112).build(), true),
			Arguments.of(TaskEntity.builder().withSortOrder(112).build(), TaskEntity.builder().withSortOrder(221).build(), false),
			Arguments.of(TaskEntity.builder().withPhase(phase).build(), TaskEntity.builder().withPhase(phase).build(), true),
			Arguments.of(null, TaskEntity.builder().build(), false),
			Arguments.of(TaskEntity.builder().build(), null, false));
	}

	@Test
	void testClone() throws Exception {
		final var entity = createChecklistEntity();
		final var json = "{}";

		when(objectMapperMock.writeValueAsString(any())).thenReturn(json);
		when(objectMapperMock.readValue(json, ChecklistEntity.class)).thenReturn(entity);

		checklistUtils.clone(entity);

		verify(objectMapperMock).writeValueAsString(entity);
		verify(objectMapperMock).readValue(json, ChecklistEntity.class);
		verifyNoMoreInteractions(objectMapperMock);
	}

	@Test
	void testCloneThrowsException() throws Exception {
		final var entity = createChecklistEntity();

		when(objectMapperMock.writeValueAsString(any())).thenThrow(new JsonMappingException(() -> {
		}, "Some exception"));

		final var e = assertThrows(ThrowableProblem.class, () -> checklistUtils.clone(entity));

		assertThat(e.getMessage()).isEqualTo("Internal Server Error: Error creating clone of checklist entity");

		verify(objectMapperMock).writeValueAsString(entity);
		verifyNoMoreInteractions(objectMapperMock);
	}
}
