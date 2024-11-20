package se.sundsvall.checklist.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
import se.sundsvall.checklist.api.validation.ValidJson;
import se.sundsvall.checklist.service.PortingService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Tag(name = "Porting resources", description = "Resources for managing import and export of checklists")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
@Validated
class PortingResource {

	private final PortingService portingService;

	PortingResource(PortingService portingService) {
		this.portingService = portingService;
	}

	@Operation(summary = "Export checklist structure",
		description = "Returns complete structure for the checklist matching provided organizationNumber. If version if prodvided it will be matched, otherwise the latest version will be returned",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
		})

	@GetMapping(path = "/{municipalityId}/export/{organizationNumber}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<String> exportChecklist(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId String municipalityId,
		@Parameter(name = "organizationNumber", description = "Organization number", example = "53") @PathVariable Integer organizationNumber,
		@Parameter(name = "version", description = "Version", example = "2") @RequestParam(required = false) Integer version) {

		return ok(portingService.exportChecklist(municipalityId, organizationNumber, version));
	}

	@Operation(summary = "Import checklist structure for a organization as a new version with lifecycle status CREATED",
		description = """
			<i>This method is safe to use in regard to that no existing checklist(s) with lifecycle status <b>ACTIVE</b> or <b>CREATED</b> will be modified. I.e. employees with active or completed checklists will not be affected.</i>
			<br><br>
			The following rules are applied:
			- If no checklist exists for the organizational unit, a new version based on provided structure will be created with lifecycle status <b>CREATED</b> (to activate, use intended endpoint).
			- If an existing version with lifecycle status <b>CREATED</b> is present, an exception will be thrown as there can only be one checklist with created lifecycle status. Remove current version with created status or use the replace endpoint instead.
			- If an existing version with lifecycle status <b>ACTIVE</b> is present, a new version based on provided structure will be created with lifecycle status <b>CREATED</b> (to activate, use intended endpoint). The currently active version will remain intact and employees with active or completed checklists will not be affected.
			""",
		responses = {
			@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful Operation", useReturnTypeSchema = true)
		})

	@PostMapping(path = "/{municipalityId}/import/add/{organizationNumber}/{organizationName}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> importChecklistAsNewVersion(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId String municipalityId,
		@Parameter(name = "organizationNumber", description = "Organization number", example = "53") @PathVariable Integer organizationNumber,
		@Parameter(name = "organizationName", description = "Organization name", example = "Organization ABC") @PathVariable String organizationName,
		@Parameter(description = "Json checklist structure to import") @RequestBody @ValidJson String jsonStructure) {

		final var id = portingService.importChecklist(municipalityId, organizationNumber, organizationName, jsonStructure, false);
		return created(UriComponentsBuilder.fromPath("/{municipalityId}/checklists/{checklistId}")
			.buildAndExpand(municipalityId, id)
			.toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@Operation(summary = "Import checklist structure for a organization, replacing currently CREATED or ACTIVE version",
		description = """
			<h3>Only use this resource if you are aware of the impact the result of the operation has on active and completed employee checklists</h3>
			<i>This method is <b>not safe to use</b> in regard to that existing checklists with lifecycle status <b>ACTIVE</b> or <b>CREATED</b> will be replaced. I.e. employee checklists (ongoing and completed) and checklist drafts <u>will be affected</u>.</i>
			The following rules are applied:
			- If no checklist exists for the organizational unit, a new version based on provided structure will be created with lifecycle status <b>CREATED</b> (to activate, use intended endpoint).
			- If an existing version with lifecycle status <b>CREATED</b> is present, it will be replaced with the provided structure (to activate, use intended endpoint). If a version with lifecycle status <b>ACTIVE</b> exists, it will remain intact and no ongoing employee checklists will be affected. Only the currently created version draft will be replaced.
			- If an existing version with lifecycle status <b>ACTIVE</b> is present and no version with status <b>CREATED</b> is present, the active version will be replaced with the provided structure. <b>Observe that all employee checklists (ongoing and completed) using the currently active version will be affected (resetted). Use with uttermost care!</b>.
			""",
		responses = {
			@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful Operation", useReturnTypeSchema = true)
		})

	@PostMapping(path = "/{municipalityId}/import/replace/{organizationNumber}/{organizationName}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> importAndOverwriteExistingChecklist(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId String municipalityId,
		@Parameter(name = "organizationNumber", description = "Organization number", example = "53") @PathVariable Integer organizationNumber,
		@Parameter(name = "organizationName", description = "Organization name", example = "Organization ABC") @PathVariable String organizationName,
		@Parameter(description = "Json checklist structure to import") @RequestBody @ValidJson String jsonStructure) {

		final var id = portingService.importChecklist(municipalityId, organizationNumber, organizationName, jsonStructure, true);
		return created(UriComponentsBuilder.fromPath("/{municipalityId}/checklists/{checklistId}")
			.buildAndExpand(municipalityId, id)
			.toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}
}
