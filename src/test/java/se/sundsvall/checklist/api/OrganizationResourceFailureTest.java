package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.TestObjectFactory;
import se.sundsvall.checklist.api.model.OrganizationCreateRequest;
import se.sundsvall.checklist.api.model.OrganizationUpdateRequest;
import se.sundsvall.checklist.service.OrganizationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class OrganizationResourceFailureTest {

	private static final String INVALID = "invalid";
	private static final String ID = UUID.randomUUID().toString();
	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_PATH = "/{municipalityId}/organizations";
	@MockitoBean
	private OrganizationService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createOrganizationInvalidPathValues() {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", INVALID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(TestObjectFactory.createOrganizationCreateRequest())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("createOrganization.municipalityId", "not a valid municipality ID"));
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void createOrganizationWithoutBody() {

		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
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
				se.sundsvall.checklist.api.OrganizationResource.createOrganization(java.lang.String,se.sundsvall.checklist.api.model.OrganizationCreateRequest)\
				""");
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void createOrganizationEmptyData() {
		final var body = OrganizationCreateRequest.builder()
			.withCommunicationChannels(Collections.emptySet())
			.withOrganizationName(" ")
			.build();

		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
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
	void fetchOrganizationWithInvalidPathValues() {

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", INVALID, "id", INVALID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("fetchOrganizationById.municipalityId", "not a valid municipality ID"),
					tuple("fetchOrganizationById.organizationId", "not a valid UUID"));
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void updateOrganizationWithInvalidPathValues() {
		final var body = OrganizationUpdateRequest.builder().build();

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", INVALID, "id", INVALID)))
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
				.containsExactlyInAnyOrder(
					tuple("updateOrganization.municipalityId", "not a valid municipality ID"),
					tuple("updateOrganization.organizationId", "not a valid UUID"));
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void updateOrganizationWithoutBody() {
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", ID)))
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
				se.sundsvall.checklist.api.OrganizationResource.updateOrganization(java.lang.String,java.lang.String,se.sundsvall.checklist.api.model.OrganizationUpdateRequest)\
				""");
		});

		verifyNoInteractions(serviceMock);
	}

	@Test
	void deleteOrganizationWithInvalidPathValues() {
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", INVALID, "id", INVALID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("deleteOrganization.municipalityId", "not a valid municipality ID"),
					tuple("deleteOrganization.organizationId", "not a valid UUID"));
		});

		verifyNoInteractions(serviceMock);
	}
}
