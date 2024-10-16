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
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.api.model.PhaseCreateRequest;
import se.sundsvall.checklist.api.model.PhaseUpdateRequest;
import se.sundsvall.checklist.service.PhaseService;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/checklists/{checklistId}/phases")
@Tag(name = "Phase resources", description = "Resources for managing phases in a checklist")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class PhaseResource {

	private final PhaseService phaseService;

	PhaseResource(final PhaseService phaseService) {
		this.phaseService = phaseService;
	}

	@Operation(summary = "Fetch all phases in a checklist")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<List<Phase>> fetchChecklistPhases(
		@PathVariable @ValidUuid final String checklistId) {
		var phases = phaseService.getChecklistPhases(checklistId);
		return ok(phases);
	}

	@Operation(summary = "Fetch phase in a checklist")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@GetMapping(value = "/{phaseId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Phase> fetchChecklistPhase(
		@PathVariable @ValidUuid final String checklistId,
		@PathVariable @ValidUuid final String phaseId) {
		return ok(phaseService.getChecklistPhase(checklistId, phaseId));
	}

	@Operation(summary = "Create phase in a checklist")
	@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful Operation", useReturnTypeSchema = true)
	@PostMapping(produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE }, consumes = { APPLICATION_JSON_VALUE })
	ResponseEntity<Void> createChecklistPhase(
		@PathVariable @ValidUuid final String checklistId,
		@RequestBody @Valid final PhaseCreateRequest request) {
		var phase = phaseService.createChecklistPhase(checklistId, request);
		return created(UriComponentsBuilder.fromPath("/checklists/{checklistId}/phases/{phaseId}")
			.buildAndExpand(checklistId, phase.getId())
			.toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Update phase in a checklist")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@PatchMapping(value = "/{phaseId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }, consumes = { APPLICATION_JSON_VALUE })
	ResponseEntity<Phase> updateChecklistPhase(
		@PathVariable @ValidUuid final String checklistId,
		@PathVariable @ValidUuid final String phaseId,
		@RequestBody @Valid final PhaseUpdateRequest request) {
		return ok(phaseService.updateChecklistPhase(checklistId, phaseId, request));
	}

	@Operation(summary = "Delete phase in a checklist")
	@ApiResponse(responseCode = "204", description = "Successful Operation", useReturnTypeSchema = true)
	@DeleteMapping(value = "/{phaseId}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> deleteChecklistPhase(
		@PathVariable @ValidUuid final String checklistId,
		@PathVariable @ValidUuid final String phaseId) {
		phaseService.deleteChecklistPhase(checklistId, phaseId);
		return noContent().build();
	}

}
