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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.checklist.api.model.Organization;
import se.sundsvall.checklist.api.model.OrganizationCreateRequest;
import se.sundsvall.checklist.api.model.OrganizationUpdateRequest;
import se.sundsvall.checklist.service.OrganizationService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@RequestMapping("/{municipalityId}/organizations")
@Tag(name = "Organization resources", description = "Resources for managing organizational units")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class OrganizationResource {

	private final OrganizationService organizationService;

	OrganizationResource(final OrganizationService organizationService) {
		this.organizationService = organizationService;
	}

	@Operation(summary = "Fetch all organizations, optionally filtered by organization number(s)", description = "Fetch all organizations", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<Organization>> fetchOrganizations(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "organizationFilter", description = "Filter response to only include organizations matching provided organization numbers") @RequestParam(required = false) final List<Integer> organizationFilter,
		@Parameter(name = "applySortFor", description = "Optional parameter for using custom sort based on provided organization number for response") @RequestParam(required = false) final Integer applySortFor) {

		return ok(organizationService.fetchAllOrganizations(municipalityId, organizationFilter, applySortFor));
	}

	@Operation(summary = "Fetch organization by id", description = "Fetch organization that matches provided id", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(value = "/{organizationId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Organization> fetchOrganizationById(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "organizationId", description = "Organization id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String organizationId,
		@Parameter(name = "applySortFor", description = "Optional parameter for using custom sort based on provided organization number for response") @RequestParam(required = false) final Integer applySortFor) {

		return ok(organizationService.fetchOrganization(municipalityId, organizationId, applySortFor));
	}

	@Operation(summary = "Create an organization", description = "Create a new organizational unit", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), useReturnTypeSchema = true),
		@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> createOrganization(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Valid @RequestBody final OrganizationCreateRequest request) {

		final var organizationId = organizationService.createOrganization(municipalityId, request);
		return created(
			UriComponentsBuilder.fromPath("/{municipalityId}/organizations/{organizationId}")
				.buildAndExpand(municipalityId, organizationId)
				.toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(summary = "Update an organization", description = "Update an existing organizational unit", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@PatchMapping(value = "/{organizationId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Organization> updateOrganization(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "organizationId", description = "Organization id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String organizationId,
		@Valid @RequestBody final OrganizationUpdateRequest request) {

		return ok(organizationService.updateOrganization(municipalityId, organizationId, request));
	}

	@Operation(summary = "Delete organization", description = "Delete an existing organizational unit", responses = {
		@ApiResponse(responseCode = "204", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@DeleteMapping(value = "/{organizationId}", produces = ALL_VALUE)
	ResponseEntity<Void> deleteOrganization(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "organizationId", description = "Organization id", example = "85fbcecb-62d9-40c4-9b3d-839e9adcfd8c") @PathVariable @ValidUuid final String organizationId) {

		organizationService.deleteOrganization(municipalityId, organizationId);
		return noContent().header(CONTENT_TYPE, ALL_VALUE).build();
	}
}
