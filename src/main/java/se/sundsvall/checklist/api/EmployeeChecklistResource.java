package se.sundsvall.checklist.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.accepted;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import se.sundsvall.checklist.api.model.CustomTask;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.service.EmployeeChecklistService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/{municipalityId}/employee-checklists")
@Tag(name = "Employee checklist resources", description = "Resources for managing employee checklists")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class EmployeeChecklistResource {

	private final EmployeeChecklistService employeeChecklistService;

	EmployeeChecklistResource(EmployeeChecklistService employeeChecklistService) {
		this.employeeChecklistService = employeeChecklistService;
	}

	@Operation(summary = "Fetch checklist where user acts as employee", description = "Fetch a users checklist where the user has the role of employee", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "204", description = "No employee checklist found")
	})
	@GetMapping(value = "/employee/{username}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<EmployeeChecklist> fetchChecklistForEmployee(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "username", description = "Username for user to fetch checklists for", example = "usr123") @PathVariable final String username) {

		return employeeChecklistService.fetchChecklistForEmployee(municipalityId, username)
			.map(ResponseEntity::ok)
			.orElse(noContent().build());
	}

	@Operation(summary = "Fetch checklists where user acts as manager", description = "Fetch a users checklists where the user has the role of manager", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(value = "/manager/{username}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<EmployeeChecklist>> fetchChecklistsForManager(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "username", description = "Username for user to fetch checklists for", example = "usr123") @PathVariable final String username) {

		return ok(employeeChecklistService.fetchChecklistsForManager(municipalityId, username));
	}

	@Operation(summary = "Delete an employee checklist", description = "Delete an employee checklist completely", responses = {
		@ApiResponse(responseCode = "204", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@DeleteMapping(value = "/{employeeChecklistId}", produces = ALL_VALUE)
	ResponseEntity<Void> deleteEmployeeChecklist(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId) {

		employeeChecklistService.deleteEmployeChecklist(municipalityId, employeeChecklistId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Create a custom task", description = "Create a custom task connected to a specific employee checklist", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/{employeeChecklistId}/phases/{phaseId}/customtasks", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<CustomTask> createCustomTask(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId,
		@RequestBody @Valid final CustomTaskCreateRequest request) {

		final var createdTask = employeeChecklistService.createCustomTask(municipalityId, employeeChecklistId, phaseId, request);
		return created(
			UriComponentsBuilder
				.fromPath("/{municipalityId}/employee-checklists/{employeeChecklistId}/customtasks/{taskId}")
				.buildAndExpand(municipalityId, employeeChecklistId, createdTask.getId())
				.toUri())
			.body(createdTask);
	}

	@Operation(summary = "Read a custom task", description = "Read a custom task connected to a specific employee checklist", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(value = "/{employeeChecklistId}/customtasks/{taskId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<CustomTask> readCustomTask(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@Parameter(name = "taskId", description = "Task id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String taskId) {

		return ok(employeeChecklistService.readCustomTask(municipalityId, employeeChecklistId, taskId));
	}

	@Operation(summary = "Update a custom task", description = "Update a custom task connected to a specific employee checklist", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PatchMapping(value = "/{employeeChecklistId}/customtasks/{taskId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<CustomTask> updateCustomTask(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@Parameter(name = "taskId", description = "Task id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String taskId,
		@RequestBody @Valid final CustomTaskUpdateRequest request) {

		return ok(employeeChecklistService.updateCustomTask(municipalityId, employeeChecklistId, taskId, request));
	}

	@Operation(summary = "Delete a custom task", description = "Delete a custom task from an employee checklist", responses = {
		@ApiResponse(responseCode = "204", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@DeleteMapping(value = "/{employeeChecklistId}/customtasks/{taskId}", produces = ALL_VALUE)
	ResponseEntity<Void> deleteCustomTask(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@Parameter(name = "taskId", description = "Task id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String taskId) {

		employeeChecklistService.deleteCustomTask(municipalityId, employeeChecklistId, taskId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Update of all tasks in a phase", description = "Bulk update of sent in attributes for all tasks in phase", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PatchMapping(value = "/{employeeChecklistId}/phases/{phaseId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<EmployeeChecklistPhase> updateAllTasksInPhase(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId,
		@RequestBody @Valid final EmployeeChecklistPhaseUpdateRequest request) {

		return ok(employeeChecklistService.updateAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request));
	}

	@Operation(summary = "Update fulfilment of a task", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PatchMapping(value = "/{employeeChecklistId}/tasks/{taskId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<EmployeeChecklistTask> updateTaskFulfilment(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@Parameter(name = "taskId", description = "Task id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String taskId,
		@RequestBody @Valid final EmployeeChecklistTaskUpdateRequest request) {

		return ok(employeeChecklistService.updateTaskFulfilment(municipalityId, employeeChecklistId, taskId, request));
	}

	@Operation(summary = "Inititalize checklists for new employees", description = "Trigger creation of checklists for all known new employees", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/initialize", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<EmployeeChecklistResponse> initiateEmployeeChecklists(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId) {

		return ok(employeeChecklistService.initiateEmployeeChecklists(municipalityId));
	}

	@Operation(summary = "Inititalize checklists for a specific employee", description = "Trigger creation of checklist for employee matching sent in person id", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/initialize/{personId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<EmployeeChecklistResponse> initiateSpecificEmployeeChecklist(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "personId", description = "Person id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String personId) {

		return ok(employeeChecklistService.initiateSpecificEmployeeChecklist(municipalityId, personId));
	}

	@Operation(summary = "Set the mentor", description = "Set the mentor on a specific employee checklist", responses = {
		@ApiResponse(responseCode = "202", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PutMapping(value = "/{employeeChecklistId}/mentor", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> setMentor(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@RequestBody @Valid final Mentor mentor) {

		employeeChecklistService.setMentor(municipalityId, employeeChecklistId, mentor);
		return accepted().header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Delete the mentor", description = "Delete the mentor on a specific employee checklist", responses = {
		@ApiResponse(responseCode = "204", description = "No Content", useReturnTypeSchema = true)
	})
	@DeleteMapping(value = "/{employeeChecklistId}/mentor", produces = ALL_VALUE)
	ResponseEntity<Void> deleteMentor(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId) {
		employeeChecklistService.deleteMentor(municipalityId, employeeChecklistId);

		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}
}
