package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.service.DelegationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DelegationResourceFailureTest {

	private static final String PATH_PREFIX = "/employee-checklists";

	@MockBean
	private DelegationService delegationServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void delegateToInvalidEmployeeChecklistUuid() {
		final var id = "invalid";

		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + "/{uuid}/delegate-to/random.email@noreply.com").build(Map.of("uuid", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("delegateEmployeeChecklist.employeeChecklistId", "not a valid UUID"));
		});

		verifyNoInteractions(delegationServiceMock);
	}

	@Test
	void delegateToInvalidEmail() {
		final var id = UUID.randomUUID().toString();

		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + "/{uuid}/delegate-to/invalid").build(Map.of("uuid", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("delegateEmployeeChecklist.email", "must be a well-formed email address"));
		});

		verifyNoInteractions(delegationServiceMock);
	}

	@Test
	void removeDelegationToInvalidEmployeeChecklistUuid() {
		final var id = "invalid";

		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH_PREFIX + "/{uuid}/delegated-to/random.email@noreply.com").build(Map.of("uuid", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("deleteEmployeeChecklistDelegation.employeeChecklistId", "not a valid UUID"));
		});

		verifyNoInteractions(delegationServiceMock);
	}

	@Test
	void removeDelegationToInvalidEmail() {
		final var id = UUID.randomUUID().toString();

		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH_PREFIX + "/{uuid}/delegated-to/invalid").build(Map.of("uuid", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("deleteEmployeeChecklistDelegation.email", "must be a well-formed email address"));
		});

		verifyNoInteractions(delegationServiceMock);
	}
}
