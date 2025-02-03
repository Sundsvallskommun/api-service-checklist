package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.service.EmployeeChecklistService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class EmployeeChecklistResourceFailureTest {

	private static final String INVALID = "invalid";
	private static final String ID = UUID.randomUUID().toString();
	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_PATH = "/{municipalityId}/employee-checklists";
	private static final String USER_ID = "userId";

	@MockitoBean
	private EmployeeChecklistService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void checkNoInteractions() {
		verifyNoInteractions(serviceMock);
	}

	@Test
	void fetchAllOngoingEmployeeChecklistsInvalidPathValues() {
		// Arrange
		final var path = "/ongoing";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path)
				.build(Map.of("municipalityId", INVALID)))
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
				.containsExactlyInAnyOrder(
					tuple("fetchAllOngoingEmployeeChecklists.municipalityId", "not a valid municipality ID"));
		});
	}

	@Test
	void fetchAlOngoingEmployeeChecklistsInvalidPagingParameters_1() {
		// Arrange
		final var path = "/ongoing";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path)
				.queryParam("page", "-10")
				.queryParam("limit", "-10")
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
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
				.containsExactlyInAnyOrder(
					tuple("page", "must be greater than or equal to 1"),
					tuple("limit", "must be greater than or equal to 1"));
		});
	}

	@Test
	void deleteEmployeeChecklistInvalidPathValues() {
		// Arrange
		final var path = "/{uuid}";

		// Act
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "uuid", INVALID)))
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
				.containsExactlyInAnyOrder(
					tuple("deleteEmployeeChecklist.municipalityId", "not a valid municipality ID"),
					tuple("deleteEmployeeChecklist.employeeChecklistId", "not a valid UUID"));
		});
	}

	@Test
	void setMentorInvalidPathValues() {
		final var path = "/{employeeChecklistId}/mentor";
		final var body = Mentor.builder()
			.withUserId("someUserId")
			.withName("someName")
			.build();

		final var response = webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "employeeChecklistId", INVALID)))
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
					tuple("setMentor.municipalityId", "not a valid municipality ID"),
					tuple("setMentor.employeeChecklistId", "not a valid UUID"));
		});
	}

	@Test
	void setMentorNullRequest() {
		final var path = "/{employeeChecklistId}/mentor";

		final var response = webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID)))
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
				Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> \
				se.sundsvall.checklist.api.EmployeeChecklistResource.setMentor(java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.Mentor)\
				""");
		});
	}

	@Test
	void setMentorEmptyRequest() {
		final var path = "/{employeeChecklistId}/mentor";
		final var body = Mentor.builder().build();

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID)))
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
					tuple("userId", "must not be blank"),
					tuple("name", "must not be blank"));
		});
	}

	@Test
	void deleteMentorInvalidPathValues() {
		final var path = "/{employeeChecklistId}/mentor";

		final var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "employeeChecklistId", INVALID)))
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
					tuple("deleteMentor.municipalityId", "not a valid municipality ID"),
					tuple("deleteMentor.employeeChecklistId", "not a valid UUID"));
		});
	}

	@Test
	void createCustomTaskInvalidPathValues() {
		// Arrange
		final var path = "/{employeeChecklistId}/phases/{phaseId}/customtasks";
		final var body = CustomTaskCreateRequest.builder()
			.withHeading("heading")
			.withQuestionType(QuestionType.YES_OR_NO_WITH_TEXT)
			.withText("text")
			.withSortOrder(1)
			.withCreatedBy("someUser")
			.build();

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "employeeChecklistId", INVALID, "phaseId", INVALID)))
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
					tuple("createCustomTask.municipalityId", "not a valid municipality ID"),
					tuple("createCustomTask.employeeChecklistId", "not a valid UUID"),
					tuple("createCustomTask.phaseId", "not a valid UUID"));
		});
	}

	@Test
	void createCustomTaskNullRequest() {
		// Arrange
		final var path = "/{employeeChecklistId}/phases/{phaseId}/customtasks";

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "phaseId", ID)))
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
				se.sundsvall.checklist.api.EmployeeChecklistResource.createCustomTask(java.lang.String,java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.CustomTaskCreateRequest)\
				""");
		});
	}

	@Test
	void createCustomTaskEmptyRequest() {
		// Arrange
		final var path = "/{employeeChecklistId}/phases/{phaseId}/customtasks";
		final var body = CustomTaskCreateRequest.builder().build();

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "phaseId", ID)))
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
					tuple("sortOrder", "must not be null"),
					tuple("createdBy", "must not be blank"));
		});
	}

	@Test
	void readCustomTaskInvalidPathValues() {
		// Arrange
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "employeeChecklistId", INVALID, "taskId", INVALID)))
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
					tuple("readCustomTask.municipalityId", "not a valid municipality ID"),
					tuple("readCustomTask.employeeChecklistId", "not a valid UUID"),
					tuple("readCustomTask.taskId", "not a valid UUID"));
		});
	}

	@Test
	void updateCustomTaskInvalidPathValues() {
		// Arrange
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";
		final var body = CustomTaskUpdateRequest.builder().withUpdatedBy("someUser").build();

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "employeeChecklistId", INVALID, "taskId", INVALID)))
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
					tuple("updateCustomTask.municipalityId", "not a valid municipality ID"),
					tuple("updateCustomTask.employeeChecklistId", "not a valid UUID"),
					tuple("updateCustomTask.taskId", "not a valid UUID"));
		});
	}

	@Test
	void deleteCustomTaskInvalidPathValues() {
		// Arrange
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		// Act
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "employeeChecklistId", INVALID, "taskId", INVALID)))
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
					tuple("deleteCustomTask.municipalityId", "not a valid municipality ID"),
					tuple("deleteCustomTask.employeeChecklistId", "not a valid UUID"),
					tuple("deleteCustomTask.taskId", "not a valid UUID"));
		});
	}

	@Test
	void updateAllTasksFulfilmentInPhaseInvalidPathValues() {
		// Arrange
		final var path = "/{employeeChecklistId}/phases/{phaseId}";
		final var body = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(FulfilmentStatus.EMPTY)
			.withUpdatedBy(USER_ID)
			.build();

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "employeeChecklistId", INVALID, "phaseId", INVALID)))
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
					tuple("updateAllTasksInPhase.municipalityId", "not a valid municipality ID"),
					tuple("updateAllTasksInPhase.employeeChecklistId", "not a valid UUID"),
					tuple("updateAllTasksInPhase.phaseId", "not a valid UUID"));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " "
	})
	@NullSource
	void updateAllTasksFulfilmentInPhaseNoPerformedBy(String performedBy) {
		// Arrange
		final var path = "/{employeeChecklistId}/phases/{phaseId}";
		final var body = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(FulfilmentStatus.EMPTY)
			.withUpdatedBy(performedBy)
			.build();

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "phaseId", ID)))
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
					tuple("updatedBy", "must not be blank"));
		});
	}

	@ParameterizedTest
	@MethodSource("updateWithNullParamProvider")
	void updateWithNullRequest(String path, Map<String, String> pathParameters, String expectedDetail) {
		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(pathParameters))
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
			assertThat(r.getDetail()).isEqualTo(expectedDetail);
		});
	}

	private static Stream<Arguments> updateWithNullParamProvider() {

		return Stream.of(
			Arguments.of("/{employeeChecklistId}/customtasks/{taskId}", Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "taskId", ID), """
				Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.checklist.api.model.CustomTask> \
				se.sundsvall.checklist.api.EmployeeChecklistResource.updateCustomTask(java.lang.String,java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.CustomTaskUpdateRequest)\
				"""),
			Arguments.of("/{employeeChecklistId}/phases/{phaseId}", Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "phaseId", ID), """
				Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.checklist.api.model.EmployeeChecklistPhase> \
				se.sundsvall.checklist.api.EmployeeChecklistResource.updateAllTasksInPhase(java.lang.String,java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest)\
				"""),
			Arguments.of("/{employeeChecklistId}/tasks/{taskId}", Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "taskId", ID), """
				Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.checklist.api.model.EmployeeChecklistTask> \
				se.sundsvall.checklist.api.EmployeeChecklistResource.updateTaskFulfilment(java.lang.String,java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest)\
				"""));
	}

	@Test
	void updateTaskFulfilmentInvalidPathValues() {
		// Arrange
		final var path = "/{employeeChecklistId}/tasks/{taskId}";
		final var body = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(FulfilmentStatus.TRUE)
			.withUpdatedBy(USER_ID)
			.build();

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "employeeChecklistId", INVALID, "taskId", INVALID)))
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
					tuple("updateTaskFulfilment.municipalityId", "not a valid municipality ID"),
					tuple("updateTaskFulfilment.employeeChecklistId", "not a valid UUID"),
					tuple("updateTaskFulfilment.taskId", "not a valid UUID"));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " "
	})
	@NullSource
	void updateTaskFulfilmentNoPerformedBy(String performedBy) {
		// Arrange
		final var path = "/{employeeChecklistId}/tasks/{taskId}";
		final var body = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(FulfilmentStatus.TRUE)
			.withUpdatedBy(performedBy)
			.build();

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "taskId", ID)))
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
					tuple("updatedBy", "must not be blank"));
		});
	}

	@Test
	void initializeEmployeeChecklistInvalidPathValues() {
		// Arrange
		final var path = "/initialize/{personId}";

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID, "personId", INVALID)))
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
					tuple("initiateSpecificEmployeeChecklist.municipalityId", "not a valid municipality ID"),
					tuple("initiateSpecificEmployeeChecklist.personId", "not a valid UUID"));
		});
	}

	@Test
	void getInitiationinfoInvalidPathValues() {
		// Arrange
		final var path = "/initiationinfo";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", INVALID)))
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
				.containsExactly(
					tuple("getInitiationInformation.municipalityId", "not a valid municipality ID"));
		});
	}
}
