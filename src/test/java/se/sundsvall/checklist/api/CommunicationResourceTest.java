package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.Correspondence;
import se.sundsvall.checklist.service.CommunicationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class CommunicationResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ID = UUID.randomUUID().toString();
	private static final String BASE_PATH = "/{municipalityId}/employee-checklists";

	@MockitoBean
	private CommunicationService communicationServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void sendEmail() {
		// Act
		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/{uuid}/email").build(Map.of("municipalityId", MUNICIPALITY_ID, "uuid", ID)))
			.exchange()
			.expectStatus().isCreated()
			.expectBody().isEmpty();

		// Assert and verify
		verify(communicationServiceMock).sendEmail(MUNICIPALITY_ID, ID);
		verifyNoMoreInteractions(communicationServiceMock);
	}

	@Test
	void retreiveCorrespondence() {
		// Arrange
		final var mockedResponse = Correspondence.builder().build();

		when(communicationServiceMock.fetchCorrespondence(MUNICIPALITY_ID, ID)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{uuid}/correspondence").build(Map.of("municipalityId", MUNICIPALITY_ID, "uuid", ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Correspondence.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);
		verify(communicationServiceMock).fetchCorrespondence(MUNICIPALITY_ID, ID);
		verifyNoMoreInteractions(communicationServiceMock);
	}
}
