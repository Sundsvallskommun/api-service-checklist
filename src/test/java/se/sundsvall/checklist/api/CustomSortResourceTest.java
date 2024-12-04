package se.sundsvall.checklist.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.checklist.TestObjectFactory.generateSortorderRequest;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.service.SortorderService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class CustomSortResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORGANIZATION_NUMBER = "123";
	private static final String BASE_PATH = "/{municipalityId}/sortorder/{organizationNumber}";

	@MockBean
	private SortorderService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void saveSortorderWithFullRequest() {
		// Arrange
		final var request = generateSortorderRequest();
		// Act
		webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "organizationNumber", ORGANIZATION_NUMBER)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isAccepted()
			.expectBody().isEmpty();

		// Assert and verify
		verify(serviceMock).saveSortorder(MUNICIPALITY_ID, 123, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void saveSortorderWhenPhaseContainsNoTasks() {
		// Arrange
		final var request = generateSortorderRequest();
		request.getPhaseOrder().getFirst().setTaskOrder(null);

		// Act
		webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "organizationNumber", ORGANIZATION_NUMBER)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isAccepted()
			.expectBody().isEmpty();

		// Assert and verify
		verify(serviceMock).saveSortorder(MUNICIPALITY_ID, 123, request);
		verifyNoMoreInteractions(serviceMock);
	}
}
