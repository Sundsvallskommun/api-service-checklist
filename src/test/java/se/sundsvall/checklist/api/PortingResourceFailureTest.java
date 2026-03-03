package se.sundsvall.checklist.api;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.service.PortingService;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class PortingResourceFailureTest {

	private static final String INVALID = "invalid";
	private static final String MUNICIPALITY_ID = "2281";
	private static final int ORGANIZATION_NUMBER = 123;
	private static final String ORGANIZATION_NAME = "organizationName";
	private static final String BASE_PATH = "/{municipalityId}/";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private PortingService mockPortingService;

	@Test
	void exportChecklistInvalidPathValue() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/export/{organizationNumber}").build(Map.of("municipalityId", INVALID, "organizationNumber", ORGANIZATION_NUMBER)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::field, Violation::message)
				.containsExactlyInAnyOrder(
					tuple("exportChecklist.municipalityId", "not a valid municipality ID"));
		});

		verifyNoInteractions(mockPortingService);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[]",
		"{}",
		"{\"name\": \"value\", \"roleType\": \"INVALID\", \"displayName\":\"value\"}",
		"{\"name\": \"\", \"displayName\":\"value\"}",
		"{\"name\": \"value\", \"displayName\":\"\"}",
	})
	void importChecklistAsNewVersionWithFaultyJsonStructures(String jsonStructure) {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/import/add/{orgNbr}/{orgName}").build(Map.of("municipalityId", MUNICIPALITY_ID, "orgNbr", ORGANIZATION_NUMBER, "orgName", ORGANIZATION_NAME)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::field, Violation::message)
				.containsExactlyInAnyOrder(
					tuple("importChecklistAsNewVersion.jsonStructure", "not a valid structure"));
		});

		verifyNoInteractions(mockPortingService);
	}

	@Test
	void importChecklistAsNewVersionMissingBody() {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/import/add/{orgNbr}/{orgName}").build(Map.of("municipalityId", MUNICIPALITY_ID, "orgNbr", ORGANIZATION_NUMBER, "orgName", ORGANIZATION_NAME)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("Failed to read request");
		});

		verifyNoInteractions(mockPortingService);
	}

	@Test
	void importChecklistAsNewVersionInvalidPathValue() {
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/import/add/{orgNbr}/{orgName}").build(Map.of("municipalityId", INVALID, "orgNbr", ORGANIZATION_NUMBER, "orgName", ORGANIZATION_NAME)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::field, Violation::message)
				.containsExactlyInAnyOrder(
					tuple("importChecklistAsNewVersion.municipalityId", "not a valid municipality ID"));
		});

		verifyNoInteractions(mockPortingService);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[]",
		"{}",
		"{\"name\": \"value\", \"displayName\":\"\"}",
		"{\"name\": \"\", \"displayName\":\"value\"}"
	})
	void importAndOverwriteExistingChecklistWithFaultyJsonStructures(String jsonStructure) {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/import/replace/{orgNbr}/{orgName}").build(Map.of("municipalityId", MUNICIPALITY_ID, "orgNbr", ORGANIZATION_NUMBER, "orgName", ORGANIZATION_NAME)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::field, Violation::message)
				.containsExactlyInAnyOrder(
					tuple("importAndOverwriteExistingChecklist.jsonStructure", "not a valid structure"));
		});

		verifyNoInteractions(mockPortingService);
	}

	@Test
	void importAndOverwriteExistingChecklistMissingBody() {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/import/replace/{orgNbr}/{orgName}").build(Map.of("municipalityId", MUNICIPALITY_ID, "orgNbr", ORGANIZATION_NUMBER, "orgName", ORGANIZATION_NAME)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("Failed to read request");
		});

		verifyNoInteractions(mockPortingService);
	}

	@Test
	void importAndOverwriteExistingChecklistInvalidPathValue() {
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/import/replace/{orgNbr}/{orgName}").build(Map.of("municipalityId", INVALID, "orgNbr", ORGANIZATION_NUMBER, "orgName", ORGANIZATION_NAME)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::field, Violation::message)
				.containsExactlyInAnyOrder(
					tuple("importAndOverwriteExistingChecklist.municipalityId", "not a valid municipality ID"));
		});

		verifyNoInteractions(mockPortingService);
	}
}
