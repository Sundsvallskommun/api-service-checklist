package se.sundsvall.checklist.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import se.sundsvall.checklist.integration.db.model.enums.RoleType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for checklist create request")
public class ChecklistCreateRequest {

	@Schema(description = "The name of the checklist", example = "Checklist_A")
	@NotBlank
	private String name;

	@Schema(description = "The display name of the checklist", example = "Display name")
	@NotBlank
	private String displayName;

	@Schema(description = "The organization that the checklist is created for", example = "11")
	@NotNull
	private Integer organizationNumber;

	@Schema(description = "The role type of the checklist")
	@NotNull
	private RoleType roleType;

	@Schema(description = "The id of the user creating the checklist")
	@NotBlank
	private String createdBy;
}
