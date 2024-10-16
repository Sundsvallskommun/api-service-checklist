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
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.ChecklistCreateRequest;
import se.sundsvall.checklist.api.model.ChecklistUpdateRequest;
import se.sundsvall.checklist.service.ChecklistService;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/checklists")
@Tag(name = "Checklist resources", description = "Resources for managing checklists")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class ChecklistResource {

	private final ChecklistService checklistService;

	ChecklistResource(final ChecklistService checklistService) {
		this.checklistService = checklistService;
	}

	@Operation(summary = "Get all checklists", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<List<Checklist>> fetchAllChecklists() {
		return ok(checklistService.getAllChecklists());
	}

	@Operation(summary = "Fetch checklist by id", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(value = "/{checklistId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Checklist> fetchChecklistById(@PathVariable @ValidUuid final String checklistId) {
		return ok(checklistService.getChecklistById(checklistId));
	}

	@Operation(summary = "Create a new checklist", responses = {
		@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE }, consumes = { APPLICATION_JSON_VALUE })
	ResponseEntity<Void> createChecklist(
		@RequestBody @Valid final ChecklistCreateRequest request) {
		final var checklist = checklistService.createChecklist(request);
		return created(UriComponentsBuilder.fromPath("/checklists" + "/{checklistId}")
			.buildAndExpand(checklist.getId())
			.toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Create new version of checklist", responses = {
		@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/{checklistId}/version", produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> createNewVersion(@PathVariable @ValidUuid final String checklistId) {
		final var checklist = checklistService.createNewVersion(checklistId);
		return created(UriComponentsBuilder.fromPath("/checklists" + "/{checklistId}")
			.buildAndExpand(checklist.getId()).toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Activate checklist", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@PatchMapping(value = "/{checklistId}/activate", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Checklist> activateChecklist(@PathVariable @ValidUuid final String checklistId) {
		return ok(checklistService.activateChecklist(checklistId));
	}

	@Operation(summary = "Update a checklist", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@PatchMapping(value = "/{checklistId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }, consumes = { APPLICATION_JSON_VALUE })
	ResponseEntity<Checklist> updateChecklist(
		@PathVariable @ValidUuid final String checklistId,
		@RequestBody @Valid final ChecklistUpdateRequest request) {
		return ok(checklistService.updateChecklist(checklistId, request));
	}

	@Operation(summary = "Delete a checklist", responses = {
		@ApiResponse(responseCode = "204", description = "No Content", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@DeleteMapping(value = "/{checklistId}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> deleteChecklist(
		@PathVariable @ValidUuid final String checklistId) {
		checklistService.deleteChecklist(checklistId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}

}
