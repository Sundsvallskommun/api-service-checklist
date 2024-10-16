package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.service.EmployeeChecklistService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class EmployeeChecklistResourceFailureTest {

	private static final String PATH_PREFIX = "/employee-checklists";

	@MockBean
	private EmployeeChecklistService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void checkNoInteractions() {
		verifyNoInteractions(serviceMock);
	}

	@Test
	void deleteEmployeeChecklistInvalidUuid() {
		// Arrange
		final var id = "invalid";
		final var path = "/{uuid}";

		// Act
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("uuid", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("deleteEmployeeChecklist.employeeChecklistId", "not a valid UUID"));
		});
	}

	@Test
	void createCustomTaskInvalidUuids() {
		// Arrange
		final var id = "invalid";
		final var path = "/{employeeChecklistId}/phases/{phaseId}/customtasks";
		final var body = CustomTaskCreateRequest.builder()
			.withHeading("heading")
			.withQuestionType(QuestionType.YES_OR_NO_WITH_TEXT)
			.withText("text")
			.withSortOrder(1)
			.build();

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "phaseId", id)))
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("createCustomTask.employeeChecklistId", "not a valid UUID"),
					tuple("createCustomTask.phaseId", "not a valid UUID"));
		});
	}

	@Test
	void createCustomTaskNullRequest() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var path = "/{employeeChecklistId}/phases/{phaseId}/customtasks";

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "phaseId", id)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("""
				Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.checklist.api.model.CustomTask> \
				se.sundsvall.checklist.api.EmployeeChecklistResource.createCustomTask(java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.CustomTaskCreateRequest)\
				""");
		});
	}

	@Test
	void createCustomTaskEmptyRequest() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var path = "/{employeeChecklistId}/phases/{phaseId}/customtasks";
		final var body = CustomTaskCreateRequest.builder().build();

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "phaseId", id)))
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("heading", "must not be blank"),
					tuple("questionType", "must not be null"),
					tuple("sortOrder", "must not be null"));
		});
	}

	@Test
	void readCustomTaskInvalidUuids() {
		// Arrange
		final var id = "invalid";
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "taskId", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("readCustomTask.employeeChecklistId", "not a valid UUID"),
					tuple("readCustomTask.taskId", "not a valid UUID"));
		});
	}

	@Test
	void updateCustomTaskInvalidUuids() {
		// Arrange
		final var id = "invalid";
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";
		final var body = CustomTaskUpdateRequest.builder().build();

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "taskId", id)))
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("updateCustomTask.employeeChecklistId", "not a valid UUID"),
					tuple("updateCustomTask.taskId", "not a valid UUID"));
		});
	}

	@Test
	void updateCustomTaskNullRequest() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "taskId", id)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("""
				Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.checklist.api.model.CustomTask> \
				se.sundsvall.checklist.api.EmployeeChecklistResource.updateCustomTask(java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.CustomTaskUpdateRequest)\
				""");
		});
	}

	@Test
	void deleteCustomTaskInvalidUuids() {
		// Arrange
		final var id = "invalid";
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		// Act
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "taskId", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("deleteCustomTask.employeeChecklistId", "not a valid UUID"),
					tuple("deleteCustomTask.taskId", "not a valid UUID"));
		});
	}

	@Test
	void updateAllTasksFulfilmentInPhaseInvalidUuids() {
		// Arrange
		final var id = "invalid";
		final var path = "/{employeeChecklistId}/phases/{phaseId}";
		final var body = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(FulfilmentStatus.EMPTY)
			.build();

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "phaseId", id)))
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("updateAllTasksInPhase.employeeChecklistId", "not a valid UUID"),
					tuple("updateAllTasksInPhase.phaseId", "not a valid UUID"));
		});
	}

	@Test
	void updateAllTasksFulfilmentInPhaseNullBody() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var path = "/{employeeChecklistId}/phases/{phaseId}";

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "phaseId", id)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("""
				Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.checklist.api.model.EmployeeChecklistPhase> \
				se.sundsvall.checklist.api.EmployeeChecklistResource.updateAllTasksInPhase(java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest)\
				""");
		});
	}

	@Test
	void updateTaskFulfilmentInvalidUuids() {
		// Arrange
		final var id = "invalid";
		final var path = "/{employeeChecklistId}/tasks/{taskId}";
		final var body = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(FulfilmentStatus.TRUE)
			.build();

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "taskId", id)))
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("updateTaskFulfilment.employeeChecklistId", "not a valid UUID"),
					tuple("updateTaskFulfilment.taskId", "not a valid UUID"));
		});
	}

	@Test
	void updateTaskFulfilmentNullBody() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var path = "/{employeeChecklistId}/tasks/{taskId}";

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", id, "taskId", id)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("""
				Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.checklist.api.model.EmployeeChecklistTask> \
				se.sundsvall.checklist.api.EmployeeChecklistResource.updateTaskFulfilment(java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest)\
				""");
		});
	}

	@Test
	void initializeEmployeeChecklistInvalidUuids() {
		// Arrange
		final var id = "invalid";
		final var path = "/initialize/{personId}";

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("personId", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("initiateSpecificEmployeeChecklist.personId", "not a valid UUID"));
		});
	}
}
