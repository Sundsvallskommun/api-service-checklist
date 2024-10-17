package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
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

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.service.PortingService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class PortingResourceFailureTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private PortingService mockPortingService;

	@ParameterizedTest
	@ValueSource(strings = {
		"[]",
		"{}",
		"{\"name\": \"value\", \"roleType\": \"EMPLOYEE\", \"displayName\":\"\"}",
		"{\"name\": \"value\", \"displayName\":\"value\"}",
		"{\"name\": \"\", \"roleType\": \"EMPLOYEE\", \"displayName\":\"value\"}" })
	void importChecklistAsNewVersionWithFaultyJsonStructures(String jsonStructure) {
		final var organizationNumber = 123;
		final var organizationName = "organizationName";

		final var response = webTestClient.post()
			.uri(builder -> builder.path("/import/add/{organizationNumber}/{organizationName}").build(Map.of("organizationNumber", organizationNumber, "organizationName", organizationName)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("400 BAD_REQUEST \"Validation failure\"");
		});

		verifyNoInteractions(mockPortingService);
	}

	@Test
	void importChecklistAsNewVersionMissingBody() {
		final var organizationNumber = 123;
		final var organizationName = "organizationName";

		final var response = webTestClient.post()
			.uri(builder -> builder.path("/import/add/{organizationNumber}/{organizationName}").build(Map.of("organizationNumber", organizationNumber, "organizationName", organizationName)))
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
				se.sundsvall.checklist.api.PortingResource.importChecklistAsNewVersion(java.lang.Integer,java.lang.String,java.lang.String)\
				""");

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
		final var organizationNumber = 123;
		final var organizationName = "organizationName";

		final var response = webTestClient.post()
			.uri(builder -> builder.path("/import/replace/{organizationNumber}/{organizationName}").build(Map.of("organizationNumber", organizationNumber, "organizationName", organizationName)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("400 BAD_REQUEST \"Validation failure\"");
		});

		verifyNoInteractions(mockPortingService);
	}

	@Test
	void importAndOverwriteExistingChecklistMissingBody() {
		final var organizationNumber = 123;
		final var organizationName = "organizationName";

		final var response = webTestClient.post()
			.uri(builder -> builder.path("/import/replace/{organizationNumber}/{organizationName}").build(Map.of("organizationNumber", organizationNumber, "organizationName", organizationName)))
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
				se.sundsvall.checklist.api.PortingResource.importAndOverwriteExistingChecklist(java.lang.Integer,java.lang.String,java.lang.String)\
				""");

		});

		verifyNoInteractions(mockPortingService);
	}
}
