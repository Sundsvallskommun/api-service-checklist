package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import se.sundsvall.checklist.api.model.DelegatedEmployeeChecklistResponse;
import se.sundsvall.checklist.service.DelegationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DelegationResourceTest {

	private static final String PATH_PREFIX = "/employee-checklists";

	@MockBean
	private DelegationService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void delegateTo() {
		final var id = UUID.randomUUID().toString();
		final var email = "test@test.com";

		webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + "/{uuid}/delegate-to/{email}").build(Map.of("uuid", id, "email", email)))
			.exchange()
			.expectStatus().isCreated()
			.expectBody().isEmpty();

		verify(serviceMock).delegateEmployeeChecklist(id, email);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchDelegations() {
		final var userName = "abc20def";
		final var mockResponse = DelegatedEmployeeChecklistResponse.builder().build();
		when(serviceMock.fetchDelegatedEmployeeChecklistsByUserName(any())).thenReturn(mockResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH_PREFIX + "/delegated-to/{userName}").build(Map.of("userName", userName)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(DelegatedEmployeeChecklistResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(mockResponse);
		verify(serviceMock).fetchDelegatedEmployeeChecklistsByUserName(userName);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void removeDelegation() {
		final var id = UUID.randomUUID().toString();

		webTestClient.delete()
			.uri(builder -> builder.path(PATH_PREFIX + "/{uuid}/delegated-to/{email}").build(Map.of("uuid", id, "email", "test@test.com")))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).removeEmployeeChecklistDelegation(id, "test@test.com");
		verifyNoMoreInteractions(serviceMock);
	}

}
