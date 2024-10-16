package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.service.PortingService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class PortingResourceTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private PortingService mockPortingService;

	@Test
	void exportLatestChecklistVersion() {
		final var organizationNumber = 123;
		final var roleType = RoleType.EMPLOYEE;
		final var jsonStructure = "{\"key\": \"value\"}";

		when(mockPortingService.exportChecklist(organizationNumber, roleType, null)).thenReturn(jsonStructure);

		final var response = webTestClient.get()
			.uri(builder -> builder.path("/export/{organizationNumber}/{roleType}").build(Map.of("organizationNumber", organizationNumber, "roleType", roleType)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(jsonStructure);

		verify(mockPortingService).exportChecklist(organizationNumber, roleType, null);
		verifyNoMoreInteractions(mockPortingService);
	}

	@Test
	void exportExplicitChecklistVersion() {
		final var organizationNumber = 123;
		final var version = 321;
		final var roleType = RoleType.EMPLOYEE;
		final var jsonStructure = "{\"key\": \"value\"}";

		when(mockPortingService.exportChecklist(organizationNumber, roleType, version)).thenReturn(jsonStructure);

		final var response = webTestClient.get()
			.uri(builder -> builder.path("/export/{organizationNumber}/{roleType}")
				.queryParam("version", version)
				.build(Map.of("organizationNumber", organizationNumber, "roleType", roleType)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(jsonStructure);

		verify(mockPortingService).exportChecklist(organizationNumber, roleType, version);
		verifyNoMoreInteractions(mockPortingService);
	}

	@Test
	void importChecklistAsNewVersion() {
		final var id = UUID.randomUUID().toString();
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var jsonStructure = """
			{
				"name": "name",
				"roleType": "EMPLOYEE",
				"displayName": "displayName"
			}""";

		when(mockPortingService.importChecklist(eq(organizationNumber), eq(organizationName), any(), eq(false))).thenReturn(id);

		webTestClient.post()
			.uri(builder -> builder.path("/import/add/{organizationNumber}/{organizationName}").build(Map.of("organizationNumber", organizationNumber, "organizationName", organizationName)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/checklists/" + id)
			.expectBody().isEmpty();

		verify(mockPortingService).importChecklist(eq(organizationNumber), eq(organizationName), any(), eq(false));
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
				"roleType": "EMPLOYEE",
				"displayName": "displayName"
			}""";

		when(mockPortingService.importChecklist(eq(organizationNumber), eq(organizationName), any(), eq(true))).thenReturn(id);

		webTestClient.post()
			.uri(builder -> builder.path("/import/replace/{organizationNumber}/{organizationName}").build(Map.of("organizationNumber", organizationNumber, "organizationName", organizationName)))
			.contentType(APPLICATION_JSON)
			.bodyValue(jsonStructure)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/checklists/" + id)
			.expectBody().isEmpty();

		verify(mockPortingService).importChecklist(eq(organizationNumber), eq(organizationName), any(), eq(true));
		verifyNoMoreInteractions(mockPortingService);
	}

}
