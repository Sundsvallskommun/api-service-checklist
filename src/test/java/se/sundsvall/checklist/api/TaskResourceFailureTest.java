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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

	private static final String BASE_PATH = "/{municipalityId}/checklists/{checklistId}/phases/{phaseId}/tasks";
	private static final String RANDOM_ID = UUID.randomUUID().toString();
	private static final String INVALID = "invalid";
	private static final String MUNICIPALITY_ID = "2281";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private TaskService mockTaskService;

	private static Stream<Arguments> fetchChecklistAndPhaseAndTaskArgumentProvider() {
		return Stream.of(
			Arguments.of(INVALID, RANDOM_ID, RANDOM_ID, RANDOM_ID, "municipalityId", "not a valid municipality ID"),
			Arguments.of(MUNICIPALITY_ID, INVALID, RANDOM_ID, RANDOM_ID, "checklistId", "not a valid UUID"),
			Arguments.of(MUNICIPALITY_ID, RANDOM_ID, INVALID, RANDOM_ID, "phaseId", "not a valid UUID"),
			Arguments.of(MUNICIPALITY_ID, RANDOM_ID, RANDOM_ID, INVALID, "taskId", "not a valid UUID"));
	}

	private static Stream<Arguments> fetchChecklistAndPhaseArgumentProvider() {
		return Stream.of(
			Arguments.of(INVALID, RANDOM_ID, RANDOM_ID, "municipalityId", "not a valid municipality ID"),
			Arguments.of(MUNICIPALITY_ID, INVALID, RANDOM_ID, "checklistId", "not a valid UUID"),
			Arguments.of(MUNICIPALITY_ID, RANDOM_ID, INVALID, "phaseId", "not a valid UUID"));
	}

	private static Stream<Arguments> createChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of(createTaskCreateRequest(), INVALID, RANDOM_ID, RANDOM_ID, "createChecklistPhaseTask.municipalityId", "not a valid municipality ID"),
			Arguments.of(createTaskCreateRequest(), MUNICIPALITY_ID, RANDOM_ID, INVALID, "createChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(createTaskCreateRequest(), MUNICIPALITY_ID, INVALID, RANDOM_ID, "createChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(createTaskCreateRequest(r -> r.setHeading(" ")), MUNICIPALITY_ID, RANDOM_ID, RANDOM_ID, "heading", "must not be blank"),
			Arguments.of(createTaskCreateRequest(r -> r.setPermission(null)), MUNICIPALITY_ID, RANDOM_ID, RANDOM_ID, "permission", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setQuestionType(null)), MUNICIPALITY_ID, RANDOM_ID, RANDOM_ID, "questionType", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setRoleType(null)), MUNICIPALITY_ID, RANDOM_ID, RANDOM_ID, "roleType", "must not be null"),
			Arguments.of(createTaskCreateRequest(r -> r.setSortOrder(null)), MUNICIPALITY_ID, RANDOM_ID, RANDOM_ID, "sortOrder", "must not be null"));
	}

	private static Stream<Arguments> updateChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of(createTaskUpdateRequest(), INVALID, RANDOM_ID, RANDOM_ID, RANDOM_ID, "updateChecklistPhaseTask.municipalityId", "not a valid municipality ID"),
			Arguments.of(createTaskUpdateRequest(), MUNICIPALITY_ID, INVALID, RANDOM_ID, RANDOM_ID, "updateChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(createTaskUpdateRequest(), MUNICIPALITY_ID, RANDOM_ID, INVALID, RANDOM_ID, "updateChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(createTaskUpdateRequest(), MUNICIPALITY_ID, RANDOM_ID, RANDOM_ID, INVALID, "updateChecklistPhaseTask.taskId", "not a valid UUID"));
	}

	private static Stream<Arguments> deleteChecklistPhaseTaskArgumentProvider() {
		return Stream.of(
			Arguments.of(INVALID, RANDOM_ID, RANDOM_ID, RANDOM_ID, "deleteChecklistPhaseTask.municipalityId", "not a valid municipality ID"),
			Arguments.of(MUNICIPALITY_ID, INVALID, RANDOM_ID, RANDOM_ID, "deleteChecklistPhaseTask.checklistId", "not a valid UUID"),
			Arguments.of(MUNICIPALITY_ID, RANDOM_ID, INVALID, RANDOM_ID, "deleteChecklistPhaseTask.phaseId", "not a valid UUID"),
			Arguments.of(MUNICIPALITY_ID, RANDOM_ID, RANDOM_ID, INVALID, "deleteChecklistPhaseTask.taskId", "not a valid UUID"));
	}

	@ParameterizedTest
	@MethodSource("fetchChecklistAndPhaseArgumentProvider")
	void fetchChecklistPhaseTasksInvalidArgument(final String municipalityId, final String checklistId, final String phaseId,
		final String badArgument, final String expectedMessage) {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", municipalityId, "checklistId", checklistId, "phaseId", phaseId)))
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
	void fetchChecklistPhaseTaskInvalidArgument(final String municipalityId, final String checklistId, final String phaseId,
		final String taskId, final String badArgument, final String expectedMessage) {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("municipalityId", municipalityId, "checklistId", checklistId, "phaseId", phaseId, "taskId", taskId)))
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
	void createChecklistPhaseTaskInvalidArgument(final TaskCreateRequest request, final String municipalityId, final String checklistId,
		final String phaseId, final String badArgument, final String expectedMessage) {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", municipalityId, "checklistId", checklistId, "phaseId", phaseId)))
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
	void updateChecklistPhaseTaskInvalidArgument(final TaskUpdateRequest request, final String municipalityId, final String checklistId,
		final String phaseId, final String taskId, final String badArgument, final String expectedMessage) {
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("municipalityId", municipalityId, "checklistId", checklistId, "phaseId", phaseId, "taskId", taskId)))
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
	void deleteChecklistPhaseTaskInvalidArgument(final String municipalityId, final String checklistId, final String phaseId,
		final String taskId, final String badArgument, final String expectedMessage) {
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("municipalityId", municipalityId, "checklistId", checklistId, "phaseId", phaseId, "taskId", taskId)))
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
