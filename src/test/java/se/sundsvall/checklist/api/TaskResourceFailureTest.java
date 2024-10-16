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
	private static final String randomId = UUID.randomUUID().toString();

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private TaskService mockTaskService;

	private static Stream<Arguments> fetchChecklistAndPhaseAndTaskArgumentProvider() {
		return Stream.of(
			Arguments.of("invalidId", randomId, randomId, "checklistId", "not a valid UUID"),
			Arguments.of(randomId, "invalidId", randomId, "phaseId", "not a valid UUID"),
			Arguments.of(randomId, randomId, "invalidId", "taskId", "not a valid UUID"));
	}

	private static Stream<Arguments> fetchChecklistAndPhaseArgumentProvider() {
		return Stream.of(
			Arguments.of("invalidId", randomId, "checklistId", "not a valid UUID"),
			Arguments.of(randomId, "invalidId", "phaseId", "not a valid UUID"));
	}

	private static Stream<Arguments> createChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of(createTaskCreateRequest(), randomId, "invalidId", "createChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(createTaskCreateRequest(), "invalidId", randomId, "createChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(createTaskCreateRequest(r -> r.setHeading(" ")), randomId, randomId, "heading", "must not be blank"),
			Arguments.of(createTaskCreateRequest(r -> r.setPermission(null)), randomId, randomId, "permission", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setQuestionType(null)), randomId, randomId, "questionType", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setRoleType(null)), randomId, randomId, "roleType", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setSortOrder(null)), randomId, randomId, "sortOrder", "must not be null"));
	}

	private static Stream<Arguments> updateChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of(createTaskUpdateRequest(), "invalidId", randomId, randomId, "updateChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(createTaskUpdateRequest(), randomId, "invalidId", randomId, "updateChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(createTaskUpdateRequest(), randomId, randomId, "invalidId", "updateChecklistPhaseTask.taskId", "not a valid UUID"));
	}

	private static Stream<Arguments> deleteChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of("invalidId", randomId, randomId, "deleteChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(randomId, "invalidId", randomId, "deleteChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(randomId, randomId, "invalidId", "deleteChecklistPhaseTask.taskId", "not a valid UUID"));
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
