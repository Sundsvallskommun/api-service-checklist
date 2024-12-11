package se.sundsvall.checklist.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.accepted;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.checklist.api.model.SortorderRequest;
import se.sundsvall.checklist.service.SortorderService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@RequestMapping("/{municipalityId}/sortorder/{organizationNumber}")
@Tag(name = "Custom sort order resources", description = "Resources for managing custom sort order of checklist phases and tasks")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class CustomSortResource {
	private final SortorderService sortorderService;

	CustomSortResource(final SortorderService sortorderService) {
		this.sortorderService = sortorderService;
	}

	@Operation(summary = "Creates or replaces a custom sort order of checklist components for the provided organisation number", responses = {
		@ApiResponse(responseCode = "202", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PutMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> saveSortorder(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "organizationNumber", description = "Organization number to the organization owning the sort order", example = "587") @PathVariable final Integer organizationNumber,
		@Valid @RequestBody final SortorderRequest request) {

		sortorderService.saveSortorder(municipalityId, organizationNumber, request);
		return accepted().header(CONTENT_TYPE, ALL_VALUE).build();
	}
}
