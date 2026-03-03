package se.sundsvall.checklist.api;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.service.SortorderService;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.checklist.TestObjectFactory.generateSortorderRequest;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class CustomSortResourceFailureTest {

	@MockitoBean
	private SortorderService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	private static final String INVALID = "invalid";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORGANIZATION_NUMBER = "123";
	private static final String BASE_PATH = "/{municipalityId}/sortorder/{organizationNumber}";

	@Test
	void saveSortorderWithInvalidMunicipalityId() {

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", INVALID, "organizationNumber", ORGANIZATION_NUMBER)))
			.contentType(APPLICATION_JSON)
			.bodyValue(generateSortorderRequest())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::field, Violation::message)
				.containsExactly(tuple("saveSortorder.municipalityId", "not a valid municipality ID"));
		});

	}

	@Test
	void saveSortorderWithInvalidOrganizationNumber() {

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "organizationNumber", INVALID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(generateSortorderRequest())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getDetail()).isEqualTo("Failed to convert 'organizationNumber' with value: 'invalid'");
		});
	}

	@Test
	void saveSortorderWithMissingBody() {

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "organizationNumber", ORGANIZATION_NUMBER)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getDetail()).isEqualTo("Failed to read request");
		});
	}

	@Test
	void saveSortorderWithMissingMandatoryRequestValues() {

		// Arrange
		final var request = generateSortorderRequest();
		request.getPhaseOrder().forEach(phase -> {
			phase.setId(null);
			phase.setPosition(null);
			phase.getTaskOrder().forEach(task -> {
				task.setId(null);
				task.setPosition(null);
			});
		});

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "organizationNumber", ORGANIZATION_NUMBER)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::field, Violation::message)
				.containsExactlyInAnyOrder(
					tuple("phaseOrder[0].id", "not a valid UUID"),
					tuple("phaseOrder[0].position", "must not be null"),
					tuple("phaseOrder[0].taskOrder[0].id", "not a valid UUID"),
					tuple("phaseOrder[0].taskOrder[0].position", "must not be null"),
					tuple("phaseOrder[0].taskOrder[1].id", "not a valid UUID"),
					tuple("phaseOrder[0].taskOrder[1].position", "must not be null"),
					tuple("phaseOrder[1].id", "not a valid UUID"),
					tuple("phaseOrder[1].position", "must not be null"),
					tuple("phaseOrder[1].taskOrder[0].id", "not a valid UUID"),
					tuple("phaseOrder[1].taskOrder[0].position", "must not be null"),
					tuple("phaseOrder[1].taskOrder[1].id", "not a valid UUID"),
					tuple("phaseOrder[1].taskOrder[1].position", "must not be null"));
		});
	}

	@AfterEach
	void verifyNoInteraction() {
		verifyNoInteractions(serviceMock);
	}
}
