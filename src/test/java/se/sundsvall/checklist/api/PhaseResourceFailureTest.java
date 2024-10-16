package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseUpdateRequest;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
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
import se.sundsvall.checklist.api.model.PhaseCreateRequest;
import se.sundsvall.checklist.api.model.PhaseUpdateRequest;
import se.sundsvall.checklist.service.PhaseService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class PhaseResourceFailureTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private PhaseService mockPhaseService;

	private static final String BASE_PATH = "/checklists/{checklistId}/phases";

	private static Stream<Arguments> invalidIdsProvider() {
		return Stream.of(
			Arguments.of("invalidId", UUID.randomUUID().toString(), "checklistId", "not a valid UUID"),
			Arguments.of(UUID.randomUUID().toString(), "invalidId", "phaseId", "not a valid UUID"));
	}

	private static Stream<Arguments> invalidCreateRequestProvider() {
		return Stream.of(
			Arguments.of(createPhaseCreateRequest(r -> r.setTimeToComplete("Not valid")), "timeToComplete", "text is not in ISO-8601 period format"),
			Arguments.of(createPhaseCreateRequest(r -> r.setName(null)), "name", "must not be blank"),
			Arguments.of(createPhaseCreateRequest(r -> r.setRoleType(null)), "roleType", "must not be null"),
			Arguments.of(createPhaseCreateRequest(r -> r.setSortOrder(null)), "sortOrder", "must not be null"),
			Arguments.of(createPhaseCreateRequest(r -> r.setPermission(null)), "permission", "must not be null"));
	}

	@Test
	void fetchChecklistPhasesWithInvalidIdTest() {
		final var id = "invalidId";

		var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("fetchChecklistPhases.checklistId", "not a valid UUID"));
		});
		verifyNoInteractions(mockPhaseService);
	}

	@Test
	void createChecklistPhaseWithInvalidChecklistIdTest() {
		final var checklistId = "invalidId";
		final var body = createPhaseCreateRequest();

		var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", checklistId)))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("createChecklistPhase.checklistId", "not a valid UUID"));
		});
		verifyNoInteractions(mockPhaseService);
	}

	@Test
	void updateChecklistPhaseWithInvalidRequestTest() {
		var request = PhaseUpdateRequest.builder()
			.withName(null)
			.withBodyText(null)
			.withTimeToComplete("THIS IS INVALID")
			.withRoleType(null)
			.withSortOrder(null)
			.build();

		var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("checklistId", UUID.randomUUID().toString(), "phaseId", UUID.randomUUID().toString())))
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
				.containsExactlyInAnyOrder(tuple("timeToComplete", "text is not in ISO-8601 period format"));
		});
		verifyNoInteractions(mockPhaseService);
	}

	@ParameterizedTest
	@MethodSource("invalidIdsProvider")
	void fetchChecklistPhaseWithInvalidIdsTest(final String checklistId, final String phaseId, final String badArgument, final String expectedMessage) {
		var faultyArgument = "fetchChecklistPhase." + badArgument;
		var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple(faultyArgument, expectedMessage));
		});
		verifyNoInteractions(mockPhaseService);
	}

	@ParameterizedTest
	@MethodSource("invalidIdsProvider")
	void deleteChecklistPhaseWithInvalidIdsTest(final String checklistId, final String phaseId, final String badArgument, final String expectedMessage) {
		var faultyArgument = "deleteChecklistPhase." + badArgument;

		var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple(faultyArgument, expectedMessage));
		});
		verifyNoInteractions(mockPhaseService);
	}

	@ParameterizedTest
	@MethodSource("invalidIdsProvider")
	void updateChecklistPhaseWithInvalidIdsTest(final String checklistId, final String phaseId, final String badArgument, final String expectedMessage) {
		var faultyArgument = "updateChecklistPhase." + badArgument;
		var request = createPhaseUpdateRequest();
		var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
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
				.containsExactlyInAnyOrder(tuple(faultyArgument, expectedMessage));
		});
		verifyNoInteractions(mockPhaseService);
	}

	@ParameterizedTest
	@MethodSource("invalidCreateRequestProvider")
	void createChecklistPhaseWithInvalidRequestTest(final PhaseCreateRequest request, final String badArgument, final String expectedMessage) {
		final var checklistId = UUID.randomUUID().toString();

		var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", checklistId)))
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
		verifyNoInteractions(mockPhaseService);
	}
}
