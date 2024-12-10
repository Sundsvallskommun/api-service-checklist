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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

	@MockitoBean
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

		when(serviceMock.createOrganization(MUNICIPALITY_ID, body)).thenReturn(ID);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/%s/organizations/%s".formatted(MUNICIPALITY_ID, ID))
			.expectBody().isEmpty();

		verify(serviceMock).createOrganization(MUNICIPALITY_ID, body);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchOrganizationsWithoutFilters() {
		when(serviceMock.fetchAllOrganizations(MUNICIPALITY_ID, null, null)).thenReturn(List.of(TestObjectFactory.createOrganization(), TestObjectFactory.createOrganization()));
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(2);
		verify(serviceMock).fetchAllOrganizations(MUNICIPALITY_ID, null, null);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchOrganizationsWithFilters() {
		when(serviceMock.fetchAllOrganizations(MUNICIPALITY_ID, List.of(12345), null)).thenReturn(List.of(TestObjectFactory.createOrganization()));
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).queryParam("organizationFilter", 12345).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(serviceMock).fetchAllOrganizations(MUNICIPALITY_ID, List.of(12345), null);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchOrganizationsWithFiltersAndCustomSortorder() {
		when(serviceMock.fetchAllOrganizations(MUNICIPALITY_ID, List.of(12345), 54321)).thenReturn(List.of(TestObjectFactory.createOrganization()));
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH)
				.queryParam("organizationFilter", 12345)
				.queryParam("applySortFor", 54321)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(serviceMock).fetchAllOrganizations(MUNICIPALITY_ID, List.of(12345), 54321);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchOrganization() {
		final var mockedResponse = TestObjectFactory.createOrganization();
		when(serviceMock.fetchOrganization(MUNICIPALITY_ID, ID, null)).thenReturn(mockedResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(mockedResponse);
		verify(serviceMock).fetchOrganization(MUNICIPALITY_ID, ID, null);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchOrganizationWithCustomSortorder() {
		final var mockedResponse = TestObjectFactory.createOrganization();
		when(serviceMock.fetchOrganization(MUNICIPALITY_ID, ID, 123)).thenReturn(mockedResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{id}")
				.queryParam("applySortFor", 123)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "id", ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Organization.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(mockedResponse);
		verify(serviceMock).fetchOrganization(MUNICIPALITY_ID, ID, 123);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateOrganization() {
		final var body = OrganizationUpdateRequest.builder()
			.withCommunicationChannels(Set.of(CommunicationChannel.EMAIL))
			.withOrganizationName("updatedName")
			.build();
		when(serviceMock.updateOrganization(MUNICIPALITY_ID, ID, body)).thenReturn(TestObjectFactory.createOrganization());

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

		verify(serviceMock).updateOrganization(MUNICIPALITY_ID, ID, body);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteOrganization() {
		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).deleteOrganization(MUNICIPALITY_ID, ID);
		verifyNoMoreInteractions(serviceMock);
	}
}
