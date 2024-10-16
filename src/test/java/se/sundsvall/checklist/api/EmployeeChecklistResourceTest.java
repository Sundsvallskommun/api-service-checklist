package se.sundsvall.checklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Status;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.CustomTask;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPaginatedResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
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

	private static final String PATH_PREFIX = "/employee-checklists";

	@MockBean
	private EmployeeChecklistService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void findEmployeeChecklistBySearchString() {
		// Arrange
		final var response1 = new EmployeeChecklistPaginatedResponse();

		when(serviceMock.findEmployeeChecklistsBySearchString(any(), any())).thenReturn(response1);

		// Act
		final var response = webTestClient.get()
			.uri(PATH_PREFIX + "/search?searchString=test")
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistPaginatedResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(response1);

		verify(serviceMock).findEmployeeChecklistsBySearchString(any(), any());
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchChecklistForEmployee() {
		// Arrange
		final var userId = "abc12def";
		final var path = "/employee/{userId}";
		final var employeeChecklist = EmployeeChecklist.builder().build();

		when(serviceMock.fetchChecklistForEmployee(userId)).thenReturn(Optional.of(employeeChecklist));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("userId", userId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(employeeChecklist);

		verify(serviceMock).fetchChecklistForEmployee(userId);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchChecklistForEmployeeWithNoActiveChecklist() {
		// Arrange
		final var userId = "abc12def";
		final var path = "/employee/{userId}";

		when(serviceMock.fetchChecklistForEmployee(userId)).thenReturn(Optional.empty());

		// Act
		webTestClient.get()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("userId", userId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody()
			.isEmpty();

		// Assert and verify
		verify(serviceMock).fetchChecklistForEmployee(userId);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void fetchChecklistsForManager() {
		// Arrange
		final var userId = "abc12def";
		final var path = "/manager/{userId}";
		final var employeeChecklists = List.of(EmployeeChecklist.builder().build(), EmployeeChecklist.builder().build());

		when(serviceMock.fetchChecklistsForManager(userId)).thenReturn(employeeChecklists);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("userId", userId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(EmployeeChecklist.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(employeeChecklists);

		verify(serviceMock).fetchChecklistsForManager(userId);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteEmployeeChecklist() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var path = "/{employeeChecklistId}";

		// Act
		webTestClient.delete()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", employeeChecklistId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert and verify
		verify(serviceMock).deleteEmployeChecklist(employeeChecklistId);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var customTask = CustomTask.builder().build();
		final var path = "/{employeeChecklistId}/phases/{phaseId}/customtasks";
		final var request = CustomTaskCreateRequest.builder()
			.withHeading("heading")
			.withQuestionType(QuestionType.YES_OR_NO_WITH_TEXT)
			.withText("text")
			.withSortOrder(1)
			.build();

		when(serviceMock.createCustomTask(employeeChecklistId, phaseId, request)).thenReturn(customTask);

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", employeeChecklistId, "phaseId", phaseId)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().exists(LOCATION)
			.expectBody(CustomTask.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(customTask);

		verify(serviceMock).createCustomTask(employeeChecklistId, phaseId, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void readCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var customTask = CustomTask.builder().build();
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		when(serviceMock.readCustomTask(employeeChecklistId, taskId)).thenReturn(customTask);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", employeeChecklistId, "taskId", taskId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CustomTask.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(customTask);

		verify(serviceMock).readCustomTask(employeeChecklistId, taskId);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var customTask = CustomTask.builder().build();
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";
		final var request = CustomTaskUpdateRequest.builder()
			.withHeading("heading")
			.withQuestionType(QuestionType.YES_OR_NO)
			.withText("text")
			.withSortOrder(1)
			.build();

		when(serviceMock.updateCustomTask(employeeChecklistId, taskId, request)).thenReturn(customTask);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", employeeChecklistId, "taskId", taskId)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CustomTask.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(customTask);

		verify(serviceMock).updateCustomTask(employeeChecklistId, taskId, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteCustomTask() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var path = "/{employeeChecklistId}/customtasks/{taskId}";

		// Act
		webTestClient.delete()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", employeeChecklistId, "taskId", taskId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert and verify
		verify(serviceMock).deleteCustomTask(employeeChecklistId, taskId);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateAllTasksFulfilmentInPhase() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var phaseId = UUID.randomUUID().toString();
		final var employeeChecklistPhase = EmployeeChecklistPhase.builder().build();
		final var path = "/{employeeChecklistId}/phases/{phaseId}";
		final var request = EmployeeChecklistPhaseUpdateRequest.builder()
			.withTasksFulfilmentStatus(FulfilmentStatus.TRUE)
			.build();

		when(serviceMock.updateAllTasksInPhase(employeeChecklistId, phaseId, request)).thenReturn(employeeChecklistPhase);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", employeeChecklistId, "phaseId", phaseId)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistPhase.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(employeeChecklistPhase);

		verify(serviceMock).updateAllTasksInPhase(employeeChecklistId, phaseId, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateTaskFulfilment() {
		// Arrange
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var taskId = UUID.randomUUID().toString();
		final var employeeChecklistTask = EmployeeChecklistTask.builder().build();
		final var path = "/{employeeChecklistId}/tasks/{taskId}";
		final var request = EmployeeChecklistTaskUpdateRequest.builder()
			.withFulfilmentStatus(FulfilmentStatus.FALSE)
			.withResponseText("responseText")
			.build();

		when(serviceMock.updateTaskFulfilment(employeeChecklistId, taskId, request)).thenReturn(employeeChecklistTask);

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("employeeChecklistId", employeeChecklistId, "taskId", taskId)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistTask.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(employeeChecklistTask);

		verify(serviceMock).updateTaskFulfilment(employeeChecklistId, taskId, request);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void initiateChecklistsForAllEmployees() {
		// Arrange
		final var path = "/initialize";
		final var result = EmployeeChecklistResponse.builder()
			.withSummary("summary")
			.withDetails(List.of(Detail.builder().withInformation("information").withStatus(Status.OK).build()))
			.build();

		when(serviceMock.initiateEmployeeChecklists()).thenReturn(result);

		// Act
		final var response = webTestClient.post()
			.uri(PATH_PREFIX + path)
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(result);

		verify(serviceMock).initiateEmployeeChecklists();
	}

	@Test
	void initiateChecklistForSpecificEmployees() {
		// Arrange
		final var path = "/initialize/{personId}";
		final var id = UUID.randomUUID().toString();
		final var result = EmployeeChecklistResponse.builder()
			.withSummary("summary")
			.withDetails(List.of(Detail.builder().withInformation("information").withStatus(Status.OK).build()))
			.build();

		when(serviceMock.initiateSpecificEmployeeChecklist(id)).thenReturn(result);

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH_PREFIX + path).build(Map.of("personId", id)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(EmployeeChecklistResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(result);

		verify(serviceMock).initiateSpecificEmployeeChecklist(id);
	}
}
