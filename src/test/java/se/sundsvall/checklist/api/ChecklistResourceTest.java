package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import se.sundsvall.checklist.TestObjectFactory;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.service.ChecklistService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ChecklistResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ID = UUID.randomUUID().toString();

	@MockBean
	private ChecklistService mockService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void fetchAllChecklists() {
		// Arrange
		when(mockService.getChecklists(MUNICIPALITY_ID)).thenReturn(List.of(TestObjectFactory.createChecklist(), TestObjectFactory.createChecklist()));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path("/{municipalityId}/checklists").build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Checklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().satisfies(r -> {
			assertThat(r).hasSize(2);
		});

		verify(mockService).getChecklists(MUNICIPALITY_ID);
	}

	@Test
	void fetchChecklistById() {
		// Arrange
		final var mockedResponse = TestObjectFactory.createChecklist();
		when(mockService.getChecklist(MUNICIPALITY_ID, ID)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path("/{municipalityId}/checklists/{checklistId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Checklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().isEqualTo(mockedResponse);

		verify(mockService).getChecklist(MUNICIPALITY_ID, ID);
	}

	@Test
	void createChecklist() {
		// Arrange
		final var body = createChecklistCreateRequest();
		final var mockedResponse = TestObjectFactory.createChecklist();
		mockedResponse.setId(ID);
		when(mockService.createChecklist(MUNICIPALITY_ID, body)).thenReturn(mockedResponse);

		// Act
		webTestClient.post()
			.uri(builder -> builder.path("/{municipalityId}/checklists").build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/%s/checklists/%s".formatted(MUNICIPALITY_ID, ID));

		// Assert and verify
		verify(mockService).createChecklist(MUNICIPALITY_ID, body);
	}

	@Test
	void createNewVersion() {
		final var checklist = TestObjectFactory.createChecklist();
		when(mockService.createNewVersion(MUNICIPALITY_ID, ID)).thenReturn(checklist);

		// Act
		webTestClient.post()
			.uri(builder -> builder.path("/{municipalityId}/checklists/{checklistId}/version").build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID)))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().exists(LOCATION)
			.expectHeader().valueEquals(LOCATION, "/%s/checklists/%s".formatted(MUNICIPALITY_ID, checklist.getId()));

		// Assert and verify
		verify(mockService).createNewVersion(MUNICIPALITY_ID, ID);
	}

	@Test
	void activateChecklist() {
		final var mockedResponse = TestObjectFactory.createChecklist();
		when(mockService.activateChecklist(MUNICIPALITY_ID, ID)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path("/{municipalityId}/checklists/{checklistId}/activate").build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Checklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().isEqualTo(mockedResponse);

		verify(mockService).activateChecklist(MUNICIPALITY_ID, ID);
	}

	@Test
	void updateChecklist() {
		// Arrange
		final var request = createChecklistUpdateRequest();
		final var mockedResponse = TestObjectFactory.createChecklist();
		mockedResponse.setId(ID);
		when(mockService.updateChecklist(MUNICIPALITY_ID, ID, request)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path("/{municipalityId}/checklists/{checklistId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Checklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().isEqualTo(mockedResponse);

		verify(mockService).updateChecklist(MUNICIPALITY_ID, ID, request);
	}

	@Test
	void deleteChecklist() {
		// Act
		webTestClient.delete()
			.uri(builder -> builder.path("/{municipalityId}/checklists/{checklistId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockService).deleteChecklist(MUNICIPALITY_ID, ID);
	}
}
