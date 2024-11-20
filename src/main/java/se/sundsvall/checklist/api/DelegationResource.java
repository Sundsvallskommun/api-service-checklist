package se.sundsvall.checklist.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import se.sundsvall.checklist.api.model.DelegatedEmployeeChecklistResponse;
import se.sundsvall.checklist.service.DelegationService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/{municipalityId}/employee-checklists")
@Tag(name = "Delegation resources", description = "Resources for managing delegations of employee checklists")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class DelegationResource {

	private final DelegationService delegationService;

	DelegationResource(final DelegationService delegationService) {
		this.delegationService = delegationService;
	}

	@Operation(summary = "Delegate an employee checklist", description = "Delegate an employee checklist to a user by email and checklist id", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@PostMapping(value = "/{employeeChecklistId}/delegate-to/{email}", produces = ALL_VALUE)
	ResponseEntity<Void> delegateEmployeeChecklist(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@Parameter(name = "email", description = "Email for person to delegate to", example = "delegate.person@noreply.com") @PathVariable @Email final String email) {

		delegationService.delegateEmployeeChecklist(municipalityId, employeeChecklistId, email);
		return status(CREATED).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Fetch all employee checklists delegated to a user", description = "Fetch all delegated employee checklists for the user that matches sent in userid", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(value = "/delegated-to/{username}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<DelegatedEmployeeChecklistResponse> fetchDelegatedEmployeeChecklists(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "username", description = "Username to fetch delegations for", example = "usr123") @PathVariable final String username) {

		return ok(delegationService.fetchDelegatedEmployeeChecklistsByUsername(municipalityId, username));
	}

	@Operation(summary = "Remove delegation of employee checklist", description = "Remove the delegation of an employee checklist matching sent in email and checklist id", responses = {
		@ApiResponse(responseCode = "204", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	})
	@DeleteMapping(value = "/{employeeChecklistId}/delegated-to/{email}", produces = ALL_VALUE)
	ResponseEntity<Void> deleteEmployeeChecklistDelegation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId,
		@Parameter(name = "email", description = "Email for person to remove delegation from", example = "delegate.person@noreply.com") @PathVariable @Email final String email) {

		delegationService.removeEmployeeChecklistDelegation(municipalityId, employeeChecklistId, email);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}

}
