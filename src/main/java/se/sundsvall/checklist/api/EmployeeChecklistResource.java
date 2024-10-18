package se.sundsvall.checklist.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.data.domain.Pageable;
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
import se.sundsvall.checklist.api.model.EmployeeChecklistPaginatedResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.specification.EmployeeCheclistFilterSpecification;
import se.sundsvall.checklist.service.EmployeeChecklistService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/{municipalityId}/employee-checklists")
@Tag(name = "Employee checklist resources", description = "Resources for managing employee checklists")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class EmployeeChecklistResource {

	private final EmployeeChecklistService employeeChecklistService;

	EmployeeChecklistResource(EmployeeChecklistService employeeChecklistService) {
		this.employeeChecklistService = employeeChecklistService;
	}

	@Operation(summary = "Fetches employee checklists where pre defined fields matches provided search string", description = """
		Search string is matched against the following
		attributes using 'LikeIgnoreCase':
		- Employee's first name
		- Employee's last name
		- Employee's username
		- Employee's company name
		- Employee's manager first name
		- Employee's manager last name
		- Delegate's first name
		- Delegate's last name
		""", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(value = "/search", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<EmployeeChecklistPaginatedResponse> findemployeeChecklistsBySearchString(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		final EmployeeCheclistFilterSpecification specification,
		final Pageable pageable) {
		return ok(employeeChecklistService.findEmployeeChecklistsBySearchString(specification, pageable));
	}

	@Operation(summary = "Fetch checklist where user acts as employee", description = "Fetch a users checklist where the user has the role of employee", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "204", description = "No employee checklist found")
	})
	@GetMapping(value = "/employee/{userId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<EmployeeChecklist> fetchChecklistForEmployee(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable final String userId) {

		return employeeChecklistService.fetchChecklistForEmployee(userId)
			.map(employeeChecklist -> ok().body(employeeChecklist))
			.orElse(noContent().build());
	}

	@Operation(summary = "Fetch checklists where user acts as manager", description = "Fetch a users checklists where the user has the role of manager", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(value = "/manager/{userId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<List<EmployeeChecklist>> fetchChecklistsForManager(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable final String userId) {

		return ok().body(employeeChecklistService.fetchChecklistsForManager(userId));
	}

	@Operation(summary = "Delete an employee checklist", description = "Delete an employee checklist completely", responses = {
		@ApiResponse(responseCode = "204", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@DeleteMapping(value = "/{employeeChecklistId}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> deleteEmployeeChecklist(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String employeeChecklistId) {

		employeeChecklistService.deleteEmployeChecklist(employeeChecklistId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Create a custom task", description = "Create a custom task connected to a specific employee checklist", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/{employeeChecklistId}/phases/{phaseId}/customtasks", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<CustomTask> createCustomTask(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String employeeChecklistId,
		@PathVariable @ValidUuid final String phaseId,
		@RequestBody @Valid final CustomTaskCreateRequest request) {

		final var createdTask = employeeChecklistService.createCustomTask(employeeChecklistId, phaseId, request);
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
	@GetMapping(value = "/{employeeChecklistId}/customtasks/{taskId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<CustomTask> readCustomTask(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String employeeChecklistId,
		@PathVariable @ValidUuid final String taskId) {

		return ok().body(employeeChecklistService.readCustomTask(employeeChecklistId, taskId));
	}

	@Operation(summary = "Update a custom task", description = "Update a custom task connected to a specific employee checklist", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PatchMapping(value = "/{employeeChecklistId}/customtasks/{taskId}", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<CustomTask> updateCustomTask(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String employeeChecklistId,
		@PathVariable @ValidUuid final String taskId,
		@RequestBody @Valid final CustomTaskUpdateRequest request) {

		return ok().body(employeeChecklistService.updateCustomTask(employeeChecklistId, taskId, request));
	}

	@Operation(summary = "Delete a custom task", description = "Delete a custom task from an employee checklist", responses = {
		@ApiResponse(responseCode = "204", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@DeleteMapping(value = "/{employeeChecklistId}/customtasks/{taskId}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> deleteCustomTask(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String employeeChecklistId,
		@PathVariable @ValidUuid final String taskId) {

		employeeChecklistService.deleteCustomTask(employeeChecklistId, taskId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Update of all tasks in a phase", description = "Bulk update of sent in attributes for all tasks in phase", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PatchMapping(value = "/{employeeChecklistId}/phases/{phaseId}", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<EmployeeChecklistPhase> updateAllTasksInPhase(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String employeeChecklistId,
		@PathVariable @ValidUuid final String phaseId,
		@RequestBody @Valid final EmployeeChecklistPhaseUpdateRequest request) {

		return ok().body(employeeChecklistService.updateAllTasksInPhase(employeeChecklistId, phaseId, request));
	}

	@Operation(summary = "Update fulfilment of a task", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PatchMapping(value = "/{employeeChecklistId}/tasks/{taskId}", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<EmployeeChecklistTask> updateTaskFulfilment(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String employeeChecklistId,
		@PathVariable @ValidUuid final String taskId,
		@RequestBody @Valid final EmployeeChecklistTaskUpdateRequest request) {

		return ok().body(employeeChecklistService.updateTaskFulfilment(employeeChecklistId, taskId, request));
	}

	@Operation(summary = "Inititalize checklists for new employees", description = "Trigger creation of checklists for all known new employees", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/initialize", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<EmployeeChecklistResponse> initiateEmployeeChecklists(
		@PathVariable @ValidMunicipalityId final String municipalityId) {

		return ok().body(employeeChecklistService.initiateEmployeeChecklists());
	}

	@Operation(summary = "Inititalize checklists for a specific employee", description = "Trigger creation of checklist for employee matching sent in person id", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/initialize/{personId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<EmployeeChecklistResponse> initiateSpecificEmployeeChecklist(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String personId) {

		return ok().body(employeeChecklistService.initiateSpecificEmployeeChecklist(personId));
	}
}
