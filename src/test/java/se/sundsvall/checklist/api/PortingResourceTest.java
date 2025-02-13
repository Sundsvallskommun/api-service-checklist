package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.service.PortingService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class PortingResourceTest {
	private static final String MUNICIPALITY_ID = "2281";
	private static final int ORGANIZATION_NUMBER = 123;
	private static final String ORGANIZATION_NAME = "organizationName";
	private static final String BASE_PATH = "/{municipalityId}/";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private PortingService mockPortingService;

	@Test
	void exportLatestChecklistVersion() {
		final var jsonStructure = "{\"key\": \"value\"}";

		when(mockPortingService.exportChecklist(MUNICIPALITY_ID, ORGANIZATION_NUMBER, null)).thenReturn(jsonStructure);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/export/{orgNbr}").build(Map.of("municipalityId", MUNICIPALITY_ID, "orgNbr", ORGANIZATION_NUMBER)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(jsonStructure);

		verify(mockPortingService).exportChecklist(MUNICIPALITY_ID, ORGANIZATION_NUMBER, null);
		verifyNoMoreInteractions(mockPortingService);
	}

	@Test
	void exportExplicitChecklistVersion() {
		final var version = 321;
		final var jsonStructure = "{\"key\": \"value\"}";

		when(mockPortingService.exportChecklist(MUNICIPALITY_ID, ORGANIZATION_NUMBER, version)).thenReturn(jsonStructure);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/export/{orgNbr}")
				.queryParam("version", version)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "orgNbr", ORGANIZATION_NUMBER)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(jsonStructure);

		verify(mockPortingService).exportChecklist(MUNICIPALITY_ID, ORGANIZATION_NUMBER, version);
		verifyNoMoreInteractions(mockPortingService);
	}

	@Test
	void importChecklistAsNewVersion() {
		final var id = UUID.randomUUID().toString();
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(mockPortingService.importChecklist(MUNICIPALITY_ID, ORGANIZATION_NUMBER, ORGANIZATION_NAME, jsonStructure, false)).thenReturn(id);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/import/add/{orgNbr}/{orgName}").build(Map.of("municipalityId", MUNICIPALITY_ID, "orgNbr", ORGANIZATION_NUMBER, "orgName", ORGANIZATION_NAME)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/%s/checklists/%s".formatted(MUNICIPALITY_ID, id))
			.expectBody().isEmpty();

		verify(mockPortingService).importChecklist(MUNICIPALITY_ID, ORGANIZATION_NUMBER, ORGANIZATION_NAME, jsonStructure, false);
		verifyNoMoreInteractions(mockPortingService);
	}

	@Test
	void importAndOverwriteExistingChecklist() {
		final var id = UUID.randomUUID().toString();
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(mockPortingService.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, true)).thenReturn(id);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/import/replace/{orgNbr}/{orgName}").build(Map.of("municipalityId", MUNICIPALITY_ID, "orgNbr", ORGANIZATION_NUMBER, "orgName", ORGANIZATION_NAME)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/%s/checklists/%s".formatted(MUNICIPALITY_ID, id))
			.expectBody().isEmpty();

		verify(mockPortingService).importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, true);
		verifyNoMoreInteractions(mockPortingService);
	}

}
