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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.DelegatedEmployeeChecklistResponse;
import se.sundsvall.checklist.service.DelegationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DelegationResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ID = UUID.randomUUID().toString();
	private static final String EMAIL = "test@test.com";
	private static final String BASE_PATH = "{municipalityId}/employee-checklists";

	@MockitoBean
	private DelegationService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void delegateTo() {
		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + "/{uuid}/delegate-to/{email}").build(Map.of("municipalityId", MUNICIPALITY_ID, "uuid", ID, "email", EMAIL)))
			.exchange()
			.expectStatus().isCreated()
			.expectBody().isEmpty();

		verify(serviceMock).delegateEmployeeChecklist(MUNICIPALITY_ID, ID, EMAIL);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchDelegations() {
		final var username = "abc20def";
		final var mockResponse = DelegatedEmployeeChecklistResponse.builder().build();
		when(serviceMock.fetchDelegatedEmployeeChecklistsByUsername(MUNICIPALITY_ID, username)).thenReturn(mockResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/delegated-to/{username}").build(Map.of("municipalityId", MUNICIPALITY_ID, "username", username)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(DelegatedEmployeeChecklistResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(mockResponse);
		verify(serviceMock).fetchDelegatedEmployeeChecklistsByUsername(MUNICIPALITY_ID, username);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void removeDelegation() {
		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{uuid}/delegated-to/{email}").build(Map.of("municipalityId", MUNICIPALITY_ID, "uuid", ID, "email", EMAIL)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).removeEmployeeChecklistDelegation(MUNICIPALITY_ID, ID, EMAIL);
		verifyNoMoreInteractions(serviceMock);
	}

}
