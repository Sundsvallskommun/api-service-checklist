package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.Map;

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

	private static final String INVALID = "invalid";
	private static final String BASE_PATH = "/{municipalityId}/employee-checklists";

	@MockBean
	private DelegationService delegationServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void delegateToEmployeeChecklistWithInvalidPathValues() {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/{uuid}/delegate-to/{email}").build(Map.of("municipalityId", INVALID, "uuid", INVALID, "email", INVALID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("delegateEmployeeChecklist.municipalityId", "not a valid municipality ID"),
					tuple("delegateEmployeeChecklist.employeeChecklistId", "not a valid UUID"),
					tuple("delegateEmployeeChecklist.email", "must be a well-formed email address"));
		});

		verifyNoInteractions(delegationServiceMock);
	}

	@Test
	void removeDelegationToEmployeeChecklistWithInvalidPathValues() {
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{uuid}/delegated-to/{email}").build(Map.of("municipalityId", INVALID, "uuid", INVALID, "email", INVALID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(
					tuple("deleteEmployeeChecklistDelegation.municipalityId", "not a valid municipality ID"),
					tuple("deleteEmployeeChecklistDelegation.employeeChecklistId", "not a valid UUID"),
					tuple("deleteEmployeeChecklistDelegation.email", "must be a well-formed email address"));
		});

		verifyNoInteractions(delegationServiceMock);
	}
}
