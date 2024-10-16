package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.checklist.TestObjectFactory.createChecklist;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistUpdateRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.service.ChecklistService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ChecklistResourceTest {

	@MockBean
	private ChecklistService mockService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void fetchAllChecklistsTest() {
		// Arrange
		when(mockService.getAllChecklists()).thenReturn(List.of(createChecklist(), createChecklist()));

		// Act
		final var response = webTestClient.get()
			.uri("/checklists")
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Checklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r).hasSize(2);
		});

		verify(mockService).getAllChecklists();
	}

	@Test
	void fetchChecklistByIdTest() {
		// Arrange
		var id = UUID.randomUUID().toString();
		var checklist = createChecklist();
		when(mockService.getChecklistById(id)).thenReturn(checklist);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path("/checklists/{checklistId}").build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Checklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().isEqualTo(checklist);

		verify(mockService).getChecklistById(id);
	}

	@Test
	void createChecklistTest() {
		// Arrange
		var body = createChecklistCreateRequest();
		var result = createChecklist();
		result.setId(UUID.randomUUID().toString());
		when(mockService.createChecklist(body)).thenReturn(result);

		// Act
		webTestClient.post()
			.uri("/checklists")
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().exists(LOCATION);

		// Assert and verify
		verify(mockService).createChecklist(body);
	}

	@Test
	void createNewVersionTest() {
		// Arrange
		var id = UUID.randomUUID().toString();
		when(mockService.createNewVersion(anyString())).thenReturn(createChecklist());

		// Act
		webTestClient.post()
			.uri(builder -> builder.path("/checklists/{checklistId}/version").build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().exists(LOCATION);

		// Assert and verify
		verify(mockService).createNewVersion(id);
	}

	@Test
	void activateChecklistTest() {
		// Arrange
		var id = UUID.randomUUID().toString();
		var response = createChecklist();
		when(mockService.activateChecklist(anyString())).thenReturn(response);

		// Act
		final var result = webTestClient.patch()
			.uri(builder -> builder.path("/checklists/{checklistId}/activate").build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Checklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(result).isNotNull().isEqualTo(response);

		verify(mockService).activateChecklist(anyString());
	}

	@Test
	void updateChecklistTest() {
		// Arrange
		var id = UUID.randomUUID().toString();
		var request = createChecklistUpdateRequest();
		var result = createChecklist();
		result.setId(id);
		when(mockService.updateChecklist(id, request)).thenReturn(result);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path("/checklists/{checklistId}").build(Map.of("checklistId", id)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Checklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().isEqualTo(result);

		verify(mockService).updateChecklist(id, request);
	}

	@Test
	void deleteChecklistTest() {
		var id = UUID.randomUUID().toString();

		// Act
		webTestClient.delete()
			.uri(builder -> builder.path("/checklists/{checklistId}").build(Map.of("checklistId", id)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockService).deleteChecklist(id);
	}

}
