package se.sundsvall.checklist.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.api.model.TaskCreateRequest;
import se.sundsvall.checklist.api.model.TaskUpdateRequest;
import se.sundsvall.checklist.service.TaskService;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/checklists/{checklistId}/phases/{phaseId}/tasks")
@Tag(name = "Task resources", description = "Resources for managing tasks in a phase")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class TaskResource {

	private final TaskService taskService;

	TaskResource(final TaskService taskService) {
		this.taskService = taskService;
	}

	@Operation(summary = "Fetch all tasks in a checklist phase")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<List<Task>> fetchChecklistPhaseTasks(
		@PathVariable @ValidUuid final String checklistId,
		@PathVariable @ValidUuid final String phaseId) {
		return ok(taskService.getAllTasksInPhase(checklistId, phaseId));
	}

	@Operation(summary = "Fetch task in a checklist phase")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@GetMapping(value = "/{taskId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Task> fetchChecklistPhaseTask(
		@PathVariable @ValidUuid final String checklistId,
		@PathVariable @ValidUuid final String phaseId,
		@PathVariable @ValidUuid final String taskId) {
		return ok(taskService.getTaskInPhaseById(checklistId, phaseId, taskId));
	}

	@Operation(summary = "Create task in checklist phase")
	@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	@PostMapping(produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE }, consumes = { APPLICATION_JSON_VALUE })
	ResponseEntity<Void> createChecklistPhaseTask(
		@PathVariable @ValidUuid final String checklistId,
		@PathVariable @ValidUuid final String phaseId,
		@RequestBody @Valid final TaskCreateRequest request) {
		final var task = taskService.createTask(checklistId, phaseId, request);
		return created(UriComponentsBuilder.fromPath("/checklists/{checklistId}/phases/{phaseId}/tasks/{taskId}")
			.buildAndExpand(checklistId, phaseId, task.getId())
			.toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Update task in checklist phase")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@PatchMapping(value = "/{taskId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }, consumes = { APPLICATION_JSON_VALUE })
	ResponseEntity<Task> updateChecklistPhaseTask(
		@PathVariable @ValidUuid final String checklistId,
		@PathVariable @ValidUuid final String phaseId,
		@PathVariable @ValidUuid final String taskId,
		@RequestBody @Valid final TaskUpdateRequest request) {
		return ok(taskService.updateTask(checklistId, phaseId, taskId, request));
	}

	@Operation(summary = "Delete task in checklist phase")
	@ApiResponse(responseCode = "204", description = "No Content", useReturnTypeSchema = true)
	@DeleteMapping(value = "/{taskId}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> deleteChecklistPhaseTask(
		@PathVariable @ValidUuid final String checklistId,
		@PathVariable @ValidUuid final String phaseId,
		@PathVariable @ValidUuid final String taskId) {
		taskService.deleteTask(checklistId, phaseId, taskId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}

}
