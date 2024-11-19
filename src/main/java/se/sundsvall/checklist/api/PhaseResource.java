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
import io.swagger.v3.oas.annotations.Parameter;
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
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/{municipalityId}/phases")
@Tag(name = "Phase resources", description = "Resources for managing the phases that will be used by all checklists")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class PhaseResource {

	private final PhaseService phaseService;

	PhaseResource(final PhaseService phaseService) {
		this.phaseService = phaseService;
	}

	@Operation(summary = "Fetch all phases")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@GetMapping(produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<List<Phase>> fetchChecklistPhases(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId) {

		final var phases = phaseService.getPhases(municipalityId);
		return ok(phases);
	}

	@Operation(summary = "Fetch single phase")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@GetMapping(value = "/{phaseId}", produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<Phase> fetchChecklistPhase(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId) {

		return ok(phaseService.getPhase(municipalityId, phaseId));
	}

	@Operation(summary = "Create a new phase")
	@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful Operation", useReturnTypeSchema = true)
	@PostMapping(produces = {
		ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	}, consumes = {
		APPLICATION_JSON_VALUE
	})
	ResponseEntity<Void> createChecklistPhase(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@RequestBody @Valid final PhaseCreateRequest request) {

		final var phase = phaseService.createPhase(municipalityId, request);
		return created(UriComponentsBuilder.fromPath("/{municipalityId}/phases/{phaseId}")
			.buildAndExpand(municipalityId, phase.getId())
			.toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Update an existing phase")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@PatchMapping(value = "/{phaseId}", produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	}, consumes = {
		APPLICATION_JSON_VALUE
	})
	ResponseEntity<Phase> updateChecklistPhase(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId,
		@RequestBody @Valid final PhaseUpdateRequest request) {

		return ok(phaseService.updatePhase(municipalityId, phaseId, request));
	}

	@Operation(summary = "Delete an existing phase")
	@ApiResponse(responseCode = "204", description = "Successful Operation", useReturnTypeSchema = true)
	@DeleteMapping(value = "/{phaseId}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> deleteChecklistPhase(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "phaseId", description = "Phase id", example = "9ee6a504-555f-4db7-bf21-2bb8a96f2b85") @PathVariable @ValidUuid final String phaseId) {

		phaseService.deletePhase(municipalityId, phaseId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}
}
