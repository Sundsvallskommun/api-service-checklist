package se.sundsvall.checklist.api;

import static java.util.UUID.randomUUID;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.service.PhaseService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class PhaseResourceTest {

	private static final String BASE_PATH = "/checklists/{checklistId}/phases";

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private PhaseService mockPhaseService;

	@Test
	void fetchChecklistPhasesTest() {
		final var checklistId = randomUUID().toString();
		final var phaseList = List.of(createPhase(), createPhase());
		when(mockPhaseService.getChecklistPhases(checklistId)).thenReturn(phaseList);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", checklistId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Phase.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(phaseList);
		verify(mockPhaseService).getChecklistPhases(checklistId);
		verifyNoMoreInteractions(mockPhaseService);
	}

	@Test
	void fetchChecklistPhaseTest() {
		final var checklistId = randomUUID().toString();
		final var phaseId = randomUUID().toString();
		final var phase = createPhase();
		when(mockPhaseService.getChecklistPhase(checklistId, phaseId)).thenReturn(phase);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Phase.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(phase);
		verify(mockPhaseService).getChecklistPhase(checklistId, phaseId);
		verifyNoMoreInteractions(mockPhaseService);
	}

	@Test
	void createChecklistPhaseTest() {
		final var checklistId = randomUUID().toString();
		final var phase = createPhase(p -> p.setId(UUID.randomUUID().toString()));
		final var request = createPhaseCreateRequest();
		when(mockPhaseService.createChecklistPhase(checklistId, request)).thenReturn(phase);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", checklistId)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/checklists/" + checklistId + "/phases/" + phase.getId())
			.expectBody().isEmpty();

		verify(mockPhaseService).createChecklistPhase(checklistId, request);
		verifyNoMoreInteractions(mockPhaseService);
	}

	@Test
	void updateChecklistPhaseTest() {
		final var checklistId = randomUUID().toString();
		final var phaseId = randomUUID().toString();
		final var phase = createPhase();
		final var request = createPhaseUpdateRequest();
		when(mockPhaseService.updateChecklistPhase(checklistId, phaseId, request)).thenReturn(phase);

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Phase.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(phase);
		verify(mockPhaseService).updateChecklistPhase(checklistId, phaseId, request);
		verifyNoMoreInteractions(mockPhaseService);
	}

	@Test
	void deleteChecklistPhaseTest() {
		final var checklistId = randomUUID().toString();
		final var phaseId = randomUUID().toString();

		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{phaseId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(mockPhaseService).deleteChecklistPhase(checklistId, phaseId);
		verifyNoMoreInteractions(mockPhaseService);
	}

}
