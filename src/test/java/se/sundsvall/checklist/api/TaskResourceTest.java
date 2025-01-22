package se.sundsvall.checklist.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.checklist.TestObjectFactory.createTask;
import static se.sundsvall.checklist.TestObjectFactory.createTaskCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createTaskUpdateRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.service.TaskService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class TaskResourceTest {
	private static final String MUNICIPALITY_ID = "2281";
	private static final String ID = UUID.randomUUID().toString();
	private static final String SUB_ID = UUID.randomUUID().toString();
	private static final String BASE_PATH = "/{municipalityId}/checklists/{checklistId}/phases/{phaseId}/tasks";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private TaskService mockTaskService;

	@Test
	void fetchChecklistPhaseTasks() {
		final var mockedResponse = List.of(createTask(), createTask());
		when(mockTaskService.getTasks(MUNICIPALITY_ID, ID, SUB_ID)).thenReturn(mockedResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID, "phaseId", SUB_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Task.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(mockedResponse);
		verify(mockTaskService).getTasks(MUNICIPALITY_ID, ID, SUB_ID);
		verifyNoMoreInteractions(mockTaskService);
	}

	@Test
	void fetchChecklistPhaseTaskTest() {
		final var taskId = randomUUID().toString();
		final var mockedResponse = createTask(task -> task.setId(taskId));
		when(mockTaskService.getTask(MUNICIPALITY_ID, ID, SUB_ID, mockedResponse.getId())).thenReturn(mockedResponse);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID, "phaseId", SUB_ID, "taskId", taskId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Task.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(mockedResponse);
		verify(mockTaskService).getTask(MUNICIPALITY_ID, ID, SUB_ID, taskId);
		verifyNoMoreInteractions(mockTaskService);
	}

	@Test
	void createChecklistPhaseTaskTest() {
		final var mockedResponse = createTask(task -> task.setId(randomUUID().toString()));
		when(mockTaskService.createTask(MUNICIPALITY_ID, ID, SUB_ID, createTaskCreateRequest())).thenReturn(mockedResponse);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID, "phaseId", SUB_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(createTaskCreateRequest())
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/%s/checklists/%s/phases/%s/tasks/%s".formatted(MUNICIPALITY_ID, ID, SUB_ID, mockedResponse.getId()))
			.expectBody().isEmpty();

		verify(mockTaskService).createTask(MUNICIPALITY_ID, ID, SUB_ID, createTaskCreateRequest());
		verifyNoMoreInteractions(mockTaskService);
	}

	@Test
	void updateChecklistPhaseTaskTest() {
		final var taskId = randomUUID().toString();
		final var mockedResponse = createTask(task -> task.setId(taskId));
		final var requestBody = createTaskUpdateRequest();
		when(mockTaskService.updateTask(MUNICIPALITY_ID, ID, SUB_ID, taskId, requestBody)).thenReturn(mockedResponse);

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID, "phaseId", SUB_ID, "taskId", taskId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Task.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(mockedResponse);
		verify(mockTaskService).updateTask(MUNICIPALITY_ID, ID, SUB_ID, taskId, requestBody);
		verifyNoMoreInteractions(mockTaskService);
	}

	@Test
	void deleteChecklistPhaseTaskTest() {
		final var taskId = randomUUID().toString();
		final var user = "Chuck Norris";

		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "checklistId", ID, "phaseId", SUB_ID, "taskId", taskId)))
			.header("x-user", user)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(mockTaskService).deleteTask(MUNICIPALITY_ID, ID, SUB_ID, taskId, user);
		verifyNoMoreInteractions(mockTaskService);
	}

}
