package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Status;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.CustomTask;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklists;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklistFilters;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.service.EmployeeChecklistService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class EmployeeChecklistResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ID = UUID.randomUUID().toString();
	private static final String SUB_ID = UUID.randomUUID().toString();
	private static final String USER_ID = "usr123";
	private static final String BASE_PATH = "/{municipalityId}/employee-checklists";

	@MockitoBean
	private EmployeeChecklistService serviceMock;

	@Captor
	private ArgumentCaptor<OngoingEmployeeChecklistFilters> ongoingEmployeeChecklistFiltersCaptor;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void fetchAllOngoingEmployeeChecklists() {
		// Arrange
		final var path = "/ongoing";
		final var page = 1;
		final var limit = 10;
		final var mockedResponse = OngoingEmployeeChecklists.builder().build();

		when(serviceMock.getOngoingEmployeeChecklists(eq(MUNICIPALITY_ID), any(OngoingEmployeeChecklistFilters.class))).thenReturn(mockedResponse);

		// Act
		var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path)
				.queryParam("page", page)
				.queryParam("limit", limit)
				.queryParam("sortDirection", Sort.Direction.DESC)
				.queryParam("sortBy", List.of("employee.firstName", "employee.lastName"))
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(OngoingEmployeeChecklists.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify

		assertThat(response).isEqualTo(mockedResponse);
		verify(serviceMock).getOngoingEmployeeChecklists(eq(MUNICIPALITY_ID), ongoingEmployeeChecklistFiltersCaptor.capture());
		assertThat(ongoingEmployeeChecklistFiltersCaptor.getValue()).satisfies(pagingBase -> {
			assertThat(pagingBase.getPage()).isEqualTo(page);
			assertThat(pagingBase.getLimit()).isEqualTo(limit);
			assertThat(pagingBase.getSortDirection()).isEqualTo(Sort.Direction.DESC);
			assertThat(pagingBase.getSortBy()).containsExactly("employee.firstName", "employee.lastName");
		});
	}

	@Test
	void fetchChecklistForEmployee() {
		// Arrange
		final var path = "/employee/{userId}";
		final var mockedResponse = EmployeeChecklist.builder().build();

		when(serviceMock.fetchChecklistForEmployee(MUNICIPALITY_ID, USER_ID)).thenReturn(Optional.of(mockedResponse));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "userId", USER_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);

		verify(serviceMock).fetchChecklistForEmployee(MUNICIPALITY_ID, USER_ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchChecklistForEmployeeWithNoActiveChecklist() {
		// Arrange
		final var path = "/employee/{userId}";

		when(serviceMock.fetchChecklistForEmployee(MUNICIPALITY_ID, USER_ID)).thenReturn(Optional.empty());

		// Act
		webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "userId", USER_ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody()
			.isEmpty();

		// Assert and verify
		verify(serviceMock).fetchChecklistForEmployee(MUNICIPALITY_ID, USER_ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchChecklistsForManager() {
		// Arrange
		final var path = "/manager/{userId}";
		final var mockedResponse = List.of(EmployeeChecklist.builder().build(), EmployeeChecklist.builder().build());

		when(serviceMock.fetchChecklistsForManager(MUNICIPALITY_ID, USER_ID)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "userId", USER_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(EmployeeChecklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);

		verify(serviceMock).fetchChecklistsForManager(MUNICIPALITY_ID, USER_ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteEmployeeChecklist() {
		// Arrange
		final var path = "/{employeeChecklistId}";

		// Act
		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert and verify
		verify(serviceMock).deleteEmployeChecklist(MUNICIPALITY_ID, ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createCustomTask() {
		// Arrange
		final var mockedResponse = CustomTask.builder().withId(UUID.randomUUID().toString()).build();
		final var path = "/{employeeChecklistId}/phases/{phaseId}/customtasks";
		final var request = CustomTaskCreateRequest.builder()
			.withHeading("heading")
			.withQuestionType(QuestionType.YES_OR_NO_WITH_TEXT)
			.withText("text")
			.withSortOrder(1)
			.withCreatedBy("someUser")
			.build();

		when(serviceMock.createCustomTask(MUNICIPALITY_ID, ID, SUB_ID, request)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "phaseId", SUB_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location("/%s/employee-checklists/%s/customtasks/%s".formatted(MUNICIPALITY_ID, ID, mockedResponse.getId()))
			.expectBody(CustomTask.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);

		verify(serviceMock).createCustomTask(MUNICIPALITY_ID, ID, SUB_ID, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void setMentor() {
		EmployeeChecklist.builder().withId(UUID.randomUUID().toString()).build();
		final var path = "/{employeeChecklistId}/mentor";
		final var request = Mentor.builder()
			.withUserId("someUserId")
			.withName("someName")
			.build();

		// Act
		webTestClient.put()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isAccepted()
			.expectBody().isEmpty();

		verify(serviceMock).setMentor(MUNICIPALITY_ID, ID, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteMentor() {
		final var path = "/{employeeChecklistId}/mentor";

		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).deleteMentor(MUNICIPALITY_ID, ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void readCustomTask() {
		// Arrange
		final var mockedResponse = CustomTask.builder().build();
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		when(serviceMock.readCustomTask(MUNICIPALITY_ID, ID, SUB_ID)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "taskId", SUB_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CustomTask.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);

		verify(serviceMock).readCustomTask(MUNICIPALITY_ID, ID, SUB_ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateCustomTask() {
		// Arrange
		final var mockedResponse = CustomTask.builder().build();
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";
		final var request = CustomTaskUpdateRequest.builder()
			.withHeading("heading")
			.withQuestionType(QuestionType.YES_OR_NO)
			.withText("text")
			.withSortOrder(1)
			.withUpdatedBy("someUser")
			.build();

		when(serviceMock.updateCustomTask(MUNICIPALITY_ID, ID, SUB_ID, request)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "taskId", SUB_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CustomTask.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);

		verify(serviceMock).updateCustomTask(MUNICIPALITY_ID, ID, SUB_ID, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteCustomTask() {
		// Arrange
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		// Act
		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "taskId", SUB_ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert and verify
		verify(serviceMock).deleteCustomTask(MUNICIPALITY_ID, ID, SUB_ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateAllTasksFulfilmentInPhase() {
		// Arrange
		final var employeeChecklistPhase = EmployeeChecklistPhase.builder().build();
		final var path = "/{employeeChecklistId}/phases/{phaseId}";
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(FulfilmentStatus.TRUE)
			.withUpdatedBy(USER_ID)
			.build();

		when(serviceMock.updateAllTasksInPhase(MUNICIPALITY_ID, ID, SUB_ID, request)).thenReturn(employeeChecklistPhase);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "phaseId", SUB_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistPhase.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(employeeChecklistPhase);

		verify(serviceMock).updateAllTasksInPhase(MUNICIPALITY_ID, ID, SUB_ID, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateTaskFulfilment() {
		// Arrange
		final var mockedResponse = EmployeeChecklistTask.builder().build();
		final var path = "/{employeeChecklistId}/tasks/{taskId}";
		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(FulfilmentStatus.FALSE)
			.withUpdatedBy(USER_ID)
			.withResponseText("responseText")
			.build();

		when(serviceMock.updateTaskFulfilment(MUNICIPALITY_ID, ID, SUB_ID, request)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "employeeChecklistId", ID, "taskId", SUB_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistTask.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);

		verify(serviceMock).updateTaskFulfilment(MUNICIPALITY_ID, ID, SUB_ID, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void initiateChecklistsForAllEmployees() {
		// Arrange
		final var path = "/initialize";
		final var mockedResponse = EmployeeChecklistResponse.builder()
			.withSummary("summary")
			.withDetails(List.of(Detail.builder().withInformation("information").withStatus(Status.OK).build()))
			.build();

		when(serviceMock.initiateEmployeeChecklists(MUNICIPALITY_ID)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);

		verify(serviceMock).initiateEmployeeChecklists(MUNICIPALITY_ID);
	}

	@Test
	void initiateChecklistForSpecificEmployees() {
		// Arrange
		final var path = "/initialize/{personId}";
		final var mockedResponse = EmployeeChecklistResponse.builder()
			.withSummary("summary")
			.withDetails(List.of(Detail.builder().withInformation("information").withStatus(Status.OK).build()))
			.build();

		when(serviceMock.initiateSpecificEmployeeChecklist(MUNICIPALITY_ID, ID)).thenReturn(mockedResponse);

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH + path).build(Map.of("municipalityId", MUNICIPALITY_ID, "personId", ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(mockedResponse);

		verify(serviceMock).initiateSpecificEmployeeChecklist(MUNICIPALITY_ID, ID);
	}
}
