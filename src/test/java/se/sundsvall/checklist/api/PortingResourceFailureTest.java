package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.service.PortingService;

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

	@MockBean
	private PortingService mockPortingService;

	@Test
	void exportChecklistInvalidPathValue() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/export/{organizationNumber}/{roleType}").build(Map.of("municipalityId", INVALID, "organizationNumber", ORGANIZATION_NUMBER, "roleType", RoleType.EMPLOYEE)))
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
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("exportChecklist.municipalityId", "not a valid municipality ID"));
		});

		verifyNoInteractions(mockPortingService);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[]",
		"{}",
		"{\"name\": \"value\", \"roleType\": \"EMPLOYEE\", \"displayName\":\"\"}",
		"{\"name\": \"value\", \"displayName\":\"value\"}",
		"{\"name\": \"\", \"roleType\": \"EMPLOYEE\", \"displayName\":\"value\"}" })
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
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
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
			assertThat(r.getDetail()).isEqualTo("""
				Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> \
				se.sundsvall.checklist.api.PortingResource.importChecklistAsNewVersion(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String)\
				""");
		});

		verifyNoInteractions(mockPortingService);
	}

	@Test
	void importChecklistAsNewVersionInvalidPathValue() {
		final var jsonStructure = """
			{
				"name": "name",
				"roleType": "EMPLOYEE",
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
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("importChecklistAsNewVersion.municipalityId", "not a valid municipality ID"));
		});

		verifyNoInteractions(mockPortingService);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[]",
		"{}",
		"{\"name\": \"value\", \"roleType\": \"EMPLOYEE\", \"displayName\":\"\"}",
		"{\"name\": \"value\", \"displayName\":\"value\"}",
		"{\"name\": \"\", \"roleType\": \"EMPLOYEE\", \"displayName\":\"value\"}" })
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
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
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
			assertThat(r.getDetail()).isEqualTo("""
				Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> \
				se.sundsvall.checklist.api.PortingResource.importAndOverwriteExistingChecklist(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String)\
				""");
		});

		verifyNoInteractions(mockPortingService);
	}

	@Test
	void importAndOverwriteExistingChecklistInvalidPathValue() {
		final var jsonStructure = """
			{
				"name": "name",
				"roleType": "EMPLOYEE",
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
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("importAndOverwriteExistingChecklist.municipalityId", "not a valid municipality ID"));
		});

		verifyNoInteractions(mockPortingService);
	}
}
