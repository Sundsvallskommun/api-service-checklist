package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static se.sundsvall.checklist.TestObjectFactory.createPhase;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseUpdateRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.service.PhaseService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class PhaseResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ID = UUID.randomUUID().toString();
	private static final String BASE_PATH = "/{municipalityId}/phases";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private PhaseService mockPhaseService;

	@Test
	void fetchPhases() {
		final var mockedResponse = List.of(createPhase(), createPhase());
		when(mockPhaseService.getPhases(MUNICIPALITY_ID)).thenReturn(mockedResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Phase.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(mockedResponse);
		verify(mockPhaseService).getPhases(MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseService);
	}

	@Test
	void fetchPhase() {
		final var mockedResponse = createPhase();
		when(mockPhaseService.getPhase(MUNICIPALITY_ID, ID)).thenReturn(mockedResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "phaseId", ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Phase.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(mockedResponse);
		verify(mockPhaseService).getPhase(MUNICIPALITY_ID, ID);
		verifyNoMoreInteractions(mockPhaseService);
	}

	@Test
	void createNewPhase() {
		final var mockedResponse = createPhase(p -> p.setId(ID));
		final var request = createPhaseCreateRequest();
		when(mockPhaseService.createPhase(MUNICIPALITY_ID, request)).thenReturn(mockedResponse);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/%s/phases/%s".formatted(MUNICIPALITY_ID, ID))
			.expectBody().isEmpty();

		verify(mockPhaseService).createPhase(MUNICIPALITY_ID, request);
		verifyNoMoreInteractions(mockPhaseService);
	}

	@Test
	void updatePhase() {
		final var mockedResponse = createPhase();
		final var request = createPhaseUpdateRequest();
		when(mockPhaseService.updatePhase(MUNICIPALITY_ID, ID, request)).thenReturn(mockedResponse);

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "phaseId", ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Phase.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(mockedResponse);
		verify(mockPhaseService).updatePhase(MUNICIPALITY_ID, ID, request);
		verifyNoMoreInteractions(mockPhaseService);
	}

	@Test
	void deletePhase() {
		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "phaseId", ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(mockPhaseService).deletePhase(MUNICIPALITY_ID, ID);
		verifyNoMoreInteractions(mockPhaseService);
	}

}
