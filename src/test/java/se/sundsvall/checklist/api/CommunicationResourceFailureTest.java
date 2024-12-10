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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.service.CommunicationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class CommunicationResourceFailureTest {

	private static final String INVALID = "invalid";
	private static final String BASE_PATH = "/{municipalityId}/employee-checklists";

	@MockitoBean
	private CommunicationService communicationServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void sendEmailWithInvalidPathValues() {

		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/{uuid}/email").build(Map.of("municipalityId", INVALID, "uuid", INVALID)))
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
					tuple("sendEmail.municipalityId", "not a valid municipality ID"),
					tuple("sendEmail.employeeChecklistId", "not a valid UUID"));
		});

		verifyNoInteractions(communicationServiceMock);
	}

	@Test
	void retreiveCorrespondenceWithInvalidPathValues() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{uuid}/correspondence").build(Map.of("municipalityId", INVALID, "uuid", INVALID)))
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
					tuple("fetchCorrespondence.municipalityId", "not a valid municipality ID"),
					tuple("fetchCorrespondence.employeeChecklistId", "not a valid UUID"));
		});

		verifyNoInteractions(communicationServiceMock);
	}
}
