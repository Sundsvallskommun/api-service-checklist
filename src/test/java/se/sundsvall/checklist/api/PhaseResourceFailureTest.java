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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
import se.sundsvall.checklist.api.model.PhaseCreateRequest;
import se.sundsvall.checklist.api.model.PhaseUpdateRequest;
import se.sundsvall.checklist.service.PhaseService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class PhaseResourceFailureTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private PhaseService mockPhaseService;

	private static final String INVALID = "invalid";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String ID = UUID.randomUUID().toString();
	private static final String SUB_ID = UUID.randomUUID().toString();
	private static final String BASE_PATH = "/{municipalityId}/phases";

	private static Stream<Arguments> invalidIdsProvider() {
		return Stream.of(
			Arguments.of(INVALID, ID, "municipalityId", "not a valid municipality ID"),
			Arguments.of(MUNICIPALITY_ID, INVALID, "phaseId", "not a valid UUID"));
	}

	private static Stream<Arguments> invalidCreateRequestProvider() {
		return Stream.of(
			Arguments.of(createPhaseCreateRequest(r -> r.setTimeToComplete("Not valid")), "timeToComplete", "text is not in ISO-8601 period format"),
			Arguments.of(createPhaseCreateRequest(r -> r.setName(null)), "name", "must not be blank"),
			Arguments.of(createPhaseCreateRequest(r -> r.setSortOrder(null)), "sortOrder", "must not be null"),
			Arguments.of(createPhaseCreateRequest(r -> r.setPermission(null)), "permission", "must not be null"),
			Arguments.of(createPhaseCreateRequest(r -> r.setCreatedBy("")), "createdBy", "must not be blank"));
	}

	@Test
	void fetchChecklistPhasesWithInvalidPathValues() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", INVALID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactly(
					tuple("fetchChecklistPhases.municipalityId", "not a valid municipality ID"));
		});
	}

	@Test
	void createChecklistPhaseWithInvalidPathValues() {
		final var body = createPhaseCreateRequest();

		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", INVALID)))
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
				.containsExactly(
					tuple("createChecklistPhase.municipalityId", "not a valid municipality ID"));
		});
	}

	@Test
	void updatePhaseWithInvalidRequest() {
		final var request = PhaseUpdateRequest.builder()
			.withName(null)
			.withBodyText(null)
			.withTimeToComplete("THIS IS INVALID")
			.withSortOrder(null)
			.withUpdatedBy("someUser")
			.build();

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "phaseId", SUB_ID)))
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
	}

	@ParameterizedTest
	@MethodSource("invalidIdsProvider")
	void fetchPhaseWithInvalidIds(final String municipalityId, final String phaseId, final String badArgument, final String expectedMessage) {
		final var faultyArgument = "fetchChecklistPhase." + badArgument;
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("municipalityId", municipalityId, "phaseId", phaseId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactly(tuple(faultyArgument, expectedMessage));
		});
	}

	@ParameterizedTest
	@MethodSource("invalidIdsProvider")
	void deleteChecklistPhaseWithInvalidIds(final String municipalityId, final String phaseId, final String badArgument, final String expectedMessage) {
		final var faultyArgument = "deleteChecklistPhase." + badArgument;

		final var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("municipalityId", municipalityId, "phaseId", phaseId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactly(tuple(faultyArgument, expectedMessage));
		});
	}

	@ParameterizedTest
	@MethodSource("invalidIdsProvider")
	void updateChecklistPhaseWithInvalidIds(final String municipalityId, final String phaseId, final String badArgument, final String expectedMessage) {
		final var faultyArgument = "updateChecklistPhase." + badArgument;
		final var request = createPhaseUpdateRequest();
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("municipalityId", municipalityId, "phaseId", phaseId)))
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
				.containsExactly(tuple(faultyArgument, expectedMessage));
		});
	}

	@ParameterizedTest
	@MethodSource("invalidCreateRequestProvider")
	void createPhaseWithInvalidRequest(final PhaseCreateRequest request, final String badArgument, final String expectedMessage) {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
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
				.containsExactly(tuple(badArgument, expectedMessage));
		});
	}

	@AfterEach
	void verifyNoInteraction() {
		verifyNoInteractions(mockPhaseService);
	}
}
