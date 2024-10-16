package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.Collections;
import java.util.Map;

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
import se.sundsvall.checklist.api.model.OrganizationCreateRequest;
import se.sundsvall.checklist.api.model.OrganizationUpdateRequest;
import se.sundsvall.checklist.service.OrganizationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class OrganizationResourceFailureTest {

	@MockBean
	private OrganizationService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createOrganizationWithoutBody() {

		var response = webTestClient.post()
			.uri("/organizations")
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("""
				Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> \
				se.sundsvall.checklist.api.OrganizationResource.createOrganization(se.sundsvall.checklist.api.model.OrganizationCreateRequest)\
				""");
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void createOrganizationEmptyData() {
		var body = OrganizationCreateRequest.builder()
			.withCommunicationChannels(Collections.emptySet())
			.withOrganizationName(" ")
			.build();

		var response = webTestClient.post()
			.uri("/organizations")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getViolations())
				.extracting(
					Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("communicationChannels", "must not be empty"),
					tuple("organizationName", "must not be blank"),
					tuple("organizationNumber", "must not be null"));
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void fetchOrganizationWithInvalidUuid() {
		final var id = "invalid";

		var response = webTestClient.get()
			.uri(builder -> builder.path("/organizations/{id}").build(Map.of("id", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("fetchOrganizationById.organizationId", "not a valid UUID"));
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void updateOrganizationWithInvalidUuid() {
		final var id = "invalid";
		final var body = OrganizationUpdateRequest.builder().build();

		var response = webTestClient.patch()
			.uri(builder -> builder.path("/organizations/{uuid}").build(Map.of("uuid", id)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("updateOrganization.organizationId", "not a valid UUID"));
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void updateOrganizationWithoutBody() {
		final var id = "invalid";

		var response = webTestClient.patch()
			.uri(builder -> builder.path("/organizations/{uuid}").build(Map.of("uuid", id)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Bad Request");
			assertThat(r.getDetail()).isEqualTo("""
				Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.checklist.api.model.Organization> \
				se.sundsvall.checklist.api.OrganizationResource.updateOrganization(java.lang.String,se.sundsvall.checklist.api.model.OrganizationUpdateRequest)\
				""");
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void deleteOrganizationWithInvalidUuid() {
		final var id = "invalid";

		var response = webTestClient.delete()
			.uri(builder -> builder.path("/organizations/{uuid}").build(Map.of("uuid", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("deleteOrganization.organizationId", "not a valid UUID"));
		});

		verifyNoInteractions(serviceMock);
	}
}
