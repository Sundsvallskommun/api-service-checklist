package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistUpdateRequest;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.EMPLOYEE;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.ChecklistCreateRequest;
import se.sundsvall.checklist.service.ChecklistService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ChecklistResourceFailureTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private ChecklistService mockChecklistService;

	@Test
	void fetchChecklistByIdWithInvalidIdTest() {
		final var id = "invalid";

		var response = webTestClient.get()
			.uri(builder -> builder.path("/checklists/{checklistId}").build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("fetchChecklistById.checklistId", "not a valid UUID"));
		});

		verifyNoInteractions(mockChecklistService);
	}

	@Test
	void createChecklistWithNullRoleTypeTest() {
		var body = ChecklistCreateRequest.builder()
			.withName("Name")
			.withOrganizationNumber(11)
			.withRoleType(null)
			.withDisplayName("displayName")
			.build();

		var response = webTestClient.post()
			.uri(builder -> builder.path("/checklists").build())
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
				.containsExactlyInAnyOrder(tuple("roleType", "must not be null"));
		});
		verifyNoInteractions(mockChecklistService);
	}

	@Test
	void createChecklistWithBlankNameTest() {
		var body = ChecklistCreateRequest.builder()
			.withName("")
			.withRoleType(EMPLOYEE)
			.withOrganizationNumber(11)
			.withDisplayName("displayName")
			.build();

		var response = webTestClient.post()
			.uri(builder -> builder.path("/checklists").build())
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
				.containsExactlyInAnyOrder(tuple("name", "must not be blank"));
		});
		verifyNoInteractions(mockChecklistService);
	}

	@Test
	void createNewVersionWithInvalidIdTest() {
		final var id = "invalid";

		var response = webTestClient.post()
			.uri(builder -> builder.path("/checklists/{checklistId}/version").build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("createNewVersion.checklistId", "not a valid UUID"));
		});

		verifyNoInteractions(mockChecklistService);
	}

	@Test
	void activateChecklistWithInvalidIdTest() {
		final var id = "invalid";

		var response = webTestClient.patch()
			.uri(builder -> builder.path("/checklists/{checklistId}/activate").build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("activateChecklist.checklistId", "not a valid UUID"));
		});

		verifyNoInteractions(mockChecklistService);
	}

	@Test
	void updateChecklistWithInvalidIdTest() {
		final var id = "invalid";

		var response = webTestClient.patch()
			.uri(builder -> builder.path("/checklists/{checklistId}").build(Map.of("checklistId", id)))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(createChecklistUpdateRequest())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("updateChecklist.checklistId", "not a valid UUID"));
		});

		verifyNoInteractions(mockChecklistService);
	}

	@Test
	void deleteChecklistWithInvalidIdTest() {
		final var id = "invalid";

		var response = webTestClient.delete()
			.uri(builder -> builder.path("/checklists/{checklistId}").build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r.getTitle()).isEqualTo("Constraint Violation");
			assertThat(r.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(r.getViolations()).extracting(Violation::getField, Violation::getMessage)
				.containsExactlyInAnyOrder(tuple("deleteChecklist.checklistId", "not a valid UUID"));
		});

		verifyNoInteractions(mockChecklistService);
	}

}
