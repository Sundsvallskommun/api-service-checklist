package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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
import se.sundsvall.checklist.TestObjectFactory;
import se.sundsvall.checklist.api.model.Organization;
import se.sundsvall.checklist.api.model.OrganizationCreateRequest;
import se.sundsvall.checklist.api.model.OrganizationUpdateRequest;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.service.OrganizationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class OrganizationResourceTest {

	private static final String ID = UUID.randomUUID().toString();
	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_PATH = "/{municipalityId}/organizations";

	@MockBean
	private OrganizationService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createOrganization() {
		final var body = OrganizationCreateRequest.builder()
			.withCommunicationChannels(Set.of(CommunicationChannel.EMAIL))
			.withOrganizationName("organizationName")
			.withOrganizationNumber(1234)
			.build();

		when(serviceMock.createOrganization(body)).thenReturn(ID);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/%s/organizations/%s".formatted(MUNICIPALITY_ID, ID))
			.expectBody().isEmpty();

		verify(serviceMock).createOrganization(body);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchOrganizations() {
		when(serviceMock.fetchAllOrganizations()).thenReturn(List.of(TestObjectFactory.createOrganization(), TestObjectFactory.createOrganization()));
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
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
	void fetchOrganization() {
		final var mockedResponse = TestObjectFactory.createOrganization();
		when(serviceMock.fetchOrganizationById(ID)).thenReturn(mockedResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(mockedResponse);
		verify(serviceMock).fetchOrganizationById(ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateOrganization() {
		final var body = OrganizationUpdateRequest.builder()
			.withCommunicationChannels(Set.of(CommunicationChannel.EMAIL))
			.withOrganizationName("updatedName")
			.build();
		when(serviceMock.updateOrganization(ID, body)).thenReturn(TestObjectFactory.createOrganization());

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();

		verify(serviceMock).updateOrganization(ID, body);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteOrganization() {
		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).deleteOrganization(ID);
		verifyNoMoreInteractions(serviceMock);
	}
}
