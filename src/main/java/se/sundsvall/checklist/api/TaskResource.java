package se.sundsvall.checklist.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.api.model.TaskCreateRequest;
import se.sundsvall.checklist.api.model.TaskUpdateRequest;
import se.sundsvall.checklist.service.TaskService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/{municipalityId}/checklists/{checklistId}/phases/{phaseId}/tasks")
@Tag(name = "Task resources", description = "Resources for managing tasks in a phase")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class TaskResource {

	private final TaskService taskService;

	TaskResource(final TaskService taskService) {
		this.taskService = taskService;
	}

	@Operation(summary = "Fetch all tasks in a checklist phase", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<Task>> fetchChecklistPhaseTasks(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "checklistId", description = "Checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String checklistId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId) {

		return ok(taskService.getTasks(municipalityId, checklistId, phaseId));
	}

	@Operation(summary = "Fetch task in a checklist phase", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(value = "/{taskId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Task> fetchChecklistPhaseTask(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "checklistId", description = "Checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String checklistId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId,
		@Parameter(name = "taskId", description = "Task id", example = "55904052-0db0-4622-850c-3273ee60def4") @PathVariable @ValidUuid final String taskId) {

		return ok(taskService.getTask(municipalityId, checklistId, phaseId, taskId));
	}

	@Operation(summary = "Create task in checklist phase", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> createChecklistPhaseTask(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "checklistId", description = "Checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String checklistId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId,
		@RequestBody @Valid final TaskCreateRequest request) {

		final var task = taskService.createTask(municipalityId, checklistId, phaseId, request);
		return created(UriComponentsBuilder.fromPath("/{municipalityId}/checklists/{checklistId}/phases/{phaseId}/tasks/{taskId}")
			.buildAndExpand(municipalityId, checklistId, phaseId, task.getId())
			.toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Update task in checklist phase", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PatchMapping(value = "/{taskId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Task> updateChecklistPhaseTask(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "checklistId", description = "Checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String checklistId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId,
		@Parameter(name = "taskId", description = "Task id", example = "55904052-0db0-4622-850c-3273ee60def4") @PathVariable @ValidUuid final String taskId,
		@RequestBody @Valid final TaskUpdateRequest request) {

		return ok(taskService.updateTask(municipalityId, checklistId, phaseId, taskId, request));
	}

	@Operation(summary = "Delete task in checklist phase", responses = {
		@ApiResponse(responseCode = "204", description = "No Content", useReturnTypeSchema = true)
	})
	@DeleteMapping(value = "/{taskId}", produces = ALL_VALUE)
	ResponseEntity<Void> deleteChecklistPhaseTask(
		@Parameter(name = "x-userid", description = "Which user sent the request") @RequestHeader(name = "x-userid", required = false) final String userId,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "checklistId", description = "Checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String checklistId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId,
		@Parameter(name = "taskId", description = "Task id", example = "55904052-0db0-4622-850c-3273ee60def4") @PathVariable @ValidUuid final String taskId) {

		taskService.deleteTask(municipalityId, checklistId, phaseId, taskId, userId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}
}
