package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.checklist.TestObjectFactory.createOrganization;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.Organization;
import se.sundsvall.checklist.api.model.OrganizationCreateRequest;
import se.sundsvall.checklist.api.model.OrganizationUpdateRequest;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.service.OrganizationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class OrganizationResourceTest {

	@MockBean
	private OrganizationService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createOrganizationTest() {
		var body = OrganizationCreateRequest.builder()
			.withCommunicationChannels(Set.of(CommunicationChannel.EMAIL))
			.withOrganizationName("organizationName")
			.withOrganizationNumber(1234)
			.build();

		when(serviceMock.createOrganization(body)).thenReturn(UUID.randomUUID().toString());

		webTestClient.post()
			.uri("/organizations")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().exists(LOCATION)
			.expectBody().isEmpty();

		verify(serviceMock).createOrganization(body);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchOrganizationsTest() {
		when(serviceMock.fetchAllOrganizations()).thenReturn(List.of(createOrganization(), createOrganization()));
		var response = webTestClient.get()
			.uri("/organizations")
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(2);
		verify(serviceMock).fetchAllOrganizations();
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchOrganizationTest() {
		final var id = UUID.randomUUID().toString();
		final var organization = createOrganization();
		when(serviceMock.fetchOrganizationById(id)).thenReturn(organization);

		var response = webTestClient.get()
			.uri(builder -> builder.path("/organizations/{id}").build(Map.of("id", id)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(organization);
		verify(serviceMock).fetchOrganizationById(id);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateOrganizationTest() {
		final var id = UUID.randomUUID().toString();
		final var body = OrganizationUpdateRequest.builder()
			.withCommunicationChannels(Set.of(CommunicationChannel.EMAIL))
			.withOrganizationName("updatedName")
			.build();
		when(serviceMock.updateOrganization(id, body)).thenReturn(createOrganization());

		var response = webTestClient.patch()
			.uri(builder -> builder.path("/organizations/{id}").build(Map.of("id", id)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();

		verify(serviceMock).updateOrganization(id, body);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteOrganizationTest() {
		final var id = UUID.randomUUID().toString();

		webTestClient.delete()
			.uri(builder -> builder.path("/organizations/{id}").build(Map.of("id", id)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).deleteOrganization(id);
		verifyNoMoreInteractions(serviceMock);
	}
}
