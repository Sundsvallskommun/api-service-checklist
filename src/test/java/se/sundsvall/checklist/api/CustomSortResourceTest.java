package se.sundsvall.checklist.api;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.checklist.TestObjectFactory.generateSortorderRequest;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class CustomSortResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORGANIZATION_NUMBER = "123";
	private static final String BASE_PATH = "/{municipalityId}/sortorder/{organizationNumber}";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void saveSortorderWithFullRequest() {
		// Act
		webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "organizationNumber", ORGANIZATION_NUMBER)))
			.bodyValue(generateSortorderRequest())
			.exchange()
			.expectStatus().isAccepted()
			.expectBody().isEmpty();

		// Assert and verify
		// TODO when service layer is present: Add -> verify(sortOrderServiceMock)...
		// TODO: Remove comment when service layer is present: verifyNoMoreInteractions(sortOrderServiceMock);
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
		// TODO when service layer is present: Add -> verify(sortOrderServiceMock)...
		// TODO: Remove comment when service layer is present: verifyNoMoreInteractions(sortOrderServiceMock);
	}
}