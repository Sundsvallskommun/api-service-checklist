package se.sundsvall.checklist.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import se.sundsvall.checklist.api.model.Correspondence;
import se.sundsvall.checklist.service.CommunicationService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/{municipalityId}/employee-checklists")
@Tag(name = "Communication resources", description = "Resources for managing communication")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class CommunicationResource {

	private final CommunicationService communicationService;

	CommunicationResource(CommunicationService communicationService) {
		this.communicationService = communicationService;
	}

	@Operation(summary = "Send email notification", description = "Send an email notification to the manager for the employee checklist that matches provided id", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/{employeeChecklistId}/email")
	ResponseEntity<Void> sendEmail(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId) {

		communicationService.sendEmail(municipalityId, employeeChecklistId);
		return status(CREATED).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Fetch correspondence", description = "Fetch the correspondence that has occured for an employee checklist", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(value = "/{employeeChecklistId}/correspondence", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Correspondence> fetchCorrespondence(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "employeeChecklistId", description = "Employee checklist id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String employeeChecklistId) {

		return ok(communicationService.fetchCorrespondence(municipalityId, employeeChecklistId));
	}
}
