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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.Correspondence;
import se.sundsvall.checklist.service.CommunicationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class CommunicationResourceTest {

	private static final String PATH_PREFIX = "/employee-checklists";

	@MockBean
	private CommunicationService communicationServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void sendEmail() {
		// Arrange
		final var id = UUID.randomUUID().toString();

		// Act
		webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + "/{uuid}/email").build(Map.of("uuid", id)))
			.exchange()
			.expectStatus().isCreated()
			.expectBody().isEmpty();

		// Assert and verify
		verify(communicationServiceMock).sendEmail(id);
		verifyNoMoreInteractions(communicationServiceMock);
	}

	@Test
	void retreiveCorrespondence() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var correspondence = Correspondence.builder().build();

		when(communicationServiceMock.fetchCorrespondence(id)).thenReturn(correspondence);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH_PREFIX + "/{uuid}/correspondence").build(Map.of("uuid", id)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Correspondence.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(correspondence);
		verify(communicationServiceMock).fetchCorrespondence(id);
		verifyNoMoreInteractions(communicationServiceMock);
	}
}
