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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.service.TaskService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class TaskResourceTest {

	private static final String BASE_PATH = "/checklists/{checklistId}/phases/{phaseId}/tasks";

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private TaskService mockTaskService;

	@Test
	void fetchChecklistPhaseTasksTest() {
		final var checklistId = randomUUID().toString();
		final var phaseId = randomUUID().toString();
		final var taskList = List.of(createTask(), createTask());
		when(mockTaskService.getAllTasksInPhase(checklistId, phaseId)).thenReturn(taskList);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Task.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(taskList);
		verify(mockTaskService).getAllTasksInPhase(checklistId, phaseId);
		verifyNoMoreInteractions(mockTaskService);
	}

	@Test
	void fetchChecklistPhaseTaskTest() {
		final var checklistId = randomUUID().toString();
		final var phaseId = randomUUID().toString();
		final var task = createTask(task1 -> task1.setId(randomUUID().toString()));
		when(mockTaskService.getTaskInPhaseById(checklistId, phaseId, task.getId())).thenReturn(task);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId, "taskId", task.getId())))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Task.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(task);
		verify(mockTaskService).getTaskInPhaseById(checklistId, phaseId, task.getId());
		verifyNoMoreInteractions(mockTaskService);
	}

	@Test
	void createChecklistPhaseTaskTest() {
		final var checklistId = randomUUID().toString();
		final var phaseId = randomUUID().toString();
		final var task = createTask(task1 -> task1.setId(randomUUID().toString()));
		when(mockTaskService.createTask(checklistId, phaseId, createTaskCreateRequest())).thenReturn(task);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_PATH).build(Map.of("checklistId", checklistId, "phaseId", phaseId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(createTaskCreateRequest())
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/checklists/" + checklistId + "/phases/" + phaseId + "/tasks/" + task.getId())
			.expectBody().isEmpty();

		verify(mockTaskService).createTask(checklistId, phaseId, createTaskCreateRequest());
		verifyNoMoreInteractions(mockTaskService);
	}

	@Test
	void updateChecklistPhaseTaskTest() {
		final var checklistId = randomUUID().toString();
		final var phaseId = randomUUID().toString();
		final var taskId = randomUUID().toString();
		final var task = createTask(task1 -> task1.setId(taskId));
		final var requestBody = createTaskUpdateRequest();
		when(mockTaskService.updateTask(checklistId, phaseId, taskId, requestBody)).thenReturn(task);

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId, "taskId", taskId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Task.class)
			.returnResult();

		assertThat(response.getResponseBody()).isEqualTo(task);
		verify(mockTaskService).updateTask(checklistId, phaseId, taskId, requestBody);
		verifyNoMoreInteractions(mockTaskService);
	}

	@Test
	void deleteChecklistPhaseTaskTest() {
		final var checklistId = randomUUID().toString();
		final var phaseId = randomUUID().toString();
		final var taskId = randomUUID().toString();

		webTestClient.delete()
			.uri(builder -> builder.path(BASE_PATH + "/{taskId}").build(Map.of("checklistId", checklistId, "phaseId", phaseId, "taskId", taskId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(mockTaskService).deleteTask(checklistId, phaseId, taskId);
		verifyNoMoreInteractions(mockTaskService);
	}

}
