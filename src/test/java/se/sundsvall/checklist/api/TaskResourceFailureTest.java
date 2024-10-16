package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.checklist.TestObjectFactory.createTaskCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createTaskUpdateRequest;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.TaskCreateRequest;
import se.sundsvall.checklist.api.model.TaskUpdateRequest;
import se.sundsvall.checklist.service.TaskService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class TaskResourceFailureTest {

	private static final String BASE_PATH = "/checklists/{checklistId}/phases/{phaseId}/tasks";
	private static final String RANDOM_ID = UUID.randomUUID().toString();

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private TaskService mockTaskService;

	private static Stream<Arguments> fetchChecklistAndPhaseAndTaskArgumentProvider() {
		return Stream.of(
			Arguments.of("invalidId", RANDOM_ID, RANDOM_ID, "checklistId", "not a valid UUID"),
			Arguments.of(RANDOM_ID, "invalidId", RANDOM_ID, "phaseId", "not a valid UUID"),
			Arguments.of(RANDOM_ID, RANDOM_ID, "invalidId", "taskId", "not a valid UUID"));
	}

	private static Stream<Arguments> fetchChecklistAndPhaseArgumentProvider() {
		return Stream.of(
			Arguments.of("invalidId", RANDOM_ID, "checklistId", "not a valid UUID"),
			Arguments.of(RANDOM_ID, "invalidId", "phaseId", "not a valid UUID"));
	}

	private static Stream<Arguments> createChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of(createTaskCreateRequest(), RANDOM_ID, "invalidId", "createChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(createTaskCreateRequest(), "invalidId", RANDOM_ID, "createChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(createTaskCreateRequest(r -> r.setHeading(" ")), RANDOM_ID, RANDOM_ID, "heading", "must not be blank"),
			Arguments.of(createTaskCreateRequest(r -> r.setPermission(null)), RANDOM_ID, RANDOM_ID, "permission", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setQuestionType(null)), RANDOM_ID, RANDOM_ID, "questionType", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setRoleType(null)), RANDOM_ID, RANDOM_ID, "roleType", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setSortOrder(null)), RANDOM_ID, RANDOM_ID, "sortOrder", "must not be null"));
	}

	private static Stream<Arguments> updateChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of(createTaskUpdateRequest(), "invalidId", RANDOM_ID, RANDOM_ID, "updateChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(createTaskUpdateRequest(), RANDOM_ID, "invalidId", RANDOM_ID, "updateChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(createTaskUpdateRequest(), RANDOM_ID, RANDOM_ID, "invalidId", "updateChecklistPhaseTask.taskId", "not a valid UUID"));
	}

	private static Stream<Arguments> deleteChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of("invalidId", RANDOM_ID, RANDOM_ID, "deleteChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(RANDOM_ID, "invalidId", RANDOM_ID, "deleteChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(RANDOM_ID, RANDOM_ID, "invalidId", "deleteChecklistPhaseTask.taskId", "not a valid UUID"));
	}

	@ParameterizedTest
	@MethodSource("fetchChecklistAndPhaseArgumentProvider")
	void fetchChecklistPhaseTasksInvalidArgumentTest(final String checklistId, final String phaseId,
		final String badArgument, final String expectedMessage) {
		var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("fetchChecklistPhaseTasks." + badArgument, expectedMessage));
		});
		verifyNoInteractions(mockTaskService);
	}

	@ParameterizedTest
	@MethodSource("fetchChecklistAndPhaseAndTaskArgumentProvider")
	void fetchChecklistPhaseTaskInvalidArgumentTest(final String checklistId, final String phaseId,
		final String taskId, final String badArgument, final String expectedMessage) {
		var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId, "taskId", taskId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("fetchChecklistPhaseTask." + badArgument, expectedMessage));
		});
		verifyNoInteractions(mockTaskService);
	}

	@ParameterizedTest
	@MethodSource("createChecklistPhaseTaskArgumentProvider")
	void createChecklistPhaseTaskInvalidArgumentTest(final TaskCreateRequest request, final String checklistId,
		final String phaseId, final String badArgument, final String expectedMessage) {
		var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple(badArgument, expectedMessage));
		});
		verifyNoInteractions(mockTaskService);
	}

	@ParameterizedTest
	@MethodSource("updateChecklistPhaseTaskArgumentProvider")
	void updateChecklistPhaseTaskInvalidArgumentTest(final TaskUpdateRequest request, final String checklistId,
		final String phaseId, final String taskId, final String badArgument, final String expectedMessage) {
		var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId, "taskId", taskId)))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple(badArgument, expectedMessage));
		});
		verifyNoInteractions(mockTaskService);
	}

	@ParameterizedTest
	@MethodSource("deleteChecklistPhaseTaskArgumentProvider")
	void deleteChecklistPhaseTaskInvalidArgumentTest(final String checklistId, final String phaseId,
		final String taskId, final String badArgument, final String expectedMessage) {
		var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId, "taskId", taskId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple(badArgument, expectedMessage));
		});
		verifyNoInteractions(mockTaskService);
	}

}
